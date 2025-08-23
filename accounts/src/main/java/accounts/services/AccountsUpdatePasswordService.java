package accounts.services;

import accounts.dtos.AccountsCacheVerificationMetaDTO;
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

    }

    @Transactional
    public ResponseEntity execute(
        String userIp,
        String userAgent,
        AccountsUpdatePasswordDTO accountsUpdatePasswordDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Decoded userEmail
        String decodedEmail = encryptionService.decodeBase64(
            accountsUpdatePasswordDTO.email()
        );

        // find userEmail and token
        AccountsCacheVerificationMetaDTO findEmailAndToken =
            Optional.ofNullable(verificationCache.get(accountsUpdatePasswordDTO.email()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationMetaDTO.class::cast)
                .orElse(null);

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            decodedEmail
        );

        // userEmail & token or account not exist
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

            // Active account if is deactivated
            if ( !findUser.get().isActive() ) {
                accountsManagementService.enableAccount(findUser.get().getId());
            }

        }

        // Delete all old tokens
        accountsManagementService.deleteAllVerificationTokenByEmailNewTransaction(
            accountsUpdatePasswordDTO.email()
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

    };

}
