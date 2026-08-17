package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.QuoteService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SendQuoteActionTool implements AITool {

    private final QuoteService quoteService;

    public SendQuoteActionTool(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public String getName() {
        return "sendQuoteAction";
    }

    @Override
    public String getDescription() {
        return "Updates a quote status to SENT after explicit human approval.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        String quoteIdStr = parameters.get("quoteId") != null ? parameters.get("quoteId").toString() : "QUO-000001";
        return ToolResult.requiresApproval(
                Map.of("action", "sendQuote", "quoteId", quoteIdStr),
                "Confirm sending quote to customer"
        );
    }
}
