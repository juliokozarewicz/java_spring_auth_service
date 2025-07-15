package com.example.demo.controllers;

import com.example.demo.services.AccountsProfileUpdateService;
import com.example.demo.validations.AccountsProfileUpdateValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping()
@Validated
class AccountsProfileUpdateController {

    // Service
    private final AccountsProfileUpdateService accountsProfileUpdateService;

    // constructor
    public AccountsProfileUpdateController(

        AccountsProfileUpdateService accountsProfileUpdateService

    ) {

        this.accountsProfileUpdateService = accountsProfileUpdateService;

    }

    @PutMapping("${BASE_URL_ACCOUNTS}/profile-update")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody(required = false)
        AccountsProfileUpdateValidation accountsProfileUpdateValidation,

        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
            request.getAttribute("credentialsData");

        return accountsProfileUpdateService.execute(
            credentialsData,
            accountsProfileUpdateValidation
        );

    }

}