package bulletingui.bulletingui.Client;

import bulletingui.bulletingui.Commen.CryptoUtils;
import bulletingui.bulletingui.Commen.Phonebook;
import bulletingui.bulletingui.Server.BulletinBoardImpl;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Client {
    private final String id; // Client's unique identifier
    private String firstTag; // Initial random tag
    private int firstcellId; // Initial random cell ID

    private SecretKey secretKey;

    private Map<Client, Phonebook> phonebook; // Map of clientId -> ClientState

    public Client(String id, SecretKey SharedKey) {
        this.id = id;
        this.firstTag = generateRandomTag();
        this.firstcellId = generateRandomCellId(10); // Assuming 10 cells
        this.secretKey = SharedKey;

    }


    public Map<Client, Phonebook> getPhonebook() {
        return phonebook;
    }

    private String generateRandomTag() {
        return UUID.randomUUID().toString();
    }

    // Generate a random cell ID
    private int generateRandomCellId(int numCells) {
        Random random = new Random();
        return random.nextInt(numCells); // Random ID between 0 and numCells-1
    }

    public void setPhonebook(Map<Client, Phonebook> phonebook) {
        this.phonebook = phonebook;
    }

    public void InitiliaseClient(Client otherClient) throws Exception {
        // Ensure the phonebook is initialized
        if (this.phonebook == null) {
            this.phonebook = new HashMap<>();
        }

        if (otherClient.getPhonebook() == null) {
            otherClient.setPhonebook(new HashMap<>());
        }

        // Generate random sending Cell ID and Tag
        int sendCellId = generateRandomCellId(10);
        String sendTag = generateRandomTag();

        // Create a Phonebook entry for the current client
        Phonebook phonebookEntry = new Phonebook(sendTag, sendCellId, CryptoUtils.generateKey());
        this.phonebook.put(otherClient, phonebookEntry);

        // Link the other's receiving data to this client's sending data
        otherClient.setRecieveId(otherClient, sendCellId);
        otherClient.setRecieveTag(otherClient, sendTag);
    }





    // Get the current tag for a specific client
    public String getSendTag(Client client) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        return phonebook1 != null ? phonebook1.getSendtag(): null;

    }

    public void updatePhonebook(Client otherClient, Map<Client, Phonebook> otherClientPhonebook) {
        // Ensure this client's phonebook is initialized
        if (this.phonebook == null) {
            throw new IllegalStateException("This client's phonebook is not initialized.");
        }

        // Ensure the other client's phonebook contains an entry for this client
        Phonebook otherPhonebookEntry = otherClientPhonebook.get(this);
        if (otherPhonebookEntry == null) {
            throw new IllegalStateException("Other client's phonebook does not contain an entry for this client.");
        }

        // Update this client's receiving metadata using the other client's sending data
        int celid = otherPhonebookEntry.getSendCellId();
        String tag = otherPhonebookEntry.getSendtag();
        setRecieveTag(otherClient,tag);
        setRecieveId(otherClient,celid);

    }

    // Get the current cell ID for a specific client
    public int getSendCellId(Client client) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        return phonebook1 != null ? phonebook1.getSendCellId() : null;
    }


    public String getRecieveTag(Client client){
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        return phonebook1 != null ? phonebook1.getRecieveTag() : null;
    }

    public int getRecieveId(Client client){
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);
        phonebook1.getRecieveCellId();
        return phonebook1 != null ? phonebook1.getRecieveCellId() : null;
    }

    public SecretKey getSecretkey(Client client){
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        return phonebook1 != null ? phonebook1.getSecretKey() : null;
    }




    // Setter for send tag
    public void setSendTag(Client client, String sendTag) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        if (phonebook1 != null) {
            phonebook1.setSendtag(sendTag);
        }
    }

    // Setter for send cell ID
    public void setSendCellId(Client client, int sendCellId) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        if (phonebook1 != null) {
            phonebook1.setSendCellId(sendCellId);
        }
    }

    // Setter for receive tag
    public void setRecieveTag(Client client, String recieveTag) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        if (phonebook1 != null) {
            phonebook1.setRecieveTag(recieveTag);

        }
    }

    // Setter for receive cell ID
    public void setRecieveId(Client client, int recieveCellId) {
        Map<Client, Phonebook> phonebookMap = getPhonebook();
        Phonebook phonebook1 = phonebookMap.get(client);

        if (phonebook1 != null) {
            phonebook1.setRecieveCellId(recieveCellId);
        }
    }



    public void send(BulletinBoardImpl bulletinBoard, Client client, String message) throws Exception {
        int Sendcellid = getSendCellId(client);
        String sendTag = getSendTag(client);
        SecretKey secretKey = this.secretKey;

        int NewSendCellid = generateRandomCellId(10) ;
        String NewSendTag = generateRandomTag();

        setSendCellId(client,NewSendCellid);
        setSendTag(client,NewSendTag);

        String Newmessage = createMessage(message,NewSendCellid,NewSendTag);

        String encryptedFirstMessage = CryptoUtils.encrypt(Newmessage, secretKey);

        bulletinBoard.sendWithTag(Sendcellid, encryptedFirstMessage, sendTag);
        System.out.println("Transmission succesfull");

    }


    public String recieve(BulletinBoardImpl bulletinBoard, Client client) throws Exception {
        int cellid = getRecieveId(client);
        String tag = getRecieveTag(client);
        SecretKey secretKey = this.secretKey;

        String encryptedRetrievedMessage = bulletinBoard.retrieveWithTag(cellid,tag);
        String decryptedRetrievedMessage = CryptoUtils.decrypt(encryptedRetrievedMessage, secretKey);

        int nextCellId = extractCellId(decryptedRetrievedMessage);
        String nexttag = extractNextPreimage(decryptedRetrievedMessage);

        setRecieveId(client,nextCellId);
        setRecieveTag(client,nexttag);

        return removeCellIdAndTag(decryptedRetrievedMessage);
    }

    public static String removeCellIdAndTag(String message) {
        String[] parts = message.split("\\|\\|");
        if (parts.length >= 1) {
            return parts[0].trim(); // Return the value part only
        }
        throw new IllegalArgumentException("Invalid message format: Cannot remove cell ID and tag.");
    }


    private static int extractCellId(String message) {
        String[] parts = message.split("\\|\\|");
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cell ID: " + parts[1].trim());
            }
        }
        throw new IllegalArgumentException("Invalid message format: Cannot extract cell ID.");
    }

    // Extract the next preimage (tag) from the message
    private static String extractNextPreimage(String message) {
        String[] parts = message.split("\\|\\|");
        if (parts.length >= 3) {
            return parts[2].trim();
        }
        throw new IllegalArgumentException("Invalid message format: Cannot extract next preimage.");
    }

    public static String createMessage(String value, int cellId, String preimage) {
        return value + " || " + cellId + " || " + preimage;
    }

}
