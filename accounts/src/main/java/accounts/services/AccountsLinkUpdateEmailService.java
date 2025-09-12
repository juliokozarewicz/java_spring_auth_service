package accounts.services;

import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.enums.AccountsUpdateEnum;
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
public class AccountsLinkUpdateEmailService {

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsLinkUpdateEmailService(

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
        AccountsLinkUpdateEmailDTO accountsLinkUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("email").toString();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLinkUpdateEmailDTO.newEmail()
        );

        if ( findUser.isPresent() ) {

            // call custom error
            errorHandler.customErrorThrow(
                409,
                messageSource.getMessage(
                    "response_update_email_sent_fail", null, locale
                )
            );

        }

        // process to change email
        // ---------------------------------------------------------------------

        // Create pin
        String pinGenerated = accountsManagementService.createVerificationPin(
            idUser,
            AccountsUpdateEnum.UPDATE_EMAIL,
            accountsLinkUpdateEmailDTO.newEmail()
        );

        // Send pin to new email
        accountsManagementService.sendEmailStandard(
            accountsLinkUpdateEmailDTO.newEmail().toLowerCase(),
            EmailResponsesEnum.UPDATE_EMAIL_PIN,
            pinGenerated
        );

        // Create token
        String tokenGenerated = accountsManagementService.createVerificationToken(
            idUser,
            AccountsUpdateEnum.UPDATE_EMAIL
        );

        // Link
        String linkFinal = UriComponentsBuilder
            .fromHttpUrl(accountsLinkUpdateEmailDTO.link())
            .queryParam("token", tokenGenerated)
            .build()
            .toUriString();

        // send link with token to old email
        accountsManagementService.sendEmailStandard(
            emailUser,
            EmailResponsesEnum.UPDATE_EMAIL_CLICK,
            linkFinal
        );

        // Revoke all tokens
        accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
            idUser
        );
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-email-link");
        customLinks.put("next", "/accounts/update-email");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_email_sent_success",
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