package bulletingui.bulletingui.Commen;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class DiffieHellmanUtils {
    private KeyPair keyPair;
    private KeyAgreement keyAgreement;

    public DiffieHellmanUtils() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
        keyPairGen.initialize(2048);
        this.keyPair = keyPairGen.generateKeyPair();
        this.keyAgreement = KeyAgreement.getInstance("DH");
        this.keyAgreement.init(keyPair.getPrivate());
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public SecretKey deriveSharedSecret(PublicKey otherPublicKey) throws Exception {
        this.keyAgreement.doPhase(otherPublicKey, true);
        byte[] sharedSecret = this.keyAgreement.generateSecret();
        return new SecretKeySpec(sharedSecret, 0, 16, "AES"); // Use AES key material
    }
}

