package accounts.services;

import accounts.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
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
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    public EncryptionService (

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
    }
    // -------------------------------------------------------------------------

    @Value("${SECRET_KEY}")
    private String secretKey;

    @Value("${PRIVATE_KEY}")
    private String privateKey;

    @Value("${PUBLIC_KEY}")
    private String publicKey;

    // encryption
    public String encrypt(String plainText) {

        try {

            byte[] decodedKey = Base64.getDecoder().decode(publicKey);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

    // decryption
    public String decrypt(String encryptedText) {

        try {

            byte[] decodedKey = Base64.getDecoder().decode(privateKey);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PrivateKey privKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

            cipher.init(Cipher.DECRYPT_MODE, privKey);

            byte[] encryptedBytes = Base64.getUrlDecoder().decode(encryptedText);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

    // Password hash
    public String hashPassword(String password) {

        String hashedPassword = encoder.encode(password + secretKey);

        return hashedPassword;

    }

    // Compare passwords
    public boolean matchPasswords(String password, String storedHash) {

        boolean passwordsCompared = encoder.matches(
            password + secretKey,
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

    // Encode to Base64
    public String encodeBase64(String text) {

        String textEncoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(
                text.getBytes(StandardCharsets.UTF_8)
            );

        return textEncoded;
    }

    // Decode to Base64
    public String decodeBase64(String text) {

        try {

            String textDecoded = new String(
                Base64.getUrlDecoder().decode(text),
                StandardCharsets.UTF_8
            );

            return textDecoded;

        } catch (Exception e) {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_bad_request", null, LocaleContextHolder.getLocale()
                )
            );

            return null;

        }
    }

    // Create token
    public String createToken(String secretWord) {

        try {

            long RandomTimestamp = System.currentTimeMillis() * 185;

            String hashConcat = RandomTimestamp + secretWord + secretKey;

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