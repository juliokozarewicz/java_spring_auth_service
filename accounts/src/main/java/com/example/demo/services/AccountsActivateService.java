package com.example.demo.services;

import com.example.demo.enums.AccountsUpdateEnum;
import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.AccountsUserLogEntity;
import com.example.demo.persistence.entities.AccountsVerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.UserLogsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsActivateValidation;
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
public class AccountsActivateService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final UserLogsRepository userLogsRepository;

    // constructor
    public AccountsActivateService (

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        VerificationTokenRepository verificationTokenRepository,
        AccountsRepository accountsRepository,
        UserLogsRepository userLogsRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.userLogsRepository = userLogsRepository;

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsActivateValidation accountsActivateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // find email and token
        Optional<AccountsVerificationTokenEntity> findEmailAndToken =
            verificationTokenRepository.findByEmailAndToken(
                accountsActivateValidation.email().toLowerCase(),
                accountsActivateValidation.token() + "_" +
                AccountsUpdateEnum.ACTIVATE_ACCOUNT.getDescription()
            );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsActivateValidation.email().toLowerCase()
        );

        // email & token or account not exist
        if ( findEmailAndToken.isEmpty() || findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_activate_account_error", null, locale
                )
            );

        }

        // Active account
        if (

            findEmailAndToken.isPresent() &&
            findUser.isPresent() &&
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Update user log
            AccountsUserLogEntity newUserLog = new AccountsUserLogEntity();
            newUserLog.setId(generatedUUID);
            newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
            newUserLog.setIpAddress(userIp);
            newUserLog.setUserId(findUser.get().getId());
            newUserLog.setAgent(userAgent);
            newUserLog.setUpdateType(
                AccountsUpdateEnum.ACTIVATE_ACCOUNT.getDescription()
            );
            newUserLog.setOldValue(String.valueOf(findUser.get().isActive()));
            newUserLog.setNewValue("true");
            userLogsRepository.save(newUserLog);

            // active account in database
            accountsManagementService.enableAccount(findUser.get().getId());

        }

        // Delete all old tokens
        verificationTokenRepository
            .findByEmail(accountsActivateValidation.email().toLowerCase())
            .forEach(verificationTokenRepository::delete);

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/activate-email");
        customLinks.put("next", "/accounts/login");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_account_activate",
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

    };

}
