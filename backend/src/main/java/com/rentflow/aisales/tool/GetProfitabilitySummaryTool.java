package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.analytics.dto.BookingProfitabilityDTO;
import com.rentflow.analytics.dto.DateRangeType;
import com.rentflow.analytics.service.ProfitabilityService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetProfitabilitySummaryTool implements AiSalesTool {

    private final ProfitabilityService profitabilityService;

    public GetProfitabilitySummaryTool(ProfitabilityService profitabilityService) {
        this.profitabilityService = profitabilityService;
    }

    @Override
    public String getName() {
        return "getProfitabilitySummary";
    }

    @Override
    public String getDescription() {
        return "Fetches booking-level profitability summaries, highlighting low margin and negative margin rentals.";
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
            List<BookingProfitabilityDTO> list = profitabilityService.getBookingProfitabilityList(tenantId, rangeType, null, null);
            BigDecimal totalRev = list.stream().map(b -> b.getTotalRevenue() != null ? b.getTotalRevenue() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalProfit = list.stream().map(b -> b.getGrossProfit() != null ? b.getGrossProfit() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            long lowMarginCount = list.stream().filter(b -> Boolean.TRUE.equals(b.getWarningFlag())).count();

            List<Map<String, Object>> topRows = list.stream().limit(5).map(b -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("bookingNumber", b.getBookingNumber());
                map.put("customerName", b.getCustomerName());
                map.put("revenue", b.getTotalRevenue());
                map.put("profit", b.getGrossProfit());
                map.put("marginPercent", b.getGrossMarginPercent());
                map.put("marginFlag", b.getMarginFlag());
                return map;
            }).collect(Collectors.toList());

            return ToolCallResultDTO.success(getName(), Map.of(
                    "totalBookings", list.size(),
                    "totalRevenue", totalRev,
                    "totalProfit", totalProfit,
                    "lowMarginWarningCount", lowMarginCount,
                    "sampleBookings", topRows
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch profitability summary: " + e.getMessage(), true);
        }
    }
}
