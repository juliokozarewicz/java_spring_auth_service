package accounts.services;

import accounts.enums.KafkaTopicEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
    @SneakyThrows // ##### try catch
    public void sendMessage(

        String recipient,
        String subject,
        String message

    ) {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("recipient", recipient);
        payload.put("subject", subject);
        payload.put("message", message);

        String json = mapper.writeValueAsString(payload);
        kafkaTemplate.send(KafkaTopicEnum.SEND_SIMPLE_EMAIL, json);

    }

}
