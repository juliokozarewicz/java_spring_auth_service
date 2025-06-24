package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.EncryptionService;
import com.example.demo.utils.StandardResponse;
import com.example.demo.utils.UserJWTService;
import com.example.demo.validations.AccountsJWTCheckValidation;
import io.jsonwebtoken.Claims;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsJWTCheckService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final UserJWTService userJWTService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;

    // constructor
    public AccountsJWTCheckService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        UserJWTService userJWTService,
        AccountsRepository accountsRepository,
        EncryptionService encryptionService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.userJWTService = userJWTService;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;

    }

    public ResponseEntity execute(

        AccountsJWTCheckValidation accountsJWTCheckValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // decrypt jwt
        String decryptedJWT = encryptionService.decrypt(
            accountsJWTCheckValidation.accessToken()
        );

        // validate credentials
        Boolean validCredentials = userJWTService.isCredentialsValid(
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

        // get info from jwt
        Claims claims = null;
        try {
            claims = userJWTService.getCredentialsData(decryptedJWT);
        } catch (Exception e) {
            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );
        }

        // find email
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            claims.get("email").toString()
        );

        // Tokens data
        Map<String, String> tokensData = new LinkedHashMap<>();
        tokensData.put("id", claims.get("id").toString());
        tokensData.put("email", claims.get("email").toString());
        tokensData.put("level", findUser.get().getLevel());

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .data(tokensData)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}