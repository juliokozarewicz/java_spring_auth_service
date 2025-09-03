package accounts.filters;

import accounts.services.UserJWTService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Order(1)
public class AccountsAuthFilter extends OncePerRequestFilter {

    // ===================================================== (Instructions init)
    /*

     * Have internationalization (i18n) already configured
     * Have the encryption service configured
     * Set the "baseURLAccounts" variable in "Settings" section
     * Add the protected endpoint to "protectedPaths" in "Settings" section

    */
    // ====================================================== (Instructions end)

    // ========================================================= (Settings init)
    String baseURLAccounts = "accounts";

    List<String> protectedPaths = List.of(

        "/" + baseURLAccounts + "/update-profile",
        "/" + baseURLAccounts + "/get-profile",
        "/" + baseURLAccounts + "/create-address",
        "/" + baseURLAccounts + "/get-address",
        "/" + baseURLAccounts + "/delete-address",
        "/" + baseURLAccounts + "/update-email-link",
        "/" + baseURLAccounts + "/update-email",
        "/" + baseURLAccounts + "/connected-devices"

    );
    // ========================================================== (Settings end)

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

    // ================================================ (Assistant methods end)

    // ====================================================== (Constructor init)

    private final MessageSource messageSource;
    private final UserJWTService userJWTService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccountsAuthFilter(

        MessageSource messageSource,
        UserJWTService userJWTService

    ) {

        this.messageSource = messageSource;
        this.userJWTService = userJWTService;

    }

    // ======================================================= (Constructor end)

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
            // -------------------------------------- (Get jwt from header init)

            // --------------------------------------------- (Validate JWT init)
            Boolean validCredentials = userJWTService.isCredentialsValid(accessCredential);

            if (!validCredentials) {
                invalidAccessError(locale, response);
                return;
            }

            Claims claims = null;
            claims = userJWTService.getCredentialsData(accessCredential);
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