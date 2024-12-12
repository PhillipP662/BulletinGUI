package bulletingui.bulletingui.Commen;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BulletinBoard extends Remote {


    void sendWithTag(int cellId, String message, String preimage) throws RemoteException;
    String retrieveWithTag(int cellId, String preimage) throws RemoteException;


}
