package email_management_service.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mailMessage = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mailMessage.setTo(recipient);
            mailMessage.setSubject(subject);
            mailMessage.setText(message, true);

            javaMailSender.send(mimeMessage);

        } catch (Exception e) {

            throw new InternalError("Error sending email in email service " +
                "[ ExecuteEmailService.sendSimpleEmail() ]: " + e);

        }

    }

}