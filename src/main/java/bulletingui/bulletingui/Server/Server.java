package bulletingui.bulletingui.Server;

import bulletingui.bulletingui.Commen.BulletinBoard;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Registry registry;
    private static BulletinBoardImpl bulletinBoard;

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            bulletinBoard = new BulletinBoardImpl(10,"");
            registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BulletinBoard", bulletinBoard);
            System.out.println("BulletinBoard server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            if (registry != null) {
                registry.unbind("BulletinBoard");
            }
            if (bulletinBoard != null) {
                UnicastRemoteObject.unexportObject(bulletinBoard, true);
            }
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
            System.out.println("BulletinBoard server stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
