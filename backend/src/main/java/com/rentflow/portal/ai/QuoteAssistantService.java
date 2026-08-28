package com.rentflow.portal.ai;

import java.util.UUID;

public interface QuoteAssistantService {
    String explainQuote(String tenantId, UUID quoteId);
}
