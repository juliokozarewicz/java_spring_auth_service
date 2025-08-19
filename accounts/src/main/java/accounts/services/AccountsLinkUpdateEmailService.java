package accounts.services;

import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsLinkUpdateEmailService {

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;

    // constructor
    public AccountsLinkUpdateEmailService(

        MessageSource messageSource,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        AccountsVerificationTokenRepository accountsVerificationTokenRepository

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.accountsManagementService = accountsManagementService;
        this.accountsVerificationTokenRepository = accountsVerificationTokenRepository;

    }

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsLinkUpdateEmailDTO accountsLinkUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Create link
        // ---------------------------------------------------------------------

        // Create token
        String tokenGenerated =
            accountsManagementService.createToken(
                accountsLinkUpdateEmailDTO.newEmail().toLowerCase(),
                AccountsUpdateEnum.UPDATE_EMAIL
            );

        // Link
        String linkFinal = (
            accountsLinkUpdateEmailDTO.link() +
                "?" +
                "email=" + accountsLinkUpdateEmailDTO.newEmail() +
                "&" +
                "token=" + tokenGenerated
        );

        // Create pin
        // send pin to new email
        // send link with token to old email

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