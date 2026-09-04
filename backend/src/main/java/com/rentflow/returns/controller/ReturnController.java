package com.rentflow.returns.controller;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.returns.dto.*;
import com.rentflow.returns.model.ReturnOrderStatus;
import com.rentflow.returns.model.ReturnPriority;
import com.rentflow.returns.service.ReturnService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    private String resolveTenantId(String headerTenantId) {
        return (headerTenantId != null && !headerTenantId.trim().isEmpty()) ? headerTenantId : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    // 1. Create Return from Booking
    @PostMapping("/returns/from-booking/{bookingId}")
    public ResponseEntity<ReturnOrderDTO> createFromBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createFromBooking(tenantId, bookingId, userNameHeader));
    }

    // 2. Query Returns
    @GetMapping("/returns")
    public ResponseEntity<List<ReturnOrderDTO>> getReturns(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) ReturnOrderStatus status,
            @RequestParam(value = "driverId", required = false) UUID driverId,
            @RequestParam(value = "customerId", required = false) UUID customerId,
            @RequestParam(value = "priority", required = false) ReturnPriority priority,
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getReturns(tenantId, date, status, driverId, customerId, priority, search));
    }

    // 3. Return Dashboards
    @GetMapping("/returns/dashboard")
    public ResponseEntity<ReturnsDashboardDTO> getReturnsDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getReturnsDashboard(tenantId));
    }

    @GetMapping("/returns/inspection")
    public ResponseEntity<InspectionDashboardDTO> getInspectionDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getInspectionDashboard(tenantId));
    }

    @GetMapping("/inventory/damage")
    public ResponseEntity<List<DamageRecordDTO>> getDamageRecords(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getDamageRecords(tenantId));
    }

    // 4. Return Detail & Customer Summary
    @GetMapping("/returns/{id}")
    public ResponseEntity<ReturnOrderDTO> getReturnById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getReturnById(tenantId, id));
    }

    @GetMapping("/returns/{id}/summary")
    public ResponseEntity<CustomerReturnSummaryDTO> getCustomerReturnSummary(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.getCustomerReturnSummary(tenantId, id));
    }

    // 5. Scheduling & Driver / Vehicle Assignment
    @PatchMapping("/returns/{id}/schedule")
    public ResponseEntity<ReturnOrderDTO> scheduleReturn(
            @PathVariable("id") UUID id,
            @RequestBody ScheduleReturnDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.scheduleReturn(tenantId, id, request, userNameHeader));
    }

    @PatchMapping("/returns/{id}/driver")
    public ResponseEntity<ReturnOrderDTO> assignDriver(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID driverId = UUID.fromString(body.get("driverId"));
        return ResponseEntity.ok(returnService.assignDriver(tenantId, id, driverId, userNameHeader));
    }

    @PatchMapping("/returns/{id}/vehicle")
    public ResponseEntity<ReturnOrderDTO> assignVehicle(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID vehicleId = UUID.fromString(body.get("vehicleId"));
        return ResponseEntity.ok(returnService.assignVehicle(tenantId, id, vehicleId, userNameHeader));
    }

    // 6. Pickup Lifecycle
    @PostMapping("/returns/{id}/start")
    public ResponseEntity<ReturnOrderDTO> startPickup(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.startPickup(tenantId, id, userNameHeader));
    }

    @PostMapping("/returns/{id}/arrive")
    public ResponseEntity<ReturnOrderDTO> arrivePickup(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.arrivePickup(tenantId, id, userNameHeader));
    }

    @PostMapping("/returns/{id}/pickup-complete")
    public ResponseEntity<ReturnOrderDTO> pickupComplete(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.pickupComplete(tenantId, id, userNameHeader));
    }

    // 7. Check-in Lifecycle
    @PostMapping("/returns/{id}/start-check-in")
    public ResponseEntity<ReturnOrderDTO> startCheckIn(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.startCheckIn(tenantId, id, userNameHeader));
    }

    @PostMapping("/returns/{id}/check-in")
    public ResponseEntity<ReturnOrderDTO> recordCheckIn(
            @PathVariable("id") UUID id,
            @RequestBody CheckInRequestDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.recordCheckIn(tenantId, id, request, userNameHeader));
    }

    // 8. Inspection Lifecycle
    @PostMapping("/returns/{id}/start-inspection")
    public ResponseEntity<ReturnOrderDTO> startInspection(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.startInspection(tenantId, id, userNameHeader));
    }

    @PostMapping("/returns/{id}/inspection")
    public ResponseEntity<ReturnOrderDTO> recordInspection(
            @PathVariable("id") UUID id,
            @RequestBody InspectionRequestDTO request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.recordInspection(tenantId, id, request, userNameHeader));
    }

    // 9. Completion & Reconciliation
    @PostMapping("/returns/{id}/complete")
    public ResponseEntity<ReturnOrderDTO> completeReturn(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userNameHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(returnService.completeReturn(tenantId, id, userNameHeader));
    }
}
