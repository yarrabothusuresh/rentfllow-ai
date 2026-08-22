package com.rentflow.notification.repository;

import com.rentflow.notification.model.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, UUID> {
    List<NotificationAudit> findByTenantIdAndNotificationIdOrderByTimestampDesc(String tenantId, UUID notificationId);
}
