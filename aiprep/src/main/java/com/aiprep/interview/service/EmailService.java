package com.aiprep.interview.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:no-reply@aiprep.dev}")
    private String fromEmail;

    @Value("${sendgrid.from-name:AI Interview Platform}")
    private String fromName;

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
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            // Don't let email failures break the request flow (e.g. registration
            // should still succeed even if SendGrid creds aren't configured yet in dev).
            log.error("Failed to send email to {}: SENDGRID_API_KEY is not configured", toEmail);
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", text);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 300) {
                log.error("Failed to send email to {}: SendGrid returned status {} - {}",
                        toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
