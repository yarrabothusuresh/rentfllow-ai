package com.rentflow.ai.controller;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AIResponse> chat(@RequestBody AIRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AIResponse response = aiService.chat(request);
        return ResponseEntity.ok(response);
    }
}
