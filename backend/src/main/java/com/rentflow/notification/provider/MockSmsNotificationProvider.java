package com.rentflow.notification.provider;

import com.rentflow.notification.dto.NotificationMessage;
import com.rentflow.notification.dto.NotificationResult;
import com.rentflow.notification.model.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockSmsNotificationProvider implements SmsNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSmsNotificationProvider.class);

    @Override
    public NotificationResult send(NotificationMessage message) {
        String phone = message.getRecipientPhone() != null ? message.getRecipientPhone() : "+15550199";

        if (phone.contains("0000") || phone.contains("fail")) {
            log.error("MOCK SMS FAILED | To: {} | Title: {}", phone, message.getTitle());
            return NotificationResult.failure("Mock SMS gateway timeout or invalid phone number format.");
        }

        log.info("==========================================");
        log.info("MOCK SMS SENT");
        log.info("To: {}", phone);
        log.info("Message: {} - {}", message.getTitle(), message.getMessage());
        log.info("==========================================");

        String extId = "sms-" + UUID.randomUUID().toString();
        return NotificationResult.success(NotificationStatus.SENT, extId);
    }
}
