package accounts.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Component
@Order(1)
public class AccountsAuthFilter extends OncePerRequestFilter {

    // ===================================================== (Instructions init)
    /*

    * keep this filter above all others "@Order(1)"

    * Configure and request access to the vault to get the variables:
    ------------------------------------------------------------------------
      SECRET_KEY
      PUBLIC_KEY
    ------------------------------------------------------------------------

    * Have internationalization (i18n) already configured (en):
    ------------------------------------------------------------------------
        response_invalid_credentials=Invalid credentials.
        response_response_server_error=An unexpected error occurred, please try again later.
    ------------------------------------------------------------------------

    * Add the protected endpoint to "protectedPaths" in "Settings" section
    * Request the public key to validate the jwt and place it in "src/main/resources/keys/public_key.pem"

    * Add this to your controller, don't forget to pass "credentialsData" to the service:
    ------------------------------------------------------------------------
    // Auth endpoint
    Map<String, Object> credentialsData = (Map<String, Object>)
    request.getAttribute("credentialsData");

    return <serviceNameService>.execute(credentialsData);
    -------------------------------------------------------------------------

    * Add this to your service:
    -------------------------------------------------------------------------
    // Credentials
    UUID idUser = UUID.fromString((String) credentialsData.get("id"));
    String emailUser = credentialsData.get("email).toString();
    String levelUser = credentialsData.get("level").toString();
    -------------------------------------------------------------------------

    */
    // ====================================================== (Instructions end)

    // ========================================================= (Settings init)
    List<String> protectedPaths = List.of(

        "/accounts/update-profile",
        "/accounts/get-profile",
        "/accounts/create-address",
        "/accounts/get-address",
        "/accounts/delete-address",
        "/accounts/update-email-link",
        "/accounts/update-email",
        "/accounts/connected-devices",
        "/accounts/delete-account-link",
        "/accounts/delete"

    );
    // ========================================================== (Settings end)

    // ====================================================== (Constructor init)

    // Keys
    // -------------------------------------------------------------------------
    @Value("${SECRET_KEY}")
    private String secretKey;

    @Value("${PUBLIC_KEY}")
    private String publicKey;
    // -------------------------------------------------------------------------

    private SecretKey aesKey;
    private final MessageSource messageSource;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccountsAuthFilter(

        MessageSource messageSource

    ) {

        this.messageSource = messageSource;

    }
    // ======================================================= (Constructor end)

    // ================================================ (Assistant methods init)
    // Invalid access error (401)
    private void invalidAccessError(
        Locale locale,
        HttpServletResponse response
    ) throws IOException {

        response.setStatus(401);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", 401);
        errorResponse.put("statusMessage", "error");
        errorResponse.put(
            "message",
            messageSource.getMessage(
                "response_invalid_credentials", null, locale
            )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);

    }

    // Server error (500)
    private void serverError(
        Locale locale,
        HttpServletResponse response
    ) throws IOException {

        response.setStatus(500);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", 500);
        errorResponse.put("statusMessage", "error");
        errorResponse.put(
            "message",
            messageSource.getMessage(
                "response_response_server_error", null, locale
            )
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);

    }

    // JWT validate
    public Claims parseAndValidateToken(String token) {

        try {

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKeyObj = keyFactory.generatePublic(keySpec);

            Jws<Claims> parsedJwt = Jwts.parserBuilder()
                .setSigningKey(publicKeyObj)
                .setAllowedClockSkewSeconds(0)
                .build()
                .parseClaimsJws(token);

            String alg = parsedJwt.getHeader().getAlgorithm();

            if (!SignatureAlgorithm.RS256.getValue().equals(alg)) {
                throw new SecurityException("Invalid JWT algorithm: " + alg);
            }

            return parsedJwt.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT: " + e.getMessage(), e);
        }

    }

    // decryption
    public String decrypt(String encryptedText) {

        try {

            byte[] encryptedData = Base64.getUrlDecoder().decode(encryptedText);

            byte[] salt = new byte[16];
            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedData.length - salt.length - iv.length];

            System.arraycopy(encryptedData, 0, salt, 0, salt.length);
            System.arraycopy(encryptedData, salt.length, iv, 0, iv.length);
            System.arraycopy(encryptedData, salt.length + iv.length, ciphertext, 0, ciphertext.length);

            PBEKeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, 1_000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {

            throw new SecurityException("Error decrypting " +
                "[ AccountsAuthFilter.decrypt() ]");

        }

    }
    // ================================================ (Assistant methods end)

    // ====================================================== (Main method init)
    @Override
    protected void doFilterInternal(

        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain

    ) throws RuntimeException, IOException {

        try {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            // ---------------------------------- (Route not authenticated init)
            String requestPath = request.getRequestURI();

            boolean isProtected = protectedPaths.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, requestPath)
            );

            if (!isProtected) {
                filterChain.doFilter(request, response);
                return;
            }
            // ----------------------------------- (Route not authenticated end)

            // -------------------------------------- (Get jwt from header init)
            String accessCredentialRaw = request.getHeader("Authorization");

            String accessCredential = accessCredentialRaw != null ?
                accessCredentialRaw.replace("Bearer ", "") :
                null;

            if (accessCredential == null || accessCredential.isBlank()) {
                invalidAccessError(locale, response);
                return;
            }
            // -------------------------------------- (Get jwt from header init)

            // --------------------------------------------- (Validate JWT init)
            Claims claims;

            try { claims = parseAndValidateToken(decrypt(accessCredential)); }
            catch (Exception e) { invalidAccessError(locale, response); return; }
            // ---------------------------------------------- (Validate JWT end)

            // ---------------------------------------------- (Claim's map init)
            if (
                claims.get("id") == null ||
                claims.get("email") == null ||
                claims.get("level") == null
            ) {
                invalidAccessError(locale, response);
                return;
            }

            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("id", claims.get("id"));
            dataMap.put("email", claims.get("email"));
            dataMap.put("level", claims.get("level"));
            // ----------------------------------------------- (Claim's map end)

            // set attributes in the request
            request.setAttribute("credentialsData", dataMap);

            // continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            serverError(locale, response);

        }

    }
    // ======================================================= (Main method end)

}