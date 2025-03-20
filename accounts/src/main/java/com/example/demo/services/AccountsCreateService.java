package com.example.demo.services;

import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsCreateValidation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountsCreateService {

    // attributes
    private final MessageSource messageSource;

    // constructor
    public AccountsCreateService (
        MessageSource messageSource
    ) {
        this.messageSource = messageSource;
    }

    public ResponseEntity execute(
        AccountsCreateValidation accountsCreateValidation
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // continue here...

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
