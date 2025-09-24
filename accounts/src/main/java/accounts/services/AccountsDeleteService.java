package accounts.services;

import accounts.dtos.AccountsCacheVerificationTokenMetaDTO;
import accounts.dtos.AccountsDeleteDTO;
import accounts.enums.AccountsUpdateEnum;
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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsDeleteService {

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final ErrorHandler errorHandler;
    private final Cache deletedAccountByUserCache;
    private final Cache verificationCache;
    private final Cache profileCache;
    private final Cache addressCache;

    // constructor
    public AccountsDeleteService(

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
        this.deletedAccountByUserCache = cacheManager.getCache("deletedAccountByUserCache");
        this.verificationCache = cacheManager.getCache("verificationCache");
        this.addressCache = cacheManager.getCache("addressCache");
        this.profileCache = cacheManager.getCache("profileCache");

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        Map<String, Object> credentialsData,
        AccountsDeleteDTO accountsDeleteDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // process to delete account
        // ---------------------------------------------------------------------

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            idUser
        );

        // User not exist
        if ( findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_delete_account_error", null, locale
                )
            );

        }

        // find email and token
        AccountsCacheVerificationTokenMetaDTO findEmailAndToken = null;
        if (findUser.isPresent()) {
            findEmailAndToken = Optional
                .ofNullable(verificationCache.get(findUser.get().getId().toString()))
                .map(Cache.ValueWrapper::get)
                .map(AccountsCacheVerificationTokenMetaDTO.class::cast)
                .filter(
                    tokenMeta -> accountsDeleteDTO
                        .token().equals(tokenMeta.getVerificationToken())
                )
                .filter(
                    tokenMeta -> tokenMeta
                        .getReason().equals(AccountsUpdateEnum.DELETE_ACCOUNT)
                )
                .orElse(null);
        }

        // email & token or account not exist
        if ( findEmailAndToken == null ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_delete_account_error", null, locale
                )
            );

        }

        // Deactivate account
        accountsManagementService.disableAccount(findUser.get().getId());

        // Create user log
        accountsManagementService.createUserLog(
            userIp,
            findUser.get().getId(),
            userAgent,
            AccountsUpdateEnum.DELETE_ACCOUNT,
            "activated",
            "deleted"
        );

        // Set delete account cache
        deletedAccountByUserCache.put(
            findUser.get().getId(),
            ZonedDateTime.now(ZoneOffset.UTC).toInstant()
        );

        // Revoke user data
        profileCache.evict(findUser.get().getId());
        addressCache.evict(findUser.get().getId());

        // Revoke all refresh tokens
        accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
            findUser.get().getId()
        );

        // Delete all verification tokens
        accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
            findUser.get().getId()
        );

        // Clean expired refresh tokens
        accountsManagementService.deleteExpiredRefreshTokensListById(
            findUser.get().getId()
        );

        // send email
        accountsManagementService.sendEmailStandard(
            findUser.get().getEmail(),
            EmailResponsesEnum.ACCOUNT_DELETED_TIME,
            null
        );

        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/delete");
        customLinks.put("next", "/accounts/signup");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_delete_account_success",
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