package security;

import java.nio.file.*;
import java.security.*;

public class MyKeyManager {

	private static final String PUBLIC_KEY_FILE = "src/security/publicKey";
	private static final String PRIVATE_KEY_FILE = "src/security/privateKey";

    // Tạo và lưu key nếu chưa có
    public static void generateAndSaveKeyPair() throws Exception {
        if (Files.exists(Paths.get(PUBLIC_KEY_FILE)) && Files.exists(Paths.get(PRIVATE_KEY_FILE))) return;

        KeyPair keyPair = EncryptionUtil.generateKeyPair();

        // Lưu public key
        Files.write(Paths.get(PUBLIC_KEY_FILE), keyPair.getPublic().getEncoded());

        // Lưu private key
        Files.write(Paths.get(PRIVATE_KEY_FILE), keyPair.getPrivate().getEncoded());
    }

    public static PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
    }

    public static PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
    }
}