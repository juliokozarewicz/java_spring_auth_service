package email_management_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import email_management_service.enums.KafkaGroupEnum;
import email_management_service.enums.KafkaTopicEnum;
import email_management_service.utils.ExecuteEmailService;
import email_management_service.validations.ExecuteEmailValidation;
import lombok.SneakyThrows;
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

    public EmailManagementKafkaService(

        KafkaTemplate<String, String> kafkaTemplate,
        ExecuteEmailService executeEmailService

    ) {

        this.kafkaTemplate = kafkaTemplate;
        this.executeEmailService = executeEmailService;

    }
    // =========================================================================

    // consumer
    @KafkaListener(
        topics = KafkaTopicEnum.SEND_SIMPLE_EMAIL,
        groupId = KafkaGroupEnum.EMAIL_MANAGEMENT_SERVICE
    )
    @SneakyThrows // ##### try catch
    public void sendSimpleEmailConsumer(

        String jsonPayload,
        Acknowledgment ack

    ) {

        ExecuteEmailValidation executeEmailValidation = objectMapper.readValue(jsonPayload, ExecuteEmailValidation.class);

        executeEmailService.sendSimpleEmail(
            executeEmailValidation.recipient(),
            executeEmailValidation.subject(),
            executeEmailValidation.message()
        );

        ack.acknowledge();

    }

}
