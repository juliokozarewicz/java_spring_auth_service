package accounts.controllers;

import accounts.dtos.AccountsUpdateEmailDTO;
import accounts.services.AccountsUpdateEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping()
@Validated
class AccountsUpdateEmailController {

    // Service
    private final AccountsUpdateEmailService accountsUpdateEmailService;

    // constructor
    public AccountsUpdateEmailController(

        AccountsUpdateEmailService accountsUpdateEmailService

    ) {

        this.accountsUpdateEmailService = accountsUpdateEmailService;

    }

    @PatchMapping("${BASE_URL_ACCOUNTS}/update-email")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody(required = false)
        AccountsUpdateEmailDTO accountsUpdateEmailDTO,

        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
            request.getAttribute("credentialsData");

        return accountsUpdateEmailService.execute(
            credentialsData,
            accountsUpdateEmailDTO
        );

    }

}