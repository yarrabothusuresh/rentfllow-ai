package com.rentflow.financial;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PaymentAndRefundIntegrityTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String tenantId;
    private Booking booking;

    @BeforeEach
    void setUp() {
        tenantId = "test-fin-" + UUID.randomUUID();

        booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-FIN-" + UUID.randomUUID().toString().substring(0, 8));
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        booking.setSubtotal(new BigDecimal("1000.00"));
        booking.setTotalAmount(new BigDecimal("1000.00"));
        booking.setDepositRequired(new BigDecimal("300.00"));
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setBalanceDue(new BigDecimal("1000.00"));

        booking = bookingRepository.save(booking);
    }

    @Test
    @DisplayName("Partial payments accumulation and final status synchronization")
    void testPartialPaymentsAccumulation() {
        // 1. First partial payment: $300.00
        RecordPaymentDTO p1 = new RecordPaymentDTO();
        p1.setAmount(new BigDecimal("300.00"));
        p1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p1.setTransactionReference("REF-P1-" + UUID.randomUUID());

        PaymentDTO res1 = paymentService.recordPayment(tenantId, booking.getId(), p1, "ADMIN");
        assertEquals(PaymentStatus.COMPLETED, res1.getPaymentStatus());

        Booking b1 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("300.00"), b1.getDepositPaid());
        assertEquals(new BigDecimal("700.00"), b1.getBalanceDue());
        assertEquals(BookingStatus.PARTIALLY_PAID, b1.getStatus());

        // 2. Second partial payment: $400.00
        RecordPaymentDTO p2 = new RecordPaymentDTO();
        p2.setAmount(new BigDecimal("400.00"));
        p2.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        p2.setTransactionReference("REF-P2-" + UUID.randomUUID());

        PaymentDTO res2 = paymentService.recordPayment(tenantId, booking.getId(), p2, "ADMIN");
        assertEquals(PaymentStatus.COMPLETED, res2.getPaymentStatus());

        Booking b2 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("700.00"), b2.getDepositPaid());
        assertEquals(new BigDecimal("300.00"), b2.getBalanceDue());
        assertEquals(BookingStatus.PARTIALLY_PAID, b2.getStatus());

        // 3. Final payment: $300.00 -> fully paid
        RecordPaymentDTO p3 = new RecordPaymentDTO();
        p3.setAmount(new BigDecimal("300.00"));
        p3.setPaymentMethod(PaymentMethod.CASH);
        p3.setTransactionReference("REF-P3-" + UUID.randomUUID());

        PaymentDTO res3 = paymentService.recordPayment(tenantId, booking.getId(), p3, "ADMIN");
        assertEquals(PaymentStatus.COMPLETED, res3.getPaymentStatus());

        Booking b3 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), b3.getDepositPaid());
        assertEquals(new BigDecimal("0.00"), b3.getBalanceDue());
        assertEquals(BookingStatus.PAID, b3.getStatus());
    }

    @Test
    @DisplayName("Overpayment rejection: payments exceeding balance must fail")
    void testOverpaymentRejection() {
        RecordPaymentDTO p = new RecordPaymentDTO();
        p.setAmount(new BigDecimal("1000.01"));
        p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p.setTransactionReference("REF-OVER-" + UUID.randomUUID());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantId, booking.getId(), p, "ADMIN")
        );
        assertTrue(ex.getMessage().contains("Payment exceeds outstanding balance"));
    }

    @Test
    @DisplayName("Full refund: transitions status to REFUNDED and restores booking balanceDue")
    void testFullRefund() {
        RecordPaymentDTO p = new RecordPaymentDTO();
        p.setAmount(new BigDecimal("400.00"));
        p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p.setTransactionReference("REF-FULL-" + UUID.randomUUID());

        PaymentDTO created = paymentService.recordPayment(tenantId, booking.getId(), p, "ADMIN");

        Booking afterPay = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("400.00"), afterPay.getDepositPaid());
        assertEquals(new BigDecimal("600.00"), afterPay.getBalanceDue());

        // Issue Full Refund
        PaymentDTO refunded = paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("400.00"), "ADMIN", "Customer event cancelled");

        assertEquals(PaymentStatus.REFUNDED, refunded.getPaymentStatus());

        Booking afterRefund = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("0.00"), afterRefund.getDepositPaid());
        assertEquals(new BigDecimal("1000.00"), afterRefund.getBalanceDue());
        assertEquals(BookingStatus.DEPOSIT_PENDING, afterRefund.getStatus());
    }

    @Test
    @DisplayName("Partial refund: decrements payment amount and increments balanceDue")
    void testPartialRefund() {
        RecordPaymentDTO p = new RecordPaymentDTO();
        p.setAmount(new BigDecimal("500.00"));
        p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p.setTransactionReference("REF-PART-" + UUID.randomUUID());

        PaymentDTO created = paymentService.recordPayment(tenantId, booking.getId(), p, "ADMIN");

        // Partial refund of $200.00
        PaymentDTO refunded = paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("200.00"), "ADMIN", "Partial price adjustment");

        // Payment record retains remaining $300.00 completed amount
        assertEquals(new BigDecimal("300.00"), refunded.getAmount());
        assertEquals(PaymentStatus.COMPLETED, refunded.getPaymentStatus());

        Booking afterRefund = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("300.00"), afterRefund.getDepositPaid());
        assertEquals(new BigDecimal("700.00"), afterRefund.getBalanceDue());
        assertEquals(BookingStatus.PARTIALLY_PAID, afterRefund.getStatus());
    }

    @Test
    @DisplayName("Refund validation: invalid amounts, voided payments, and unauthorized roles")
    void testRefundValidations() {
        RecordPaymentDTO p = new RecordPaymentDTO();
        p.setAmount(new BigDecimal("250.00"));
        p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        p.setTransactionReference("REF-VAL-" + UUID.randomUUID());

        PaymentDTO created = paymentService.recordPayment(tenantId, booking.getId(), p, "ADMIN");

        // 1. Refund exceeding payment amount
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("300.00"), "ADMIN", "Too much")
        );
        assertTrue(ex1.getMessage().contains("cannot exceed original payment amount"));

        // 2. Negative or zero refund
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.refundPayment(tenantId, created.getId(), BigDecimal.ZERO, "ADMIN", "Zero")
        );
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("-50.00"), "ADMIN", "Negative")
        );

        // 3. Unauthorized role (CUSTOMER or WAREHOUSE)
        assertThrows(SecurityException.class, () ->
                paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("100.00"), "CUSTOMER", "Not allowed")
        );

        // 4. Voiding then attempting refund -> throws IllegalStateException
        paymentService.voidPayment(tenantId, created.getId(), "ADMIN", "Voiding test");
        assertThrows(IllegalStateException.class, () ->
                paymentService.refundPayment(tenantId, created.getId(), new BigDecimal("100.00"), "ADMIN", "Already void")
        );
    }
}
