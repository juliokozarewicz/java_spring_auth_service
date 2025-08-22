package accounts.services;

import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountsLinkUpdateEmailService {

    private final MessageSource messageSource;
    private final AccountsManagementService accountsManagementService;
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;

    // constructor
    public AccountsLinkUpdateEmailService(

        MessageSource messageSource,
        AccountsManagementService accountsManagementService,
        AccountsVerificationTokenRepository accountsVerificationTokenRepository

    ) {

        this.messageSource = messageSource;
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

        // Credentials
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("userEmail").toString();

        // process to change userEmail
        // ---------------------------------------------------------------------

        // Clean all old validation tokens
        accountsManagementService.deleteAllVerificationTokenByEmailNewTransaction(
            emailUser
        );

        // Clean all old pin's
        accountsManagementService.deleteAllVerificationPinByUserId(idUser);

        // Create pin
        String pinGenerated = accountsManagementService.createVerificationPin(
            idUser,
            AccountsUpdateEnum.UPDATE_EMAIL
        );

        // Send pin to new userEmail
        accountsManagementService.sendEmailStandard(
            accountsLinkUpdateEmailDTO.newEmail().toLowerCase(),
            EmailResponsesEnum.UPDATE_EMAIL_PIN,
            pinGenerated
        );

        // Create token
        String tokenGenerated = accountsManagementService.createVerificationToken(
                emailUser,
                AccountsUpdateEnum.UPDATE_EMAIL
        );

        // Link
        String encodedEmail = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(
                emailUser
                .getBytes(StandardCharsets.UTF_8)
            );

        String linkFinal = UriComponentsBuilder
            .fromHttpUrl(accountsLinkUpdateEmailDTO.link())
            .queryParam("userEmail", encodedEmail)
            .queryParam("token", tokenGenerated)
            .build()
            .toUriString();

        // send link with token to old userEmail
        accountsManagementService.sendEmailStandard(
            emailUser,
            EmailResponsesEnum.UPDATE_EMAIL_CLICK,
            linkFinal
        );
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-userEmail-link");
        customLinks.put("next", "/accounts/update-userEmail");

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