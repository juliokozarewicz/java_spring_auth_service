package com.example.demo.controllers;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.services.AccountsActivateService;
import com.example.demo.validations.AccountsActivateValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping()
@Validated
class AccountsActivateController {

    // Service
    private final AccountsActivateService accountsActivateService;
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsActivateController(

        AccountsActivateService accountsActivateService,
        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.accountsActivateService = accountsActivateService;
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/activate-account")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsActivateValidation accountsActivateValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------
        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Regex for request
        Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4]" +
                "[0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
        );

        Pattern USER_AGENT_PATTERN = Pattern.compile(
            "^[\\w\\d\\s\\.\\/\\-\\(\\)\\;\\,\\:]+$"
        );

        if (

            userIp == null ||
                userIp.isEmpty() ||
                !IP_PATTERN.matcher(userIp).matches() ||
                userAgent == null ||
                userAgent.isEmpty() ||
                !USER_AGENT_PATTERN.matcher(userAgent).matches()

        ) {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_bad_request", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        return accountsActivateService.execute(
            userIp,
            userAgent,
            accountsActivateValidation
        );

    }

}