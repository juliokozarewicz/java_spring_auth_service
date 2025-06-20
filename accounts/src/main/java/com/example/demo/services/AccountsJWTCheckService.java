package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.UserCredentialsJWT;
import com.example.demo.validations.AccountsJWTCheckValidation;
import io.jsonwebtoken.Claims;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountsJWTCheckService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsManagementService accountsManagementService;
    private final UserCredentialsJWT userCredentialsJWT;
    private final EncryptionControl encryptionControl;

    // constructor
    public AccountsJWTCheckService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        UserCredentialsJWT userCredentialsJWT,
        EncryptionControl encryptionControl

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.userCredentialsJWT = userCredentialsJWT;
        this.encryptionControl = encryptionControl;

    }

    public ResponseEntity<Map<String, Object>> execute(

        AccountsJWTCheckValidation accountsJWTCheckValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // decrypt jwt
        String decryptedJWT = encryptionControl.decrypt(
            accountsJWTCheckValidation.accessToken()
        );

        // validate credentials
        Boolean validCredentials = userCredentialsJWT.isCredentialsValid(
            decryptedJWT
        );

        if (!validCredentials) {
            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );
        };

        // get info from jwt #####
        Claims claims = null;
        try {
            claims = userCredentialsJWT.getCredentialsData(decryptedJWT);
        } catch (Exception e) {
            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", claims.get("id"));
        result.put("email", claims.get("email"));

        return ResponseEntity.ok(result);

    }

}