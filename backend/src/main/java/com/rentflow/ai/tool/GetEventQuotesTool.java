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
public class GetEventQuotesTool implements AITool {

    private final QuoteService quoteService;

    public GetEventQuotesTool(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public String getName() {
        return "getEventQuotes";
    }

    @Override
    public String getDescription() {
        return "Retrieves all rental quotes linked to a specific event ID.";
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
            String eventIdStr = (String) parameters.get("eventId");
            if (eventIdStr == null || eventIdStr.isBlank()) {
                return ToolResult.error("eventId parameter is required");
            }
            UUID eventId = UUID.fromString(eventIdStr);

            List<QuoteDTO> quotes = quoteService.getQuotes(tenantId, userRole).stream()
                    .filter(q -> eventId.equals(q.getEventId()))
                    .collect(Collectors.toList());

            return ToolResult.ok(quotes, "Found " + quotes.size() + " quotes for event ID " + eventIdStr);
        } catch (Exception e) {
            return ToolResult.error("Failed to retrieve event quotes: " + e.getMessage());
        }
    }
}
