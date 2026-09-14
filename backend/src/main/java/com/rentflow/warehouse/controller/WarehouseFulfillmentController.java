package com.rentflow.warehouse.controller;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.warehouse.dto.*;
import com.rentflow.warehouse.model.PackingContainer;
import com.rentflow.warehouse.service.WarehouseFulfillmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseFulfillmentController {

    private final WarehouseFulfillmentService fulfillmentService;
    private final com.rentflow.security.CurrentUserService currentUserService;

    public WarehouseFulfillmentController(WarehouseFulfillmentService fulfillmentService,
                                          com.rentflow.security.CurrentUserService currentUserService) {
        this.fulfillmentService = fulfillmentService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenant(String ignoredHeader) {
        return currentUserService.requireTenantId();
    }

    private String resolveRole(String ignoredHeader) {
        return currentUserService.requireRole();
    }

    // ==========================================
    // 1. PICK LIST ENDPOINTS
    // ==========================================

    @PostMapping("/orders/{orderId}/pick-list")
    public ResponseEntity<PickListDTO> generatePickList(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.generatePickList(resolveTenant(tenantHeader), orderId, resolveRole(roleHeader)));
    }

    @GetMapping("/pick-lists")
    public ResponseEntity<List<PickListDTO>> getPickLists(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedTo,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.getPickLists(resolveTenant(tenantHeader), status, priority, assignedTo, resolveRole(roleHeader)));
    }

    @GetMapping("/pick-lists/{id}")
    public ResponseEntity<PickListDTO> getPickListById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.getPickListById(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/start")
    public ResponseEntity<PickListDTO> startPicking(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        String assignedTo = body != null ? body.get("assignedTo") : null;
        return ResponseEntity.ok(fulfillmentService.startPicking(resolveTenant(tenantHeader), id, assignedTo, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/scan")
    public ResponseEntity<PickScanResponseDTO> scanPickItem(
            @PathVariable UUID id,
            @RequestBody PickScanRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.scanPickItem(resolveTenant(tenantHeader), id, request.getCode(), resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/items/{itemId}/pick")
    public ResponseEntity<PickListDTO> pickItemQuantity(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String notes = body.get("notes") != null ? body.get("notes").toString() : null;
        return ResponseEntity.ok(fulfillmentService.pickQuantity(resolveTenant(tenantHeader), id, itemId, quantity, notes, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/items/{itemId}/shortage")
    public ResponseEntity<WarehouseExceptionDTO> reportShortage(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        int shortQty = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String reason = body.get("reason") != null ? body.get("reason").toString() : "NOT_FOUND";
        String desc = body.get("description") != null ? body.get("description").toString() : "Short pick";
        return ResponseEntity.ok(fulfillmentService.reportShortage(resolveTenant(tenantHeader), id, itemId, shortQty, reason, desc, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/items/{itemId}/damage")
    public ResponseEntity<WarehouseExceptionDTO> reportDamage(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        UUID assetId = body.get("inventoryItemId") != null ? UUID.fromString(body.get("inventoryItemId").toString()) : null;
        String desc = body.get("description") != null ? body.get("description").toString() : "Damaged during pick";
        return ResponseEntity.ok(fulfillmentService.reportDamage(resolveTenant(tenantHeader), id, itemId, assetId, desc, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/complete")
    public ResponseEntity<PickListDTO> completePickList(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.completePickList(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/pick-lists/{id}/verify")
    public ResponseEntity<WarehouseOrderDTO> verifyPickList(
            @PathVariable UUID id,
            @RequestBody(required = false) PickVerificationRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.verifyPickList(resolveTenant(tenantHeader), id, request, resolveRole(roleHeader)));
    }

    // ==========================================
    // 2. PACK LIST ENDPOINTS
    // ==========================================

    @PostMapping("/orders/{orderId}/pack-list")
    public ResponseEntity<PackListDTO> generatePackList(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.generatePackList(resolveTenant(tenantHeader), orderId, resolveRole(roleHeader)));
    }

    @GetMapping("/pack-lists")
    public ResponseEntity<List<PackListDTO>> getPackLists(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getPackLists(resolveTenant(tenantHeader), status));
    }

    @GetMapping("/pack-lists/{id}")
    public ResponseEntity<PackListDTO> getPackListById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getPackListById(resolveTenant(tenantHeader), id));
    }

    @PostMapping("/pack-lists/{id}/start")
    public ResponseEntity<PackListDTO> startPacking(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.startPacking(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/pack-lists/{id}/items/{itemId}/pack")
    public ResponseEntity<PackListDTO> packItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        int qty = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String container = body.get("containerCode") != null ? body.get("containerCode").toString() : null;
        String notes = body.get("notes") != null ? body.get("notes").toString() : null;
        return ResponseEntity.ok(fulfillmentService.packItem(resolveTenant(tenantHeader), id, itemId, qty, container, notes, resolveRole(roleHeader)));
    }

    @PostMapping("/pack-lists/{id}/complete")
    public ResponseEntity<PackListDTO> completePackList(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.completePackList(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    // ==========================================
    // 3. LOAD LIST & HANDOFF ENDPOINTS
    // ==========================================

    @PostMapping("/orders/{orderId}/load-list")
    public ResponseEntity<LoadListDTO> generateLoadList(
            @PathVariable UUID orderId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        UUID delId = (body != null && body.get("deliveryId") != null) ? UUID.fromString(body.get("deliveryId")) : null;
        UUID vehId = (body != null && body.get("vehicleId") != null) ? UUID.fromString(body.get("vehicleId")) : null;
        UUID drvId = (body != null && body.get("driverId") != null) ? UUID.fromString(body.get("driverId")) : null;
        return ResponseEntity.ok(fulfillmentService.generateLoadList(resolveTenant(tenantHeader), orderId, delId, vehId, drvId, resolveRole(roleHeader)));
    }

    @GetMapping("/load-lists")
    public ResponseEntity<List<LoadListDTO>> getLoadLists(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getLoadLists(resolveTenant(tenantHeader), status));
    }

    @GetMapping("/load-lists/{id}")
    public ResponseEntity<LoadListDTO> getLoadListById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getLoadListById(resolveTenant(tenantHeader), id));
    }

    @PostMapping("/load-lists/{id}/start")
    public ResponseEntity<LoadListDTO> startLoading(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.startLoading(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/load-lists/{id}/items/{itemId}/load")
    public ResponseEntity<LoadListDTO> loadItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        int qty = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String notes = body.get("notes") != null ? body.get("notes").toString() : null;
        return ResponseEntity.ok(fulfillmentService.loadItem(resolveTenant(tenantHeader), id, itemId, qty, notes, resolveRole(roleHeader)));
    }

    @PostMapping("/load-lists/{id}/verify")
    public ResponseEntity<LoadListDTO> verifyLoad(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.verifyLoad(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/load-lists/{id}/handoff")
    public ResponseEntity<LoadListDTO> driverHandoff(
            @PathVariable UUID id,
            @RequestBody(required = false) LoadHandoffRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.driverHandoff(resolveTenant(tenantHeader), id, request, resolveRole(roleHeader)));
    }

    // ==========================================
    // 4. EXCEPTIONS & SUBSTITUTIONS
    // ==========================================

    @GetMapping("/exceptions")
    public ResponseEntity<List<WarehouseExceptionDTO>> getExceptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String type,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getExceptions(resolveTenant(tenantHeader), status, severity, type));
    }

    @PostMapping("/exceptions")
    public ResponseEntity<WarehouseExceptionDTO> createException(
            @RequestBody CreateExceptionRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.createException(resolveTenant(tenantHeader), request, resolveRole(roleHeader)));
    }

    @PostMapping("/exceptions/{id}/resolve")
    public ResponseEntity<WarehouseExceptionDTO> resolveException(
            @PathVariable UUID id,
            @RequestBody(required = false) ResolveExceptionRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.resolveException(resolveTenant(tenantHeader), id, request, resolveRole(roleHeader)));
    }

    @GetMapping("/substitutions")
    public ResponseEntity<List<WarehouseSubstitutionDTO>> getSubstitutions(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getSubstitutions(resolveTenant(tenantHeader)));
    }

    @PostMapping("/substitutions")
    public ResponseEntity<WarehouseSubstitutionDTO> proposeSubstitution(
            @RequestBody ProposeSubstitutionRequestDTO request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.proposeSubstitution(resolveTenant(tenantHeader), request, resolveRole(roleHeader)));
    }

    @PostMapping("/substitutions/{id}/approve")
    public ResponseEntity<WarehouseSubstitutionDTO> approveSubstitution(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.approveSubstitution(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    @PostMapping("/substitutions/{id}/reject")
    public ResponseEntity<WarehouseSubstitutionDTO> rejectSubstitution(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.rejectSubstitution(resolveTenant(tenantHeader), id, resolveRole(roleHeader)));
    }

    // ==========================================
    // 5. CONTAINERS & CHECKLISTS
    // ==========================================

    @GetMapping("/containers")
    public ResponseEntity<List<PackingContainer>> getContainers(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getContainers(resolveTenant(tenantHeader)));
    }

    @PostMapping("/containers")
    public ResponseEntity<PackingContainer> createContainer(
            @RequestBody PackingContainer container,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.createContainer(resolveTenant(tenantHeader), container));
    }

    @GetMapping("/checklists/{orderId}")
    public ResponseEntity<List<WarehouseChecklistDTO>> getOrderChecklists(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getOrderChecklists(resolveTenant(tenantHeader), orderId));
    }

    @PostMapping("/checklists/{checklistId}/toggle")
    public ResponseEntity<WarehouseChecklistDTO> toggleChecklistItem(
            @PathVariable UUID checklistId,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        boolean completed = body.get("completed") != null && body.get("completed");
        return ResponseEntity.ok(fulfillmentService.toggleChecklistItem(resolveTenant(tenantHeader), checklistId, completed, resolveRole(roleHeader)));
    }

    // ==========================================
    // 6. OPERATOR MY WORK & PRODUCTIVITY METRICS
    // ==========================================

    @GetMapping("/my-work")
    public ResponseEntity<MyWorkDTO> getMyWork(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return ResponseEntity.ok(fulfillmentService.getMyWork(resolveTenant(tenantHeader), resolveRole(roleHeader)));
    }

    @GetMapping("/metrics")
    public ResponseEntity<WarehouseMetricsDTO> getMetrics(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        return ResponseEntity.ok(fulfillmentService.getMetrics(resolveTenant(tenantHeader)));
    }
}
