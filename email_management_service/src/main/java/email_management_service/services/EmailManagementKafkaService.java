package email_management_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import email_management_service.enums.KafkaGroupEnum;
import email_management_service.enums.KafkaTopicEnum;
import email_management_service.utils.ExecuteEmailService;
import email_management_service.validations.SendEmailDataValidation;
import jakarta.validation.Validator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class EmailManagementKafkaService {

    // constructor
    // =========================================================================
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ExecuteEmailService executeEmailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator;

    public EmailManagementKafkaService(

        KafkaTemplate<String, String> kafkaTemplate,
        ExecuteEmailService executeEmailService,
        Validator validator

    ) {

        this.kafkaTemplate = kafkaTemplate;
        this.executeEmailService = executeEmailService;
        this.validator = validator;

    }
    // =========================================================================

    // consumer
    @KafkaListener(
        topics = KafkaTopicEnum.SEND_SIMPLE_EMAIL,
        groupId = KafkaGroupEnum.EMAIL_MANAGEMENT_SERVICE
    )
    public void sendSimpleEmailConsumer(

        String jsonPayload,
        Acknowledgment ack

    ) {

        try {

            SendEmailDataValidation sendEmailDataValidation = objectMapper.readValue(
                jsonPayload,
                SendEmailDataValidation.class
            );

            executeEmailService.sendSimpleEmail(
                sendEmailDataValidation.recipient(),
                sendEmailDataValidation.subject(),
                sendEmailDataValidation.message()
            );

            ack.acknowledge();

        } catch (Exception e) {

            throw new InternalError("Error consuming kafka message in email " +
                "sending service [ EmailManagementKafkaService" +
                ".sendSimpleEmailConsumer() ]: " + e);

        }

    }

}
