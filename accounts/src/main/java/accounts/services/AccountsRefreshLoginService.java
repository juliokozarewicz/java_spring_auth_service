package accounts.services;

import accounts.dtos.AccountsCacheRefreshTokenDTO;
import accounts.dtos.AccountsRefreshLoginDTO;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsRefreshLoginService {

    // constructor
    // -------------------------------------------------------------------------
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final CacheManager cacheManager;
    private final Cache refreshLoginCache;

    public AccountsRefreshLoginService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.accountsManagementService = accountsManagementService;
        this.cacheManager = cacheManager;
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");

    }
    // -------------------------------------------------------------------------

    // Main method
    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsRefreshLoginDTO accountsRefreshLoginDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Find token
        // ---------------------------------------------------------------------
        AccountsCacheRefreshTokenDTO findToken = refreshLoginCache.get(
            accountsRefreshLoginDTO.refreshToken(),
            AccountsCacheRefreshTokenDTO.class
        );
        // ---------------------------------------------------------------------

        // Invalid credentials
        // ---------------------------------------------------------------------
        if ( findToken == null ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // find email
        // ---------------------------------------------------------------------
        Optional<AccountsEntity> findUser =  accountsRepository.findById(
            findToken.getIdUser()
        );
        // ---------------------------------------------------------------------

        // Invalid credentials
        // ---------------------------------------------------------------------
        if ( findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Account banned, disabled, or suspicious access
        // ---------------------------------------------------------------------
        if (

            findUser.get().isBanned() ||
            !findUser.get().isActive() ||
            !userIp.equals(findToken.getUserIp()) ||
            !userAgent.equals(findToken.getUserAgent())

        ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
                findToken.getIdUser()
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Expired token
        // ---------------------------------------------------------------------
        Instant fifteenDaysAgo = Instant.now()
            .minus(15, java.time.temporal.ChronoUnit.DAYS);
        // ---------------------------------------------------------------------

        // Invalid credentials
        // ---------------------------------------------------------------------
        if ( findToken.getCreatedAt().isBefore(fifteenDaysAgo) ) {

            accountsManagementService.deleteOneRefreshLogin(
                findUser.get().getId(),
                accountsRefreshLoginDTO.refreshToken()
            );

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Create JWT
        // ---------------------------------------------------------------------
        String AccessCredential = accountsManagementService.createCredentialJWT(
            findUser.get().getEmail()
        );
        // ---------------------------------------------------------------------

        // Create refresh token
        // ---------------------------------------------------------------------
        String RefreshToken=  accountsManagementService.createRefreshLogin(
            findUser.get().getId(),
            userIp,
            userAgent,
            findToken.getCreatedAt()
        );
        // ---------------------------------------------------------------------

        // delete current token
        // ---------------------------------------------------------------------
        accountsManagementService.deleteOneRefreshLogin(
            findUser.get().getId(),
            accountsRefreshLoginDTO.refreshToken()
        );
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/refresh-login");
        customLinks.put("next", "/accounts/profile-get");

        // Tokens data
        Map<String, String> tokensData = new LinkedHashMap<>();
        tokensData.put("access", AccessCredential);
        tokensData.put("refresh", RefreshToken);

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_login_success",
                    null,
                    locale
                )
            )
            .data(tokensData)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

        // ---------------------------------------------------------------------

    }

}