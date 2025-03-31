package com.example.demo.controllers;

import com.example.demo.services.AccountsLinkUpdatePasswordService;
import com.example.demo.validations.AccountsLinkUpdatePasswordValidation;
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
class AccountsLinkUpdatePasswordController {

    // Service
    private final AccountsLinkUpdatePasswordService accountsLinkUpdatePasswordService;

    // constructor
    public AccountsLinkUpdatePasswordController(
        AccountsLinkUpdatePasswordService accountsLinkUpdatePasswordService
    ) {
        this.accountsLinkUpdatePasswordService = accountsLinkUpdatePasswordService;
    }

    @PostMapping("${BASE_URL_ACCOUNTS}/update-password-link")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsLinkUpdatePasswordValidation accountsCreateValidation,
        BindingResult bindingResult

    ) {

        return accountsLinkUpdatePasswordService.execute(accountsCreateValidation);

    }

}