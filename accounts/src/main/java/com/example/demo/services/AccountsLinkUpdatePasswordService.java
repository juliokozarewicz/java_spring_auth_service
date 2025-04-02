package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsLinkUpdatePasswordValidation;
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
public class AccountsLinkUpdatePasswordService {

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

        AccountsLinkUpdatePasswordValidation accountsLinkUpdatePasswordValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLinkUpdatePasswordValidation.email().toLowerCase()
        );

        if (

            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            verificationTokenRepository
                .findByEmail(accountsLinkUpdatePasswordValidation.email().toLowerCase())
                .forEach(verificationTokenRepository::delete);

            // Create token
            String tokenGenerated =
            accountsManagementService.createToken(
                accountsLinkUpdatePasswordValidation.email().toLowerCase(),
                "update-password"
            );

            // Link
            String linkFinal = (
                accountsLinkUpdatePasswordValidation.link() +
                "?" +
                "email=" + accountsLinkUpdatePasswordValidation.email() +
                "&" +
                "token=" + tokenGenerated
            );

            // send email
            accountsManagementService.sendEmailStandard(
                accountsLinkUpdatePasswordValidation.email().toLowerCase(),
                "change_password",
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
                    "change_password_sent_ok",
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