package com.rentflow.automation.controller;

import com.rentflow.automation.dto.AutomationDashboardDTO;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.BusinessSignalRepository;
import com.rentflow.automation.repository.AutomationExecutionRepository;
import com.rentflow.automation.service.AutomationApprovalService;
import com.rentflow.automation.service.AutomationDashboardService;
import com.rentflow.automation.service.AutomationRuleService;
import com.rentflow.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/automation")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AutomationController {

    @Autowired
    private AutomationDashboardService dashboardService;

    @Autowired
    private AutomationApprovalService approvalService;

    @Autowired
    private AutomationRuleService ruleService;

    @Autowired
    private BusinessSignalRepository signalRepository;

    @Autowired
    private AutomationExecutionRepository executionRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<AutomationDashboardDTO> getDashboard(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Role", required = false) String headerRole
    ) {
        String role = headerRole != null ? headerRole : "ROLE_ADMIN";
        if ("ROLE_CUSTOMER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }

        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(dashboardService.getDashboard(tenantId));
    }

    @GetMapping("/signals")
    public ResponseEntity<List<BusinessSignal>> getSignals(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestParam(required = false) BusinessSignalStatus status
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        if (status != null) {
            return ResponseEntity.ok(signalRepository.findByTenantIdAndStatus(tenantId, status));
        }
        return ResponseEntity.ok(signalRepository.findByTenantIdAndStatus(tenantId, BusinessSignalStatus.ACTIVE));
    }

    @GetMapping("/approvals")
    public ResponseEntity<Page<AutomationApproval>> getApprovals(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestParam(required = false) AutomationApprovalStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        if (status == AutomationApprovalStatus.PENDING || status == null) {
            return ResponseEntity.ok(approvalService.getPendingApprovals(tenantId, PageRequest.of(page, size)));
        }
        return ResponseEntity.ok(approvalService.getAllApprovals(tenantId, PageRequest.of(page, size)));
    }

    @PostMapping("/approvals/{id}/approve")
    public ResponseEntity<?> approve(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
        @RequestHeader(value = "X-User-Role", required = false) String headerRole
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String user = headerUserId != null ? headerUserId : SecurityUtils.getCurrentUser();
        String role = headerRole != null ? headerRole : "ROLE_ADMIN";
        String reason = body != null ? body.get("reason") : "Manual human confirmation";

        try {
            AutomationExecution exec = approvalService.approveAndExecute(id, tenantId, user, reason, role);
            return ResponseEntity.ok(exec);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("error", se.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/approvals/{id}/reject")
    public ResponseEntity<?> reject(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
        @RequestHeader(value = "X-User-Role", required = false) String headerRole
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String user = headerUserId != null ? headerUserId : SecurityUtils.getCurrentUser();
        String role = headerRole != null ? headerRole : "ROLE_ADMIN";
        String reason = body != null ? body.getOrDefault("reason", "Rejected by user") : "Rejected by user";

        try {
            AutomationApproval app = approvalService.rejectApproval(id, tenantId, user, reason, role);
            return ResponseEntity.ok(app);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(Map.of("error", se.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AutomationRule>> getRules(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ruleService.getRules(tenantId));
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<AutomationRule> updateRule(
        @PathVariable UUID id,
        @RequestBody Map<String, Object> body,
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        String modeStr = (String) body.get("mode");
        Boolean enabled = (Boolean) body.get("enabled");

        AutomationMode mode = modeStr != null ? AutomationMode.valueOf(modeStr) : AutomationMode.APPROVAL_REQUIRED;
        AutomationRule updated = ruleService.updateRuleMode(tenantId, id, mode, enabled != null ? enabled : true);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/executions")
    public ResponseEntity<Page<AutomationExecution>> getExecutions(
        @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String tenantId = (headerTenant != null && !headerTenant.isBlank()) ? headerTenant : SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(executionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
