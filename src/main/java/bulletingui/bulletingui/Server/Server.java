package bulletingui.bulletingui.Server;

import bulletingui.bulletingui.Commen.BulletinBoard;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            BulletinBoardImpl bulletinBoard = new BulletinBoardImpl(10,""); // Create the remote object
            Registry registry = LocateRegistry.createRegistry(1099); // Start the registry on port 1099
            registry.rebind("BulletinBoard", bulletinBoard); // Bind the object to the registry
            System.out.println("BulletinBoard server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
