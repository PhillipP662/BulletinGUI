package bulletingui.bulletingui.Server;

import java.security.MessageDigest;
import java.util.*;

public class BulletinBoardImpl {
    private final int numCells;
    private final List<Map<String, String>> cells; // Each cell stores 〈t, message〉 pairs

    public BulletinBoardImpl(int numCells) {
        this.numCells = numCells;
        this.cells = new ArrayList<>();
        for (int i = 0; i < numCells; i++) {
            cells.add(new HashMap<>()); // Initialize each cell
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
    public void sendWithTag(int cellId, String message, String preimage) {
        int normalizedCellId = normalizeCellId(cellId);
        String tag = computeTag(preimage); // Compute the tag
        cells.get(normalizedCellId).put(tag, message); // Store the message with the tag
        System.out.println("Message stored in cell " + normalizedCellId + " with tag " + tag);
    }

    // Retrieve a message from the bulletin board
    public String retrieveWithTag(int cellId, String preimage) {
        int normalizedCellId = normalizeCellId(cellId);
        String tag = computeTag(preimage); // Compute the tag from the preimage
        Map<String, String> cell = cells.get(normalizedCellId);

        if (cell != null && cell.containsKey(tag)) {
            String message = cell.remove(tag); // Retrieve and remove the message
            System.out.println("Message retrieved from cell " + normalizedCellId + ": " + message);
            return message;
        }
        System.out.println("No message found for tag in cell " + normalizedCellId);
        return "⊥"; // Return ⊥ if no message is found
    }

    // Normalize cell IDs to handle wraparound
    private int normalizeCellId(int cellId) {
        return (cellId % numCells + numCells) % numCells;
    }
}