package bulletingui.bulletingui.Commen;

import bulletingui.bulletingui.Client.Client;
import bulletingui.bulletingui.Server.BulletinBoardImpl;

import javax.crypto.SecretKey;

public class BulletinBoardTest {
    public static void main(String[] args) {
        try {
            BulletinBoardImpl bulletinBoard = new BulletinBoardImpl(10);
            SecretKey sharedKey = CryptoUtils.generateKey();

            // Create clients
            Client Alice = new Client("Alice",sharedKey);
            Client Bob = new Client("Bob",sharedKey);

            // Initialize phonebooks
            Alice.InitiliaseClient(Bob);
            Bob.InitiliaseClient(Alice);
            Alice.updatePhonebook(Bob, Bob.getPhonebook());
            Bob.updatePhonebook(Alice, Alice.getPhonebook());



            // Send message
            String message1 = "Test inschalla";
            Alice.send(bulletinBoard,Bob,message1);
            System.out.println("bericht1: "+message1 );

            // recieve Message
            String message = Bob.recieve(bulletinBoard,Alice);
            System.out.println("Ontvangen bericht: "+ message);








            System.out.println("test 2");
            String message2 = "Test2";
            Bob.send(bulletinBoard,Alice,message2);
            System.out.println("bericht1: "+message2 );
            String message3 = Alice.recieve(bulletinBoard,Bob);
            System.out.println("Ontvangen bericht: "+ message3);
            System.out.println("Done baby");


//            // Generate a shared AES key
//            SecretKey sharedKey = CryptoUtils.generateKey();
//
//            // Initialize the bulletin board with 10 cells
//            BulletinBoardImpl bulletinBoard = new BulletinBoardImpl(10);
//            // Step 1: Alice encrypts and sends the first message
//            int cell1 = 1;
//            String preimage1 = "secret123"; // Preimage Bob knows
//            String firstMessage = "Hello Bob || 2 || nextSecret";
//            String encryptedFirstMessage = CryptoUtils.encrypt(firstMessage, sharedKey);
//            bulletinBoard.sendWithTag(cell1, encryptedFirstMessage, preimage1);
//            System.out.println("Alice sent (encrypted): " + encryptedFirstMessage);
//
//            // Step 2: Bob retrieves and decrypts the first message
//            String encryptedRetrievedMessage = bulletinBoard.retrieveWithTag(cell1, preimage1);
//            String decryptedRetrievedMessage = CryptoUtils.decrypt(encryptedRetrievedMessage, sharedKey);
//            System.out.println("Bob retrieved (decrypted): " + decryptedRetrievedMessage);
//
//            // Step 3: Alice encrypts and sends the second message
//            int cell2 = 2;
//            String secondMessage = "Message2 || 3 || Secret2";
//            String preimage2 = "nextSecret"; // Preimage Bob extracts from the first message
//            String encryptedSecondMessage = CryptoUtils.encrypt(secondMessage, sharedKey);
//            bulletinBoard.sendWithTag(cell2, encryptedSecondMessage, preimage2);
//            System.out.println("Alice sent (encrypted): " + encryptedSecondMessage);
//
//            // Step 4: Bob retrieves and decrypts the second message
//            String encryptedRetrievedMessage2 = bulletinBoard.retrieveWithTag(cell2, preimage2);
//            String decryptedRetrievedMessage2 = CryptoUtils.decrypt(encryptedRetrievedMessage2, sharedKey);
//            System.out.println("Bob retrieved (decrypted): " + decryptedRetrievedMessage2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
