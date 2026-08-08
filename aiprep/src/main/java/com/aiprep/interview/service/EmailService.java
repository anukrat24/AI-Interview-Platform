package com.aiprep.interview.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void logConfigOnStartup() {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            log.debug("EmailService init: SENDGRID_API_KEY is NULL/BLANK - emails will not be sent");
        } else {
            log.debug("EmailService init: SENDGRID_API_KEY is loaded (length={})", sendGridApiKey.length());
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        log.debug("sendOtpEmail() invoked for {} on thread {}", toEmail, Thread.currentThread().getName());
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
        log.debug("send() reached for toEmail={}, subject={}", toEmail, subject);

        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            // Don't let email failures break the request flow (e.g. registration
            // should still succeed even if SendGrid creds aren't configured yet in dev).
            log.debug("send(): SENDGRID_API_KEY is NULL/BLANK - aborting send to {}", toEmail);
            log.error("Failed to send email to {}: SENDGRID_API_KEY is not configured", toEmail);
            return;
        }

        log.debug("send(): SENDGRID_API_KEY present (length={}), proceeding to build SendGrid client for {}",
                sendGridApiKey.length(), toEmail);

        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", text);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            log.debug("send(): SendGrid client initialized successfully for {}", toEmail);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            log.debug("send(): dispatching SendGrid request to mail/send for {}", toEmail);
            Response response = sg.api(request);
            log.debug("send(): SendGrid response received for {} - status={}", toEmail, response.getStatusCode());
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
