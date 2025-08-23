package bulletingui.bulletingui.Commen;

import bulletingui.bulletingui.Client.Client;

import javax.swing.*;
import java.util.Base64;

public class BulletinBoardUpdated {

    public static void main(String[] args) throws Exception {

        System.out.println("Setup");

        Client Alice = new Client("Alice");
        Client Bob = new Client("Bob");

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


        Alice.recoverFromDiskWith(Bob);
        Bob.recoverFromDiskWith(Alice);
        // Debug ná verkeer (let op: idx/preimage zijn op beide kanten verschoven)
        Alice.debugPrintState(Bob);
        Bob.debugPrintState(Alice);




    }




}
