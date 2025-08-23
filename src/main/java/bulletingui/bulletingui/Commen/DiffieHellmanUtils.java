package bulletingui.bulletingui.Commen;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;


/**
 * DiffieHellmanUtils verzorgt het opzetten van een gedeeld geheim
 * tussen twee clients via Elliptic Curve Diffie-Hellman (ECDH).
 *
 * Kenmerken:
 * - Genereert bij constructie een ECDH sleutelpaar op curve secp256r1.
 * - Biedt toegang tot de publieke sleutel om met een peer te delen.
 * - Kan, gegeven de publieke sleutel van een andere client, het
 *   gedeelde geheime materiaal (shared secret) afleiden.
 *
 * Dit gedeeld geheim vormt de basis voor sleutelafleiding via HKDF
 * en wordt verder gebruikt voor AEAD-encryptie in het ABB-WPES protocol.
 */

public class DiffieHellmanUtils {
    private final KeyPair keyPair;
    private final KeyAgreement ka;


    public DiffieHellmanUtils() throws Exception {
        // ECDH (secp256r1); als je bij klassieke DH wilt blijven kan dat ook.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        this.keyPair = kpg.generateKeyPair();

        this.ka = KeyAgreement.getInstance("ECDH");
        this.ka.init(keyPair.getPrivate());
    }


    public PublicKey getPublicKey() throws Exception {
        return this.keyPair.getPublic();
    }

    public byte[] deriveSharedSecret(PublicKey other) throws Exception {
        ka.doPhase(other, true);
        return ka.generateSecret();
    }
// oude code
//    public DiffieHellmanUtils() throws Exception {
//        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
//        keyPairGen.initialize(2048);
//        this.keyPair = keyPairGen.generateKeyPair();
//        this.keyAgreement = KeyAgreement.getInstance("DH");
//        this.keyAgreement.init(keyPair.getPrivate());
//    }
//
//    public PublicKey getPublicKey() {
//        return this.keyPair.getPublic();
//    }
//
//    public SecretKey deriveSharedSecret(PublicKey otherPublicKey) throws Exception {
//        this.keyAgreement.doPhase(otherPublicKey, true);
//        byte[] sharedSecret = this.keyAgreement.generateSecret();
//        return new SecretKeySpec(sharedSecret, 0, 16, "AES"); // Use AES key material
//    }
}

