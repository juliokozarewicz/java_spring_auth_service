package accounts.controllers;

import accounts.dtos.AccountsAddressDTO;
import accounts.services.AccountsAddressCreateService;
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
class AccountsAddressCreateController {

    // Service
    private final AccountsAddressCreateService accountsAddressCreateService;

    // constructor
    public AccountsAddressCreateController(

        AccountsAddressCreateService accountsAddressCreateService

    ) {

        this.accountsAddressCreateService = accountsAddressCreateService;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/address-create")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        @Valid @RequestBody AccountsAddressDTO accountsAddressDTO,
        BindingResult bindingResult,
        HttpServletRequest request

    ) {

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
            request.getAttribute("credentialsData");

        return accountsAddressCreateService.execute(
            credentialsData,
            accountsAddressDTO
        );

    }

}