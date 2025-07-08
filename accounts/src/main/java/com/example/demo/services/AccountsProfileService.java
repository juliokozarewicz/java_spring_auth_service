package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
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
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("email").toString();
        String levelUser = credentialsData.get("level").toString();

        // find user
        Optional<AccountsProfileEntity> findProfileUser =  profileRepository
        .findById(
            idUser
        );

        // user address
        Map<String, String> userAddress = new LinkedHashMap<>();

        // Profile Data
        Map<String, Object> userProfileData = new LinkedHashMap<>();

        userProfileData.put(
            "profileImage",
            findProfileUser.get().getProfileImage()
        );

        userProfileData.put(
            "name",
            findProfileUser.get().getName()

        );

        userProfileData.put(
            "email",
            emailUser
        );

        userProfileData.put(
            "phone",
            findProfileUser.get().getPhone()
        );

        userProfileData.put(
            "identityDocument",
            findProfileUser.get().getIdentityDocument()
        );

        userProfileData.put(
            "gender",
            findProfileUser.get().getGender()
        );

        userProfileData.put(
            "birthdate",
            findProfileUser.get().getBirthdate() != null ?
            findProfileUser.get().getBirthdate() :
            null
        );

        userProfileData.put(
            "language",
            findProfileUser.get().getLanguage()
        );

        userProfileData.put(
            "address",
            userAddress
        );

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile");
        customLinks.put("next", "/accounts/profile-update");

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .data(userProfileData)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}