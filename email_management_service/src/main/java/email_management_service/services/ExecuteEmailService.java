package email_management_service.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ExecuteEmailService {

    // atributes
    private final JavaMailSender javaMailSender;

    // constructor
    public ExecuteEmailService (
        JavaMailSender javaMailSender
    ) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendSimpleEmail(

        String recipient,
        String subject,
        String message

    ) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipient);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);

    }

}