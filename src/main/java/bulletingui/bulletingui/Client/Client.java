package bulletingui.bulletingui.Client;

import bulletingui.bulletingui.Commen.*;
import bulletingui.bulletingui.Server.BulletinBoardImpl;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static bulletingui.bulletingui.Commen.SnapshotStore.sanitize;


public class Client {

    private final String id;
    private final int NUM_CELLS = 10;

    private final DiffieHellmanUtils dh;
    private BulletinBoard bb;

    private Map<Client,Phonebook> phonebook = new HashMap<>();

    private final SnapshotStore snapshots;


    public Client(String id) throws Exception {
        this.id = id;
        this.dh = new DiffieHellmanUtils();
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        this.bb = (BulletinBoard) registry.lookup("BulletinBoard");

        this.snapshots = new SnapshotStore(Paths.get("state", sanitize(id)));
    }

    public String getId() {return id;}

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    public PublicKey getPublicKey() throws Exception {return dh.getPublicKey();}

    public Map<Client,Phonebook> getPhonebooks() {return phonebook;}

    private int randcell(){return new SecureRandom().nextInt(NUM_CELLS);}

    public void establishContactBidirectioneel(Client other) throws Exception {
        // 1) ECDH shared secret (ruw)
        byte[] shared = dh.deriveSharedSecret(other.getPublicKey());
        // 2) HKDF-Extract: let op de volgorde (salt, ikm)!
        byte[] salt = (id + "|" + other.getId()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] prk  = CryptoUtils.hkdfExtract(salt, shared);

        // 3) Gedetermineerde richtinglabels
        String low  = id.compareTo(other.getId()) <= 0 ? id : other.getId();
        String high = id.equals(low) ? other.getId() : id;

        // 4) Twee directionele AEAD-sleutels
        SecretKey k_low_to_high  = CryptoUtils.hkdfToAesKey(prk, (low  + "->" + high));
        SecretKey k_high_to_low  = CryptoUtils.hkdfToAesKey(prk, (high + "->" + low));

        // 5) Zorg dat phonebook entries bestaan
        phonebook.putIfAbsent(other, Phonebook.empty());
        other.getPhonebooks().putIfAbsent(this, Phonebook.empty());

        // 6) LOW -> HIGH (door low gekozen)
        int idxL  = randcell();
        String tagL = CryptoUtils.randomTagPreimage();

        if (id.equals(low)) {
            // Ik ben LOW: mijn outbound naar HIGH
            Phonebook.ChannelState myOutbound = new Phonebook.ChannelState(k_low_to_high, idxL, tagL);
            Phonebook.ChannelState peerInbound = new Phonebook.ChannelState(k_low_to_high, idxL, tagL);

            Phonebook myPB    = phonebook.get(other);
            Phonebook peerPB  = other.getPhonebooks().get(this);

            phonebook.put(other, new Phonebook(
                    myOutbound,
                    (myPB != null ? myPB.getInbound() : null)   // behoud bestaande inbound indien al gezet
            ));
            other.getPhonebooks().put(this, new Phonebook(
                    (peerPB != null ? peerPB.getOutbound() : null),
                    peerInbound
            ));
        } else {
            // Ik ben HIGH: mijn inbound van LOW wordt in de LOW-tak gezet; hier niets te doen.
        }

        // 7) HIGH -> LOW (door high gekozen)
        int idxH  = randcell();
        String tagH = CryptoUtils.randomTagPreimage();

        if (id.equals(high)) {
            // Ik ben HIGH: mijn outbound naar LOW
            Phonebook.ChannelState myOutbound = new Phonebook.ChannelState(k_high_to_low, idxH, tagH);
            Phonebook.ChannelState peerInbound = new Phonebook.ChannelState(k_high_to_low, idxH, tagH);

            Phonebook myPB    = phonebook.get(other);
            Phonebook peerPB  = other.getPhonebooks().get(this);

            phonebook.put(other, new Phonebook(
                    myOutbound,
                    (myPB != null ? myPB.getInbound() : null)
            ));
            other.getPhonebooks().put(this, new Phonebook(
                    (peerPB != null ? peerPB.getOutbound() : null),
                    peerInbound
            ));
        } else {
            // Ik ben LOW: mijn inbound van HIGH wordt in de HIGH-tak gezet; hier niets te doen.
        }
    }

