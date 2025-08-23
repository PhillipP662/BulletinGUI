package bulletingui.bulletingui.Commen;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * SnapshotStore verzorgt de lokale recoverability van de ABB-WPES client.
 *
 * Functies:
 * - Beheert een lokale master key (AES-128) die enkel op het device wordt gebruikt
 *   om snapshots en journal entries versleuteld op te slaan.
 * - Slaat per peer een volledige Phonebook-state (outbound en inbound ChannelState)
 *   encrypted op als snapshot. Dit laat toe de communicatie-state te herstellen
 *   na een crash of herstart.
 * - Werkt met atomic writes (via *.tmp + ATOMIC_MOVE) om corrupte bestanden
 *   te vermijden.
 * - Houdt een mini-journal bij voor pending sends: berichten die reeds lokaal
 *   versleuteld zijn maar mogelijk nog niet op het BulletinBoard zijn geschreven.
 *   Deze kunnen na een crash idempotent opnieuw verstuurd worden.
 * - Biedt helpers om snapshots en pending sends te laden, en om journal bestanden
 *   te verwijderen na succesvolle replay.
 *
 * Deze klasse is essentieel om te voldoen aan de availability-eis van ABB-WPES:
 * een bericht dat eenmaal lokaal is aangeboden gaat niet definitief verloren,
 * zelfs niet bij crashes of herstarten van de client.
 */

public final class SnapshotStore {

    private final Path baseDir;           // bv. state/<clientId>/
    private final Path masterKeyPath;     // bv. state/<clientId>/master.key
    private final Path snapshotsDir;      // bv. state/<clientId>/snapshots/
    private final Path journalDir;        // bv. state/<clientId>/journal/
    private SecretKey masterKey;          // AES key voor opslag (alleen lokaal)

    public SnapshotStore(Path baseDir) throws Exception {
        this.baseDir = baseDir;
        this.masterKeyPath = baseDir.resolve("master.key");
        this.snapshotsDir = baseDir.resolve("snapshots");
        this.journalDir = baseDir.resolve("journal");
        Files.createDirectories(this.snapshotsDir);
        Files.createDirectories(this.journalDir);
        this.masterKey = loadOrCreateMasterKey();
    }

    /* ------------------------------------------------------------
       Master key management (AES-128) – lokal opslag (Base64)
       ------------------------------------------------------------ */

    private SecretKey loadOrCreateMasterKey() throws Exception {
        if (Files.exists(masterKeyPath)) {
            byte[] b64 = Files.readAllBytes(masterKeyPath);
            byte[] raw = Base64.getDecoder().decode(new String(b64, StandardCharsets.UTF_8).trim());
            return new SecretKeySpec(raw, "AES");
        } else {
            SecretKey mk = CryptoUtils.generateKey(); // AES-128
            String b64 = Base64.getEncoder().encodeToString(mk.getEncoded());
            atomicWrite(masterKeyPath, b64.getBytes(StandardCharsets.UTF_8));
            return mk;
        }
    }

    /* ------------------------------------------------------------
       Helpers
       ------------------------------------------------------------ */

