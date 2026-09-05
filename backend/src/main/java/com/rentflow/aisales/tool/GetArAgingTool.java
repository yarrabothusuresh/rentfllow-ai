package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetArAgingTool implements AiSalesTool {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    public GetArAgingTool(InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public String getName() {
        return "getArAging";
    }

    @Override
    public String getDescription() {
        return "Calculates accounts receivable aging buckets (0-30, 31-60, 61-90, 90+ days) and lists top debtor accounts.";
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
        try {
            List<Invoice> invoices = invoiceRepository.findByTenantId(tenantId);
            LocalDate today = LocalDate.now();

            BigDecimal currentDue = BigDecimal.ZERO; // Not yet overdue or 0-30 days
            BigDecimal days31To60 = BigDecimal.ZERO;
            BigDecimal days61To90 = BigDecimal.ZERO;
            BigDecimal days90Plus = BigDecimal.ZERO;
            BigDecimal totalOverdue = BigDecimal.ZERO;
            BigDecimal totalOutstanding = BigDecimal.ZERO;

            Map<UUID, BigDecimal> customerBalances = new HashMap<>();
            List<Map<String, Object>> overdueInvoicesList = new ArrayList<>();

            for (Invoice inv : invoices) {
                if (inv.getStatus() == InvoiceStatus.PAID || inv.getStatus() == InvoiceStatus.VOID) continue;
                BigDecimal bal = inv.getBalanceDue() != null ? inv.getBalanceDue() : BigDecimal.ZERO;
                if (bal.compareTo(BigDecimal.ZERO) <= 0) continue;

                totalOutstanding = totalOutstanding.add(bal);

                if (inv.getDueDate() != null && inv.getDueDate().isBefore(today)) {
                    long days = ChronoUnit.DAYS.between(inv.getDueDate(), today);
                    totalOverdue = totalOverdue.add(bal);

                    if (days <= 30) {
                        currentDue = currentDue.add(bal);
                    } else if (days <= 60) {
                        days31To60 = days31To60.add(bal);
                    } else if (days <= 90) {
                        days61To90 = days61To90.add(bal);
                    } else {
                        days90Plus = days90Plus.add(bal);
                    }

                    if (inv.getCustomerId() != null) {
                        customerBalances.merge(inv.getCustomerId(), bal, BigDecimal::add);
                    }

                    Map<String, Object> invMap = new LinkedHashMap<>();
                    invMap.put("invoiceNumber", inv.getInvoiceNumber());
                    invMap.put("dueDate", inv.getDueDate().toString());
                    invMap.put("daysPastDue", days);
                    invMap.put("balanceDue", bal);
                    invMap.put("customerId", inv.getCustomerId());
                    overdueInvoicesList.add(invMap);
                }
            }

            BigDecimal overdueOver30Days = days31To60.add(days61To90).add(days90Plus);

            // Resolve top debtor customer names
            List<Map<String, Object>> topDebtors = customerBalances.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .map(e -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        Customer c = customerRepository.findById(e.getKey()).orElse(null);
                        String name = c != null ? (c.getCompanyName() != null && !c.getCompanyName().isBlank() ? c.getCompanyName() : (c.getFirstName() + " " + c.getLastName())) : "Unknown Customer";
                        map.put("customerId", e.getKey().toString());
                        map.put("customerName", name);
                        map.put("overdueBalance", e.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalOutstanding", totalOutstanding);
            result.put("totalOverdue", totalOverdue);
            result.put("bucket0to30Days", currentDue);
            result.put("bucket31to60Days", days31To60);
            result.put("bucket61to90Days", days61To90);
            result.put("bucket90PlusDays", days90Plus);
            result.put("totalOverdueMoreThan30Days", overdueOver30Days);
            result.put("topDebtorCustomers", topDebtors);
            result.put("sampleOverdueInvoices", overdueInvoicesList.stream().limit(5).collect(Collectors.toList()));

            return ToolCallResultDTO.success(getName(), result, true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to calculate AR aging: " + e.getMessage(), true);
        }
    }
}
