package com.example.demo.controllers;

import com.example.demo.services.AccountsCreateService;
import com.example.demo.validations.AccountsCreateValidation;
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
class AccountsCreateController {

    // Service
    private final AccountsCreateService accountsCreateService;

    // constructor
    public AccountsCreateController(
        AccountsCreateService accountsCreateService
    ) {
        this.accountsCreateService = accountsCreateService;
    }

    @GetMapping("${BASE_URL_ACCOUNTS}/accounts")
    public ResponseEntity handle(

        // validations errors
        @Valid AccountsCreateValidation accountsCreateValidation,
        BindingResult bindingResult

    ) {

        // message
        String message = accountsCreateValidation.message() != null ?
            accountsCreateValidation.message() : "Hello World!";

        return accountsCreateService.execute(message);

    }

}