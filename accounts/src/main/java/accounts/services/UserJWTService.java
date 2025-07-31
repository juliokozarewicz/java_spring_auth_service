package accounts.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class UserJWTService {

    // Secret key
    @Value("${SECRET_KEY}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // JWT lifespan
    private static final long EXPIRATION_TIME = 120000; // 2 minutes

    // Create credentials
    // -------------------------------------------------------------------------
    public String createCredential(

        Map<String, String> claims

    ) {

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();

    }
    // -------------------------------------------------------------------------

    // Verify credential
    // -------------------------------------------------------------------------
    public boolean isCredentialsValid(String token) {

        try {

            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(0)
                .build()
                .parseClaimsJws(token);

            return true;

        } catch (Exception e) {

            return false;

        }

    }
    // -------------------------------------------------------------------------

    // Get credential data
    // -------------------------------------------------------------------------
    public Claims getCredentialsData(String token) throws Exception {

        try {

            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        } catch (Exception e) {

            throw new RuntimeException(e.getMessage());

        }

    }
    // -------------------------------------------------------------------------

}