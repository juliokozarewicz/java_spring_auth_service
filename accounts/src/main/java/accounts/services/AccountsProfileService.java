package accounts.services;

import accounts.dtos.AccountsProfileDTO;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountsProfileService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsProfileRepository accountsProfileRepository;
    private final CacheManager cacheManager;
    private final Cache profileCache;

    // constructor
    public AccountsProfileService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsProfileRepository accountsProfileRepository,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsProfileRepository = accountsProfileRepository;
        this.cacheManager = cacheManager;
        this.profileCache = cacheManager.getCache("profileCache");

    }

    // execute
    public ResponseEntity execute(

        Map<String, Object> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // Init dto profile
        AccountsProfileDTO dtoProfile = new AccountsProfileDTO();

        // Redis cache ( get or set )
        // =================================================================
        Cache.ValueWrapper cached = profileCache.get(idUser);

        dtoProfile = cached != null
            ? (AccountsProfileDTO) cached.get()
            : new AccountsProfileDTO();

        if (cached == null) {

            // If the cache does not exist, do a hard query
            Optional<AccountsProfileEntity> findProfileUser = accountsProfileRepository
                .findById(idUser);

            // Invalid user
            if (findProfileUser.isEmpty()) {

                // call custom error
                errorHandler.customErrorThrow(
                    404,
                    messageSource.getMessage(
                        "response_invalid_credentials", null, locale
                    )
                );

            }

            AccountsProfileEntity entity = findProfileUser.get();

            dtoProfile.setName(entity.getName());
            dtoProfile.setPhone(entity.getPhone());
            dtoProfile.setIdentityDocument(entity.getIdentityDocument());
            dtoProfile.setGender(entity.getGender());
            dtoProfile.setBirthdate(entity.getBirthdate());
            dtoProfile.setBiography(entity.getBiography());
            dtoProfile.setProfileImage(entity.getProfileImage());
            dtoProfile.setLanguage(entity.getLanguage());

            profileCache.put(idUser, dtoProfile);

        }

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-get");
        customLinks.put("next", "/accounts/profile-update");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .data(dtoProfile)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}