    public void sendTo(Client peer, String message) throws Exception {
        Phonebook.ChannelState out = requireOutbound(peer);
        final int    currIdx = out.getCellId();
        final String currPre = out.getPreimage();
        final javax.crypto.SecretKey k = out.getKey();

        // Kies volgende coördinaten (random)
        final int nextIdx = new SecureRandom().nextInt(NUM_CELLS);
        final String nextPre = CryptoUtils.randomTagPreimage();

        // Bouw payload
        final String payload = message + " || " + nextIdx + " || " + nextPre;

        // Encrypt (AEAD)
        final String enc = CryptoUtils.encryptAEAD(payload, k);

        // Schrijf & update alleen bij succes
        try {
            bb.sendWithTag(currIdx, enc, currPre);                 // 1) remote write OK?

            out.setCellId(nextIdx);                                // 2) state shift
            out.setPreimage(nextPre);
            out.setKey(CryptoUtils.ratchet(k));                    // 3) ratchet richting-key

            // Snapshot na succesvolle update
            snapshots.saveSnapshot(peer.getId(), this.getPhonebooks().get(peer));

            // Journal opruimen
            snapshots.clearPendingSend(peer.getId());

            System.out.println("[SEND " + id + "->" + peer.getId() + "] wrote at cell " + currIdx +
                    "; next=(" + nextIdx + ", preimage...) ; key ratcheted.");
        } catch (Exception e) {
            // Geen updates bij failure
            System.err.println("[SEND " + id + "->" + peer.getId() + "] FAILED: " + e.getMessage());
            throw e;
        }
    }


    public String receiveFrom(Client peer) throws Exception {
        Phonebook.ChannelState in = requireInbound(peer);
        final int    currIdx = in.getCellId();
        final String currPre = in.getPreimage();
        final javax.crypto.SecretKey k = in.getKey();

        // Ophalen
        final String enc = bb.retrieveWithTag(currIdx, currPre);
        if (enc == null || "⊥".equals(enc)) {
            return null; // niets te ontvangen
        }

        // Decrypt
        final String payload = CryptoUtils.decryptAEAD(enc, k);

        // Parse
        final String[] parts = payload.split("\\|\\|", -1);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid payload format.");
        final String msg = parts[0].trim();
        final int nextIdx = Integer.parseInt(parts[1].trim());
        final String nextPre = parts[2].trim();

        // Succesvol: state verschuiven + ratchet key
        in.setCellId(nextIdx);
        in.setPreimage(nextPre);
        in.setKey(CryptoUtils.ratchet(k));

        snapshots.saveSnapshot(peer.getId(), this.getPhonebooks().get(peer));

        System.out.println("[RECV " + id + " <- " + peer.getId() + "] read at cell " + currIdx +
                "; next=(" + nextIdx + ", preimage...) ; key ratcheted.");

        return msg;
    }
    public void recoverFromDiskWith(Client peer) throws Exception {
        // 1) Pending send replay (idempotent)
        snapshots.tryLoadPendingSend(peer.getId()).ifPresent(p -> {
            try {
                bb.sendWithTag(p.currId, p.cipher, p.currPre); // safe: overschrijft dezelfde tag-entry
                snapshots.clearPendingSend(peer.getId());
                System.out.println("[RECOVER] Replayed pending send to " + peer.getId() +
                        " at cell " + p.currId + " (preimage hidden).");
            } catch (Exception e) {
                System.err.println("[RECOVER] Failed to replay pending send: " + e.getMessage());
            }
        });

        // 2) Snapshot restore (overschrijft huidige in-memory state)
        snapshots.tryLoadSnapshot(peer.getId()).ifPresent(pb -> {
            this.getPhonebooks().put(peer, pb);
            System.out.println("[RECOVER] Restored snapshot for peer " + peer.getId());
        });
    }


