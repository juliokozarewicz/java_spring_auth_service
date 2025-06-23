package com.example.demo.controllers;

import com.example.demo.services.AccountsRefreshLoginService;
import com.example.demo.utils.AuthEndpointService;
import com.example.demo.validations.AccountsRefreshLoginValidation;
import com.example.demo.validations.AccountsRequestValidation;
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
class AccountsRefreshLoginController {

    // Service
    private final AccountsRefreshLoginService accountsRefreshLoginService;
    private final AccountsRequestValidation accountsRequestValidation;
    private final AuthEndpointService authEndpointService;

    // constructor
    public AccountsRefreshLoginController(

        AccountsRefreshLoginService accountsRefreshLoginService,
        AccountsRequestValidation accountsRequestValidation,
        AuthEndpointService authEndpointService

    ) {

        this.accountsRefreshLoginService = accountsRefreshLoginService;
        this.accountsRequestValidation = accountsRequestValidation;
        this.authEndpointService = authEndpointService;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/refresh-login")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsRefreshLoginValidation accountsRefreshLoginValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // #####
        authEndpointService.validateCredentialJWT("L47JnW3FnekcGcLU3QlccZc+bnyai5b1jrEgaHZCspXYdM/UGBvr4eOE7BfLcKub2tggeW/9KSu+JnXzQGNcxyDTJwDqtRl5CX0vzb88b4w/UTzSXwMjcP8xn7Ah/Y+xXrWRNgGDyXUrHwgmGKc2N/tn75uCsNt2a3LZopQkrB7eU+dgkK10BlfgPFXA4cBxZikTadMfqSJSFM9ucbfWUuxTK5xr554oYrR0MnQlLEJ98NLrV7eIsa7/Pw9BHmE3Liz/JJmhjko+lFdf+XZc10zLG+MISCVadJ/BSfRLtyL1AJxZ6Ki4x8+rsCfTPsMH9oT3OA7xBSJZCj2FytQQkKW/vUBFAXE69Mw+GCBTqDJnUk7SthihUKNGxrphPcqoo+WWDJUZqJ4kBLyHh0g4uszg+pKFk/1ckR+9yMlN123AjKnVBFQcByY3ETUytyDkGjxs0uPNKjACam+aMLU6v6ww5jSojhnIjD3FODpENqq1Zsig/6tbHqhnV5buz6fTcupPg/UaiBUyCayB7YAYVO4t6lm2SPnS/lyl4pyye/D4bZbTS9HlNoFxhvHuStIH6mJkZX+4PEH2cRTx4q+CxzzYuhLzkI5p1B9qhvvzKKgTu8ZR+feq7AP6XmyiqjVaOYi3nLOiDTSF1L3uDwhSRvC4txBC3tdFoLi7hFbsKks=");

        // Request data
        // ---------------------------------------------------------------------
        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestValidation.validateUserIp(userIp);
        accountsRequestValidation.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsRefreshLoginService.execute(
            userIp,
            userAgent,
            accountsRefreshLoginValidation
        );

    }

}