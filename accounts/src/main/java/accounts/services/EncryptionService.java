package accounts.services;

import accounts.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Component
public class EncryptionService {

    // Keys
    // -------------------------------------------------------------------------
    @Value("${SECRET_KEY}")
    private String secretKey;
    // -------------------------------------------------------------------------

    // Constructor
    // -------------------------------------------------------------------------
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private static final BCryptPasswordEncoder encoderPassword = new BCryptPasswordEncoder(10);
    private static final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }
    // -------------------------------------------------------------------------

    // encryption
    // -------------------------------------------------------------------------
    public String encrypt(String plainText) {

        try {

            byte[] salt = new byte[16];
            byte[] iv = new byte[12];
            secureRandom.nextBytes(salt);
            secureRandom.nextBytes(iv);

            PBEKeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, 10_000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedData = new byte[salt.length + iv.length + ciphertext.length];
            System.arraycopy(salt, 0, encryptedData, 0, salt.length);
            System.arraycopy(iv, 0, encryptedData, salt.length, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, salt.length + iv.length, ciphertext.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData);

        } catch (Exception e) {

            throw new SecurityException("Error encrypting " +
                "[ EncryptionService.encrypt() ]: ");

        }

    }
    // -------------------------------------------------------------------------

    // decryption
    // -------------------------------------------------------------------------
    public String decrypt(String encryptedText) {

        try {

            byte[] encryptedData = Base64.getUrlDecoder().decode(encryptedText);

            byte[] salt = new byte[16];
            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedData.length - salt.length - iv.length];

            System.arraycopy(encryptedData, 0, salt, 0, salt.length);
            System.arraycopy(encryptedData, salt.length, iv, 0, iv.length);
            System.arraycopy(encryptedData, salt.length + iv.length, ciphertext, 0, ciphertext.length);

            PBEKeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, 10_000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

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
    // -------------------------------------------------------------------------

    // Password hash
    // -------------------------------------------------------------------------
    public String hashPassword(String password) {

        String hashedPassword = encoderPassword.encode(password);

        return hashedPassword;

    }
    // -------------------------------------------------------------------------

    // Compare passwords
    // -------------------------------------------------------------------------
    public boolean matchPasswords(String password, String storedHash) {

        boolean passwordsCompared = encoderPassword.matches(
            password,
            storedHash
        );

        return passwordsCompared;

    }
    // -------------------------------------------------------------------------

    // Create token
    // -------------------------------------------------------------------------
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
    // -------------------------------------------------------------------------

}