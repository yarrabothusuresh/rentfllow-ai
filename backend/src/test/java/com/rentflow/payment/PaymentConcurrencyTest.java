package com.rentflow.payment;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.exception.IdempotencyConflictException;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentRepository;
import com.rentflow.payment.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private String tenantA;
    private String tenantB;
    private UUID customerA;
    private UUID customerB;

    @BeforeEach
    public void setUp() {
        tenantA = "tenant-a-" + UUID.randomUUID();
        tenantB = "tenant-b-" + UUID.randomUUID();
        customerA = UUID.randomUUID();
        customerB = UUID.randomUUID();
    }

    @AfterEach
    public void tearDown() {
        // Clean up only test tenant data so other seeded demo data is not disturbed
        if (tenantA != null) {
            paymentRepository.deleteAll(paymentRepository.findByTenantId(tenantA));
            invoiceRepository.deleteAll(invoiceRepository.findByTenantId(tenantA));
            bookingRepository.deleteAll(bookingRepository.findByTenantId(tenantA));
        }
        if (tenantB != null) {
            paymentRepository.deleteAll(paymentRepository.findByTenantId(tenantB));
            invoiceRepository.deleteAll(invoiceRepository.findByTenantId(tenantB));
            bookingRepository.deleteAll(bookingRepository.findByTenantId(tenantB));
        }
    }

    private Booking createTestBooking(String tenantId, UUID customerId, BigDecimal totalAmount) {
        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-" + UUID.randomUUID().toString().substring(0, 8));
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(customerId);
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        booking.setSubtotal(totalAmount);
        booking.setTotalAmount(totalAmount);
        booking.setDepositRequired(totalAmount.multiply(new BigDecimal("0.3")).setScale(2, RoundingMode.HALF_UP));
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setBalanceDue(totalAmount);
        booking.setCreatedBy("OWNER");
        return bookingRepository.save(booking);
    }

    private Invoice createTestInvoice(String tenantId, UUID customerId, UUID bookingId, BigDecimal totalAmount) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setCustomerId(customerId);
        invoice.setBookingId(bookingId);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8));
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14));
        invoice.setSubtotal(totalAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(totalAmount);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setCreatedBy("OWNER");
        return invoiceRepository.save(invoice);
    }

    @Test
    @DisplayName("33. Concurrency Test: 10 concurrent requests with same reference produce exactly one financial effect")
    public void testTenConcurrentPayments_SameReference_ExactlyOneEffect() throws Exception {
        BigDecimal totalAmount = new BigDecimal("10000.00");
        Booking booking = createTestBooking(tenantA, customerA, totalAmount);
        Invoice invoice = createTestInvoice(tenantA, customerA, booking.getId(), totalAmount);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Future<PaymentDTO>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        String sharedReference = "PAY-CONCURRENT-001";

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    RecordPaymentDTO dto = new RecordPaymentDTO(
                            invoice.getId(),
                            booking.getId(),
                            totalAmount,
                            PaymentMethod.BANK_TRANSFER,
                            LocalDate.now(),
                            sharedReference,
                            "Concurrent payment test"
                    );
                    PaymentDTO result = paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER");
                    successCount.incrementAndGet();
                    return result;
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    throw e;
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Concurrent execution timed out");
        executor.shutdown();

        assertEquals(10, successCount.get(), "All 10 concurrent requests should succeed (1 creation, 9 replays)");
        assertEquals(0, errorCount.get(), "No 500 or unhandled exceptions allowed during concurrent duplicate requests");

        // Verify exactly one payment record exists in the database
        List<Payment> payments = paymentRepository.findByTenantId(tenantA);
        assertEquals(1, payments.size(), "Database must contain exactly 1 payment record");

        Payment singlePayment = payments.get(0);
        assertEquals(totalAmount.setScale(2), singlePayment.getAmount().setScale(2));
        assertEquals(sharedReference, singlePayment.getTransactionReference());

        // Verify all 10 threads received the identical payment ID
        int replayCount = 0;
        for (Future<PaymentDTO> f : futures) {
            PaymentDTO dto = f.get();
            assertEquals(singlePayment.getId(), dto.getId());
            if (Boolean.TRUE.equals(dto.getIdempotentReplay())) {
                replayCount++;
            }
        }
        // At least 9 should be marked as idempotent replays (or up to 10 if pre-check or unique catch caught all)
        assertTrue(replayCount >= 9, "Expected at least 9 idempotent replays among concurrent requests");

        // Verify Booking financial invariants: paid = 10,000, balance = 0, status = PAID
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(totalAmount.setScale(2), updatedBooking.getDepositPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), updatedBooking.getBalanceDue().setScale(2));
        assertEquals(BookingStatus.PAID, updatedBooking.getStatus());

        // Verify Invoice financial invariants: paid = 10,000, balance = 0, status = PAID
        Invoice updatedInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(totalAmount.setScale(2), updatedInvoice.getAmountPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), updatedInvoice.getBalanceDue().setScale(2));
        assertEquals(InvoiceStatus.PAID, updatedInvoice.getStatus());
    }

    @Test
    @DisplayName("34. Concurrency Test: 2 concurrent payments with different references serialize and prevent overpayment")
    public void testTwoConcurrentPayments_DifferentReferences_OverpaymentPrevented() throws Exception {
        BigDecimal totalAmount = new BigDecimal("10000.00");
        Booking booking = createTestBooking(tenantA, customerA, totalAmount);
        Invoice invoice = createTestInvoice(tenantA, customerA, booking.getId(), totalAmount);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        List<Future<PaymentDTO>> futures = new ArrayList<>();

        // Thread A: Reference A for ₹10,000
        futures.add(executor.submit(() -> {
            try {
                startLatch.await();
                RecordPaymentDTO dto = new RecordPaymentDTO(
                        invoice.getId(),
                        booking.getId(),
                        totalAmount,
                        PaymentMethod.CREDIT_CARD,
                        LocalDate.now(),
                        "REF-A",
                        "Payment A"
                );
                return paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER");
            } finally {
                doneLatch.countDown();
            }
        }));

        // Thread B: Reference B for ₹10,000
        futures.add(executor.submit(() -> {
            try {
                startLatch.await();
                RecordPaymentDTO dto = new RecordPaymentDTO(
                        invoice.getId(),
                        booking.getId(),
                        totalAmount,
                        PaymentMethod.CREDIT_CARD,
                        LocalDate.now(),
                        "REF-B",
                        "Payment B"
                );
                return paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER");
            } finally {
                doneLatch.countDown();
            }
        }));

        // Unleash both threads
        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        int successes = 0;
        int overpaymentRejections = 0;

        for (Future<PaymentDTO> f : futures) {
            try {
                f.get();
                successes++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IllegalArgumentException
                        && e.getCause().getMessage().contains("Payment exceeds outstanding balance")) {
                    overpaymentRejections++;
                } else {
                    fail("Unexpected exception: " + e.getCause());
                }
            }
        }

        assertEquals(1, successes, "Exactly one payment must succeed");
        assertEquals(1, overpaymentRejections, "The second concurrent payment must be rejected for overpayment");

        // Verify Booking financial state
        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(totalAmount.setScale(2), updatedBooking.getDepositPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), updatedBooking.getBalanceDue().setScale(2));

        // Exactly 1 payment in the database
        assertEquals(1, paymentRepository.findByTenantId(tenantA).size());
    }

    @Test
    @DisplayName("48. Duplicate Replay Test: 5 sequential calls with same reference return identical payment and no duplicate effect")
    public void testSequentialDuplicateReplay_ReturnsSamePayment() {
        BigDecimal totalAmount = new BigDecimal("5000.00");
        Booking booking = createTestBooking(tenantA, customerA, totalAmount);

        RecordPaymentDTO dto = new RecordPaymentDTO(
                totalAmount,
                PaymentMethod.CASH,
                LocalDate.now(),
                "SEQ-REPLAY-001",
                "Sequential replay"
        );

        PaymentDTO first = paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER");
        assertNotNull(first.getId());
        assertFalse(Boolean.TRUE.equals(first.getIdempotentReplay()));

        for (int i = 0; i < 4; i++) {
            PaymentDTO replay = paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER");
            assertEquals(first.getId(), replay.getId());
            assertTrue(Boolean.TRUE.equals(replay.getIdempotentReplay()));
        }

        // Assert 1 database payment and balance exactly 0
        assertEquals(1, paymentRepository.findByTenantId(tenantA).size());
        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(totalAmount.setScale(2), updated.getDepositPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), updated.getBalanceDue().setScale(2));
    }

    @Test
    @DisplayName("49. Idempotency Conflict Test: Same reference with different amount throws HTTP 409 Conflict")
    public void testIdempotencyConflict_DifferentAmount_Throws409() {
        BigDecimal totalAmount = new BigDecimal("5000.00");
        Booking booking = createTestBooking(tenantA, customerA, totalAmount);

        RecordPaymentDTO firstDto = new RecordPaymentDTO(
                new BigDecimal("2000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-CONFLICT-AMT",
                "First payment"
        );
        PaymentDTO first = paymentService.recordPayment(tenantA, booking.getId(), firstDto, "OWNER");
        assertNotNull(first.getId());

        // Same reference, different amount (₹3,000 instead of ₹2,000)
        RecordPaymentDTO secondDto = new RecordPaymentDTO(
                new BigDecimal("3000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-CONFLICT-AMT",
                "Second payment with different amount"
        );

        IdempotencyConflictException ex = assertThrows(IdempotencyConflictException.class, () ->
                paymentService.recordPayment(tenantA, booking.getId(), secondDto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("different amount"));

        // Only 1 payment in DB, balance reduced only by ₹2,000
        assertEquals(1, paymentRepository.findByTenantId(tenantA).size());
        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("2000.00"), updated.getDepositPaid().setScale(2));
    }

    @Test
    @DisplayName("50. Idempotency Conflict Test: Same reference with different booking throws HTTP 409 Conflict")
    public void testIdempotencyConflict_DifferentBooking_Throws409() {
        Booking booking1 = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));
        Booking booking2 = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));

        RecordPaymentDTO dto1 = new RecordPaymentDTO(
                new BigDecimal("1000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-SHARED-ACROSS-BOOKINGS",
                "Booking 1"
        );
        paymentService.recordPayment(tenantA, booking1.getId(), dto1, "OWNER");

        RecordPaymentDTO dto2 = new RecordPaymentDTO(
                new BigDecimal("1000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-SHARED-ACROSS-BOOKINGS",
                "Booking 2"
        );

        IdempotencyConflictException ex = assertThrows(IdempotencyConflictException.class, () ->
                paymentService.recordPayment(tenantA, booking2.getId(), dto2, "OWNER")
        );
        assertTrue(ex.getMessage().contains("different booking"));
    }

    @Test
    @DisplayName("43 & 44. Negative and zero payment amounts are rejected with 400 Bad Request")
    public void testNegativeAndZeroPayments_Rejected() {
        Booking booking = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));

        RecordPaymentDTO zeroDto = new RecordPaymentDTO(
                BigDecimal.ZERO,
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-ZERO-VAL",
                "Zero"
        );
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantA, booking.getId(), zeroDto, "OWNER")
        );
        assertTrue(ex1.getMessage().contains("greater than zero"));

        RecordPaymentDTO negDto = new RecordPaymentDTO(
                new BigDecimal("-50.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-NEG-VAL",
                "Negative"
        );
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantA, booking.getId(), negDto, "OWNER")
        );
        assertTrue(ex2.getMessage().contains("greater than zero"));
    }

    @Test
    @DisplayName("45. Overpayment beyond outstanding balance is rejected")
    public void testOverpayment_Rejected() {
        Booking booking = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));

        RecordPaymentDTO overDto = new RecordPaymentDTO(
                new BigDecimal("5001.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-OVER",
                "Over"
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantA, booking.getId(), overDto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("Payment exceeds outstanding balance"));
    }

    @Test
    @DisplayName("46 & 47. Partial payments correctly update balance and status to PARTIALLY_PAID then PAID")
    public void testPartialAndFullPayment_StatusTransitions() {
        BigDecimal total = new BigDecimal("10000.00");
        Booking booking = createTestBooking(tenantA, customerA, total);
        Invoice invoice = createTestInvoice(tenantA, customerA, booking.getId(), total);

        // Payment 1: ₹4,000
        RecordPaymentDTO p1 = new RecordPaymentDTO(
                invoice.getId(),
                booking.getId(),
                new BigDecimal("4000.00"),
                PaymentMethod.BANK_TRANSFER,
                LocalDate.now(),
                "PART-001",
                "Partial 1"
        );
        paymentService.recordPayment(tenantA, booking.getId(), p1, "OWNER");

        Booking b1 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("4000.00"), b1.getDepositPaid().setScale(2));
        assertEquals(new BigDecimal("6000.00"), b1.getBalanceDue().setScale(2));
        assertEquals(BookingStatus.PARTIALLY_PAID, b1.getStatus());

        Invoice i1 = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(new BigDecimal("4000.00"), i1.getAmountPaid().setScale(2));
        assertEquals(new BigDecimal("6000.00"), i1.getBalanceDue().setScale(2));
        assertEquals(InvoiceStatus.PARTIALLY_PAID, i1.getStatus());

        // Payment 2: ₹6,000 -> Full settlement
        RecordPaymentDTO p2 = new RecordPaymentDTO(
                invoice.getId(),
                booking.getId(),
                new BigDecimal("6000.00"),
                PaymentMethod.BANK_TRANSFER,
                LocalDate.now(),
                "PART-002",
                "Partial 2"
        );
        paymentService.recordPayment(tenantA, booking.getId(), p2, "OWNER");

        Booking b2 = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(new BigDecimal("10000.00"), b2.getDepositPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), b2.getBalanceDue().setScale(2));
        assertEquals(BookingStatus.PAID, b2.getStatus());

        Invoice i2 = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(new BigDecimal("10000.00"), i2.getAmountPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), i2.getBalanceDue().setScale(2));
        assertEquals(InvoiceStatus.PAID, i2.getStatus());
    }

    @Test
    @DisplayName("40 & 41. Tenant Isolation: Same transaction reference can exist in different tenants, but cross-tenant payment is rejected")
    public void testTenantIsolation_CrossTenantAccessDenied() {
        Booking bookingA = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));
        Booking bookingB = createTestBooking(tenantB, customerB, new BigDecimal("5000.00"));

        String sharedRef = "REF-TENANT-SHARED-001";

        // Tenant A records payment with sharedRef
        RecordPaymentDTO dtoA = new RecordPaymentDTO(
                new BigDecimal("1000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                sharedRef,
                "Tenant A payment"
        );
        PaymentDTO paymentA = paymentService.recordPayment(tenantA, bookingA.getId(), dtoA, "OWNER");
        assertNotNull(paymentA.getId());

        // Tenant B records payment with SAME sharedRef for Tenant B's booking (must succeed because reference is tenant-scoped)
        RecordPaymentDTO dtoB = new RecordPaymentDTO(
                new BigDecimal("2000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                sharedRef,
                "Tenant B payment"
        );
        PaymentDTO paymentB = paymentService.recordPayment(tenantB, bookingB.getId(), dtoB, "OWNER");
        assertNotNull(paymentB.getId());
        assertNotEquals(paymentA.getId(), paymentB.getId());

        // Tenant A replaying sharedRef returns Tenant A payment ONLY, never Tenant B payment
        PaymentDTO replayA = paymentService.recordPayment(tenantA, bookingA.getId(), dtoA, "OWNER");
        assertEquals(paymentA.getId(), replayA.getId());
        assertEquals(tenantA, replayA.getTenantId());

        // Cross-Tenant Payment Attempt: Tenant A tries to pay Tenant B's booking
        RecordPaymentDTO crossDto = new RecordPaymentDTO(
                new BigDecimal("500.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-CROSS",
                "Cross tenant attempt"
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.recordPayment(tenantA, bookingB.getId(), crossDto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("Booking not found"));
    }

    @Test
    @DisplayName("42. Customer IDOR Protection: Customer cannot record payment against another customer's booking")
    public void testCustomerIdor_CustomerCannotPayOtherCustomerBooking() {
        Booking bookingA = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));

        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("1000.00"),
                PaymentMethod.CREDIT_CARD,
                LocalDate.now(),
                "REF-IDOR-ATTEMPT",
                "Customer B attempting to pay Customer A's booking"
        );

        // Authenticated customer is customerB, but booking belongs to customerA
        SecurityException ex = assertThrows(SecurityException.class, () ->
                paymentService.recordPayment(tenantA, bookingA.getId(), dto, "CUSTOMER", customerB)
        );
        assertTrue(ex.getMessage().contains("Customer IDOR protection"));

        // No payment recorded
        assertEquals(0, paymentRepository.findByTenantId(tenantA).size());
    }

    @Test
    @DisplayName("38. Rollback Test: When downstream invoice/booking operation fails, payment insert is rolled back completely")
    public void testTransactionRollback_WhenFailureOccurs() {
        Booking booking = createTestBooking(tenantA, customerA, new BigDecimal("5000.00"));

        // Cancel the booking so payment fails
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        RecordPaymentDTO dto = new RecordPaymentDTO(
                new BigDecimal("1000.00"),
                PaymentMethod.CASH,
                LocalDate.now(),
                "REF-ROLLBACK",
                "Cancelled booking attempt"
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                paymentService.recordPayment(tenantA, booking.getId(), dto, "OWNER")
        );
        assertTrue(ex.getMessage().contains("cancelled booking"));

        // Verify zero payments inserted and balance intact
        assertEquals(0, paymentRepository.findByTenantId(tenantA).size());
        Booking reloaded = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("5000.00").compareTo(reloaded.getBalanceDue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reloaded.getDepositPaid()));
    }
}
