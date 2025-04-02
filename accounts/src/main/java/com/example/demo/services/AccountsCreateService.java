package com.example.demo.services;

import com.example.demo.enums.AccountsUpdateEnum;
import com.example.demo.enums.EmailResponsesEnum;
import com.example.demo.enums.UserLevelEnum;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.ProfileEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
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
    private final VerificationTokenRepository verificationTokenRepository;

    // constructor
    public AccountsCreateService (

        MessageSource messageSource,
        EncryptionControl encryptionControl,
        AccountsRepository accountsRepository,
        ProfileRepository profileRepository,
        AccountsManagementService accountsManagementService,
        VerificationTokenRepository verificationTokenRepository

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.profileRepository = profileRepository;
        this.accountsManagementService = accountsManagementService;
        this.verificationTokenRepository = verificationTokenRepository;

    }

    @Transactional
    public ResponseEntity execute(

        AccountsCreateValidation accountsCreateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email().toLowerCase()
        );

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // account exist and activated
        // ---------------------------------------------------------------------
        if (

            !findUser.isEmpty() &&
            findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            accountsManagementService.sendEmailStandard(
                accountsCreateValidation.email().toLowerCase(),
                EmailResponsesEnum.ERROR_ACCOUNT_EXIST_ACTIVATED.getDescription(),
                null
            );

        }
        // ---------------------------------------------------------------------

        // user not existing
        // ---------------------------------------------------------------------
        if ( findUser.isEmpty() ) {

            // Create Account
            AccountsEntity newAccount = new AccountsEntity();
            newAccount.setId(generatedUUID);
            newAccount.setCreatedAt(nowTimestamp.toLocalDateTime());
            newAccount.setUpdatedAt(nowTimestamp.toLocalDateTime());
            newAccount.setLevel(UserLevelEnum.USER.getDescription());
            newAccount.setEmail(accountsCreateValidation.email().toLowerCase());
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

        }
        // ---------------------------------------------------------------------

        if (

            findUser.isEmpty() ||
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            verificationTokenRepository
                .findByEmail(accountsCreateValidation.email().toLowerCase())
                .forEach(verificationTokenRepository::delete);

            // Create token
            String tokenGenerated =
            accountsManagementService.createToken(
                accountsCreateValidation.email().toLowerCase(),
                AccountsUpdateEnum.ACTIVATE_ACCOUNT.getDescription()
            );

            // Link
            String linkFinal = (
                accountsCreateValidation.link() +
                "?" +
                "email=" + accountsCreateValidation.email() +
                "&" +
                "token=" + tokenGenerated
            );

            // send email
            accountsManagementService.sendEmailStandard(
                accountsCreateValidation.email().toLowerCase(),
                EmailResponsesEnum.SUCCESS_ACTIVATE_ACCOUNT.getDescription(),
                linkFinal
            );

        }
        // ---------------------------------------------------------------------

        // response (links)
        // ---------------------------------------------------------------------
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
        // ---------------------------------------------------------------------

    }

}