package com.rentflow.payment;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentAuditRepository;
import com.rentflow.payment.repository.PaymentRepository;
import com.rentflow.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAuditRepository paymentAuditRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String tenantId;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        tenantId = "test-tenant-" + UUID.randomUUID();

        booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-TEST-" + System.currentTimeMillis());
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
    @DisplayName("1 & 2. Record partial payment successfully and update status to PARTIALLY_PAID")
    public void testRecordPartialPayment() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("500.00"),
                PaymentMethod.BANK_TRANSFER,
                LocalDate.now(),
                "REF-001",
                "Partial deposit"
        );

        PaymentDTO result = paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER");

        assertNotNull(result.getId());
        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        assertEquals(new BigDecimal("500.00"), result.getAmount());

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("500.00"), updated.getDepositPaid());
        assertEquals(new BigDecimal("2000.00"), updated.getBalanceDue());
        assertEquals(BookingStatus.PARTIALLY_PAID, updated.getStatus());
    }

    @Test
    @DisplayName("3 & 4. Record full payment and update status to PAID")
    public void testRecordFullPayment() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("2500.00"),
                PaymentMethod.CREDIT_CARD,
                LocalDate.now(),
                "REF-FULL",
                "Full payment"
        );

        PaymentDTO result = paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER");

        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("2500.00"), updated.getDepositPaid());
        assertEquals(BigDecimal.ZERO.setScale(2), updated.getBalanceDue().setScale(2));
        assertEquals(BookingStatus.PAID, updated.getStatus());
    }

    @Test
    @DisplayName("6. Zero payment rejected")
    public void testZeroPaymentRejected() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                BigDecimal.ZERO,
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-ZERO",
                "Zero"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("7. Negative payment rejected")
    public void testNegativePaymentRejected() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("-100.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-NEG",
                "Negative"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("must be greater than zero"));
    }

    @Test
    @DisplayName("8. Payment exceeding balance rejected")
    public void testExceedingPaymentRejected() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("2600.00"),
                PaymentMethod.CREDIT_CARD,
                LocalDate.now(),
                "REF-EXCEED",
                "Exceed"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("Payment exceeds outstanding balance of $2500.00"));
    }

    @Test
    @DisplayName("9. Unauthorized user cannot record payment")
    public void testUnauthorizedRecordPayment() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("500.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-UNAUTH",
                "Unauth"
        );

        assertThrows(SecurityException.class, () ->
                paymentService.recordPayment(tenantId, booking.getId(), dto, "DRIVER")
        );
    }

    @Test
    @DisplayName("10. User cannot access another tenant's payment")
    public void testTenantIsolation() {
        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("500.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-TENANT",
                "Tenant test"
        );

        paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER");

        String otherTenant = "other-tenant-" + UUID.randomUUID();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getBookingPayments(otherTenant, booking.getId())
        );
        assertTrue(ex.getMessage().contains("Booking not found"));
    }

    @Test
    @DisplayName("11 & 12. Void payment recalculates balance and status")
    public void testVoidPaymentRecalculation() {
        RecordPaymentDTO dto1 = new RecordPaymentDTO(new BigDecimal("1000.00"), PaymentMethod.BANK_TRANSFER, LocalDate.now(), "P1", "Pay 1");
        RecordPaymentDTO dto2 = new RecordPaymentDTO(new BigDecimal("1500.00"), PaymentMethod.CHECK, LocalDate.now(), "P2", "Pay 2");

        paymentService.recordPayment(tenantId, booking.getId(), dto1, "OWNER");
        PaymentDTO p2 = paymentService.recordPayment(tenantId, booking.getId(), dto2, "OWNER");

        Booking bookingAfterPays = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.PAID, bookingAfterPays.getStatus());
        assertEquals(new BigDecimal("2500.00"), bookingAfterPays.getDepositPaid());

        // Void 2nd payment of $1,500
        paymentService.voidPayment(tenantId, p2.getId(), "ADMIN", "Customer check bounced");

        Booking bookingAfterVoid = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), bookingAfterVoid.getDepositPaid());
        assertEquals(new BigDecimal("1500.00"), bookingAfterVoid.getBalanceDue());
        assertEquals(BookingStatus.PARTIALLY_PAID, bookingAfterVoid.getStatus());
    }

    @Test
    @DisplayName("13. Already void payment cannot be voided again")
    public void testCannotVoidAlreadyVoidPayment() {
        RecordPaymentDTO dto = new RecordPaymentDTO(new BigDecimal("500.00"), PaymentMethod.CASH, LocalDate.now(), "P1", "Pay");
        PaymentDTO p = paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER");

        paymentService.voidPayment(tenantId, p.getId(), "FINANCE", "Entered in error");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                paymentService.voidPayment(tenantId, p.getId(), "FINANCE", "Second void attempt")
        );
        assertTrue(ex.getMessage().contains("already been voided"));
    }

    @Test
    @DisplayName("Financial summary calculations")
    public void testGetFinancialSummary() {
        RecordPaymentDTO dto = new RecordPaymentDTO(new BigDecimal("750.00"), PaymentMethod.DEBIT_CARD, LocalDate.now(), "FS", "Summary test");
        paymentService.recordPayment(tenantId, booking.getId(), dto, "OWNER");

        BookingFinancialSummaryDTO summary = paymentService.getFinancialSummary(tenantId, booking.getId());
        assertEquals(new BigDecimal("2500.00"), summary.getBookingTotal());
        assertEquals(new BigDecimal("750.00"), summary.getDepositRequired());
        assertEquals(new BigDecimal("750.00"), summary.getAmountPaid());
        assertEquals(new BigDecimal("1750.00"), summary.getOutstandingBalance());
        assertEquals("PARTIALLY_PAID", summary.getPaymentStatus());
    }
}
