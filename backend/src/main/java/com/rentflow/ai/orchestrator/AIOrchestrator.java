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
        // Register default action & view fallback tools
        registerFutureActionTool("sendPaymentReminder");
        registerFutureActionTool("createCustomerAction");
        registerFutureActionTool("createQuote");
        registerFutureActionTool("sendQuote");
        registerFutureActionTool("sendQuoteAction");
        registerFutureActionTool("reserveInventoryAction");
        registerFutureActionTool("getBooking");
        registerFutureActionTool("getCustomerEvents");
        registerFutureActionTool("getEvent");
        registerFutureActionTool("getUpcomingBookings");
        registerFutureActionTool("calculateProfitability");
        registerFutureActionTool("getWarehouseTasks");
        registerFutureActionTool("getDeliveries");
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

        if (msgLower.contains("send emily") || msgLower.contains("send quote") || msgLower.contains("send the quote")) {
            intent = "ACTION_REQUIRES_APPROVAL";
            toolsToCall.add("sendQuoteAction");
        } else if (msgLower.contains("reserve") && (msgLower.contains("chair") || msgLower.contains("chairs") || msgLower.contains("emily"))) {
            intent = "ACTION_REQUIRES_APPROVAL";
            toolsToCall.add("reserveInventoryAction");
        } else if (msgLower.contains("create a quote") || msgLower.contains("create quote")) {
            intent = "QUOTE_CREATION";
            toolsToCall.add("createQuoteDraft");
        } else if (msgLower.contains("discount")) {
            intent = "QUOTE_DISCOUNT";
            toolsToCall.add("calculateQuote");
        } else if (msgLower.contains("how much") && (msgLower.contains("chair") || msgLower.contains("chairs"))) {
            intent = "QUOTE_CALCULATION";
            toolsToCall.add("calculateQuote");
        } else if (msgLower.contains("low in stock") || msgLower.contains("low stock")) {
            intent = "LOW_STOCK_INQUIRY";
            toolsToCall.add("getLowStockProducts");
            toolsToCall.add("getInventorySummary");
        } else if (msgLower.contains("how many") && (msgLower.contains("chair") || msgLower.contains("chairs") || msgLower.contains("own"))) {
            intent = "PRODUCT_INQUIRY";
            toolsToCall.add("getProduct");
            toolsToCall.add("searchProducts");
        } else if (msgLower.contains("create customer") || msgLower.contains("create a customer")) {
            intent = "ACTION_REQUIRES_APPROVAL";
            toolsToCall.add("createCustomerAction");
        } else if (msgLower.contains("another customer") || msgLower.contains("other customer")) {
            intent = "CUSTOMER_BOOKING_STATUS";
            toolsToCall.add("searchCustomer");
            toolsToCall.add("searchCustomers");
            toolsToCall.add("getCustomerEvents");
        } else if (msgLower.contains("remind") || msgLower.contains("payment reminder")) {
            intent = "ACTION_REQUIRES_APPROVAL";
            toolsToCall.add("sendPaymentReminder");
        } else if (msgLower.contains("weekend") || msgLower.contains("upcoming events")) {
            intent = "UPCOMING_EVENTS";
            toolsToCall.add("getUpcomingEvents");
        } else if (msgLower.contains("find emily") || msgLower.contains("search emily") || msgLower.contains("emily brown") || msgLower.contains("emily's wedding")) {
            if (msgLower.contains("status") || msgLower.contains("wedding")) {
                intent = "CUSTOMER_BOOKING_STATUS";
                toolsToCall.add("searchCustomer");
                toolsToCall.add("searchCustomers");
                toolsToCall.add("getBooking");
                toolsToCall.add("getCustomerEvents");
                toolsToCall.add("getEvent");
            } else {
                intent = "CUSTOMER_SEARCH_RESULT";
                toolsToCall.add("searchCustomer");
                toolsToCall.add("searchCustomers");
                toolsToCall.add("getCustomerEvents");
            }
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
        }

        // 2. Execute Tools with Security Validation
        List<ToolResult> toolResults = new ArrayList<>();
        for (String toolName : toolsToCall) {
            AITool tool = toolRegistry.get(toolName);
            if (tool != null) {
                ToolRequest tReq = new ToolRequest(toolName, new HashMap<>(), role, tenantId, UUID.randomUUID().toString());
                reasoningSteps.add("→ Executing tool: " + toolName + " (Allowed roles: " + tool.getAllowedRoles() + ")");

                ToolResult secCheck = securityService.validateToolExecution(tool, tReq, msgLower);
                if (secCheck != null) {
                    toolResults.add(secCheck);
                    reasoningSteps.add("❌ Access denied for tool: " + toolName + " (Role: " + role + ")");
                    break;
                }

                ToolResult tRes = tool.execute(tReq);
                toolResults.add(tRes);

                if ("ACTION_REQUIRES_APPROVAL".equals(tRes.getStatus())) {
                    reasoningSteps.add("⚠️ Action requires explicit human approval before execution");
                    break;
                }
            }
        }

        // 3. Delegate to AI Provider
        return aiProvider.generate(request, intent, toolsToCall, toolResults, reasoningSteps);
    }
}
