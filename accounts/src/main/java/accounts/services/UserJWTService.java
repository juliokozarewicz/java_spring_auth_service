package accounts.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class UserJWTService {

    // JWT lifespan
    private static final long EXPIRATION_TIME = 120000; // 2 minutes

    @Value("${PRIVATE_KEY}")
    private String privateKey;

    // Create credentials
    // -------------------------------------------------------------------------
    public String createCredential(

        Map<String, String> claims

    ) {

        try {

            byte[] keyBytes = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        } catch (Exception e) {

            throw new InternalError("Error generating JWT " +
                "[ UserJWTService.createCredential() ]: " + e);

        }

    }
    // -------------------------------------------------------------------------

}