package com.airesume.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.frontend.reset-password-url}")
    private String resetUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = resetUrl + "?token=" + token;

        if (mailUsername == null || mailUsername.isBlank()) {
            // Dev mode: no SMTP configured. Log the link so flows remain testable.
            log.warn("[DEV] SMTP not configured. Password reset link for {}: {}", to, link);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Reset your AI Resume Analyzer password");
            message.setText("""
                    You requested a password reset.

                    Click the link below to set a new password (valid for 30 minutes):
                    %s

                    If you did not request this, you can safely ignore this email.
                    """.formatted(link));
            message.setFrom(mailUsername);
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send reset email to {}: {}", to, e.getMessage());
        }
    }
}
