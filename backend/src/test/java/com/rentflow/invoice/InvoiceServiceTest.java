package com.rentflow.invoice;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingItem;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.BookingItemRepository;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceAuditRepository;
import com.rentflow.invoice.repository.InvoiceItemRepository;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.invoice.service.InvoiceService;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.PaymentMethod;
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
public class InvoiceServiceTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private InvoiceAuditRepository invoiceAuditRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentService paymentService;

    private String tenantId;
    private Customer customer;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        tenantId = "test-tenant-" + UUID.randomUUID();

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-TEST-" + System.currentTimeMillis());
        customer.setFirstName("Alice");
        customer.setLastName("Wonderland");
        customer.setCompanyName("Wonderland Events");
        customer.setEmail("alice@wonderland.test");
        customer.setPhone("(555) 123-4567");
        customer.setBillingAddress("123 Fantasy Way");
        customer.setCity("Dallas");
        customer.setState("TX");
        customer.setZipCode("75201");
        customer.setCountry("USA");
        customer = customerRepository.save(customer);

        booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BKG-TEST-" + System.currentTimeMillis());
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(customer.getId());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        booking.setSubtotal(new BigDecimal("1000.00"));
        booking.setDiscountAmount(new BigDecimal("100.00"));
        booking.setDeliveryFee(new BigDecimal("50.00"));
        booking.setPickupFee(new BigDecimal("25.00"));
        booking.setTaxAmount(new BigDecimal("80.44"));
        booking.setTotalAmount(new BigDecimal("1055.44"));
        booking.setDepositRequired(new BigDecimal("300.00"));
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setBalanceDue(new BigDecimal("1055.44"));
        booking = bookingRepository.save(booking);

        BookingItem item1 = new BookingItem();
        item1.setBookingId(booking.getId());
        item1.setProductId(UUID.randomUUID());
        item1.setDescription("Chiavari Chair");
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("5.00"));
        item1.setRentalStartDateTime(booking.getRentalStartDateTime());
        item1.setRentalEndDateTime(booking.getRentalEndDateTime());
        item1.setLineSubtotal(new BigDecimal("500.00"));
        bookingItemRepository.save(item1);

        BookingItem item2 = new BookingItem();
        item2.setBookingId(booking.getId());
        item2.setProductId(UUID.randomUUID());
        item2.setDescription("Round Banquet Table");
        item2.setQuantity(10);
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setRentalStartDateTime(booking.getRentalStartDateTime());
        item2.setRentalEndDateTime(booking.getRentalEndDateTime());
        item2.setLineSubtotal(new BigDecimal("500.00"));
        bookingItemRepository.save(item2);
    }

    @Test
    @DisplayName("1 & 2. Create invoice from booking & snapshot items")
    public void testCreateInvoiceFromBooking() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Test Note", LocalDate.now().plusDays(14), "OWNER");

        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(tenantId, dto.getTenantId());
        assertEquals(booking.getId(), dto.getBookingId());
        assertEquals(customer.getId(), dto.getCustomerId());
        assertEquals("Alice Wonderland", dto.getCustomerName());
        assertEquals(2, dto.getItems().size());
        assertEquals("Chiavari Chair", dto.getItems().get(0).getDescription());
    }

    @Test
    @DisplayName("3, 4, 5, 6, 7, 8. Correct calculation of total, discount, fee, tax, paid, balance")
    public void testInvoiceCalculations() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Calculation Test", LocalDate.now().plusDays(14), "OWNER");

        assertEquals(new BigDecimal("1000.00"), dto.getSubtotal());
        assertEquals(new BigDecimal("100.00"), dto.getDiscount());
        assertEquals(new BigDecimal("75.00"), dto.getFees()); // 50 delivery + 25 pickup
        assertEquals(new BigDecimal("80.44"), dto.getTax());
        assertEquals(new BigDecimal("1055.44"), dto.getTotalAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getAmountPaid().setScale(2));
        assertEquals(new BigDecimal("1055.44"), dto.getBalanceDue());
        assertEquals(InvoiceStatus.DRAFT, dto.getStatus());
    }

    @Test
    @DisplayName("9. Prevent duplicate invoice for same booking")
    public void testPreventDuplicateInvoice() {
        invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "First Invoice", LocalDate.now().plusDays(14), "OWNER");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Second Invoice", LocalDate.now().plusDays(14), "OWNER");
        });
        assertTrue(ex.getMessage().contains("An invoice already exists"));
    }

    @Test
    @DisplayName("10. Invoice number generated correctly (INV-000001 format)")
    public void testInvoiceNumberGeneration() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Number Test", LocalDate.now().plusDays(14), "OWNER");

        assertNotNull(dto.getInvoiceNumber());
        assertTrue(dto.getInvoiceNumber().startsWith("INV-"));
    }

    @Test
    @DisplayName("11, 12, 13. Payment updates invoice to PARTIALLY_PAID and PAID")
    public void testPaymentUpdatesInvoiceStatus() {
        InvoiceDTO inv = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Payment Sync Test", LocalDate.now().plusDays(14), "OWNER");
        invoiceService.updateInvoiceStatus(tenantId, inv.getId(), InvoiceStatus.SENT, "OWNER");

        // Partial payment $500
        RecordPaymentDTO payment1 = new RecordPaymentDTO();
        payment1.setAmount(new BigDecimal("500.00"));
        payment1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment1.setPaymentDate(LocalDate.now());
        paymentService.recordPayment(tenantId, booking.getId(), payment1, "OWNER");

        InvoiceDTO updated1 = invoiceService.getInvoice(tenantId, inv.getId(), "OWNER").orElseThrow();
        assertEquals(new BigDecimal("500.00"), updated1.getAmountPaid());
        assertEquals(new BigDecimal("555.44"), updated1.getBalanceDue());
        assertEquals(InvoiceStatus.PARTIALLY_PAID, updated1.getStatus());

        // Second payment to complete balance ($555.44)
        RecordPaymentDTO payment2 = new RecordPaymentDTO();
        payment2.setAmount(new BigDecimal("555.44"));
        payment2.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment2.setPaymentDate(LocalDate.now());
        paymentService.recordPayment(tenantId, booking.getId(), payment2, "OWNER");

        InvoiceDTO updated2 = invoiceService.getInvoice(tenantId, inv.getId(), "OWNER").orElseThrow();
        assertEquals(new BigDecimal("1055.44"), updated2.getAmountPaid());
        assertEquals(BigDecimal.ZERO.setScale(2), updated2.getBalanceDue().setScale(2));
        assertEquals(InvoiceStatus.PAID, updated2.getStatus());
    }

    @Test
    @DisplayName("14 & 15. Due invoice becomes OVERDUE, but PAID invoice does not")
    public void testOverdueLogic() {
        Invoice inv = new Invoice();
        inv.setTenantId(tenantId);
        inv.setBookingId(booking.getId());
        inv.setCustomerId(customer.getId());
        inv.setInvoiceNumber("INV-TEST-OVERDUE");
        inv.setIssueDate(LocalDate.now().minusDays(30));
        inv.setDueDate(LocalDate.now().minusDays(15));
        inv.setSubtotal(new BigDecimal("500.00"));
        inv.setTotalAmount(new BigDecimal("500.00"));
        inv.setAmountPaid(BigDecimal.ZERO);
        inv.setBalanceDue(new BigDecimal("500.00"));
        inv.setStatus(InvoiceStatus.SENT);
        inv = invoiceRepository.save(inv);

        // Fetching list triggers auto-overdue check
        List<InvoiceDTO> list = invoiceService.listInvoices(tenantId, null, null, null, "INV-TEST-OVERDUE", "OWNER");
        assertFalse(list.isEmpty());
        assertEquals(InvoiceStatus.OVERDUE, list.get(0).getStatus());

        // Test PAID invoice does NOT become overdue
        inv.setStatus(InvoiceStatus.PAID);
        inv.setAmountPaid(new BigDecimal("500.00"));
        inv.setBalanceDue(BigDecimal.ZERO);
        invoiceRepository.save(inv);

        List<InvoiceDTO> listPaid = invoiceService.listInvoices(tenantId, null, null, null, "INV-TEST-OVERDUE", "OWNER");
        assertEquals(InvoiceStatus.PAID, listPaid.get(0).getStatus());
    }

    @Test
    @DisplayName("16. Unauthorized user cannot void invoice")
    public void testUnauthorizedUserCannotVoidInvoice() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Void Auth Test", LocalDate.now().plusDays(14), "OWNER");

        assertThrows(SecurityException.class, () -> {
            invoiceService.voidInvoice(tenantId, dto.getId(), "Try voiding", "CUSTOMER");
        });
    }

    @Test
    @DisplayName("17. Voided invoice cannot be voided again")
    public void testDoubleVoidRejected() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Double Void Test", LocalDate.now().plusDays(14), "OWNER");

        invoiceService.voidInvoice(tenantId, dto.getId(), "First Void", "OWNER");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            invoiceService.voidInvoice(tenantId, dto.getId(), "Second Void", "OWNER");
        });
        assertTrue(ex.getMessage().contains("already been voided"));
    }

    @Test
    @DisplayName("18. Cross-tenant invoice access rejected")
    public void testCrossTenantAccess() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Tenant Test", LocalDate.now().plusDays(14), "OWNER");

        String otherTenant = "tenant-other-12345";
        assertTrue(invoiceService.getInvoice(otherTenant, dto.getId(), "OWNER").isEmpty());
    }

    @Test
    @DisplayName("19. Issued invoice snapshot remains unchanged after booking modifications")
    public void testSnapshotPriceProtection() {
        InvoiceDTO dto = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Snapshot Test", LocalDate.now().plusDays(14), "OWNER");

        // Mutate original booking subtotal & price
        booking.setSubtotal(new BigDecimal("9999.99"));
        booking.setTotalAmount(new BigDecimal("9999.99"));
        bookingRepository.save(booking);

        // Fetch invoice again and verify it maintains original snapshot totals ($1055.44)
        InvoiceDTO fetched = invoiceService.getInvoice(tenantId, dto.getId(), "OWNER").orElseThrow();
        assertEquals(new BigDecimal("1055.44"), fetched.getTotalAmount());
        assertEquals(new BigDecimal("1000.00"), fetched.getSubtotal());
    }

    @Test
    @DisplayName("20. Void invoice with attached payments is rejected with clear message")
    public void testVoidInvoiceWithPaymentsPrevented() {
        InvoiceDTO inv = invoiceService.createInvoiceFromBooking(tenantId, booking.getId(), "Void Payment Test", LocalDate.now().plusDays(14), "OWNER");

        RecordPaymentDTO p = new RecordPaymentDTO();
        p.setAmount(new BigDecimal("200.00"));
        p.setPaymentMethod(PaymentMethod.CASH);
        paymentService.recordPayment(tenantId, booking.getId(), p, "OWNER");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            invoiceService.voidInvoice(tenantId, inv.getId(), "Voiding with payment", "OWNER");
        });
        assertTrue(ex.getMessage().contains("Invoice cannot be voided because payments totaling"));
    }
}
