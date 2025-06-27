package com.example.demo.utils;

import com.example.demo.exceptions.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Service
public class AuthEndpointService {

    // =========================================================================
    /*

        To authenticate an endpoint:
         * Perform dependency injection in the endpoint controller;
         * Paste this before anything else, inside the handle method
           in the controller::

            ------------------------

            // Auth endpoint
            authEndpointService.validateCredentialJWT(
                request.getHeader("Authorization")
                .replace("Bearer ", "")
            );

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

    public ResponseEntity<String> validateCredentialJWT(String accessToken) {

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

            return response;

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