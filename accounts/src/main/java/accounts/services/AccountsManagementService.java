package accounts.services;

import accounts.dtos.*;
import accounts.interfaces.AccountsManagementInterface;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsLogEntity;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${APPLICATION_TITLE}")
    private String applicationTitle;

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsLogRepository accountsLogRepository;
    private final UserJWTService userJWTService;
    private final AccountsKafkaService accountsKafkaService;
    private final CacheManager cacheManager;
    private final Cache ArrayLoginsCache;
    private final Cache refreshLoginCache;
    private final Cache pinVerificationCache;
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
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");
        this.ArrayLoginsCache = cacheManager.getCache("ArrayLoginsCache");
        this.pinVerificationCache = cacheManager.getCache("pinVerificationCache");
        this.verificationCache = cacheManager.getCache("verificationCache");

    }

    @Override
    public UUID createUniqueId(){

        return UuidCreator.getTimeOrderedEpoch();

    }

    @Override
    public void enableAccount(UUID idUser) {

        // Timestamp
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            idUser
        );

        if ( findUser.isPresent() && !findUser.get().isBanned() ) {

            // Update
            AccountsEntity user = findUser.get();
            user.setActive(true);
            user.setUpdatedAt(nowUtc);
            accountsRepository.save(user);

        }

    }

    @Override
    public void disableAccount(UUID idUser) {

        // Timestamp
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            idUser
        );

        if ( findUser.isPresent() && !findUser.get().isBanned() ) {

            // Update
            AccountsEntity user = findUser.get();
            user.setActive(false);
            user.setUpdatedAt(nowUtc);
            accountsRepository.save(user);

        }

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
            boolean isUrl = link.startsWith("http://") || link.startsWith("https://");
            messageEmail.append("<b>")
                .append(isUrl ? "<a href=\"" + link + "\" target=\"_blank\">" + link + "</a>" : link)
                .append("</b><br><br>");
        }

        // Close
        messageEmail.append(messageSource.getMessage(
            "email_closing", null, locale)
        ).append("<br>").append(applicationTitle);

        String subject = "[ " + applicationTitle + " ] - " + messageSource.
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
    public String createVerificationToken(UUID idUser, String reason) {

        // Get hash
        String hashFinal = encryptionService.createToken();

        // Verification DTO
        AccountsCacheVerificationTokenMetaDTO verificationDTO =
            new AccountsCacheVerificationTokenMetaDTO();
            verificationDTO.setVerificationToken(hashFinal);
            verificationDTO.setReason(reason);

        // Clean old verification token
        verificationCache.evict(idUser);

        // Redis cache (hashFinal and reason in metadata)
        verificationCache.put(
            idUser,
            verificationDTO
        );

        return hashFinal;

    }

    @Override
    public String createVerificationPin(
        UUID idUser,
        String reason,
        Object meta
    ) {

        // Clean all old pin's
        pinVerificationCache.evict(idUser);

        // Create pin
        int pin = new Random().nextInt(900000) + 100000;
        String pinCode = String.valueOf(pin);

        AccountsCacheVerificationPinMetaDTO pinDTO =
            new AccountsCacheVerificationPinMetaDTO();
            pinDTO.setVerificationPin(pinCode);
            pinDTO.setReason(reason);
            pinDTO.setMeta(meta);

        pinVerificationCache.put(
            idUser,
            pinDTO
            );

        return pinCode;

    }

    @Override
    public void deletePinByIdUser(UUID idUser) {
        pinVerificationCache.evict(idUser);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllVerificationTokenByIdUserNewTransaction(UUID idUser) {

        verificationCache.evict(idUser);

    }

    @Override
    public void createUserLog(
        String ipAddress,
        UUID idUser,
        String agent,
        String updateType,
        String oldValue,
        String newValue
    ) {

        // ID and Timestamp
        UUID generatedUniqueId = createUniqueId();
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // Create log
        AccountsLogEntity newUserLog = new AccountsLogEntity();
        newUserLog.setId(generatedUniqueId);
        newUserLog.setCreatedAt(nowUtc);
        newUserLog.setIpAddress(ipAddress);
        newUserLog.setIdUser(idUser);
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
        credentialPayload.put("id", findUser.get().getId().toString());
        credentialPayload.put("email", findUser.get().getEmail());
        credentialPayload.put("level", findUser.get().getLevel());

        // Create raw JWT
        String credentialsTokenRaw = userJWTService.createCredential(
            credentialPayload
        );

        return encryptionService.encrypt(credentialsTokenRaw);

    }

    @Override
    public String createRefreshLogin(
        UUID idUser,
        String userIp,
        String userAgent,
        Instant createdAt
    ) {

        // Timestamp
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // Create raw refresh token
        String hashFinal = encryptionService.createToken();

        // Encrypt refresh token
        String encryptedRefreshToken = encryptionService.encrypt(
            hashFinal
        );

        // Redis cache token -> data
        // ---------------------------------------------------------------------
        Instant newCreatedAt = createdAt == null
            ? nowUtc
            : createdAt;

        AccountsCacheRefreshTokenDTO dtoRefreshToken = new
            AccountsCacheRefreshTokenDTO(
                idUser,
                userIp,
                userAgent,
                newCreatedAt
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

        refreshTokensList = (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null)
            ? new ArrayList<>()
            : new ArrayList<>(tokensDTO.getRefreshTokensActive());

        // Create metadata for the new token (including the timestamp)
        AccountsCacheRefreshTokensListMetaDTO newTokenMeta =
            new AccountsCacheRefreshTokensListMetaDTO(
            nowUtc, // Timestamp
            encryptedRefreshToken
        );

        // Add the new token metadata to the list
        refreshTokensList.add(newTokenMeta);

        // Update the cache with the new list of tokens and metadata
        AccountsCacheRefreshTokensListDTO updatedDTO =
            new AccountsCacheRefreshTokensListDTO(refreshTokensList);

        ArrayLoginsCache.put(idUser, updatedDTO);

        // --------------------------------------------------------------------

        return encryptedRefreshToken;

    }

    @Override
    public void deleteOneRefreshLogin(UUID idUser, String refreshToken) {

        refreshLoginCache.evict(refreshToken);

        // Get user's tokens with metadata
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            idUser,
            AccountsCacheRefreshTokensListDTO.class
        );

        if (
            tokensDTO == null || tokensDTO.getRefreshTokensActive() == null
        ) return;

        // Convert list of tokens with metadata to a mutable list
        List<AccountsCacheRefreshTokensListMetaDTO> tokensList = new ArrayList<>(
            tokensDTO.getRefreshTokensActive()
        );

        // Remove token from list
        boolean removed = tokensList.removeIf(
            tokenMeta -> tokenMeta
            .getRefreshToken().equals(refreshToken)
        );

        if (!removed) return; // Token not found, so we exit

        // Update the cache with the remaining tokens
        AccountsCacheRefreshTokensListDTO updatedDTO =
            new AccountsCacheRefreshTokensListDTO(tokensList);

        ArrayLoginsCache.put(idUser, updatedDTO);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllRefreshTokensByIdNewTransaction(UUID idUser) {

        // Recover all tokens by user id
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            idUser,
            AccountsCacheRefreshTokensListDTO.class
        );

        if (
            tokensDTO == null ||
            tokensDTO.getRefreshTokensActive() == null
        ) return;

        // Revoke all tokens by iterating over the metadata list
        for (
            AccountsCacheRefreshTokensListMetaDTO tokenMeta :
            tokensDTO.getRefreshTokensActive()
        ) {
            // Using the refreshToken from the metadata to delete it
            deleteOneRefreshLogin(idUser, tokenMeta.getRefreshToken());
        }

        // Evict user from the cache after revoking all tokens
        ArrayLoginsCache.evict(idUser);

    }

    @Override
    public void deleteExpiredRefreshTokensListById(UUID idUser) {

        // Retrieve all active tokens from the cache for the given user
        AccountsCacheRefreshTokensListDTO tokensDTO = ArrayLoginsCache.get(
            idUser,
            AccountsCacheRefreshTokensListDTO.class
        );

        // If no tokens exist or the list is empty, return early
        if (tokensDTO == null || tokensDTO.getRefreshTokensActive() == null) return;

        // Retrieve the list of tokens with metadata
        List<AccountsCacheRefreshTokensListMetaDTO> tokensList =
            new ArrayList<>(tokensDTO.getRefreshTokensActive());

        // Remove expired tokens (those older than 16 days)
        Instant fifteenDaysAgo = Instant.now().minus(15, ChronoUnit.DAYS);

        tokensList.removeIf(tokenMeta ->
            !tokenMeta.getTimestamp().isAfter(fifteenDaysAgo));

        // Update the cache with the filtered list of tokens
        AccountsCacheRefreshTokensListDTO updatedDTO =
            new AccountsCacheRefreshTokensListDTO(tokensList);

        ArrayLoginsCache.put(idUser, updatedDTO);

    }

}