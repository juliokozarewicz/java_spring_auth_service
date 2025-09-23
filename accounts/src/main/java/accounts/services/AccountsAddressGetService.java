package accounts.services;

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
public class AccountsAddressGetService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final AccountsAddressRepository accountsAddressRepository;
    private final CacheManager cacheManager;
    private final Cache jwtCache;

    // constructor
    public AccountsAddressGetService(

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
    @SuppressWarnings("unchecked")
    public ResponseEntity execute(

        Map<String, Object> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        UUID idUser = UUID.fromString((String) credentialsData.get("id"));

        // Init dto address
        List<AccountsAddressGetDTO> dtoAddressList = new ArrayList<>();

        // Redis cache ( get or set )
        // =================================================================
        Cache.ValueWrapper cached = jwtCache.get(idUser);

        dtoAddressList = cached != null
            ? (List<AccountsAddressGetDTO>) cached.get()
            : new ArrayList<>();

        if ( cached == null ) {

            List<AccountsAddressEntity> findAddress = accountsAddressRepository
                .findByIdUser(idUser);

            for (AccountsAddressEntity entity : findAddress) {

                AccountsAddressGetDTO dto = new AccountsAddressGetDTO();

                dto.setAddressId(entity.getId());
                dto.setAddressName(entity.getAddressName());
                dto.setZipCode(entity.getZipCode());
                dto.setStreet(entity.getStreet());
                dto.setNumber(entity.getNumber());
                dto.setAddressLineTwo(entity.getAddressLineTwo());
                dto.setNeighborhood(entity.getNeighborhood());
                dto.setCity(entity.getCity());
                dto.setState(entity.getState());
                dto.setCountry(entity.getCountry());
                dto.setAddressType(entity.getAddressType());
                dto.setIsPrimary(entity.getIsPrimary());
                dto.setLandmark(entity.getLandmark());

                dtoAddressList.add(dto);

            }

            jwtCache.put(idUser, dtoAddressList);

        }
        // =================================================================

        // Metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("totalItems", dtoAddressList.size());

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/address-get");
        customLinks.put("next", "/accounts/address-create");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .data(dtoAddressList)
            .meta(metadata)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}