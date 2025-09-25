package accounts.services;

import accounts.dtos.AccountsLinkUpdateEmailDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class AccountsLinkUpdateEmailService {

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;

    // constructor
    public AccountsLinkUpdateEmailService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;
        this.encryptionService = encryptionService;
        this.accountsRepository = accountsRepository;

    }

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsLinkUpdateEmailDTO accountsLinkUpdateEmailDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Verify and authorize URL
        // ---------------------------------------------------------------------
        boolean isAllowedURL = false;

        try {

            String linkRaw = accountsLinkUpdateEmailDTO.link().trim();

            if (!linkRaw.startsWith("http://") && !linkRaw.startsWith("https://")) {
                linkRaw = "http://" + linkRaw;
            }

            URI linkUri = new URI(linkRaw);

            String linkHost = linkUri.getHost();

            linkHost = linkHost.toLowerCase().replaceFirst("^www\\.", "");

            String[] allowedOrigins = publicDomain.split(",");

            for (String origin : allowedOrigins) {

                String originTrimmed = origin.trim();

                if (!originTrimmed.startsWith("http://") && !originTrimmed.startsWith("https://")) {
                    originTrimmed = "http://" + originTrimmed;
                }

                URI originUri = new URI(originTrimmed);
                String originHost = originUri.getHost();
                if (originHost != null) {
                    originHost = originHost.toLowerCase().replaceFirst("^www\\.", "");
                    if (linkHost.equals(originHost)) {
                        isAllowedURL = true;
                        break;
                    }
                }
            }

        } catch (Exception e) {

            throw new InternalError( "It was not possible to validate the " +
                "provided URL and compare it with the authorized URLs: " +
                "[AccountsLinkUpdateEmailService.execute()]: " + e );

        }

        if (!isAllowedURL) {

            // call custom error
            errorHandler.customErrorThrow(
                403,
                messageSource.getMessage(
                    "validation_valid_link", null, locale
                )
            );

        }
        // ---------------------------------------------------------------------

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));
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