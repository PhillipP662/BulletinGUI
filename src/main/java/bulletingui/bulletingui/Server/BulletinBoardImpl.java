package bulletingui.bulletingui.Server;

import bulletingui.bulletingui.Commen.BulletinBoard;
import bulletingui.bulletingui.Commen.CryptoUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * BulletinBoardImpl is de server-side implementatie van het publieke bulletin board
 * waarop clients berichten kunnen plaatsen en ophalen.
 *
 * Functies:
 * - Beheert een vaste set van cells, waarin elk bericht wordt opgeslagen als
 *   〈tag, message〉-paar. De tag = SHA-256(preimage) garandeert dat alleen de
 *   juiste ontvanger het bericht kan verwijderen.
 * - Implementatie van de RMI-interface BulletinBoard:
 *   - sendWithTag(cellId, message, preimage): slaat een bericht op in de opgegeven cel.
 *   - retrieveWithTag(cellId, preimage): verwijdert en retourneert een bericht
 *     indien de tag overeenkomt, anders ⊥.
 * - Ondersteunt recoverability via state-serialisatie naar disk en periodieke
 *   automatische back-ups.
 * - Biedt functies voor integriteitscontrole van de board-state via SHA-256 hashes
 *   en validatie van back-upbestanden.
 *
 * Opzet:
 * - Wordt als RMI-remote object geëxporteerd zodat clients op afstand
 *   cellen kunnen benaderen.
 * - Volgt het ABB-WPES ontwerp: de server is "honest but curious" en mag berichten
 *   opslaan, maar leert de inhoud pas als de preimage van de tag bekend is.
 */

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {
    private final int numCells;
    private final List<Map<String, String>> cells; // Each cell stores 〈t, message〉 pairs

    private ScheduledExecutorService backupExecutor;


    public BulletinBoardImpl(int numCells,String backupFilePath) throws RemoteException {
        super();
        this.numCells = numCells;
        this.cells = new ArrayList<>();
        for (int i = 0; i < numCells; i++) {
            cells.add(new HashMap<>()); // Initialize each cell
        }
        try {
            this.loadState(backupFilePath);
            System.out.println("Loaded state from backup.");
        } catch (Exception e) {
            System.err.println("Failed to load state, initializing new board: " + e.getMessage());
        }

    }

    // Compute the tag t = B(preimage)
    private String computeTag(String preimage) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(preimage.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error computing tag", e);
        }
    }

    // Store a message in the bulletin board
    @Override
    public void sendWithTag(int cellId, String message, String preimage) {
        int normalizedCellId = normalizeCellId(cellId);
        String tag = computeTag(preimage); // Compute the tag
        cells.get(normalizedCellId).put(tag, message); // Store the message with the tag
        System.out.println("Message stored in cell " + normalizedCellId + " with tag " + tag);
    }

    // Retrieve a message from the bulletin board
    @Override
    public String retrieveWithTag(int cellId, String preimage) {
        int normalizedCellId = normalizeCellId(cellId);
        String tag = computeTag(preimage); // Compute the tag from the preimage

        Map<String, String> targetCell = cells.get(normalizedCellId);

        if (targetCell != null && targetCell.containsKey(tag)) {
            String message = targetCell.remove(tag); // Retrieve and remove the message
            System.out.println("Message retrieved from cell " + normalizedCellId + ": " + message);
            return message;
        }

        // If not found, iterate over all cells in the list
        for (int i = 0; i < cells.size(); i++) {
            if (i == normalizedCellId) {
                continue; // Skip the already checked cell
            }
            Map<String, String> cell = cells.get(i);
            if (cell != null && cell.containsKey(tag)) {
                String message = cell.remove(tag); // Retrieve and remove the message
                System.out.println("Message retrieved from cell " + i + ": " + message);
                return message;
            }
        }

        // If the tag is not found in any cell
        System.out.println("No message found for tag in any cell");
        return "⊥"; // Return ⊥ if no message is found
    }

    // Normalize cell IDs to handle wraparound
    private int normalizeCellId(int cellId) {
        return (cellId % numCells + numCells) % numCells;
    }

    // uitbreiding Recoverability
    public void saveState(String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this.cells); // Serialize the cells list
        }
    }
    public void loadState(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            List<Map<String, String>> loadedCells = (List<Map<String, String>>) in.readObject();
            this.cells.clear(); // Clear current content
            this.cells.addAll(loadedCells); // Add all deserialized data to the existing final list
        }
    }

    // Periodieke Backup
    public void startPeriodicBackup(String filePath, int intervalMinutes) {
        this.backupExecutor = Executors.newScheduledThreadPool(1);
        this.backupExecutor.scheduleAtFixedRate(() -> {
            try {
                saveState(filePath);
                System.out.println("Backup saved successfully.");
            } catch (IOException e) {
                System.err.println("Failed to save backup: " + e.getMessage());
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    public void stopPeriodicBackup() {
        if (this.backupExecutor != null) {
            this.backupExecutor.shutdown();
        }
    }

    // Integrity Validation
    public String computeStateHash() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteStream);
        out.writeObject(this.cells);
        byte[] hash = digest.digest(byteStream.toByteArray());
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean validateBackup(String filePath, String expectedHash) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            byte[] fileBytes = fileStream.readAllBytes();
            byte[] computedHash = digest.digest(fileBytes);
            return Base64.getEncoder().encodeToString(computedHash).equals(expectedHash);
        }
    }


}