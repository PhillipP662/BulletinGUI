package bulletingui.bulletingui.Commen;

import bulletingui.bulletingui.Client.Client;
import bulletingui.bulletingui.Server.BulletinBoardImpl;

import javax.crypto.SecretKey;
import java.util.HashMap;
public class BulletinBoardTest {
    public static void main(String[] args) {
        try {
            //Setup
            //////////////////////
            //main code
            SecretKey sharedKey = CryptoUtils.generateKey();

            // Create clients
            Client Alice = new Client("Alice",sharedKey);
            Client Bob = new Client("Bob",sharedKey);

            // Initialize phonebooks
            Alice.InitiliaseClient(Bob);
            Bob.InitiliaseClient(Alice);
            Alice.updatePhonebook(Bob, Bob.getPhonebook());
            Bob.updatePhonebook(Alice, Alice.getPhonebook());

            // Create Privat keys
            Alice.setOtherClientPublicKey(Bob.getPublicKey());
            Bob.setOtherClientPublicKey(Alice.getPublicKey());

            ///////////////////////////////////////////

            //Sending & Recieving
            ///////////////////////////////////////
            // Send message
            String message1 = "Test inschalla";
            Alice.send(Bob,message1);
            System.out.println("bericht1: "+message1 );

            // recieve Message
            String message = Bob.recieve(Alice);
            System.out.println("Ontvangen bericht: "+ message);

            ///////////////////////////////////////////////////////////////////

            //Uitbreiding 1
            /////////////////////////////////////////

            // Test Recoverabilty - Static Backup
/*          try {

            String backupFilePath = "backup.dat";
            BulletinBoardImpl board = new BulletinBoardImpl(10, backupFilePath);
            board.sendWithTag(1, "Message 1", "tag1");
            board.sendWithTag(2, "Message 2", "tag2");

            System.out.println("Initial Messages:");
            board.saveState(backupFilePath);

            // Integrity Test
            board.saveState(backupFilePath);
            System.out.println("State saved to backup.");
            String stateHash = board.computeStateHash();
            System.out.println("Computed state hash: " + stateHash);
            boolean isValid = board.validateBackup(backupFilePath, stateHash);
            System.out.println("Backup validation result: " + isValid);


            // Corrupt: Simulate data loss
            System.out.println("Simulating corruption...");
            board.retrieveWithTag(1, "tag1"); //    Remove Message 1
            board.retrieveWithTag(2, "tag2"); // Remove Message 2

            // Recover: Load state
            board.loadState(backupFilePath);
            System.out.println("State restored from backup.");

            // Verify restored data
            System.out.println("Restored Messages:");
            System.out.println("Cell 1: " + board.retrieveWithTag(1, "tag1"));
            System.out.println("Cell 2: " + board.retrieveWithTag(2, "tag2"));

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            // Test Recoverabilty - Periodic Backup

            /*String backupFilePath = "backup.dat";
            BulletinBoardImpl board = new BulletinBoardImpl(10, backupFilePath);
            board.sendWithTag(1, "Message 1", "tag1");
            board.sendWithTag(2, "Message 2", "tag2");
            // Start periodic backup
            board.startPeriodicBackup(backupFilePath, 1); // Every 1 minute

            // Modify the bulletin board
            board.sendWithTag(3, "Periodic Message", "tag3");
            System.out.println("Added message during periodic backup.");

            // Wait for the backup to trigger
            Thread.sleep(2 * 60 * 1000); // Wait 2 minutes

            // Stop periodic backup
            board.stopPeriodicBackup();
            System.out.println("Stopped periodic backup.");

            // Verify backup
            board.loadState(backupFilePath);
            System.out.println("State after periodic backup:");
            System.out.println("Cell 3: " + board.retrieveWithTag(3, "tag3"));

            String stateHash = board.computeStateHash();
            System.out.println("Computed state hash: " + stateHash);
            boolean isValid = board.validateBackup(backupFilePath, stateHash);
            System.out.println("Backup validation result: " + isValid);*/




        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
