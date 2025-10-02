package accounts.services;

import accounts.dtos.AccountsLoginDTO;
import accounts.enums.EmailResponsesEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsLoginService {

    // constructor
    // -------------------------------------------------------------------------
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;

    public AccountsLoginService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsManagementService = accountsManagementService;

    }
    // -------------------------------------------------------------------------

    // main method
    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsLoginDTO accountsLoginDTO

    ) {

        // Language
        Locale locale = LocaleContextHolder.getLocale();

        // Find user
        // ---------------------------------------------------------------------
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLoginDTO.email().toLowerCase()
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

        // Password compare
        // ---------------------------------------------------------------------
        boolean passwordCompare = encryptionService.matchPasswords(
            accountsLoginDTO.password(),
            findUser.get().getPassword()
        );
        // ---------------------------------------------------------------------

        // Invalid credentials
        // ---------------------------------------------------------------------
        if ( !passwordCompare ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Account banned
        // ---------------------------------------------------------------------
        if ( findUser.get().isBanned() ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
                findUser.get().getId()
            );

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_BANNED_ERROR,
                null
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_login_error", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Account deactivated
        // ---------------------------------------------------------------------
        if ( !findUser.get().isActive() ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
                findUser.get().getId()
            );

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_EXIST_DEACTIVATED_ERROR,
                null
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_login_error", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Create JWT
        // ---------------------------------------------------------------------
        String AccessCredential = accountsManagementService.createCredentialJWT(
            accountsLoginDTO.email().toLowerCase()
        );
        // ---------------------------------------------------------------------

        // Create refresh token
        // ---------------------------------------------------------------------

        // Clean old tokens
        accountsManagementService.deleteExpiredRefreshTokensListById(
            findUser.get().getId()
        );

        String RefreshToken=  accountsManagementService.createRefreshLogin(
            findUser.get().getId(),
            userIp,
            userAgent,
            null
        );
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/login");
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