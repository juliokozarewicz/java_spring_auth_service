package accounts.services;

import accounts.dtos.AccountsCreateDTO;
import accounts.enums.AccountsUpdateEnum;
import accounts.enums.EmailResponsesEnum;
import accounts.enums.UserLevelEnum;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import accounts.persistence.repositories.AccountsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsCreateService {

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    // constructor
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;
    private final AccountsProfileRepository accountsProfileRepository;
    private final AccountsManagementService accountsManagementService;
    private final CacheManager cacheManager;
    private final Cache notActivatedAccountCache;

    public AccountsCreateService (

        MessageSource messageSource,
        ErrorHandler errorHandler,
        EncryptionService encryptionService,
        AccountsRepository accountsRepository,
        AccountsProfileRepository accountsProfileRepository,
        AccountsManagementService accountsManagementService,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;
        this.accountsProfileRepository = accountsProfileRepository;
        this.accountsManagementService = accountsManagementService;
        this.cacheManager = cacheManager;
        this.notActivatedAccountCache = cacheManager.getCache("notActivatedAccountCache");

    }

    @Transactional
    public ResponseEntity execute(

        AccountsCreateDTO accountsCreateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Verify and authorize URL
        // ---------------------------------------------------------------------
        boolean isAllowedURL = false;

        try {

            String linkRaw = accountsCreateDTO.link().trim();

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
                "[AccountsCreateService.execute()]: " + e );

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

            // Set cache
            notActivatedAccountCache.put(generatedUniqueId, nowUtc);

        }
        // ---------------------------------------------------------------------

        // Account exist and user deactivated
        // ---------------------------------------------------------------------
        if (

            findUser.isPresent() &&
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

            // Set cache
            notActivatedAccountCache.put(findUser.get().getId(), nowUtc);

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