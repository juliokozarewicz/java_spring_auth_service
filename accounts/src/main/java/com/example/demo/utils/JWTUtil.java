package com.example.demo.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JWTUtil {

    @Value("${secretKey}")
    private static String secretKey;

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME = 120000; // 2 minutes

    public static String createCredential(

        String subject,
        Map<String, String> claims

    ) {

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact();

    }

    public static boolean isCredentialsValid(String token) {

        try {

            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);

            return true;

        } catch (Exception e) {

            return false;

        }

    }

    public static Claims getCredentialsData(String token) throws Exception {

        try {

            return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        } catch (Exception e) {

            throw new RuntimeException(e.getMessage());

        }

    }

}