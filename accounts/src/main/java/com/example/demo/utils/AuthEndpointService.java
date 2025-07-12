package com.example.demo.utils;

import com.example.demo.exceptions.ErrorHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthEndpointService {

    // =========================================================================
    /*

        To authenticate an endpoint:
         * Perform dependency injection in the endpoint controller;
         * Paste this before anything else, inside the handle method
           in the controller:
            ------------------------
            // Auth endpoint
            String accessCredential = request.getHeader("Authorization");
            Map<String, String> credentialsData = authEndpointService
                .validateCredentialJWT(
                    accessCredential != null ?
                    accessCredential.replace("Bearer ", "") :
                    null
            );
            ------------------------

        * Paste this before anything else, inside the execute method
           in the service:
           ------------------------
           // Credentials
           String idUser = credentialsData.get("id");
           String emailUser = credentialsData.get("email");
           String levelUser = credentialsData.get("level");
           ------------------------

    */
    // =========================================================================

    @Value("${PRIVATE_DOMAIN}")
    private String privateDomain;

    @Value("${ACCOUNTS_PORT}")
    private String accountsPort;

    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    // constructor
    public AuthEndpointService(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }

    public Map<String, String> validateCredentialJWT(String accessToken) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        try {

            // Endpoint from .env
            String url = "http://" +
                privateDomain +
                ":" +
                accountsPort +
                "/accounts/jwt-credentials-validation?accessToken=" +
                accessToken;

            RestTemplate restTemplate = new RestTemplate();

            // Request
            ResponseEntity<String> response = restTemplate.getForEntity(
                url,
                String.class
            );

            // Convert to generic json
            String responseBody = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

            // Object response
            Map<String, String> credentialsData = new LinkedHashMap<>();
            credentialsData.put("id", dataMap.get("id").toString());
            credentialsData.put("email", dataMap.get("email").toString());
            credentialsData.put("level", dataMap.get("level").toString());

            return credentialsData;

        } catch (Exception e) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

            return null;

        }

    }

}