package com.rentflow.ai.controller;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.service.AIService;
import com.rentflow.auth.RateLimitExceededException;
import com.rentflow.security.RateLimitingService;
import com.rentflow.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final RateLimitingService rateLimitingService;

    public AIController(AIService aiService, RateLimitingService rateLimitingService) {
        this.aiService = aiService;
        this.rateLimitingService = rateLimitingService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AIResponse> chat(@Valid @RequestBody AIRequest request, HttpServletRequest httpRequest) {
        String rateLimitKey;
        if (SecurityUtils.hasExplicitTenantContext()) {
            rateLimitKey = SecurityUtils.getCurrentTenantId() + ":" + SecurityUtils.getCurrentUser();
        } else {
            rateLimitKey = rateLimitingService.resolveSafeClientIp(httpRequest);
        }

        if (!rateLimitingService.tryAcquire(RateLimitingService.RateLimitCategory.AI_SALES, rateLimitKey)) {
            throw new RateLimitExceededException("AI Sales chat rate limit exceeded. Please try again shortly.");
        }

        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty() || request.getMessage().length() > 4000) {
            return ResponseEntity.badRequest().build();
        }

        // Ensure tenantId from JWT is authoritative
        if (SecurityUtils.hasExplicitTenantContext()) {
            request.setTenantId(SecurityUtils.getCurrentTenantId());
            request.setRole(SecurityUtils.getCurrentUserRole());
            request.setUserId(SecurityUtils.getCurrentUser());
        }

        AIResponse response = aiService.chat(request);
        return ResponseEntity.ok(response);
    }
}
