package com.ly.lmsbackend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.properties.mail.smtp.from:honglylay14@gmail.com}")
    private String FROM_EMAIL;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String buildHtmlLayout(String title, String bodyContent) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>" + title + "</title>\n"
                + "</head>\n"
                + "<body style=\"margin:0;padding:0;background-color:#f4f5f8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#1e293b;\">\n"
                + "  <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color:#f4f5f8;padding:36px 16px;\">\n"
                + "    <tr>\n"
                + "      <td align=\"center\">\n"
                + "        <table role=\"presentation\" width=\"100%\" style=\"max-width:560px;background:#ffffff;border-radius:12px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.04);\">\n"
                + "          <tr>\n"
                + "            <td style=\"background:linear-gradient(135deg,#4f46e5,#6366f1);padding:30px 32px;text-align:center;\">\n"
                + "              <h1 style=\"margin:0;color:#ffffff;font-size:22px;font-weight:700;letter-spacing:-0.5px;\">Lumen LMS</h1>\n"
                + "              <p style=\"margin:4px 0 0 0;color:#e0e7ff;font-size:13px;letter-spacing:0.2px;\">Learning & Development Portal</p>\n"
                + "            </td>\n"
                + "          </tr>\n"
                + "          <tr>\n"
                + "            <td style=\"padding:32px 36px;\">\n"
                +                bodyContent + "\n"
                + "            </td>\n"
                + "          </tr>\n"
                + "          <tr>\n"
                + "            <td style=\"background:#f8fafc;padding:22px 32px;border-top:1px solid #f1f5f9;text-align:center;\">\n"
                + "              <p style=\"margin:0;color:#64748b;font-size:12px;\">© 2026 Lumen LMS. All rights reserved.</p>\n"
                + "              <p style=\"margin:6px 0 0 0;color:#94a3b8;font-size:11px;\">This is an automated system message. Please do not reply directly to this email.</p>\n"
                + "            </td>\n"
                + "          </tr>\n"
                + "        </table>\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </table>\n"
                + "</body>\n"
                + "</html>";
    }

    private void sendHtmlEmail(String toEmail, String subject, String plainText, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(FROM_EMAIL, "Lumen LMS");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, htmlContent);
            helper.setSentDate(new Date());
            mailSender.send(mimeMessage);
            log.info("Successfully sent email '{}' to {}", subject, toEmail);
        } catch (Exception e) {
            log.warn("HTML email dispatch failed for '{}' to {}: {}. Attempting plain-text fallback.", subject, toEmail, e.getMessage());
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(FROM_EMAIL);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(plainText);
                message.setSentDate(new Date());
                mailSender.send(message);
                log.info("Plain-text fallback email sent successfully to {}", toEmail);
            } catch (Exception ex) {
                log.warn("Could not dispatch fallback email to {}: {}", toEmail, ex.getMessage());
            }
        }
    }

    public void sendOtp(String toEmail, String otp) {
        String subject = "Verify your Lumen LMS account (" + otp + ")";
        String plainText = "Welcome to Lumen LMS!\n\n"
                + "Your account verification code is: " + otp + "\n\n"
                + "This code will expire in 10 minutes.\n"
                + "For your security, do not share this code with anyone.\n\n"
                + "Best regards,\nThe Lumen LMS Team";

        String htmlContent = buildHtmlLayout(
                "Verify Your Email",
                "<h2 style=\"margin:0 0 12px;font-size:18px;color:#0f172a;\">Verify Your Email Address</h2>\n"
                + "<p style=\"margin:0 0 20px;font-size:14px;color:#475569;line-height:1.6;\">\n"
                + "  Thank you for creating an account with <strong>Lumen LMS</strong>. To complete your registration and activate your student workspace, please enter the 6-digit confirmation code below:\n"
                + "</p>\n"
                + "<div style=\"background:#f8fafc;border:2px dashed #cbd5e1;border-radius:10px;padding:22px;text-align:center;margin:24px 0;\">\n"
                + "  <div style=\"font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:1.5px;margin-bottom:8px;\">Verification Code</div>\n"
                + "  <div style=\"font-family:'Courier New',Courier,monospace;font-size:34px;font-weight:800;letter-spacing:8px;color:#4f46e5;\">" + otp + "</div>\n"
                + "  <div style=\"font-size:12px;color:#94a3b8;margin-top:8px;\">Valid for the next 10 minutes</div>\n"
                + "</div>\n"
                + "<p style=\"margin:20px 0 0;font-size:13px;color:#64748b;line-height:1.5;\">\n"
                + "  🔒 <strong>Security Tip:</strong> Never share this code with anyone. Lumen staff will never ask for your verification code.\n"
                + "</p>"
        );

        sendHtmlEmail(toEmail, subject, plainText, htmlContent);
    }

    public void successOtp(String toEmail) {
        String subject = "Welcome to Lumen LMS – Your Account is Active!";
        String plainText = "Congratulations!\n\n"
                + "Your email has been successfully verified, and your Lumen LMS account is now fully active.\n\n"
                + "You can now log in, explore the course catalog, enroll in interactive lessons, and earn certificates.\n\n"
                + "Happy learning!\n\nThe Lumen LMS Team";

        String htmlContent = buildHtmlLayout(
                "Account Activated",
                "<h2 style=\"margin:0 0 12px;font-size:18px;color:#0f172a;\">Account Activated 🎉</h2>\n"
                + "<p style=\"margin:0 0 18px;font-size:14px;color:#475569;line-height:1.6;\">\n"
                + "  Congratulations! Your email has been verified and your account is now ready to use.\n"
                + "</p>\n"
                + "<div style=\"background:#ecfdf5;border:1px solid #a7f3d0;border-radius:8px;padding:16px;margin:20px 0;\">\n"
                + "  <p style=\"margin:0;font-size:13px;color:#065f46;font-weight:600;\">✓ All features are unlocked</p>\n"
                + "  <p style=\"margin:4px 0 0;font-size:12px;color:#047857;\">Browse expert-led courses, test your knowledge with interactive quizzes, and earn verifiable certificates.</p>\n"
                + "</div>\n"
                + "<p style=\"margin:24px 0 0;font-size:13px;color:#64748b;line-height:1.5;\">\n"
                + "  Ready to jump in? Visit our portal to browse catalog offerings and start learning today.\n"
                + "</p>"
        );

        sendHtmlEmail(toEmail, subject, plainText, htmlContent);
    }

    public void sendWelcomeLogin(String toEmail) {
        String subject = "Security Notification: Sign-In to Lumen LMS";
        String plainText = "Hello,\n\n"
                + "We detected a recent sign-in to your Lumen LMS account (" + toEmail + ").\n\n"
                + "If this was you, you can safely ignore this message.\n"
                + "If you did not perform this login, please reset your password immediately to safeguard your account.\n\n"
                + "The Lumen LMS Security Team";

        String htmlContent = buildHtmlLayout(
                "Sign-In Notification",
                "<h2 style=\"margin:0 0 12px;font-size:18px;color:#0f172a;\">New Sign-In Detected</h2>\n"
                + "<p style=\"margin:0 0 16px;font-size:14px;color:#475569;line-height:1.6;\">\n"
                + "  A successful sign-in to your account (<strong>" + toEmail + "</strong>) was just completed.\n"
                + "</p>\n"
                + "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px;margin:20px 0;font-size:13px;color:#475569;\">\n"
                + "  <div style=\"margin-bottom:6px;\"><strong>Platform:</strong> Lumen LMS Web Portal</div>\n"
                + "  <div><strong>Timestamp:</strong> " + new Date() + "</div>\n"
                + "</div>\n"
                + "<p style=\"margin:16px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;\">\n"
                + "  If you initiated this sign-in, you don't need to do anything. If you suspect unauthorized access, please update your password immediately from your account profile.\n"
                + "</p>"
        );

        sendHtmlEmail(toEmail, subject, plainText, htmlContent);
    }

    public void sendResetOtp(String toEmail, String otp) {
        String subject = "Password Reset Request – Lumen LMS (" + otp + ")";
        String plainText = "Hello,\n\n"
                + "We received a request to reset the password for your Lumen LMS account.\n\n"
                + "Your password reset code is: " + otp + "\n\n"
                + "This code is valid for 10 minutes.\n"
                + "If you did not request a password reset, please ignore this email. Your current password remains secure.\n\n"
                + "The Lumen LMS Team";

        String htmlContent = buildHtmlLayout(
                "Reset Password",
                "<h2 style=\"margin:0 0 12px;font-size:18px;color:#0f172a;\">Reset Your Password</h2>\n"
                + "<p style=\"margin:0 0 20px;font-size:14px;color:#475569;line-height:1.6;\">\n"
                + "  We received a request to reset your password. Enter the 6-digit confirmation code below in the password reset form:\n"
                + "</p>\n"
                + "<div style=\"background:#fff7ed;border:2px dashed #fed7aa;border-radius:10px;padding:22px;text-align:center;margin:24px 0;\">\n"
                + "  <div style=\"font-size:11px;font-weight:700;color:#c2410c;text-transform:uppercase;letter-spacing:1.5px;margin-bottom:8px;\">Reset Code</div>\n"
                + "  <div style=\"font-family:'Courier New',Courier,monospace;font-size:34px;font-weight:800;letter-spacing:8px;color:#ea580c;\">" + otp + "</div>\n"
                + "  <div style=\"font-size:12px;color:#9a3412;margin-top:8px;\">Expires in 10 minutes</div>\n"
                + "</div>\n"
                + "<p style=\"margin:20px 0 0;font-size:13px;color:#64748b;line-height:1.5;\">\n"
                + "  If you did not request this password reset, no action is needed. Your account remains completely secure.\n"
                + "</p>"
        );

        sendHtmlEmail(toEmail, subject, plainText, htmlContent);
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
        String subject = "🎉 You're In! Course Access Confirmed for " + courseTitle;
        String plainText = "Dear " + studentName + ",\n\n"
                + "Great news! We have successfully received and verified your payment via ABA PayWay.\n"
                + "Your enrollment for \"" + courseTitle + "\" has been approved, and your access is now fully active!\n\n"
                + "Details:\n"
                + "• Course: " + courseTitle + "\n"
                + "• Instructor: " + instructorName + "\n"
                + "• Amount Paid: $" + amount + " USD\n"
                + "• Method: ABA PayWay\n"
                + "• Transaction Reference: " + transactionRef + "\n"
                + "• Date: " + enrollmentDate + "\n\n"
                + "Start learning: " + courseUrl + "\n\n"
                + "Happy learning!\nThe Lumen LMS Team";

        String htmlContent = buildHtmlLayout(
                "Enrollment Confirmed",
                "<h2 style=\"margin:0 0 10px;font-size:18px;color:#0f172a;\">Enrollment Confirmed 🎉</h2>\n"
                + "<p style=\"margin:0 0 18px;font-size:14px;color:#475569;line-height:1.6;\">\n"
                + "  Dear <strong>" + studentName + "</strong>,<br>\n"
                + "  Your payment via <strong>ABA PayWay</strong> has been verified. You now have full lifetime access to <strong>" + courseTitle + "</strong>.\n"
                + "</p>\n"
                + "<table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;margin:20px 0;background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;overflow:hidden;\">\n"
                + "  <tr style=\"border-bottom:1px solid #e2e8f0;\">\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Course:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;font-weight:600;color:#0f172a;\">" + courseTitle + "</td>\n"
                + "  </tr>\n"
                + "  <tr style=\"border-bottom:1px solid #e2e8f0;\">\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Instructor:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#0f172a;\">" + instructorName + "</td>\n"
                + "  </tr>\n"
                + "  <tr style=\"border-bottom:1px solid #e2e8f0;\">\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Amount Paid:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;font-weight:700;color:#059669;\">$" + amount + " USD</td>\n"
                + "  </tr>\n"
                + "  <tr style=\"border-bottom:1px solid #e2e8f0;\">\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Payment Method:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#0f172a;\">ABA PayWay (KHQR / Card)</td>\n"
                + "  </tr>\n"
                + "  <tr style=\"border-bottom:1px solid #e2e8f0;\">\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Reference:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;font-family:monospace;color:#0f172a;\">" + transactionRef + "</td>\n"
                + "  </tr>\n"
                + "  <tr>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#64748b;\">Enrollment Date:</td>\n"
                + "    <td style=\"padding:10px 16px;font-size:13px;color:#0f172a;\">" + enrollmentDate + "</td>\n"
                + "  </tr>\n"
                + "</table>\n"
                + "<div style=\"text-align:center;margin:28px 0 16px;\">\n"
                + "  <a href=\"" + courseUrl + "\" style=\"display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;font-size:14px;font-weight:600;padding:12px 28px;border-radius:8px;box-shadow:0 2px 4px rgba(79,70,229,0.2);\">Go to Course</a>\n"
                + "</div>"
        );

        sendHtmlEmail(toEmail, subject, plainText, htmlContent);
    }
}
