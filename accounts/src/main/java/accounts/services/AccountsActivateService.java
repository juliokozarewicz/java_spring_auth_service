package accounts.services;

import accounts.dtos.AccountsActivateDTO;
import accounts.dtos.AccountsCacheVerificationTokenMetaDTO;
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsActivateService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final AccountsLogRepository accountsLogRepository;
    private final EncryptionService encryptionService;
    private final CacheManager cacheManager;
    private final Cache verificationCache;
    private final Cache notActivatedAccountCache;

    // constructor
    public AccountsActivateService (

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        AccountsRepository accountsRepository,
        AccountsLogRepository accountsLogRepository,
        EncryptionService encryptionService,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.accountsRepository = accountsRepository;
        this.accountsLogRepository = accountsLogRepository;
        this.encryptionService = encryptionService;
        this.cacheManager = cacheManager;
        this.verificationCache = cacheManager.getCache("verificationCache");
        this.notActivatedAccountCache = cacheManager.getCache("notActivatedAccountCache");

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsActivateDTO accountsActivateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Decrypted email
        String decryptedEmail = encryptionService.decrypt(
            accountsActivateDTO.email()
        );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            decryptedEmail
        );

        // find email and token
        AccountsCacheVerificationTokenMetaDTO findEmailAndToken = null;
        if (findUser.isPresent()) {
            findEmailAndToken = Optional
                .ofNullable(verificationCache.get(findUser.get().getId().toString()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationTokenMetaDTO.class::cast)
                .filter(
                    tokenMeta -> accountsActivateDTO
                        .token().equals(tokenMeta.getVerificationToken())
                )
                .filter(
                    tokenMeta -> tokenMeta
                    .getReason().equals(AccountsUpdateEnum.ACTIVATE_ACCOUNT)
                )
                .orElse(null);
        }

        // email & token or account not exist
        if ( findEmailAndToken == null || findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_activate_account_error", null, locale
                )
            );

        }

        // Active account
        if (

            findEmailAndToken != null &&
            findUser.isPresent() &&
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Update user log
            accountsManagementService.createUserLog(
                userIp,
                findUser.get().getId(),
                userAgent,
                AccountsUpdateEnum.ACTIVATE_ACCOUNT,
                String.valueOf(findUser.get().isActive()),
                "true"
            );

            // active account in database
            accountsManagementService.enableAccount(findUser.get().getId());

            // Evict not activated account cache
            notActivatedAccountCache.evict(findUser.get().getId());

        }

        // Delete all old tokens
        accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
            findUser.get().getId()
        );

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/activate-email");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_account_activate",
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
