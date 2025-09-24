package accounts.services;

import accounts.dtos.AccountsCacheVerificationTokenMetaDTO;
import accounts.dtos.AccountsUpdatePasswordDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsUpdatePasswordService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsManagementService accountsManagementService;
    private final AccountsLogRepository accountsLogRepository;
    private final CacheManager cacheManager;
    private final Cache verificationCache;
    private final Cache notActivatedAccountCache;
    private final Cache deletedAccountByUserCache;

    // constructor
    public AccountsUpdatePasswordService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsRepository accountsRepository,
        EncryptionService encryptionService,
        AccountsManagementService accountsManagementService,
        AccountsLogRepository accountsLogRepository,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsManagementService = accountsManagementService;
        this.accountsLogRepository = accountsLogRepository;
        this.cacheManager = cacheManager;
        this.verificationCache = cacheManager.getCache("verificationCache");
        this.notActivatedAccountCache = cacheManager.getCache("notActivatedAccountCache");
        this.deletedAccountByUserCache = cacheManager.getCache("deletedAccountByUserCache");

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsUpdatePasswordDTO accountsUpdatePasswordDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Decrypted email
        String decryptedEmail = encryptionService.decrypt(
            accountsUpdatePasswordDTO.email()
        );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            decryptedEmail
        );

        // Timestamp
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // find email and token
        AccountsCacheVerificationTokenMetaDTO findEmailAndToken = null;
        if (findUser.isPresent()) {
            findEmailAndToken = Optional
                .ofNullable(verificationCache.get(findUser.get().getId()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationTokenMetaDTO.class::cast)
                .filter(
                    tokenMeta -> accountsUpdatePasswordDTO
                        .token().equals(tokenMeta.getVerificationToken())
                )
                .filter(
                    tokenMeta -> tokenMeta
                        .getReason().equals(AccountsUpdateEnum.UPDATE_PASSWORD)
                )
                .orElse(null);
        }

        // email & token or account not exist
        if ( findEmailAndToken == null || findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_update_password_error", null, locale
                )
            );

        }

        // Update password
        if (

            findEmailAndToken != null &&
            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // Password hash
            String passwordHashed = encryptionService.hashPassword(
                accountsUpdatePasswordDTO.password()
            );

            // Update user log
            accountsManagementService.createUserLog(
                userIp,
                findUser.get().getId(),
                userAgent,
                AccountsUpdateEnum.UPDATE_PASSWORD,
                findUser.get().getPassword(),
                passwordHashed
            );

            // update password
            findUser.get().setPassword(passwordHashed);
            findUser.get().setUpdatedAt(nowUtc);
            accountsRepository.save(findUser.get());

            // Active account if is deactivated
            if ( !findUser.get().isActive() ) {
                accountsManagementService.enableAccount(findUser.get().getId());
            }

            // Evict not activated account cache
            notActivatedAccountCache.evict(findUser.get().getId());

            // Evict deleted account by user cache
            deletedAccountByUserCache.evict(findUser.get().getId());

        }

        // Delete all old tokens
        accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
            findUser.get().getId()
        );

        // Revoke all refresh tokens
        accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
            findUser.get().getId()
        );

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_password_success",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);
        // ---------------------------------------------------------------------

    }

}
