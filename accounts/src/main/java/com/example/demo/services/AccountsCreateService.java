package com.example.demo.services;

import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.ProfileEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.ProfileRepository;
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
    private final EncryptionControl encryptionControl;
    private final AccountsRepository accountsRepository;
    private final ProfileRepository profileRepository;
    private final AccountsManagementService accountsManagementService;

    // constructor
    public AccountsCreateService (
        MessageSource messageSource,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository,
        ProfileRepository profileRepository,
        AccountsManagementService accountsManagementService
    ) {
        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.profileRepository = profileRepository;
        this.accountsManagementService = accountsManagementService;
    }

    @Transactional
    public ResponseEntity execute(
        AccountsCreateValidation accountsCreateValidation
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email().toUpperCase()
        );

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // user not existing
        // ---------------------------------------------------------------------
        /*
        Transaction:
            [] CODE
            [] Send email link (async)
         */
         if (findUser.isEmpty()) {

             // Create Account
             AccountsEntity newAccount = new AccountsEntity();
             newAccount.setId(generatedUUID);
             newAccount.setCreatedAt(nowTimestamp.toLocalDateTime());
             newAccount.setUpdatedAt(nowTimestamp.toLocalDateTime());
             newAccount.setLevel("user");
             newAccount.setEmail(accountsCreateValidation.email().toUpperCase());
             newAccount.setPassword(
                 encryptionControl.hashPassword(
                    accountsCreateValidation.password()
                 )
             );
             newAccount.setActive(false);
             newAccount.setBanned(false);
             accountsRepository.save(newAccount);

             // Create profile
             ProfileEntity newProfile = new ProfileEntity();
             newProfile.setId(generatedUUID);
             newProfile.setCreatedAt(nowTimestamp.toLocalDateTime());
             newProfile.setUpdatedAt(nowTimestamp.toLocalDateTime());
             newProfile.setName(accountsCreateValidation.name());
             profileRepository.save(newProfile);

             // ##### Create token (timestamp + email + pepper + random bytes)
             String tokenGenerated = accountsManagementService.createToken(
                "activate-email"
             );

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
