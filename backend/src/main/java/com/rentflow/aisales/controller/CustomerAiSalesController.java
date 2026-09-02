package com.rentflow.aisales.controller;

import com.rentflow.aisales.dto.AiSalesChatRequestDTO;
import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.dto.AiSalesConversationDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.service.AiSalesAgentService;
import com.rentflow.aisales.service.AiSalesConversationService;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/portal/ai-sales")
public class CustomerAiSalesController {

    private final AiSalesAgentService agentService;
    private final AiSalesConversationService conversationService;

    public CustomerAiSalesController(
        AiSalesAgentService agentService,
        AiSalesConversationService conversationService
    ) {
        this.agentService = agentService;
        this.conversationService = conversationService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<AiSalesChatResponseDTO> startOrResumeConversation(
        @RequestBody AiSalesChatRequestDTO request
    ) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        request.setChannel("CUSTOMER_PORTAL");

        // Execute initial message or greeting
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            request.setMessage("Hello! I need help with rental equipment.");
        }

        AiSalesChatResponseDTO response = agentService.handleMessage(tenantId, "CUSTOMER", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<AiSalesChatResponseDTO> sendMessage(
        @PathVariable UUID id,
        @RequestBody AiSalesChatRequestDTO request
    ) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        request.setConversationId(id);
        request.setChannel("CUSTOMER_PORTAL");

        AiSalesChatResponseDTO response = agentService.handleMessage(tenantId, "CUSTOMER", request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<AiSalesConversationDTO> getConversation(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return conversationService.getConversation(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/public/{publicId}")
    public ResponseEntity<AiSalesConversationDTO> getConversationByPublicId(@PathVariable String publicId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return conversationService.getConversationByPublicId(tenantId, publicId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
