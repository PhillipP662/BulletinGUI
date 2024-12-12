package bulletingui.bulletingui.Commen;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtils {
    // Generate a new AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // AES-128
        return keyGen.generateKey();
    }

    // Encrypt a message using a secret key
    public static String encrypt(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt a message using a secret key
    public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
        return new String(cipher.doFinal(decodedBytes));
    }

    // Convert a secret key to a Base64 string
    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // Convert a Base64 string to a secret key
    public static SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static SecretKey deriveNewKey(SecretKey oldKey, String context) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(oldKey);
        byte[] newKeyMaterial = hmac.doFinal(context.getBytes());
        byte[] keyBytes = new byte[16]; // Use first 128 bits for AES
        System.arraycopy(newKeyMaterial, 0, keyBytes, 0, 16);
        return new SecretKeySpec(keyBytes, "AES");
    }




}
