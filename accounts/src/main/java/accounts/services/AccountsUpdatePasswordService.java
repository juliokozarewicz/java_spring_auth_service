package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsUserLogEntity;
import accounts.persistence.entities.AccountsVerificationTokenEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.UserLogsRepository;
import accounts.persistence.repositories.VerificationTokenRepository;
import accounts.utils.EncryptionService;
import accounts.utils.StandardResponse;
import accounts.validations.AccountsUpdatePasswordValidation;
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
public class AccountsUpdatePasswordService {

    // attributes
    private final MessageSource messageSource;
    private final  VerificationTokenRepository verificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsManagementService accountsManagementService;
    private final UserLogsRepository userLogsRepository;

    // constructor
    public AccountsUpdatePasswordService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        VerificationTokenRepository verificationTokenRepository,
        AccountsRepository accountsRepository,
        EncryptionService encryptionService,
        AccountsManagementService accountsManagementService,
        UserLogsRepository userLogsRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.verificationTokenRepository = verificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
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
        Optional<AccountsVerificationTokenEntity> findEmailAndToken =
            verificationTokenRepository.findByEmailAndToken(
                accountsUpdatePasswordValidation.email().toLowerCase(),
                accountsUpdatePasswordValidation.token() + "_" +
                AccountsUpdateEnum.UPDATE_PASSWORD
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
            String passwordHashed = encryptionService.hashPassword(
                accountsUpdatePasswordValidation.password()
            );

            // Update user log
            AccountsUserLogEntity newUserLog = new AccountsUserLogEntity();
            newUserLog.setId(generatedUUID);
            newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
            newUserLog.setIpAddress(userIp);
            newUserLog.setUserId(findUser.get().getId());
            newUserLog.setAgent(userAgent);
            newUserLog.setUpdateType(AccountsUpdateEnum.UPDATE_PASSWORD);
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
