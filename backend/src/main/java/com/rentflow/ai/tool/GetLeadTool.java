package com.rentflow.ai.tool;

import com.rentflow.ai.dto.LeadDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.LeadService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class GetLeadTool implements AITool {

    private final LeadService leadService;

    public GetLeadTool(LeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public String getName() {
        return "getLead";
    }

    @Override
    public String getDescription() {
        return "Retrieve details for a specific lead";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        List<LeadDTO> leads = leadService.getLeads(request.getTenantId());
        if (!leads.isEmpty()) {
            return ToolResult.ok(leads.get(0), "Retrieved lead details.");
        }
        return ToolResult.error("No lead found.");
    }
}
