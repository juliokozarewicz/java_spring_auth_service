package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.dtos.AccountsProfileDTO;
import com.example.demo.persistence.entities.AccountsProfileEntity;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.utils.StandardResponse;
import org.springframework.cache.annotation.Cacheable;
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

    // constructor
    public AccountsProfileService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        ProfileRepository profileRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.profileRepository = profileRepository;

    }

    // get profile method
    @Cacheable(value = "profileCache", key = "#idUser")
    public AccountsProfileDTO getProfileDTO(

        String idUser

    ) {

        Optional<AccountsProfileEntity> findProfileUser = profileRepository.findById(idUser);

        AccountsProfileEntity entity = findProfileUser.get();

        AccountsProfileDTO dtoProfile = new AccountsProfileDTO();
        dtoProfile.setName(entity.getName());
        dtoProfile.setPhone(entity.getPhone());
        dtoProfile.setIdentityDocument(entity.getIdentityDocument());
        dtoProfile.setGender(entity.getGender());
        dtoProfile.setBirthdate(entity.getBirthdate());
        dtoProfile.setBiography(entity.getBiography());
        dtoProfile.setProfileImage(entity.getProfileImage());
        dtoProfile.setLanguage(entity.getLanguage());

        return dtoProfile;

    }

    // execute
    public ResponseEntity execute(

        Map<String, Object> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();

        // get profile dto
        AccountsProfileDTO dtoProfile = getProfileDTO(idUser);

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-get");
        customLinks.put("next", "/accounts/profile-update");

        // Response
        StandardResponse response = new StandardResponse.Builder()
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