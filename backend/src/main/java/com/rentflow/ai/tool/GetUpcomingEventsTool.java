package com.rentflow.ai.tool;

import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.EventService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class GetUpcomingEventsTool implements AITool {

    private final EventService eventService;

    public GetUpcomingEventsTool(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public String getName() {
        return "getUpcomingEvents";
    }

    @Override
    public String getDescription() {
        return "Get upcoming events scheduled for this weekend or month";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<EventDTO> events = eventService.getEvents(request.getTenantId());
        return ToolResult.ok(events, "Found " + events.size() + " upcoming event(s).");
    }
}
