package com.rentflow.crm.listener;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.crm.model.*;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadService;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.event.BookingConfirmedEvent;
import com.rentflow.notification.event.CustomerRequestCreatedEvent;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class CrmEventListener {

    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final LeadActivityService activityService;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public CrmEventListener(LeadRepository leadRepository,
                            LeadService leadService,
                            LeadActivityService activityService,
                            CustomerRepository customerRepository,
                            NotificationService notificationService) {
        this.leadRepository = leadRepository;
        this.leadService = leadService;
        this.activityService = activityService;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @EventListener
    @Transactional
    public void onCustomerRequestCreated(CustomerRequestCreatedEvent event) {
        if (event == null || event.getTenantId() == null || event.getRequestId() == null) {
            return;
        }

        String lockKey = (event.getTenantId() + ":lead-req:" + event.getRequestId()).intern();
        synchronized (lockKey) {
            // Idempotency check: Do not create duplicate lead for the same rental request
            Optional<Lead> existingLead = leadRepository.findByTenantIdAndRentalRequestId(event.getTenantId(), event.getRequestId());
            if (existingLead.isPresent()) {
                return;
            }

            Customer customer = null;
            if (event.getCustomerId() != null) {
                customer = customerRepository.findByTenantIdAndId(event.getTenantId(), event.getCustomerId()).orElse(null);
            }

        Lead lead = new Lead();
        lead.setTenantId(event.getTenantId());
        lead.setLeadNumber(leadService.generateLeadNumber(event.getTenantId()));
        lead.setSource(LeadSource.STOREFRONT_REQUEST);
        lead.setStage(LeadStage.NEW);
        lead.setPriority(LeadPriority.NORMAL);

        lead.setFirstName(customer != null ? customer.getFirstName() : (event.getCustomerName() != null ? event.getCustomerName() : "Valued"));
        lead.setLastName(customer != null ? customer.getLastName() : "Client");
        lead.setCompanyName(customer != null ? customer.getCompanyName() : null);
        lead.setEmail(customer != null ? customer.getEmail() : "request@storefront.com");
        lead.setPhone(customer != null ? customer.getPhone() : null);

        lead.setEventName(event.getSubject() != null ? event.getSubject() : "Storefront Rental Request");
        lead.setCustomerNotes(event.getMessage());
        lead.setRentalRequestId(event.getRequestId());

        if (customer != null) {
            lead.setCustomerId(customer.getId());
            lead.setCustomerName(customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName() : ""));
        }

        lead.setCreatedBy("STOREFRONT_REQUEST");
        try {
            Lead saved = leadRepository.saveAndFlush(lead);
            activityService.logActivity(event.getTenantId(), saved.getId(), ActivityType.STATUS_CHANGE, ActivityDirection.INBOUND,
                    "Lead Created from Storefront Request (" + saved.getLeadNumber() + ")",
                    "Linked Rental Request #" + event.getRequestId(),
                    event.getMessage(), null, "RENTAL_REQUEST", event.getRequestId().toString(), "STOREFRONT");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Concurrent duplicate event delivery: existing lead already persisted by another thread
        }
        }
    }

    @EventListener
    @Transactional
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        if (event == null || event.getTenantId() == null) {
            return;
        }

        if (event.getCustomerId() != null) {
            List<Lead> customerLeads = leadRepository.findByTenantIdAndCustomerId(event.getTenantId(), event.getCustomerId());
            for (Lead lead : customerLeads) {
                if (lead.getStage() == LeadStage.QUOTE_SENT || lead.getStage() == LeadStage.FOLLOW_UP || lead.getStage() == LeadStage.NEGOTIATION) {
                    leadService.markWon(event.getTenantId(), lead.getId(), lead.getQuoteId(), event.getBookingId(), "BOOKING_SYSTEM");
                    break;
                }
            }
        }
    }
}
