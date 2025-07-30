package email_management_service.services;

import email_management_service.enums.KafkaGroupEnum;
import email_management_service.enums.KafkaTopicEnum;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class EmailManagementKafkaService {

    // constructor
    // =========================================================================
    private final KafkaTemplate<String, String> kafkaTemplate;

    public EmailManagementKafkaService(

        KafkaTemplate<String, String> kafkaTemplate

    ) {

        this.kafkaTemplate = kafkaTemplate;

    }
    // =========================================================================

    // consumer
    @KafkaListener(
        topics = KafkaTopicEnum.SEND_SIMPLE_EMAIL,
        groupId = KafkaGroupEnum.EMAIL_MANAGEMENT_SERVICE
    )
    public void sendSimpleEmailConsumer(
        String mensagem, Acknowledgment ack
    ) {

        System.out.println("message received from Kafka: " + mensagem);

        ack.acknowledge();

    }

}
