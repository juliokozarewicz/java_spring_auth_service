package com.example.demo.controllers;

import com.example.demo.services.AccountsLoginService;
import com.example.demo.validations.AccountsLoginValidation;
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

    // constructor
    public AccountsLoginController(
        AccountsLoginService accountsLoginService
    ) {
        this.accountsLoginService = accountsLoginService;
    }

    @PostMapping("${BASE_URL_ACCOUNTS}/login")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsLoginValidation accountsLoginValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        return accountsLoginService.execute(
            userIp,
            userAgent,
            accountsLoginValidation
        );

    }

}