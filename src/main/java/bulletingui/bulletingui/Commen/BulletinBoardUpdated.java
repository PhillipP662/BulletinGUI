package bulletingui.bulletingui.Commen;

import bulletingui.bulletingui.Client.Client;

import javax.swing.*;
import java.util.Base64;

/**
 * Demo-klasse om de werking van het ABB-WPES protocol te tonen.
 *
 * Functies van main():
 * - Maakt twee clients aan (Alice en Bob) en initialiseert hun contact
 *   via Diffie-Hellman sleuteluitwisseling en phonebook-setup.
 * - Print de interne state van beide clients (debug).
 * - Laat Alice een bericht naar Bob sturen en Bob terug naar Alice,
 *   waarbij de encryptie, cell-ID/tag updates en sleutelratchets
 *   zichtbaar worden.
 * - Roept de recover-functionaliteit aan (herlezen van snapshots en
 *   pending sends na een crash).
 * - Toont de geüpdatete interne state na de berichtenuitwisseling.
 */



public class BulletinBoardUpdated {

    public static void main(String[] args) throws Exception {

        // Client Alice en Bob aanmaken
        System.out.println("Setup");
        Client Alice = new Client("Alice");
        Client Bob = new Client("Bob");

        //initialiseert hun contact via Diffie-Hellman sleuteluitwisseling en phonebook-setup.
        Alice.establishContactBidirectioneel(Bob);
        Bob.establishContactBidirectioneel(Alice);

        System.out.println("DEBUG Setup: ");
        System.out.println("---------------------------------------------- ");

        System.out.println("Alice debug output:");
        Alice.debugPrintState(Bob);
        System.out.println("Bob debug output:");
        Bob.debugPrintState(Alice);

        System.out.println("----------------------------------------------");
        System.out.println("----------------------------------------------");


        System.out.println("Send and recieve");
        // Alice -> Bob
        Alice.sendTo(Bob, "Sup Bob!");
        String r1 = Bob.receiveFrom(Alice);
        System.out.println("Bob ontving: " + r1);

        // Bob -> Alice
        Bob.sendTo(Alice, "Hoi Alice!");
        String r2 = Alice.receiveFrom(Bob);
        System.out.println("Alice ontving: " + r2);

        // Roept de recover-functionaliteit
        Alice.recoverFromDiskWith(Bob);
        Bob.recoverFromDiskWith(Alice);
        // Debug ná verkeer
        Alice.debugPrintState(Bob);
        Bob.debugPrintState(Alice);




    }




}
