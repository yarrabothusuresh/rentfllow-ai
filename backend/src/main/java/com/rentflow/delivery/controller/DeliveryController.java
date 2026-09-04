package com.rentflow.delivery.controller;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.delivery.dto.*;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.service.DeliveryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private String resolveTenantId(String headerTenantId) {
        return (headerTenantId != null && !headerTenantId.trim().isEmpty()) ? headerTenantId : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    @PostMapping("/deliveries/from-warehouse-order/{warehouseOrderId}")
    public ResponseEntity<DeliveryDTO> createFromWarehouseOrder(
            @PathVariable("warehouseOrderId") UUID warehouseOrderId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createFromWarehouseOrder(tenantId, warehouseOrderId, userNameHeader));
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDTO>> getDeliveries(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) DeliveryStatus status,
            @RequestParam(value = "driverId", required = false) UUID driverId,
            @RequestParam(value = "vehicleId", required = false) UUID vehicleId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getDeliveries(tenantId, date, status, driverId, vehicleId));
    }

    @GetMapping("/deliveries/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        if ("CUSTOMER".equalsIgnoreCase(roleHeader) && customerIdHeader != null && !customerIdHeader.trim().isEmpty()) {
            return ResponseEntity.ok(deliveryService.getSanitizedDeliveryForCustomer(tenantId, UUID.fromString(customerIdHeader), id));
        }
        return ResponseEntity.ok(deliveryService.getDeliveryById(tenantId, id));
    }

    @PatchMapping("/deliveries/{id}/schedule")
    public ResponseEntity<DeliveryDTO> scheduleDelivery(
            @PathVariable("id") UUID id,
            @RequestBody ScheduleDeliveryDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.scheduleDelivery(tenantId, id, request, userNameHeader));
    }

    @PatchMapping("/deliveries/{id}/driver")
    public ResponseEntity<DeliveryDTO> assignDriver(
            @PathVariable("id") UUID id,
            @RequestBody AssignDriverDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.assignDriver(tenantId, id, request.getDriverId(), userNameHeader));
    }

    @PatchMapping("/deliveries/{id}/vehicle")
    public ResponseEntity<DeliveryDTO> assignVehicle(
            @PathVariable("id") UUID id,
            @RequestBody AssignVehicleDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.assignVehicle(tenantId, id, request.getVehicleId(), userNameHeader));
    }

    @PostMapping("/deliveries/{id}/start")
    public ResponseEntity<DeliveryDTO> startDelivery(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.startDelivery(tenantId, id, userNameHeader));
    }

    @PostMapping("/deliveries/{id}/arrive")
    public ResponseEntity<DeliveryDTO> arriveDelivery(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.arriveDelivery(tenantId, id, userNameHeader));
    }

    @PostMapping("/deliveries/{id}/start-setup")
    public ResponseEntity<DeliveryDTO> startSetup(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.startSetup(tenantId, id, userNameHeader));
    }

    @PostMapping("/deliveries/{id}/complete")
    public ResponseEntity<DeliveryDTO> completeDelivery(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.completeDelivery(tenantId, id, userNameHeader));
    }

    @PostMapping("/deliveries/{id}/fail")
    public ResponseEntity<DeliveryDTO> failDelivery(
            @PathVariable("id") UUID id,
            @RequestBody FailDeliveryDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.failDelivery(tenantId, id, request, userNameHeader));
    }

    @GetMapping("/deliveries/dashboard")
    public ResponseEntity<DeliveryDashboardDTO> getDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getDashboardMetrics(tenantId));
    }

    @GetMapping("/deliveries/calendar")
    public ResponseEntity<List<DeliveryDTO>> getCalendar(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(deliveryService.getDeliveries(tenantId, targetDate, null, null, null));
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverDTO>> getDrivers(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getDrivers(tenantId));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDTO>> getVehicles(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getVehicles(tenantId));
    }

    @PostMapping("/delivery-routes")
    public ResponseEntity<DeliveryRouteDTO> createRoute(
            @RequestBody CreateRouteDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createRoute(tenantId, request, userNameHeader));
    }

    @GetMapping("/delivery-routes")
    public ResponseEntity<List<DeliveryRouteDTO>> getRoutes(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getRoutes(tenantId, date));
    }

    @GetMapping("/delivery-routes/{id}")
    public ResponseEntity<DeliveryRouteDTO> getRouteById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.getRouteById(tenantId, id));
    }

    @PatchMapping("/delivery-routes/{id}/sequence")
    public ResponseEntity<DeliveryRouteDTO> updateRouteSequence(
            @PathVariable("id") UUID id,
            @RequestBody SequenceUpdateDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(deliveryService.updateRouteSequence(tenantId, id, request, userNameHeader));
    }
}
