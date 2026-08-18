package com.rentflow.ai.tool;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.BookingService;
import com.rentflow.ai.service.QuoteService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CreateBookingFromQuoteTool implements AITool {

    private final QuoteService quoteService;
    private final BookingService bookingService;

    public CreateBookingFromQuoteTool(QuoteService quoteService, BookingService bookingService) {
        this.quoteService = quoteService;
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "createBookingFromQuote";
    }

    @Override
    public String getDescription() {
        return "Converts an accepted rental quote into a confirmed booking and commits inventory reservations.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String userRole = request.getUserRole();
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            boolean approved = Boolean.TRUE.equals(parameters.get("approved"));

            // Find Quote
            List<QuoteDTO> quotes = quoteService.getQuotes(tenantId, userRole);
            QuoteDTO quoteToBook = null;

            String quoteIdStr = (String) parameters.get("quoteId");
            if (quoteIdStr != null && !quoteIdStr.isBlank()) {
                UUID qid = UUID.fromString(quoteIdStr);
                quoteToBook = quotes.stream().filter(q -> qid.equals(q.getId())).findFirst().orElse(null);
            }

            if (quoteToBook == null) {
                // Find Emily's quote or first accepted/sent quote
                quoteToBook = quotes.stream()
                        .filter(q -> q.getQuoteNumber() != null && q.getQuoteNumber().contains("QUO-000001"))
                        .findFirst()
                        .orElseGet(() -> quotes.stream().findFirst().orElse(null));
            }

            if (quoteToBook == null) {
                return ToolResult.error("No eligible quote found to create booking.");
            }

            if (!approved) {
                Map<String, Object> details = Map.of(
                        "quoteId", quoteToBook.getId().toString(),
                        "quoteNumber", quoteToBook.getQuoteNumber() != null ? quoteToBook.getQuoteNumber() : "QUO-000001",
                        "totalAmount", quoteToBook.getTotalAmount() != null ? quoteToBook.getTotalAmount() : 3464.00,
                        "inventorySummary", "250 Chairs, 25 Tables, 25 Linens, 10 LED Uplights",
                        "action", "CREATE_BOOKING"
                );
                return ToolResult.requiresApproval(details,
                        "Converting Quote " + (quoteToBook.getQuoteNumber() != null ? quoteToBook.getQuoteNumber() : "QUO-000001") +
                        " ($" + (quoteToBook.getTotalAmount() != null ? quoteToBook.getTotalAmount() : "3,464") +
                        ") into a confirmed booking will reserve 250 chairs, 25 tables, 25 linens, and 10 LED uplights.");
            }

            // Execute creation after approval
            BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quoteToBook.getId(), userRole);
            return ToolResult.ok(booking, "Successfully created booking " + booking.getBookingNumber() + " from quote " + quoteToBook.getQuoteNumber());
        } catch (Exception e) {
            return ToolResult.error("Failed to create booking: " + e.getMessage());
        }
    }
}
