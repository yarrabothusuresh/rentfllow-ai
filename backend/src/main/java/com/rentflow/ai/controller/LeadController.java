package com.rentflow.ai.controller;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.LeadStatus;
import com.rentflow.ai.service.LeadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
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
        return List.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER").contains(role);
    }

    private boolean canWrite(String role) {
        return List.of("OWNER", "ADMIN", "SALES").contains(role);
    }

    @GetMapping
    public ResponseEntity<?> getLeads(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "status", required = false) LeadStatus status,
            @RequestParam(value = "query", required = false) String query) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view leads."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(leadService.searchLeads(tenantId, query));
        } else if (status != null) {
            return ResponseEntity.ok(leadService.getLeadsByStatus(tenantId, status));
        }
        return ResponseEntity.ok(leadService.getLeads(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view lead details."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return leadService.getLeadById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createLead(
            @RequestBody LeadDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to create leads."));
        }

        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "First name is required."));
        }
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email format is required."));
        }
        if (dto.getGuestCount() != null && dto.getGuestCount() < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Guest count must be >= 0."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        LeadDTO created = leadService.createLead(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(
            @PathVariable("id") UUID id,
            @RequestBody LeadDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to update leads."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return leadService.updateLead(tenantId, id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateLeadStatus(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to update lead status."));
        }

        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status field is required."));
        }

        LeadStatus status = LeadStatus.valueOf(statusStr.toUpperCase());
        String tenantId = resolveTenantId(tenantHeader);

        return leadService.updateLeadStatus(tenantId, id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertLead(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) LeadConversionRequest req,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to convert leads."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        try {
            LeadConversionResult result = leadService.convertLead(tenantId, id, req);
            if ("DUPLICATE_FOUND".equals(result.getStatus())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to delete leads."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        boolean deleted = leadService.deleteLead(tenantId, id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
