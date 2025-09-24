package accounts.services;

import accounts.dtos.AccountsLinkDeleteDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
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
public class AccountsLinkDeleteService {

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;

    // constructor
    public AccountsLinkDeleteService(

        MessageSource messageSource,
        AccountsRepository accountsRepository,
        AccountsManagementService accountsManagementService,
        EncryptionService encryptionService

    ) {

        this.messageSource = messageSource;
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