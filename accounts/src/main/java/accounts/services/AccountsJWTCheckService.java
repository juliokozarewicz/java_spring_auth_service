package accounts.services;

import accounts.exceptions.ErrorHandler;
import accounts.persistence.entities.AccountsEntity;
import accounts.persistence.repositories.AccountsRepository;
import accounts.utils.EncryptionService;
import accounts.utils.StandardResponse;
import accounts.utils.UserJWTService;
import accounts.validations.AccountsJWTCheckValidation;
import io.jsonwebtoken.Claims;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountsJWTCheckService {

    // attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;
    private final UserJWTService userJWTService;
    private final EncryptionService encryptionService;
    private final AccountsRepository accountsRepository;

    // constructor
    public AccountsJWTCheckService(

        MessageSource messageSource,
        ErrorHandler errorHandler,
        UserJWTService userJWTService,
        AccountsRepository accountsRepository,
        EncryptionService encryptionService

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;
        this.userJWTService = userJWTService;
        this.accountsRepository = accountsRepository;
        this.encryptionService = encryptionService;

    }

    public ResponseEntity execute(

        AccountsJWTCheckValidation accountsJWTCheckValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        try {

            // decrypt jwt
            String decryptedJWT = encryptionService.decrypt(
                accountsJWTCheckValidation.accessToken()
            );

            // validate credentials
            Boolean validCredentials = userJWTService.isCredentialsValid(
                decryptedJWT
            );

            if (!validCredentials) {
                // call custom error
                errorHandler.customErrorThrow(
                    401,
                    messageSource.getMessage(
                        "response_invalid_credentials", null, locale
                    )
                );
            }

            // get info from jwt
            Claims claims = null;
            claims = userJWTService.getCredentialsData(decryptedJWT);

            // find email
            Optional<AccountsEntity> findUser = accountsRepository.findByEmail(
                claims.get("email").toString()
            );

            // Invalid user credentials
            if (
                findUser.isEmpty() ||
                !findUser.get().isActive() ||
                findUser.get().isBanned()
            ) {

                // call custom error
                errorHandler.customErrorThrow(
                    401,
                    messageSource.getMessage(
                        "response_invalid_credentials", null, locale
                    )
                );

            }

            // Tokens data
            Map<String, String> tokensData = new LinkedHashMap<>();
            tokensData.put("id", claims.get("id").toString());
            tokensData.put("email", claims.get("email").toString());
            tokensData.put("level", findUser.get().getLevel());

            // Response
            StandardResponse response = new StandardResponse.Builder()
                .statusCode(200)
                .statusMessage("success")
                .data(tokensData)
                .build();

            return ResponseEntity
                .status(response.getStatusCode())
                .body(response);

        } catch (Exception e) {

            // call custom error
            errorHandler.customErrorThrow(
                401,
                messageSource.getMessage(
                    "response_invalid_credentials", null, locale
                )
            );

        }

        throw new IllegalStateException();

    }

}