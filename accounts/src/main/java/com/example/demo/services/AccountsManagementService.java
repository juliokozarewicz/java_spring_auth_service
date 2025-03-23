package com.example.demo.services;

import com.example.demo.interfaces.AccountsManagementInterface;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${SECRET_KEY}")
    private String secretKey;

    private VerificationTokenRepository verificationTokenRepository;

    // Constructor
    public AccountsManagementService (
        VerificationTokenRepository verificationTokenRepository
    ) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public void enableAccount(String activeId) {
    }

    @Override
    public void disableAccount(String activeId) {
    }

    @Override
    public String createToken(String email, String reason) {

        try {

            // UUID
            String generatedUUID = UUID.randomUUID().toString();

            // Timestamp
            ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
            Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

            // Current timestamp
            long RandomTimestamp = System.currentTimeMillis() * 185;

            // Concatenates everything
            String hashConcat = generatedUUID + RandomTimestamp + email + secretKey;

            // Create hash
            MessageDigest digest = MessageDigest.getInstance(
                "SHA-512"
            );
            byte[] hashRaw = digest.digest(hashConcat.getBytes());

            // convert hash to hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashRaw) {
                hexString.append(String.format("%02x", b));
            }
            String hashFinal = hexString.toString();

            // Write to database
            VerificationTokenEntity newToken = new VerificationTokenEntity();
            newToken.setId(generatedUUID);
            newToken.setCreatedAt(nowTimestamp.toLocalDateTime());
            newToken.setUpdatedAt(nowTimestamp.toLocalDateTime());
            newToken.setEmail(email);
            newToken.setToken(hashFinal + "_" + reason);
            verificationTokenRepository.save(newToken);

            return hashFinal;

        } catch (Exception e) {

            throw new SecurityException(e);
        }

    }

}
