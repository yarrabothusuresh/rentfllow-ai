package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerService customerService;
    private final EventService eventService;

    public LeadService(LeadRepository leadRepository,
                       CustomerService customerService,
                       EventService eventService) {
        this.leadRepository = leadRepository;
        this.customerService = customerService;
        this.eventService = eventService;
    }

    public List<LeadDTO> getLeads(String tenantId) {
        return leadRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LeadDTO> getLeadById(String tenantId, UUID id) {
        return leadRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toDTO);
    }

    public List<LeadDTO> getLeadsByStatus(String tenantId, LeadStatus status) {
        return leadRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<LeadDTO> searchLeads(String tenantId, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getLeads(tenantId);
        }
        return leadRepository.searchLeads(tenantId, query.trim()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeadDTO createLead(String tenantId, LeadDTO dto) {
        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setFirstName(dto.getFirstName());
        lead.setLastName(dto.getLastName());
        lead.setCompanyName(dto.getCompanyName());
        lead.setEmail(dto.getEmail());
        lead.setPhone(dto.getPhone());
        lead.setSource(dto.getSource() != null ? dto.getSource() : LeadSource.WEBSITE);
        lead.setEventType(dto.getEventType() != null ? dto.getEventType() : EventType.WEDDING);
        lead.setEventDate(dto.getEventDate());
        lead.setGuestCount(dto.getGuestCount() != null ? dto.getGuestCount() : 0);
        lead.setVenueName(dto.getVenueName());
        lead.setNotes(dto.getNotes());
        lead.setStatus(dto.getStatus() != null ? dto.getStatus() : LeadStatus.NEW);
        lead.setAssignedTo(dto.getAssignedTo() != null ? dto.getAssignedTo() : "Sales Team");

        Lead saved = leadRepository.save(lead);
        return toDTO(saved);
    }

    @Transactional
    public Optional<LeadDTO> updateLead(String tenantId, UUID id, LeadDTO dto) {
        return leadRepository.findByTenantIdAndId(tenantId, id).map(existing -> {
            if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
            if (dto.getCompanyName() != null) existing.setCompanyName(dto.getCompanyName());
            if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
            if (dto.getPhone() != null) existing.setPhone(dto.getPhone());
            if (dto.getSource() != null) existing.setSource(dto.getSource());
            if (dto.getEventType() != null) existing.setEventType(dto.getEventType());
            if (dto.getEventDate() != null) existing.setEventDate(dto.getEventDate());
            if (dto.getGuestCount() != null) existing.setGuestCount(dto.getGuestCount());
            if (dto.getVenueName() != null) existing.setVenueName(dto.getVenueName());
            if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
            if (dto.getAssignedTo() != null) existing.setAssignedTo(dto.getAssignedTo());

            Lead saved = leadRepository.save(existing);
            return toDTO(saved);
        });
    }

    @Transactional
    public Optional<LeadDTO> updateLeadStatus(String tenantId, UUID id, LeadStatus status) {
        return leadRepository.findByTenantIdAndId(tenantId, id).map(existing -> {
            existing.setStatus(status);
            Lead saved = leadRepository.save(existing);
            return toDTO(saved);
        });
    }

    @Transactional
    public boolean deleteLead(String tenantId, UUID id) {
        Optional<Lead> leadOpt = leadRepository.findByTenantIdAndId(tenantId, id);
        if (leadOpt.isPresent()) {
            leadRepository.delete(leadOpt.get());
            return true;
        }
        return false;
    }

    @Transactional
    public LeadConversionResult convertLead(String tenantId, UUID leadId, LeadConversionRequest req) {
        Lead lead = leadRepository.findByTenantIdAndId(tenantId, leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found or does not belong to tenant"));

        LeadConversionResult result = new LeadConversionResult();
        result.setLeadId(lead.getId());

        CustomerDTO customerDTO;

        // Duplicate Check by email
        Optional<CustomerDTO> existingCustomerOpt = customerService.findByEmail(tenantId, lead.getEmail());

        if (existingCustomerOpt.isPresent() &&
            (req == null || (!req.isForceNewCustomer() && req.getUseExistingCustomerId() == null))) {
            // Found possible duplicate customer, prompt user
            result.setPossibleDuplicateFound(true);
            result.setDuplicateCustomer(existingCustomerOpt.get());
            result.setStatus("DUPLICATE_FOUND");
            result.setMessage("An existing customer with this email was found.");
            return result;
        }

        if (req != null && req.getUseExistingCustomerId() != null) {
            customerDTO = customerService.getCustomerById(tenantId, req.getUseExistingCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected customer not found"));
        } else if (existingCustomerOpt.isPresent() && (req == null || !req.isForceNewCustomer())) {
            customerDTO = existingCustomerOpt.get();
        } else {
            // Create new customer
            CustomerDTO newCustReq = new CustomerDTO();
            newCustReq.setFirstName(lead.getFirstName());
            newCustReq.setLastName(lead.getLastName());
            newCustReq.setCompanyName(lead.getCompanyName());
            newCustReq.setEmail(lead.getEmail());
            newCustReq.setPhone(lead.getPhone());
            newCustReq.setCustomerType(lead.getCompanyName() != null && !lead.getCompanyName().isEmpty()
                    ? CustomerType.BUSINESS : CustomerType.INDIVIDUAL);
            newCustReq.setNotes(lead.getNotes());
            newCustReq.setCity("Dallas");
            newCustReq.setState("TX");
            newCustReq.setCountry("USA");
            newCustReq.setStatus(CustomerStatus.ACTIVE);

            customerDTO = customerService.createCustomer(tenantId, newCustReq);
        }

        // Create Event
        String eventName = lead.getFirstName() + "'s " +
                (lead.getEventType() != null ? lead.getEventType().name() : "Event");

        EventDTO eventReq = new EventDTO();
        eventReq.setCustomerId(customerDTO.getId());
        eventReq.setEventName(eventName);
        eventReq.setEventType(lead.getEventType() != null ? lead.getEventType() : EventType.WEDDING);
        eventReq.setEventDate(lead.getEventDate() != null ? lead.getEventDate() : java.time.LocalDate.now().plusDays(30));
        eventReq.setGuestCount(lead.getGuestCount() != null ? lead.getGuestCount() : 100);
        eventReq.setVenueName(lead.getVenueName() != null ? lead.getVenueName() : "Venue TBD");
        eventReq.setCity("Dallas");
        eventReq.setState("TX");
        eventReq.setStatus(EventStatus.PLANNING);
        eventReq.setSpecialInstructions(lead.getNotes());

        EventDTO createdEvent = eventService.createEvent(tenantId, eventReq);

        // Populate default rental requirements (for demonstration scenario e.g. 250 chairs, 25 tables, 25 linens)
        int guests = lead.getGuestCount() != null && lead.getGuestCount() > 0 ? lead.getGuestCount() : 100;
        int tables = (int) Math.ceil(guests / 10.0);

        EventRequirementDTO req1 = new EventRequirementDTO();
        req1.setDescription("Chiavari Chairs");
        req1.setQuantity(guests);
        req1.setNotes("Gold chiavari chairs with white cushions");
        eventService.addRequirement(tenantId, createdEvent.getId(), req1);

        EventRequirementDTO req2 = new EventRequirementDTO();
        req2.setDescription("Round Tables");
        req2.setQuantity(tables);
        req2.setNotes("60-inch round folding tables");
        eventService.addRequirement(tenantId, createdEvent.getId(), req2);

        EventRequirementDTO req3 = new EventRequirementDTO();
        req3.setDescription("White Table Linens");
        req3.setQuantity(tables);
        req3.setNotes("120-inch round polyester tablecloths");
        eventService.addRequirement(tenantId, createdEvent.getId(), req3);

        // Mark lead as CONVERTED
        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        result.setCustomerId(customerDTO.getId());
        result.setCustomerNumber(customerDTO.getCustomerNumber());
        result.setEventId(createdEvent.getId());
        result.setPossibleDuplicateFound(false);
        result.setStatus("SUCCESS");
        result.setMessage("Lead converted successfully. Customer (" + customerDTO.getCustomerNumber() + ") and Event created.");
        return result;
    }

    public LeadDTO toDTO(Lead lead) {
        LeadDTO dto = new LeadDTO();
        dto.setId(lead.getId());
        dto.setTenantId(lead.getTenantId());
        dto.setFirstName(lead.getFirstName());
        dto.setLastName(lead.getLastName());
        dto.setCompanyName(lead.getCompanyName());
        dto.setEmail(lead.getEmail());
        dto.setPhone(lead.getPhone());
        dto.setSource(lead.getSource());
        dto.setEventType(lead.getEventType());
        dto.setEventDate(lead.getEventDate());
        dto.setGuestCount(lead.getGuestCount());
        dto.setVenueName(lead.getVenueName());
        dto.setNotes(lead.getNotes());
        dto.setStatus(lead.getStatus());
        dto.setAssignedTo(lead.getAssignedTo());
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());
        return dto;
    }
}
