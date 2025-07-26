package accounts.services;

import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.enums.UserLevelEnum;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.persistence.repositories.ProfileRepository;
import accounts.persistence.repositories.VerificationTokenRepository;
import accounts.utils.EncryptionService;
import accounts.utils.StandardResponse;
import accounts.validations.AccountsCreateValidation;
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

    // attributes
    private final MessageSource messageSource;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final ProfileRepository profileRepository;
    private final AccountsManagementService accountsManagementService;
    private final VerificationTokenRepository verificationTokenRepository;

    // constructor
    public AccountsCreateService (

        MessageSource messageSource,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        ProfileRepository profileRepository,
        AccountsManagementService accountsManagementService,
        VerificationTokenRepository verificationTokenRepository

    ) {

        this.messageSource = messageSource;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.profileRepository = profileRepository;
        this.accountsManagementService = accountsManagementService;
        this.verificationTokenRepository = verificationTokenRepository;

    }

    @Transactional
    public ResponseEntity execute(

        AccountsCreateValidation accountsCreateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // find user
        Optional<AccountsEntity> findUser =  accountsRepository.findByEmail(
            accountsCreateValidation.email().toLowerCase()
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
                accountsCreateValidation.email().toLowerCase(),
                EmailResponsesEnum.ACCOUNT_EXIST_ACTIVATED_ERROR.getDescription(),
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
            newAccount.setLevel(UserLevelEnum.USER.getDescription());
            newAccount.setEmail(accountsCreateValidation.email().toLowerCase());
            newAccount.setPassword(
                encryptionService.hashPassword(
                    accountsCreateValidation.password()
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
            newProfile.setName(accountsCreateValidation.name());
            profileRepository.save(newProfile);

        }
        // ---------------------------------------------------------------------

        if (

            findUser.isEmpty() ||
            !findUser.get().isActive() &&
            !findUser.get().isBanned()

        ) {

            // Delete all old tokens
            verificationTokenRepository
                .findByEmail(accountsCreateValidation.email().toLowerCase())
                .forEach(verificationTokenRepository::delete);

            // Create token
            String tokenGenerated =
            accountsManagementService.createToken(
                accountsCreateValidation.email().toLowerCase(),
                AccountsUpdateEnum.ACTIVATE_ACCOUNT.getDescription()
            );

            // Link
            String linkFinal = (
                accountsCreateValidation.link() +
                "?" +
                "email=" + accountsCreateValidation.email() +
                "&" +
                "token=" + tokenGenerated
            );

            // send email
            accountsManagementService.sendEmailStandard(
                accountsCreateValidation.email().toLowerCase(),
                EmailResponsesEnum.ACTIVATE_ACCOUNT_SUCCESS.getDescription(),
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

        StandardResponse response = new StandardResponse.Builder()
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