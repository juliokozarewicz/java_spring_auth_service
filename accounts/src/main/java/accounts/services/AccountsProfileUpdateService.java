package accounts.services;

import accounts.dtos.AccountsProfileUpdateDTO;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsProfileEntity;
import accounts.persistence.repositories.AccountsProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountsProfileUpdateService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsProfileRepository accountsProfileRepository;

    // constructor
    public AccountsProfileUpdateService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsProfileRepository accountsProfileRepository

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsProfileRepository = accountsProfileRepository;

    }

    @CacheEvict(value = "profileCache", key = "#credentialsData['id']")
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsProfileUpdateDTO accountsProfileUpdateDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // find user
        Optional<AccountsProfileEntity> findProfile =  accountsProfileRepository
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

        if ( accountsProfileUpdateDTO != null) {

            if (accountsProfileUpdateDTO.name() != null) {
                profileUpdated.setName(
                    accountsProfileUpdateDTO.name()
                );
            }

            if (accountsProfileUpdateDTO.phone() != null) {
                profileUpdated.setPhone(
                    accountsProfileUpdateDTO.phone()
                );
            }

            if (accountsProfileUpdateDTO.identityDocument() != null) {
                profileUpdated.setIdentityDocument(
                    accountsProfileUpdateDTO.identityDocument()
                );
            }

            if (accountsProfileUpdateDTO.gender() != null) {
                profileUpdated.setGender(
                    accountsProfileUpdateDTO.gender()
                );
            }

            if (accountsProfileUpdateDTO.birthdate() != null) {
                profileUpdated.setBirthdate(
                    accountsProfileUpdateDTO.birthdate()
                );
            }

            if (accountsProfileUpdateDTO.biography() != null) {
                profileUpdated.setBiography(
                    accountsProfileUpdateDTO.biography()
                );
            }

            if (accountsProfileUpdateDTO.language() != null) {
                profileUpdated.setLanguage(
                    accountsProfileUpdateDTO.language()
                );
            }

            accountsProfileRepository.save(profileUpdated);

        }

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/profile-update");
        customLinks.put("next", "/accounts/profile-get");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_update_profile_success",
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