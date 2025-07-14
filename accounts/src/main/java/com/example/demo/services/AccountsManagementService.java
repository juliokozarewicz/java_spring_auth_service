package com.example.demo.services;

import com.example.demo.interfaces.AccountsManagementInterface;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.AccountsRefreshLoginEntity;
import com.example.demo.persistence.entities.AccountsUserLogEntity;
import com.example.demo.persistence.entities.AccountsVerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.RefreshLoginRepository;
import com.example.demo.persistence.repositories.UserLogsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EmailService;
import com.example.demo.utils.EncryptionService;
import com.example.demo.utils.UserJWTService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private final VerificationTokenRepository verificationTokenRepository;
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final EmailService emailService;
    private final UserLogsRepository userLogsRepository;
    private final UserJWTService userJWTService;
    private final RefreshLoginRepository refreshLoginRepository;

    // Constructor
    public AccountsManagementService (

        VerificationTokenRepository verificationTokenRepository,
        MessageSource messageSource,
        EmailService emailService,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        UserLogsRepository userLogsRepository,
        RefreshLoginRepository refreshLoginRepository,
        UserJWTService userJWTService

    ) {

        this.verificationTokenRepository = verificationTokenRepository;
        this.messageSource = messageSource;
        this.emailService = emailService;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;
        this.userLogsRepository = userLogsRepository;
        this.refreshLoginRepository = refreshLoginRepository;
        this.userJWTService = userJWTService;

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
        String secretWord = generatedUUID + email + nowTimestamp;

        // Get hash
        String hashFinal = encryptionService.createToken(secretWord);

        // Write to database
        AccountsVerificationTokenEntity newToken = new AccountsVerificationTokenEntity();
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
        AccountsUserLogEntity newUserLog = new AccountsUserLogEntity();
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

    public String createCredentialJWT(String email) {

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            email.toLowerCase()
        );

        // Payload
        Map<String, String> credentialPayload = new LinkedHashMap<>();
        credentialPayload.put("id", findUser.get().getId());
        credentialPayload.put("email", findUser.get().getEmail());

        // Create raw JWT
        String credentialsTokenRaw = userJWTService.createCredential(
            credentialPayload
        );

        // Encrypt the JWT
        String encryptedCredential = encryptionService.encrypt(
            credentialsTokenRaw
        );

        return encryptedCredential;

    }

    public String createRefreshLogin(String email) {

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            email.toLowerCase()
        );

        // Get all tokens
        List<AccountsRefreshLoginEntity> findTokens =  refreshLoginRepository
            .findByEmail(
                email.toLowerCase()
            );

        // Sort tokens
        List<AccountsRefreshLoginEntity> sortedTokens = findTokens.stream()
            .sorted(Comparator.comparing(AccountsRefreshLoginEntity::getCreatedAt).reversed())
            .collect(Collectors.toList());

        // Delete tokens (more than 15 days)
        LocalDateTime dayLimit = LocalDateTime.now().minusDays(15);

        List<AccountsRefreshLoginEntity> deleteOldTokens = sortedTokens.stream()
            .filter(token -> token.getCreatedAt().isBefore(dayLimit))
            .collect(Collectors.toList());

        if (!deleteOldTokens.isEmpty()) {
            refreshLoginRepository.deleteAll(deleteOldTokens);
        }

        // Create raw refresh token
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());
        String secretWord = generatedUUID + email.toLowerCase() + nowTimestamp;
        String hashFinal = encryptionService.createToken(secretWord);

        // Encrypt refresh token
        String encryptedRefreshToken = encryptionService.encrypt(
            hashFinal
        );

        // Store refresh token
        AccountsRefreshLoginEntity newRefreshToken = new AccountsRefreshLoginEntity();
        newRefreshToken.setId(generatedUUID);
        newRefreshToken.setCreatedAt(nowTimestamp.toLocalDateTime());
        newRefreshToken.setUpdatedAt(nowTimestamp.toLocalDateTime());
        newRefreshToken.setEmail(email);
        newRefreshToken.setToken(encryptedRefreshToken);
        refreshLoginRepository.save(newRefreshToken);

        return encryptedRefreshToken;
    }

    public void deleteRefreshLogin(String refreshToken) {

        // find token
        Optional<AccountsRefreshLoginEntity> findToken= refreshLoginRepository
            .findByToken(
                refreshToken
            );

        refreshLoginRepository.deleteByToken(findToken.get().getToken());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllRefreshTokensByEmail(String email) {

        // find all tokens
        List<AccountsRefreshLoginEntity> findAllTokens= refreshLoginRepository
            .findByEmail(email);

        refreshLoginRepository.deleteAll(findAllTokens);

    }

}