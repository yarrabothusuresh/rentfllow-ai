package com.rentflow.ai.tool;

import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.EventService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class GetEventTool implements AITool {

    private final EventService eventService;

    public GetEventTool(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public String getName() {
        return "getEvent";
    }

    @Override
    public String getDescription() {
        return "Retrieve details for a specific event";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<EventDTO> events = eventService.getEvents(request.getTenantId());
        if (!events.isEmpty()) {
            return ToolResult.ok(events.get(0), "Retrieved event details.");
        }
        return ToolResult.error("No events found.");
    }
}
