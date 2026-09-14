package com.rentflow.warehouse.controller;

import com.rentflow.warehouse.dto.*;
import com.rentflow.warehouse.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/orders/from-booking/{bookingId}")
    public ResponseEntity<?> createOrderFromBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.createOrderFromBooking(tenantId, bookingId, userRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            List<WarehouseOrderDTO> list = warehouseService.getOrders(tenantId, status, priority, search, userRole);
            return ResponseEntity.ok(list);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            return warehouseService.getOrderById(tenantId, id, userRole)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/start-picking")
    public ResponseEntity<?> startPicking(
            @PathVariable UUID id,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.startPicking(tenantId, id, userId, userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/items/{itemId}/pick")
    public ResponseEntity<?> pickItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody PickItemRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.pickItem(tenantId, id, itemId, request.getQuantity(), request.getNotes(), request.isShortage(), userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/complete-picking")
    public ResponseEntity<?> completePicking(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteWorkflowRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            boolean confirm = request != null && request.isConfirmShortage();
            WarehouseOrderDTO dto = warehouseService.completePicking(tenantId, id, confirm, userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/start-packing")
    public ResponseEntity<?> startPacking(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.startPacking(tenantId, id, userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/items/{itemId}/pack")
    public ResponseEntity<?> packItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody PackItemRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.packItem(tenantId, id, itemId, request.getQuantity(), request.getNotes(), userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/complete-packing")
    public ResponseEntity<?> completePacking(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteWorkflowRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            boolean confirm = request != null && request.isConfirmShortage();
            WarehouseOrderDTO dto = warehouseService.completePacking(tenantId, id, confirm, userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}/assign")
    public ResponseEntity<?> assignOrder(
            @PathVariable UUID id,
            @RequestBody AssignWarehouseOrderDTO request,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseOrderDTO dto = warehouseService.assignOrder(tenantId, id, request.getUserId(), userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole) {
        try {
            WarehouseDashboardDTO dto = warehouseService.getDashboard(tenantId, userRole);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getLocations(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "tenant-evergreen") String tenantId) {
        List<WarehouseLocationDTO> locations = warehouseService.getLocations(tenantId);
        return ResponseEntity.ok(locations);
    }
}
