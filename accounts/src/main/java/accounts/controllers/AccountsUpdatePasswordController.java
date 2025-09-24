package accounts.controllers;

import accounts.dtos.AccountsRequestDTO;
import accounts.dtos.AccountsUpdatePasswordDTO;
import accounts.services.AccountsUpdatePasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Validated
class AccountsUpdatePasswordController {

    // Attributes
    private final AccountsUpdatePasswordService accountsUpdatePasswordService;
    private final AccountsRequestDTO accountsRequestDTO;

    // constructor
    public AccountsUpdatePasswordController(

        AccountsUpdatePasswordService accountsUpdatePasswordService,
        AccountsRequestDTO accountsRequestDTO

    ) {

        this.accountsUpdatePasswordService = accountsUpdatePasswordService;
        this.accountsRequestDTO = accountsRequestDTO;

    }

    @PatchMapping("${ACCOUNTS_BASE_URL}/update-password")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsUpdatePasswordDTO
            accountsUpdatePasswordDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------
        String userIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
            .filter(ip -> !ip.isBlank())
            .map(ip -> ip.contains(",") ? ip.split(",")[0].trim() : ip)
            .orElse(request.getRemoteAddr());

        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestDTO.validateUserIp(userIp);
        accountsRequestDTO.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsUpdatePasswordService.execute(
            userIp,
            userAgent,
            accountsUpdatePasswordDTO
        );

    }

}