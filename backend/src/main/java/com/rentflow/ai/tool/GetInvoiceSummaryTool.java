package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.service.InvoiceService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GetInvoiceSummaryTool implements AITool {

    private final InvoiceService invoiceService;

    public GetInvoiceSummaryTool(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public String getName() {
        return "getInvoiceSummary";
    }

    @Override
    public String getDescription() {
        return "Queries and summarizes tenant invoices, overdue balances, customer unpaid totals, or status filters. Read-only inquiry tool for AI Copilot.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "FINANCE", "WAREHOUSE", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            String statusStr = (String) parameters.get("status");
            String search = (String) parameters.get("search");

            InvoiceStatus status = null;
            if (statusStr != null && !statusStr.isBlank()) {
                status = InvoiceStatus.valueOf(statusStr.toUpperCase());
            }

            List<InvoiceDTO> invoices = invoiceService.listInvoices(tenantId, status, null, null, search, userRole);

            long totalCount = invoices.size();
            long overdueCount = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.OVERDUE).count();
            long unpaidCount = invoices.stream().filter(i -> i.getStatus() != InvoiceStatus.PAID && i.getStatus() != InvoiceStatus.VOID).count();

            return ToolResult.ok(
                Map.of(
                    "totalInvoices", totalCount,
                    "overdueCount", overdueCount,
                    "unpaidCount", unpaidCount,
                    "invoices", invoices
                ),
                String.format("Found %d invoices (%d unpaid, %d overdue).", totalCount, unpaidCount, overdueCount)
            );
        } catch (Exception e) {
            return ToolResult.error("Failed to fetch invoice summary: " + e.getMessage());
        }
    }
}
