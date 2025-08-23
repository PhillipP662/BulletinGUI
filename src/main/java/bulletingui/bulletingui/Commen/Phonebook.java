package bulletingui.bulletingui.Commen;

import bulletingui.bulletingui.Client.Client;

import javax.crypto.SecretKey;
import java.io.*;


/**
 * Phonebook houdt de communicatie-state bij tussen twee clients
 * in het ABB-WPES protocol.
 *
 * Functies:
 * - Bevat voor elke richting een ChannelState (outbound en inbound).
 *   Elke ChannelState bevat:
 *   - de huidige AES-sleutel (met ratchet mechanisme),
 *   - het cellId op het bulletin board waar het volgende bericht
 *     moet worden geplaatst of opgehaald,
 *   - de bijhorende tag-preimage.
 * - Wordt door de Client gebruikt om bij te houden waar te schrijven,
 *   waar te lezen en welke sleutel daarbij hoort.
 * - Ondersteunt initialisatie (empty) en eenvoudige getters/setters
 *   voor state-updates na elke send/receive.
 *
 * Hiermee vormt Phonebook de kern van de "per-peer" state die nodig is
 * om berichten asynchroon en veilig uit te wisselen.
 */



public class Phonebook  {

    public static class ChannelState {
        private SecretKey key;
        private int cellId;
        private String preimage;

        public ChannelState(SecretKey key, int cellId, String preimage) {
            this.key = key;
            this.cellId = cellId;
            this.preimage = preimage;
        }

        public SecretKey getKey() {
            return key;
        }

        public int getCellId() {
            return cellId;
        }

        public String getPreimage() {
            return preimage;
        }

        public void setKey(SecretKey key) {
            this.key = key;
        }

        public void setCellId(int cellId) {
            this.cellId = cellId;
        }

        public void setPreimage(String preimage) {
            this.preimage = preimage;
        }
    }

    private ChannelState outbound;
    private ChannelState inbound;

    public Phonebook(ChannelState outbound, ChannelState inbound) {
        this.outbound = outbound;
        this.inbound = inbound;

    }

    public ChannelState getOutbound() {return outbound;}
    public ChannelState getInbound() {return inbound;}

    public static Phonebook empty() {return new Phonebook(null, null);}

    @Override
    public String toString() {
        return "Phonebook{" +
                "outbound=" + outbound +
                ", inbound=" + inbound +
                '}';
    }
// Oude code
//    private String sendtag;
//    private int sendCellId;
//
//    private String recieveTag;
//
//    private int recieveCellId;
//    private SecretKey secretKey;
//
//    // Uitbreiding 1: Recoverability
//    private static final long serialVersionUID = 1L; // For serialization consistency
//
//
//    public Phonebook(String tag, int cellId,SecretKey secretKey) {
//        this.sendtag = tag;
//        this.sendCellId = cellId;
//        this.secretKey = secretKey;
//    }
//
//    public String getSendtag() {
//        return sendtag;
//    }
//
//    public void setSendtag(String sendtag) {
//        this.sendtag = sendtag;
//    }
//
//    public int getSendCellId() {
//        return sendCellId;
//    }
//
//    public void setSendCellId(int sendCellId) {
//        this.sendCellId = sendCellId;
//    }
//
//    public String getRecieveTag() {
//        return recieveTag;
//    }
//
//    public void setRecieveTag(String recieveTag) {
//        this.recieveTag = recieveTag;
//    }
//
//    public int getRecieveCellId() {
//        return recieveCellId;
//    }
//
//    public void setRecieveCellId(int recieveCellId) {
//        this.recieveCellId = recieveCellId;
//    }
//
//    public void setSecretKey(SecretKey secretKey) {
//        this.secretKey = secretKey;
//    }
//
//    public SecretKey getSecretKey(){return secretKey;}


}
