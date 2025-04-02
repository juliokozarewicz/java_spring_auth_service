package com.example.demo.services;

import com.example.demo.interfaces.AccountsManagementInterface;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.UserLogsEntity;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.UserLogsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EmailService;
import com.example.demo.utils.EncryptionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final VerificationTokenRepository verificationTokenRepository;
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionControl encryptionControl;
    private final EmailService emailService;
    private final UserLogsRepository userLogsRepository;

    // Constructor
    public AccountsManagementService (

        VerificationTokenRepository verificationTokenRepository,
        MessageSource messageSource,
        EmailService emailService,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository,
        UserLogsRepository userLogsRepository

    ) {

        this.verificationTokenRepository = verificationTokenRepository;
        this.messageSource = messageSource;
        this.emailService = emailService;
        this.encryptionControl = encryptionControl;
        this.accountsRepository = accountsRepository;
        this.userLogsRepository = userLogsRepository;

    }

    // UUID and Timestamp
    ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
    Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

    @Override
    public void enableAccount(String userId) {

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            userId.toLowerCase()
        );

        if ( findUser.isPresent() && !findUser.get().isBanned() ) {

            // Update
            AccountsEntity user = findUser.get();
            user.setActive(true);
            user.setUpdatedAt(nowTimestamp.toLocalDateTime());
            accountsRepository.save(user);

        }

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

        String subject = "[ " + applicatonTitle + " ] - " + messageSource.
            getMessage(
                "email_subject_account", null, locale
            );

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

    @Override
    public void createUserLog(
        String ipAddress,
        String userId,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    ) {

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // Create log
        UserLogsEntity newUserLog = new UserLogsEntity();
        newUserLog.setId(generatedUUID);
        newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
        newUserLog.setIpAddress(ipAddress);
        newUserLog.setUserId(userId);
        newUserLog.setAgent(agent);
        newUserLog.setUpdateType(updateType);
        newUserLog.setOldValue(oldValue);
        newUserLog.setNewValue(newValue);
        userLogsRepository.save(newUserLog);

    }

}
