package com.rentflow.portal.controller;

import com.rentflow.rentalrequest.dto.RentalRequestDTO;
import com.rentflow.rentalrequest.service.RentalRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/rental-requests")
public class PublicCheckoutController {

    private final RentalRequestService rentalRequestService;

    public PublicCheckoutController(RentalRequestService rentalRequestService) {
        this.rentalRequestService = rentalRequestService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader.trim() : "tenant-evergreen";
    }

    @PostMapping
    public ResponseEntity<RentalRequestDTO> submitRentalRequest(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestBody RentalRequestDTO dto) {

        String tenantId = resolveTenantId(tenantHeader);

        // Header takes precedence over body if provided
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            dto.setIdempotencyKey(idempotencyKeyHeader.trim());
        }

        if (dto.getCustomerEmail() == null || !dto.getCustomerEmail().contains("@")) {
            throw new IllegalArgumentException("A valid customer email is required.");
        }

        RentalRequestDTO created = rentalRequestService.createRentalRequest(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalRequestDTO> getRentalRequestById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        String tenantId = resolveTenantId(tenantHeader);
        return rentalRequestService.getRentalRequestById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
