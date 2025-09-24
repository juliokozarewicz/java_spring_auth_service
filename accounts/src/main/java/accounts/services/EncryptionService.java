package accounts.services;

import accounts.exceptions.ErrorHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class EncryptionService {

    // ====================================================== (Constructor init)

    // Keys
    // -------------------------------------------------------------------------
    @Value("${SECRET_KEY}")
    private String secretKey;
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private static final BCryptPasswordEncoder encoderPassword = new BCryptPasswordEncoder(10);
    private static final SecureRandom secureRandom = new SecureRandom();
    private SecretKey aesKey;

    public EncryptionService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }
    // ======================================================= (Constructor end)

    // =================================================== (Post construct init)
    @PostConstruct
    private void init() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.aesKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES key", e);
        }
    }
    // ==================================================== (Post construct end)

    // ======================================================= (encryption init)
    public String encrypt(String plainText) {

        try {

            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData);

        } catch (Exception e) {

            throw new SecurityException("Error encrypting " +
                "[ EncryptionService.encrypt() ]: ");

        }

    }
    // ======================================================== (encryption end)

    // ========================================================== (decrypt init)
    public String decrypt(String encryptedText) {

        try {

            byte[] encryptedData = Base64.getUrlDecoder().decode(encryptedText);

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedData.length - iv.length];

            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {

            throw new SecurityException("Error decrypting " +
                "[ EncryptionService.decrypt() ]");

        }

    }
    // =========================================================== (decrypt end)

    // ==================================================== (Password hash init)
    public String hashPassword(String password) {

        String hashedPassword = encoderPassword.encode(password);

        return hashedPassword;

    }
    // ===================================================== (Password hash end)

    // ================================================ (Compare passwords init)
    public boolean matchPasswords(String password, String storedHash) {

        boolean passwordsCompared = encoderPassword.matches(
            password,
            storedHash
        );

        return passwordsCompared;

    }
    // ================================================= (Compare passwords end)

    // ===================================================== (Create token init)
    public String createToken(String secretWord) {

        try {

            long timestamp = System.currentTimeMillis();

            String randomIdOne = UUID.randomUUID().toString();
            String randomIdTwo = UUID.randomUUID().toString();
            String randomIdThree = UUID.randomUUID().toString();

            String hashConcat = timestamp + secretWord +randomIdOne
                + randomIdTwo + randomIdThree;

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

            throw new SecurityException("Error creating token " +
                "[ EncryptionService.createToken() ]");

        }

    }
    // ====================================================== (Create token end)

}