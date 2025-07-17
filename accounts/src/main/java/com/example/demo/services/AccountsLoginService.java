package com.example.demo.services;

import com.example.demo.enums.EmailResponsesEnum;
import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.EncryptionService;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsLoginValidation;
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

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;

    // constructor
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

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsLoginValidation accountsLoginValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLoginValidation.email().toLowerCase()
        );

        // Invalid credentials
        if ( findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Password compare
        boolean passwordCompare = encryptionService.matchPasswords(
            accountsLoginValidation.password(),
            findUser.get().getPassword()
        );

        // Invalid credentials
        if ( !passwordCompare ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Account banned
        if ( findUser.get().isBanned() ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByEmail(
                findUser.get().getEmail().toLowerCase()
            );

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_BANNED_ERROR.getDescription(),
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

        // Account deactivated
        if ( !findUser.get().isActive() ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByEmail(
                findUser.get().getEmail().toLowerCase()
            );

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_EXIST_DEACTIVATED_ERROR.getDescription(),
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

        // Create JWT
        // ---------------------------------------------------------------------
        String AccessCredential = accountsManagementService.createCredentialJWT(
            accountsLoginValidation.email().toLowerCase()
        );
        // ---------------------------------------------------------------------

        // Create refresh token
        // ---------------------------------------------------------------------
        String RefreshToken=  accountsManagementService.createRefreshLogin(
            userIp,
            userAgent,
            accountsLoginValidation.email().toLowerCase()
        );
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/login");
        customLinks.put("next", "/accounts/profile");

        // Tokens data
        Map<String, String> tokensData = new LinkedHashMap<>();
        tokensData.put("access", AccessCredential);
        tokensData.put("refresh", RefreshToken);

        StandardResponse response = new StandardResponse.Builder()
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