package com.rentflow.claims.controller;

import com.rentflow.claims.dto.ReplacementOrderDTO;
import com.rentflow.claims.model.ReplacementOrderStatus;
import com.rentflow.claims.service.ReplacementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/replacements")
@CrossOrigin(originPatterns = "*")
public class ReplacementController {

    private final ReplacementService replacementService;

    public ReplacementController(ReplacementService replacementService) {
        this.replacementService = replacementService;
    }

    @PostMapping
    public ResponseEntity<ReplacementOrderDTO> createReplacement(@RequestBody Map<String, Object> body) {
        UUID claimId = UUID.fromString(body.get("claimId").toString());
        UUID productId = UUID.fromString(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", 1).toString());
        String reason = body.getOrDefault("reason", "Item Replacement").toString();
        BigDecimal unitCost = body.get("unitCost") != null ? new BigDecimal(body.get("unitCost").toString()) : BigDecimal.valueOf(150.00);
        return ResponseEntity.ok(replacementService.createReplacementOrder(claimId, productId, quantity, reason, unitCost));
    }

    @GetMapping
    public ResponseEntity<List<ReplacementOrderDTO>> getReplacements(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ReplacementOrderStatus status) {
        return ResponseEntity.ok(replacementService.getReplacements(search, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReplacementOrderDTO> getReplacementById(@PathVariable UUID id) {
        return ResponseEntity.ok(replacementService.getReplacementById(id));
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<ReplacementOrderDTO> orderReplacement(@PathVariable UUID id) {
        return ResponseEntity.ok(replacementService.orderReplacement(id));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<ReplacementOrderDTO> receiveReplacement(@PathVariable UUID id) {
        return ResponseEntity.ok(replacementService.receiveReplacement(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ReplacementOrderDTO> completeReplacement(@PathVariable UUID id) {
        return ResponseEntity.ok(replacementService.completeReplacement(id));
    }
}
