package bulletingui.bulletingui.Commen;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {


    void sendAB(String senderId, String receiverId, String message) throws RemoteException;
    String receiveAB(String receiverId, String senderId) throws RemoteException;


}
