package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.analytics.dto.BookingProfitDetailDTO;
import com.rentflow.analytics.service.ProfitabilityService;
import com.rentflow.common.financial.FinancialMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class CalculateProfitabilityTool implements AITool {

    private final BookingRepository bookingRepository;
    private final ProfitabilityService profitabilityService;
    private final DemoDataRepository demoDataRepository;

    public CalculateProfitabilityTool(BookingRepository bookingRepository, ProfitabilityService profitabilityService) {
        this.bookingRepository = bookingRepository;
        this.profitabilityService = profitabilityService;
        this.demoDataRepository = null;
    }

    public CalculateProfitabilityTool(DemoDataRepository demoDataRepository) {
        this.bookingRepository = null;
        this.profitabilityService = null;
        this.demoDataRepository = demoDataRepository;
    }

    @Autowired
    public CalculateProfitabilityTool(Optional<BookingRepository> bookingRepository,
                                      Optional<ProfitabilityService> profitabilityService,
                                      Optional<DemoDataRepository> demoDataRepository) {
        this.bookingRepository = bookingRepository.orElse(null);
        this.profitabilityService = profitabilityService.orElse(null);
        this.demoDataRepository = demoDataRepository.orElse(null);
    }

    @Override
    public String getName() {
        return "calculateProfitability";
    }

    @Override
    public String getDescription() {
        return "Calculate authoritative estimated revenue, cost, profit, and margin for a booking or event";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "FINANCE");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String bookingId = request.getParams() != null ? (String) request.getParams().get("bookingId") : null;

        // 1. Authoritative Production Path (queries DB & ProfitabilityService)
        if (bookingRepository != null && profitabilityService != null) {
            Optional<Booking> bookingOpt = Optional.empty();

            if (bookingId != null && !bookingId.isBlank()) {
                try {
                    UUID bId = UUID.fromString(bookingId.trim());
                    bookingOpt = bookingRepository.findByTenantIdAndId(tenantId, bId);
                } catch (IllegalArgumentException e) {
                    bookingOpt = bookingRepository.findByTenantIdAndBookingNumber(tenantId, bookingId.trim());
                }
            }

            if (bookingOpt.isEmpty()) {
                List<Booking> list = bookingRepository.findByTenantId(tenantId);
                if (!list.isEmpty()) {
                    bookingOpt = Optional.of(list.get(0));
                }
            }

            if (bookingOpt.isPresent()) {
                Booking b = bookingOpt.get();
                BookingProfitDetailDTO detail = profitabilityService.getBookingProfitDetail(tenantId, b.getId());

                Map<String, Object> resultData = new LinkedHashMap<>();
                resultData.put("bookingId", b.getId().toString());
                resultData.put("bookingNumber", b.getBookingNumber());
                resultData.put("customerName", detail.getCustomerName());
                resultData.put("eventName", detail.getEventName());
                resultData.put("revenue", detail.getTotalRevenue());
                resultData.put("estimatedCost", detail.getTotalDirectCost());
                resultData.put("estimatedProfit", detail.getGrossProfit());
                resultData.put("estimatedMargin", detail.getGrossMarginPercent());
                resultData.put("marginFlag", detail.getMarginFlag());
                resultData.put("isAuthoritative", true);

                return ToolResult.ok(resultData, "Authoritative profitability calculated for " + b.getBookingNumber());
            }

            return ToolResult.error("No active bookings found for tenant " + tenantId + " to calculate profitability.");
        }

        // 2. Demo Repository Fallback (uses pure BigDecimal without floating point drift)
        if (demoDataRepository != null) {
            String targetId = bookingId != null ? bookingId : "booking-001";
            List<Map<String, Object>> bookings = demoDataRepository.getBookings(tenantId);
            Optional<Map<String, Object>> match = bookings.stream()
                    .filter(b -> targetId.equals(b.get("bookingId")))
                    .findFirst();

            BigDecimal revenue = new BigDecimal("6480.00");
            BigDecimal cost = new BigDecimal("2920.00");
            String eventName = "Emily Brown Wedding";

            if (match.isPresent()) {
                Map<String, Object> b = match.get();
                if (b.get("totalPrice") instanceof Number num) {
                    revenue = new BigDecimal(num.toString());
                }
                if (b.get("estimatedCost") instanceof Number num) {
                    cost = new BigDecimal(num.toString());
                }
                eventName = (String) b.getOrDefault("customerName", eventName);
            }

            BigDecimal profit = FinancialMath.calculateProfit(revenue, cost);
            BigDecimal margin = FinancialMath.calculateMargin(revenue, cost);

            Map<String, Object> resultData = new LinkedHashMap<>();
            resultData.put("bookingId", targetId);
            resultData.put("eventName", eventName);
            resultData.put("revenue", revenue);
            resultData.put("estimatedCost", cost);
            resultData.put("estimatedProfit", profit);
            resultData.put("estimatedMargin", margin);
            resultData.put("isDemoData", true);

            return ToolResult.ok(resultData, "Profitability calculated (Demo Data)");
        }

        return ToolResult.error("Profitability calculation service is unavailable.");
    }
}
