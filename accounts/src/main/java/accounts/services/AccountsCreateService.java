package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.enums.UserLevelEnum;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.AccountsProfileRepository;
import accounts.persistence.repositories.AccountsVerificationTokenRepository;
import accounts.dtos.AccountsCreateDTO;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
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
    private final AccountsVerificationTokenRepository accountsVerificationTokenRepository;

    public AccountsCreateService (

        MessageSource messageSource,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService,
        AccountsVerificationTokenRepository accountsVerificationTokenRepository

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;
        this.accountsVerificationTokenRepository = accountsVerificationTokenRepository;

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

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

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
            newAccount.setId(generatedUUID);
            newAccount.setCreatedAt(nowTimestamp.toLocalDateTime());
            newAccount.setUpdatedAt(nowTimestamp.toLocalDateTime());
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
            newProfile.setId(generatedUUID);
            newProfile.setCreatedAt(nowTimestamp.toLocalDateTime());
            newProfile.setUpdatedAt(nowTimestamp.toLocalDateTime());
            newProfile.setName(accountsCreateDTO.name());
            accountsProfileRepository.save(newProfile);

        }
        // ---------------------------------------------------------------------

        if (

            findUser.isEmpty() ||
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            accountsVerificationTokenRepository
                .findByEmail(accountsCreateDTO.email().toLowerCase())
                .forEach(accountsVerificationTokenRepository::delete);

            // Create token
            String tokenGenerated =
            accountsManagementService.createToken(
                accountsCreateDTO.email().toLowerCase(),
                AccountsUpdateEnum.ACTIVATE_ACCOUNT
            );

            // Link
            String linkFinal = (
                accountsCreateDTO.link() +
                "?" +
                "email=" + accountsCreateDTO.email() +
                "&" +
                "token=" + tokenGenerated
            );

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