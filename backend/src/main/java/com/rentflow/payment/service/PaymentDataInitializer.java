package com.rentflow.payment.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.repository.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(10)
public class PaymentDataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;

    public PaymentDataInitializer(PaymentRepository paymentRepository,
                                  PaymentService paymentService,
                                  BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (paymentRepository.count() > 0) {
            return;
        }

        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        // 1. Find or Seed Demo Booking 1 (Emily's Wedding / ABC Events LLC)
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        Booking booking1 = null;
        Booking booking2 = null;

        if (!bookings.isEmpty()) {
            booking1 = bookings.get(0);
            if (bookings.size() > 1) {
                booking2 = bookings.get(1);
            }
        }

        if (booking1 == null) {
            booking1 = new Booking();
            booking1.setTenantId(tenantId);
            booking1.setBookingNumber("BKG-000001");
            booking1.setQuoteId(UUID.randomUUID());
            booking1.setCustomerId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
            booking1.setEventId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
            booking1.setStatus(BookingStatus.CONFIRMED);
            booking1.setBookingDate(LocalDate.of(2026, 8, 19));
            booking1.setRentalStartDateTime(LocalDateTime.of(2026, 9, 20, 8, 0));
            booking1.setRentalEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
            booking1.setSubtotal(new BigDecimal("2100.00"));
            booking1.setTaxAmount(new BigDecimal("400.00"));
            booking1.setTotalAmount(new BigDecimal("2500.00"));
            booking1.setDepositRequired(new BigDecimal("750.00"));
            booking1.setDepositPaid(BigDecimal.ZERO);
            booking1.setBalanceDue(new BigDecimal("2500.00"));
            booking1.setCreatedBy("OWNER");
            booking1 = bookingRepository.save(booking1);
        } else {
            // Ensure values for demo alignment ($2,500 total, $750 deposit required)
            booking1.setTotalAmount(new BigDecimal("2500.00"));
            booking1.setDepositRequired(new BigDecimal("750.00"));
            booking1.setDepositPaid(BigDecimal.ZERO);
            booking1.setBalanceDue(new BigDecimal("2500.00"));
            booking1 = bookingRepository.save(booking1);
        }

        // Record $500 Partial Payment for Booking 1
        RecordPaymentDTO p1 = new RecordPaymentDTO(
                new BigDecimal("500.00"),
                PaymentMethod.BANK_TRANSFER,
                LocalDate.of(2026, 8, 19),
                "BANK-12345",
                "Initial deposit payment"
        );
        paymentService.recordPayment(tenantId, booking1.getId(), p1, "OWNER");
        System.out.println("✅ Seeded Partial Payment of $500.00 for Booking " + booking1.getBookingNumber() + " (Status: PARTIALLY_PAID)");

        // 2. Find or Seed Demo Booking 2 ($1,500 total, full payment $1,500 -> PAID)
        if (booking2 == null) {
            booking2 = new Booking();
            booking2.setTenantId(tenantId);
            booking2.setBookingNumber("BKG-000002");
            booking2.setQuoteId(UUID.randomUUID());
            booking2.setCustomerId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
            booking2.setEventId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
            booking2.setStatus(BookingStatus.CONFIRMED);
            booking2.setBookingDate(LocalDate.of(2026, 8, 18));
            booking2.setRentalStartDateTime(LocalDateTime.of(2026, 9, 25, 8, 0));
            booking2.setRentalEndDateTime(LocalDateTime.of(2026, 9, 26, 18, 0));
            booking2.setSubtotal(new BigDecimal("1350.00"));
            booking2.setTaxAmount(new BigDecimal("150.00"));
            booking2.setTotalAmount(new BigDecimal("1500.00"));
            booking2.setDepositRequired(new BigDecimal("450.00"));
            booking2.setDepositPaid(BigDecimal.ZERO);
            booking2.setBalanceDue(new BigDecimal("1500.00"));
            booking2.setCreatedBy("OWNER");
            booking2 = bookingRepository.save(booking2);
        } else {
            booking2.setTotalAmount(new BigDecimal("1500.00"));
            booking2.setDepositRequired(new BigDecimal("450.00"));
            booking2.setDepositPaid(BigDecimal.ZERO);
            booking2.setBalanceDue(new BigDecimal("1500.00"));
            booking2 = bookingRepository.save(booking2);
        }

        // Record $1,500 Full Payment for Booking 2
        RecordPaymentDTO p2 = new RecordPaymentDTO(
                new BigDecimal("1500.00"),
                PaymentMethod.CREDIT_CARD,
                LocalDate.of(2026, 8, 18),
                "CC-98765",
                "Full booking payment"
        );
        paymentService.recordPayment(tenantId, booking2.getId(), p2, "OWNER");
        System.out.println("✅ Seeded Full Payment of $1,500.00 for Booking " + booking2.getBookingNumber() + " (Status: PAID)");
    }
}
