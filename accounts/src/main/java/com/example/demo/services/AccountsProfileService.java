package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.dtos.AccountsProfileDTO;
import com.example.demo.persistence.entities.AccountsProfileEntity;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.utils.StandardResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsProfileService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final ProfileRepository profileRepository;
    private final CacheManager cacheManager;
    private final Cache jwtCache;

    // constructor
    public AccountsProfileService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        ProfileRepository profileRepository,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.profileRepository = profileRepository;
        this.cacheManager = cacheManager;
        this.jwtCache = cacheManager.getCache("profileCache");

    }

    // execute
    public ResponseEntity execute(

        Map<String, Object> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();

        // Init dto profile:
        AccountsProfileDTO dtoProfile = new AccountsProfileDTO();

        // Redis cache ( get or set )
        // =================================================================
        Cache.ValueWrapper cached = jwtCache.get(idUser);

        if (cached != null) {

            dtoProfile = (AccountsProfileDTO) cached.get();

        } else {

            // If the cache does not exist, do a hard query
            Optional<AccountsProfileEntity> findProfileUser = profileRepository
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

            jwtCache.put(idUser, dtoProfile);

        }

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-get");
        customLinks.put("next", "/accounts/profile-update");

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .data(dtoProfile != null ? dtoProfile : null)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}