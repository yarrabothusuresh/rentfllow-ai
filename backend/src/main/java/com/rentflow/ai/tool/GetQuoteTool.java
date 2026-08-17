package com.rentflow.ai.tool;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.QuoteService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class GetQuoteTool implements AITool {

    private final QuoteService quoteService;

    public GetQuoteTool(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public String getName() {
        return "getQuote";
    }

    @Override
    public String getDescription() {
        return "Retrieves quote details by Quote ID or Quote Number.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            String idStr = (String) parameters.get("quoteId");
            if (idStr == null || idStr.isBlank()) {
                return ToolResult.error("quoteId parameter is required");
            }

            Optional<QuoteDTO> opt = quoteService.getQuoteById(tenantId, UUID.fromString(idStr), userRole);
            if (opt.isEmpty()) {
                return ToolResult.error("Quote not found with ID: " + idStr);
            }

            QuoteDTO q = opt.get();
            return ToolResult.ok(q, "Quote " + q.getQuoteNumber() + " total: $" + q.getTotalAmount());
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve quote: " + e.getMessage());
        }
    }
}
