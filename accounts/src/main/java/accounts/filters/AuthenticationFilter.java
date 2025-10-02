package accounts.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

// ========================================================== (Constructor init)

    // Keys
    // -------------------------------------------------------------------------
    @Value("${SECRET_KEY}")
    private String secretKey;

    @Value("${PUBLIC_KEY}")
    private String publicKey;
    // -------------------------------------------------------------------------

    // Public Endpoints
    // -------------------------------------------------------------------------
    private final List<String> publicPaths = Arrays.asList(
        "/accounts/signup",
        "/accounts/activate-account",
        "/accounts/update-password-link",
        "/accounts/update-password",
        "/accounts/login",
        "/accounts/static/public/**",
        // "/accounts/static/uploads/avatar/**", Remove in production
        "/accounts/refresh-login"
    );

    public List<String> getPublicPaths() {
        return new ArrayList<>(publicPaths);
    }
    // -------------------------------------------------------------------------

    private final MessageSource messageSource;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private SecretKey aesKey;

    public AuthenticationFilter(

        MessageSource messageSource

    ) {

        this.messageSource = messageSource;

    }
    // ======================================================= (Constructor end)

    // =================================================== (Post construct init)
    @PostConstruct
    private void init() {

        try {

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.aesKey = new SecretKeySpec(keyBytes, "AES");

        } catch (Exception e) {

            throw new SecurityException("Failed to initialize AES key " +
                "[ AccountsAuthFilter.init() ]: ");

        }

    }
    // ==================================================== (Post construct end)

    // ================================================ (Assistant methods init)

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

            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[encryptedData.length - iv.length];

            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);

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
    // ================================================= (Assistant methods end)

    @Override
    protected void doFilterInternal(

        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain

    ) throws RuntimeException, IOException {

        try {

            // Language
            Locale locale = LocaleContextHolder.getLocale();

            // ---------------------------------- (Route not authenticated init)
            String requestPath = request.getRequestURI();

            boolean isNotProtected = publicPaths.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, requestPath)
            );

            if (isNotProtected) {
                filterChain.doFilter(request, response);
                return;
            }
            // ----------------------------------- (Route not authenticated end)

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

            Object idUser = claims.get("id");
            String emailUser = claims.get("email", String.class);
            String levelUser = claims.get("level", String.class);
            // ----------------------------------------------- (Claim's map end)

            // Convert roles
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + levelUser.toUpperCase())
            );

            // Create token
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(emailUser, null, authorities);

            // User credentials data
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("id", idUser);
            dataMap.put("email", emailUser);
            dataMap.put("level", levelUser);

            // set attributes in the request
            request.setAttribute("credentialsData", dataMap);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            // Language
            Locale locale = LocaleContextHolder.getLocale();

            serverError(locale, response);

        }

    }
}
