package bulletingui.bulletingui.Commen;

import javax.crypto.SecretKey;
import java.io.*;

public class Phonebook  {
    private String sendtag;
    private int sendCellId;

    private String recieveTag;

    private int recieveCellId;
    private SecretKey secretKey;

    // Uitbreiding 1: Recoverability
    private static final long serialVersionUID = 1L; // For serialization consistency


    public Phonebook(String tag, int cellId,SecretKey secretKey) {
        this.sendtag = tag;
        this.sendCellId = cellId;
        this.secretKey = secretKey;
    }

    public String getSendtag() {
        return sendtag;
    }

    public void setSendtag(String sendtag) {
        this.sendtag = sendtag;
    }

    public int getSendCellId() {
        return sendCellId;
    }

    public void setSendCellId(int sendCellId) {
        this.sendCellId = sendCellId;
    }

    public String getRecieveTag() {
        return recieveTag;
    }

    public void setRecieveTag(String recieveTag) {
        this.recieveTag = recieveTag;
    }

    public int getRecieveCellId() {
        return recieveCellId;
    }

    public void setRecieveCellId(int recieveCellId) {
        this.recieveCellId = recieveCellId;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public SecretKey getSecretKey(){return secretKey;}


}
