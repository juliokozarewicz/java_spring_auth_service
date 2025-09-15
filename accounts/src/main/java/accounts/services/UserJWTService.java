package accounts.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class UserJWTService {

    // JWT lifespan
    private static final long EXPIRATION_TIME = 120000; // 2 minutes

    // Constructor
    // -------------------------------------------------------------------------

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public UserJWTService() {
        try {

            this.privateKey = loadPrivateKey();
            this.publicKey = loadPublicKey();

        } catch (Exception e) {

            throw new InternalError("Failed to load RSA keys " +
                "[ UserJWTService.UserJWTService() ]: " + e);

        }
    }
    // -------------------------------------------------------------------------

    // Load keys
    // -------------------------------------------------------------------------
    private PrivateKey loadPrivateKey() throws Exception {
        String key = new String(Files.readAllBytes(
            Paths.get("src/main/resources/keys/private_key.pem")),
            StandardCharsets.UTF_8
        );
        key = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        String key = new String(Files.readAllBytes(
            Paths.get("src/main/resources/keys/public_key.pem")),
            StandardCharsets.UTF_8
        );
        key = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
    // -------------------------------------------------------------------------

    // Create credentials
    // -------------------------------------------------------------------------
    public String createCredential(

        Map<String, String> claims

    ) {

        try {

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS512)
                .compact();

        } catch (Exception e) {

            throw new InternalError("Error generating JWT " +
                "[ UserJWTService.createCredential() ]: " + e);

        }

    }
    // -------------------------------------------------------------------------

}