package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class EncryptionControl {

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

            return Base64.getEncoder().encodeToString(encryptedBytes);

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

            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );

            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

            return new String(decryptedBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

    // Password hash
    public String hashPassword(String password) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String hashedPassword = encoder.encode(password + secretKey);

        return hashedPassword;

    }

    // Compare passwords
    public boolean matchPasswords(String password, String storedHash) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        boolean passwordsCompared = encoder.matches(
            password + secretKey,
            storedHash
        );

        return passwordsCompared;

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