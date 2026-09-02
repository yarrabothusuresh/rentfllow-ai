package com.rentflow.aisales.provider;

import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.model.AiSalesMessage;
import com.rentflow.aisales.model.RentalInquiry;

import java.util.List;

public interface AiSalesProvider {
    String getProviderName();
    boolean isHealthy();
    AiSalesChatResponseDTO processMessage(
        String tenantId,
        String userRole,
        AiSalesConversation conversation,
        List<AiSalesMessage> history,
        String incomingMessage,
        RentalInquiry currentInquiry
    );
}
