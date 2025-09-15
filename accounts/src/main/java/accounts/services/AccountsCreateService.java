package accounts.services;

import accounts.dtos.AccountsCreateDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.enums.UserLevelEnum;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsCreateService {

    // constructor
    private final MessageSource messageSource;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final AccountsProfileRepository accountsProfileRepository;
    private final AccountsManagementService accountsManagementService;

    public AccountsCreateService (

        MessageSource messageSource,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;

    }

    @Transactional
    public ResponseEntity execute(

        AccountsCreateDTO accountsCreateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateDTO.email().toLowerCase()
        );

        // ID and Timestamp
        UUID generatedUniqueId = accountsManagementService.createUniqueId();
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // account exist and activated
        // ---------------------------------------------------------------------
        if (

            !findUser.isEmpty() &&
            findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            accountsManagementService.sendEmailStandard(
                accountsCreateDTO.email().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_EXIST_ACTIVATED_ERROR,
                null
            );

        }
        // ---------------------------------------------------------------------

        // user not existing
        // ---------------------------------------------------------------------
        if ( findUser.isEmpty() ) {

            // Create Account
            AccountsEntity newAccount = new AccountsEntity();
            newAccount.setId(generatedUniqueId);
            newAccount.setCreatedAt(nowUtc);
            newAccount.setUpdatedAt(nowUtc);
            newAccount.setLevel(UserLevelEnum.USER);
            newAccount.setEmail(accountsCreateDTO.email().toLowerCase());
            newAccount.setPassword(
                encryptionService.hashPassword(
                    accountsCreateDTO.password()
                )
            );
            newAccount.setActive(false);
            newAccount.setBanned(false);
            accountsRepository.save(newAccount);

            // Create profile
            AccountsProfileEntity newProfile = new AccountsProfileEntity();
            newProfile.setId(generatedUniqueId);
            newProfile.setCreatedAt(nowUtc);
            newProfile.setUpdatedAt(nowUtc);
            newProfile.setName(accountsCreateDTO.name());
            accountsProfileRepository.save(newProfile);

        }
        // ---------------------------------------------------------------------

        // Account exist and user deactivated
        // ---------------------------------------------------------------------
        if (

            findUser.isEmpty() ||
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Encrypted email
            String encryptedEmail = encryptionService.encrypt(
                accountsCreateDTO.email().toLowerCase()
            );

            // Delete all old tokens
            accountsManagementService.deleteAllVerificationTokenByIdUserNewTransaction(
                findUser.isPresent() ? findUser.get().getId() : generatedUniqueId
            );

            // Create token
            String tokenGenerated = accountsManagementService.createVerificationToken(
                findUser.isPresent() ? findUser.get().getId() : generatedUniqueId,
                AccountsUpdateEnum.ACTIVATE_ACCOUNT
            );

            // Link
            String linkFinal = UriComponentsBuilder
                .fromHttpUrl(accountsCreateDTO.link())
                .queryParam("email", encryptedEmail)
                .queryParam("token", tokenGenerated)
                .build()
                .toUriString();

            // send email
            accountsManagementService.sendEmailStandard(
                accountsCreateDTO.email().toLowerCase(),
                EmailResponsesEnum.ACTIVATE_ACCOUNT_SUCCESS,
                linkFinal
            );

        }
        // ---------------------------------------------------------------------

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/signup");
        customLinks.put("next", "/accounts/activate-email");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_account_created_successfully",
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