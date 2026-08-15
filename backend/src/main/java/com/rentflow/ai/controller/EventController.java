package com.rentflow.ai.controller;

import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.EventRequirementDTO;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController("crmEventController")
@RequestMapping("/api/crm/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    private String resolveTenantId(String tenantIdHeader) {
        return (tenantIdHeader != null && !tenantIdHeader.isBlank())
                ? tenantIdHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
    }

    private boolean canRead(String role) {
        return List.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER").contains(role);
    }

    private boolean canWrite(String role) {
        return List.of("OWNER", "ADMIN", "SALES").contains(role);
    }

    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view events."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(eventService.getEvents(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view event details."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return eventService.getEventById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody EventDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to create events."));
        }

        if (dto.getEventName() == null || dto.getEventName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Event name is required."));
        }
        if (dto.getCustomerId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Customer ID is required."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        EventDTO created = eventService.createEvent(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable("id") UUID id,
            @RequestBody EventDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to update events."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return eventService.updateEvent(tenantId, id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Requirements Endpoints
    @GetMapping("/{eventId}/requirements")
    public ResponseEntity<?> getRequirements(
            @PathVariable("eventId") UUID eventId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view requirements."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(eventService.getRequirements(tenantId, eventId));
    }

    @PostMapping("/{eventId}/requirements")
    public ResponseEntity<?> addRequirement(
            @PathVariable("eventId") UUID eventId,
            @RequestBody EventRequirementDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to add event requirements."));
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is required."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        EventRequirementDTO created = eventService.addRequirement(tenantId, eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{eventId}/requirements/{requirementId}")
    public ResponseEntity<?> updateRequirement(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("requirementId") UUID requirementId,
            @RequestBody EventRequirementDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to update event requirements."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return eventService.updateRequirement(tenantId, eventId, requirementId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{eventId}/requirements/{requirementId}")
    public ResponseEntity<?> deleteRequirement(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("requirementId") UUID requirementId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to delete event requirements."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        boolean deleted = eventService.deleteRequirement(tenantId, eventId, requirementId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