    // HELPER_FUNCTIONS

    private static String computeTagFromPreimage(String preimage) {
        if (preimage == null) return "null";
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(preimage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "<err:" + e.getMessage() + ">";
        }
    }
    private Phonebook.ChannelState requireOutbound(Client peer) {
        Phonebook pb = phonebook.get(peer);
        if (pb == null || pb.getOutbound() == null) {
            throw new IllegalStateException("Outbound state towards " + peer.getId() + " is not initialized.");
        }
        return pb.getOutbound();
    }

    private Phonebook.ChannelState requireInbound(Client peer) {
        Phonebook pb = phonebook.get(peer);
        if (pb == null || pb.getInbound() == null) {
            throw new IllegalStateException("Inbound state from " + peer.getId() + " is not initialized.");
        }
        return pb.getInbound();
    }


    // DEBUG
    public void debugPrintState(Client peer) {
        System.out.println("========== STATE of " + this.id + " wrt peer " + peer.id + " ==========");
        Phonebook pb = phonebook.get(peer);
        if (pb == null) {
            System.out.println("no phonebook entry for peer");
            System.out.println("================================================================");
            return;
        }

        Phonebook.ChannelState ob = pb.getOutbound();
        Phonebook.ChannelState ib = pb.getInbound();

        // OUTBOUND (dit -> peer)
        System.out.println("Outbound (" + this.id + " -> " + peer.id + ")");
        if (ob == null) {
            System.out.println("  <null>");
        } else {
            String tagHash = computeTagFromPreimage(ob.getPreimage());
            String kB64 = (ob.getKey() != null) ? java.util.Base64.getEncoder().encodeToString(ob.getKey().getEncoded()) : "null";
            System.out.println("  sendCellId      : " + ob.getCellId());
            System.out.println("  sendPreimage    : " + ob.getPreimage());
            System.out.println("  sendTag = B(b)  : " + tagHash);
            System.out.println("  sendKey(Base64) : " + kB64);
        }

        // INBOUND (peer -> dit)
        System.out.println("Inbound (" + peer.id + " -> " + this.id + ")");
        if (ib == null) {
            System.out.println("  <null>");
        } else {
            String tagHash = computeTagFromPreimage(ib.getPreimage());
            String kB64 = (ib.getKey() != null) ? java.util.Base64.getEncoder().encodeToString(ib.getKey().getEncoded()) : "null";
            System.out.println("  recvCellId      : " + ib.getCellId());
            System.out.println("  recvPreimage    : " + ib.getPreimage());
            System.out.println("  recvTag = B(b)  : " + tagHash);
            System.out.println("  recvKey(Base64) : " + kB64);
        }

        System.out.println("================================================================");
    }







//    private final String id; // Client's unique identifier
//    private String firstTag; // Initial random tag
//    private int firstcellId; // Initial random cell ID
//
//    private Map<Client, Phonebook> phonebook; // Map of clientId -> ClientState
//
//    private DiffieHellmanUtils dhUtils;
//    private PublicKey otherClientPublicKey;
//    private SecretKey sessionKey; // Ephemeral session key
//    private BulletinBoard bulletinBoard;
//
//
//
//
//    public Client(String id,SecretKey SharedKey) throws Exception {
//        this.id = id;
//        this.firstTag = generateRandomTag();
//        this.firstcellId = generateRandomCellId(10); // Assuming 10 cells
//        this.dhUtils = new DiffieHellmanUtils();
//        Registry registry = LocateRegistry.getRegistry("localhost",1099);
//        this.bulletinBoard = (BulletinBoard) registry.lookup("BulletinBoard");
//
//    }
//
//    public PublicKey getPublicKey() {
//        return dhUtils.getPublicKey();
//    }
//
//    public void setOtherClientPublicKey(PublicKey otherPublicKey) throws Exception {
//        this.otherClientPublicKey = otherPublicKey;
//        this.sessionKey = dhUtils.deriveSharedSecret(otherPublicKey);
//    }
//
//    public SecretKey getSessionKey() {
//        return this.sessionKey;
//    }
//
//
//    public Map<Client, Phonebook> getPhonebook() {
//        return phonebook;
//    }
//
//    private String generateRandomTag() {
//        return UUID.randomUUID().toString();
//    }
//
//    // Generate a random cell ID
//    private int generateRandomCellId(int numCells) {
//        Random random = new Random();
//        return random.nextInt(numCells); // Random ID between 0 and numCells-1
//    }
//
//    public void setPhonebook(Map<Client, Phonebook> phonebook) {
//        this.phonebook = phonebook;
//    }
//
//    public void InitiliaseClient(Client otherClient) throws Exception {
//        // Ensure the phonebook is initialized
//        if (this.phonebook == null) {
//            this.phonebook = new HashMap<>();
//        }
//
//        if (otherClient.getPhonebook() == null) {
//            otherClient.setPhonebook(new HashMap<>());
//        }
//
//        // Generate random sending Cell ID and Tag
//        int sendCellId = generateRandomCellId(10);
//        String sendTag = generateRandomTag();
//
//        // Create a Phonebook entry for the current client
//        Phonebook phonebookEntry = new Phonebook(sendTag, sendCellId, CryptoUtils.generateKey());
//        this.phonebook.put(otherClient, phonebookEntry);
//
//        // Link the other's receiving data to this client's sending data
//        otherClient.setRecieveId(otherClient, sendCellId);
//        otherClient.setRecieveTag(otherClient, sendTag);
//    }
//
//
//
//
//
//    // Get the current tag for a specific client
//    public String getSendTag(Client client) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        return phonebook1 != null ? phonebook1.getSendtag(): null;
//
//    }
//
//    public void updatePhonebook(Client otherClient, Map<Client, Phonebook> otherClientPhonebook) {
//        // Ensure this client's phonebook is initialized
//        if (this.phonebook == null) {
//            throw new IllegalStateException("This client's phonebook is not initialized.");
//        }
//
//        // Ensure the other client's phonebook contains an entry for this client
//        Phonebook otherPhonebookEntry = otherClientPhonebook.get(this);
//        if (otherPhonebookEntry == null) {
//            throw new IllegalStateException("Other client's phonebook does not contain an entry for this client.");
//        }
//
//        // Update this client's receiving metadata using the other client's sending data
//        int celid = otherPhonebookEntry.getSendCellId();
//        String tag = otherPhonebookEntry.getSendtag();
//        setRecieveTag(otherClient,tag);
//        setRecieveId(otherClient,celid);
//
//    }
//
//    // Get the current cell ID for a specific client
//    public int getSendCellId(Client client) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        return phonebook1 != null ? phonebook1.getSendCellId() : null;
//    }
//
//
//    public String getRecieveTag(Client client){
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        return phonebook1 != null ? phonebook1.getRecieveTag() : null;
//    }
//
//    public int getRecieveId(Client client){
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//        phonebook1.getRecieveCellId();
//        return phonebook1 != null ? phonebook1.getRecieveCellId() : null;
//    }
//
//
//
//    // Setter for send tag
//    public void setSendTag(Client client, String sendTag) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        if (phonebook1 != null) {
//            phonebook1.setSendtag(sendTag);
//        }
//    }
//
//    // Setter for send cell ID
//    public void setSendCellId(Client client, int sendCellId) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        if (phonebook1 != null) {
//            phonebook1.setSendCellId(sendCellId);
//        }
//    }
//
//    // Setter for receive tag
//    public void setRecieveTag(Client client, String recieveTag) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        if (phonebook1 != null) {
//            phonebook1.setRecieveTag(recieveTag);
//
//        }
//    }
//
//    // Setter for receive cell ID
//    public void setRecieveId(Client client, int recieveCellId) {
//        Map<Client, Phonebook> phonebookMap = getPhonebook();
//        Phonebook phonebook1 = phonebookMap.get(client);
//
//        if (phonebook1 != null) {
//            phonebook1.setRecieveCellId(recieveCellId);
//        }
//    }
//
//
//
//    public void send( Client client, String message) throws Exception {
//        SecretKey sessionKey = this.getSessionKey();
//
//        int Sendcellid = getSendCellId(client);
//        String sendTag = getSendTag(client);
//
//        int NewSendCellid = generateRandomCellId(10) ;
//        String NewSendTag = generateRandomTag();
//
//        setSendCellId(client,NewSendCellid);
//        setSendTag(client,NewSendTag);
//
//        String macinput = createMessage(message,NewSendCellid,NewSendTag);
//
//        String mac = CryptoUtils.generateHMAC(sessionKey,macinput);
//
//        String plaintext = macinput + " || " + mac;
//
//
//        String encryptedFirstMessage = CryptoUtils.encrypt(plaintext, sessionKey);
//
//        bulletinBoard.sendWithTag(Sendcellid, encryptedFirstMessage, sendTag);
//        System.out.println("Transmission succesfull");
//
//    }
//
//
//    public String recieve( Client client) throws Exception {
//        SecretKey sessionKey = this.getSessionKey();
//        int cellid = getRecieveId(client);
//        String tag = getRecieveTag(client);
//
//        String encryptedRetrievedMessage = bulletinBoard.retrieveWithTag(cellid,tag);
//        String decryptedRetrievedMessage = CryptoUtils.decrypt(encryptedRetrievedMessage, sessionKey);
//
//
//        int nextCellId = extractCellId(decryptedRetrievedMessage);
//        String nexttag = extractNextPreimage(decryptedRetrievedMessage);
//        String hash = extractHash(decryptedRetrievedMessage);
//        String message = removeCellIdAndTag(decryptedRetrievedMessage);
//
//        String macinput = createMessage(message,nextCellId,nexttag);
//        String recomputedmac = CryptoUtils.generateHMAC(sessionKey,macinput);
//
//        if (!hash.equals(recomputedmac)) {
//            throw new SecurityException("MAC verification failed! Message tampered.");
//        }
//
//        setRecieveId(client,nextCellId);
//        setRecieveTag(client,nexttag);
//
//        System.out.println("Bob receives: " + message);
//        return message; // Return the plaintext message
//    }
//
//
//
//    public static String removeCellIdAndTag(String message) {
//        String[] parts = message.split("\\|\\|");
//        if (parts.length >= 4) {
//            return parts[0].trim(); // Return only the main message (first part)
//        }
//        throw new IllegalArgumentException("Invalid message format: Cannot remove cell ID, tag, and hash.");
//    }
//
//
//    private static int extractCellId(String message) {
//        String[] parts = message.split("\\|\\|");
//        if (parts.length >= 2) {
//            try {
//                return Integer.parseInt(parts[1].trim());
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid cell ID: " + parts[1].trim());
//            }
//        }
//        throw new IllegalArgumentException("Invalid message format: Cannot extract cell ID.");
//    }
//
//    // Extract the next preimage (tag) from the message
//    private static String extractNextPreimage(String message) {
//        String[] parts = message.split("\\|\\|");
//        if (parts.length >= 3) {
//            return parts[2].trim();
//        }
//        throw new IllegalArgumentException("Invalid message format: Cannot extract next preimage.");
//    }
//    private static String extractHash(String message) {
//        String[] parts = message.split("\\|\\|");
//        if (parts.length >= 4) {
//            return parts[3].trim();
//        }
//        throw new IllegalArgumentException("Invalid message format: Cannot extract hash.");
//    }
//    public static String createMessage(String value, int cellId, String preimage) {
//        return value + " || " + cellId + " || " + preimage;
//    }
//
//    // Uitbreiding 1: Recoverabilty







}
