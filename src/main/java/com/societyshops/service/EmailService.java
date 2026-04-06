package com.societyshops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${spring.mail.from:noreply@localhost}")
    private String fromEmail;

    @Async
    public void sendPasswordReset(String name, String email, String token) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email);
            msg.setSubject("Reset Your Password — Society Shops");
            msg.setText(
                "Hi " + name + ",\n\n" +
                "Click the link below to reset your password (valid for 30 minutes):\n\n" +
                "http://localhost:8080/web/reset-password?token=" + token + "\n\n" +
                "If you didn't request this, ignore this email."
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }

    @Async
    public void sendShopkeeperRegistration(String name, String email) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(adminEmail);
            msg.setSubject("New Shopkeeper Registered: " + name);
            msg.setText(
                "A new shopkeeper has registered and is waiting for shop approval.\n\n" +
                "Name  : " + name + "\n" +
                "Email : " + email + "\n\n" +
                "They will register their shop next. Approve from the admin dashboard:\n" +
                "http://localhost:8080/web/login"
            );
            mailSender.send(msg);
            log.info("Shopkeeper registration email sent to admin for: {}", email);
        } catch (Exception e) {
            log.error("Failed to send shopkeeper registration email: {}", e.getMessage());
        }
    }

    @Async
    public void sendShopApprovalRequest(String shopName, String ownerName, String ownerEmail, Long shopId) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(adminEmail);
            msg.setSubject("New Shop Registration: " + shopName);
            msg.setText(
                "A new shop registration is pending your approval.\n\n" +
                "Shop Name : " + shopName + "\n" +
                "Owner     : " + ownerName + "\n" +
                "Email     : " + ownerEmail + "\n\n" +
                "Login to approve or reject:\n" +
                "http://localhost:8080/web/login"
            );
            mailSender.send(msg);
            log.info("Approval email sent to admin for shop: {}", shopName);
        } catch (Exception e) {
            log.error("Failed to send approval email: {}", e.getMessage());
        }
    }
}
