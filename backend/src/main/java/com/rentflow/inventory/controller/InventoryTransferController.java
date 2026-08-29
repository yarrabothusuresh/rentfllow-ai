package com.rentflow.inventory.controller;

import com.rentflow.inventory.dto.InventoryTransferDTO;
import com.rentflow.inventory.service.WarehouseTransferService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/transfers")
public class InventoryTransferController {

    private final WarehouseTransferService transferService;

    public InventoryTransferController(WarehouseTransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryTransferDTO>> getTransfers() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(transferService.getTransfers(tenantId));
    }

    @PostMapping
    public ResponseEntity<InventoryTransferDTO> createTransfer(@RequestBody InventoryTransferDTO request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(transferService.createTransfer(tenantId, request, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransferDTO> getTransferById(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(transferService.getTransferById(tenantId, id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<InventoryTransferDTO> approveTransfer(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(transferService.approveTransfer(tenantId, id, username));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<InventoryTransferDTO> shipTransfer(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(transferService.shipTransfer(tenantId, id, username));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<InventoryTransferDTO> receiveTransfer(@PathVariable UUID id,
                                                                @RequestBody(required = false) List<InventoryTransferDTO.TransferItemDTO> receivedItems) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(transferService.receiveTransfer(tenantId, id, receivedItems, username));
    }
}
