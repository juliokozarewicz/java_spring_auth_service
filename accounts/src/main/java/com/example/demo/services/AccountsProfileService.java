package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.dtos.AccountsProfileDTO;
import com.example.demo.persistence.entities.AccountsEntity;
import com.example.demo.persistence.entities.AccountsProfileEntity;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.utils.StandardResponse;
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

    public ResponseEntity execute(

        Map<String, String> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id");
        String emailUser = credentialsData.get("email");
        String levelUser = credentialsData.get("level");

        // find user
        Optional<AccountsProfileEntity> findProfileUser =  profileRepository
        .findById(
            idUser
        );

        // Convert to DTO
        AccountsProfileDTO dtoProfile = new AccountsProfileDTO();
        dtoProfile.setName(findProfileUser.get().getName());
        dtoProfile.setPhone(findProfileUser.get().getPhone());
        dtoProfile.setIdentityDocument(findProfileUser.get().getIdentityDocument());
        dtoProfile.setGender(findProfileUser.get().getGender());
        dtoProfile.setBirthdate(findProfileUser.get().getBirthdate());
        dtoProfile.setBiography(findProfileUser.get().getBiography());
        dtoProfile.setProfileImage(findProfileUser.get().getProfileImage());
        dtoProfile.setLanguage(findProfileUser.get().getLanguage());

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile");
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