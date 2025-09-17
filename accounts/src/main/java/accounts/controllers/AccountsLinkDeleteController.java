package accounts.controllers;

import accounts.dtos.AccountsLinkDeleteDTO;
import accounts.services.AccountsLinkDeleteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping()
@Validated
class AccountsLinkDeleteController {

    // Service
    private final AccountsLinkDeleteService accountsLinkDeleteService;

    // constructor
    public AccountsLinkDeleteController(

        AccountsLinkDeleteService accountsLinkDeleteService

    ) {

        this.accountsLinkDeleteService = accountsLinkDeleteService;

    }

    @PostMapping("${ACCOUNTS_BASE_URL}/delete-account-link")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody() AccountsLinkDeleteDTO accountsLinkDeleteDTO,

        BindingResult bindingResult,
        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
            request.getAttribute("credentialsData");

        return accountsLinkDeleteService.execute(
            credentialsData,
            accountsLinkDeleteDTO
        );

    }

}