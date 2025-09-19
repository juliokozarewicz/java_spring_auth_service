package accounts.services;

import accounts.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class EncryptionService {

    // Keys
    // -------------------------------------------------------------------------
    @Value("${PRIVATE_KEY}")
    private String privateKey;

    @Value("${PUBLIC_KEY}")
    private String publicKey;
    // -------------------------------------------------------------------------

    // Constructor
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private static final BCryptPasswordEncoder encoderPassword = new BCryptPasswordEncoder(12);

    public EncryptionService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }
    // -------------------------------------------------------------------------

    // encryption
    public String encryptSign(String plainText) {

        try {

            byte[] decoded = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec));

            int blockSize = 150;
            StringBuilder encryptedText = new StringBuilder();

            for (int i = 0; i < plainText.length(); i += blockSize) {

                String block = plainText.substring(i, Math.min(i + blockSize, plainText.length()));
                byte[] encryptedBytes = cipher.doFinal(block.getBytes(StandardCharsets.UTF_8));
                String encodedBlock = Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);

                if (encryptedText.length() > 0) {
                    encryptedText.append(":::");
                }

                encryptedText.append(encodedBlock);

            }

            return encryptedText.toString();

        } catch (Exception e) {

            throw new InternalError("Error encrypting " +
                "[ EncryptionService.encrypt() ]: " + e);

        }

    }

    // decryption
    public String decryptVerify(String encryptedText) {

        try {

            byte[] decoded = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));
            String[] encryptedBlocks = encryptedText.split(":::");
            StringBuilder decryptedText = new StringBuilder();

            for (String block : encryptedBlocks) {

                byte[] encryptedBytes = Base64.getUrlDecoder().decode(block);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

                decryptedText.append(new String(decryptedBytes, StandardCharsets.UTF_8));

            }

            return decryptedText.toString();

        } catch (Exception e) {

            throw new HttpMessageNotReadableException("Error decrypting " +
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

    // Create token
    public String createToken(String secretWord) {

        try {

            long timestamp = System.currentTimeMillis();

            String randomId = UUID.randomUUID().toString();

            String hashConcat = timestamp + secretWord + randomId;

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