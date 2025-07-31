package accounts.services;

import accounts.enums.KafkaTopicEnum;
import accounts.persistence.dtos.SendEmailDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccountsKafkaService {

    // constructor
    // =========================================================================
    private final KafkaTemplate<String, SendEmailDataDTO> kafkaTemplate;

    public AccountsKafkaService(

        KafkaTemplate<String, SendEmailDataDTO> kafkaTemplate

    ) {

        this.kafkaTemplate = kafkaTemplate;

    }
    // =========================================================================

    // producer
    public void sendSimpleEmailMessage(

        SendEmailDataDTO sendEmailDataDTO

    ) {

        try {

            kafkaTemplate.send(
                KafkaTopicEnum.SEND_SIMPLE_EMAIL,
                sendEmailDataDTO
            );

        } catch (Exception e) {

            throw new InternalError("Error creating message for kafka in account " +
                "service [ AccountsKafkaService.sendSimpleEmailMessage() ]: " + e);

        }

    }

}
