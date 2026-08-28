package com.rentflow.portal.ai;

import java.util.UUID;

public interface MessageAssistantService {
    String draftStaffResponse(String tenantId, UUID conversationId);
}
