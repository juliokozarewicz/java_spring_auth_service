package accounts.services;

import accounts.dtos.AccountsConnectedDevicesGetDTO;
import accounts.exceptions.ErrorHandler;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsConnectedDevicesGetService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final CacheManager cacheManager;
    private final Cache profileCache;

    // constructor
    public AccountsConnectedDevicesGetService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.cacheManager = cacheManager;
        this.profileCache = cacheManager.getCache("profileCache");

    }

    // execute
    public ResponseEntity execute(

        Map<String, Object> credentialsData

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // Credentials
        String idUser = credentialsData.get("id").toString();

        // Redis cache ( get or set )
        // =================================================================

        // Connected devices
        Map<String, String> connectedDevices = new LinkedHashMap<>();
        connectedDevices.put("deviceName", "");
        connectedDevices.put("country", "");
        connectedDevices.put("regionName", "");
        connectedDevices.put("city", "");
        connectedDevices.put("lat", "");
        connectedDevices.put("lon", "");
        // =================================================================

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/connected-devices");
        customLinks.put("next", "/accounts/profile-get");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            //.data(connectedDevices != null ? connectedDevices : null)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}