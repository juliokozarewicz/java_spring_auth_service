package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class EncryptionControl {

    @Value("${PUBLIC_KEY}")
    private String publicKey;

    @Value("${PRIVATE_KEY}")
    private String privateKey;

    // encryption
    public String encrypt(String plainText) {

        try {

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }
    }

    // decryption
    public String decrypt(String encryptedText) {

        try {

            Cipher cipher = Cipher.getInstance(
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

    // pub key (Base64) to PublicKey
    private PublicKey getPublicKey(String base64PublicKey) {

        try {

            byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

    // private key (Base64) to PrivateKey
    private PrivateKey getPrivateKey(String base64PrivateKey) {

        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {

            throw new SecurityException(e);

        }

    }

}