package com.rentflow.automation;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.detector.FinanceSignalDetector;
import com.rentflow.automation.detector.QuoteSignalDetector;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.BusinessSignalRepository;
import com.rentflow.automation.service.BusinessSignalService;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day29SignalDetectionTest {

    @Autowired
    private QuoteSignalDetector quoteSignalDetector;

    @Autowired
    private FinanceSignalDetector financeSignalDetector;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BusinessSignalService businessSignalService;

    @Autowired
    private BusinessSignalRepository businessSignalRepository;

    private final String tenantId = "test-tenant-day29-signals";

    @BeforeEach
    void setup() {
        businessSignalRepository.deleteAll();
    }

    @Test
    void testDetectExpiringQuoteSignal() {
        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setQuoteNumber("QUO-TEST-EXP");
        quote.setCustomerId(UUID.randomUUID());
        quote.setEventId(UUID.randomUUID());
        quote.setStatus(QuoteStatus.SENT);
        quote.setQuoteDate(LocalDate.now());
        quote.setValidUntil(LocalDate.now().plusDays(1)); // Expiring tomorrow
        quote.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        quote.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quoteRepository.save(quote);

        List<DetectedSignal> signals = quoteSignalDetector.detect(tenantId);
        assertFalse(signals.isEmpty(), "Should detect expiring quote signal");

        DetectedSignal match = signals.stream()
            .filter(s -> s.signalType() == BusinessSignalType.QUOTE_EXPIRING_SOON)
            .findFirst()
            .orElse(null);

        assertNotNull(match);
        assertEquals("QUOTE", match.sourceEntityType());
        assertEquals("QUO-TEST-EXP", match.sourceEntityNumber());
        assertEquals(AutomationActionType.SEND_QUOTE_REMINDER, match.suggestedActionType());
        assertTrue(match.evidence().containsKey("daysUntilExpiry"));
    }

    @Test
    void testDetectOverdueInvoiceAndUnpaidDeposit() {
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber("INV-TEST-OVD");
        invoice.setBookingId(UUID.randomUUID());
        invoice.setCustomerId(UUID.randomUUID());
        invoice.setIssueDate(LocalDate.now().minusDays(15));
        invoice.setDueDate(LocalDate.now().minusDays(5));
        invoice.setTotalAmount(new BigDecimal("2000.00"));
        invoice.setBalanceDue(new BigDecimal("2000.00"));
        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(invoice);

        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-TEST-DEP");
        booking.setCustomerId(UUID.randomUUID());
        booking.setQuoteId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(3));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(5));
        booking.setTotalAmount(new BigDecimal("1000.00"));
        booking.setDepositRequired(new BigDecimal("300.00"));
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setContractSigned(false);
        bookingRepository.save(booking);

        List<DetectedSignal> signals = financeSignalDetector.detect(tenantId);
        assertFalse(signals.isEmpty());

        boolean hasOverdue = signals.stream().anyMatch(s -> s.signalType() == BusinessSignalType.INVOICE_OVERDUE);
        boolean hasUnpaidDeposit = signals.stream().anyMatch(s -> s.signalType() == BusinessSignalType.DEPOSIT_UNPAID);
        boolean hasUnsignedContract = signals.stream().anyMatch(s -> s.signalType() == BusinessSignalType.CONTRACT_UNSIGNED);

        assertTrue(hasOverdue, "Should detect overdue invoice");
        assertTrue(hasUnpaidDeposit, "Should detect unpaid deposit");
        assertTrue(hasUnsignedContract, "Should detect unsigned contract");
    }

    @Test
    void testSignalDeduplicationAndUpdate() {
        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setQuoteNumber("QUO-TEST-DEDUPE");
        quote.setCustomerId(UUID.randomUUID());
        quote.setEventId(UUID.randomUUID());
        quote.setStatus(QuoteStatus.SENT);
        quote.setQuoteDate(LocalDate.now());
        quote.setValidUntil(LocalDate.now().plusDays(1));
        quote.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        quote.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        quote.setTotalAmount(new BigDecimal("2500.00"));
        quoteRepository.save(quote);

        List<DetectedSignal> signals = quoteSignalDetector.detect(tenantId);
        DetectedSignal sig = signals.get(0);

        // First record
        BusinessSignal recorded1 = businessSignalService.recordOrUpdateSignal(tenantId, sig);
        assertNotNull(recorded1.getId());
        LocalDateTime firstDetectedAt = recorded1.getDetectedAt();

        // Second record with same dedupeKey
        BusinessSignal recorded2 = businessSignalService.recordOrUpdateSignal(tenantId, sig);
        assertEquals(recorded1.getId(), recorded2.getId(), "Deduplication must reuse existing signal entity ID");
        assertEquals(firstDetectedAt, recorded2.getDetectedAt(), "Original detectedAt must be preserved");

        long count = businessSignalRepository.countByTenantIdAndStatus(tenantId, BusinessSignalStatus.ACTIVE);
        assertEquals(1, count, "Only 1 active signal should exist in database");
    }
}
