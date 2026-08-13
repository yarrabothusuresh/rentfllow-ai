package com.rentflow.event;

import com.rentflow.event.dto.CreateEventRequest;
import com.rentflow.event.dto.EventDTO;
import com.rentflow.workflow.model.EventStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventRepository eventRepository;
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody CreateEventRequest request) {
        UUID tenantId = request.getTenantId() != null ? request.getTenantId() : DEFAULT_TENANT_ID;

        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTenantId(tenantId);
        event.setCustomerId(request.getCustomerId() != null ? request.getCustomerId() : UUID.randomUUID());
        event.setEventName(request.getEventName() != null ? request.getEventName() : "Untitled Event");
        event.setEventType(request.getEventType() != null ? request.getEventType() : "General Event");
        event.setEventDate(request.getEventDate() != null ? request.getEventDate() : LocalDate.of(2026, 9, 20));
        event.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalTime.of(14, 0));
        event.setEndTime(request.getEndTime() != null ? request.getEndTime() : LocalTime.of(23, 0));
        event.setGuestCount(request.getGuestCount() != null ? request.getGuestCount() : 100);
        event.setVenueName(request.getVenueName());
        event.setVenueAddress(request.getVenueAddress());
        event.setSpecialInstructions(request.getSpecialInstructions());
        event.setStatus(EventStatus.PLANNING);

        Event saved = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents(@RequestParam(required = false) UUID tenantId) {
        UUID effectiveTenantId = tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        List<Event> events = eventRepository.findByTenantId(effectiveTenantId);

        // If repo is empty, return a default seed list for demo purposes
        if (events.isEmpty()) {
            Event demoEvent = new Event(
                    UUID.fromString("d3b07384-d113-4601-a71f-488667c48564"),
                    effectiveTenantId,
                    UUID.fromString("66666666-6666-6666-6666-666666666666"),
                    "Brown Wedding Reception",
                    "Wedding",
                    LocalDate.of(2026, 9, 20),
                    LocalTime.of(15, 0),
                    LocalTime.of(23, 0),
                    250,
                    "Evergreen Event Pavilion",
                    "Dallas, Texas",
                    "Deliver chairs to rear courtyard by 10 AM",
                    EventStatus.QUOTED
            );
            eventRepository.save(demoEvent);
            events = List.of(demoEvent);
        }

        List<EventDTO> dtos = events.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + id));
        return ResponseEntity.ok(mapToDTO(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable UUID id, @RequestBody EventDTO updateDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + id));

        if (updateDTO.getEventName() != null) event.setEventName(updateDTO.getEventName());
        if (updateDTO.getEventType() != null) event.setEventType(updateDTO.getEventType());
        if (updateDTO.getEventDate() != null) event.setEventDate(updateDTO.getEventDate());
        if (updateDTO.getStartTime() != null) event.setStartTime(updateDTO.getStartTime());
        if (updateDTO.getEndTime() != null) event.setEndTime(updateDTO.getEndTime());
        if (updateDTO.getGuestCount() != null) event.setGuestCount(updateDTO.getGuestCount());
        if (updateDTO.getVenueName() != null) event.setVenueName(updateDTO.getVenueName());
        if (updateDTO.getVenueAddress() != null) event.setVenueAddress(updateDTO.getVenueAddress());
        if (updateDTO.getSpecialInstructions() != null) event.setSpecialInstructions(updateDTO.getSpecialInstructions());
        if (updateDTO.getStatus() != null) event.setStatus(updateDTO.getStatus());

        Event saved = eventRepository.save(event);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    private EventDTO mapToDTO(Event entity) {
        EventDTO dto = new EventDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setEventName(entity.getEventName());
        dto.setEventType(entity.getEventType());
        dto.setEventDate(entity.getEventDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setGuestCount(entity.getGuestCount());
        dto.setVenueName(entity.getVenueName());
        dto.setVenueAddress(entity.getVenueAddress());
        dto.setSpecialInstructions(entity.getSpecialInstructions());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
