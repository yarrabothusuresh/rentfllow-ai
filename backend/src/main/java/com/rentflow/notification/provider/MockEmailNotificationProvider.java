package com.rentflow.notification.provider;

import com.rentflow.notification.dto.NotificationMessage;
import com.rentflow.notification.dto.NotificationResult;
import com.rentflow.notification.model.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockEmailNotificationProvider implements EmailNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockEmailNotificationProvider.class);

    @Override
    public NotificationResult send(NotificationMessage message) {
        String recipient = message.getRecipientEmail() != null ? message.getRecipientEmail() : "customer@example.com";

        // Demo test trigger for failure handling: email containing 'fail' or 'invalid' simulates provider failure
        if (recipient.contains("fail") || recipient.contains("invalid")) {
            log.error("MOCK EMAIL FAILED | To: {} | Subject: {}", recipient, message.getTitle());
            return NotificationResult.failure("Mock email server unavailable or recipient email invalid.");
        }

        log.info("==========================================");
        log.info("MOCK EMAIL SENT");
        log.info("To: {}", recipient);
        log.info("Subject: {}", message.getTitle());
        log.info("Body:\n{}", message.getMessage());
        log.info("==========================================");

        String extId = "email-" + UUID.randomUUID().toString();
        return NotificationResult.success(NotificationStatus.SENT, extId);
    }
}
