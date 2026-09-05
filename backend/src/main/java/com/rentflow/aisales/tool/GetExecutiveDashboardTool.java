package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.analytics.dto.DateRangeType;
import com.rentflow.analytics.dto.ExecutiveDashboardDTO;
import com.rentflow.analytics.service.AnalyticsService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class GetExecutiveDashboardTool implements AiSalesTool {

    private final AnalyticsService analyticsService;

    public GetExecutiveDashboardTool(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public String getName() {
        return "getExecutiveDashboard";
    }

    @Override
    public String getDescription() {
        return "Fetches executive analytics KPIs including booked revenue, collected revenue, gross profit, margin, and order counts.";
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
        return Set.of("OWNER", "ADMIN", "FINANCE");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        String rangeStr = request.getStringParam("dateRange", "THIS_MONTH");
        DateRangeType rangeType;
        try {
            rangeType = DateRangeType.valueOf(rangeStr.toUpperCase());
        } catch (Exception e) {
            rangeType = DateRangeType.THIS_MONTH;
        }

        try {
            ExecutiveDashboardDTO dash = analyticsService.getExecutiveDashboard(tenantId, rangeType, null, null);
            return ToolCallResultDTO.success(getName(), Map.of(
                    "periodName", dash.getPeriodName(),
                    "currency", dash.getCurrency(),
                    "bookedRevenue", dash.getBookedRevenue() != null ? dash.getBookedRevenue().getCurrentValue() : 0,
                    "collectedRevenue", dash.getCollectedRevenue() != null ? dash.getCollectedRevenue().getCurrentValue() : 0,
                    "outstandingRevenue", dash.getOutstandingRevenue() != null ? dash.getOutstandingRevenue().getCurrentValue() : 0,
                    "grossProfit", dash.getGrossProfit() != null ? dash.getGrossProfit().getCurrentValue() : 0,
                    "grossMarginPercent", dash.getGrossMargin() != null ? dash.getGrossMargin().getCurrentValue() : 0,
                    "bookingsCount", dash.getBookingsCount() != null ? dash.getBookingsCount().getCurrentValue() : 0,
                    "dataFreshness", dash.getDataFreshness(),
                    "dataQualityWarnings", dash.getDataQualityWarnings()
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch executive dashboard: " + e.getMessage(), true);
        }
    }
}
