package bulletingui.bulletingui.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            int numPartitions = 5; // Example: 5 partitions
            //BulletinBoard bulletinBoard = new BulletinBoardImpl(numPartitions);

            // Start the RMI registry and bind the server
            Registry registry = LocateRegistry.createRegistry(1099);
            //registry.rebind("BulletinBoard", bulletinBoard);
            System.out.println("Bulletin Board Server is running with " + numPartitions + " partitions...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
