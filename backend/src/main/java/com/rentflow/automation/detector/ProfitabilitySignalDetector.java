package com.rentflow.automation.detector;

import com.rentflow.analytics.dto.BookingProfitabilityDTO;
import com.rentflow.analytics.dto.DateRangeType;
import com.rentflow.analytics.service.ProfitabilityService;
import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProfitabilitySignalDetector implements BusinessSignalDetector {

    @Autowired
    private ProfitabilityService profitabilityService;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.BOOKING_NEGATIVE_MARGIN,
            BusinessSignalType.BOOKING_MARGIN_LOW
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        try {
            List<BookingProfitabilityDTO> list = profitabilityService.getBookingProfitabilityList(tenantId, DateRangeType.THIS_YEAR, null, null);
            if (list == null) return signals;

            for (BookingProfitabilityDTO b : list) {
                if (b.getBookingId() == null) continue;

                boolean isNegative = "NEGATIVE_MARGIN".equalsIgnoreCase(b.getMarginFlag()) || 
                                     (b.getGrossProfit() != null && b.getGrossProfit().compareTo(BigDecimal.ZERO) < 0);
                boolean isLow = "LOW_MARGIN".equalsIgnoreCase(b.getMarginFlag()) ||
                                (b.getGrossMarginPercent() != null && b.getGrossMarginPercent().compareTo(new BigDecimal("15.00")) < 0 && !isNegative);

                if (isNegative) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("bookingNumber", b.getBookingNumber());
                    evidence.put("customerName", b.getCustomerName());
                    evidence.put("totalRevenue", b.getTotalRevenue() != null ? b.getTotalRevenue().toString() : "0.00");
                    evidence.put("directCost", b.getDirectCost() != null ? b.getDirectCost().toString() : "0.00");
                    evidence.put("grossProfit", b.getGrossProfit() != null ? b.getGrossProfit().toString() : "0.00");
                    evidence.put("grossMarginPercent", b.getGrossMarginPercent() != null ? b.getGrossMarginPercent().toString() : "0.00");
                    evidence.put("marginFlag", b.getMarginFlag());

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("taskTitle", "CRITICAL: Negative Margin on Booking " + b.getBookingNumber());
                    payload.put("entityType", "BOOKING");
                    payload.put("entityId", b.getBookingId().toString());
                    payload.put("priority", "CRITICAL");
                    payload.put("assignedRole", "ROLE_ADMIN");

                    signals.add(new DetectedSignal(
                        BusinessSignalType.BOOKING_NEGATIVE_MARGIN,
                        BusinessSignalCategory.PROFITABILITY,
                        "BOOKING",
                        b.getBookingId().toString(),
                        b.getBookingNumber(),
                        BusinessSignalSeverity.CRITICAL,
                        tenantId + ":BOOKING_NEGATIVE_MARGIN:" + b.getBookingId(),
                        evidence,
                        AutomationActionType.CREATE_INTERNAL_TASK,
                        payload,
                        "Negative margin detected on Booking " + b.getBookingNumber(),
                        "Booking " + b.getBookingNumber() + " produces a negative net profit of $" + b.getGrossProfit() + " (costs $" + b.getDirectCost() + " exceed revenue $" + b.getTotalRevenue() + ").",
                        "Negative margin rentals cause direct financial loss to the business and require urgent management escalation."
                    ));
                } else if (isLow) {
                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("bookingNumber", b.getBookingNumber());
                    evidence.put("customerName", b.getCustomerName());
                    evidence.put("totalRevenue", b.getTotalRevenue() != null ? b.getTotalRevenue().toString() : "0.00");
                    evidence.put("directCost", b.getDirectCost() != null ? b.getDirectCost().toString() : "0.00");
                    evidence.put("grossProfit", b.getGrossProfit() != null ? b.getGrossProfit().toString() : "0.00");
                    evidence.put("grossMarginPercent", b.getGrossMarginPercent() != null ? b.getGrossMarginPercent().toString() : "0.00");
                    evidence.put("thresholdPercent", "15.00");

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("taskTitle", "Review Low Margin Booking " + b.getBookingNumber());
                    payload.put("entityType", "BOOKING");
                    payload.put("entityId", b.getBookingId().toString());
                    payload.put("priority", "HIGH");
                    payload.put("assignedRole", "ROLE_MANAGER");

                    signals.add(new DetectedSignal(
                        BusinessSignalType.BOOKING_MARGIN_LOW,
                        BusinessSignalCategory.PROFITABILITY,
                        "BOOKING",
                        b.getBookingId().toString(),
                        b.getBookingNumber(),
                        BusinessSignalSeverity.HIGH,
                        tenantId + ":BOOKING_MARGIN_LOW:" + b.getBookingId(),
                        evidence,
                        AutomationActionType.CREATE_INTERNAL_TASK,
                        payload,
                        "Low profit margin on Booking " + b.getBookingNumber(),
                        "Booking " + b.getBookingNumber() + " has gross margin of " + b.getGrossMarginPercent() + "%, which is below the 15% minimum healthy target.",
                        "Low margin bookings restrict operational capacity and should be reviewed for price/cost optimization."
                    ));
                }
            }
        } catch (Exception e) {
            // Log and preserve stability
        }
        return signals;
    }
}