    public static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private void atomicWrite(Path dest, byte[] data) throws IOException {
        Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".tmp");
        Files.write(tmp, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private Path snapshotFile(String peerId) {
        return snapshotsDir.resolve(sanitize(peerId) + ".snapshot");
    }

    private Path journalFile(String peerId) {
        return journalDir.resolve(sanitize(peerId) + ".pendingSend");
    }

    /* ------------------------------------------------------------
       Snapshot formaat (plaintext, daarna AES-GCM encrypted):
       v=1
       ts=<epochMillis>
       out.key=<Base64(AES)>
       out.id=<int>
       out.pre=<String>
       in.key=<Base64(AES)>
       in.id=<int>
       in.pre=<String>
       ------------------------------------------------------------ */

    public void saveSnapshot(String peerId, Phonebook pb) throws Exception {
        Objects.requireNonNull(pb, "phonebook");
        Phonebook.ChannelState ob = pb.getOutbound();
        Phonebook.ChannelState ib = pb.getInbound();
        if (ob == null || ib == null)
            throw new IllegalStateException("Snapshot requires both outbound and inbound states.");

        String outKey = Base64.getEncoder().encodeToString(ob.getKey().getEncoded());
        String inKey  = Base64.getEncoder().encodeToString(ib.getKey().getEncoded());

        long ts = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("v=1\n");
        sb.append("ts=").append(ts).append("\n");
        sb.append("out.key=").append(outKey).append("\n");
        sb.append("out.id=").append(ob.getCellId()).append("\n");
        sb.append("out.pre=").append(ob.getPreimage()).append("\n");
        sb.append("in.key=").append(inKey).append("\n");
        sb.append("in.id=").append(ib.getCellId()).append("\n");
        sb.append("in.pre=").append(ib.getPreimage()).append("\n");

        String enc = CryptoUtils.encryptAEAD(sb.toString(), masterKey);
        atomicWrite(snapshotFile(peerId), enc.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<Phonebook> tryLoadSnapshot(String peerId) throws Exception {
        Path f = snapshotFile(peerId);
        if (!Files.exists(f)) return Optional.empty();
        String enc = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        String pt  = CryptoUtils.decryptAEAD(enc, masterKey);

        String[] lines = pt.split("\\r?\\n");
        String outKeyB64=null, inKeyB64=null, outPre=null, inPre=null;
        Integer outId=null, inId=null;
        for (String line: lines) {
            if (line.startsWith("out.key=")) outKeyB64 = line.substring(8);
            else if (line.startsWith("out.id=")) outId = Integer.parseInt(line.substring(7));
            else if (line.startsWith("out.pre=")) outPre = line.substring(8);
            else if (line.startsWith("in.key=")) inKeyB64 = line.substring(7);
            else if (line.startsWith("in.id=")) inId = Integer.parseInt(line.substring(6));
            else if (line.startsWith("in.pre=")) inPre = line.substring(7);
        }
        if (outKeyB64==null || inKeyB64==null || outPre==null || inPre==null || outId==null || inId==null)
            return Optional.empty();

        SecretKey outKey = new SecretKeySpec(Base64.getDecoder().decode(outKeyB64), "AES");
        SecretKey inKey  = new SecretKeySpec(Base64.getDecoder().decode(inKeyB64), "AES");

        Phonebook.ChannelState outbound = new Phonebook.ChannelState(outKey, outId, outPre);
        Phonebook.ChannelState inbound  = new Phonebook.ChannelState(inKey, inId, inPre);
        return Optional.of(new Phonebook(outbound, inbound));
    }

    /* ------------------------------------------------------------
       Journal (pending send) – idempotent replay na crash:
       v=1
       curr.id=<int>
       curr.pre=<String>
       cipher=<String>    // de AEAD string die je naar bb.sendWithTag stuurde
       ------------------------------------------------------------ */

    public void writePendingSend(String peerId, int currId, String currPre, String ciphertext) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("v=1\n");
        sb.append("curr.id=").append(currId).append("\n");
        sb.append("curr.pre=").append(currPre).append("\n");
        sb.append("cipher=").append(ciphertext).append("\n");
        String enc = CryptoUtils.encryptAEAD(sb.toString(), masterKey);
        atomicWrite(journalFile(peerId), enc.getBytes(StandardCharsets.UTF_8));
    }

    public void clearPendingSend(String peerId) throws IOException {
        Path jf = journalFile(peerId);
        if (Files.exists(jf)) Files.delete(jf);
    }

    public Optional<PendingSend> tryLoadPendingSend(String peerId) throws Exception {
        Path jf = journalFile(peerId);
        if (!Files.exists(jf)) return Optional.empty();
        String enc = new String(Files.readAllBytes(jf), StandardCharsets.UTF_8);
        String pt  = CryptoUtils.decryptAEAD(enc, masterKey);

        Integer currId=null; String currPre=null, cipher=null;
        for (String line : pt.split("\\r?\\n")) {
            if (line.startsWith("curr.id=")) currId = Integer.parseInt(line.substring(8));
            else if (line.startsWith("curr.pre=")) currPre = line.substring(9);
            else if (line.startsWith("cipher=")) cipher = line.substring(7);
        }
        if (currId==null || currPre==null || cipher==null) return Optional.empty();
        return Optional.of(new PendingSend(currId, currPre, cipher));
    }

    public static final class PendingSend {
        public final int currId; public final String currPre; public final String cipher;
        public PendingSend(int id, String pre, String c) { this.currId=id; this.currPre=pre; this.cipher=c; }
    }
}
