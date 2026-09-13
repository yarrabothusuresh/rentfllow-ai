package com.rentflow.notification.controller;

import com.rentflow.notification.dto.NotificationDTO;
import com.rentflow.notification.dto.NotificationPreferenceDTO;
import com.rentflow.notification.dto.NotificationUnreadCountDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationStatus;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationPreferenceService;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    public CustomerPortalNotificationController(
            NotificationService notificationService,
            NotificationPreferenceService preferenceService,
            CurrentUserService currentUserService) {
        this.notificationService = notificationService;
        this.preferenceService = preferenceService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId() {
        return currentUserService.requireTenantId();
    }

    private UUID resolveCustomerId() {
        return currentUserService.requireCustomerId();
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<NotificationDTO>> getCustomerNotifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationDTO> result = notificationService.getNotificationsForCustomer(
                resolveTenantId(), resolveCustomerId(), type, channel, status, unreadOnly, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<NotificationUnreadCountDTO> getCustomerUnreadCount() {
        return ResponseEntity.ok(notificationService.getCustomerUnreadCount(resolveTenantId(), resolveCustomerId()));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, resolveTenantId(), null, resolveCustomerId()));
    }

    @PatchMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(resolveTenantId(), null, resolveCustomerId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getCustomerPreferences() {
        return ResponseEntity.ok(preferenceService.getCustomerPreferences(resolveTenantId(), resolveCustomerId()));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> updateCustomerPreferences(
            @RequestBody List<NotificationPreferenceDTO> dtos) {
        return ResponseEntity.ok(preferenceService.updateCustomerPreferences(resolveTenantId(), resolveCustomerId(), dtos));
    }
}
