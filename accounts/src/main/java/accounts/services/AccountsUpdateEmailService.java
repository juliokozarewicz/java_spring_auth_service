package accounts.services;

import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.dtos.AccountsUpdateEmailDTO;
import accounts.enums.EmailResponsesEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsUpdateEmailService {

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsUpdateEmailService(

        MessageSource messageSource,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.accountsManagementService = accountsManagementService;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;
        this.errorHandler = errorHandler;

    }

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsUpdateEmailDTO accountsUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("email").toString();

        // process to change email
        // ---------------------------------------------------------------------

        // find user
        Optional<AccountsEntity> findOldUser =  accountsRepository.findByEmail(
            emailUser
        );

        if ( findOldUser.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                400,
                messageSource.getMessage(
                    "response_update_email_fail", null, locale
                )
            );

        }

        // Encoded email
        String encodedEmail = encryptionService.encodeBase64(
            emailUser
        );

        // ##### If new email exist return error
        // ##### If password not match return error
        // ##### Find old email and token
        // ##### find old email and pin
        // ##### If token or email not found return error
        // ##### Update database with new email
        // ##### Clean all refresh tokens

        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-email");
        customLinks.put("next", "/accounts/login");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_email_success",
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