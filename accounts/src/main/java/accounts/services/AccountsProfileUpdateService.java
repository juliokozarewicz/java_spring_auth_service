package accounts.services;

import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.ProfileRepository;
import accounts.utils.StandardResponse;
import accounts.validations.AccountsProfileUpdateValidation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    @CacheEvict(value = "profileCache", key = "#credentialsData['id']")
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsProfileUpdateValidation accountsProfileUpdateValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();

        // find user
        Optional<AccountsProfileEntity> findProfile =  profileRepository
        .findById(
            idUser
        );

        // Invalid user
        if ( findProfile.isEmpty() ) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        // Update profile
        AccountsProfileEntity profileUpdated = findProfile.get();

        if ( accountsProfileUpdateValidation != null) {

            if (accountsProfileUpdateValidation.name() != null) {
                profileUpdated.setName(
                    accountsProfileUpdateValidation.name()
                );
            }

            if (accountsProfileUpdateValidation.phone() != null) {
                profileUpdated.setPhone(
                    accountsProfileUpdateValidation.phone()
                );
            }

            if (accountsProfileUpdateValidation.identityDocument() != null) {
                profileUpdated.setIdentityDocument(
                    accountsProfileUpdateValidation.identityDocument()
                );
            }

            if (accountsProfileUpdateValidation.gender() != null) {
                profileUpdated.setGender(
                    accountsProfileUpdateValidation.gender()
                );
            }

            if (accountsProfileUpdateValidation.birthdate() != null) {
                profileUpdated.setBirthdate(
                    accountsProfileUpdateValidation.birthdate()
                );
            }

            if (accountsProfileUpdateValidation.biography() != null) {
                profileUpdated.setBiography(
                    accountsProfileUpdateValidation.biography()
                );
            }

            if (accountsProfileUpdateValidation.language() != null) {
                profileUpdated.setLanguage(
                    accountsProfileUpdateValidation.language()
                );
            }

            profileRepository.save(profileUpdated);

        }

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-update");
        customLinks.put("next", "/accounts/profile-get");

        // Response
        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "update_profile_success",
                    null,
                    locale
                )
            )
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}