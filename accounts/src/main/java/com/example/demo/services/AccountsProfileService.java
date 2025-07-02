package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.utils.StandardResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class AccountsProfileService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsProfileService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }

    public ResponseEntity execute() {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success_xxx")
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}