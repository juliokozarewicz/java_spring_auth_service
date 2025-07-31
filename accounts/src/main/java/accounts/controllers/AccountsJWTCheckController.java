package accounts.controllers;

import accounts.exceptions.ErrorHandler;
import accounts.services.AccountsJWTCheckService;
import accounts.dtos.AccountsJWTCheckDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping()
@Validated
class AccountsJWTCheckController {

    // Service
    private final AccountsJWTCheckService accountsJWTCheckService;
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsJWTCheckController(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsJWTCheckService accountsJWTCheckService

    ) {

        this.accountsJWTCheckService = accountsJWTCheckService;
        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }

    @PostMapping("${BASE_URL_ACCOUNTS}/jwt-credentials-validation")
    public ResponseEntity handle(

        // get token from header
        @RequestHeader("Authorization") String auth

    ) {

        // bearer validation
        if ( auth == null || !auth.startsWith("Bearer ") ) {
                callCustomErrorInvalidToken();
        }

        // token validation
        String token = auth.substring(7);
        if ( token.isEmpty() || !token.matches("^[A-Za-z0-9_-]+$") ) {
            callCustomErrorInvalidToken();
        }

        // call service
        return accountsJWTCheckService.execute(
            new AccountsJWTCheckDTO(token)
        );

    }

    private void callCustomErrorInvalidToken() {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // call custom error
        errorHandler.customErrorThrow(
            422,
            messageSource.getMessage(
                "response_invalid_credentials", null, locale
            )
        );

    }

}