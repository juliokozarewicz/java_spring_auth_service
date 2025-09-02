package accounts.controllers;

import accounts.services.AccountsLoginService;
import accounts.dtos.AccountsLoginDTO;
import accounts.dtos.AccountsRequestDTO;
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
class AccountsLoginController {

    // Service
    private final AccountsLoginService accountsLoginService;
    private final AccountsRequestDTO accountsRequestDTO;

    // constructor
    public AccountsLoginController(

        AccountsLoginService accountsLoginService,
        AccountsRequestDTO accountsRequestDTO

    ) {

        this.accountsLoginService = accountsLoginService;
        this.accountsRequestDTO = accountsRequestDTO;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/login")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsLoginDTO accountsLoginDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------
        String userIp = request.getHeader("X-Forwarded-For");
        if (userIp == null || userIp.isBlank()) {
            userIp = request.getRemoteAddr(); // fallback
        } else if (userIp.contains(",")) {
            userIp = userIp.split(",")[0].trim();
        }
        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestDTO.validateUserIp(userIp);
        accountsRequestDTO.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsLoginService.execute(
            userIp,
            userAgent,
            accountsLoginDTO
        );

    }

}