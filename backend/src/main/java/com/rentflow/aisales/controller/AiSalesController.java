package com.rentflow.aisales.controller;

import com.rentflow.ai.service.QuoteService;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.AiEscalationPriority;
import com.rentflow.aisales.model.AiEscalationReason;
import com.rentflow.aisales.service.AiSalesAgentService;
import com.rentflow.aisales.service.AiSalesConversationService;
import com.rentflow.aisales.tool.AiSalesToolRegistry;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai-sales")
public class AiSalesController {

    private final AiSalesAgentService agentService;
    private final AiSalesConversationService conversationService;
    private final QuoteService quoteService;
    private final AiSalesToolRegistry toolRegistry;

    public AiSalesController(
        AiSalesAgentService agentService,
        AiSalesConversationService conversationService,
        QuoteService quoteService,
        AiSalesToolRegistry toolRegistry
    ) {
        this.agentService = agentService;
        this.conversationService = conversationService;
        this.quoteService = quoteService;
        this.toolRegistry = toolRegistry;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AiSalesDashboardDTO> getDashboard() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(conversationService.getDashboardSummary(tenantId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AiSalesConversationDTO>> getConversations() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(conversationService.getConversations(tenantId));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<AiSalesConversationDTO> getConversation(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return conversationService.getConversation(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<AiSalesChatResponseDTO> sendMessage(
        @PathVariable UUID id,
        @RequestBody AiSalesChatRequestDTO request
    ) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userRole = "SALES";
        request.setConversationId(id);
        return ResponseEntity.ok(agentService.handleMessage(tenantId, userRole, request));
    }

    @PostMapping("/conversations/{id}/takeover")
    public ResponseEntity<Map<String, Object>> takeOver(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String currentUserId = SecurityUtils.getCurrentUser();
        boolean ok = conversationService.takeOverConversation(tenantId, id, currentUserId);
        return ResponseEntity.ok(Map.of("success", ok, "status", "HUMAN_ACTIVE"));
    }

    @PostMapping("/conversations/{id}/return-to-ai")
    public ResponseEntity<Map<String, Object>> returnToAi(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        boolean ok = conversationService.returnToAi(tenantId, id);
        return ResponseEntity.ok(Map.of("success", ok, "status", "ACTIVE"));
    }

    @PostMapping("/conversations/{id}/escalate")
    public ResponseEntity<ToolCallResultDTO> escalate(
        @PathVariable UUID id,
        @RequestBody Map<String, Object> body
    ) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String role = "SALES";

        ToolCallRequestDTO toolReq = new ToolCallRequestDTO("escalateToHuman", Map.of(
            "conversationId", id.toString(),
            "reason", body.getOrDefault("reason", AiEscalationReason.CUSTOMER_REQUEST.name()),
            "summary", body.getOrDefault("summary", "Staff-initiated conversation escalation")
        ));
        return ResponseEntity.ok(toolRegistry.executeTool(tenantId, role, toolReq));
    }

    @GetMapping("/escalations")
    public ResponseEntity<List<AiEscalationDTO>> getEscalations() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(conversationService.getEscalations(tenantId));
    }

    @GetMapping("/quotes/{id}/review")
    public ResponseEntity<AiQuoteReviewDTO> getQuoteReview(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return conversationService.getQuoteReview(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/quotes/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveQuote(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String currentUserId = SecurityUtils.getCurrentUser();

        try {
            quoteService.updateStatus(tenantId, id, com.rentflow.ai.model.QuoteStatus.SENT, currentUserId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "quoteId", id,
                "status", "SENT",
                "message", "Quote approved and successfully transmitted to customer."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/quotes/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectQuote(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body
    ) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        quoteService.updateStatus(tenantId, id, com.rentflow.ai.model.QuoteStatus.CANCELLED, "SALES");
        return ResponseEntity.ok(Map.of(
            "success", true,
            "quoteId", id,
            "status", "CANCELLED",
            "message", "Quote rejected."
        ));
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody AiFeedbackRequestDTO req) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String currentUserId = SecurityUtils.getCurrentUser();
        agentService.recordFeedback(tenantId, currentUserId, req);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
