package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsActivateValidation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountsActivateService {

    // attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private VerificationTokenRepository verificationTokenRepository;
    private ErrorHandler errorHandler;
    private AccountsRepository accountsRepository;

    // constructor
    public AccountsActivateService (
        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        VerificationTokenRepository verificationTokenRepository,
        AccountsRepository accountsRepository
    ) {
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.accountsRepository = accountsRepository;
    }

    public ResponseEntity execute(
        AccountsActivateValidation accountsActivateValidation
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find email and token
        Optional<VerificationTokenEntity> findEmailAndToken =
            verificationTokenRepository.findByEmailAndToken(
                accountsActivateValidation.email().toLowerCase(),
                accountsActivateValidation.token()
            );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsActivateValidation.email().toLowerCase()
        );

        // email & token or account not exist
        if ( findEmailAndToken.isEmpty() || findUser.isEmpty() ) {
            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "activate_email_error", null, locale
                )
            );
        }

        // Active account
        if (
            !findEmailAndToken.isEmpty() &&
            !findUser.isEmpty() &&
            !findUser.get().isActive()
        ) {
           accountsManagementService.enableAccount(findUser.get().getId());
        }

        // Delete all old tokens
        verificationTokenRepository
            .findByEmail(accountsActivateValidation.email().toLowerCase())
            .forEach(verificationTokenRepository::delete);

        // response (links)
        // ---------------------------------------------------------------------
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/activate-email");
        customLinks.put("next", "/accounts/login");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "email_activate",
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
