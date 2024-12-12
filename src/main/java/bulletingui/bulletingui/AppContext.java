package bulletingui.bulletingui;

import bulletingui.bulletingui.Client.Client;
import bulletingui.bulletingui.Server.BulletinBoardImpl;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

public class AppContext {
    private static SecretKey sharedKey;
    private static Map<String, Client> clients = new HashMap<>();

    public static void setSharedKey(SecretKey sharedKey) {
        AppContext.sharedKey = sharedKey;
    }
    public static SecretKey getSharedKey() {
        return sharedKey;
    }
    public static Client getClient(String name) {
        return clients.get(name);
    }
    public static void setClient(String id, Client client) {
        AppContext.clients.put(id, client);
    }
    public static void reset() {
        AppContext.sharedKey = null;
        AppContext.clients.clear();
    }
}
