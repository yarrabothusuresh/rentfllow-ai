package com.rentflow.ai.provider;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.dto.ToolResult;

import java.util.List;

public interface AIProvider {
    /**
     * Provider implementation identifier (e.g., "MockAI", "OpenAI", "Anthropic", "Gemini", "Ollama")
     */
    String getProviderName();

    /**
     * Generate response based on user request, detected intent, tools executed, and tool results.
     */
    AIResponse generate(AIRequest request, String intent, List<String> toolsUsed, List<ToolResult> toolResults, List<String> reasoningSteps);
}
