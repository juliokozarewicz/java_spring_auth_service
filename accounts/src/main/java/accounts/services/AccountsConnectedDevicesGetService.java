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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class AccountsConnectedDevicesGetService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final CacheManager cacheManager;
    private final Cache ArrayLoginsCache;
    private final Cache refreshLoginCache;
    private final RestTemplate restTemplate;
    private final AccountsManagementService accountsManagementService;

    // constructor
    public AccountsConnectedDevicesGetService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        CacheManager cacheManager,
        RestTemplate restTemplate,
        AccountsManagementService accountsManagementService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.cacheManager = cacheManager;
        this.ArrayLoginsCache = cacheManager.getCache("ArrayLoginsCache");
        this.refreshLoginCache = cacheManager.getCache("refreshLoginCache");
        this.restTemplate = restTemplate;
        this.accountsManagementService = accountsManagementService;

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

        // Clean expired tokens
        accountsManagementService.deleteExpiredRefreshTokensListById(
            idUser
        );

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

                if (wrapper == null) {
                    continue;
                }

                AccountsCacheRefreshTokenDTO refreshLogin = (
                    AccountsCacheRefreshTokenDTO
                ) wrapper.get();

                String url = UriComponentsBuilder.fromHttpUrl(
                    "http://ip-api.com/json/" + refreshLogin.getUserIp())
                    .queryParam(
                        "fields",
                        "status," +
                        "country," +
                        "regionName," +
                        "city," +
                        "lat," +
                        "lon," +
                        "message"
                    )
                    .toUriString();

                Map<String, Object> geoData = restTemplate.getForObject(
                    url,
                    Map.class
                );

                Map<String, String> device = new LinkedHashMap<>();
                device.put("createdAt", token.getTimestamp().toString());
                device.put("deviceName", refreshLogin.getUserAgent());

                // Fill geo data if request was successful or not
                boolean success = geoData != null && "success".equals(geoData.get("status"));

                if (success) {
                    device.put("country", String.valueOf(geoData.getOrDefault("country", "")));
                    device.put("regionName", String.valueOf(geoData.getOrDefault("regionName", "")));
                    device.put("city", String.valueOf(geoData.getOrDefault("city", "")));
                    device.put("lat", String.valueOf(geoData.getOrDefault("lat", "")));
                    device.put("lon", String.valueOf(geoData.getOrDefault("lon", "")));
                }

                if (!success) {
                    device.put("country", null);
                    device.put("regionName", null);
                    device.put("city", null);
                    device.put("lat", null);
                    device.put("lon", null);
                }

                connectedDevices.add(device);

            }

        } catch (Exception e) {

            System.out.println(e);

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