package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsLogEntity;
import accounts.persistence.entities.AccountsVerificationTokenEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import accounts.dtos.AccountsUpdatePasswordDTO;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsUpdatePasswordService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final EncryptionService encryptionService;
    private final AccountsManagementService accountsManagementService;
    private final AccountsLogRepository accountsLogRepository;

    // constructor
    public AccountsUpdatePasswordService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsVerificationTokenRepository accountsVerificationTokenRepository,
        AccountsRepository accountsRepository,
        EncryptionService encryptionService,
        AccountsManagementService accountsManagementService,
        AccountsLogRepository accountsLogRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsVerificationTokenRepository = accountsVerificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsManagementService = accountsManagementService;
        this.accountsLogRepository = accountsLogRepository;

    }

    @Transactional
    public ResponseEntity execute(
        String userIp,
        String userAgent,
        AccountsUpdatePasswordDTO accountsUpdatePasswordDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Decoded userEmail
        String decodedEmail = encryptionService.decodeBase64(
            accountsUpdatePasswordDTO.email()
        );

        // find userEmail and token
        Optional<AccountsVerificationTokenEntity> findEmailAndToken =
            accountsVerificationTokenRepository.findByEmailAndToken(
                decodedEmail,
                accountsUpdatePasswordDTO.token() + "_" +
                AccountsUpdateEnum.UPDATE_PASSWORD
            );

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            decodedEmail
        );

        // userEmail & token or account not exist
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
                accountsUpdatePasswordDTO.password()
            );

            // Update user log
            accountsManagementService.createUserLog(
                userIp,
                findUser.get().getId(),
                userAgent,
                AccountsUpdateEnum.UPDATE_PASSWORD,
                findUser.get().getPassword(),
                passwordHashed
            );

            // update password
            findUser.get().setPassword(passwordHashed);

            // Active account if is deactivated
            if ( !findUser.get().isActive() ) {
                accountsManagementService.enableAccount(findUser.get().getId());
            }

        }

        // Delete all old tokens
        accountsManagementService.deleteAllVerificationTokenByEmailNewTransaction(
            decodedEmail
        );

        // Revoke all refresh tokens
        accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
            findUser.get().getId()
        );

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
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
