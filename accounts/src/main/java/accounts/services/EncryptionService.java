package accounts.services;

import accounts.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;

@Component
public class EncryptionService {

    // Constructor
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private static final BCryptPasswordEncoder encoderPassword = new BCryptPasswordEncoder(12);
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public EncryptionService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {
        try {

            this.privateKey = loadPrivateKey();
            this.publicKey = loadPublicKey();
            this.messageSource = messageSource;
            this.errorHandler = errorHandler;

        } catch (Exception e) {

            throw new InternalError("Failed to load RSA keys " +
                "[ EncryptionService.EncryptionService() ]: " + e);

        }
    }
    // -------------------------------------------------------------------------

    // Load keys
    // -------------------------------------------------------------------------
    private PrivateKey loadPrivateKey() throws Exception {
        String key = new String(Files.readAllBytes(
            Paths.get("src/main/resources/keys/private_key.pem")),
            StandardCharsets.UTF_8
        );
        key = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(
            "src/main/resources/keys/public_key.pem")),
            StandardCharsets.UTF_8
        );
        key = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
    // -------------------------------------------------------------------------

    // encryption
    public String encrypt(String plainText) {

        try {

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);

        } catch (Exception e) {

            throw new InternalError("Error encrypting " +
                "[ EncryptionService.encrypt() ]: " + e);

        }

    }

    // decryption
    public String decrypt(String encryptedText) {

        try {

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.getUrlDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {

            throw new InternalError("Error decrypting " +
                "[ EncryptionService.decrypt() ]: " + e);

        }

    }

    // Password hash
    public String hashPassword(String password) {

        String hashedPassword = encoderPassword.encode(password);

        return hashedPassword;

    }

    // Compare passwords
    public boolean matchPasswords(String password, String storedHash) {

        boolean passwordsCompared = encoderPassword.matches(
            password,
            storedHash
        );

        return passwordsCompared;

    }

    // Hash SHA-256 text
    public String hashSHA256(String text) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }
    }

    // Create token
    public String createToken(String secretWord) {

        try {

            long RandomTimestamp = System.currentTimeMillis() * 185;

            String hashConcat = RandomTimestamp + secretWord;

            MessageDigest digest = MessageDigest.getInstance(
                "SHA-512"
            );

            byte[] hashRaw = digest.digest(hashConcat.getBytes());

            StringBuilder hexString = new StringBuilder();

            for (byte b : hashRaw) {
                hexString.append(String.format("%02x", b));
            }

            String hashFinal = hexString.toString();

            return hashFinal;

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

}