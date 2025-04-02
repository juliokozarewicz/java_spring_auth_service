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
    private final VerificationTokenRepository verificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;

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
                accountsActivateValidation.token() + "_activate-account"
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

            findEmailAndToken.isPresent() &&
            findUser.isPresent() &&
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

           accountsManagementService.enableAccount(findUser.get().getId());

           // ##### update user log

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
            .statusCode(200)
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
