package com.rentflow.analytics.service;

import com.rentflow.analytics.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessInsightService {

    @Autowired
    private ProfitabilityService profitabilityService;

    @Autowired
    private InventoryAnalyticsService inventoryAnalyticsService;

    @Autowired
    private OperationsAnalyticsService operationsAnalyticsService;

    public List<BusinessInsightDTO> getStructuredBusinessInsights(String tenantId) {
        List<BusinessInsightDTO> insights = new ArrayList<>();

        // 1. High Demand Expansion Opportunity
        InventoryUtilizationDTO util = inventoryAnalyticsService.getInventoryUtilization(tenantId, DateRangeType.THIS_MONTH, null, null);
        for (ConflictDemandProductDTO c : util.getHighDemandConflictProducts()) {
            BusinessInsightDTO ins = new BusinessInsightDTO(
                    "FLEET_EXPANSION_OPPORTUNITY",
                    "High Demand & Fleet Capacity Constraint: " + c.getName(),
                    c.getName() + " is operating at " + c.getCurrentUtilizationPercent() + "% utilization with " + c.getConflictIncidentsCount() + " conflict incidents. Estimated $ " + c.getPotentialRevenueOpportunity() + " in unmet demand.",
                    "INVENTORY",
                    "OPPORTUNITY",
                    c.getPotentialRevenueOpportunity()
            );
            ins.getContextData().put("productId", c.getProductId());
            ins.getContextData().put("sku", c.getSku());
            ins.getContextData().put("utilization", c.getCurrentUtilizationPercent());
            insights.add(ins);
            break; // take top
        }

        // 2. Negative Margin Booking Risk
        List<BookingProfitabilityDTO> bookings = profitabilityService.getBookingProfitabilityList(tenantId, DateRangeType.THIS_MONTH, null, null);
        for (BookingProfitabilityDTO b : bookings) {
            if ("NEGATIVE_MARGIN".equals(b.getMarginFlag())) {
                BusinessInsightDTO ins = new BusinessInsightDTO(
                        "LOSS_MAKING_BOOKING_ALERT",
                        "Negative Margin Booking Detected: " + b.getBookingNumber(),
                        "Booking " + b.getBookingNumber() + " for " + b.getCustomerName() + " has total revenue of $" + b.getTotalRevenue() + " against estimated direct cost of $" + b.getDirectCost() + " (Gross Margin: " + b.getGrossMarginPercent() + "%).",
                        "PROFITABILITY",
                        "CRITICAL",
                        b.getGrossProfit().abs()
                );
                ins.getContextData().put("bookingId", b.getBookingId());
                ins.getContextData().put("bookingNumber", b.getBookingNumber());
                ins.getContextData().put("lossAmount", b.getGrossProfit().abs());
                insights.add(ins);
                break; // take top
            }
        }

        // 3. Underutilized Inventory Capital
        for (UnderutilizedProductDTO u : util.getUnderutilizedProducts()) {
            if (u.getInventoryAssetValue().compareTo(new BigDecimal("1000.00")) > 0) {
                BusinessInsightDTO ins = new BusinessInsightDTO(
                        "UNDERUTILIZED_ASSET_CAPITAL",
                        "Low Utilization Fleet Capital: " + u.getName(),
                        u.getName() + " has $" + u.getInventoryAssetValue() + " tied up in inventory with only " + u.getUtilizationPercent() + "% utilization this period. Action: " + u.getSuggestedAction(),
                        "INVENTORY",
                        "WARNING",
                        u.getInventoryAssetValue()
                );
                ins.getContextData().put("productId", u.getProductId());
                ins.getContextData().put("inventoryValue", u.getInventoryAssetValue());
                insights.add(ins);
                break;
            }
        }

        // 4. Overdue AR Aging Risk
        PaymentAndArAnalyticsDTO ar = operationsAnalyticsService.getPaymentAndArAnalytics(tenantId, DateRangeType.THIS_MONTH, null, null);
        if (ar.getTotalOverdueAmount().compareTo(BigDecimal.ZERO) > 0) {
            BusinessInsightDTO ins = new BusinessInsightDTO(
                    "OVERDUE_RECEIVABLES_EXPOSURE",
                    "Accounts Receivable Overdue Exposure: $" + ar.getTotalOverdueAmount(),
                    "Total outstanding invoices exceed due dates by $" + ar.getTotalOverdueAmount() + ". Overall payment collection rate is " + ar.getCollectionRatePercent() + "%.",
                    "REVENUE",
                    "WARNING",
                    ar.getTotalOverdueAmount()
            );
            ins.getContextData().put("overdueAmount", ar.getTotalOverdueAmount());
            ins.getContextData().put("collectionRate", ar.getCollectionRatePercent());
            insights.add(ins);
        }

        return insights;
    }
}
