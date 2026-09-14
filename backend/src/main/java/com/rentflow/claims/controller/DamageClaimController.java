package com.rentflow.claims.controller;

import com.rentflow.claims.dto.*;
import com.rentflow.claims.model.ClaimResolution;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.ClaimType;
import com.rentflow.claims.model.ClaimAudit;
import com.rentflow.claims.service.DamageClaimService;
import com.rentflow.claims.service.RepairService;
import com.rentflow.claims.service.ReplacementService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/damage-claims")
public class DamageClaimController {

    private final DamageClaimService claimService;
    private final RepairService repairService;
    private final ReplacementService replacementService;

    public DamageClaimController(DamageClaimService claimService,
                                 RepairService repairService,
                                 ReplacementService replacementService) {
        this.claimService = claimService;
        this.repairService = repairService;
        this.replacementService = replacementService;
    }

    @PostMapping("/from-return/{returnId}")
    public ResponseEntity<DamageClaimDTO> createClaimFromReturn(@PathVariable UUID returnId) {
        return ResponseEntity.ok(claimService.createClaimFromReturn(returnId));
    }

    @GetMapping
    public ResponseEntity<List<DamageClaimDTO>> getClaims(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) ClaimType type,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(claimService.getClaims(search, status, type, customerId, priority));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ClaimDashboardDTO> getDashboard() {
        return ResponseEntity.ok(claimService.getDashboard());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DamageClaimDTO> getClaimById(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    @PostMapping("/{id}/estimate")
    public ResponseEntity<ClaimEstimateDTO> createEstimate(@PathVariable UUID id, @RequestBody CreateEstimateRequestDTO request) {
        return ResponseEntity.ok(claimService.createEstimate(id, request));
    }

    @PostMapping("/{id}/send-to-customer")
    public ResponseEntity<DamageClaimDTO> sendToCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.sendToCustomer(id));
    }

    @PostMapping("/{id}/customer-approve")
    public ResponseEntity<DamageClaimDTO> customerApprove(@PathVariable UUID id) {
        String currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(claimService.customerApprove(id, currentUser));
    }

    @PostMapping("/{id}/customer-dispute")
    public ResponseEntity<DamageClaimDTO> customerDispute(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Customer disputed claim amount or liability.");
        String currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(claimService.customerDispute(id, reason, currentUser));
    }

    @PostMapping("/{id}/waive")
    public ResponseEntity<DamageClaimDTO> waiveClaim(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Waived by management.");
        String currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(claimService.waiveClaim(id, reason, currentUser));
    }

    @PostMapping("/{id}/start-repair")
    public ResponseEntity<RepairOrderDTO> startRepairForClaim(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        UUID productId = UUID.fromString(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", 1).toString());
        String desc = body.getOrDefault("description", "Claim Repair Order").toString();
        BigDecimal cost = body.get("estimatedCost") != null ? new BigDecimal(body.get("estimatedCost").toString()) : BigDecimal.ZERO;
        return ResponseEntity.ok(repairService.createRepairOrder(id, productId, quantity, desc, cost));
    }

    @PostMapping("/{id}/replacement-required")
    public ResponseEntity<ReplacementOrderDTO> replacementRequiredForClaim(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        UUID productId = UUID.fromString(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", 1).toString());
        String reason = body.getOrDefault("reason", "Unrepairable damage / lost item replacement").toString();
        BigDecimal unitCost = body.get("unitCost") != null ? new BigDecimal(body.get("unitCost").toString()) : BigDecimal.valueOf(150.00);
        return ResponseEntity.ok(replacementService.createReplacementOrder(id, productId, quantity, reason, unitCost));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<DamageClaimDTO> resolveClaim(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        ClaimResolution resolution = ClaimResolution.valueOf(body.getOrDefault("resolution", "REPAIRED"));
        String notes = body.getOrDefault("notes", "Claim resolved successfully.");
        String currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(claimService.resolveClaim(id, resolution, notes, currentUser));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<ClaimAudit>> getTimeline(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.getTimeline(id));
    }
}
