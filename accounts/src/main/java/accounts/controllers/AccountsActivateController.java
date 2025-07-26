package accounts.controllers;

import accounts.services.AccountsActivateService;
import accounts.validations.AccountsActivateValidation;
import accounts.validations.AccountsRequestValidation;
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
class AccountsActivateController {

    // Service
    private final AccountsActivateService accountsActivateService;
    private final AccountsRequestValidation accountsRequestValidation;

    // constructor
    public AccountsActivateController(

        AccountsActivateService accountsActivateService,
        AccountsRequestValidation accountsRequestValidation

    ) {

        this.accountsActivateService = accountsActivateService;
        this.accountsRequestValidation = accountsRequestValidation;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/activate-account")
    public ResponseEntity handle(

        // validations errors
        @Valid @RequestBody AccountsActivateValidation accountsActivateValidation,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------
        String userIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestValidation.validateUserIp(userIp);
        accountsRequestValidation.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsActivateService.execute(
            userIp,
            userAgent,
            accountsActivateValidation
        );

    }

}