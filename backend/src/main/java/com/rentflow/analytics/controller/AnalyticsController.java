package com.rentflow.analytics.controller;

import com.rentflow.analytics.dto.*;
import com.rentflow.analytics.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    @Autowired
    private ProfitabilityService profitabilityService;

    @Autowired
    private InventoryAnalyticsService inventoryAnalyticsService;

    @Autowired
    private CustomerAnalyticsService customerAnalyticsService;

    @Autowired
    private QuoteAnalyticsService quoteAnalyticsService;

    @Autowired
    private OperationsAnalyticsService operationsAnalyticsService;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    @Autowired
    private BusinessInsightService businessInsightService;

    private String getTenantId(String headerTenantId) {
        return (headerTenantId != null && !headerTenantId.trim().isEmpty()) ? headerTenantId : "tenant-1";
    }

    private void enforceInternalStaffRole(String roleHeader) {
        if (roleHeader != null) {
            String r = roleHeader.toUpperCase().trim();
            if ("CUSTOMER".equals(r) || "DRIVER".equals(r)) {
                throw new SecurityException("Access to internal analytics and profitability is restricted.");
            }
        }
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ExecutiveDashboardDTO> getDashboard(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(analyticsService.getExecutiveDashboard(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/summary")
    public ResponseEntity<ExecutiveDashboardDTO> getSummary(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(analyticsService.getExecutiveDashboard(tenantId, rangeType, null, null));
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueAnalyticsDTO> getRevenue(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(revenueAnalyticsService.getRevenueAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/revenue/trend")
    public ResponseEntity<List<RevenueTrendItemDTO>> getRevenueTrend(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(revenueAnalyticsService.getRevenueAnalytics(tenantId, DateRangeType.THIS_MONTH, null, null).getRevenueTrend());
    }

    @GetMapping("/profitability")
    public ResponseEntity<List<BookingProfitabilityDTO>> getProfitability(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(profitabilityService.getBookingProfitabilityList(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingProfitabilityDTO>> getBookingsProfitability(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(profitabilityService.getBookingProfitabilityList(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingProfitDetailDTO> getBookingProfitDetail(
            @PathVariable("id") UUID bookingId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(profitabilityService.getBookingProfitDetail(tenantId, bookingId));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductProfitabilityDTO>> getProductsProfitability(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(profitabilityService.getProductProfitabilityList(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductProfitDetailDTO> getProductProfitDetail(
            @PathVariable("id") UUID productId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(profitabilityService.getProductProfitDetail(tenantId, productId));
    }

    @GetMapping("/utilization")
    public ResponseEntity<InventoryUtilizationDTO> getUtilization(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(inventoryAnalyticsService.getInventoryUtilization(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerAnalyticsDTO>> getCustomers(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(customerAnalyticsService.getCustomerAnalyticsList(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerAnalyticsDetailDTO> getCustomerDetail(
            @PathVariable("id") UUID customerId,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(customerAnalyticsService.getCustomerAnalyticsDetail(tenantId, customerId));
    }

    @GetMapping("/quotes")
    public ResponseEntity<QuoteAnalyticsDTO> getQuotes(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(quoteAnalyticsService.getQuoteAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/warehouse")
    public ResponseEntity<WarehouseAnalyticsDTO> getWarehouse(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(operationsAnalyticsService.getWarehouseAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/delivery")
    public ResponseEntity<DeliveryAnalyticsDTO> getDelivery(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(operationsAnalyticsService.getDeliveryAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/returns")
    public ResponseEntity<ReturnAndDamageAnalyticsDTO> getReturns(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(operationsAnalyticsService.getReturnAndDamageAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/damage")
    public ResponseEntity<ReturnAndDamageAnalyticsDTO> getDamage(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(operationsAnalyticsService.getReturnAndDamageAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentAndArAnalyticsDTO> getPayments(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(operationsAnalyticsService.getPaymentAndArAnalytics(tenantId, rangeType, startDate, endDate));
    }

    @GetMapping("/conflicts")
    public ResponseEntity<List<ConflictDemandProductDTO>> getConflicts(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(inventoryAnalyticsService.getInventoryUtilization(tenantId, DateRangeType.THIS_MONTH, null, null).getHighDemandConflictProducts());
    }

    @GetMapping("/insights")
    public ResponseEntity<List<BusinessInsightDTO>> getInsights(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        return ResponseEntity.ok(businessInsightService.getStructuredBusinessInsights(tenantId));
    }

    @GetMapping("/export/{reportType}")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable("reportType") String reportType,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(value = "range", required = false) DateRangeType rangeType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        enforceInternalStaffRole(roleHeader);
        String tenantId = getTenantId(tenantHeader);
        String csv = analyticsExportService.exportReportCsv(tenantId, reportType, rangeType, startDate, endDate);

        byte[] output = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "rentflow-" + reportType + "-report.csv");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }
}
