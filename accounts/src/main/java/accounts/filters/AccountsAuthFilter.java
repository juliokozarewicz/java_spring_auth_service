package accounts.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
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
      SECRET_KEY_JWT
      PRIVATE_KEY
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
        "/accounts/delete-account-link"

    );
    // ========================================================== (Settings end)

    // ====================================================== (Constructor init)

    // Keys
    // -------------------------------------------------------------------------
    @Value("${SECRET_KEY_JWT}")
    private String secretKeyJWT;

    @Value("${PRIVATE_KEY}")
    private String privateKey;
    // -------------------------------------------------------------------------

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

            SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyJWT.getBytes());

            Jws<Claims> parsedJwt = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .setAllowedClockSkewSeconds(0)
                .build()
                .parseClaimsJws(token);

            String alg = parsedJwt.getHeader().getAlgorithm();

            if (!SignatureAlgorithm.HS256.getValue().equals(alg)) {
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

            byte[] decoded = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));
            String[] encryptedBlocks = encryptedText.split(":::");
            StringBuilder decryptedText = new StringBuilder();

            for (String block : encryptedBlocks) {

                byte[] encryptedBytes = Base64.getUrlDecoder().decode(block);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

                decryptedText.append(new String(decryptedBytes, StandardCharsets.UTF_8));

            }

            return decryptedText.toString();

        } catch (Exception e) {

            throw new HttpMessageNotReadableException("Error decrypting " +
                "[ EncryptionService.decrypt() ]: " + e);

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