package com.example.demo.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    public void validateCredentialJWT(String accessToken) {
        String url = "http://192.168.0.105:3003/accounts/jwt-credentials-validation";

        // Body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("accessToken", accessToken);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Body + Header
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        // Request
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Resposta OK:");
            System.out.println(response.getBody());
        } else {
            System.out.println("Erro na requisição. Status: " + response.getStatusCodeValue());
        }
    }
}