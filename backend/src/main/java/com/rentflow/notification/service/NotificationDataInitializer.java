package com.rentflow.notification.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.notification.model.*;
import com.rentflow.notification.repository.NotificationRepository;
import com.rentflow.notification.repository.NotificationTemplateRepository;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationDataInitializer implements CommandLineRunner {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public NotificationDataInitializer(
            NotificationTemplateRepository templateRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository) {
        this.templateRepository = templateRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (templateRepository.count() > 0 && notificationRepository.count() > 0) {
            return;
        }

        // 1. Seed Templates
        if (templateRepository.count() == 0) {
            NotificationTemplate t1 = new NotificationTemplate(
                    tenantId,
                    "QUOTE_SENT_EMAIL",
                    NotificationType.QUOTE_SENT,
                    NotificationChannel.EMAIL,
                    "Your rental quote {{quoteNumber}} is ready",
                    "Hello {{customerName}},\n\nYour quote {{quoteNumber}} for {{eventName}} is ready for review.\nTotal Amount: {{totalAmount}}\n\nPlease log in to review and accept your quote online.\n\nThank you,\n{{companyName}}"
            );

            NotificationTemplate t2 = new NotificationTemplate(
                    tenantId,
                    "BOOKING_CONFIRMED_EMAIL",
                    NotificationType.BOOKING_CONFIRMED,
                    NotificationChannel.EMAIL,
                    "Booking Confirmed: {{bookingNumber}}",
                    "Hello {{customerName}},\n\nYour rental booking {{bookingNumber}} for {{eventName}} on {{eventDate}} has been confirmed.\n\nThank you for choosing {{companyName}}!"
            );

            NotificationTemplate t3 = new NotificationTemplate(
                    tenantId,
                    "PAYMENT_RECEIVED_EMAIL",
                    NotificationType.PAYMENT_RECEIVED,
                    NotificationChannel.EMAIL,
                    "Payment Received for {{bookingNumber}}",
                    "Hello {{customerName}},\n\nPayment of {{paymentAmount}} has been received for booking {{bookingNumber}}.\nRemaining Balance: {{balanceDue}}.\n\nThank you,\n{{companyName}}"
            );

            NotificationTemplate t4 = new NotificationTemplate(
                    tenantId,
                    "INVOICE_SENT_EMAIL",
                    NotificationType.INVOICE_SENT,
                    NotificationChannel.EMAIL,
                    "Invoice {{invoiceNumber}} from {{companyName}}",
                    "Hello {{customerName}},\n\nInvoice {{invoiceNumber}} totaling {{totalAmount}} is ready. Balance due: {{balanceDue}}.\n\nPlease review and pay in your customer portal."
            );

            templateRepository.saveAll(List.of(t1, t2, t3, t4));
        }

        // 2. Seed Realistic Notifications
        if (notificationRepository.count() == 0) {
            List<User> users = userRepository.findByTenant_Id(UUID.fromString(tenantId));
            UUID userId = users.isEmpty() ? null : users.get(0).getId();

            List<Customer> customers = customerRepository.findByTenantId(tenantId);
            UUID customerId = customers.isEmpty() ? null : customers.get(0).getId();

            // Staff Notifications
            if (userId != null) {
                Notification n1 = new Notification(
                        tenantId, userId, null,
                        NotificationType.QUOTE_ACCEPTED, NotificationChannel.IN_APP,
                        "ABC Events accepted Quote QUOTE-000123",
                        "ABC Events LLC accepted quote QUOTE-000123 for $2,500.00.",
                        "QUOTE", "q0000000-0000-0000-0000-000000000001", NotificationPriority.HIGH
                );
                n1.setStatus(NotificationStatus.SENT);

                Notification n2 = new Notification(
                        tenantId, userId, null,
                        NotificationType.PAYMENT_RECEIVED, NotificationChannel.IN_APP,
                        "Payment of $500 received",
                        "Payment of $500.00 received from ABC Events LLC for booking BKG-000002.",
                        "PAYMENT", "p0000000-0000-0000-0000-000000000001", NotificationPriority.NORMAL
                );
                n2.setStatus(NotificationStatus.SENT);

                Notification n3 = new Notification(
                        tenantId, userId, null,
                        NotificationType.CUSTOMER_REQUEST_CREATED, NotificationChannel.IN_APP,
                        "New Customer Request: Delivery timing",
                        "ABC Events LLC asked: Can delivery happen before 10 AM on event day?",
                        "CUSTOMER_REQUEST", "cr000000-0000-0000-0000-000000000001", NotificationPriority.HIGH
                );
                n3.setStatus(NotificationStatus.SENT);

                notificationRepository.saveAll(List.of(n1, n2, n3));
            }

            // Customer Notifications
            if (customerId != null) {
                Notification cn1 = new Notification(
                        tenantId, null, customerId,
                        NotificationType.QUOTE_SENT, NotificationChannel.IN_APP,
                        "Your Rental Quote Is Ready",
                        "Your rental quote for Wedding Reception is ready for review.",
                        "QUOTE", "q0000000-0000-0000-0000-000000000001", NotificationPriority.NORMAL
                );
                cn1.setStatus(NotificationStatus.SENT);

                Notification cn2 = new Notification(
                        tenantId, null, customerId,
                        NotificationType.PAYMENT_RECEIVED, NotificationChannel.IN_APP,
                        "Payment Received ($500.00)",
                        "Thank you! Payment of $500.00 was recorded for booking BKG-000002. Balance due: $2,000.00.",
                        "PAYMENT", "p0000000-0000-0000-0000-000000000001", NotificationPriority.NORMAL
                );
                cn2.setStatus(NotificationStatus.SENT);

                Notification cn3 = new Notification(
                        tenantId, null, customerId,
                        NotificationType.INVOICE_SENT, NotificationChannel.IN_APP,
                        "Invoice INV-000001 Available",
                        "Invoice INV-000001 for $2,500.00 is ready for online review.",
                        "INVOICE", "i0000000-0000-0000-0000-000000000001", NotificationPriority.HIGH
                );
                cn3.setStatus(NotificationStatus.SENT);

                notificationRepository.saveAll(List.of(cn1, cn2, cn3));
            }
        }
    }
}
