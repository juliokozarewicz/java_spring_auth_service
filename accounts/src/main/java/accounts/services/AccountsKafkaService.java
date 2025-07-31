package accounts.services;

import accounts.enums.KafkaTopicEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccountsKafkaService {

    // constructor
    // =========================================================================
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AccountsKafkaService(

        KafkaTemplate<String, String> kafkaTemplate

    ) {

        this.kafkaTemplate = kafkaTemplate;

    }
    // =========================================================================

    // producer
    public void sendSimpleEmailMessage(

        String recipient,
        String subject,
        String message

    ) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("recipient", recipient);
            payload.put("subject", subject);
            payload.put("message", message);

            String json = mapper.writeValueAsString(payload);
            kafkaTemplate.send(KafkaTopicEnum.SEND_SIMPLE_EMAIL, json);

        } catch (Exception e) {

            throw new InternalError("Error creating message for kafka in account " +
                "service [ AccountsKafkaService.sendSimpleEmailMessage() ]: " + e);

        }

    }

}
