package com.rentflow.payment;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    private String tenantId;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        tenantId = "test-controller-tenant-" + UUID.randomUUID();

        booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-CTRL-" + System.currentTimeMillis());
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        booking.setSubtotal(new BigDecimal("2000.00"));
        booking.setTaxAmount(new BigDecimal("500.00"));
        booking.setTotalAmount(new BigDecimal("2500.00"));
        booking.setDepositRequired(new BigDecimal("750.00"));
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setBalanceDue(new BigDecimal("2500.00"));
        booking.setCreatedBy("OWNER");
        booking = bookingRepository.save(booking);
    }

    @Test
    @DisplayName("GET /api/bookings/{bookingId}/payments returns payment list")
    public void testGetPaymentsEndpoint() throws Exception {
        mockMvc.perform(get("/api/bookings/" + booking.getId() + "/payments")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-User-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/bookings/{bookingId}/payments records payment successfully")
    public void testRecordPaymentEndpoint() throws Exception {
        String jsonRequest = """
                {
                  "amount": 500.00,
                  "paymentMethod": "BANK_TRANSFER",
                  "paymentDate": "2026-08-19",
                  "transactionReference": "BANK-12345",
                  "notes": "Initial deposit"
                }
                """;

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/payments")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-User-Role", "OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.paymentMethod").value("BANK_TRANSFER"))
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/bookings/{bookingId}/financial-summary returns summary")
    public void testFinancialSummaryEndpoint() throws Exception {
        mockMvc.perform(get("/api/bookings/" + booking.getId() + "/financial-summary")
                        .header("X-Tenant-Id", tenantId)
                        .header("X-User-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingTotal").value(2500.00))
                .andExpect(jsonPath("$.depositRequired").value(750.00))
                .andExpect(jsonPath("$.amountPaid").value(0.00))
                .andExpect(jsonPath("$.outstandingBalance").value(2500.00))
                .andExpect(jsonPath("$.paymentStatus").value("DEPOSIT_PENDING"));
    }
}
