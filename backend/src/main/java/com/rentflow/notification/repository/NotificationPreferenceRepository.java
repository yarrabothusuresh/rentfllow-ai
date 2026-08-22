package com.rentflow.notification.repository;

import com.rentflow.notification.model.NotificationPreference;
import com.rentflow.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByTenantIdAndCustomerIdAndNotificationType(
            String tenantId, UUID customerId, NotificationType notificationType);

    Optional<NotificationPreference> findByTenantIdAndUserIdAndNotificationType(
            String tenantId, UUID userId, NotificationType notificationType);

    List<NotificationPreference> findByTenantIdAndCustomerId(String tenantId, UUID customerId);

    List<NotificationPreference> findByTenantIdAndUserId(String tenantId, UUID userId);
}
