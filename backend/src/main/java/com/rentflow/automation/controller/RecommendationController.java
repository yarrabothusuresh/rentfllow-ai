package com.rentflow.automation.controller;

import com.rentflow.automation.model.*;
import com.rentflow.automation.service.AutomationApprovalService;
import com.rentflow.automation.service.RecommendationService;
import com.rentflow.automation.service.SignalDetectionService;
import com.rentflow.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SignalDetectionService signalDetectionService;

    @Autowired
    private AutomationApprovalService approvalService;

    @GetMapping
    public ResponseEntity<Page<AiRecommendation>> getRecommendations(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Role", required = false) String headerRole,
        @RequestParam(required = false) RecommendationStatus status,
        @RequestParam(required = false) RecommendationPriority priority,
        @RequestParam(required = false) BusinessSignalCategory category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String role = headerRole != null ? headerRole : "ROLE_ADMIN";
        if ("ROLE_CUSTOMER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        Page<AiRecommendation> results = recommendationService.getRecommendations(
            tenantId, status, priority, category, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiRecommendation> getRecommendationById(
        @PathVariable UUID id,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        return recommendationService.getRecommendationById(id, tenantId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<AiRecommendation> markReviewed(
        @PathVariable UUID id,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String user = headerUserId != null ? headerUserId : SecurityUtils.getCurrentUser();
        try {
            AiRecommendation updated = recommendationService.markReviewed(id, tenantId, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<AiRecommendation> dismissRecommendation(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String user = headerUserId != null ? headerUserId : SecurityUtils.getCurrentUser();
        String reason = body != null ? body.getOrDefault("reason", "Dismissed by user") : "Dismissed by user";

        try {
            AiRecommendation updated = recommendationService.dismissRecommendation(id, tenantId, user, reason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/propose-action")
    public ResponseEntity<AutomationApproval> proposeAction(
        @PathVariable UUID id,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String user = headerUserId != null ? headerUserId : SecurityUtils.getCurrentUser();

        AiRecommendation rec = recommendationService.getRecommendationById(id, tenantId).orElse(null);
        if (rec == null || rec.getSuggestedActionType() == null) {
            return ResponseEntity.badRequest().build();
        }

        AutomationApproval approval = approvalService.createApprovalRequest(
            tenantId, rec.getId(), rec.getSuggestedActionType(), rec.getSuggestedActionPayloadJson(), user
        );
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/detect")
    public ResponseEntity<List<AiRecommendation>> triggerDetection(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        List<AiRecommendation> created = signalDetectionService.runDetection(tenantId);
        return ResponseEntity.ok(created);
    }
}
