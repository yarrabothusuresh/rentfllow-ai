package com.rentflow.ai.service;

import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.EventRequirementDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.model.EventRequirement;
import com.rentflow.ai.model.EventStatus;
import com.rentflow.ai.model.EventType;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.EventRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final EventRequirementRepository requirementRepository;

    public EventService(EventRepository eventRepository,
                        CustomerRepository customerRepository,
                        EventRequirementRepository requirementRepository) {
        this.eventRepository = eventRepository;
        this.customerRepository = customerRepository;
        this.requirementRepository = requirementRepository;
    }

    public List<EventDTO> getEvents(String tenantId) {
        return eventRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<EventDTO> getEventById(String tenantId, UUID id) {
        return eventRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toDTO);
    }

    public List<EventDTO> getEventsByCustomer(String tenantId, UUID customerId) {
        return eventRepository.findByTenantIdAndCustomerId(tenantId, customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> getUpcomingEvents(String tenantId, LocalDate startDate, LocalDate endDate) {
        return eventRepository.findByTenantIdAndEventDateBetween(tenantId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO createEvent(String tenantId, EventDTO dto) {
        Event event = new Event();
        event.setTenantId(tenantId);
        event.setCustomerId(dto.getCustomerId());
        event.setEventName(dto.getEventName());
        event.setEventType(dto.getEventType() != null ? dto.getEventType() : EventType.WEDDING);
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setGuestCount(dto.getGuestCount() != null ? dto.getGuestCount() : 0);
        event.setVenueName(dto.getVenueName());
        event.setVenueAddress(dto.getVenueAddress());
        event.setCity(dto.getCity());
        event.setState(dto.getState());
        event.setZipCode(dto.getZipCode());
        event.setSpecialInstructions(dto.getSpecialInstructions());
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : EventStatus.PLANNING);

        Event saved = eventRepository.save(event);
        return toDTO(saved);
    }

    @Transactional
    public Optional<EventDTO> updateEvent(String tenantId, UUID id, EventDTO dto) {
        return eventRepository.findByTenantIdAndId(tenantId, id).map(existing -> {
            if (dto.getEventName() != null) existing.setEventName(dto.getEventName());
            if (dto.getEventType() != null) existing.setEventType(dto.getEventType());
            if (dto.getEventDate() != null) existing.setEventDate(dto.getEventDate());
            if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
            if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
            if (dto.getGuestCount() != null) existing.setGuestCount(dto.getGuestCount());
            if (dto.getVenueName() != null) existing.setVenueName(dto.getVenueName());
            if (dto.getVenueAddress() != null) existing.setVenueAddress(dto.getVenueAddress());
            if (dto.getCity() != null) existing.setCity(dto.getCity());
            if (dto.getState() != null) existing.setState(dto.getState());
            if (dto.getZipCode() != null) existing.setZipCode(dto.getZipCode());
            if (dto.getSpecialInstructions() != null) existing.setSpecialInstructions(dto.getSpecialInstructions());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

            Event saved = eventRepository.save(existing);
            return toDTO(saved);
        });
    }

    // Requirements
    public List<EventRequirementDTO> getRequirements(String tenantId, UUID eventId) {
        return requirementRepository.findByTenantIdAndEventId(tenantId, eventId).stream()
                .map(this::toRequirementDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequirementDTO addRequirement(String tenantId, UUID eventId, EventRequirementDTO dto) {
        EventRequirement req = new EventRequirement();
        req.setTenantId(tenantId);
        req.setEventId(eventId);
        req.setDescription(dto.getDescription());
        req.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        req.setNotes(dto.getNotes());

        EventRequirement saved = requirementRepository.save(req);
        return toRequirementDTO(saved);
    }

    @Transactional
    public Optional<EventRequirementDTO> updateRequirement(String tenantId, UUID eventId, UUID requirementId, EventRequirementDTO dto) {
        return requirementRepository.findByTenantIdAndId(tenantId, requirementId)
                .filter(req -> req.getEventId().equals(eventId))
                .map(existing -> {
                    if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
                    if (dto.getQuantity() != null) existing.setQuantity(dto.getQuantity());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());

                    EventRequirement saved = requirementRepository.save(existing);
                    return toRequirementDTO(saved);
                });
    }

    @Transactional
    public boolean deleteRequirement(String tenantId, UUID eventId, UUID requirementId) {
        Optional<EventRequirement> reqOpt = requirementRepository.findByTenantIdAndId(tenantId, requirementId);
        if (reqOpt.isPresent() && reqOpt.get().getEventId().equals(eventId)) {
            requirementRepository.delete(reqOpt.get());
            return true;
        }
        return false;
    }

    public EventDTO toDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTenantId(event.getTenantId());
        dto.setCustomerId(event.getCustomerId());

        // Resolve customer name
        if (event.getCustomerId() != null) {
            customerRepository.findByTenantIdAndId(event.getTenantId(), event.getCustomerId())
                    .ifPresent(c -> {
                        String name = (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
                        dto.setCustomerName(name);
                    });
        }

        dto.setEventName(event.getEventName());
        dto.setEventType(event.getEventType());
        dto.setEventDate(event.getEventDate());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setGuestCount(event.getGuestCount());
        dto.setVenueName(event.getVenueName());
        dto.setVenueAddress(event.getVenueAddress());
        dto.setCity(event.getCity());
        dto.setState(event.getState());
        dto.setZipCode(event.getZipCode());
        dto.setSpecialInstructions(event.getSpecialInstructions());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }

    public EventRequirementDTO toRequirementDTO(EventRequirement req) {
        EventRequirementDTO dto = new EventRequirementDTO();
        dto.setId(req.getId());
        dto.setTenantId(req.getTenantId());
        dto.setEventId(req.getEventId());
        dto.setDescription(req.getDescription());
        dto.setQuantity(req.getQuantity());
        dto.setNotes(req.getNotes());
        dto.setCreatedAt(req.getCreatedAt());
        return dto;
    }
}
