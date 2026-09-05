package com.rentflow.aisales.service;

import com.rentflow.ai.dto.BookingAttentionItemDTO;
import com.rentflow.ai.model.BookingAttentionSignal;
import com.rentflow.ai.service.BookingAttentionService;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.CopilotIntent;
import com.rentflow.analytics.dto.DateRangeType;
import com.rentflow.analytics.dto.ExecutiveDashboardDTO;
import com.rentflow.analytics.service.AnalyticsService;
import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.LeadFollowUp;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.warehouse.model.WarehouseException;
import com.rentflow.warehouse.model.WarehouseExceptionStatus;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseExceptionRepository;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CopilotBriefingService {

    private final AnalyticsService analyticsService;
    private final BookingAttentionService bookingAttentionService;
    private final LeadFollowUpRepository leadFollowUpRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final WarehouseExceptionRepository warehouseExceptionRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;

    public CopilotBriefingService(AnalyticsService analyticsService,
                                  BookingAttentionService bookingAttentionService,
                                  LeadFollowUpRepository leadFollowUpRepository,
                                  WarehouseOrderRepository warehouseOrderRepository,
                                  WarehouseExceptionRepository warehouseExceptionRepository,
                                  DeliveryRepository deliveryRepository,
                                  InvoiceRepository invoiceRepository) {
        this.analyticsService = analyticsService;
        this.bookingAttentionService = bookingAttentionService;
        this.leadFollowUpRepository = leadFollowUpRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.warehouseExceptionRepository = warehouseExceptionRepository;
        this.deliveryRepository = deliveryRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public CopilotResponseDTO generateDailyBriefing(String tenantId, String userRole, String userId, String userName) {
        String role = (userRole != null ? userRole.toUpperCase() : "OWNER");
        String name = (userName != null && !userName.isBlank()) ? userName : role;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        CopilotResponseDTO res = new CopilotResponseDTO();
        res.setConversationId(UUID.randomUUID());
        res.setRole("assistant");
        res.setDetectedIntent(CopilotIntent.DAILY_BRIEFING);
        res.setDeterministic(true);
        res.setExplanation("Deterministic role-specific morning briefing synthesized from live operational, analytics, and CRM records.");

        // Fetch Attention Signals
        List<BookingAttentionItemDTO> attentionItems = bookingAttentionService.getBookingsNeedingAttention(tenantId, 1);
        long highSeverityCount = attentionItems.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();

        // 1. Role-specific synthesis
        switch (role) {
            case "SALES":
                buildSalesBriefing(tenantId, name, dateStr, res, attentionItems);
                break;
            case "OPERATIONS":
                buildOperationsBriefing(tenantId, name, dateStr, res, attentionItems);
                break;
            case "WAREHOUSE":
                buildWarehouseBriefing(tenantId, name, dateStr, res, attentionItems);
                break;
            case "FINANCE":
                buildFinanceBriefing(tenantId, name, dateStr, res);
                break;
            case "OWNER":
            case "ADMIN":
            default:
                buildExecutiveBriefing(tenantId, name, dateStr, res, attentionItems, highSeverityCount);
                break;
        }

        return res;
    }

    private void buildExecutiveBriefing(String tenantId, String name, String dateStr,
                                       CopilotResponseDTO res, List<BookingAttentionItemDTO> attentionItems,
                                       long highSeverityCount) {
        ExecutiveDashboardDTO dash = analyticsService.getExecutiveDashboard(tenantId, DateRangeType.THIS_MONTH, null, null);
        BigDecimal rev = (dash.getBookedRevenue() != null && dash.getBookedRevenue().getCurrentValue() != null)
                ? dash.getBookedRevenue().getCurrentValue() : BigDecimal.ZERO;
        BigDecimal margin = (dash.getGrossMargin() != null && dash.getGrossMargin().getCurrentValue() != null)
                ? dash.getGrossMargin().getCurrentValue() : new BigDecimal("0.0");

        // Overdue Invoices
        List<Invoice> invoices = invoiceRepository.findByTenantId(tenantId);
        BigDecimal overdueAmount = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.OVERDUE || (i.getDueDate() != null && i.getDueDate().isBefore(LocalDate.now()) && i.getBalanceDue().compareTo(BigDecimal.ZERO) > 0))
                .map(Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Open warehouse exceptions
        long openExceptions = warehouseExceptionRepository.countByTenantIdAndStatus(tenantId, WarehouseExceptionStatus.OPEN);

        StringBuilder sb = new StringBuilder();
        sb.append("Good morning, ").append(name).append("!\n\n");
        sb.append("Here is your **RentFlow Executive Morning Briefing** for ").append(dateStr).append(":\n\n");
        sb.append("• **Month-to-Date Revenue**: $").append(String.format("%,.2f", rev)).append(" (Gross Margin: ").append(margin).append("%)\n");
        sb.append("• **Operational Attention**: ").append(attentionItems.size()).append(" bookings flagged for action tomorrow (").append(highSeverityCount).append(" critical/high priority)\n");
        sb.append("• **Accounts Receivable**: $").append(String.format("%,.2f", overdueAmount)).append(" in past-due balances\n");
        sb.append("• **Warehouse Operations**: ").append(openExceptions).append(" unresolved fulfillment exceptions\n\n");

        if (attentionItems.isEmpty() && overdueAmount.compareTo(BigDecimal.ZERO) == 0) {
            sb.append("All operational pipelines and collections are healthy today.");
        } else {
            sb.append("Review the flagged items below to prevent fulfillment delays and accelerate overdue collections.");
        }
        res.setMessage(sb.toString());

        // KPIs Data Block
        Map<String, Object> kpiData = new LinkedHashMap<>();
        kpiData.put("MTD Booked Revenue", "$" + String.format("%,.2f", rev));
        kpiData.put("Gross Margin", margin + "%");
        kpiData.put("Bookings Needing Attention", attentionItems.size());
        kpiData.put("Overdue A/R", "$" + String.format("%,.2f", overdueAmount));
        kpiData.put("Open Warehouse Exceptions", openExceptions);
        res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Executive KPI Overview", kpiData));

        // Attention Table Block
        if (!attentionItems.isEmpty()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (BookingAttentionItemDTO item : attentionItems.stream().limit(5).collect(Collectors.toList())) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Booking", item.getBookingNumber());
                row.put("Customer", item.getCustomerName());
                row.put("Severity", item.getSeverity());
                row.put("Signals", item.getSignals().stream().map(Enum::name).collect(Collectors.joining(", ")));
                rows.add(row);
            }
            res.getDataBlocks().add(new CopilotDataBlockDTO("TABLE", "Top Bookings Requiring Attention Tomorrow", rows));
        }

        // Recommendations
        if (highSeverityCount > 0) {
            res.getRecommendations().add(new CopilotRecommendationDTO("Resolve Operational Blockers",
                    highSeverityCount + " bookings have high-severity inventory or dispatch issues before tomorrow.",
                    "HIGH", "Open Booking Attention Details"));
        }
        if (overdueAmount.compareTo(new BigDecimal("1000.00")) > 0) {
            res.getRecommendations().add(new CopilotRecommendationDTO("Review Overdue Invoices",
                    "Over $" + String.format("%,.0f", overdueAmount) + " overdue across active customers.",
                    "MEDIUM", "View Accounts Receivable Aging"));
        }

        // Sources
        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Executive Dashboard", "/analytics/executive", "ANALYTICS", "DASHBOARD"));
        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Operations Calendar", "/calendar", "OPERATIONS", "CALENDAR"));

        // Suggested Prompts
        res.setSuggestedPrompts(List.of(
                "Why did margin fall?",
                "Which bookings need attention tomorrow?",
                "How much is more than 30 days overdue?",
                "What is blocking the warehouse?"
        ));
    }

    private void buildSalesBriefing(String tenantId, String name, String dateStr,
                                    CopilotResponseDTO res, List<BookingAttentionItemDTO> attentionItems) {
        List<LeadFollowUp> openFollowUps = leadFollowUpRepository.findByTenantIdAndStatus(tenantId, FollowUpStatus.OPEN);
        long overdueFollowUps = openFollowUps.stream()
                .filter(f -> f.getDueAt() != null && f.getDueAt().isBefore(LocalDateTime.now()))
                .count();
        long dueToday = openFollowUps.stream()
                .filter(f -> f.getDueAt() != null && f.getDueAt().toLocalDate().isEqual(LocalDate.now()))
                .count();

        res.setMessage("Good morning, " + name + "! Here is your **Sales Pipeline Briefing** for " + dateStr + ":\n\n" +
                "• **Overdue Follow-ups**: " + overdueFollowUps + " require immediate customer outreach\n" +
                "• **Follow-ups Due Today**: " + dueToday + " scheduled tasks\n" +
                "• **Customer Blockers**: " + attentionItems.size() + " upcoming bookings have pending customer actions (deposits/contracts)\n\n" +
                "Focus on closing overdue follow-ups and confirming pending customer agreements.");

        Map<String, Object> kpiData = new LinkedHashMap<>();
        kpiData.put("Overdue Follow-ups", overdueFollowUps);
        kpiData.put("Due Today", dueToday);
        kpiData.put("Open Follow-ups Total", openFollowUps.size());
        res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Sales Activity Pipeline", kpiData));

        if (overdueFollowUps > 0) {
            res.getRecommendations().add(new CopilotRecommendationDTO("Clear Overdue Follow-ups",
                    overdueFollowUps + " sales outreach tasks have passed their target contact window.",
                    "HIGH", "Open CRM Follow-ups"));
        }

        res.getSourceReferences().add(new CopilotSourceReferenceDTO("CRM Pipeline", "/crm/leads", "CRM", "LEADS"));
        res.setSuggestedPrompts(List.of(
                "What should I follow up today?",
                "Which quotes need follow-up?",
                "Find customer Acme Corp"
        ));
    }

    private void buildOperationsBriefing(String tenantId, String name, String dateStr,
                                         CopilotResponseDTO res, List<BookingAttentionItemDTO> attentionItems) {
        LocalDate today = LocalDate.now();
        List<Delivery> todayDeliveries = deliveryRepository.findByTenantIdAndScheduledDate(tenantId, today);
        long unassignedDrivers = todayDeliveries.stream().filter(d -> d.getDriverId() == null).count();
        long unassignedVehicles = todayDeliveries.stream().filter(d -> d.getVehicleId() == null).count();

        res.setMessage("Good morning, " + name + "! Here is your **Logistics & Operations Briefing** for " + dateStr + ":\n\n" +
                "• **Deliveries Today**: " + todayDeliveries.size() + " scheduled dispatches\n" +
                "• **Unassigned Drivers**: " + unassignedDrivers + " deliveries lack an assigned driver\n" +
                "• **Unassigned Vehicles**: " + unassignedVehicles + " deliveries lack an assigned vehicle\n" +
                "• **Bookings Needing Attention**: " + attentionItems.size() + " have operational flags\n\n" +
                (unassignedDrivers > 0 ? "⚠️ Immediate dispatch action needed: Assign drivers to pending routes." : "Fleet and driver assignments are currently on schedule."));

        Map<String, Object> kpiData = new LinkedHashMap<>();
        kpiData.put("Today's Deliveries", todayDeliveries.size());
        kpiData.put("Unassigned Drivers", unassignedDrivers);
        kpiData.put("Unassigned Vehicles", unassignedVehicles);
        kpiData.put("Operational Flags", attentionItems.size());
        res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Fleet & Dispatch Status", kpiData));

        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Deliveries Board", "/deliveries", "DELIVERY", "DISPATCH"));
        res.setSuggestedPrompts(List.of(
                "Are any deliveries at risk tomorrow?",
                "Which bookings need attention tomorrow?",
                "What is blocking the warehouse?"
        ));
    }

    private void buildWarehouseBriefing(String tenantId, String name, String dateStr,
                                       CopilotResponseDTO res, List<BookingAttentionItemDTO> attentionItems) {
        List<WarehouseOrder> orders = warehouseOrderRepository.findByTenantId(tenantId);
        long readyToPick = orders.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.READY_TO_PICK).count();
        long picking = orders.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.PICKING).count();
        long openExceptions = warehouseExceptionRepository.countByTenantIdAndStatus(tenantId, WarehouseExceptionStatus.OPEN);

        res.setMessage("Good morning, " + name + "! Here is your **Warehouse Fulfillment Briefing** for " + dateStr + ":\n\n" +
                "• **Ready to Pick**: " + readyToPick + " orders waiting for warehouse pick lists\n" +
                "• **In Progress Picking**: " + picking + " orders currently being picked\n" +
                "• **Open Exceptions**: " + openExceptions + " item shortages or damage issues reported\n\n" +
                (openExceptions > 0 ? "⚠️ Inspect open warehouse exceptions to authorize substitutions or repairs." : "Warehouse floor operations are clear."));

        Map<String, Object> kpiData = new LinkedHashMap<>();
        kpiData.put("Ready to Pick", readyToPick);
        kpiData.put("Picking In Progress", picking);
        kpiData.put("Open Exceptions", openExceptions);
        res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Warehouse Operations", kpiData));

        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Warehouse Fulfillment", "/warehouse", "WAREHOUSE", "ORDERS"));
        res.setSuggestedPrompts(List.of(
                "What is blocking the warehouse?",
                "Which products have availability conflicts?",
                "Which bookings need attention tomorrow?"
        ));
    }

    private void buildFinanceBriefing(String tenantId, String name, String dateStr, CopilotResponseDTO res) {
        List<Invoice> invoices = invoiceRepository.findByTenantId(tenantId);
        LocalDate today = LocalDate.now();

        BigDecimal overdue = BigDecimal.ZERO;
        BigDecimal due30Plus = BigDecimal.ZERO;
        int overdueCount = 0;

        for (Invoice inv : invoices) {
            if (inv.getStatus() == InvoiceStatus.PAID || inv.getStatus() == InvoiceStatus.VOID) continue;
            BigDecimal bal = inv.getBalanceDue() != null ? inv.getBalanceDue() : BigDecimal.ZERO;
            if (bal.compareTo(BigDecimal.ZERO) <= 0) continue;

            if (inv.getDueDate() != null && inv.getDueDate().isBefore(today)) {
                overdue = overdue.add(bal);
                overdueCount++;
                long daysPast = java.time.temporal.ChronoUnit.DAYS.between(inv.getDueDate(), today);
                if (daysPast > 30) {
                    due30Plus = due30Plus.add(bal);
                }
            }
        }

        res.setMessage("Good morning, " + name + "! Here is your **Finance & Receivables Briefing** for " + dateStr + ":\n\n" +
                "• **Total Overdue Invoices**: " + overdueCount + " invoices totaling $" + String.format("%,.2f", overdue) + "\n" +
                "• **Past Due > 30 Days**: $" + String.format("%,.2f", due30Plus) + " requiring escalation\n\n" +
                "Review the aging report below to take collection action on aged accounts.");

        Map<String, Object> kpiData = new LinkedHashMap<>();
        kpiData.put("Overdue Balance", "$" + String.format("%,.2f", overdue));
        kpiData.put("Overdue Invoices Count", overdueCount);
        kpiData.put("Overdue > 30 Days", "$" + String.format("%,.2f", due30Plus));
        res.getDataBlocks().add(new CopilotDataBlockDTO("KPI", "Receivables Overview", kpiData));

        res.getSourceReferences().add(new CopilotSourceReferenceDTO("Invoices & Billing", "/invoices", "FINANCE", "INVOICES"));
        res.setSuggestedPrompts(List.of(
                "How much is more than 30 days overdue?",
                "Which customers owe us the most?",
                "Why did margin fall?"
        ));
    }
}
