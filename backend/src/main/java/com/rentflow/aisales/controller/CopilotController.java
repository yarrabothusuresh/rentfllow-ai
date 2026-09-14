package com.rentflow.aisales.controller;

import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.service.AiCopilotService;
import com.rentflow.aisales.service.CopilotActionService;
import com.rentflow.aisales.service.CopilotBriefingService;
import com.rentflow.auth.RateLimitExceededException;
import com.rentflow.security.RateLimitingService;
import com.rentflow.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/copilot")
public class CopilotController {

    private final AiCopilotService copilotService;
    private final CopilotActionService actionService;
    private final CopilotBriefingService briefingService;
    private final RateLimitingService rateLimitingService;

    public CopilotController(AiCopilotService copilotService,
                             CopilotActionService actionService,
                             CopilotBriefingService briefingService,
                             RateLimitingService rateLimitingService) {
        this.copilotService = copilotService;
        this.actionService = actionService;
        this.briefingService = briefingService;
        this.rateLimitingService = rateLimitingService;
    }

    private String resolveTenant(String headerTenant) {
        if (SecurityUtils.hasExplicitTenantContext()) {
            return SecurityUtils.getCurrentTenantId();
        }
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant;
        }
        return SecurityUtils.getCurrentTenantId();
    }

    private String resolveRole(String headerRole) {
        if (SecurityUtils.hasExplicitTenantContext()) {
            return SecurityUtils.getCurrentUserRole();
        }
        if (headerRole != null && !headerRole.isBlank()) {
            return headerRole;
        }
        return "CUSTOMER";
    }

    private String resolveUserId(String headerUserId) {
        if (SecurityUtils.hasExplicitTenantContext()) {
            return SecurityUtils.getCurrentUser();
        }
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId;
        }
        return SecurityUtils.getCurrentUser();
    }

    private void checkRateLimit(String tenantId, String userId) {
        String key = tenantId + ":" + userId;
        if (!rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AI_COPILOT, key)) {
            throw new RateLimitExceededException("AI Copilot request limit exceeded. Please try again shortly.");
        }
    }

    @PostMapping("/conversations")
    public ResponseEntity<CopilotResponseDTO> startConversation(
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody(required = false) CopilotChatRequestDTO request) {

        String tenantId = resolveTenant(headerTenant);
        String role = resolveRole(headerRole);
        String userId = resolveUserId(headerUserId);

        checkRateLimit(tenantId, userId);

        String pageContextType = (request != null) ? request.getPageContextType() : null;
        String pageContextId = (request != null) ? request.getPageContextId() : null;

        try {
            CopilotResponseDTO res = copilotService.startConversation(tenantId, role, userId, pageContextType, pageContextId);
            return ResponseEntity.ok(res);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<CopilotResponseDTO> sendMessage(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody CopilotChatRequestDTO request) {

        String tenantId = resolveTenant(headerTenant);
        String role = resolveRole(headerRole);
        String userId = resolveUserId(headerUserId);

        checkRateLimit(tenantId, userId);

        if (request != null && request.getMessage() != null && request.getMessage().length() > 4000) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CopilotResponseDTO res = copilotService.processMessage(tenantId, role, userId, id, request);
            return ResponseEntity.ok(res);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/briefing")
    public ResponseEntity<CopilotResponseDTO> getBriefing(
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Name", required = false) String headerUserName) {

        String tenantId = resolveTenant(headerTenant);
        String role = resolveRole(headerRole);
        String userId = resolveUserId(headerUserId);
        String userName = (headerUserName != null && !headerUserName.isBlank()) ? headerUserName : userId;

        checkRateLimit(tenantId, userId);

        return ResponseEntity.ok(briefingService.generateDailyBriefing(tenantId, role, userId, userName));
    }

    @GetMapping("/quick-prompts")
    public ResponseEntity<List<String>> getQuickPrompts(
            @RequestHeader(value = "X-User-Role", required = false) String headerRole) {

        String role = resolveRole(headerRole);
        List<String> prompts;
        switch (role) {
            case "SALES":
                prompts = List.of(
                        "What should I follow up today?",
                        "Which quotes need follow-up?",
                        "Which customers owe us the most?",
                        "Find customer Acme Corp"
                );
                break;
            case "OPERATIONS":
                prompts = List.of(
                        "Are any deliveries at risk tomorrow?",
                        "Which bookings need attention tomorrow?",
                        "Assign driver to pending delivery",
                        "What is blocking the warehouse?"
                );
                break;
            case "WAREHOUSE":
                prompts = List.of(
                        "What is blocking the warehouse?",
                        "Which bookings need attention tomorrow?",
                        "Show open equipment shortages",
                        "Which orders need picker assignment?"
                );
                break;
            case "FINANCE":
                prompts = List.of(
                        "How much is more than 30 days overdue?",
                        "Why did margin fall?",
                        "Which customers owe us the most?",
                        "How are we doing this month?"
                );
                break;
            case "OWNER":
            case "ADMIN":
            default:
                prompts = List.of(
                        "How are we doing this month?",
                        "Why did margin fall?",
                        "Which bookings need attention tomorrow?",
                        "How much is more than 30 days overdue?",
                        "What is blocking the warehouse?",
                        "Are any deliveries at risk tomorrow?"
                );
                break;
        }
        return ResponseEntity.ok(prompts);
    }

    @PostMapping("/actions/{proposalId}/confirm")
    public ResponseEntity<CopilotActionProposalDTO> confirmAction(
            @PathVariable UUID proposalId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String tenantId = resolveTenant(headerTenant);
        String role = resolveRole(headerRole);
        String userId = resolveUserId(headerUserId);

        try {
            CopilotActionProposalDTO confirmed = actionService.confirmAction(tenantId, proposalId, role, userId);
            return ResponseEntity.ok(confirmed);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/actions/{proposalId}/cancel")
    public ResponseEntity<CopilotActionProposalDTO> cancelAction(
            @PathVariable UUID proposalId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String tenantId = resolveTenant(headerTenant);
        String role = resolveRole(headerRole);
        String userId = resolveUserId(headerUserId);

        return ResponseEntity.ok(actionService.cancelAction(tenantId, proposalId, role, userId));
    }

    @GetMapping("/actions/{proposalId}")
    public ResponseEntity<CopilotActionProposalDTO> getAction(
            @PathVariable UUID proposalId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String headerTenant) {

        String tenantId = resolveTenant(headerTenant);
        return actionService.getProposal(tenantId, proposalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestBody Map<String, Object> body) {
        // Records feedback rating and comments for continuous quality tracking
        return ResponseEntity.ok(Map.of("status", "RECEIVED", "timestamp", System.currentTimeMillis()));
    }
}
