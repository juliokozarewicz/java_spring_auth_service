package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.ProfileEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EmailService;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsCreateValidation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsLinkUpdatePasswordService {

    // attributes
    @Value("${APPLICATION_TITLE}")
    private String applicatonTitle;

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private VerificationTokenRepository verificationTokenRepository;

    // constructor
    public AccountsLinkUpdatePasswordService(

        MessageSource messageSource,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        VerificationTokenRepository verificationTokenRepository

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.accountsManagementService = accountsManagementService;
        this.verificationTokenRepository = verificationTokenRepository;

    }

    @Transactional
    public ResponseEntity execute(

        AccountsCreateValidation accountsCreateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email().toLowerCase()
        );

        // #####
        if (

            findUser.isPresent() ||
            findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            verificationTokenRepository
                .findByEmail(accountsCreateValidation.email().toLowerCase())
                .forEach(verificationTokenRepository::delete);

            // Create token
            String tokenGenerated =
            accountsManagementService.createToken(
                accountsCreateValidation.email().toLowerCase(),
                "activate-email"
            );

            // Link
            String linkFinal = (
                accountsCreateValidation.link() +
                "?" +
                "email=" + accountsCreateValidation.email() +
                "&" +
                "token=" + tokenGenerated
            );

            // send email
            accountsManagementService.sendEmailStandard(
                accountsCreateValidation.email().toLowerCase(),
                "##### activation_email",
                linkFinal
            );

        }
        // ---------------------------------------------------------------------

        // response (links)
        // ---------------------------------------------------------------------
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password-link");
        customLinks.put("next", "/accounts/update-password");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "##### account_created_successfully",
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