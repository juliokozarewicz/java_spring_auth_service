package com.example.demo.controllers;

import com.example.demo.services.AccountsUpdatePasswordService;
import com.example.demo.validations.AccountsRequestValidation;
import com.example.demo.validations.AccountsUpdatePasswordValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class AccountsUpdatePasswordController {

    // Attributes
    private final AccountsUpdatePasswordService accountsUpdatePasswordService;
    private final AccountsRequestValidation accountsRequestValidation;

    // constructor
    public AccountsUpdatePasswordController(

        AccountsUpdatePasswordService accountsUpdatePasswordService,
        AccountsRequestValidation accountsRequestValidation

    ) {

        this.accountsUpdatePasswordService = accountsUpdatePasswordService;
        this.accountsRequestValidation = accountsRequestValidation;

    }

    @PatchMapping("${BASE_URL_ACCOUNTS}/update-password")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsUpdatePasswordValidation
            accountsUpdatePasswordValidation,
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

        return accountsUpdatePasswordService.execute(
            userIp,
            userAgent,
            accountsUpdatePasswordValidation
        );

    }

}