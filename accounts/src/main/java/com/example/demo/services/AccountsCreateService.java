package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsCreateValidation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsCreateService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;

    // constructor
    public AccountsCreateService (
        MessageSource messageSource,
        AccountsRepository accountsRepository
    ) {
        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
    }

    public ResponseEntity execute(
        AccountsCreateValidation accountsCreateValidation
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email()
        );

        // ##### user not existing
        /*
        Transaction:
            [] ACCOUNT
            [] PROFILE
            [] CODE
            [] Send email link (async)
         */

        // ##### user existing

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/signup");
        customLinks.put("next", "/accounts/activate-email");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "account_created_successfully",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();
        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}
