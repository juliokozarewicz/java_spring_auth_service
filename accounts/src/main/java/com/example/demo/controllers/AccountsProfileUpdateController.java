package com.example.demo.controllers;

import com.example.demo.services.AccountsProfileUpdateService;
import com.example.demo.utils.AuthEndpointService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping()
@Validated
class AccountsProfileUpdateController {

    // Service
    private final AccountsProfileUpdateService accountsProfileUpdateService;
    private final AuthEndpointService authEndpointService;

    // constructor
    public AccountsProfileUpdateController(

        AccountsProfileUpdateService accountsProfileUpdateService,
        AuthEndpointService authEndpointService

    ) {

        this.accountsProfileUpdateService = accountsProfileUpdateService;
        this.authEndpointService = authEndpointService;

    }

    @PutMapping("${BASE_URL_ACCOUNTS}/profile-update")
    public ResponseEntity handle(

        HttpServletRequest request

    ) {

        // Auth endpoint
        String accessCredential = request.getHeader("Authorization");
        Map<String, String> credentialsData = authEndpointService
            .validateCredentialJWT(
                accessCredential != null ?
                accessCredential.replace("Bearer ", "") :
                null
        );

        return accountsProfileUpdateService.execute(credentialsData);

    }

}