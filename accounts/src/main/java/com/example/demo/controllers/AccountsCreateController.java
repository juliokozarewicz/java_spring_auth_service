package com.example.demo.controllers;

import com.example.demo.services.AccountsCreateService;
import com.example.demo.validations.AccountsCreateValidation;
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
class AccountsCreateController {

    // Service
    private final AccountsCreateService accountsCreateService;

    // constructor
    public AccountsCreateController(
        AccountsCreateService accountsCreateService
    ) {
        this.accountsCreateService = accountsCreateService;
    }

    @PostMapping("${BASE_URL_ACCOUNTS}/signup")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsCreateValidation accountsCreateValidation,
        BindingResult bindingResult

    ) {

        return accountsCreateService.execute(accountsCreateValidation);

    }

}