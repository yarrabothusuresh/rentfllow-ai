package com.rentflow.notification;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.notification.dto.NotificationDTO;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CustomerPortalNotificationTest {

    @Autowired
    private NotificationService notificationService;

    private String tenantId;
    private UUID customerAId;
    private UUID customerBId;

    @BeforeEach
    void setUp() {
        tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        customerAId = UUID.fromString("c1000000-0000-0000-0000-000000000001");
        customerBId = UUID.fromString("c2000000-0000-0000-0000-000000000002");
    }

    @Test
    void test1_CustomerReceivesOnlyOwnNotifications() {
        // Customer A Notification
        NotificationRequestDTO reqA = new NotificationRequestDTO();
        reqA.setTenantId(tenantId);
        reqA.setRecipientCustomerId(customerAId);
        reqA.setType(NotificationType.QUOTE_SENT);
        reqA.setChannel(NotificationChannel.IN_APP);
        reqA.setCustomTitle("Quote for Customer A");
        reqA.setCustomMessage("Message A");
        notificationService.sendNotification(reqA);

        // Customer B Notification
        NotificationRequestDTO reqB = new NotificationRequestDTO();
        reqB.setTenantId(tenantId);
        reqB.setRecipientCustomerId(customerBId);
        reqB.setType(NotificationType.PAYMENT_RECEIVED);
        reqB.setChannel(NotificationChannel.IN_APP);
        reqB.setCustomTitle("Payment for Customer B");
        reqB.setCustomMessage("Message B");
        notificationService.sendNotification(reqB);

        // Query Customer A Notifications
        Page<NotificationDTO> pageA = notificationService.getNotificationsForCustomer(
                tenantId, customerAId, null, null, null, false, 0, 10);
        assertTrue(pageA.getContent().stream().allMatch(n -> customerAId.equals(n.getRecipientCustomerId())));
        assertTrue(pageA.getContent().stream().noneMatch(n -> customerBId.equals(n.getRecipientCustomerId())));
    }

    @Test
    void test2_CrossCustomerReadAccessRejected() {
        NotificationRequestDTO reqA = new NotificationRequestDTO();
        reqA.setTenantId(tenantId);
        reqA.setRecipientCustomerId(customerAId);
        reqA.setType(NotificationType.INVOICE_SENT);
        reqA.setChannel(NotificationChannel.IN_APP);
        reqA.setCustomTitle("Invoice A");
        reqA.setCustomMessage("Invoice for A");

        NotificationDTO notifA = notificationService.sendNotification(reqA).get();

        // Customer B attempts to mark Customer A's notification as read
        assertThrows(SecurityException.class, () ->
                notificationService.markAsRead(notifA.getId(), tenantId, null, customerBId));
    }
}
