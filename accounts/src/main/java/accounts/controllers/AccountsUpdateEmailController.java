package accounts.controllers;

import accounts.dtos.AccountsRequestDTO;
import accounts.dtos.AccountsUpdateEmailDTO;
import accounts.services.AccountsUpdateEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping()
@Validated
class AccountsUpdateEmailController {

    // Service
    private final AccountsUpdateEmailService accountsUpdateEmailService;
    private final AccountsRequestDTO accountsRequestDTO;

    // constructor
    public AccountsUpdateEmailController(

        AccountsUpdateEmailService accountsUpdateEmailService,
        AccountsRequestDTO accountsRequestDTO

    ) {

        this.accountsUpdateEmailService = accountsUpdateEmailService;
        this.accountsRequestDTO = accountsRequestDTO;

    }

    @PatchMapping("${ACCOUNTS_BASE_URL}/update-email")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody AccountsUpdateEmailDTO accountsUpdateEmailDTO,
        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        // user log
        String userIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
            .filter(ip -> !ip.isBlank())
            .map(ip -> ip.contains(",") ? ip.split(",")[0].trim() : ip)
            .orElse(request.getRemoteAddr());

        String userAgent = request.getHeader("User-Agent");

        //validation request data
        accountsRequestDTO.validateUserIp(userIp);
        accountsRequestDTO.validateUserAgent(userAgent);
        // ---------------------------------------------------------------------

        return accountsUpdateEmailService.execute(
            userIp,
            userAgent,
            credentialsData,
            accountsUpdateEmailDTO
        );

    }

}