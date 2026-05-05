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

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Welcome to Ly's Learning Management System");
        message.setText(
                "To proceed further, this is your verification otp code: " + otp
                + "\n\nThis is only for 10 minutes and dont share it around!!"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }

    public void successOtp(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Welcome to Ly's Learning Management System");
        message.setText(
                "You have verify your account, now you can proceed to use our system. \n"
                        +"Thank you!!!"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }
}
