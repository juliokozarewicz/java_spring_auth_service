package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsUpdatePasswordValidation;
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
public class AccountsUpdatePasswordService {

    // attributes
    private final MessageSource messageSource;
    private VerificationTokenRepository verificationTokenRepository;
    private ErrorHandler errorHandler;
    private AccountsRepository accountsRepository;
    private EncryptionControl encryptionControl;
    private AccountsManagementService accountsManagementService;

    // constructor
    public AccountsUpdatePasswordService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        VerificationTokenRepository verificationTokenRepository,
        AccountsRepository accountsRepository,
        EncryptionControl encryptionControl,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.verificationTokenRepository = verificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.accountsManagementService = accountsManagementService;

    }

    public ResponseEntity execute(

        AccountsUpdatePasswordValidation accountsActivateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find email and token
        Optional<VerificationTokenEntity> findEmailAndToken =
            verificationTokenRepository.findByEmailAndToken(
                accountsActivateValidation.email().toLowerCase(),
                accountsActivateValidation.token() + "_update-password"
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
                    "change_password_error", null, locale
                )
            );

        }

        // Update password
        if (

            findEmailAndToken.isPresent() &&
            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // update password
            findUser.get().setPassword(
                encryptionControl.hashPassword(
                    accountsActivateValidation.password()
                 )
            );

            // Active account if is deactivated
            if ( !findUser.get().isActive() ) {
                accountsManagementService.enableAccount(findUser.get().getId());
            }

        }

        // Delete all old tokens
        verificationTokenRepository
            .findByEmail(accountsActivateValidation.email().toLowerCase())
            .forEach(verificationTokenRepository::delete);

        // response (links)
        // ---------------------------------------------------------------------
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password");
        customLinks.put("next", "/accounts/login");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "change_password_ok",
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
