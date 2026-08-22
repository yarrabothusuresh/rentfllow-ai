package com.rentflow.notification.repository;

import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationTemplate;
import com.rentflow.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByIdAndTenantId(UUID id, String tenantId);

    Optional<NotificationTemplate> findByTenantIdAndNotificationTypeAndChannelAndActiveTrue(
            String tenantId, NotificationType notificationType, NotificationChannel channel);

    List<NotificationTemplate> findByTenantId(String tenantId);
}
