package accounts.services;

import accounts.dtos.AccountsCacheVerificationPinMetaDTO;
import accounts.dtos.AccountsCacheVerificationTokenMetaDTO;
import accounts.dtos.AccountsUpdateEmailDTO;
import accounts.enums.AccountsUpdateEnum;
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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

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

        String userIp,
        String userAgent,
        Map<String, Object> credentialsData,
        AccountsUpdateEmailDTO accountsUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // process to change email
        // ---------------------------------------------------------------------

        // find user
        Optional<AccountsEntity> findOldUser =  accountsRepository.findById(
            idUser
        );

        // Timestamp
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

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
        AccountsCacheVerificationPinMetaDTO pinDTO = null;
        if (findOldUser.isPresent()) {
            pinDTO = Optional
                .ofNullable(pinVerificationCache.get(findOldUser.get().getId()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationPinMetaDTO.class::cast)
                .filter(
                    tokenMeta -> accountsUpdateEmailDTO
                        .pin().equals(tokenMeta.getVerificationPin())
                )
                .filter(
                    tokenMeta -> tokenMeta
                    .getReason().equals(AccountsUpdateEnum.UPDATE_EMAIL)
                )
                .orElse(null);
        }

        // find old email and token
        AccountsCacheVerificationTokenMetaDTO findOldEmailAndToken = null;
        if (findOldUser.isPresent()) {
            findOldEmailAndToken = Optional
                .ofNullable(verificationCache.get(findOldUser.get().getId()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationTokenMetaDTO.class::cast)
                .filter(
                    tokenMeta -> accountsUpdateEmailDTO
                        .token().equals(tokenMeta.getVerificationToken())
                )
                .filter(
                    tokenMeta -> tokenMeta
                        .getReason().equals(AccountsUpdateEnum.UPDATE_EMAIL)
                )
                .orElse(null);
        }

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

        // Decoded new email
        String decodedNewEmail = String.valueOf(pinDTO.getMeta());

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

        // Create user log
        accountsManagementService.createUserLog(
            userIp,
            findOldUser.get().getId(),
            userAgent,
            AccountsUpdateEnum.UPDATE_EMAIL,
            findOldUser.get().getEmail(),
            decodedNewEmail
        );

        // Update database with new email
        findOldUser.get().setEmail(decodedNewEmail);
        findOldUser.get().setUpdatedAt(nowUtc);
        accountsRepository.save(findOldUser.get());

        // Revoke all refresh tokens
        accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
            findOldUser.get().getId()
        );

        // Delete current pin
        accountsManagementService.deletePinByIdUser(
            findOldUser.get().getId()
        );

        // Delete all verification tokens
        accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
            findOldUser.get().getId()
        );

        // Clean expired refresh tokens
        accountsManagementService.deleteExpiredRefreshTokensListById(
            findOldUser.get().getId()
        );

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