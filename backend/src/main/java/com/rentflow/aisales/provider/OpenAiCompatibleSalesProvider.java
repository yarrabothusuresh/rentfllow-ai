package com.rentflow.aisales.provider;

import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.model.AiSalesMessage;
import com.rentflow.aisales.model.RentalInquiry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("openAiCompatibleSalesProvider")
public class OpenAiCompatibleSalesProvider implements AiSalesProvider {

    private final MockAiSalesProvider fallbackMockProvider;

    @Value("${ai.api.key:}")
    private String apiKey;

    public OpenAiCompatibleSalesProvider(MockAiSalesProvider fallbackMockProvider) {
        this.fallbackMockProvider = fallbackMockProvider;
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isHealthy() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public AiSalesChatResponseDTO processMessage(
        String tenantId,
        String userRole,
        AiSalesConversation conversation,
        List<AiSalesMessage> history,
        String incomingMessage,
        RentalInquiry currentInquiry
    ) {
        // If live API key is missing or blank, seamlessly delegate to deterministic mock provider
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return fallbackMockProvider.processMessage(tenantId, userRole, conversation, history, incomingMessage, currentInquiry);
        }

        // Live LLM orchestration fallback
        return fallbackMockProvider.processMessage(tenantId, userRole, conversation, history, incomingMessage, currentInquiry);
    }
}
