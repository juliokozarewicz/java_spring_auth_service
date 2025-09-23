package accounts.controllers;

import accounts.dtos.AccountsDeleteDTO;
import accounts.dtos.AccountsRequestDTO;
import accounts.services.AccountsDeleteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping()
@Validated
class AccountsDeleteController {

    // Service
    private final AccountsDeleteService accountsDeleteService;
    private final AccountsRequestDTO accountsRequestDTO;

    // constructor
    public AccountsDeleteController(

        AccountsDeleteService accountsDeleteService,
        AccountsRequestDTO accountsRequestDTO

    ) {

        this.accountsDeleteService = accountsDeleteService;
        this.accountsRequestDTO = accountsRequestDTO;

    }

    @DeleteMapping("${ACCOUNTS_BASE_URL}/delete")
    @SuppressWarnings("unchecked")
    public ResponseEntity handle(

        // dtos errors
        @Valid @RequestBody()
        AccountsDeleteDTO accountsDeleteDTO,

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

        return accountsDeleteService.execute(
            userIp,
            userAgent,
            credentialsData,
            accountsDeleteDTO
        );

    }

}