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
        message.setSubject("Ly's Learning Management System");
        message.setText(
                "You have verify your account, now you can proceed to use our system. \n"
                        +"Thank you!!!"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }

    public void sendWelcomeLogin(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Ly's Learning Management System");
        message.setText(
                "Welcome to Ly's Learning Management System!"
                +"Enjoy your time with us!!!"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }

    public void sendResetOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("Ly's Learning Management System");
        message.setText(
                "This is your reset password otp: " + otp
                        + "\n\nThis is only for 10 minutes and dont share it around!!"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }

    public void sendAbaPayWayEnrollmentSuccess(
            String toEmail,
            String studentName,
            String courseTitle,
            String instructorName,
            String amount,
            String transactionRef,
            String enrollmentDate,
            String courseUrl
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject("🎉 You're In! Course Access Confirmed for " + courseTitle);
        message.setText(
                "Dear " + studentName + ",\n\n" +
                "Great news! We have successfully received and verified your payment via ABA PayWay. " +
                "Your enrollment for \"" + courseTitle + "\" has been approved, and your access is now fully active!\n\n" +
                "Here are your enrollment and transaction details:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "• Course Name: " + courseTitle + "\n" +
                "• Instructor: " + instructorName + "\n" +
                "• Amount Paid: $" + amount + " USD\n" +
                "• Payment Method: ABA PayWay (KHQR / Card)\n" +
                "• Transaction Reference: " + transactionRef + "\n" +
                "• Enrollment Date: " + enrollmentDate + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "What’s next?\n" +
                "You can now access all course lessons, download supplemental materials, and start learning immediately:\n\n" +
                "👉 Go to My Courses: " + courseUrl + "\n\n" +
                "Tips for getting started:\n" +
                "1. Log in to your student dashboard.\n" +
                "2. Watch the introductory lessons and track your progress.\n" +
                "3. Check the \"Assignments\" tab for any upcoming tasks and course deadlines.\n\n" +
                "If you have any questions or experience any issues accessing your course, simply reply directly to this email or reach out to our support team.\n\n" +
                "Happy learning!\n\n" +
                "Best regards,\n" +
                "The Lumen LMS Team"
        );
        message.setSentDate(new java.util.Date());
        mailSender.send(message);
    }
}
