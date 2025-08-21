package accounts.filters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class AccountsAuthFilter extends OncePerRequestFilter {

    // Instructions
    // =========================================================================
    /*

    > Required environment variables:
     - PRIVATE_DOMAIN  → Internal hostname or IP of the accounts service.
     - ACCOUNTS_PORT   → Port number where the accounts service is reachable.
     - BASE_URL_ACCOUNTS → Base name for the service URL.

    > Protected routes:
     - Add or modify protected paths inside the `protectedPaths` list
       defined in the `doFilterInternal` method.

    > Controller usage:
     - In protected controllers, read the validated credentials data like this:

         @SuppressWarnings("unchecked")
         Map<String, Object> credentialsData = (Map<String, Object>)
             request.getAttribute("credentialsData");

     - Then pass it to your service:

         return myService.execute(credentialsData);

     - Make sure your controller method includes HttpServletRequest as a parameter.

    > Add to services:
        // Credentials
        String idUser = credentialsData.get("id").toString();
        String emailUser = credentialsData.get("userEmail").toString();
        String levelUser = credentialsData.get("level").toString();

    > You should also apply the following configuration::
    //--------------------------------------------------------------------------
    package com.example.demo.configurations;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.client.ClientHttpResponse;
    import org.springframework.web.client.DefaultResponseErrorHandler;
    import org.springframework.web.client.RestTemplate;
    import java.io.IOException;

    @Configuration
    public class RestTemplateConfig {

        @Bean
        public RestTemplate restTemplate() {

            RestTemplate restTemplate = new RestTemplate();

            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) throws IOException {
                    return response.getStatusCode() != HttpStatus.UNAUTHORIZED &&
                        super.hasError(response);
                }
            });

            return restTemplate;
        }
    }
    //--------------------------------------------------------------------------

    > Configure a two-minute cache with redis and import here.

    > Do not modify this filter unless you understand the authentication flow.
    */
    // =========================================================================

    // env vars
    // =========================================================================
    @Value("${PRIVATE_DOMAIN}")
    private String privateDomain;

    @Value("${ACCOUNTS_PORT}")
    private String accountsPort;
    // =========================================================================

    // constructor
    // =========================================================================
    private final List<String> protectedPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final MessageSource messageSource;
    private final RestTemplate restTemplate;
    private final String baseURLAccounts;
    private final CacheManager cacheManager;
    private final Cache jwtCache;

    // constructor
    public AccountsAuthFilter(
        MessageSource messageSource,
        RestTemplate restTemplate,
        CacheManager cacheManager,
        @Value("${BASE_URL_ACCOUNTS}") String baseURLAccounts
    ) {
        this.messageSource = messageSource;
        this.restTemplate = restTemplate;
        this.baseURLAccounts = baseURLAccounts;
        this.cacheManager = cacheManager;
        this.jwtCache = cacheManager.getCache("jwtValidationCache");
        this.protectedPaths = List.of(
            "/" + baseURLAccounts + "/update-profile",
            "/" + baseURLAccounts + "/get-profile",
            "/" + baseURLAccounts + "/create-address",
            "/" + baseURLAccounts + "/get-address",
            "/" + baseURLAccounts + "/delete-address",
            "/" + baseURLAccounts + "/update-userEmail-link"
        );
    }
    // =========================================================================

    // errors
    // =========================================================================
    private void invalidAccessError(
        Locale locale,
        HttpServletResponse response
    ) throws IOException {

        response.setStatus(401);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", 401);
        errorResponse.put("statusMessage", "error");
        errorResponse.put(
            "message",
            messageSource.getMessage(
                "response_invalid_credentials", null, locale
            )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);

    }

    private void serverError(
        Locale locale,
        HttpServletResponse response
    ) throws IOException {

        response.setStatus(503);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", 503);
        errorResponse.put("statusMessage", "error");
        errorResponse.put(
            "message",
            messageSource.getMessage(
                "response_response_server_error", null, locale
            )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);

    }
    // =========================================================================

    // filter
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws RuntimeException, IOException {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // remove malicious data
        request.removeAttribute("credentialsData");

        try {

            // authenticated routes
            // =================================================================

            // if the route does not need to be authenticated
            String requestPath = request.getRequestURI();

            boolean isProtected = protectedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));

            if (!isProtected) {
                filterChain.doFilter(request, response);
                return;
            }
            // =================================================================

            // get token from header
            // =================================================================
            String accessCredentialRaw = request.getHeader("Authorization");

            String accessCredential = accessCredentialRaw != null ?
                accessCredentialRaw.replace("Bearer ", "") :
                null;

            // validate token
            final Pattern BASE64URL_REGEX = Pattern.compile("^[A-Za-z0-9_-]{8,}$");

            if (
                accessCredential == null ||
                !BASE64URL_REGEX.matcher(accessCredential).matches()
            ) {
                invalidAccessError(locale, response);
                return;
            }
            // =================================================================

            // Redis cache
            // =================================================================
            Cache.ValueWrapper cached = jwtCache.get(accessCredential);

            if (cached != null && cached.get() instanceof Map) {

                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cached.get();
                request.setAttribute("credentialsData", cachedData);
                filterChain.doFilter(request, response);
                return;

            }
            // =================================================================

            // endpoint from env
            String urlRequest = "http://" + privateDomain + ":" + accountsPort +
                "/" + baseURLAccounts + "/jwt-credentials-validation";

            // request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessCredential);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> AccountsServiceResponse = restTemplate
                .exchange(urlRequest, HttpMethod.POST, requestEntity, String.class);

            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response)
                throws IOException {
                    return response.getStatusCode() != HttpStatus.UNAUTHORIZED &&
                    super.hasError(response);
                }
            });

            if (
                AccountsServiceResponse.getStatusCode()
                .equals(HttpStatus.UNAUTHORIZED)
            ) {
                invalidAccessError(locale, response);
                return;
            };

            // convert to generic json
            String responseBody = AccountsServiceResponse.getBody();

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> responseMap = mapper.readValue(
                responseBody, new TypeReference<>() {}
            );

            Map<String, Object> dataMap = mapper.convertValue(
                responseMap.get("data"),
                new TypeReference<>() {
                }
            );

            // Redis storage cache
            jwtCache.put(accessCredential, dataMap);

            // set attributes in the request
            request.setAttribute("credentialsData", dataMap);

            // continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            serverError(locale, response);

        }

    }

}