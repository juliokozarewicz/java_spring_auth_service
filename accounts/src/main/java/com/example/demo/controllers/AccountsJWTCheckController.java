package com.example.demo.controllers;

import com.example.demo.services.AccountsJWTCheckService;
import com.example.demo.validations.AccountsJWTCheckValidation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Validated
class AccountsJWTCheckController {

    // Service
    private final AccountsJWTCheckService accountsJWTCheckService;

    // constructor
    public AccountsJWTCheckController(

        AccountsJWTCheckService accountsJWTCheckService

    ) {

        this.accountsJWTCheckService = accountsJWTCheckService;

    }

    @GetMapping("${BASE_URL_ACCOUNTS}/jwt-credentials-validation")
    public ResponseEntity handle(

        // validations errors
        @Valid AccountsJWTCheckValidation accountsJWTCheckValidation,
        BindingResult bindingResult

    ) {

        return accountsJWTCheckService.execute(
            accountsJWTCheckValidation
        );

    }

}