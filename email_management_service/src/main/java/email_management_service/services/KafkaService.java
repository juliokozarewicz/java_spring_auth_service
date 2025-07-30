package email_management_service.services;

import email_management_service.enums.KafkaTopicEnum;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // producer
    public void sendMessage(String message) {

        kafkaTemplate.send( KafkaTopicEnum.SEND_SIMPLE_EMAIL, message);
        System.out.println("Message send to Kafka: " + message);

    }

    // consumer
    @KafkaListener( topics = KafkaTopicEnum.SEND_SIMPLE_EMAIL )
    public void receiveMessage(String mensagem) {

        System.out.println("message received from Kafka: " + mensagem);

    }

}
