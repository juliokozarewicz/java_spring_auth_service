package com.example.demo.services;

import com.example.demo.exceptions.ErrorHandler;
import com.example.demo.persistence.repositories.ProfileRepository;
import com.example.demo.utils.StandardResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AccountsProfileUpdateService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final ProfileRepository profileRepository;

    // constructor
    public AccountsProfileUpdateService(

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

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-update");
        customLinks.put("next", "/accounts/profile");

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success_***PENDING***")
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}