package accounts.services;

import accounts.dtos.AccountsLinkDeleteDTO;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsLinkDeleteService {

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;

    // constructor
    public AccountsLinkDeleteService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.accountsManagementService = accountsManagementService;
        this.encryptionService  = encryptionService;

    }

    @Transactional
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsLinkDeleteDTO accountsLinkDeleteDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Verify and authorize URL
        // ---------------------------------------------------------------------
        boolean isAllowedURL = false;

        try {

            String linkRaw = accountsLinkDeleteDTO.link().trim();

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
                "[AccountsLinkUpdatePasswordService.execute()]: " + e );

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
        String emailUser = credentialsData.get("email").toString();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            emailUser
        );

        if (

            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
                findUser.get().getId()
            );

            // Create token
            String tokenGenerated = accountsManagementService
                .createVerificationToken(
                    findUser.get().getId(),
                    AccountsUpdateEnum.DELETE_ACCOUNT
                );

            // Link
            String linkFinal = UriComponentsBuilder
                .fromHttpUrl(accountsLinkDeleteDTO.link())
                .queryParam("token", tokenGenerated)
                .build()
                .toUriString();

            // send email
            accountsManagementService.sendEmailStandard(
                emailUser,
                EmailResponsesEnum.ACCOUNT_DELETE_CLICK,
                linkFinal
            );

            // Revoke all tokens
            accountsManagementService.deleteAllRefreshTokensByIdNewTransaction(
                findUser.get().getId()
            );

        }
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/delete-account-link");
        customLinks.put("next", "/accounts/delete-account");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_delete_account_sent_success",
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