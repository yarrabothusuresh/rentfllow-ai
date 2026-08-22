package com.rentflow.notification.service;

import com.rentflow.notification.dto.AIDraftDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIDraftServiceImpl implements AIDraftService {

    @Override
    public AIDraftDTO draftMessage(String tenantId, NotificationType notificationType, NotificationChannel channel, Map<String, Object> context) {
        String customerName = (String) context.getOrDefault("customerName", "Valued Customer");
        String referenceNumber = (String) context.getOrDefault("referenceNumber", "REF-1001");
        String amount = (String) context.getOrDefault("amount", "$0.00");
        String companyName = (String) context.getOrDefault("companyName", "RentFlow AI");

        String subject;
        String body;

        switch (notificationType) {
            case INVOICE_OVERDUE:
                subject = "Reminder: Invoice " + referenceNumber + " Overdue Notice";
                body = "Hello " + customerName + ",\n\nOur records indicate invoice " + referenceNumber + " with a balance of " + amount + " is overdue. Please log in to your customer portal to make a payment or reply with questions.\n\nThank you,\n" + companyName;
                break;
            case PAYMENT_DUE:
                subject = "Upcoming Payment Due for " + referenceNumber;
                body = "Hello " + customerName + ",\n\nThis is a friendly reminder that a payment of " + amount + " for " + referenceNumber + " is due soon. You can safely complete payment in your portal.\n\nBest regards,\n" + companyName;
                break;
            case QUOTE_SENT:
                subject = "Your Rental Quote " + referenceNumber + " is Ready";
                body = "Hello " + customerName + ",\n\nWe have prepared your rental quote " + referenceNumber + " totaling " + amount + ". Please review and accept your proposal online.\n\nThank you,\n" + companyName;
                break;
            default:
                subject = "Important Update Regarding " + referenceNumber;
                body = "Hello " + customerName + ",\n\nHere is an update concerning " + referenceNumber + " (" + amount + "). Please check your account portal for complete details.\n\nSincerely,\n" + companyName;
                break;
        }

        return new AIDraftDTO(subject, body, notificationType, channel);
    }
}
