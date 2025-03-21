package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsCreateValidation;
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
public class AccountsCreateService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final EncryptionControl encryptionControl;

    // constructor
    public AccountsCreateService (
        MessageSource messageSource,
        AccountsRepository accountsRepository,
        EncryptionControl encryptionControl
    ) {
        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
    }

    @Transactional
    public ResponseEntity execute(
        AccountsCreateValidation accountsCreateValidation
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email()
        );

        //
        // ##### user not existing
        // ---------------------------------------------------------------------
        /*
        [x] Transaction:
            [x] ACCOUNT
            [] PROFILE
            [] CODE
            [] Send email link (async)
         */

         // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

         // Create Account
         if (findUser.isEmpty()) {

             AccountsEntity newAccount = new AccountsEntity();
             newAccount.setId(generatedUUID);
             newAccount.setCreatedAt(nowTimestamp.toLocalDateTime());
             newAccount.setUpdatedAt(nowTimestamp.toLocalDateTime());
             newAccount.setLevel("user");
             newAccount.setEmail(accountsCreateValidation.email());
             String hashedPassword = encryptionControl.hashPassword(accountsCreateValidation.password());
             newAccount.setPassword(hashedPassword);
             newAccount.setActive(false);
             newAccount.setBanned(false);

             accountsRepository.save(newAccount);
         }
        // ---------------------------------------------------------------------

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/signup");
        customLinks.put("next", "/accounts/activate-email");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "account_created_successfully",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();
        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}
