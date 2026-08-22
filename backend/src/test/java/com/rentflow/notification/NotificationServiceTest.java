package com.rentflow.notification;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.notification.dto.*;
import com.rentflow.notification.model.*;
import com.rentflow.notification.service.NotificationPreferenceService;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.notification.service.NotificationTemplateService;
import com.rentflow.notification.service.SafeTemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationTemplateService templateService;

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private SafeTemplateRenderer templateRenderer;

    private String tenantId;
    private UUID testUserId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        testCustomerId = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    }

    @Test
    void test1_SendInAppNotificationSuccess() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientUserId(testUserId);
        req.setType(NotificationType.QUOTE_ACCEPTED);
        req.setChannel(NotificationChannel.IN_APP);
        req.setCustomTitle("Quote Accepted");
        req.setCustomMessage("Customer accepted quote Q-100");

        Optional<NotificationDTO> result = notificationService.sendNotification(req);
        assertTrue(result.isPresent());
        NotificationDTO dto = result.get();
        assertEquals(NotificationStatus.SENT, dto.getStatus());
        assertEquals("Quote Accepted", dto.getTitle());
    }

    @Test
    void test2_SendMockEmailNotificationSuccess() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(testCustomerId);
        req.setRecipientEmail("customer@abcevents.demo");
        req.setType(NotificationType.QUOTE_SENT);
        req.setChannel(NotificationChannel.EMAIL);
        req.setCustomTitle("Your Quote");
        req.setCustomMessage("Quote details here");

        Optional<NotificationDTO> result = notificationService.sendNotification(req);
        assertTrue(result.isPresent());
        assertEquals(NotificationStatus.SENT, result.get().getStatus());
    }

    @Test
    void test3_SendMockSmsNotificationSuccess() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(testCustomerId);
        req.setRecipientPhone("+15551234567");
        req.setType(NotificationType.BOOKING_CONFIRMED);
        req.setChannel(NotificationChannel.SMS);
        req.setCustomTitle("Booking Confirmed");
        req.setCustomMessage("Your booking BKG-001 is confirmed");

        Optional<NotificationDTO> result = notificationService.sendNotification(req);
        assertTrue(result.isPresent());
        assertEquals(NotificationStatus.SENT, result.get().getStatus());
    }

    @Test
    void test4_ChannelDisabledByPreferenceSkipped() {
        // Disable SMS for BOOKING_CONFIRMED
        NotificationPreferenceDTO pref = new NotificationPreferenceDTO();
        pref.setNotificationType(NotificationType.BOOKING_CONFIRMED);
        pref.setSmsEnabled(false);
        pref.setEmailEnabled(true);
        pref.setInAppEnabled(true);
        preferenceService.updateCustomerPreferences(tenantId, testCustomerId, List.of(pref));

        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(testCustomerId);
        req.setType(NotificationType.BOOKING_CONFIRMED);
        req.setChannel(NotificationChannel.SMS);
        req.setCustomTitle("Test SMS");
        req.setCustomMessage("Disabled message");

        Optional<NotificationDTO> result = notificationService.sendNotification(req);
        assertTrue(result.isEmpty()); // Skipped due to preference
    }

    @Test
    void test5_IdempotencyPreventsDuplicateNotifications() {
        String refId = UUID.randomUUID().toString();

        NotificationRequestDTO req1 = new NotificationRequestDTO();
        req1.setTenantId(tenantId);
        req1.setRecipientUserId(testUserId);
        req1.setType(NotificationType.PAYMENT_RECEIVED);
        req1.setChannel(NotificationChannel.IN_APP);
        req1.setReferenceType("PAYMENT");
        req1.setReferenceId(refId);
        req1.setCustomTitle("Payment 1");
        req1.setCustomMessage("First payment message");

        Optional<NotificationDTO> first = notificationService.sendNotification(req1);
        assertTrue(first.isPresent());

        // Duplicate call with same refId
        Optional<NotificationDTO> second = notificationService.sendNotification(req1);
        assertTrue(second.isPresent());
        assertEquals(first.get().getId(), second.get().getId());
    }

    @Test
    void test6_RetryFailedNotification() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(testCustomerId);
        req.setRecipientEmail("invalid-fail@example.com"); // Triggers mock failure
        req.setType(NotificationType.INVOICE_SENT);
        req.setChannel(NotificationChannel.EMAIL);
        req.setCustomTitle("Invoice Sent");
        req.setCustomMessage("Invoice details");

        Optional<NotificationDTO> opt = notificationService.sendNotification(req);
        assertTrue(opt.isPresent());
        NotificationDTO failedNotif = opt.get();
        assertEquals(NotificationStatus.FAILED, failedNotif.getStatus());
        assertEquals(1, failedNotif.getRetryCount());

        // Retry 1
        NotificationDTO retried = notificationService.retryNotification(failedNotif.getId(), tenantId);
        assertEquals(2, retried.getRetryCount());
    }

    @Test
    void test7_MaxRetryEnforced() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientCustomerId(testCustomerId);
        req.setRecipientEmail("invalid-fail@example.com");
        req.setType(NotificationType.INVOICE_SENT);
        req.setChannel(NotificationChannel.EMAIL);
        req.setCustomTitle("Invoice Sent");
        req.setCustomMessage("Invoice details");

        NotificationDTO notif = notificationService.sendNotification(req).get();
        notificationService.retryNotification(notif.getId(), tenantId); // retry 2
        notificationService.retryNotification(notif.getId(), tenantId); // retry 3

        // 4th retry should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> notificationService.retryNotification(notif.getId(), tenantId));
    }

    @Test
    void test8_SafeTemplateRenderingAndSanitization() {
        String template = "Hello {{customerName}}, your quote {{quoteNumber}} is <script>alert(1)</script> ready!";
        Map<String, Object> vars = Map.of("customerName", "ABC Events", "quoteNumber", "Q-999");

        String rendered = templateRenderer.render(template, vars);
        assertEquals("Hello ABC Events, your quote Q-999 is alert(1) ready!", rendered);
        assertFalse(rendered.contains("<script>"));
    }

    @Test
    void test9_MarkAsReadAndUnreadCount() {
        NotificationRequestDTO req = new NotificationRequestDTO();
        req.setTenantId(tenantId);
        req.setRecipientUserId(testUserId);
        req.setType(NotificationType.SYSTEM);
        req.setChannel(NotificationChannel.IN_APP);
        req.setCustomTitle("System Alert");
        req.setCustomMessage("System update notification");

        NotificationDTO created = notificationService.sendNotification(req).get();
        assertNull(created.getReadAt());

        NotificationDTO readNotif = notificationService.markAsRead(created.getId(), tenantId, testUserId, null);
        assertNotNull(readNotif.getReadAt());
        assertEquals(NotificationStatus.READ, readNotif.getStatus());
    }
}
