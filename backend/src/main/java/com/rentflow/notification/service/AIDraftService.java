package com.rentflow.notification.service;

import com.rentflow.notification.dto.AIDraftDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;

import java.util.Map;

public interface AIDraftService {
    AIDraftDTO draftMessage(String tenantId, NotificationType notificationType, NotificationChannel channel, Map<String, Object> context);
}
