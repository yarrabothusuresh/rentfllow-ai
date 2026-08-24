package com.rentflow.ai.controller;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.ai.service.InventoryService;
import com.rentflow.ai.service.ProductService;
import com.rentflow.ai.service.InventoryAlternativeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(originPatterns = "*")
public class InventoryController {

    private final InventoryService inventoryService;
    private final AvailabilityService availabilityService;
    private final ProductService productService;

    private final InventoryAlternativeService alternativeService;

    public InventoryController(InventoryService inventoryService,
                               AvailabilityService availabilityService,
                               ProductService productService,
                               InventoryAlternativeService alternativeService) {
        this.inventoryService = inventoryService;
        this.availabilityService = availabilityService;
        this.productService = productService;
        this.alternativeService = alternativeService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank())
                ? tenantHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            return ResponseEntity.ok(inventoryService.getSummary(tenantId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        return getSummary(tenantHeader);
    }

    @GetMapping("/products")
    public ResponseEntity<?> getInventoryProducts(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return ResponseEntity.ok(productService.getProducts(tenantId, role));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getInventoryProduct(
            @PathVariable("productId") UUID productId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return productService.getProductById(tenantId, productId, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<?> adjustInventory(
            @PathVariable("productId") UUID productId,
            @RequestBody InventoryAdjustmentRequest request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            String role = resolveRole(roleHeader);
            if (!List.of("OWNER", "ADMIN", "WAREHOUSE").contains(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Sales, Driver, and Customer roles cannot adjust inventory directly."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            ProductDTO updated = inventoryService.adjustInventory(tenantId, productId, request, userId != null ? userId : role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/products/{productId}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable("productId") UUID productId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            return ResponseEntity.ok(inventoryService.getTransactions(tenantId, productId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(
            @RequestParam("productId") UUID productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestParam("startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam("endDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, quantity, startDateTime, endDateTime);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/availability/check")
    public ResponseEntity<?> checkBulkAvailability(
            @RequestBody BulkAvailabilityRequestDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            BulkAvailabilityResultDTO result = availabilityService.checkBulkAvailability(tenantId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<?> checkProductAvailabilityTimeline(
            @PathVariable("productId") UUID productId,
            @RequestParam(value = "quantity", defaultValue = "1", required = false) int quantity,
            @RequestParam(value = "startDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam(value = "days", defaultValue = "7", required = false) int days,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            if (startDateTime != null && endDateTime != null) {
                AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, quantity, startDateTime, endDateTime);
                return ResponseEntity.ok(result);
            } else {
                List<AvailabilityResultDTO> timeline = availabilityService.getProductAvailabilityTimeline(tenantId, productId, days);
                return ResponseEntity.ok(timeline);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/products/{productId}/alternatives")
    public ResponseEntity<?> getAlternatives(
            @PathVariable("productId") UUID productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestParam(value = "startDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            LocalDateTime start = startDateTime != null ? startDateTime : LocalDateTime.now();
            LocalDateTime end = endDateTime != null ? endDateTime : start.plusDays(1);
            return ResponseEntity.ok(alternativeService.findAlternatives(tenantId, productId, start, end, quantity));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<?> getReservations(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            return ResponseEntity.ok(inventoryService.getReservations(tenantId, status, search));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getReservationById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            return inventoryService.getReservationById(tenantId, id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(
            @RequestBody Object dtoOrBatch,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            String user = userId != null ? userId : role;

            if (dtoOrBatch instanceof Map<?,?> map && map.containsKey("items")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.findAndRegisterModules();
                BatchReservationRequestDTO batch = mapper.convertValue(map, BatchReservationRequestDTO.class);
                List<InventoryReservationDTO> created = inventoryService.createBatchReservations(tenantId, batch, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
            } else {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.findAndRegisterModules();
                InventoryReservationDTO single = mapper.convertValue(dtoOrBatch, InventoryReservationDTO.class);
                InventoryReservationDTO created = inventoryService.createReservation(tenantId, single, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
            }
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(Map.of("error", rse.getReason()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/reservations/{id}/release")
    public ResponseEntity<?> releaseReservation(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String user = userId != null ? userId : resolveRole(roleHeader);
            return inventoryService.releaseReservation(tenantId, id, user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/conflicts")
    public ResponseEntity<?> getConflicts(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            List<InventoryConflictDTO> conflicts = inventoryService.getConflicts(tenantId);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
