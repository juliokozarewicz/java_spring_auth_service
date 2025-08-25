package accounts.services;

import accounts.dtos.*;
import accounts.interfaces.AccountsManagementInterface;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsLogEntity;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsLogRepository accountsLogRepository;
    private final UserJWTService userJWTService;
    private final AccountsKafkaService accountsKafkaService;
    private final CacheManager cacheManager;
    private final Cache pinVerificationCache;
    private final Cache refreshLoginCache;
    private final Cache ArrayLoginsCache;
    private final Cache verificationCache;

    // Constructor
    public AccountsManagementService (

        MessageSource messageSource,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsLogRepository accountsLogRepository,
        UserJWTService userJWTService,
        AccountsKafkaService accountsKafkaService,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;
        this.accountsLogRepository = accountsLogRepository;
        this.userJWTService = userJWTService;
        this.accountsKafkaService = accountsKafkaService;
        this.cacheManager = cacheManager;
        this.pinVerificationCache = cacheManager.getCache("pinVerificationCache");
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");
        this.ArrayLoginsCache = cacheManager.getCache("ArrayLoginsCache");
        this.verificationCache = cacheManager.getCache("verificationCache");

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
        ).append("<br><br>");

        // Body
        messageEmail.append(messageSource.getMessage(
            message, null, locale)
        ).append("<br><br>");

        // add link if exist
        if (link != null && !link.isEmpty()) {

            if (link.startsWith("http://") || link.startsWith("https://")) {

                messageEmail.append("<b><a href=\"").append(link).append("\" target=\"_blank\">")
                    .append(link).append("</a></b>").append("<br><br>");

            } else {

                messageEmail.append("<b>").append(link).append("</b>").append("<br><br>");

            }
        }

        // Close
        messageEmail.append(messageSource.getMessage(
            "email_closing", null, locale)
        ).append("<br>").append(applicatonTitle);

        String subject = "[ " + applicatonTitle + " ] - " + messageSource.
            getMessage(
                "email_subject_account", null, locale
            );

        // send email dto
        SendEmailDataDTO sendEmailDataDTO = new SendEmailDataDTO(
            email,
            subject,
            messageEmail.toString()
        );

        accountsKafkaService.sendSimpleEmailMessage(sendEmailDataDTO);

    }

    @Override
    public String createVerificationToken(String email) {

        // UUID
        String generatedUUID = UUID.randomUUID().toString();

        // Timestamp
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // Concatenates everything
        String secretWord = generatedUUID + email + nowTimestamp;

        // Get hash
        String hashFinal = encryptionService.createToken(secretWord);

        // Verification DTO
        AccountsCacheVerificationTokenMetaDTO verificationDTO = new AccountsCacheVerificationTokenMetaDTO();
        verificationDTO.setVerificationToken(hashFinal);

        // Clean old verification token
        verificationCache.evict(email);

        // Redis cache (hashFinal and reason in metadata)
        verificationCache.put(
            email,
            verificationDTO
        );

        return hashFinal;

    }

    @Override
    public String createVerificationPin(
        String email
    ) {

        // Clean all old pin's
        pinVerificationCache.evict(email);

        // Create pin
        int pin = new Random().nextInt(900000) + 100000;
        String pinCode = String.valueOf(pin);

        AccountsCacheVerificationPinMetaDTO pinDTO = new AccountsCacheVerificationPinMetaDTO();
        pinDTO.setVerificationPin(pinCode);

        pinVerificationCache.put(
            email,
            pinDTO
            );

        return pinCode;

    }

    @Override
    public void deletePinByEmail(String email) {
        pinVerificationCache.evict(email);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllVerificationTokenByEmailNewTransaction(String email) {

        verificationCache.evict(email);

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
        AccountsLogEntity newUserLog = new AccountsLogEntity();
        newUserLog.setId(generatedUUID);
        newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
        newUserLog.setIpAddress(ipAddress);
        newUserLog.setUserId(userId);
        newUserLog.setAgent(agent);
        newUserLog.setUpdateType(updateType);
        newUserLog.setOldValue(oldValue);
        newUserLog.setNewValue(newValue);
        accountsLogRepository.save(newUserLog);

    }

    @Override
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

    @Override
    public String createRefreshLogin(
        String idUser,
        String userIp,
        String userAgent
    ) {

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            idUser
        );

        // Create raw refresh token
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());
        String secretWord = generatedUUID + idUser + nowTimestamp;
        String hashFinal = encryptionService.createToken(secretWord);

        // Encrypt refresh token
        String encryptedRefreshToken = encryptionService.encrypt(
            hashFinal
        );

        // Redis cache token -> data
        // ---------------------------------------------------------------------
        AccountsCacheRefreshTokenDTO dtoRefreshToken = new
            AccountsCacheRefreshTokenDTO(
                idUser,
                userIp,
                userAgent
            );

        refreshLoginCache.put(
            encryptedRefreshToken,
            dtoRefreshToken
        );

        // Redis cache idUser -> tokens
        // ---------------------------------------------------------------------
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            idUser,
            AccountsCacheRefreshTokensListDTO.class
        );

        List<AccountsCacheRefreshTokensListMetaDTO> refreshTokensList;

        if (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null) {
            refreshTokensList = new ArrayList<>();
        } else {
            refreshTokensList = new ArrayList<>(tokensDTO.getRefreshTokensActive());
        }

        // Create metadata for the new token (including the timestamp)
        AccountsCacheRefreshTokensListMetaDTO newTokenMeta = new AccountsCacheRefreshTokensListMetaDTO(
            nowUtc.toInstant(), // Timestamp of creation
            encryptedRefreshToken
        );

        // Add the new token metadata to the list
        refreshTokensList.add(newTokenMeta);

        // Update the cache with the new list of tokens and metadata
        AccountsCacheRefreshTokensListDTO updatedDTO = new AccountsCacheRefreshTokensListDTO(refreshTokensList);

        ArrayLoginsCache.put(idUser, updatedDTO);

        // --------------------------------------------------------------------

        return encryptedRefreshToken;

    }

    @Override
    public void deleteOneRefreshLogin(String idUser, String refreshToken) {

        refreshLoginCache.evict(refreshToken);

        // Get user's tokens with metadata
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            idUser,
            AccountsCacheRefreshTokensListDTO.class
        );

        if (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null) return;

        // Convert list of tokens with metadata to a mutable list
        List<AccountsCacheRefreshTokensListMetaDTO> tokensList = new ArrayList<>(tokensDTO.getRefreshTokensActive());

        // Remove token from list
        boolean removed = tokensList.removeIf(tokenMeta -> tokenMeta.getRefreshToken().equals(refreshToken));

        if (!removed) return; // Token not found, so we exit

        // Update the cache with the remaining tokens
        AccountsCacheRefreshTokensListDTO updatedDTO = new AccountsCacheRefreshTokensListDTO(tokensList);
        ArrayLoginsCache.put(idUser, updatedDTO);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllRefreshTokensByIdNewTransaction(String userId) {

        // Recover all tokens by user id
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            userId,
            AccountsCacheRefreshTokensListDTO.class
        );

        if (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null) return;

        // Revoke all tokens by iterating over the metadata list
        for (AccountsCacheRefreshTokensListMetaDTO tokenMeta : tokensDTO.getRefreshTokensActive()) {
            // Using the refreshToken from the metadata to delete it
            deleteOneRefreshLogin(userId, tokenMeta.getRefreshToken());
        }

        // Evict user from the cache after revoking all tokens
        ArrayLoginsCache.evict(userId);

    }

    @Override
    public void cleanExpiredRefreshTokensList(String userId) {

        // Retrieve all active tokens from the cache for the given user
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            userId,
            AccountsCacheRefreshTokensListDTO.class
        );

        // If no tokens exist or the list is empty, return early
        if (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null) return;

        // Calculate the threshold date (16 days ago from now)
        Instant sixteenDaysAgo = Instant.now().minus(15, ChronoUnit.DAYS);

        // Retrieve the list of tokens with metadata
        List<AccountsCacheRefreshTokensListMetaDTO> tokensList = new ArrayList<>(tokensDTO.getRefreshTokensActive());

        // Remove expired tokens (those older than 16 days)
        tokensList.removeIf(tokenMeta -> tokenMeta.getTimestamp().isBefore(sixteenDaysAgo));

        // Update the cache with the filtered list of tokens
        AccountsCacheRefreshTokensListDTO updatedDTO = new AccountsCacheRefreshTokensListDTO(tokensList);
        ArrayLoginsCache.put(userId, updatedDTO);

    }

}