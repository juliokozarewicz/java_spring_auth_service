package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsLoginValidation;
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
    private final EncryptionControl encryptionControl;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;

    // constructor
    public AccountsLoginService(

        MessageSource messageSource,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.accountsManagementService = accountsManagementService;

    }

    @Transactional
    public ResponseEntity execute(

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

        // ##### User exist and password compare is true

        // ##### Invalid credentials
        // ##### Account banned
        // ##### Account deactivated
        // ##### Create JWT
        // ##### Encrypt the JWT
        // ##### Delete 15 days old tokens
        // ##### Get all refresh tokens, delete all tokens less than 15 days old, and keep only the last five valid refresh tokens
        // ##### Create refresh token
        // ##### Encrypt refresh token
        // ##### Store refresh token

        // response (links)
        // ---------------------------------------------------------------------
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/login");
        customLinks.put("next", "/accounts/profile");

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
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);
        // ---------------------------------------------------------------------

    }

}