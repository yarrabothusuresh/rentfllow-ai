package com.rentflow.crm.service;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.CustomerType;
import com.rentflow.ai.model.Quote;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.ai.service.CustomerService;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.crm.dto.*;
import com.rentflow.crm.model.*;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("crmLeadService")
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadValidationService validationService;
    private final LeadActivityService activityService;
    private final LeadFollowUpRepository followUpRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final QuoteService quoteService;
    private final QuoteRepository quoteRepository;
    private final NotificationService notificationService;

    public LeadService(LeadRepository leadRepository,
                       LeadValidationService validationService,
                       LeadActivityService activityService,
                       LeadFollowUpRepository followUpRepository,
                       CustomerService customerService,
                       CustomerRepository customerRepository,
                       QuoteService quoteService,
                       QuoteRepository quoteRepository,
                       NotificationService notificationService) {
        this.leadRepository = leadRepository;
        this.validationService = validationService;
        this.activityService = activityService;
        this.followUpRepository = followUpRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.quoteService = quoteService;
        this.quoteRepository = quoteRepository;
        this.notificationService = notificationService;
    }

    public synchronized String generateLeadNumber(String tenantId) {
        long count = leadRepository.count() + 1;
        String candidate = String.format("LEAD-%06d", count);
        while (leadRepository.findByLeadNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("LEAD-%06d", count);
        }
        return candidate;
    }

    public LeadDetailDTO createLead(String tenantId, LeadCreateRequest req, String user) {
        if (req.getEmail() == null && req.getPhone() == null) {
            throw new IllegalArgumentException("At least one contact method (email or phone) is required.");
        }

        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setLeadNumber(generateLeadNumber(tenantId));
        lead.setSource(req.getSource() != null ? req.getSource() : LeadSource.MANUAL);
        lead.setStage(LeadStage.NEW);
        lead.setPriority(req.getPriority() != null ? req.getPriority() : LeadPriority.NORMAL);

        lead.setFirstName(req.getFirstName() != null ? req.getFirstName().trim() : "Prospective");
        lead.setLastName(req.getLastName() != null ? req.getLastName().trim() : "Client");
        lead.setCompanyName(req.getCompanyName());
        lead.setEmail(req.getEmail() != null ? req.getEmail().trim() : "inquiry@client.com");
        lead.setPhone(req.getPhone());
        lead.setPreferredContactMethod(req.getPreferredContactMethod() != null ? req.getPreferredContactMethod() : "EMAIL");

        lead.setEventName(req.getEventName());
        lead.setEventType(req.getEventType());
        lead.setEventDate(req.getEventDate());
        lead.setRentalStartDate(req.getRentalStartDate());
        lead.setRentalEndDate(req.getRentalEndDate());
        lead.setVenueName(req.getVenueName());
        lead.setVenueAddressSnapshot(req.getVenueAddressSnapshot());
        lead.setGuestCount(req.getGuestCount());
        lead.setEstimatedBudget(req.getEstimatedBudget());
        lead.setEstimatedValue(req.getEstimatedValue());

        lead.setCustomerNotes(req.getCustomerNotes());
        lead.setInternalNotes(req.getInternalNotes());

        lead.setAssignedSalesUserId(req.getAssignedSalesUserId());
        lead.setAssignedSalesUserName(req.getAssignedSalesUserName());

        lead.setCreatedBy(user != null ? user : "SYSTEM");
        lead.setUpdatedBy(user != null ? user : "SYSTEM");

        Lead saved = leadRepository.save(lead);

        // Log initial activity
        activityService.logActivity(tenantId, saved.getId(), ActivityType.STATUS_CHANGE, ActivityDirection.INTERNAL,
                "Lead Captured (" + saved.getLeadNumber() + ")",
                "Source: " + saved.getSource() + " | Priority: " + saved.getPriority(),
                req.getCustomerNotes(), null, "LEAD", saved.getId().toString(), user);

        // If assigned, log assignment activity
        if (saved.getAssignedSalesUserId() != null) {
            activityService.logActivity(tenantId, saved.getId(), ActivityType.ASSIGNMENT, ActivityDirection.INTERNAL,
                    "Assigned to " + saved.getAssignedSalesUserName(),
                    "Assigned on creation by " + user, null, null, null, null, user);
        }

        // Notify assigned user if assigned on creation
        if (saved.getAssignedSalesUserId() != null && !saved.getAssignedSalesUserId().equalsIgnoreCase(user)) {
            try {
                UUID recipientId = null;
                try { recipientId = UUID.fromString(saved.getAssignedSalesUserId()); } catch (Exception ignored) {}
                if (recipientId != null) {
                    NotificationRequestDTO notif = new NotificationRequestDTO();
                    notif.setTenantId(tenantId);
                    notif.setRecipientUserId(recipientId);
                    notif.setCustomTitle("New Lead Assigned: " + saved.getLeadNumber());
                    notif.setCustomMessage("You have been assigned to " + saved.getContactName() + " (" + saved.getEventName() + ")");
                    notif.setPriority(saved.getPriority() == LeadPriority.URGENT ? NotificationPriority.URGENT : NotificationPriority.NORMAL);
                    notif.setType(NotificationType.SYSTEM);
                    notif.setReferenceType("CRM_LEAD");
                    notif.setReferenceId(saved.getId().toString());
                    notificationService.sendNotification(notif);
                }
            } catch (Exception ignored) {}
        }

        return toDetailDTO(saved);
    }

    public LeadDetailDTO updateLead(String tenantId, UUID id, LeadUpdateRequest req, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        if (req.getFirstName() != null) lead.setFirstName(req.getFirstName().trim());
        if (req.getLastName() != null) lead.setLastName(req.getLastName().trim());
        if (req.getCompanyName() != null) lead.setCompanyName(req.getCompanyName().trim());
        if (req.getEmail() != null) lead.setEmail(req.getEmail().trim());
        if (req.getPhone() != null) lead.setPhone(req.getPhone().trim());
        if (req.getPreferredContactMethod() != null) lead.setPreferredContactMethod(req.getPreferredContactMethod());
        if (req.getSource() != null) lead.setSource(req.getSource());
        if (req.getPriority() != null) lead.setPriority(req.getPriority());

        if (req.getEventName() != null) lead.setEventName(req.getEventName());
        if (req.getEventType() != null) lead.setEventType(req.getEventType());
        if (req.getEventDate() != null) lead.setEventDate(req.getEventDate());
        if (req.getRentalStartDate() != null) lead.setRentalStartDate(req.getRentalStartDate());
        if (req.getRentalEndDate() != null) lead.setRentalEndDate(req.getRentalEndDate());
        if (req.getVenueName() != null) lead.setVenueName(req.getVenueName());
        if (req.getVenueAddressSnapshot() != null) lead.setVenueAddressSnapshot(req.getVenueAddressSnapshot());
        if (req.getGuestCount() != null) lead.setGuestCount(req.getGuestCount());
        if (req.getEstimatedBudget() != null) lead.setEstimatedBudget(req.getEstimatedBudget());
        if (req.getEstimatedValue() != null) lead.setEstimatedValue(req.getEstimatedValue());

        if (req.getCustomerNotes() != null) lead.setCustomerNotes(req.getCustomerNotes());
        if (req.getInternalNotes() != null) lead.setInternalNotes(req.getInternalNotes());

        lead.setUpdatedBy(user != null ? user : "SYSTEM");
        Lead saved = leadRepository.save(lead);
        return toDetailDTO(saved);
    }

    public LeadDetailDTO assignLead(String tenantId, UUID id, LeadAssignRequest req, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        String prevAssigned = lead.getAssignedSalesUserName();
        lead.setAssignedSalesUserId(req.getAssignedSalesUserId());
        lead.setAssignedSalesUserName(req.getAssignedSalesUserName());
        lead.setUpdatedBy(user);

        Lead saved = leadRepository.save(lead);

        String summary = req.getAssignedSalesUserName() != null ?
                "Reassigned from " + (prevAssigned != null ? prevAssigned : "Unassigned") + " to " + req.getAssignedSalesUserName() :
                "Unassigned";

        activityService.logActivity(tenantId, id, ActivityType.ASSIGNMENT, ActivityDirection.INTERNAL,
                "Lead Assignment Changed", summary, null, null, null, null, user);

        if (req.getAssignedSalesUserId() != null && !req.getAssignedSalesUserId().equalsIgnoreCase(user)) {
            try {
                UUID recipientId = null;
                try { recipientId = UUID.fromString(req.getAssignedSalesUserId()); } catch (Exception ignored) {}
                if (recipientId != null) {
                    NotificationRequestDTO notif = new NotificationRequestDTO();
                    notif.setTenantId(tenantId);
                    notif.setRecipientUserId(recipientId);
                    notif.setCustomTitle("Lead Assigned: " + saved.getLeadNumber());
                    notif.setCustomMessage("You have been assigned to lead " + saved.getContactName() + " (" + saved.getEventName() + ")");
                    notif.setPriority(NotificationPriority.NORMAL);
                    notif.setType(NotificationType.SYSTEM);
                    notif.setReferenceType("CRM_LEAD");
                    notif.setReferenceId(saved.getId().toString());
                    notificationService.sendNotification(notif);
                }
            } catch (Exception ignored) {}
        }

        return toDetailDTO(saved);
    }

    public LeadDetailDTO transitionStage(String tenantId, UUID id, LeadTransitionRequest req, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        validationService.validateTransition(lead, req.getTargetStage());

        LeadStage prev = lead.getStage();
        lead.setStage(req.getTargetStage());
        lead.setUpdatedBy(user);

        if (req.getTargetStage() == LeadStage.CONTACTED && lead.getLastContactedAt() == null) {
            lead.setLastContactedAt(LocalDateTime.now());
        }

        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.STATUS_CHANGE, ActivityDirection.INTERNAL,
                "Stage Changed: " + prev + " → " + req.getTargetStage(),
                req.getReason() != null ? req.getReason() : "Stage progression by " + user,
                req.getNotes(), null, null, null, user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO qualifyLead(String tenantId, UUID id, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        validationService.validateQualification(lead);

        lead.setStage(LeadStage.QUALIFIED);
        lead.setQualifiedAt(LocalDateTime.now());
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.STATUS_CHANGE, ActivityDirection.INTERNAL,
                "Lead Qualified", "Lead marked as QUALIFIED by " + user, null, null, null, null, user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO disqualifyLead(String tenantId, UUID id, String reason, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        lead.setStage(LeadStage.DISQUALIFIED);
        lead.setLostReason(LeadLostReason.NOT_A_FIT);
        lead.setLostReasonNotes(reason);
        lead.setLostAt(LocalDateTime.now());
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.STATUS_CHANGE, ActivityDirection.INTERNAL,
                "Lead Disqualified", reason != null ? reason : "Disqualified by " + user, null, null, null, null, user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO markWon(String tenantId, UUID id, UUID quoteId, UUID bookingId, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        lead.setStage(LeadStage.WON);
        lead.setWonAt(LocalDateTime.now());
        if (quoteId != null) lead.setQuoteId(quoteId);
        if (bookingId != null) lead.setBookingId(bookingId);
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.WON, ActivityDirection.INTERNAL,
                "Lead Closed WON", "Opportunity successfully won by " + user, null, null, null, null, user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO markLost(String tenantId, UUID id, LeadLostRequest req, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        validationService.validateLost(req);

        lead.setStage(LeadStage.LOST);
        lead.setLostReason(req.getLostReason());
        lead.setLostReasonNotes(req.getLostReasonNotes());
        lead.setLostAt(LocalDateTime.now());
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.LOST, ActivityDirection.INTERNAL,
                "Lead Closed LOST (" + req.getLostReason() + ")",
                req.getLostReasonNotes() != null ? req.getLostReasonNotes() : "Marked lost by " + user,
                null, null, null, null, user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO reopenLead(String tenantId, UUID id, LeadReopenRequest req, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        validationService.validateReopen(req);

        lead.setStage(LeadStage.CONTACTED);
        lead.setReopenedAt(LocalDateTime.now());
        lead.setReopenReason(req.getReopenReason());
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, id, ActivityType.REOPENED, ActivityDirection.INTERNAL,
                "Lead Reopened",
                "Reason: " + req.getReopenReason() + " | Reopened by " + user,
                null, null, null, null, user);

        return toDetailDTO(saved);
    }

    public List<CustomerMatchDTO> checkCustomerMatches(String tenantId, UUID leadId) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        List<CustomerMatchDTO> matches = new ArrayList<>();

        if (lead.getEmail() != null && !lead.getEmail().trim().isEmpty()) {
            customerRepository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, lead.getEmail().trim())
                    .ifPresent(c -> matches.add(new CustomerMatchDTO(c.getId(), c.getCustomerNumber(),
                            c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : ""),
                            c.getEmail(), c.getPhone(), c.getCompanyName(), "EMAIL_MATCH")));
        }

        if (lead.getCompanyName() != null && !lead.getCompanyName().trim().isEmpty()) {
            List<Customer> companyMatches = customerRepository.searchCustomers(tenantId, lead.getCompanyName().trim());
            for (Customer c : companyMatches) {
                if (matches.stream().noneMatch(m -> m.getCustomerId().equals(c.getId()))) {
                    matches.add(new CustomerMatchDTO(c.getId(), c.getCustomerNumber(),
                            c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : ""),
                            c.getEmail(), c.getPhone(), c.getCompanyName(), "COMPANY_MATCH"));
                }
            }
        }

        return matches;
    }

    public LeadDetailDTO linkCustomer(String tenantId, UUID leadId, UUID customerId, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        lead.setCustomerId(customer.getId());
        lead.setCustomerName(customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName() : ""));
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, leadId, ActivityType.CUSTOMER_LINKED, ActivityDirection.INTERNAL,
                "Customer Linked: " + lead.getCustomerName(),
                "Linked to existing customer #" + customer.getCustomerNumber(),
                null, null, "CUSTOMER", customer.getId().toString(), user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO convertCustomer(String tenantId, UUID leadId, boolean forceNew, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        if (lead.getCustomerId() != null) {
            return toDetailDTO(lead); // Idempotent return
        }

        if (!forceNew) {
            Optional<Customer> existing = customerRepository.findFirstByTenantIdAndEmailIgnoreCase(tenantId, lead.getEmail());
            if (existing.isPresent()) {
                return linkCustomer(tenantId, leadId, existing.get().getId(), user);
            }
        }

        Customer newCustomer = new Customer();
        newCustomer.setTenantId(tenantId);
        newCustomer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        newCustomer.setFirstName(lead.getFirstName());
        newCustomer.setLastName(lead.getLastName());
        newCustomer.setCompanyName(lead.getCompanyName());
        newCustomer.setEmail(lead.getEmail());
        newCustomer.setPhone(lead.getPhone());
        newCustomer.setCustomerType(lead.getCompanyName() != null && !lead.getCompanyName().trim().isEmpty() ? CustomerType.BUSINESS : CustomerType.INDIVIDUAL);
        newCustomer.setBillingAddress(lead.getVenueAddressSnapshot());
        newCustomer.setNotes("Created from CRM Lead " + lead.getLeadNumber());

        Customer savedCustomer = customerRepository.save(newCustomer);

        lead.setCustomerId(savedCustomer.getId());
        lead.setCustomerName(savedCustomer.getFirstName() + " " + (savedCustomer.getLastName() != null ? savedCustomer.getLastName() : ""));
        lead.setConvertedAt(LocalDateTime.now());
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, leadId, ActivityType.CUSTOMER_CREATED, ActivityDirection.INTERNAL,
                "Customer Created: " + lead.getCustomerName(),
                "Generated customer #" + savedCustomer.getCustomerNumber(),
                null, null, "CUSTOMER", savedCustomer.getId().toString(), user);

        return toDetailDTO(saved);
    }

    public LeadDetailDTO createQuoteDraftFromLead(String tenantId, UUID leadId, String user) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        if (lead.getCustomerId() == null) {
            convertCustomer(tenantId, leadId, false, user);
            lead = leadRepository.findByTenantIdAndId(tenantId, leadId).orElseThrow();
        }

        if (lead.getQuoteId() != null) {
            return toDetailDTO(lead); // Idempotent return
        }

        QuoteDTO quoteDto = new QuoteDTO();
        quoteDto.setCustomerId(lead.getCustomerId());
        quoteDto.setQuoteDate(java.time.LocalDate.now());
        if (lead.getRentalStartDate() != null) {
            quoteDto.setRentalStartDateTime(lead.getRentalStartDate().atTime(8, 0));
        } else if (lead.getEventDate() != null) {
            quoteDto.setRentalStartDateTime(lead.getEventDate().atTime(8, 0));
        }
        if (lead.getRentalEndDate() != null) {
            quoteDto.setRentalEndDateTime(lead.getRentalEndDate().atTime(20, 0));
        } else if (lead.getEventDate() != null) {
            quoteDto.setRentalEndDateTime(lead.getEventDate().plusDays(1).atTime(20, 0));
        }
        quoteDto.setEventName(lead.getEventName());
        quoteDto.setVenueName(lead.getVenueName());
        quoteDto.setNotes("Draft quote created from CRM Lead " + lead.getLeadNumber());
        quoteDto.setItems(Collections.emptyList());

        QuoteDTO createdQuote = quoteService.createQuote(tenantId, quoteDto, "SALES");

        lead.setQuoteId(createdQuote.getId());
        lead.setQuoteNumber(createdQuote.getQuoteNumber());
        lead.setStage(LeadStage.QUOTE_PREPARED);
        lead.setUpdatedBy(user);
        Lead saved = leadRepository.save(lead);

        activityService.logActivity(tenantId, leadId, ActivityType.QUOTE_CREATED, ActivityDirection.INTERNAL,
                "Quote Draft Created: " + createdQuote.getQuoteNumber(),
                "Quote created with status DRAFT", null, null, "QUOTE", createdQuote.getId().toString(), user);

        return toDetailDTO(saved);
    }

    public Page<LeadSummaryDTO> searchLeads(String tenantId, String query, LeadStage stage,
                                           String assignedUserId, boolean unassignedOnly, Pageable pageable) {
        return leadRepository.searchLeadsAdvanced(tenantId, query, stage, assignedUserId, unassignedOnly, pageable)
                .map(this::toSummaryDTO);
    }

    public Optional<LeadDetailDTO> getLeadById(String tenantId, UUID id) {
        return leadRepository.findByTenantIdAndId(tenantId, id).map(this::toDetailDTO);
    }

    public LeadSummaryDTO toSummaryDTO(Lead l) {
        LeadSummaryDTO dto = new LeadSummaryDTO();
        dto.setId(l.getId());
        dto.setLeadNumber(l.getLeadNumber());
        dto.setSource(l.getSource());
        dto.setStage(l.getStage());
        dto.setPriority(l.getPriority());
        dto.setContactName(l.getContactName());
        dto.setCompanyName(l.getCompanyName());
        dto.setEmail(l.getEmail());
        dto.setPhone(l.getPhone());
        dto.setEventName(l.getEventName());
        dto.setEventType(l.getEventType());
        dto.setEventDate(l.getEventDate());
        dto.setEstimatedValue(l.getEstimatedValue());
        dto.setAssignedSalesUserId(l.getAssignedSalesUserId());
        dto.setAssignedSalesUserName(l.getAssignedSalesUserName());
        dto.setNextFollowUpAt(l.getNextFollowUpAt());
        dto.setLastContactedAt(l.getLastContactedAt());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setOverdueFollowUp(l.getNextFollowUpAt() != null && l.getNextFollowUpAt().isBefore(LocalDateTime.now()));
        return dto;
    }

    public LeadDetailDTO toDetailDTO(Lead l) {
        LeadDetailDTO dto = new LeadDetailDTO();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setLeadNumber(l.getLeadNumber());
        dto.setSource(l.getSource());
        dto.setStage(l.getStage());
        dto.setPriority(l.getPriority());

        dto.setFirstName(l.getFirstName());
        dto.setLastName(l.getLastName());
        dto.setContactName(l.getContactName());
        dto.setCompanyName(l.getCompanyName());
        dto.setEmail(l.getEmail());
        dto.setPhone(l.getPhone());
        dto.setPreferredContactMethod(l.getPreferredContactMethod());

        dto.setEventName(l.getEventName());
        dto.setEventType(l.getEventType());
        dto.setEventDate(l.getEventDate());
        dto.setRentalStartDate(l.getRentalStartDate());
        dto.setRentalEndDate(l.getRentalEndDate());
        dto.setVenueName(l.getVenueName());
        dto.setVenueAddressSnapshot(l.getVenueAddressSnapshot());
        dto.setGuestCount(l.getGuestCount());

        dto.setEstimatedBudget(l.getEstimatedBudget());
        dto.setEstimatedValue(l.getEstimatedValue());

        dto.setCustomerNotes(l.getCustomerNotes());
        dto.setInternalNotes(l.getInternalNotes());

        dto.setAssignedSalesUserId(l.getAssignedSalesUserId());
        dto.setAssignedSalesUserName(l.getAssignedSalesUserName());

        dto.setRentalRequestId(l.getRentalRequestId());
        dto.setCustomerId(l.getCustomerId());
        dto.setCustomerName(l.getCustomerName());
        dto.setQuoteId(l.getQuoteId());
        dto.setQuoteNumber(l.getQuoteNumber());
        dto.setBookingId(l.getBookingId());
        dto.setBookingNumber(l.getBookingNumber());

        dto.setLostReason(l.getLostReason());
        dto.setLostReasonNotes(l.getLostReasonNotes());
        dto.setReopenReason(l.getReopenReason());

        dto.setNextFollowUpAt(l.getNextFollowUpAt());
        dto.setLastContactedAt(l.getLastContactedAt());
        dto.setQualifiedAt(l.getQualifiedAt());
        dto.setConvertedAt(l.getConvertedAt());
        dto.setWonAt(l.getWonAt());
        dto.setLostAt(l.getLostAt());
        dto.setReopenedAt(l.getReopenedAt());

        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        dto.setCreatedBy(l.getCreatedBy());
        dto.setUpdatedBy(l.getUpdatedBy());
        return dto;
    }
}
