package accounts.filters;

import accounts.services.UserJWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Component
@Order(1)
public class AccountsAuthFilter extends OncePerRequestFilter {

    // ===================================================== (Instructions init)
    /*

    * Have internationalization (i18n) already configured
    * Add the protected endpoint to "protectedPaths" in "Settings" section
    * Request the public key to validate the jwt and place it in "src/main/resources/keys/public_key.pem"
    * keep this filter above all others "@Order(1)"

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
    String idUser = credentialsData.get("id").toString();
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
        "/accounts/connected-devices"

    );
    // ========================================================== (Settings end)

    // ====================================================== (Constructor init)
    private final MessageSource messageSource;
    private final PublicKey publicKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccountsAuthFilter(

        MessageSource messageSource

    ) {

        this.messageSource = messageSource;

        try {

            this.publicKey = loadPublicKey();

        } catch (Exception e) {

            throw new InternalError("Failed to load RSA keys " +
                "[ AccountsAuthFilter.AccountsAuthFilter() ]: " + e);

        }

    }
    // ======================================================= (Constructor end)

    // ================================================ (Assistant methods init)
    // Load keys
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
            Jws<Claims> parsedJwt = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .setAllowedClockSkewSeconds(0)
                .build()
                .parseClaimsJws(token);

            String alg = parsedJwt.getHeader().getAlgorithm();

            if (!SignatureAlgorithm.RS512.getValue().equals(alg)) {
                throw new SecurityException("Invalid JWT algorithm: " + alg);
            }

            return parsedJwt.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT: " + e.getMessage(), e);
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

            try {
                claims = parseAndValidateToken(accessCredential);
            } catch (Exception e) {
                invalidAccessError(locale, response);
                return;
            }
            // ---------------------------------------------- (Validate JWT end)

            // ---------------------------------------------- (Claim's map init)
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