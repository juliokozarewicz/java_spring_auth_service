package accounts.services;

import accounts.dtos.AccountsAddressDTO;
import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsAddressEntity;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsAddressRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AccountsAddressCreateService {

    // constructor
    private final MessageSource messageSource;
    private final AccountsAddressRepository accountsAddressRepository;
    private final ErrorHandler errorHandler;

    public AccountsAddressCreateService (

        MessageSource messageSource,
        AccountsAddressRepository accountsAddressRepository,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.accountsAddressRepository = accountsAddressRepository;
        this.errorHandler = errorHandler;

    }

    public ResponseEntity execute (

        Map<String, Object> credentialsData,
        AccountsAddressDTO accountsAddressDTO

    ) {

        // Language
        Locale locale = LocaleContextHolder.getLocale();

        // Auth
        String idUser = credentialsData.get("id").toString();

        // Find address
        List<AccountsAddressEntity> findAddress =  accountsAddressRepository.findByUserId(
            idUser
        );

        // Keep five address
        if ( findAddress.size() >= 5 ) {

            // call custom error
            errorHandler.customErrorThrow(
                422,
                messageSource.getMessage(
                    "address_inserted_limit", null, locale
                )
            );

        }

        // Address already exists
        boolean addressExists = findAddress.stream().anyMatch(
            existingAddress ->
                existingAddress.getAddressName().equals(accountsAddressDTO.addressName()) &&
                existingAddress.getZipCode().equals(accountsAddressDTO.zipCode())
        );

        if ( addressExists ) {

            // call custom error
            errorHandler.customErrorThrow(
                422,
                messageSource.getMessage(
                    "address_inserted_exists", null, locale
                )
            );

        }

        // UUID and Timestamp
        String generatedUUID = UUID.randomUUID().toString();
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Timestamp nowTimestamp = Timestamp.from(nowUtc.toInstant());

        // Commit db
        AccountsAddressEntity newAddress = new AccountsAddressEntity();
        newAddress.setId(generatedUUID);
        newAddress.setCreatedAt(nowTimestamp.toLocalDateTime());
        newAddress.setAddressName(accountsAddressDTO.addressName());
        newAddress.setZipCode(accountsAddressDTO.zipCode());
        newAddress.setStreet(accountsAddressDTO.street());
        newAddress.setNumber(accountsAddressDTO.number());
        newAddress.setAddressLineTwo(accountsAddressDTO.addressLineTwo());
        newAddress.setNeighborhood(accountsAddressDTO.neighborhood());
        newAddress.setCity(accountsAddressDTO.city());
        newAddress.setState(accountsAddressDTO.state());
        newAddress.setCountry(accountsAddressDTO.country());
        newAddress.setAddressType(accountsAddressDTO.addressType());
        newAddress.setIsPrimary(accountsAddressDTO.isPrimary());
        newAddress.setLandmark(accountsAddressDTO.landmark());
        newAddress.setUserId(idUser);

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
                    "address_created_success",
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
