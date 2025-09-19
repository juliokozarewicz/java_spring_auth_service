package accounts.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class UserJWTService {

    // JWT lifespan
    private static final long EXPIRATION_TIME = 120000; // 2 minutes

    // Keys
    @Value("${SECRET_KEY}")
    private String secretKey;

    // Create credentials
    // -------------------------------------------------------------------------
    public String createCredential(

        Map<String, String> claims

    ) {

        try {

            SecretKey secretKeySHA = Keys.hmacShaKeyFor(secretKey.getBytes());

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKeySHA, SignatureAlgorithm.HS256)
                .compact();

        } catch (Exception e) {

            throw new InternalError("Error generating JWT " +
                "[ UserJWTService.createCredential() ]: " + e);

        }

    }
    // -------------------------------------------------------------------------

}