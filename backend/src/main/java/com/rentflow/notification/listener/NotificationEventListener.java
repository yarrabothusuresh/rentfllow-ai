package com.rentflow.notification.listener;

import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.event.*;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationEventListener(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleQuoteSent(QuoteSentEvent event) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", event.getCustomerName());
        vars.put("quoteNumber", event.getQuoteNumber());
        vars.put("eventName", event.getEventName() != null ? event.getEventName() : "Event");
        vars.put("totalAmount", "$" + (event.getTotalAmount() != null ? event.getTotalAmount().toString() : "0.00"));
        vars.put("companyName", "RentFlow AI");

        // Customer In-App Notification
        NotificationRequestDTO inAppReq = new NotificationRequestDTO();
        inAppReq.setTenantId(event.getTenantId());
        inAppReq.setRecipientCustomerId(event.getCustomerId());
        inAppReq.setType(NotificationType.QUOTE_SENT);
        inAppReq.setChannel(NotificationChannel.IN_APP);
        inAppReq.setPriority(NotificationPriority.NORMAL);
        inAppReq.setReferenceType("QUOTE");
        inAppReq.setReferenceId(event.getQuoteId().toString());
        inAppReq.setTemplateVariables(vars);
        notificationService.sendNotification(inAppReq);

        // Customer Email Notification
        NotificationRequestDTO emailReq = new NotificationRequestDTO();
        emailReq.setTenantId(event.getTenantId());
        emailReq.setRecipientCustomerId(event.getCustomerId());
        emailReq.setRecipientEmail(event.getCustomerEmail());
        emailReq.setType(NotificationType.QUOTE_SENT);
        emailReq.setChannel(NotificationChannel.EMAIL);
        emailReq.setPriority(NotificationPriority.NORMAL);
        emailReq.setReferenceType("QUOTE");
        emailReq.setReferenceId(event.getQuoteId().toString());
        emailReq.setTemplateVariables(vars);
        notificationService.sendNotification(emailReq);
    }

    @EventListener
    public void handleQuoteAccepted(QuoteAcceptedEvent event) {
        // Staff Notification
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", event.getCustomerName());
        vars.put("quoteNumber", event.getQuoteNumber());
        vars.put("totalAmount", "$" + (event.getTotalAmount() != null ? event.getTotalAmount().toString() : "0.00"));

        List<User> staffUsers = getStaffUsers(event.getTenantId());
        for (User staff : staffUsers) {
            NotificationRequestDTO req = new NotificationRequestDTO();
            req.setTenantId(event.getTenantId());
            req.setRecipientUserId(staff.getId());
            req.setType(NotificationType.QUOTE_ACCEPTED);
            req.setChannel(NotificationChannel.IN_APP);
            req.setPriority(NotificationPriority.HIGH);
            req.setReferenceType("QUOTE");
            req.setReferenceId(event.getQuoteId().toString());
            req.setCustomTitle("Quote " + event.getQuoteNumber() + " Accepted");
            req.setCustomMessage(event.getCustomerName() + " accepted quote " + event.getQuoteNumber() + " (" + vars.get("totalAmount") + ").");
            notificationService.sendNotification(req);
        }
    }

    @EventListener
    public void handleQuoteChangeRequested(QuoteChangeRequestedEvent event) {
        List<User> staffUsers = getStaffUsers(event.getTenantId());
        for (User staff : staffUsers) {
            NotificationRequestDTO req = new NotificationRequestDTO();
            req.setTenantId(event.getTenantId());
            req.setRecipientUserId(staff.getId());
            req.setType(NotificationType.QUOTE_CHANGE_REQUESTED);
            req.setChannel(NotificationChannel.IN_APP);
            req.setPriority(NotificationPriority.HIGH);
            req.setReferenceType("QUOTE");
            req.setReferenceId(event.getQuoteId().toString());
            req.setCustomTitle("Change Requested: Quote " + event.getQuoteNumber());
            req.setCustomMessage(event.getCustomerName() + " requested changes to quote " + event.getQuoteNumber() + ": " + event.getNotes());
            notificationService.sendNotification(req);
        }
    }

    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", event.getCustomerName());
        vars.put("bookingNumber", event.getBookingNumber());
        vars.put("eventName", event.getEventName() != null ? event.getEventName() : "Event");
        vars.put("eventDate", event.getEventDate() != null ? event.getEventDate().toString() : "");

        NotificationRequestDTO inApp = new NotificationRequestDTO();
        inApp.setTenantId(event.getTenantId());
        inApp.setRecipientCustomerId(event.getCustomerId());
        inApp.setType(NotificationType.BOOKING_CONFIRMED);
        inApp.setChannel(NotificationChannel.IN_APP);
        inApp.setPriority(NotificationPriority.NORMAL);
        inApp.setReferenceType("BOOKING");
        inApp.setReferenceId(event.getBookingId().toString());
        inApp.setTemplateVariables(vars);
        notificationService.sendNotification(inApp);
    }

    @EventListener
    public void handlePaymentReceived(PaymentReceivedEvent event) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("customerName", event.getCustomerName());
        vars.put("bookingNumber", event.getBookingNumber());
        vars.put("paymentAmount", "$" + (event.getAmount() != null ? event.getAmount().toString() : "0.00"));
        vars.put("balanceDue", "$" + (event.getBalanceDue() != null ? event.getBalanceDue().toString() : "0.00"));

        // Customer In-App Notification
        NotificationRequestDTO inApp = new NotificationRequestDTO();
        inApp.setTenantId(event.getTenantId());
        inApp.setRecipientCustomerId(event.getCustomerId());
        inApp.setType(NotificationType.PAYMENT_RECEIVED);
        inApp.setChannel(NotificationChannel.IN_APP);
        inApp.setPriority(NotificationPriority.NORMAL);
        inApp.setReferenceType("PAYMENT");
        inApp.setReferenceId(event.getPaymentId().toString());
        inApp.setCustomTitle("Payment Received ($" + event.getAmount() + ")");
        inApp.setCustomMessage("Thank you for your payment of $" + event.getAmount() + " for booking " + event.getBookingNumber() + ". Remaining balance: $" + event.getBalanceDue() + ".");
        notificationService.sendNotification(inApp);

        // Staff In-App Notification
        List<User> staffUsers = getStaffUsers(event.getTenantId());
        for (User staff : staffUsers) {
            NotificationRequestDTO staffReq = new NotificationRequestDTO();
            staffReq.setTenantId(event.getTenantId());
            staffReq.setRecipientUserId(staff.getId());
            staffReq.setType(NotificationType.PAYMENT_RECEIVED);
            staffReq.setChannel(NotificationChannel.IN_APP);
            staffReq.setPriority(NotificationPriority.NORMAL);
            staffReq.setReferenceType("PAYMENT");
            staffReq.setReferenceId(event.getPaymentId().toString());
            staffReq.setCustomTitle("Payment of $" + event.getAmount() + " Received");
            staffReq.setCustomMessage("Payment of $" + event.getAmount() + " received from " + event.getCustomerName() + " for booking " + event.getBookingNumber() + ".");
            notificationService.sendNotification(staffReq);
        }
    }

    @EventListener
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        NotificationRequestDTO inApp = new NotificationRequestDTO();
        inApp.setTenantId(event.getTenantId());
        inApp.setRecipientCustomerId(event.getCustomerId());
        inApp.setType(NotificationType.INVOICE_CREATED);
        inApp.setChannel(NotificationChannel.IN_APP);
        inApp.setPriority(NotificationPriority.NORMAL);
        inApp.setReferenceType("INVOICE");
        inApp.setReferenceId(event.getInvoiceId().toString());
        inApp.setCustomTitle("Invoice " + event.getInvoiceNumber() + " Created");
        inApp.setCustomMessage("Invoice " + event.getInvoiceNumber() + " ($" + event.getTotalAmount() + ") has been issued for your rental booking.");
        notificationService.sendNotification(inApp);
    }

    @EventListener
    public void handleInvoiceSent(InvoiceSentEvent event) {
        NotificationRequestDTO inApp = new NotificationRequestDTO();
        inApp.setTenantId(event.getTenantId());
        inApp.setRecipientCustomerId(event.getCustomerId());
        inApp.setType(NotificationType.INVOICE_SENT);
        inApp.setChannel(NotificationChannel.IN_APP);
        inApp.setPriority(NotificationPriority.NORMAL);
        inApp.setReferenceType("INVOICE");
        inApp.setReferenceId(event.getInvoiceId().toString());
        inApp.setCustomTitle("Invoice " + event.getInvoiceNumber() + " Available");
        inApp.setCustomMessage("Invoice " + event.getInvoiceNumber() + " totaling $" + event.getTotalAmount() + " is ready for review.");
        notificationService.sendNotification(inApp);

        NotificationRequestDTO email = new NotificationRequestDTO();
        email.setTenantId(event.getTenantId());
        email.setRecipientCustomerId(event.getCustomerId());
        email.setRecipientEmail(event.getCustomerEmail());
        email.setType(NotificationType.INVOICE_SENT);
        email.setChannel(NotificationChannel.EMAIL);
        email.setPriority(NotificationPriority.NORMAL);
        email.setReferenceType("INVOICE");
        email.setReferenceId(event.getInvoiceId().toString());
        email.setCustomTitle("Your Invoice " + event.getInvoiceNumber() + " is Ready");
        email.setCustomMessage("Hello " + event.getCustomerName() + ",\n\nInvoice " + event.getInvoiceNumber() + " for $" + event.getTotalAmount() + " has been sent. Balance due: $" + event.getBalanceDue() + ".");
        notificationService.sendNotification(email);
    }

    @EventListener
    public void handleCustomerRequestCreated(CustomerRequestCreatedEvent event) {
        List<User> staffUsers = getStaffUsers(event.getTenantId());
        for (User staff : staffUsers) {
            NotificationRequestDTO req = new NotificationRequestDTO();
            req.setTenantId(event.getTenantId());
            req.setRecipientUserId(staff.getId());
            req.setType(NotificationType.CUSTOMER_REQUEST_CREATED);
            req.setChannel(NotificationChannel.IN_APP);
            req.setPriority(NotificationPriority.HIGH);
            req.setReferenceType("CUSTOMER_REQUEST");
            req.setReferenceId(event.getRequestId().toString());
            req.setCustomTitle("New Customer Request: " + event.getSubject());
            req.setCustomMessage("Request from " + event.getCustomerName() + " (" + event.getRequestType() + "): " + event.getMessage());
            notificationService.sendNotification(req);
        }
    }

    private List<User> getStaffUsers(String tenantId) {
        try {
            return userRepository.findByTenant_Id(UUID.fromString(tenantId));
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
