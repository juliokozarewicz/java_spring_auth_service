package accounts.controllers;

import accounts.dtos.AccountsRefreshLoginDTO;
import accounts.dtos.AccountsRequestDTO;
import accounts.services.AccountsRefreshLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping()
@Validated
class AccountsRefreshLoginController {

    // Service
    private final AccountsRefreshLoginService accountsRefreshLoginService;
    private final AccountsRequestDTO accountsRequestDTO;

    // constructor
    public AccountsRefreshLoginController(

        AccountsRefreshLoginService accountsRefreshLoginService,
        AccountsRequestDTO accountsRequestDTO

    ) {

        this.accountsRefreshLoginService = accountsRefreshLoginService;
        this.accountsRequestDTO = accountsRequestDTO;

    }

    @PostMapping("${ACCOUNTS_BASE_URL}/refresh-login")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsRefreshLoginDTO accountsRefreshLoginDTO,
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

        return accountsRefreshLoginService.execute(
            userIp,
            userAgent,
            accountsRefreshLoginDTO
        );

    }

}