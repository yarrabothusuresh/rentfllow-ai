package com.rentflow.notification.controller;

import com.rentflow.notification.dto.*;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationStatus;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.AIDraftService;
import com.rentflow.notification.service.NotificationPreferenceService;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.notification.service.NotificationTemplateService;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final NotificationPreferenceService preferenceService;
    private final AIDraftService aiDraftService;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationService notificationService,
            NotificationTemplateService templateService,
            NotificationPreferenceService preferenceService,
            AIDraftService aiDraftService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
        this.aiDraftService = aiDraftService;
        this.userRepository = userRepository;
    }

    // --- STAFF NOTIFICATION ENDPOINTS ---

    @GetMapping("/api/notifications")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = resolveUserId(tenantId);
        Page<NotificationDTO> result = notificationService.getNotificationsForStaff(tenantId, userId, type, channel, status, unreadOnly, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<NotificationUnreadCountDTO> getUnreadCount(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId) {
        UUID userId = resolveUserId(tenantId);
        return ResponseEntity.ok(notificationService.getStaffUnreadCount(tenantId, userId));
    }

    @GetMapping("/api/notifications/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id, tenantId));
    }

    @PatchMapping("/api/notifications/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @PathVariable UUID id) {
        UUID userId = resolveUserId(tenantId);
        return ResponseEntity.ok(notificationService.markAsRead(id, tenantId, userId, null));
    }

    @PatchMapping("/api/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId) {
        UUID userId = resolveUserId(tenantId);
        notificationService.markAllAsRead(tenantId, userId, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/notifications/{id}/retry")
    public ResponseEntity<NotificationDTO> retryNotification(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.retryNotification(id, tenantId));
    }

    // --- STAFF PREFERENCES ---

    @GetMapping("/api/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getPreferences(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId) {
        UUID userId = resolveUserId(tenantId);
        return ResponseEntity.ok(preferenceService.getUserPreferences(tenantId, userId));
    }

    @PutMapping("/api/notification-preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> updatePreferences(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @RequestBody List<NotificationPreferenceDTO> dtos) {
        UUID userId = resolveUserId(tenantId);
        return ResponseEntity.ok(preferenceService.updateUserPreferences(tenantId, userId, dtos));
    }

    // --- TEMPLATE ADMIN ---

    @GetMapping("/api/notification-templates")
    public ResponseEntity<List<NotificationTemplateDTO>> getTemplates(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId) {
        return ResponseEntity.ok(templateService.getTemplates(tenantId));
    }

    @GetMapping("/api/notification-templates/{id}")
    public ResponseEntity<NotificationTemplateDTO> getTemplateById(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplateById(id, tenantId));
    }

    @PostMapping("/api/notification-templates")
    public ResponseEntity<NotificationTemplateDTO> createTemplate(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole,
            @RequestBody NotificationTemplateDTO dto) {
        validateAdminRole(userRole);
        return ResponseEntity.ok(templateService.createTemplate(tenantId, dto));
    }

    @PutMapping("/api/notification-templates/{id}")
    public ResponseEntity<NotificationTemplateDTO> updateTemplate(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole,
            @PathVariable UUID id,
            @RequestBody NotificationTemplateDTO dto) {
        validateAdminRole(userRole);
        return ResponseEntity.ok(templateService.updateTemplate(id, tenantId, dto));
    }

    @PostMapping("/api/notification-templates/{id}/preview")
    public ResponseEntity<Map<String, String>> previewTemplate(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> variables) {
        NotificationTemplateDTO t = templateService.getTemplateById(id, tenantId);
        Map<String, Object> sampleVars = variables != null ? variables : Map.of(
                "customerName", "ABC Events LLC",
                "quoteNumber", "QUOTE-000123",
                "bookingNumber", "BKG-000002",
                "invoiceNumber", "INV-000001",
                "totalAmount", "$2,500.00",
                "balanceDue", "$2,000.00",
                "companyName", "RentFlow AI"
        );

        String renderedSubject = templateService.renderPreview(t.getSubject(), sampleVars);
        String renderedBody = templateService.renderPreview(t.getBody(), sampleVars);

        return ResponseEntity.ok(Map.of(
                "subject", renderedSubject,
                "body", renderedBody
        ));
    }

    // --- AI DRAFTING ---

    @PostMapping("/api/notifications/ai-draft")
    public ResponseEntity<AIDraftDTO> draftMessage(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "00000000-0000-0000-0000-000000000001") String tenantId,
            @RequestBody Map<String, Object> body) {
        String typeStr = (String) body.getOrDefault("notificationType", "INVOICE_OVERDUE");
        String channelStr = (String) body.getOrDefault("channel", "EMAIL");
        NotificationType type = NotificationType.valueOf(typeStr);
        NotificationChannel channel = NotificationChannel.valueOf(channelStr);

        AIDraftDTO draft = aiDraftService.draftMessage(tenantId, type, channel, body);
        return ResponseEntity.ok(draft);
    }

    private void validateAdminRole(String userRole) {
        if (userRole == null) throw new SecurityException("Unauthorized: User role required.");
        String r = userRole.toUpperCase();
        if (!"OWNER".equals(r) && !"ADMIN".equals(r)) {
            throw new SecurityException("Unauthorized: Only Owner or Admin can manage notification templates.");
        }
    }

    private UUID resolveUserId(String tenantId) {
        try {
            List<User> users = userRepository.findByTenant_Id(UUID.fromString(tenantId));
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
        } catch (Exception ignored) {}
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
