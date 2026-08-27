package com.rentflow.claims.controller;

import com.rentflow.claims.dto.RepairDashboardDTO;
import com.rentflow.claims.dto.RepairOrderDTO;
import com.rentflow.claims.model.RepairOrderStatus;
import com.rentflow.claims.service.RepairService;
import com.rentflow.returns.model.InspectionCondition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = "*")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @PostMapping
    public ResponseEntity<RepairOrderDTO> createRepair(@RequestBody Map<String, Object> body) {
        UUID claimId = UUID.fromString(body.get("claimId").toString());
        UUID productId = UUID.fromString(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", 1).toString());
        String desc = body.getOrDefault("description", "Manual Repair Order").toString();
        BigDecimal cost = body.get("estimatedCost") != null ? new BigDecimal(body.get("estimatedCost").toString()) : BigDecimal.ZERO;
        return ResponseEntity.ok(repairService.createRepairOrder(claimId, productId, quantity, desc, cost));
    }

    @GetMapping
    public ResponseEntity<List<RepairOrderDTO>> getRepairs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RepairOrderStatus status,
            @RequestParam(required = false) UUID productId) {
        return ResponseEntity.ok(repairService.getRepairs(search, status, productId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<RepairDashboardDTO> getDashboard() {
        return ResponseEntity.ok(repairService.getDashboard());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairOrderDTO> getRepairById(@PathVariable UUID id) {
        return ResponseEntity.ok(repairService.getRepairById(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<RepairOrderDTO> startRepair(@PathVariable UUID id) {
        return ResponseEntity.ok(repairService.startRepair(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RepairOrderDTO> completeRepair(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        int qtyRepaired = Integer.parseInt(body.getOrDefault("quantityRepaired", 1).toString());
        InspectionCondition condition = InspectionCondition.valueOf(body.getOrDefault("condition", "GOOD").toString());
        BigDecimal actualCost = body.get("actualCost") != null ? new BigDecimal(body.get("actualCost").toString()) : BigDecimal.ZERO;
        String notes = body.getOrDefault("notes", "Repair completed.").toString();
        return ResponseEntity.ok(repairService.completeRepair(id, qtyRepaired, condition, actualCost, notes));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<RepairOrderDTO> failRepair(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Repair unfeasible / failed.").toString();
        return ResponseEntity.ok(repairService.failRepair(id, reason));
    }
}
