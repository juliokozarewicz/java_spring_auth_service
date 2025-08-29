package accounts.services;

import accounts.dtos.AccountsCacheVerificationPinMetaDTO;
import accounts.dtos.AccountsCacheVerificationTokenMetaDTO;
import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.dtos.AccountsUpdateEmailDTO;
import accounts.enums.EmailResponsesEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsUpdateEmailService {

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final ErrorHandler errorHandler;
    private final Cache pinVerificationCache;
    private final Cache verificationCache;

    // constructor
    public AccountsUpdateEmailService(

        MessageSource messageSource,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        ErrorHandler errorHandler,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.accountsManagementService = accountsManagementService;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;
        this.errorHandler = errorHandler;
        this.pinVerificationCache = cacheManager.getCache("pinVerificationCache");
        this.verificationCache = cacheManager.getCache("verificationCache");

    }

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsUpdateEmailDTO accountsUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String emailUser = credentialsData.get("email").toString();

        // process to change email
        // ---------------------------------------------------------------------

        // Encoded old email
        String encodedOldEmail = encryptionService.encodeBase64(
            emailUser
        );

        // find user
        Optional<AccountsEntity> findOldUser =  accountsRepository.findByEmail(
            emailUser
        );

        if ( findOldUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_update_email_fail", null, locale
                )
            );

        }

        // find old email and pin
        AccountsCacheVerificationPinMetaDTO pinDTO = Optional.ofNullable(pinVerificationCache.get(encodedOldEmail))
            .map(Cache.ValueWrapper::get)
            .map(AccountsCacheVerificationPinMetaDTO.class::cast)
            .filter(tokenMeta -> accountsUpdateEmailDTO.pin().equals(tokenMeta.getVerificationPin()))
            .orElse(null);

        // Find old email and token
        AccountsCacheVerificationTokenMetaDTO findOldEmailAndToken =
            Optional.ofNullable(verificationCache.get(encodedOldEmail))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationTokenMetaDTO.class::cast)
                .filter(tokenMeta -> accountsUpdateEmailDTO.token().equals(tokenMeta.getVerificationToken()))
                .orElse(null);

        // If token or email not found return error
        if ( pinDTO == null || findOldEmailAndToken == null ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_update_email_fail", null, locale
                )
            );

        }

        // If new email exist return error
        Optional<AccountsEntity> findNewUser =  accountsRepository.findByEmail(
            String.valueOf(pinDTO.getMeta())
        );

        if ( findNewUser.isPresent() ) {

            // call custom error
            errorHandler.customErrorThrow(
                409,
                messageSource.getMessage(
                    "response_update_email_fail", null, locale
                )
            );

        }
        // If password not match return error
        boolean passwordCompare = encryptionService.matchPasswords(
            accountsUpdateEmailDTO.password(),
            findOldUser.get().getPassword()
        );

        // Invalid credentials
        if ( !passwordCompare ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_update_email_fail", null, locale
                )
            );

        }

        // ##### Update database with new email
        // ##### Clean all refresh tokens
        // ##### Create user log

        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-email");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_email_success",
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