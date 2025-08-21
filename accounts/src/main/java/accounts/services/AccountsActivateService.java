package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsLogEntity;
import accounts.persistence.entities.AccountsVerificationTokenEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsLogRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import accounts.dtos.AccountsActivateDTO;
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
public class AccountsActivateService {

    // attributes
    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final AccountsLogRepository accountsLogRepository;

    // constructor
    public AccountsActivateService (

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        AccountsVerificationTokenRepository accountsVerificationTokenRepository,
        AccountsRepository accountsRepository,
        AccountsLogRepository accountsLogRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.accountsVerificationTokenRepository = accountsVerificationTokenRepository;
        this.accountsRepository = accountsRepository;
        this.accountsLogRepository = accountsLogRepository;

    }

    @Transactional
    public ResponseEntity execute(

        String userIp,
        String userAgent,
        AccountsActivateDTO accountsActivateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Decoded userEmail
        String decodedEmail = new String (
            Base64.getUrlDecoder().decode(accountsActivateDTO.email()),
            StandardCharsets.UTF_8
        );

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // find userEmail and token
        Optional<AccountsVerificationTokenEntity> findEmailAndToken =
            accountsVerificationTokenRepository.findByEmailAndToken(
                decodedEmail,
                accountsActivateDTO.token() + "_" +
                AccountsUpdateEnum.ACTIVATE_ACCOUNT
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
            AccountsLogEntity newUserLog = new AccountsLogEntity();
            newUserLog.setId(generatedUUID);
            newUserLog.setCreatedAt(nowTimestamp.toLocalDateTime());
            newUserLog.setIpAddress(userIp);
            newUserLog.setUserId(findUser.get().getId());
            newUserLog.setAgent(userAgent);
            newUserLog.setUpdateType(
                AccountsUpdateEnum.ACTIVATE_ACCOUNT
            );
            newUserLog.setOldValue(String.valueOf(findUser.get().isActive()));
            newUserLog.setNewValue("true");
            accountsLogRepository.save(newUserLog);

            // active account in database
            accountsManagementService.enableAccount(findUser.get().getId());

        }

        // Delete all old tokens
        accountsManagementService.deleteAllVerificationTokenByEmailNewTransaction(
            decodedEmail
        );

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/activate-userEmail");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
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

    }

}
