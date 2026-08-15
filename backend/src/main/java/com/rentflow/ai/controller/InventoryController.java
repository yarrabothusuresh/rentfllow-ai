package com.rentflow.ai.controller;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.ai.service.InventoryService;
import com.rentflow.ai.service.ProductService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public InventoryController(InventoryService inventoryService,
                               AvailabilityService availabilityService,
                               ProductService productService) {
        this.inventoryService = inventoryService;
        this.availabilityService = availabilityService;
        this.productService = productService;
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
            // Only WAREHOUSE, ADMIN, OWNER can adjust inventory directly. SALES, DRIVER, CUSTOMER are 403 FORBIDDEN.
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

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<?> checkAvailability(
            @PathVariable("productId") UUID productId,
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(
            @RequestBody InventoryReservationDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            InventoryReservationDTO created = inventoryService.createReservation(tenantId, dto, userId != null ? userId : role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
