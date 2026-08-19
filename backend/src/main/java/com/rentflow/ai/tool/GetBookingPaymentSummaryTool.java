package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.service.PaymentService;
import com.rentflow.ai.service.BookingService;
import com.rentflow.ai.dto.BookingDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GetBookingPaymentSummaryTool implements AITool {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public GetBookingPaymentSummaryTool(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @Override
    public String getName() {
        return "getBookingPaymentSummary";
    }

    @Override
    public String getDescription() {
        return "Retrieves the financial status, booking total, deposit, amount paid, and outstanding balance for a customer or booking. Read-only inquiry tool.";
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
            String bookingIdStr = (String) parameters.get("bookingId");
            String query = (String) parameters.get("query");

            UUID targetBookingId = null;

            if (bookingIdStr != null && !bookingIdStr.isBlank()) {
                targetBookingId = UUID.fromString(bookingIdStr);
            } else {
                // Try searching bookings by customer or query
                List<BookingDTO> bookings = bookingService.getBookings(tenantId, userRole);
                if (query != null && !query.isBlank()) {
                    String qLower = query.toLowerCase();
                    for (BookingDTO b : bookings) {
                        if ((b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(qLower)) ||
                            (b.getBookingNumber() != null && b.getBookingNumber().toLowerCase().contains(qLower)) ||
                            (b.getEventName() != null && b.getEventName().toLowerCase().contains(qLower))) {
                            targetBookingId = b.getId();
                            break;
                        }
                    }
                }
                if (targetBookingId == null && !bookings.isEmpty()) {
                    targetBookingId = bookings.get(0).getId();
                }
            }

            if (targetBookingId == null) {
                return ToolResult.error("No matching booking found to calculate financial summary.");
            }

            BookingFinancialSummaryDTO summary = paymentService.getFinancialSummary(tenantId, targetBookingId);
            return ToolResult.ok(summary, String.format("Booking total: $%s, Paid: $%s, Outstanding balance: $%s (Status: %s)",
                    summary.getBookingTotal(), summary.getAmountPaid(), summary.getOutstandingBalance(), summary.getPaymentStatus()));

        } catch (Exception e) {
            return ToolResult.error("Failed to fetch payment summary: " + e.getMessage());
        }
    }
}
