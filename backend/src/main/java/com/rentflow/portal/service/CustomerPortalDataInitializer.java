package com.rentflow.portal.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.CrmDataInitializer;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.portal.model.CustomerUser;
import com.rentflow.portal.repository.CustomerUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CustomerPortalDataInitializer implements CommandLineRunner {

    public static final UUID CUSTOMER_B_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID CUSTOMER_B_QUOTE_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    public static final UUID CUSTOMER_B_BOOKING_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    public static final UUID CUSTOMER_B_INVOICE_ID = UUID.fromString("11112222-3333-4444-5555-666677778888");

    private final CustomerUserRepository customerUserRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final EventRepository eventRepository;

    public CustomerPortalDataInitializer(CustomerUserRepository customerUserRepository,
                                       CustomerRepository customerRepository,
                                       QuoteRepository quoteRepository,
                                       BookingRepository bookingRepository,
                                       InvoiceRepository invoiceRepository,
                                       EventRepository eventRepository) {
        this.customerUserRepository = customerUserRepository;
        this.customerRepository = customerRepository;
        this.quoteRepository = quoteRepository;
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (customerUserRepository.count() > 0) {
            return;
        }

        // 1. Seed Customer A Portal Account (ABC Events LLC / Emily Brown)
        CustomerUser cuA1 = new CustomerUser();
        cuA1.setTenantId(tenantId);
        cuA1.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        cuA1.setUserId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        cuA1.setEmail("customer@abcevents.demo");
        cuA1.setPasswordHash("demo");
        cuA1.setActive(true);
        customerUserRepository.save(cuA1);

        CustomerUser cuA2 = new CustomerUser();
        cuA2.setTenantId(tenantId);
        cuA2.setCustomerId(CrmDataInitializer.EMILY_CUSTOMER_ID);
        cuA2.setUserId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        cuA2.setEmail("emily.brown@example-demo.com");
        cuA2.setPasswordHash("demo");
        cuA2.setActive(true);
        customerUserRepository.save(cuA2);

        // 2. Seed Customer B Account & Resources (XYZ Events - for cross-customer security verification)
        Customer custB = new Customer();
        custB.setId(CUSTOMER_B_ID);
        custB.setTenantId(tenantId);
        custB.setCustomerNumber("CUS-000099");
        custB.setFirstName("Robert");
        custB.setLastName("Vance");
        custB.setCompanyName("XYZ Events Inc.");
        custB.setEmail("customer.b@xyzevents.demo");
        custB.setPhone("+1 555-099-9999");
        custB.setBillingAddress("99 Corporate Pkwy");
        custB.setCity("Dallas");
        custB.setState("TX");
        custB.setZipCode("75202");
        custB.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(custB);

        CustomerUser cuB = new CustomerUser();
        cuB.setTenantId(tenantId);
        cuB.setCustomerId(CUSTOMER_B_ID);
        cuB.setUserId(UUID.randomUUID());
        cuB.setEmail("customer.b@xyzevents.demo");
        cuB.setPasswordHash("demo");
        cuB.setActive(true);
        customerUserRepository.save(cuB);

        // Seed Customer B Event
        UUID eventBId = UUID.randomUUID();
        Event eventB = new Event();
        eventB.setId(eventBId);
        eventB.setTenantId(tenantId);
        eventB.setCustomerId(CUSTOMER_B_ID);
        eventB.setEventName("XYZ Corporate Summit");
        eventB.setEventType(EventType.CORPORATE);
        eventB.setEventDate(LocalDate.now().plusDays(45));
        eventB.setVenueName("Vance Tower Ballroom");
        eventB.setStatus(EventStatus.PLANNING);
        eventRepository.save(eventB);

        // Seed Customer B Quote
        Quote quoteB = new Quote();
        quoteB.setId(CUSTOMER_B_QUOTE_ID);
        quoteB.setTenantId(tenantId);
        quoteB.setQuoteNumber("QUO-000099");
        quoteB.setCustomerId(CUSTOMER_B_ID);
        quoteB.setEventId(eventBId);
        quoteB.setStatus(QuoteStatus.SENT);
        quoteB.setQuoteDate(LocalDate.now());
        quoteB.setValidUntil(LocalDate.now().plusDays(30));
        quoteB.setRentalStartDateTime(LocalDateTime.now().plusDays(45));
        quoteB.setRentalEndDateTime(LocalDateTime.now().plusDays(46));
        quoteB.setSubtotal(new BigDecimal("3500.00"));
        quoteB.setTotalAmount(new BigDecimal("3788.75"));
        quoteRepository.save(quoteB);

        // Seed Customer B Booking
        Booking bookingB = new Booking();
        bookingB.setId(CUSTOMER_B_BOOKING_ID);
        bookingB.setTenantId(tenantId);
        bookingB.setBookingNumber("BOOK-000099");
        bookingB.setQuoteId(CUSTOMER_B_QUOTE_ID);
        bookingB.setCustomerId(CUSTOMER_B_ID);
        bookingB.setEventId(eventBId);
        bookingB.setStatus(BookingStatus.CONFIRMED);
        bookingB.setBookingDate(LocalDate.now());
        bookingB.setRentalStartDateTime(LocalDateTime.now().plusDays(45));
        bookingB.setRentalEndDateTime(LocalDateTime.now().plusDays(46));
        bookingB.setSubtotal(new BigDecimal("3500.00"));
        bookingB.setTotalAmount(new BigDecimal("3788.75"));
        bookingB.setDepositPaid(new BigDecimal("1000.00"));
        bookingB.setBalanceDue(new BigDecimal("2788.75"));
        bookingRepository.save(bookingB);

        // Seed Customer B Invoice
        Invoice invoiceB = new Invoice();
        invoiceB.setId(CUSTOMER_B_INVOICE_ID);
        invoiceB.setTenantId(tenantId);
        invoiceB.setInvoiceNumber("INV-000099");
        invoiceB.setBookingId(CUSTOMER_B_BOOKING_ID);
        invoiceB.setCustomerId(CUSTOMER_B_ID);
        invoiceB.setCustomerName("Robert Vance");
        invoiceB.setCompanyName("XYZ Events Inc.");
        invoiceB.setEmail("customer.b@xyzevents.demo");
        invoiceB.setIssueDate(LocalDate.now());
        invoiceB.setDueDate(LocalDate.now().plusDays(14));
        invoiceB.setSubtotal(new BigDecimal("3500.00"));
        invoiceB.setTax(new BigDecimal("288.75"));
        invoiceB.setTotalAmount(new BigDecimal("3788.75"));
        invoiceB.setAmountPaid(new BigDecimal("1000.00"));
        invoiceB.setBalanceDue(new BigDecimal("2788.75"));
        invoiceB.setStatus(InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoiceB);
    }
}
