package com.example.demo.controllers;

import com.example.demo.services.AccountsActivateService;
import com.example.demo.validations.AccountsActivateValidation;
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
class AccountsActivateController {

    // Service
    private final AccountsActivateService accountsActivateService;

    // constructor
    public AccountsActivateController(
        AccountsActivateService accountsActivateService
    ) {
        this.accountsActivateService = accountsActivateService;
    }

    @PostMapping("${BASE_URL_ACCOUNTS}/activate-account")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsActivateValidation accountsActivateValidation,
        BindingResult bindingResult

    ) {

        return accountsActivateService.execute(accountsActivateValidation);

    }

}