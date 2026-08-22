package com.rentflow.notification.repository;

import com.rentflow.notification.model.Notification;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationStatus;
import com.rentflow.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndTenantId(UUID id, String tenantId);

    List<Notification> findByTenantIdAndReferenceTypeAndReferenceIdAndTypeAndChannel(
            String tenantId, String referenceType, String referenceId, NotificationType type, NotificationChannel channel);

    long countByTenantIdAndRecipientUserIdAndReadAtIsNull(String tenantId, UUID recipientUserId);
    long countByTenantIdAndRecipientCustomerIdAndReadAtIsNull(String tenantId, UUID recipientCustomerId);

    List<Notification> findByTenantIdAndRecipientUserIdOrderByCreatedAtDesc(String tenantId, UUID recipientUserId, Pageable pageable);
    List<Notification> findByTenantIdAndRecipientCustomerIdOrderByCreatedAtDesc(String tenantId, UUID recipientCustomerId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId " +
           "AND (:recipientUserId IS NULL OR n.recipientUserId = :recipientUserId) " +
           "AND (:recipientCustomerId IS NULL OR n.recipientCustomerId = :recipientCustomerId) " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:channel IS NULL OR n.channel = :channel) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:unreadOnly = false OR n.readAt IS NULL) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(
            @Param("tenantId") String tenantId,
            @Param("recipientUserId") UUID recipientUserId,
            @Param("recipientCustomerId") UUID recipientCustomerId,
            @Param("type") NotificationType type,
            @Param("channel") NotificationChannel channel,
            @Param("status") NotificationStatus status,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable);
}
