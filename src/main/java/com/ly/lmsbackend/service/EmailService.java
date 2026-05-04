package com.ly.lmsbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String FROM_EMAIL;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(
            String toEmail, String name
    ){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Welcome to Ly's Learning Management System");
        message.setText(
                "Welcome "
                + name
                + " to this amazing system built on Java 25, Spring Boot 4 and Oracle!!!"
                + " \n Please go and verify your account before proceeding."
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }
}
