package com.example.demo.utils;

import com.example.demo.exceptions.ErrorHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthEndpointService {

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

    public void validateCredentialJWT(String accessToken) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        try {

            // ##### get endpoint from .env
            String url = "http://192.168.0.105:3003" +
                "/accounts/jwt-credentials-validation";


            // Body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("accessToken", accessToken);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Body + Header
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(
                requestBody,
                headers
            );

            RestTemplate restTemplate = new RestTemplate();

            // Request
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                requestEntity,
                String.class
            );

            System.out.println(response);

        } catch (Exception e) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

    }
}