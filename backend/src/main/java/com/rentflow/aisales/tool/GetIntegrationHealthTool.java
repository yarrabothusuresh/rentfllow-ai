package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.integration.dto.IntegrationDashboardSummaryDTO;
import com.rentflow.integration.service.IntegrationService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class GetIntegrationHealthTool implements AiSalesTool {

    private final IntegrationService integrationService;

    public GetIntegrationHealthTool(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @Override
    public String getName() {
        return "getIntegrationHealth";
    }

    @Override
    public String getDescription() {
        return "Fetches health status of external integrations (QuickBooks, Stripe, Zapier) and event sync health.";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "OPERATIONS");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        try {
            IntegrationDashboardSummaryDTO summary = integrationService.getDashboardSummary(tenantId);
            return ToolCallResultDTO.success(getName(), Map.of(
                    "activeConnectionsCount", summary.getActiveConnectionsCount(),
                    "totalOutboxEvents", summary.getTotalOutboxEvents(),
                    "pendingOutboxEvents", summary.getPendingOutboxEvents(),
                    "failedOutboxEvents", summary.getFailedOutboxEvents(),
                    "recentSyncJobs", summary.getRecentSyncJobs() != null ? summary.getRecentSyncJobs().size() : 0,
                    "allSystemsOperational", summary.getFailedOutboxEvents() == 0
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch integration health: " + e.getMessage(), true);
        }
    }
}
