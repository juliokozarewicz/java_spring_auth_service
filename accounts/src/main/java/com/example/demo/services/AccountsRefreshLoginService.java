package com.example.demo.services;

import com.example.demo.enums.EmailResponsesEnum;
import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.AccountsRefreshLoginEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.RefreshLoginRepository;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsRefreshLoginValidation;
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
public class AccountsRefreshLoginService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final RefreshLoginRepository refreshLoginRepository;

    // constructor
    public AccountsRefreshLoginService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        RefreshLoginRepository refreshLoginRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.accountsManagementService = accountsManagementService;
        this.refreshLoginRepository = refreshLoginRepository;

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsRefreshLoginValidation accountsRefreshLoginValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find token
        Optional<AccountsRefreshLoginEntity> findToken= refreshLoginRepository
            .findByToken(
                accountsRefreshLoginValidation.refreshToken()
            );

        // Invalid credentials
        if ( findToken.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // find email
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            findToken.get().getEmail().toLowerCase()
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

        // Account banned, disabled, or suspicious access
        if (

            findUser.get().isBanned() ||
            !findUser.get().isActive() ||
            !userIp.equals(findToken.get().getIpAddress()) ||
            !userAgent.equals(findToken.get().getAgent())

        ) {

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByEmail(
                findUser.get().getEmail().toLowerCase()
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Create JWT
        // ---------------------------------------------------------------------
        String AccessCredential = accountsManagementService.createCredentialJWT(
            findToken.get().getEmail().toLowerCase()
        );
        // ---------------------------------------------------------------------

        // Create refresh token
        // ---------------------------------------------------------------------
        String RefreshToken=  accountsManagementService.createRefreshLogin(
            userIp,
            userAgent,
            findToken.get().getEmail().toLowerCase()
        );
        // ---------------------------------------------------------------------

        // delete current token
        // ---------------------------------------------------------------------
        accountsManagementService.deleteRefreshLogin(
            accountsRefreshLoginValidation.refreshToken()
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