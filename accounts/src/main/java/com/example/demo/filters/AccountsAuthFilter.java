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

    // env vars
    // =========================================================================
    @Value("${PRIVATE_DOMAIN}")
    private String privateDomain;

    @Value("${ACCOUNTS_PORT}")
    private String accountsPort;
    // =========================================================================

    // constructor
    // =========================================================================
    private final MessageSource messageSource;

    public AccountsAuthFilter(MessageSource messageSource) {
        this.messageSource = messageSource;
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

    // Filter
    @Override
    protected void doFilterInternal(

        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain

    ) throws RuntimeException, IOException {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        try {

            // Authenticated routes
            // =================================================================
            final List<String> protectedPaths = List.of(
                "/accounts/profile-update",
                "/accounts/profile-get"
            );

            // if the route does not need to be authenticated
            String requestPath = request.getRequestURI();

            if (protectedPaths.stream().noneMatch(requestPath::startsWith)) {
                filterChain.doFilter(request, response);
                return;
            }
            // =================================================================

            // Get token from header
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

            // Endpoint from env
            String urlRequest = "http://" +
                privateDomain +
                ":" +
                accountsPort +
                "/accounts/jwt-credentials-validation?accessToken=" +
                accessCredential;

            // Request
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

            // Convert to generic json
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

            // Set attributes in the request
            request.setAttribute("credentialsData", dataMap);

            // Continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            serverError(locale, response);

        }

    }

}