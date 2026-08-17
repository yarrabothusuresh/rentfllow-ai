package com.rentflow.ai.tool;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.QuoteService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetCustomerQuotesTool implements AITool {

    private final QuoteService quoteService;

    public GetCustomerQuotesTool(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public String getName() {
        return "getCustomerQuotes";
    }

    @Override
    public String getDescription() {
        return "Retrieves all rental quotes for a specific customer ID.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            String customerIdStr = (String) parameters.get("customerId");
            if (customerIdStr == null || customerIdStr.isBlank()) {
                return ToolResult.error("customerId parameter is required");
            }
            UUID customerId = UUID.fromString(customerIdStr);

            List<QuoteDTO> quotes = quoteService.getQuotes(tenantId, userRole).stream()
                    .filter(q -> customerId.equals(q.getCustomerId()))
                    .collect(Collectors.toList());

            return ToolResult.ok(quotes, "Found " + quotes.size() + " quotes for customer ID " + customerIdStr);
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve customer quotes: " + e.getMessage());
        }
    }
}
