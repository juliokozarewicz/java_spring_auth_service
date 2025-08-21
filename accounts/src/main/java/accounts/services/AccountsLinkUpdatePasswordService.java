package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import accounts.dtos.AccountsLinkUpdatePasswordDTO;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AccountsLinkUpdatePasswordService {

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final AccountsManagementService accountsManagementService;
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;

    // constructor
    public AccountsLinkUpdatePasswordService(

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
            accountsManagementService.deleteAllVerificationTokenByEmailNewTransaction(
                accountsLinkUpdatePasswordDTO.email().toLowerCase()
            );

            // Create token
            String tokenGenerated =
            accountsManagementService.createVerificationToken(
                accountsLinkUpdatePasswordDTO.email().toLowerCase(),
                AccountsUpdateEnum.UPDATE_PASSWORD
            );

            // Link
            String encodedEmail = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(
                    accountsLinkUpdatePasswordDTO.email().toLowerCase()
                        .getBytes(StandardCharsets.UTF_8)
                );

            String linkFinal = (
                accountsLinkUpdatePasswordDTO.link() +
                "?" +
                "userEmail=" + encodedEmail +
                "&" +
                "token=" + tokenGenerated
            );

            // send userEmail
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