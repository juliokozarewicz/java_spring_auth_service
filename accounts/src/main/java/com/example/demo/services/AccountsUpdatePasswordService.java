package com.example.demo.services;

import com.example.demo.enums.AccountsUpdateEnum;
import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.UserLogsEntity;
import com.example.demo.persistence.entities.VerificationTokenEntity;
import com.example.demo.persistence.repositories.AccountsRepository;
import com.example.demo.persistence.repositories.UserLogsRepository;
import com.example.demo.persistence.repositories.VerificationTokenRepository;
import com.example.demo.utils.EncryptionControl;
import com.example.demo.utils.StandardResponse;
import com.example.demo.validations.AccountsUpdatePasswordValidation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsUpdatePasswordService {

    // attributes
    private final MessageSource messageSource;
    private final  VerificationTokenRepository verificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final EncryptionControl encryptionControl;
    private final AccountsManagementService accountsManagementService;
    private final UserLogsRepository userLogsRepository;

    // constructor
    public AccountsUpdatePasswordService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        VerificationTokenRepository verificationTokenRepository,
        AccountsRepository accountsRepository,
        EncryptionControl encryptionControl,
        AccountsManagementService accountsManagementService,
        UserLogsRepository userLogsRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.verificationTokenRepository = verificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.encryptionControl = encryptionControl;
        this.accountsManagementService = accountsManagementService;
        this.userLogsRepository = userLogsRepository;

    }

    @Transactional
    public ResponseEntity execute(
        String userIp,
        String userAgent,
        AccountsUpdatePasswordValidation accountsUpdatePasswordValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // find email and token
        Optional<VerificationTokenEntity> findEmailAndToken =
            verificationTokenRepository.findByEmailAndToken(
                accountsUpdatePasswordValidation.email().toLowerCase(),
                accountsUpdatePasswordValidation.token() + "_" +
                AccountsUpdateEnum.UPDATE_PASSWORD.getDescription()
            );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsUpdatePasswordValidation.email().toLowerCase()
        );

        // email & token or account not exist
        if ( findEmailAndToken.isEmpty() || findUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_update_password_error", null, locale
                )
            );

        }

        // Update password
        if (

            findEmailAndToken.isPresent() &&
            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // Password hash
            String passwordHashed = encryptionControl.hashPassword(
                accountsUpdatePasswordValidation.password()
            );

            // Update user log
            UserLogsEntity newUserLog = new UserLogsEntity();
            newUserLog.setId(generatedUUID);
            newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
            newUserLog.setIpAddress(userIp);
            newUserLog.setUserId(findUser.get().getId());
            newUserLog.setAgent(userAgent);
            newUserLog.setUpdateType(AccountsUpdateEnum
                .UPDATE_PASSWORD.getDescription());
            newUserLog.setOldValue(findUser.get().getPassword());
            newUserLog.setNewValue(passwordHashed);
            userLogsRepository.save(newUserLog);

            // update password
            findUser.get().setPassword(passwordHashed);

            // Active account if is deactivated
            if ( !findUser.get().isActive() ) {
                accountsManagementService.enableAccount(findUser.get().getId());
            }

        }

        // Delete all old tokens
        verificationTokenRepository
            .findByEmail(accountsUpdatePasswordValidation.email().toLowerCase())
            .forEach(verificationTokenRepository::delete);

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password");
        customLinks.put("next", "/accounts/login");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_password_success",
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
