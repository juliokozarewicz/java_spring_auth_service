package accounts.controllers;

import accounts.services.AccountsUpdatePasswordService;
import accounts.dtos.AccountsRequestDTO;
import accounts.dtos.AccountsUpdatePasswordDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PatchMapping("${BASE_URL_ACCOUNTS}/update-password")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsUpdatePasswordDTO
            accountsUpdatePasswordDTO,
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

        return accountsUpdatePasswordService.execute(
            userIp,
            userAgent,
            accountsUpdatePasswordDTO
        );

    }

}