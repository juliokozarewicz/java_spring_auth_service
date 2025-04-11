package com.example.demo.services;

import com.example.demo.enums.EmailResponsesEnum;
import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.UserCredentialsJWT;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsLoginValidation;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsLoginService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final EncryptionControl encryptionControl;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final UserCredentialsJWT userCredentialsJWT;

    // constructor
    public AccountsLoginService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        UserCredentialsJWT userCredentialsJWT

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.accountsManagementService = accountsManagementService;
        this.userCredentialsJWT = userCredentialsJWT;

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsLoginValidation accountsLoginValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLoginValidation.email().toLowerCase()
        );

        // Invalid credentials
        if ( findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Password compare
        boolean passwordCompare = encryptionControl.matchPasswords(
            accountsLoginValidation.password(),
            findUser.get().getPassword()
        );

        // Invalid credentials
        if ( !passwordCompare ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Account banned
        if ( findUser.get().isBanned() ) {

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_BANNED_ERROR.getDescription(),
                null
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_login_error", null, locale
                )
            );

        }

        // Account deactivated
        if ( !findUser.get().isActive() ) {

            // send email
            accountsManagementService.sendEmailStandard(
                findUser.get().getEmail().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_EXIST_DEACTIVATED_ERROR.getDescription(),
                null
            );

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "response_login_error", null, locale
                )
            );

        }

        // Create JWT
        // ---------------------------------------------------------------------

        // Payload
        Map<String, String> credentialPayload = new LinkedHashMap<>();
        credentialPayload.put("id", findUser.get().getId());
        credentialPayload.put("email", findUser.get().getEmail());

        // Create raw JWT
        String credentialsTokenRaw = userCredentialsJWT.createCredential(
            credentialPayload
        );

        System.out.println("Open =============================================");

        // ##### Encrypt the JWT (encryption error, why?)
        String encryptedCredential = encryptionControl.encrypt(
            credentialsTokenRaw
        );

        System.out.println("Close =============================================");
        // ---------------------------------------------------------------------

        // ##### Get all refresh tokens, delete all tokens less than 15 days old, and keep only the last five valid refresh tokens
        // ##### Create refresh token
        // ##### Encrypt refresh token
        // ##### Store refresh token

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/login");
        customLinks.put("next", "/accounts/profile");

        // Tokens data
        Map<String, String> tokensData = new LinkedHashMap<>();
        tokensData.put("access", encryptedCredential);
        tokensData.put("refresh", credentialsTokenRaw);

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_login_success",
                    null,
                    locale
                )
            )
            .data(tokensData)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);
        // ---------------------------------------------------------------------

    }

}