package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(

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