package com.example.demo.filters;

import com.example.demo.exceptions.ErrorHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
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
    // Required environment variables:
    // - PRIVATE_DOMAIN  → Internal hostname or IP of the accounts service.
    // - ACCOUNTS_PORT   → Port number where the accounts service is reachable.
    // - BASE_URL_ACCOUNTS → Base name for the service URL.
    //
    // Protected routes:
    // - Add or modify protected paths inside the `protectedPaths` list
    //   defined in the `doFilterInternal` method.
    //
    // Controller usage:
    // - In protected controllers, read the validated credentials data like this:
    //
    //     @SuppressWarnings("unchecked")
    //     Map<String, Object> credentialsData = (Map<String, Object>)
    //         request.getAttribute("credentialsData");
    //
    // - Then pass it to your service:
    //
    //     return myService.execute(credentialsData);
    //
    // - Make sure your controller method includes HttpServletRequest as a parameter.
    //
    // Do not modify this filter unless you understand the authentication flow.
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
    private final MessageSource messageSource;

    // constructor
    public AccountsAuthFilter(
        MessageSource messageSource,
        @Value("${BASE_URL_ACCOUNTS}") String baseURLAccounts
    ) {
        this.messageSource = messageSource;

        // Set protected paths using injected base URL
        this.protectedPaths = List.of(
            "/" + baseURLAccounts + "/profile-update",
            "/" + baseURLAccounts + "/profile-get"
        );
    }
    // =========================================================================

    // errors
    // =========================================================================
    private void invalidResponse(
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

        try {

            // authenticated routes
            // =================================================================

            // if the route does not need to be authenticated
            String requestPath = request.getRequestURI();

            if (protectedPaths.stream().noneMatch(requestPath::startsWith)) {
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

            final Pattern BASE64URL_REGEX = Pattern.compile("^[A-Za-z0-9_-]{8,}$");

            // validate token
            if (
                accessCredential == null ||
                !BASE64URL_REGEX.matcher(accessCredential).matches()
            ) {
                invalidResponse(locale, response);
                return;
            }
            // =================================================================

            // endpoint from env
            String urlRequest = "http://" +
                privateDomain +
                ":" +
                accountsPort +
                "/accounts/jwt-credentials-validation?accessToken=" +
                accessCredential;

            // request
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response)
                throws IOException {
                    return response.getStatusCode() != HttpStatus.UNAUTHORIZED &&
                    super.hasError(response);
                }
            });

            ResponseEntity<String> AccountsServiceResponse = restTemplate
                .getForEntity(
                    urlRequest,
                    String.class
                );

            if (
                AccountsServiceResponse.getStatusCode()
                .equals(HttpStatus.UNAUTHORIZED)
            ) {
                invalidResponse(locale, response);
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

            // set attributes in the request
            request.setAttribute("credentialsData", dataMap);

            // continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            serverError(locale, response);

        }

    }

}