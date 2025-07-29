package email_management_service.services;

import email_management_service.utils.ExecuteEmailService;
import email_management_service.utils.KafkaService;
import email_management_service.utils.StandardResponse;
import email_management_service.validations.ExecuteEmailValidation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ExecuteSendEmailService {

    // attributes
    private final MessageSource messageSource;
    private final ExecuteEmailService executeEmailService;
    private final KafkaService kafkaService;

    // constructor
    public ExecuteSendEmailService(

        MessageSource messageSource,
        ExecuteEmailService executeEmailService,
        KafkaService kafkaService

    ) {

        this.messageSource = messageSource;
        this.executeEmailService = executeEmailService;
        this.kafkaService = kafkaService;

    }

    public ResponseEntity execute(

        ExecuteEmailValidation executeEmailValidation

    ) {

        // language
        Locale locale = LocaleContextHolder.getLocale();

        kafkaService.sendMessage("testex", "*** 132456798 ***");

        StandardResponse response = new StandardResponse.Builder()
            .statusCode(200)
            .statusMessage("success")
            .build();
        return ResponseEntity
            .status(response.getStatusCode())
            .body(response);

    }

}