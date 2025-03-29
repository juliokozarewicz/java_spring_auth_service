package com.example.demo.services;

import com.example.demo.interfaces.AccountsManagementInterface;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EmailService;
import com.example.demo.utils.EncryptionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private VerificationTokenRepository verificationTokenRepository;
    private final MessageSource messageSource;
    private AccountsRepository accountsRepository;
    private EncryptionControl encryptionControl;
    private final EmailService emailService;

    // Constructor
    public AccountsManagementService (

        VerificationTokenRepository verificationTokenRepository,
        MessageSource messageSource,
        EmailService emailService,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository

    ) {

        this.verificationTokenRepository = verificationTokenRepository;
        this.messageSource = messageSource;
        this.emailService = emailService;
        this.encryptionControl = encryptionControl;
        this.accountsRepository = accountsRepository;

    }

    @Override
    public void enableAccount(String userId) {

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // Create Account
        AccountsEntity activeAccount = new AccountsEntity();
        activeAccount.setId(userId);
        activeAccount.setUpdatedAt(nowTimestamp.toLocalDateTime());
        activeAccount.setActive(false);
        accountsRepository.save(activeAccount);

    }

    @Override
    public void disableAccount(String userId) {
    }

    @Override
    public void sendEmailStandard(String email, String message, String link) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // send email body
        StringBuilder messageEmail = new StringBuilder();

        // Greeting
        messageEmail.append(messageSource.getMessage(
            "email_greeting", null, locale)
        ).append("\n\n");

        // Body
        messageEmail.append(messageSource.getMessage(
            message, null, locale)
        ).append("\n\n");

        // add link if exist
        if (link != null && !link.isEmpty()) {
            messageEmail.append(link).append("\n\n");
        }

        // Close
        messageEmail.append(messageSource.getMessage(
            "email_closing", null, locale)
        ).append("\n").append(applicatonTitle);

        String subject = "[ " + applicatonTitle + " ] - Account Service";

        emailService.sendSimpleEmail(
            email,
            subject,
            messageEmail.toString()
        );

    }

    @Override
    public String createToken(String email, String reason) {

        // UUID
        String generatedUUID = UUID.randomUUID().toString();

        // Timestamp
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // Concatenates everything
        String secretWord = generatedUUID + email;

        // Get hash
        String hashFinal = encryptionControl.createToken(secretWord);

        // Write to database
        VerificationTokenEntity newToken = new VerificationTokenEntity();
        newToken.setId(generatedUUID);
        newToken.setCreatedAt(nowTimestamp.toLocalDateTime());
        newToken.setUpdatedAt(nowTimestamp.toLocalDateTime());
        newToken.setEmail(email);
        newToken.setToken(hashFinal + "_" + reason);
        verificationTokenRepository.save(newToken);

        return hashFinal;

    }

}
