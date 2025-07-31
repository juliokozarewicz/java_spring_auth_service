package helloworld.services;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class HelloWorldService {

    // attributes
    private final MessageSource messageSource;

    // constructor
    public HelloWorldService(
        MessageSource messageSource
    ) {
        this.messageSource = messageSource;
    }

    public ResponseEntity execute(
        String message
    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        // response (links)
        Map<String, String> customLinks = new LinkedHashMap<>();
        customLinks.put("self", "/helloworld/helloworld");
        customLinks.put("next", "/documentation/swagger");

        StandardResponseService response = new StandardResponseService.Builder()
            .statusCode(200)
            .statusMessage("success")
            .message(
                messageSource.getMessage(
                    "response_get_data_success",
                    null,
                    locale
                ) + " (" + message + ")"
            )
            .links(customLinks)
            .build();
        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}