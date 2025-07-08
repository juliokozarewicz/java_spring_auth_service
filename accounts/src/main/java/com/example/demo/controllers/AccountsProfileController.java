package com.example.demo.controllers;

import com.example.demo.services.AccountsProfileService;
import com.example.demo.utils.AuthEndpointService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping()
@Validated
class AccountsProfileController {

    // Service
    private final AccountsProfileService accountsProfileService;
    private final AuthEndpointService authEndpointService;

    // constructor
    public AccountsProfileController(

        AccountsProfileService accountsProfileService,
        AuthEndpointService authEndpointService

    ) {

        this.accountsProfileService = accountsProfileService;
        this.authEndpointService = authEndpointService;

    }

    @GetMapping("${BASE_URL_ACCOUNTS}/profile")
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

        return accountsProfileService.execute(credentialsData);

    }

}