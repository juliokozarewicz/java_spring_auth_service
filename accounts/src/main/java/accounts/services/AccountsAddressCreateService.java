package accounts.services;

import accounts.dtos.AccountsAddressCreateDTO;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsAddressEntity;
import accounts.persistence.repositories.AccountsAddressRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsAddressCreateService {

    // constructor
    private final MessageSource messageSource;
    private final AccountsAddressRepository accountsAddressRepository;
    private final ErrorHandler errorHandler;
    private final AccountsManagementService accountsManagementService;

    public AccountsAddressCreateService (

        MessageSource messageSource,
        AccountsAddressRepository accountsAddressRepository,
        ErrorHandler errorHandler,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.accountsAddressRepository = accountsAddressRepository;
        this.errorHandler = errorHandler;
        this.accountsManagementService = accountsManagementService;

    }

    @CacheEvict(value = "addressCache", key = "#credentialsData['id']")
    public ResponseEntity execute (

        Map<String, Object> credentialsData,
        AccountsAddressCreateDTO accountsAddressCreateDTO

    ) {

        // Language
        Locale locale = LocaleContextHolder.getLocale();

        // Auth
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // Find address
        List<AccountsAddressEntity> findAddress =  accountsAddressRepository.findByIdUser(
            idUser
        );

        // Keep five address
        if ( findAddress.size() >= 5 ) {

            // call custom error
            errorHandler.customErrorThrow(
                422,
                messageSource.getMessage(
                    "response_address_inserted_limit", null, locale
                )
            );

        }

        // Address already exists
        boolean addressExists = findAddress.stream().anyMatch(
            existingAddress ->
                existingAddress.getAddressName().equals(accountsAddressCreateDTO.addressName()) &&
                existingAddress.getZipCode().equals(accountsAddressCreateDTO.zipCode())
        );

        if ( addressExists ) {

            // call custom error
            errorHandler.customErrorThrow(
                409,
                messageSource.getMessage(
                    "response_address_inserted_exists", null, locale
                )
            );

        }

        // ID and Timestamp
        UUID generatedUniqueId = accountsManagementService.createUniqueId();
        Instant nowUtc = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

        // Commit db
        AccountsAddressEntity newAddress = new AccountsAddressEntity();
        newAddress.setId(generatedUniqueId);
        newAddress.setCreatedAt(nowUtc);
        newAddress.setAddressName(accountsAddressCreateDTO.addressName());
        newAddress.setZipCode(accountsAddressCreateDTO.zipCode());
        newAddress.setStreet(accountsAddressCreateDTO.street());
        newAddress.setNumber(accountsAddressCreateDTO.number());
        newAddress.setAddressLineTwo(accountsAddressCreateDTO.addressLineTwo());
        newAddress.setNeighborhood(accountsAddressCreateDTO.neighborhood());
        newAddress.setCity(accountsAddressCreateDTO.city());
        newAddress.setState(accountsAddressCreateDTO.state());
        newAddress.setCountry(accountsAddressCreateDTO.country());
        newAddress.setAddressType(accountsAddressCreateDTO.addressType());
        newAddress.setIsPrimary(accountsAddressCreateDTO.isPrimary());
        newAddress.setLandmark(accountsAddressCreateDTO.landmark());
        newAddress.setIdUser(idUser);

        // Set primary address
        if ( newAddress.getIsPrimary() ) {
            findAddress.forEach( existingAddress -> {
                existingAddress.setIsPrimary(false);
                accountsAddressRepository.save(existingAddress);
            });
        }

        accountsAddressRepository.save(newAddress);

        // Response
        // ---------------------------------------------------------------------

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/address-create");
        customLinks.put("next", "/accounts/address-get");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(201)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_address_created_success",
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