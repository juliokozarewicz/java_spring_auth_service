package com.example.demo.controllers;

import com.example.demo.services.AccountsUpdatePasswordService;
import com.example.demo.validations.AccountsUpdatePasswordValidation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class AccountsUpdatePasswordController {

    // Attributes
    private final AccountsUpdatePasswordService accountsUpdatePasswordService;

    // constructor
    public AccountsUpdatePasswordController(
        AccountsUpdatePasswordService accountsUpdatePasswordService
    ) {
        this.accountsUpdatePasswordService = accountsUpdatePasswordService;
    }

    @PatchMapping("${BASE_URL_ACCOUNTS}/update-password")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsUpdatePasswordValidation
        accountsUpdatePasswordValidation,
        BindingResult bindingResult

    ) {

        return accountsUpdatePasswordService.execute(
            accountsUpdatePasswordValidation
        );

    }

}