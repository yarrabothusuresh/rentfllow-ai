package com.rentflow.ai.controller;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.calendar.dto.ConflictResponseDTO;
import com.rentflow.calendar.service.ConflictDetectionService;
import com.rentflow.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final ConflictDetectionService conflictDetectionService;

    public AvailabilityController(AvailabilityService availabilityService,
                                 ConflictDetectionService conflictDetectionService) {
        this.availabilityService = availabilityService;
        this.conflictDetectionService = conflictDetectionService;
    }

    @GetMapping("/api/availability")
    public ResponseEntity<AvailabilityResultDTO> getAvailability(
            @RequestParam UUID productId,
            @RequestParam int quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, quantity, start, end);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/availability/bulk")
    public ResponseEntity<BulkAvailabilityResultDTO> checkBulkAvailability(
            @RequestBody BulkAvailabilityRequestDTO request) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        BulkAvailabilityResultDTO bulkResult = availabilityService.checkBulkAvailability(tenantId, request);
        return ResponseEntity.ok(bulkResult);
    }

    @GetMapping("/api/availability/matrix")
    public ResponseEntity<AvailabilityMatrixDTO> getAvailabilityMatrix() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        AvailabilityMatrixDTO matrix = availabilityService.getAvailabilityMatrix(tenantId);
        return ResponseEntity.ok(matrix);
    }

    @GetMapping("/api/availability/alternatives")
    public ResponseEntity<AlternativeSuggestionsDTO> getAlternativeSuggestions(
            @RequestParam UUID productId,
            @RequestParam int quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        AlternativeSuggestionsDTO alternatives = availabilityService.getAlternativeSuggestions(tenantId, productId, quantity, start, end);
        return ResponseEntity.ok(alternatives);
    }

    @GetMapping("/api/availability/products/{id}/timeline")
    public ResponseEntity<List<AvailabilityResultDTO>> getProductTimeline(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "14") int days) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        List<AvailabilityResultDTO> timeline = availabilityService.getProductAvailabilityTimeline(tenantId, id, days);
        return ResponseEntity.ok(timeline);
    }

    @PostMapping("/api/bookings/check-conflicts")
    public ResponseEntity<ConflictResponseDTO> checkBookingConflicts(@RequestBody BookingConflictCheckDTO request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ConflictResponseDTO response = conflictDetectionService.checkBookingConflict(tenantId, request);
        return ResponseEntity.ok(response);
    }
}
