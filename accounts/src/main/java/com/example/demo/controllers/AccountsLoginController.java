package com.example.demo.controllers;

import com.example.demo.services.AccountsLoginService;
import com.example.demo.validations.AccountsLoginValidation;
import com.example.demo.validations.AccountsRequestValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Validated
class AccountsLoginController {

    // Service
    private final AccountsLoginService accountsLoginService;
    private final AccountsRequestValidation accountsRequestValidation;

    // constructor
    public AccountsLoginController(

        AccountsLoginService accountsLoginService,
        AccountsRequestValidation accountsRequestValidation

    ) {

        this.accountsLoginService = accountsLoginService;
        this.accountsRequestValidation = accountsRequestValidation;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/login")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsLoginValidation accountsLoginValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------
        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestValidation.validateUserIp(userIp);
        accountsRequestValidation.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsLoginService.execute(
            userIp,
            userAgent,
            accountsLoginValidation
        );

    }

}