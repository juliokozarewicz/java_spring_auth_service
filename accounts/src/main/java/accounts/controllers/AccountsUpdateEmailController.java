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

    @PatchMapping("${BASE_URL_ACCOUNTS}/update-email")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody(required = false)
        AccountsUpdateEmailDTO accountsUpdateEmailDTO,

        BindingResult bindingResult,

        HttpServletRequest request

    ) {

        // Request data
        // ---------------------------------------------------------------------

        // Auth endpoint
        Map<String, Object> credentialsData = (Map<String, Object>)
        request.getAttribute("credentialsData");

        // user log
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

        return accountsUpdateEmailService.execute(
            userIp,
            userAgent,
            credentialsData,
            accountsUpdateEmailDTO
        );

    }

}