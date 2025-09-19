package accounts.services;

import accounts.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public String encrypt(String plainText) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));

            char[] passwordChars = secretKey.toCharArray();
            int iterations = 100_000;
            int keyLength = 256;

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterations, keyLength);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData);

        } catch (Exception e) {

            throw new InternalError("Error encrypting " +
                "[ EncryptionService.encrypt() ]: " + e);

        }

    }

    // decryption
    public String decrypt(String encryptedText) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));

            char[] passwordChars = secretKey.toCharArray();
            int iterations = 100_000;
            int keyLength = 256;

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterations, keyLength);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            byte[] encryptedData = Base64.getUrlDecoder().decode(encryptedText);

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedData.length - 12];

            System.arraycopy(encryptedData, 0, iv, 0, 12);
            System.arraycopy(encryptedData, 12, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

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