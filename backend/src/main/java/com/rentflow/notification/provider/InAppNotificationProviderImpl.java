package com.rentflow.notification.provider;

import com.rentflow.notification.dto.NotificationMessage;
import com.rentflow.notification.dto.NotificationResult;
import com.rentflow.notification.model.NotificationStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InAppNotificationProviderImpl implements InAppNotificationProvider {

    @Override
    public NotificationResult send(NotificationMessage message) {
        // In-App notifications are persisted in DB with status SENT so the UI bell icon can render them
        String extId = "inapp-" + UUID.randomUUID().toString();
        return NotificationResult.success(NotificationStatus.SENT, extId);
    }
}
