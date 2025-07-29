package email_management_service.utils;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // productor
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Message send to Kafka: " + message);
    }

    // consumer
    @KafkaListener(topics = "topic-exemple", groupId = "meu-grupo")
    public void consumeMessage(String mensagem) {
        System.out.println("message received from Kafka: " + mensagem);
    }

}
