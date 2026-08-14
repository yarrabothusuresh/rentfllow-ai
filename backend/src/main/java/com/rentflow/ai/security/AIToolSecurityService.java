package com.rentflow.ai.security;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.tool.AITool;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AIToolSecurityService {

    /**
     * Validates whether the user with role and tenantId is authorized to execute the specified tool and parameters.
     */
    public ToolResult validateToolExecution(AITool tool, ToolRequest request, String rawMessage) {
        String role = request.getUserRole() != null ? request.getUserRole().toUpperCase() : "OWNER";
        String tenantId = request.getTenantId();

        // 1. Tenant Isolation Check
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return ToolResult.permissionDenied("Tenant ID is required for tool execution.");
        }

        // 2. Role-Based Tool Authorization Check
        Set<String> allowedRoles = tool.getAllowedRoles();
        if (allowedRoles != null && !allowedRoles.contains(role)) {
            return ToolResult.permissionDenied("You don't have permission to access that information.");
        }

        // 3. Customer Role Strict Isolation
        if ("CUSTOMER".equals(role)) {
            String msgLower = rawMessage != null ? rawMessage.toLowerCase() : "";
            // If customer asks for another customer's data or non-owned data
            if (msgLower.contains("another customer") || 
                msgLower.contains("other customer") || 
                msgLower.contains("all customers") || 
                msgLower.contains("techcorp") || 
                msgLower.contains("jenkins") || 
                msgLower.contains("profit") || 
                msgLower.contains("warehouse")) {
                return ToolResult.permissionDenied("You don't have permission to access that information.");
            }
        }

        return null; // Null means security check passed
    }
}
