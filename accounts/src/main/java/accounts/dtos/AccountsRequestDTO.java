package accounts.dtos;

import accounts.exceptions.ErrorHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class AccountsRequestDTO {

    // Attributes
    private final MessageSource messageSource;
    private final ErrorHandler errorHandler;

    // constructor
    public AccountsRequestDTO(

        MessageSource messageSource,
        ErrorHandler errorHandler

    ) {

        this.messageSource = messageSource;
        this.errorHandler = errorHandler;

    }

    // Validate IP
    public void validateUserIp(String userIp) {

        // Regex
        Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4]" +
                "[0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
        );

        if (
            userIp == null ||
                userIp.isEmpty() ||
                !IP_PATTERN.matcher(userIp).matches()
        ) {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_bad_request", null, locale
                )
            );

        }

    }

    // Validate Agent
    public void validateUserAgent(String userAgent) {

        // Regex
        Pattern USER_AGENT_PATTERN = Pattern.compile(
            "^[\\w\\d\\s\\.\\/\\-\\(\\)\\;\\,\\:]+$"
        );

        if (
            userAgent == null ||
                userAgent.isEmpty() ||
                !USER_AGENT_PATTERN.matcher(userAgent).matches()
        ) {

            // language
            Locale locale = LocaleContextHolder.getLocale();

            // call custom error
            errorHandler.customErrorThrow(
                404,
                messageSource.getMessage(
                    "response_bad_request", null, locale
                )
            );

        }
    }

}