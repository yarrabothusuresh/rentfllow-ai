package com.rentflow.portal.ai;

public interface CustomerAssistantService {
    String answerQuestion(String tenantId, String customerId, String question);
}
