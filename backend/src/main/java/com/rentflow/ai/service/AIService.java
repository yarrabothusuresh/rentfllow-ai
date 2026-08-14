package com.rentflow.ai.service;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.orchestrator.AIOrchestrator;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final AIOrchestrator orchestrator;

    public AIService(AIOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public AIResponse chat(AIRequest request) {
        return orchestrator.processRequest(request);
    }
}
