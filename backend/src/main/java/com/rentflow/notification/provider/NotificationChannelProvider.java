package com.rentflow.notification.provider;

import com.rentflow.notification.dto.NotificationMessage;
import com.rentflow.notification.dto.NotificationResult;
import com.rentflow.notification.model.NotificationChannel;

public interface NotificationChannelProvider {
    NotificationChannel getChannel();
    NotificationResult send(NotificationMessage message);
}
