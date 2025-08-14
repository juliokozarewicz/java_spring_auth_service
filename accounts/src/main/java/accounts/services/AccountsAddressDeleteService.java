package accounts.services;

import accounts.dtos.AccountsAddressDeleteDTO;
import accounts.dtos.AccountsAddressGetDTO;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsAddressEntity;
import accounts.persistence.repositories.AccountsAddressRepository;
import accounts.persistence.repositories.AccountsProfileRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountsAddressDeleteService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsAddressRepository accountsAddressRepository;
    private final CacheManager cacheManager;
    private final Cache jwtCache;

    // constructor
    public AccountsAddressDeleteService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        AccountsAddressRepository accountsAddressRepository,
        AccountsProfileRepository accountsProfileRepository,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.accountsAddressRepository = accountsAddressRepository;
        this.cacheManager = cacheManager;
        this.jwtCache = cacheManager.getCache("addressCache");

    }

    // execute
    public ResponseEntity execute(

        Map<String, Object> credentialsData,
        AccountsAddressDeleteDTO accountsAddressDeleteDTO

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("email").toString();






        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/address-delete");
        customLinks.put("next", "/accounts/address-get");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "address_deleted_success",
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