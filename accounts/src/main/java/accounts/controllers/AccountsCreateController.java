package accounts.controllers;

import accounts.dtos.AccountsCreateDTO;
import accounts.services.AccountsCreateService;
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
class AccountsCreateController {

    // Service
    private final AccountsCreateService accountsCreateService;

    // constructor
    public AccountsCreateController(
        AccountsCreateService accountsCreateService
    ) {
        this.accountsCreateService = accountsCreateService;
    }

    @PostMapping("${ACCOUNTS_BASE_URL}/signup")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsCreateDTO accountsCreateDTO,
        BindingResult bindingResult

    ) {

        return accountsCreateService.execute(accountsCreateDTO);

    }

}