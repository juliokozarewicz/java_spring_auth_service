package accounts.services;

import accounts.dtos.AccountsCacheRefreshTokenDTO;
import accounts.dtos.AccountsCacheUserMapRefreshDTO;
import accounts.dtos.SendEmailDataDTO;
import accounts.interfaces.AccountsManagementInterface;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsLogEntity;
import accounts.persistence.entities.AccountsVerificationTokenEntity;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountsManagementService implements AccountsManagementInterface {

    // Attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsLogRepository accountsLogRepository;
    private final UserJWTService userJWTService;
    private final AccountsKafkaService accountsKafkaService;
    private final CacheManager cacheManager;
    private final Cache pinVerificationCache;
    private final Cache refreshLoginCache;

    // Constructor
    public AccountsManagementService (

        AccountsVerificationTokenRepository accountsVerificationTokenRepository,
        MessageSource messageSource,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsLogRepository accountsLogRepository,
        UserJWTService userJWTService,
        AccountsKafkaService accountsKafkaService,
        CacheManager cacheManager

    ) {

        this.accountsVerificationTokenRepository = accountsVerificationTokenRepository;
        this.messageSource = messageSource;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;
        this.accountsLogRepository = accountsLogRepository;
        this.userJWTService = userJWTService;
        this.accountsKafkaService = accountsKafkaService;
        this.cacheManager = cacheManager;
        this.pinVerificationCache = cacheManager.getCache("pinVerificationCache");
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");

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

        // send userEmail body
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
            messageEmail.append("<b>" + link + "</b>").append("<br><br>");
        }

        // Close
        messageEmail.append(messageSource.getMessage(
            "email_closing", null, locale)
        ).append("<br>").append(applicatonTitle);

        String subject = "[ " + applicatonTitle + " ] - " + messageSource.
            getMessage(
                "email_subject_account", null, locale
            );

        // send userEmail dto
        SendEmailDataDTO sendEmailDataDTO = new SendEmailDataDTO(
            email,
            subject,
            messageEmail.toString()
        );

        accountsKafkaService.sendSimpleEmailMessage(sendEmailDataDTO);

    }

    @Override
    public String createVerificationToken(String email, String reason) {

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
        accountsVerificationTokenRepository.save(newToken);

        return hashFinal;

    }

    @Override
    public String createVerificationPin(
        String idUser,
        String reason
    ) {

        // Create pin
        int pin = new Random().nextInt(900000) + 100000;
        String pinCode = String.valueOf(pin);

        pinVerificationCache.put(
            idUser + "::" + reason + "::" + pinCode,
            Boolean.TRUE
            );

        return pinCode;

    }

    @Override
    public void deleteAllVerificationPinByUserId(String idUser) {
        pinVerificationCache.evict(idUser);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllVerificationTokenByEmailNewTransaction(String email) {

        // find all verification tokens
        List<AccountsVerificationTokenEntity> findAllTokens =
            accountsVerificationTokenRepository.findByEmail( email );

        accountsVerificationTokenRepository.deleteAll(findAllTokens);

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
        credentialPayload.put("userEmail", findUser.get().getEmail());

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
        AccountsCacheUserMapRefreshDTO userTokensDTO = refreshLoginCache.get(
            idUser,
            AccountsCacheUserMapRefreshDTO.class
        );

        List<String> tokensList;

        if (

            userTokensDTO == null ||
            userTokensDTO.getRefreshTokensActive() == null

        ) {

            tokensList = new ArrayList<>();

        } else {

            tokensList = new ArrayList<>(
                Arrays.asList(userTokensDTO.getRefreshTokensActive())
            );

        }

        tokensList.add(encryptedRefreshToken);

        AccountsCacheUserMapRefreshDTO updatedDTO = new AccountsCacheUserMapRefreshDTO(
            tokensList.toArray(new String[0])
        );

        refreshLoginCache.put(
            idUser,
            updatedDTO
        );
        // ---------------------------------------------------------------------

        return encryptedRefreshToken;

    }

    @Override
    public void deleteRefreshLoginByToken(String refreshToken) {

        refreshLoginCache.evict(refreshToken);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllRefreshTokensByIdNewTransaction(String userId) {

        // ##### delete all method

    }

}