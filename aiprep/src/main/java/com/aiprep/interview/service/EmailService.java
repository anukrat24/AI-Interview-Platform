package com.aiprep.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        send(toEmail, "Your verification code",
                "Your one-time verification code is: " + otp + "\n\nThis code expires in 10 minutes.");
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        send(toEmail, "Reset your password",
                "Click the link below to reset your password:\n\n" + resetLink
                        + "\n\nIf you didn't request this, you can ignore this email.");
    }

    @Async
    public void sendSubscriptionConfirmationEmail(String toEmail) {
        send(toEmail, "You're now Premium!",
                "Thanks for upgrading. You now have unlimited AI mock interviews and full access to every feature.");
    }

    private void send(String toEmail, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let email failures break the request flow (e.g. registration
            // should still succeed even if SMTP creds aren't configured yet in dev).
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
