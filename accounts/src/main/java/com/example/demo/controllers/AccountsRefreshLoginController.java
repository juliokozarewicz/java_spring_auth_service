package com.example.demo.controllers;

import com.example.demo.services.AccountsRefreshLoginService;
import com.example.demo.validations.AccountsRefreshLoginValidation;
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
class AccountsRefreshLoginController {

    // Service
    private final AccountsRefreshLoginService accountsRefreshLoginService;
    private final AccountsRequestValidation accountsRequestValidation;

    // constructor
    public AccountsRefreshLoginController(

        AccountsRefreshLoginService accountsRefreshLoginService,
        AccountsRequestValidation accountsRequestValidation

    ) {

        this.accountsRefreshLoginService = accountsRefreshLoginService;
        this.accountsRequestValidation = accountsRequestValidation;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/refresh-login")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsRefreshLoginValidation accountsRefreshLoginValidation,
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

        return accountsRefreshLoginService.execute(
            userIp,
            userAgent,
            accountsRefreshLoginValidation
        );

    }

}