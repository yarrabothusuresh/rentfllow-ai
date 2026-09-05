package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Quote;
import com.rentflow.ai.model.QuoteStatus;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetQuotesAwaitingResponseTool implements AiSalesTool {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;

    public GetQuotesAwaitingResponseTool(QuoteRepository quoteRepository, CustomerRepository customerRepository) {
        this.quoteRepository = quoteRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public String getName() {
        return "getQuotesAwaitingResponse";
    }

    @Override
    public String getDescription() {
        return "Lists pending quotes that were sent to customers and await response or contract approval.";
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
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        try {
            List<Quote> quotes = quoteRepository.findByTenantIdAndStatus(tenantId, QuoteStatus.SENT);
            LocalDate today = LocalDate.now();

            List<Map<String, Object>> rows = new ArrayList<>();
            for (Quote q : quotes) {
                Customer c = (q.getCustomerId() != null) ? customerRepository.findById(q.getCustomerId()).orElse(null) : null;
                String customerName = (c != null) ? (c.getCompanyName() != null && !c.getCompanyName().isBlank() ? c.getCompanyName() : (c.getFirstName() + " " + c.getLastName())) : "Unknown Customer";

                long daysWaiting = (q.getQuoteDate() != null) ? ChronoUnit.DAYS.between(q.getQuoteDate(), today) : 0;
                boolean expiringSoon = (q.getValidUntil() != null && !q.getValidUntil().isBefore(today) && ChronoUnit.DAYS.between(today, q.getValidUntil()) <= 3);

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("quoteId", q.getId().toString());
                map.put("quoteNumber", q.getQuoteNumber());
                map.put("customerName", customerName);
                map.put("totalAmount", q.getTotalAmount());
                map.put("quoteDate", q.getQuoteDate() != null ? q.getQuoteDate().toString() : "N/A");
                map.put("validUntil", q.getValidUntil() != null ? q.getValidUntil().toString() : "N/A");
                map.put("daysWaiting", daysWaiting);
                map.put("expiringSoon", expiringSoon);
                rows.add(map);
            }

            rows.sort((a, b) -> Long.compare((Long) b.get("daysWaiting"), (Long) a.get("daysWaiting")));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalQuotesAwaitingResponse", quotes.size());
            result.put("quotes", rows);

            return ToolCallResultDTO.success(getName(), result, true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch pending quotes: " + e.getMessage(), true);
        }
    }
}
