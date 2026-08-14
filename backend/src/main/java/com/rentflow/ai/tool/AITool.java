package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;

import java.util.Set;

public interface AITool {
    /**
     * Unique name of the AI tool (e.g., "searchCustomer")
     */
    String getName();

    /**
     * Clear human-readable description of what the tool does
     */
    String getDescription();

    /**
     * Roles allowed to use this tool (e.g. OWNER, SALES, WAREHOUSE, DRIVER, CUSTOMER)
     */
    Set<String> getAllowedRoles();

    /**
     * Executes the tool with the given request parameters.
     */
    ToolResult execute(ToolRequest request);
}
