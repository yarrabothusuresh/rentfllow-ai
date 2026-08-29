package com.rentflow.inventory.controller;

import com.rentflow.inventory.dto.CycleCountRequestDTO;
import com.rentflow.inventory.model.InventoryCycleCount;
import com.rentflow.inventory.model.InventoryCycleCountItem;
import com.rentflow.inventory.service.CycleCountService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/counts")
public class InventoryCycleCountController {

    private final CycleCountService cycleCountService;

    public InventoryCycleCountController(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryCycleCount>> getCycleCounts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(cycleCountService.getCycleCounts(tenantId));
    }

    @PostMapping
    public ResponseEntity<InventoryCycleCount> createCycleCount(@RequestBody CycleCountRequestDTO request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(cycleCountService.createCycleCount(tenantId, request, username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryCycleCount> getById(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(cycleCountService.getById(tenantId, id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<InventoryCycleCountItem>> getItems(@PathVariable UUID id) {
        return ResponseEntity.ok(cycleCountService.getItems(id));
    }

    @PostMapping("/{id}/record")
    public ResponseEntity<InventoryCycleCount> recordCounts(@PathVariable UUID id,
                                                             @RequestBody List<CycleCountRequestDTO.CountItemDTO> itemCounts) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(cycleCountService.recordCounts(tenantId, id, itemCounts));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<InventoryCycleCount> completeCycleCount(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(cycleCountService.completeCycleCount(tenantId, id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<InventoryCycleCount> approveCycleCount(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(cycleCountService.approveCycleCount(tenantId, id, username));
    }
}
