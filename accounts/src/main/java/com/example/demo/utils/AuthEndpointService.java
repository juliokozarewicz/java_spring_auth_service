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

    @Value("${PRIVATE_DOMAIN}")
    private String privateDomain;

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
            String url = "http://" + privateDomain + ":3003" +
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