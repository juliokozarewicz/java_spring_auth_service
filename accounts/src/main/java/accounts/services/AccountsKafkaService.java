package accounts.services;

import accounts.enums.KafkaTopicEnum;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
    public void sendMessage(String message) {

        kafkaTemplate.send( KafkaTopicEnum.SEND_SIMPLE_EMAIL, message);
        System.out.println("Message send to Kafka: " + message);

    }

}
