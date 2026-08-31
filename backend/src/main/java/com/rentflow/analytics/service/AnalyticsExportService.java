package com.rentflow.analytics.service;

import com.rentflow.analytics.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsExportService {

    @Autowired
    private ProfitabilityService profitabilityService;

    @Autowired
    private CustomerAnalyticsService customerAnalyticsService;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    @Autowired
    private InventoryAnalyticsService inventoryAnalyticsService;

    @Autowired
    private OperationsAnalyticsService operationsAnalyticsService;

    public String exportReportCsv(String tenantId, String reportType, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        if (reportType == null) reportType = "bookings";
        String type = reportType.toLowerCase().trim();

        switch (type) {
            case "products":
                return exportProductsCsv(tenantId, rangeType, customStart, customEnd);
            case "customers":
                return exportCustomersCsv(tenantId, rangeType, customStart, customEnd);
            case "revenue":
                return exportRevenueCsv(tenantId, rangeType, customStart, customEnd);
            case "utilization":
                return exportUtilizationCsv(tenantId, rangeType, customStart, customEnd);
            case "ar":
            case "receivables":
                return exportArCsv(tenantId, rangeType, customStart, customEnd);
            case "damage":
                return exportDamageCsv(tenantId, rangeType, customStart, customEnd);
            case "bookings":
            default:
                return exportBookingsCsv(tenantId, rangeType, customStart, customEnd);
        }
    }

    private String exportBookingsCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        List<BookingProfitabilityDTO> list = profitabilityService.getBookingProfitabilityList(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Booking Number,Customer Name,Event Name,Booking Date,Status,Total Revenue,Direct Cost,Gross Profit,Margin Percent,Margin Flag\n");
        for (BookingProfitabilityDTO b : list) {
            sb.append(escape(b.getBookingNumber())).append(",")
                    .append(escape(b.getCustomerName())).append(",")
                    .append(escape(b.getEventName())).append(",")
                    .append(b.getBookingDate() != null ? b.getBookingDate().toString() : "").append(",")
                    .append(escape(b.getStatus())).append(",")
                    .append(b.getTotalRevenue()).append(",")
                    .append(b.getDirectCost()).append(",")
                    .append(b.getGrossProfit()).append(",")
                    .append(b.getGrossMarginPercent()).append("%,")
                    .append(b.getMarginFlag()).append("\n");
        }
        return sb.toString();
    }

    private String exportProductsCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        List<ProductProfitabilityDTO> list = profitabilityService.getProductProfitabilityList(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("SKU,Product Name,Category,Quantity Owned,Rental Count,Quantity Rented,Utilization Percent,Rental Revenue,Direct Cost,Repair Cost,Gross Profit,Margin Percent\n");
        for (ProductProfitabilityDTO p : list) {
            sb.append(escape(p.getSku())).append(",")
                    .append(escape(p.getName())).append(",")
                    .append(escape(p.getCategoryName())).append(",")
                    .append(p.getQuantityOwned()).append(",")
                    .append(p.getRentalCount()).append(",")
                    .append(p.getTotalQuantityRented()).append(",")
                    .append(p.getUtilizationPercent()).append("%,")
                    .append(p.getRentalRevenue()).append(",")
                    .append(p.getDirectCost()).append(",")
                    .append(p.getRepairCost()).append(",")
                    .append(p.getProfit()).append(",")
                    .append(p.getMarginPercent()).append("%\n");
        }
        return sb.toString();
    }

    private String exportCustomersCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        List<CustomerAnalyticsDTO> list = customerAnalyticsService.getCustomerAnalyticsList(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Customer Number,Customer Name,Company Name,Customer Type,Total Bookings,Lifetime Revenue,Lifetime Collected,Outstanding Balance,Lifetime Profit,Margin Percent,Quote Conversion\n");
        for (CustomerAnalyticsDTO c : list) {
            sb.append(escape(c.getCustomerNumber())).append(",")
                    .append(escape(c.getCustomerName())).append(",")
                    .append(escape(c.getCompanyName())).append(",")
                    .append(escape(c.getCustomerType())).append(",")
                    .append(c.getTotalBookingsCount()).append(",")
                    .append(c.getLifetimeRevenue()).append(",")
                    .append(c.getLifetimeCollected()).append(",")
                    .append(c.getOutstandingBalance()).append(",")
                    .append(c.getLifetimeProfit()).append(",")
                    .append(c.getLifetimeMarginPercent()).append("%,")
                    .append(c.getQuoteConversionRate()).append("%\n");
        }
        return sb.toString();
    }

    private String exportRevenueCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        RevenueAnalyticsDTO rev = revenueAnalyticsService.getRevenueAnalytics(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Period,Booked Revenue,Invoiced Revenue,Collected Revenue,Estimated Gross Profit,Bookings Count\n");
        for (RevenueTrendItemDTO r : rev.getRevenueTrend()) {
            sb.append(r.getPeriod()).append(",")
                    .append(r.getBookedRevenue()).append(",")
                    .append(r.getInvoicedRevenue()).append(",")
                    .append(r.getCollectedRevenue()).append(",")
                    .append(r.getGrossProfit()).append(",")
                    .append(r.getBookingsCount()).append("\n");
        }
        return sb.toString();
    }

    private String exportUtilizationCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        InventoryUtilizationDTO util = inventoryAnalyticsService.getInventoryUtilization(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Product SKU,Product Name,Category,Quantity Owned,Rental Revenue,Utilization Percent,Margin Percent\n");
        for (ProductProfitabilityDTO p : util.getTopUtilizedProducts()) {
            sb.append(escape(p.getSku())).append(",")
                    .append(escape(p.getName())).append(",")
                    .append(escape(p.getCategoryName())).append(",")
                    .append(p.getQuantityOwned()).append(",")
                    .append(p.getRentalRevenue()).append(",")
                    .append(p.getUtilizationPercent()).append("%,")
                    .append(p.getMarginPercent()).append("%\n");
        }
        return sb.toString();
    }

    private String exportDamageCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        ReturnAndDamageAnalyticsDTO dmg = operationsAnalyticsService.getReturnAndDamageAnalytics(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Total Returns Processed,").append(dmg.getTotalReturnsProcessed()).append("\n");
        sb.append("Total Returned Units,").append(dmg.getTotalReturnedUnits()).append("\n");
        sb.append("Damaged Units Count,").append(dmg.getDamagedUnitsCount()).append("\n");
        sb.append("Damage Rate Percent,").append(dmg.getDamageRatePercent()).append("%\n");
        sb.append("Estimated Damage Cost,").append(dmg.getEstimatedDamageCost()).append("\n");
        sb.append("Approved Damage Cost,").append(dmg.getApprovedDamageCost()).append("\n");
        sb.append("Actual Repair Cost,").append(dmg.getActualRepairCost()).append("\n");
        return sb.toString();
    }

    private String exportArCsv(String tenantId, DateRangeType rangeType, LocalDate customStart, LocalDate customEnd) {
        PaymentAndArAnalyticsDTO ar = operationsAnalyticsService.getPaymentAndArAnalytics(tenantId, rangeType, customStart, customEnd);
        StringBuilder sb = new StringBuilder();
        sb.append("Aging Bucket,Amount,Invoice Count,Percentage Of Total\n");
        for (ArAgingBucketDTO b : ar.getArAgingBuckets()) {
            sb.append(escape(b.getBucketName())).append(",")
                    .append(b.getAmount()).append(",")
                    .append(b.getInvoiceCount()).append(",")
                    .append(b.getPercentageOfTotal()).append("%\n");
        }
        return sb.toString();
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
