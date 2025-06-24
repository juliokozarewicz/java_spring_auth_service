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
        authEndpointService.validateCredentialJWT("Kfv3Bpo+h4X//vk9ceaA1/3DqB8jdDJl3Kx49h03pVtFINOZFQaEqRZxKrDJyou8tUxK3fJpjn80WdO1S7C6QI6ZyYH5pf38LwfZeI7PTC80XqclfYa3vjDFGUMbuVNBguwKRU4X1YhJgMESreL8fE6/Em0VXaTKwuT72dQ6nT12XqL83o+PPlG6w1ZO84Oxj1ihAv0X5o1sgPWMLYbvd79jjhaNhXftQoEMaFHR9DyVD8SB59yfnI9IFQhgToZb/waJ+OD2dJ6pkUuYgOVmXQp10G7LrNprI/GNJITUkBF4k7HI+kdJy7vvUeqnAjLSIGOnKPzibfghhUmpiwZTVbqns91xduUlXpBFDBO+o3aVOouS3NlYXUhoxnRB5jvy0az+1Iasq8+/YObHcOgOvyRFIGFy3iB0qKgPAK3Q9bzUOLLzubJbFE9h2MbkR1lfaiZqtSG8FJZ06NBGQcvvchLgoMwOkTq38KFiJTi7B0j7QJc1M/hyan8PLOuMUgUx3PCJIOu+iAa04dk3FgOFNwutchbN+cQ7xA97/ZI+2SUYwXpSvrvGDzr2ejZ3LGw3X+zKMlYZJ/WTPIMzLPq5iQjHB54vOzX4349/JRts/Xikmrzlr0E65yYvhH+Wo/uDdItyFZPJexVPIWrZYD+2MKc+LclNU2yOWidzXP0LwV8=");

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