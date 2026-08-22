package com.rentflow.notification.service;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.notification.dto.*;
import com.rentflow.notification.model.*;
import com.rentflow.notification.provider.NotificationChannelProvider;
import com.rentflow.notification.repository.NotificationAuditRepository;
import com.rentflow.notification.repository.NotificationRepository;
import com.rentflow.notification.repository.NotificationTemplateRepository;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationAuditRepository auditRepository;
    private final NotificationPreferenceService preferenceService;
    private final SafeTemplateRenderer templateRenderer;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final Map<NotificationChannel, NotificationChannelProvider> channelProviders;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationTemplateRepository templateRepository,
            NotificationAuditRepository auditRepository,
            NotificationPreferenceService preferenceService,
            SafeTemplateRenderer templateRenderer,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            List<NotificationChannelProvider> providers) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.auditRepository = auditRepository;
        this.preferenceService = preferenceService;
        this.templateRenderer = templateRenderer;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;

        this.channelProviders = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelProvider provider : providers) {
            this.channelProviders.put(provider.getChannel(), provider);
        }
    }

    @Transactional
    public Optional<NotificationDTO> sendNotification(NotificationRequestDTO request) {
        if (request.getTenantId() == null || request.getTenantId().trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId is required for notifications");
        }
        if (request.getRecipientUserId() == null && request.getRecipientCustomerId() == null) {
            throw new IllegalArgumentException("Notification recipient must be specified");
        }

        NotificationChannel channel = request.getChannel() != null ? request.getChannel() : NotificationChannel.IN_APP;
        NotificationType type = request.getType() != null ? request.getType() : NotificationType.SYSTEM;
        NotificationPriority priority = request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL;

        // 1. Preference Validation
        boolean enabled = preferenceService.isChannelEnabled(
                request.getTenantId(), request.getRecipientCustomerId(), request.getRecipientUserId(), type, channel.name());
        if (!enabled) {
            recordAudit(request.getTenantId(), null, "NOTIFICATION_SKIPPED_PREFERENCE", "System",
                    "Channel " + channel + " is disabled in preferences for type " + type);
            return Optional.empty();
        }

        // 2. Idempotency Check
        if (request.getReferenceType() != null && request.getReferenceId() != null) {
            List<Notification> existing = notificationRepository.findByTenantIdAndReferenceTypeAndReferenceIdAndTypeAndChannel(
                    request.getTenantId(), request.getReferenceType(), request.getReferenceId(), type, channel);
            if (!existing.isEmpty()) {
                Notification first = existing.get(0);
                recordAudit(request.getTenantId(), first.getId(), "NOTIFICATION_DUPLICATE_PREVENTED", "System",
                        "Prevented duplicate notification creation for " + request.getReferenceType() + ":" + request.getReferenceId());
                return Optional.of(mapToDTO(first));
            }
        }

        // 3. Resolve Template & Render Content
        String title = request.getCustomTitle();
        String messageText = request.getCustomMessage();

        if (title == null || messageText == null) {
            Optional<NotificationTemplate> optTemplate = templateRepository.findByTenantIdAndNotificationTypeAndChannelAndActiveTrue(
                    request.getTenantId(), type, channel);
            if (optTemplate.isPresent()) {
                NotificationTemplate template = optTemplate.get();
                if (title == null) title = templateRenderer.render(template.getSubject(), request.getTemplateVariables());
                if (messageText == null) messageText = templateRenderer.render(template.getBody(), request.getTemplateVariables());
            } else {
                if (title == null) title = defaultTitleForType(type);
                if (messageText == null) messageText = defaultMessageForType(type, request.getTemplateVariables());
            }
        }

        // 4. Create Notification Record
        Notification notification = new Notification(
                request.getTenantId(),
                request.getRecipientUserId(),
                request.getRecipientCustomerId(),
                type,
                channel,
                title,
                messageText,
                request.getReferenceType(),
                request.getReferenceId(),
                priority
        );
        notification = notificationRepository.save(notification);
        recordAudit(request.getTenantId(), notification.getId(), "NOTIFICATION_CREATED", "System", "Notification initialized with PENDING status");

        // 5. Dispatch via Channel Provider
        NotificationChannelProvider provider = channelProviders.get(channel);
        if (provider != null) {
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            NotificationMessage msg = new NotificationMessage(
                    request.getTenantId(),
                    request.getRecipientUserId(),
                    request.getRecipientCustomerId(),
                    request.getRecipientEmail(),
                    request.getRecipientPhone(),
                    type,
                    channel,
                    title,
                    messageText,
                    request.getReferenceType(),
                    request.getReferenceId(),
                    priority
            );

            NotificationResult result = provider.send(msg);
            if (result.isSuccess()) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                recordAudit(request.getTenantId(), notification.getId(), "NOTIFICATION_SENT", "System", "Delivered via " + channel);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailedAt(LocalDateTime.now());
                notification.setRetryCount(1);
                notification.setFailureReason(result.getFailureReason());
                notificationRepository.save(notification);
                recordAudit(request.getTenantId(), notification.getId(), "NOTIFICATION_FAILED", "System", "Failed: " + result.getFailureReason());
            }
        } else {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return Optional.of(mapToDTO(notification));
    }

    @Transactional
    public NotificationDTO retryNotification(UUID id, String tenantId) {
        Notification notification = notificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or unauthorized"));

        if (notification.getRetryCount() >= 3) {
            throw new IllegalStateException("Maximum retry limit (3) reached for notification " + id);
        }

        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);

        NotificationChannelProvider provider = channelProviders.get(notification.getChannel());
        if (provider != null) {
            NotificationMessage msg = new NotificationMessage(
                    notification.getTenantId(),
                    notification.getRecipientUserId(),
                    notification.getRecipientCustomerId(),
                    null,
                    null,
                    notification.getType(),
                    notification.getChannel(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getReferenceType(),
                    notification.getReferenceId(),
                    notification.getPriority()
            );

            NotificationResult result = provider.send(msg);
            if (result.isSuccess()) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setFailureReason(null);
                recordAudit(tenantId, id, "NOTIFICATION_RETRIED_SUCCESS", "Staff", "Retry attempt " + notification.getRetryCount() + " succeeded");
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailedAt(LocalDateTime.now());
                notification.setFailureReason(result.getFailureReason());
                recordAudit(tenantId, id, "NOTIFICATION_RETRIED_FAILED", "Staff", "Retry attempt " + notification.getRetryCount() + " failed: " + result.getFailureReason());
            }
        }
        return mapToDTO(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsForStaff(String tenantId, UUID userId, NotificationType type, NotificationChannel channel, NotificationStatus status, boolean unreadOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.searchNotifications(tenantId, userId, null, type, channel, status, unreadOnly, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsForCustomer(String tenantId, UUID customerId, NotificationType type, NotificationChannel channel, NotificationStatus status, boolean unreadOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.searchNotifications(tenantId, null, customerId, type, channel, status, unreadOnly, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(UUID id, String tenantId) {
        Notification notification = notificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or unauthorized"));
        return mapToDTO(notification);
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountDTO getStaffUnreadCount(String tenantId, UUID userId) {
        long count = notificationRepository.countByTenantIdAndRecipientUserIdAndReadAtIsNull(tenantId, userId);
        return new NotificationUnreadCountDTO(count);
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountDTO getCustomerUnreadCount(String tenantId, UUID customerId) {
        long count = notificationRepository.countByTenantIdAndRecipientCustomerIdAndReadAtIsNull(tenantId, customerId);
        return new NotificationUnreadCountDTO(count);
    }

    @Transactional
    public NotificationDTO markAsRead(UUID id, String tenantId, UUID userId, UUID customerId) {
        Notification notification = notificationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or unauthorized"));

        if (customerId != null && !customerId.equals(notification.getRecipientCustomerId())) {
            throw new SecurityException("Access denied: notification belongs to another customer");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
            notification = notificationRepository.save(notification);
            recordAudit(tenantId, id, "NOTIFICATION_READ", customerId != null ? "Customer" : "User", "Marked notification as read");
        }
        return mapToDTO(notification);
    }

    @Transactional
    public void markAllAsRead(String tenantId, UUID userId, UUID customerId) {
        List<Notification> notifications;
        if (customerId != null) {
            notifications = notificationRepository.findByTenantIdAndRecipientCustomerIdOrderByCreatedAtDesc(tenantId, customerId, PageRequest.of(0, 100));
        } else {
            notifications = notificationRepository.findByTenantIdAndRecipientUserIdOrderByCreatedAtDesc(tenantId, userId, PageRequest.of(0, 100));
        }

        LocalDateTime now = LocalDateTime.now();
        for (Notification n : notifications) {
            if (n.getReadAt() == null) {
                n.setReadAt(now);
                n.setStatus(NotificationStatus.READ);
                notificationRepository.save(n);
            }
        }
        recordAudit(tenantId, null, "NOTIFICATION_READ_ALL", customerId != null ? "Customer" : "User", "Marked all unread notifications as read");
    }

    private void recordAudit(String tenantId, UUID notificationId, String action, String performedBy, String details) {
        NotificationAudit audit = new NotificationAudit(tenantId, notificationId, action, performedBy, details);
        auditRepository.save(audit);
    }

    public NotificationDTO mapToDTO(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setRecipientUserId(entity.getRecipientUserId());
        dto.setRecipientCustomerId(entity.getRecipientCustomerId());

        if (entity.getRecipientUserId() != null) {
            Optional<User> u = userRepository.findById(entity.getRecipientUserId());
            u.ifPresent(user -> dto.setRecipientName(user.getName()));
        } else if (entity.getRecipientCustomerId() != null) {
            Optional<Customer> c = customerRepository.findById(entity.getRecipientCustomerId());
            c.ifPresent(customer -> dto.setRecipientName(customer.getCompanyName() != null && !customer.getCompanyName().isBlank() ? customer.getCompanyName() : customer.getFirstName()));
        }

        dto.setType(entity.getType());
        dto.setChannel(entity.getChannel());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setReferenceType(entity.getReferenceType());
        dto.setReferenceId(entity.getReferenceId());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        dto.setReadAt(entity.getReadAt());
        dto.setSentAt(entity.getSentAt());
        dto.setFailedAt(entity.getFailedAt());
        dto.setRetryCount(entity.getRetryCount());
        dto.setFailureReason(entity.getFailureReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private String defaultTitleForType(NotificationType type) {
        switch (type) {
            case QUOTE_SENT: return "Your Rental Quote is Ready";
            case QUOTE_ACCEPTED: return "Quote Accepted";
            case QUOTE_CHANGE_REQUESTED: return "Quote Change Requested";
            case BOOKING_CONFIRMED: return "Booking Confirmed";
            case BOOKING_CANCELLED: return "Booking Cancelled";
            case PAYMENT_RECEIVED: return "Payment Received";
            case PAYMENT_DUE: return "Payment Due Notice";
            case PAYMENT_FAILED: return "Payment Failed Alert";
            case INVOICE_CREATED: return "New Invoice Created";
            case INVOICE_SENT: return "Invoice Available for Review";
            case INVOICE_OVERDUE: return "Invoice Overdue Notice";
            case CUSTOMER_REQUEST_CREATED: return "New Support Request";
            default: return "RentFlow AI Notification";
        }
    }

    private String defaultMessageForType(NotificationType type, Map<String, Object> vars) {
        String customerName = vars != null && vars.get("customerName") != null ? String.valueOf(vars.get("customerName")) : "Valued Client";
        String refNum = vars != null && vars.get("quoteNumber") != null ? String.valueOf(vars.get("quoteNumber")) :
                        vars != null && vars.get("bookingNumber") != null ? String.valueOf(vars.get("bookingNumber")) :
                        vars != null && vars.get("invoiceNumber") != null ? String.valueOf(vars.get("invoiceNumber")) : "your record";
        String amount = vars != null && vars.get("totalAmount") != null ? String.valueOf(vars.get("totalAmount")) :
                        vars != null && vars.get("paymentAmount") != null ? String.valueOf(vars.get("paymentAmount")) : "";

        return "Hello " + customerName + ", update for " + refNum + " " + amount + ". Please view your account portal for complete details.";
    }
}
