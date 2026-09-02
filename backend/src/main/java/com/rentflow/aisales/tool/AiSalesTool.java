package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;

import java.util.Set;

public interface AiSalesTool {
    String getName();
    String getDescription();
    boolean isCustomerVisible();
    boolean isMutating();
    Set<String> getAllowedRoles();
    ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request);
}
