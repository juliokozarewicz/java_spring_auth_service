package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.validations.AccountsJWTCheckValidation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountsJWTCheckService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsManagementService accountsManagementService;

    // constructor
    public AccountsJWTCheckService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;

    }

    public Map<String, Object> execute(

        AccountsJWTCheckValidation accountsJWTCheckValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        Map<String, Object> result = new HashMap<>();
        result.put("id", "id_example");
        result.put("email", "email_example");

        return result;

    }

}