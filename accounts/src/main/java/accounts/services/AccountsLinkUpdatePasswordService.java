package accounts.services;

import accounts.dtos.AccountsLinkUpdatePasswordDTO;
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
public class AccountsLinkUpdatePasswordService {

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final EncryptionService encryptionService;

    // constructor
    public AccountsLinkUpdatePasswordService(

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

        AccountsLinkUpdatePasswordDTO accountsLinkUpdatePasswordDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsLinkUpdatePasswordDTO.email().toLowerCase()
        );

        if (

            findUser.isPresent() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
                findUser.get().getId()
            );

            // Encrypted email
            String encryptedEmail = encryptionService.encrypt(
                accountsLinkUpdatePasswordDTO.email().toLowerCase()
            );

            // Create token
            String tokenGenerated = accountsManagementService
                .createVerificationToken(
                    findUser.get().getId(),
                    AccountsUpdateEnum.UPDATE_PASSWORD
                );

            // Link
            String linkFinal = UriComponentsBuilder
                .fromHttpUrl(accountsLinkUpdatePasswordDTO.link())
                .queryParam("email", encryptedEmail)
                .queryParam("token", tokenGenerated)
                .build()
                .toUriString();

            // send email
            accountsManagementService.sendEmailStandard(
                accountsLinkUpdatePasswordDTO.email().toLowerCase(),
                EmailResponsesEnum.UPDATE_PASSWORD_CLICK,
                linkFinal
            );

        }
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/update-password-link");
        customLinks.put("next", "/accounts/update-password");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_change_password_link_success",
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