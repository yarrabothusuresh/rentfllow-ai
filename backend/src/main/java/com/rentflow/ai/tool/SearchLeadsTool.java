package com.rentflow.ai.tool;

import com.rentflow.ai.dto.LeadDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.LeadService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class SearchLeadsTool implements AITool {

    private final LeadService leadService;

    public SearchLeadsTool(LeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public String getName() {
        return "searchLeads";
    }

    @Override
    public String getDescription() {
        return "Search leads by name, email, phone, company, or event type";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String query = request.getParams() != null ? (String) request.getParams().get("query") : "";
        List<LeadDTO> leads = leadService.searchLeads(request.getTenantId(), query);

        if (!leads.isEmpty()) {
            return ToolResult.ok(leads, "Found " + leads.size() + " lead(s) matching query.");
        }
        return ToolResult.error("No leads found matching: " + query);
    }
}
