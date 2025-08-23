package bulletingui.bulletingui.Commen;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * CryptoUtils biedt cryptografische hulpfuncties die gebruikt worden
 * door het ABB-WPES protocol.
 *
 * Belangrijkste functies:
 * - Authenticated Encryption (AES/GCM/NoPadding) met willekeurige 96-bit IV
 *   voor vertrouwelijkheid, integriteit en authenticiteit.
 * - HKDF (met HMAC-SHA256) om veilige sleutels af te leiden uit een
 *   Diffie-Hellman gedeeld geheim.
 * - Eenvoudige ratchet-functie op basis van HMAC-SHA256 om forward secrecy
 *   te garanderen door de sleutel na elk bericht te vernieuwen.
 * - Hulpfuncties voor random tag-preimages (Base64-encoded) en AES-sleutelgeneratie.
 *
 * Deze klasse centraliseert alle crypto-logica zodat de Client en Server
 * klassen zich kunnen focussen op protocolstappen. Correct gebruik van
 * deze functies is cruciaal voor de veiligheid van ABB-WPES.
 */
public class CryptoUtils {

    private static final SecureRandom RNG = new SecureRandom();

    public static String encryptAEAD(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[12]; // 96-bit nonce as recommended for GCM
        RNG.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // store as base64(iv) : base64(ciphertext+tag)
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ct);
    }

    public static String decryptAEAD(String enc, SecretKey key) throws Exception {
        String[] parts = enc.split(":", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid AEAD payload");

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] ct = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, StandardCharsets.UTF_8);
    }

    /* =====================  HKDF (HMAC-SHA256) ===================== */

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec ks = new SecretKeySpec(key, "HmacSHA256");
        mac.init(ks);
        return mac.doFinal(data);
    }

    // HKDF-Extract(salt, IKM) -> PRK
    public static byte[] hkdfExtract(byte[] salt, byte[] ikm) throws Exception {
        byte[] saltOrZero = (salt == null ? new byte[32] : salt);
        return hmacSha256(saltOrZero, ikm);
    }

    // HKDF-Expand(PRK, info, L) -> OKM
    public static byte[] hkdfExpand(byte[] prk, byte[] info, int L) throws Exception {
        int hashLen = 32; // SHA-256
        int n = (int) Math.ceil((double) L / hashLen);
        ByteBuffer okm = ByteBuffer.allocate(n * hashLen);

        byte[] t = new byte[0];
        for (int i = 1; i <= n; i++) {
            ByteBuffer buf = ByteBuffer.allocate(t.length + (info == null ? 0 : info.length) + 1);
            buf.put(t);
            if (info != null) buf.put(info);
            buf.put((byte) i);
            t = hmacSha256(prk, buf.array());
            okm.put(t);
        }
        byte[] out = new byte[L];
        okm.rewind();
        okm.get(out, 0, L);
        return out;
    }

    public static SecretKey hkdfToAesKey(byte[] prk, String infoLabel) throws Exception {
        byte[] okm = hkdfExpand(prk, infoLabel.getBytes(StandardCharsets.UTF_8), 16); // AES-128
        return new SecretKeySpec(okm, "AES");
    }

    /* =====================  Simple ratchet KDF ===================== */

    public static SecretKey ratchet(SecretKey current) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(current.getEncoded(), "HmacSHA256"));
        byte[] next = mac.doFinal("ratchet".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] k = new byte[16]; // 128-bit AES key
        System.arraycopy(next, 0, k, 0, 16);
        return new SecretKeySpec(k, "AES");
    }

    /* =====================  Helpers for tags/ids ===================== */

    public static String randomTagPreimage() {
        byte[] t = new byte[32];
        RNG.nextBytes(t);
        return Base64.getEncoder().encodeToString(t);
    }

    // Generate a new AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // AES-128
        return keyGen.generateKey();
    }

// Oude code
//
//    // Encrypt a message using a secret key
//    public static String encrypt(String message, SecretKey key) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
//        return Base64.getEncoder().encodeToString(encryptedBytes);
//    }
//
//    // Decrypt a message using a secret key
//    public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, key);
//        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
//
//        return new String(cipher.doFinal(decodedBytes));
//    }
//    public static String generateHMAC(SecretKey key, String message) throws Exception {
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(key);
//        byte[] hmacBytes = mac.doFinal(message.getBytes());
//        return Base64.getEncoder().encodeToString(hmacBytes); // Return Base64 encoded HMAC
//    }
//
//    // Convert a secret key to a Base64 string
//    public static String keyToString(SecretKey key) {
//        return Base64.getEncoder().encodeToString(key.getEncoded());
//    }
//
//    // Convert a Base64 string to a secret key
//    public static SecretKey stringToKey(String keyString) {
//        byte[] decodedKey = Base64.getDecoder().decode(keyString);
//        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//    }
//
//    public static SecretKey deriveNewKey(SecretKey oldKey, String context) throws Exception {
//        Mac hmac = Mac.getInstance("HmacSHA256");
//        hmac.init(oldKey);
//        byte[] newKeyMaterial = hmac.doFinal(context.getBytes());
//        byte[] keyBytes = new byte[16]; // Use first 128 bits for AES
//        System.arraycopy(newKeyMaterial, 0, keyBytes, 0, 16);
//        return new SecretKeySpec(keyBytes, "AES");
//    }




}
