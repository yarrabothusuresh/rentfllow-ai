package com.rentflow.notification.controller;

import com.rentflow.notification.dto.NotificationDTO;
import com.rentflow.notification.dto.NotificationPreferenceDTO;
import com.rentflow.notification.dto.NotificationUnreadCountDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationStatus;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationPreferenceService;
import com.rentflow.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal")
public class CustomerPortalNotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;

    public CustomerPortalNotificationController(
            NotificationService notificationService,
            NotificationPreferenceService preferenceService) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationDTO>> getCustomerNotifications(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationDTO> result = notificationService.getNotificationsForCustomer(
                tenantId, customerId, type, channel, status, unreadOnly, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<NotificationUnreadCountDTO> getCustomerUnreadCount(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(notificationService.getCustomerUnreadCount(tenantId, customerId));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, tenantId, null, customerId));
    }

    @PatchMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId) {
        notificationService.markAllAsRead(tenantId, null, customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getCustomerPreferences(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(preferenceService.getCustomerPreferences(tenantId, customerId));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> updateCustomerPreferences(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Customer-Id") UUID customerId,
            @RequestBody List<NotificationPreferenceDTO> dtos) {
        return ResponseEntity.ok(preferenceService.updateCustomerPreferences(tenantId, customerId, dtos));
    }
}
