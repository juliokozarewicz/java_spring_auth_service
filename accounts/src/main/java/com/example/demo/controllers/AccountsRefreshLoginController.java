package com.example.demo.controllers;

import com.example.demo.services.AccountsRefreshLoginService;
import com.example.demo.utils.AuthService;
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
    private final AuthService authService;

    // constructor
    public AccountsRefreshLoginController(

        AccountsRefreshLoginService accountsRefreshLoginService,
        AccountsRequestValidation accountsRequestValidation,
        AuthService authService

    ) {

        this.accountsRefreshLoginService = accountsRefreshLoginService;
        this.accountsRequestValidation = accountsRequestValidation;
        this.authService = authService;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/refresh-login")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsRefreshLoginValidation accountsRefreshLoginValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // #####
        authService.validateCredentialJWT("AaHepXRqGOksGHKJpsfalmdr6eWGHT69JCAq/wNySZ//kqVozZXFddTEOZwhenU39TPS94XP0PI3nLy7IgxoJH1rtsgIjntkGwymbe1wZTVeE46CTe5sGa6dE4UApvo7y991XJtY4s1bblF/Ur2x8mKS0SPtGhseoUGOkfyjA7kN08O5sutFW7tJnsxUAryYnViTDeEbvdPVe9j2HSvlK7zD2Hoes5I8oDWDXp4lSGsp+BebpB9ypjZXwjGtWs6DazApLMecNSG7HaYmtfNghv/F4YR2TiGcosJrjiZ21AxoJWWa1rKBTN4gyjJFT1uc0zBzMeT9gnPaQIUjKl1/TFyAjvqkVQbxYg3YlEIMXEM2bZp+3RcSuwZxHyG/dwdWOXzxcQIEgglwr8AFUrRgDXkd3xbAJp5qXSMQKEGUUNhilj7vyBlEcwDyLOtptbGccqdkcXxAQOnrRrQrq1EK9ZKITx+m214AcCoZzo2upUJ1Pf4WjObtehMpsvje2elTubf7IoZ3GncVmerqkxyMRtkz+8+cWrwdOnI0IhsnT95mC8sCBYpAUPbLZU72xfY37qvr1NXMmskBuBRjYUiDu0CP0UkMzQoWTKRq8iXJoMHIeEdknwnamOO76wVVTHVcM0rj2Dak2cy8xt/UqeexieofT6ka5M5WeF/kgNWTB5k=");

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