package accounts.services;

import accounts.dtos.AccountsCacheRefreshTokenDTO;
import accounts.dtos.AccountsCacheRefreshTokensListDTO;
import accounts.dtos.AccountsCacheRefreshTokensListMetaDTO;
import accounts.exceptions.ErrorHandler;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountsConnectedDevicesGetService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final CacheManager cacheManager;
    private final Cache ArrayLoginsCache;
    private final Cache refreshLoginCache;

    // constructor
    public AccountsConnectedDevicesGetService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        CacheManager cacheManager

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.cacheManager = cacheManager;
        this.ArrayLoginsCache = cacheManager.getCache("ArrayLoginsCache");
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");

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

        // Get cache
        Cache.ValueWrapper arrayLoginsCache = ArrayLoginsCache.get(idUser);

        // Connected devices
        List<Map<String, String>> connectedDevices = new ArrayList<>();

        // Iterate tokens
        try {

            Object value = arrayLoginsCache.get();
            AccountsCacheRefreshTokensListDTO dto = (AccountsCacheRefreshTokensListDTO) value;
            List<AccountsCacheRefreshTokensListMetaDTO> metaList = dto.getRefreshTokensActive();

            for (AccountsCacheRefreshTokensListMetaDTO token : metaList) {

                String refreshToken = token.getRefreshToken();
                Cache.ValueWrapper wrapper = refreshLoginCache.get(refreshToken);
                AccountsCacheRefreshTokenDTO refreshLogin = (AccountsCacheRefreshTokenDTO) wrapper.get();

                Map<String, String> device = new LinkedHashMap<>();
                device.put("createdAt", token.getTimestamp().toString());
                device.put("deviceName", refreshLogin.getUserAgent());
                device.put("country", "");
                device.put("regionName", "");
                device.put("city", "");
                device.put("lat", "");
                device.put("lon", "");

                connectedDevices.add(device);

            }

        } catch (Exception e) {

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_get_connected_devices_error", null, locale
                )
            );

        }
        // =================================================================

        // Links
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/accounts/connected-devices");
        customLinks.put("next", "/accounts/profile-get");

        // Response
        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_get_connected_devices_success",
                    null,
                    locale
                )
            )
            .data(connectedDevices)
            .links(customLinks)
            .build();

        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}