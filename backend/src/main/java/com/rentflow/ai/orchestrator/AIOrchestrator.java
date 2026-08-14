package com.rentflow.ai.orchestrator;

import com.rentflow.ai.dto.AIRequest;
import com.rentflow.ai.dto.AIResponse;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.provider.AIProvider;
import com.rentflow.ai.security.AIToolSecurityService;
import com.rentflow.ai.tool.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIOrchestrator {

    private final AIProvider aiProvider;
    private final AIToolSecurityService securityService;
    private final Map<String, AITool> toolRegistry = new HashMap<>();

    public AIOrchestrator(AIProvider aiProvider,
                          AIToolSecurityService securityService,
                          List<AITool> tools) {
        this.aiProvider = aiProvider;
        this.securityService = securityService;
        for (AITool tool : tools) {
            toolRegistry.put(tool.getName(), tool);
        }
        // Register default future action tools
        registerFutureActionTool("sendPaymentReminder");
        registerFutureActionTool("createQuote");
        registerFutureActionTool("sendQuote");
        registerFutureActionTool("reserveInventory");
        registerFutureActionTool("createBooking");
        registerFutureActionTool("assignDriver");
        registerFutureActionTool("createWarehouseTask");
    }

    private void registerFutureActionTool(String name) {
        if (!toolRegistry.containsKey(name)) {
            toolRegistry.put(name, new FutureActionTool(name));
        }
    }

    public AIResponse processRequest(AIRequest request) {
        String msg = request.getMessage() != null ? request.getMessage().trim() : "";
        String msgLower = msg.toLowerCase();
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "OWNER";
        String tenantId = request.getTenantId() != null ? request.getTenantId() : DemoDataRepository.EVERGREEN_TENANT_ID;
        request.setTenantId(tenantId);
        request.setRole(role);

        List<String> reasoningSteps = new ArrayList<>();
        reasoningSteps.add("✓ Understood request intent from input");
        reasoningSteps.add("✓ Validated tenant context (" + tenantId.substring(0, 8) + "...) and user role (" + role + ")");

        // 1. Detect Intent and Selected Tools
        String intent;
        List<String> toolsToCall = new ArrayList<>();

        if (msgLower.contains("another customer") || msgLower.contains("other customer")) {
            intent = "CUSTOMER_BOOKING_STATUS";
            toolsToCall.add("searchCustomer");
            toolsToCall.add("getBooking");
        } else if (msgLower.contains("remind") || msgLower.contains("payment reminder")) {
            intent = "ACTION_REQUIRES_APPROVAL";
            toolsToCall.add("sendPaymentReminder");
        } else if (msgLower.contains("emily") || (msgLower.contains("status") && msgLower.contains("wedding"))) {
            intent = "CUSTOMER_BOOKING_STATUS";
            toolsToCall.add("searchCustomer");
            toolsToCall.add("getBooking");
        } else if (msgLower.contains("chair") || msgLower.contains("chairs") || msgLower.contains("availability")) {
            intent = "AVAILABILITY_CHECK";
            toolsToCall.add("searchProducts");
            toolsToCall.add("checkAvailability");
        } else if (msgLower.contains("profit") || msgLower.contains("profitable")) {
            intent = "PROFITABILITY_ANALYSIS";
            toolsToCall.add("getUpcomingBookings");
            toolsToCall.add("calculateProfitability");
        } else if (msgLower.contains("warehouse") || msgLower.contains("prepare")) {
            intent = "WAREHOUSE_PREP";
            toolsToCall.add("getUpcomingBookings");
            toolsToCall.add("getWarehouseTasks");
        } else if (msgLower.contains("deliveries") || msgLower.contains("delivery")) {
            intent = "DELIVERY_SCHEDULE";
            toolsToCall.add("getDeliveries");
        } else if (msgLower.contains("quote") || msgLower.contains("250-person")) {
            intent = "QUOTE_CALCULATION";
            toolsToCall.add("searchProducts");
            toolsToCall.add("checkAvailability");
            toolsToCall.add("calculateQuote");
        } else {
            intent = "GENERAL_PRIORITIES";
            toolsToCall.add("getUpcomingBookings");
            toolsToCall.add("getWarehouseTasks");
        }

        // 2. Execute Tools with Security Validation
        List<String> toolsUsed = new ArrayList<>();
        List<ToolResult> toolResults = new ArrayList<>();

        for (String toolName : toolsToCall) {
            AITool tool = toolRegistry.get(toolName);
            if (tool != null) {
                ToolRequest toolReq = new ToolRequest(toolName, Map.of("query", msg), tenantId, request.getUserId(), role);
                
                // Security Check
                ToolResult securityError = securityService.validateToolExecution(tool, toolReq, msg);
                if (securityError != null) {
                    reasoningSteps.add("❌ Security violation: Permission denied for tool " + toolName);
                    toolResults.add(securityError);
                    toolsUsed.add(toolName);
                    break;
                }

                // Execute Tool
                reasoningSteps.add("✓ Executed tool: " + toolName);
                ToolResult result = tool.execute(toolReq);
                toolsUsed.add(toolName);
                toolResults.add(result);

                if ("ACTION_REQUIRES_APPROVAL".equals(result.getStatus())) {
                    reasoningSteps.add("⚠️ Tool " + toolName + " requires approval before execution");
                }
            }
        }

        reasoningSteps.add("✓ Generated synthesized operational response");

        // 3. Delegate to Provider to produce AIResponse DTO
        return aiProvider.generate(request, intent, toolsUsed, toolResults, reasoningSteps);
    }
}
