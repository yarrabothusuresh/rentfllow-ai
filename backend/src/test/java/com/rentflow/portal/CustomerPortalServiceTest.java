package com.rentflow.portal;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.CrmDataInitializer;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.model.CustomerRequest;
import com.rentflow.portal.model.CustomerUser;
import com.rentflow.portal.model.RequestStatus;
import com.rentflow.portal.model.RequestType;
import com.rentflow.portal.repository.CustomerRequestRepository;
import com.rentflow.portal.repository.CustomerUserRepository;
import com.rentflow.portal.service.CustomerPortalDataInitializer;
import com.rentflow.portal.service.CustomerPortalService;
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
public class CustomerPortalServiceTest {

    @Autowired
    private CustomerPortalService portalService;

    @Autowired
    private CustomerUserRepository customerUserRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRequestRepository requestRepository;

    private String tenantId = "99999999-9999-9999-9999-999999999999";
    private UUID emilyCustomerId = CrmDataInitializer.EMILY_CUSTOMER_ID;
    private UUID customerBId = CustomerPortalDataInitializer.CUSTOMER_B_ID;

    @BeforeEach
    public void setUp() {
        if (!customerRepository.existsById(emilyCustomerId)) {
            Customer emily = new Customer();
            emily.setId(emilyCustomerId);
            emily.setTenantId(tenantId);
            emily.setCustomerNumber("CUS-TEST-EMILY-" + UUID.randomUUID().toString().substring(0, 6));
            emily.setFirstName("Emily");
            emily.setLastName("Brown");
            emily.setCompanyName("Brown Wedding");
            emily.setEmail("emily.brown@example-demo.com");
            emily.setPhone("+1 555-010-1001");
            emily.setStatus(CustomerStatus.ACTIVE);
            customerRepository.save(emily);
        }

        if (!customerRepository.existsById(customerBId)) {
            Customer custB = new Customer();
            custB.setId(customerBId);
            custB.setTenantId(tenantId);
            custB.setCustomerNumber("CUS-TEST-CUSTB-" + UUID.randomUUID().toString().substring(0, 6));
            custB.setFirstName("Robert");
            custB.setLastName("Vance");
            custB.setCompanyName("XYZ Events Inc.");
            custB.setEmail("customer.b@xyzevents.demo");
            custB.setStatus(CustomerStatus.ACTIVE);
            customerRepository.save(custB);
        }

        if (customerUserRepository.findByEmailIgnoreCase("customer@abcevents.demo").isEmpty()) {
            CustomerUser cu = new CustomerUser();
            cu.setTenantId(tenantId);
            cu.setCustomerId(emilyCustomerId);
            cu.setUserId(UUID.randomUUID());
            cu.setEmail("customer@abcevents.demo");
            cu.setPasswordHash("demo");
            cu.setActive(true);
            customerUserRepository.save(cu);
        }
    }

    @Test
    @DisplayName("1. Customer login succeeds with valid credentials")
    public void testCustomerLoginSuccess() {
        CustomerAuthResponseDTO res = portalService.login("customer@abcevents.demo", "demo");
        assertNotNull(res);
        assertEquals("CUSTOMER", res.getRole());
        assertEquals(emilyCustomerId, res.getCustomerId());
        assertEquals(tenantId, res.getTenantId());
    }

    @Test
    @DisplayName("2. Customer login fails with invalid password")
    public void testCustomerLoginInvalidPassword() {
        assertThrows(IllegalArgumentException.class, () -> portalService.login("customer@abcevents.demo", "wrongpassword"));
    }

    @Test
    @DisplayName("3. Customer can view own dashboard")
    public void testGetDashboard() {
        CustomerPortalDashboardDTO dash = portalService.getDashboard(tenantId, emilyCustomerId);
        assertNotNull(dash);
        assertNotNull(dash.getCustomerName());
    }

    @Test
    @DisplayName("4. Customer can view own quote")
    public void testViewOwnQuote() {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber("QUO-TEST-001");
        q.setCustomerId(emilyCustomerId);
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.SENT);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(14));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        q.setSubtotal(new BigDecimal("1000.00"));
        q.setTotalAmount(new BigDecimal("1082.50"));
        q = quoteRepository.save(q);

        CustomerPortalQuoteDTO dto = portalService.getQuoteDetail(tenantId, emilyCustomerId, q.getId());
        assertNotNull(dto);
        assertEquals("QUO-TEST-001", dto.getQuoteNumber());
    }

    @Test
    @DisplayName("5. Customer CANNOT view another customer's quote (Access Denied)")
    public void testCannotViewOtherCustomerQuote() {
        Quote qB = new Quote();
        qB.setTenantId(tenantId);
        qB.setQuoteNumber("QUO-TEST-CUSTB");
        qB.setCustomerId(customerBId);
        qB.setEventId(UUID.randomUUID());
        qB.setStatus(QuoteStatus.SENT);
        qB.setQuoteDate(LocalDate.now());
        qB.setValidUntil(LocalDate.now().plusDays(14));
        qB.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        qB.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        qB = quoteRepository.save(qB);

        final UUID quoteBId = qB.getId();

        assertThrows(SecurityException.class, () -> {
            portalService.getQuoteDetail(tenantId, emilyCustomerId, quoteBId);
        });
    }

    @Test
    @DisplayName("6. Customer can accept valid quote")
    public void testAcceptValidQuote() {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber("QUO-TEST-ACCEPT");
        q.setCustomerId(emilyCustomerId);
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.SENT);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(14));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        q.setSubtotal(new BigDecimal("1000.00"));
        q.setTotalAmount(new BigDecimal("1082.50"));
        q = quoteRepository.save(q);

        CustomerPortalQuoteDTO accepted = portalService.acceptQuote(tenantId, emilyCustomerId, q.getId(), "CUSTOMER");
        assertEquals("ACCEPTED", accepted.getStatus());
    }

    @Test
    @DisplayName("7. Expired quote CANNOT be accepted")
    public void testCannotAcceptExpiredQuote() {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber("QUO-TEST-EXPIRED");
        q.setCustomerId(emilyCustomerId);
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.SENT);
        q.setQuoteDate(LocalDate.now().minusDays(30));
        q.setValidUntil(LocalDate.now().minusDays(1)); // Expired
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        q = quoteRepository.save(q);

        final UUID expiredQuoteId = q.getId();
        assertThrows(IllegalStateException.class, () -> {
            portalService.acceptQuote(tenantId, emilyCustomerId, expiredQuoteId, "CUSTOMER");
        });
    }

    @Test
    @DisplayName("8. Customer can request quote changes")
    public void testRequestQuoteChanges() {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber("QUO-TEST-CHANGE");
        q.setCustomerId(emilyCustomerId);
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.SENT);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(14));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        q = quoteRepository.save(q);

        CustomerPortalQuoteDTO updated = portalService.requestQuoteChanges(tenantId, emilyCustomerId, q.getId(), "Please add 20 extra chairs.", "CUSTOMER");
        assertEquals("CHANGE_REQUESTED", updated.getStatus());
    }

    @Test
    @DisplayName("9. Customer can view own booking")
    public void testViewOwnBooking() {
        Booking b = new Booking();
        b.setTenantId(tenantId);
        b.setBookingNumber("BOOK-TEST-001");
        b.setQuoteId(UUID.randomUUID());
        b.setCustomerId(emilyCustomerId);
        b.setEventId(UUID.randomUUID());
        b.setStatus(BookingStatus.CONFIRMED);
        b.setBookingDate(LocalDate.now());
        b.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        b.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        b.setSubtotal(new BigDecimal("1500.00"));
        b.setTotalAmount(new BigDecimal("1623.75"));
        b = bookingRepository.save(b);

        CustomerPortalBookingDTO dto = portalService.getBookingDetail(tenantId, emilyCustomerId, b.getId());
        assertNotNull(dto);
        assertEquals("BOOK-TEST-001", dto.getBookingNumber());
    }

    @Test
    @DisplayName("10. Customer CANNOT view another customer's booking")
    public void testCannotViewOtherCustomerBooking() {
        Booking bB = new Booking();
        bB.setTenantId(tenantId);
        bB.setBookingNumber("BOOK-TEST-CUSTB");
        bB.setQuoteId(UUID.randomUUID());
        bB.setCustomerId(customerBId);
        bB.setEventId(UUID.randomUUID());
        bB.setStatus(BookingStatus.CONFIRMED);
        bB.setBookingDate(LocalDate.now());
        bB.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        bB.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        bB = bookingRepository.save(bB);

        final UUID bookingBId = bB.getId();

        assertThrows(SecurityException.class, () -> {
            portalService.getBookingDetail(tenantId, emilyCustomerId, bookingBId);
        });
    }

    @Test
    @DisplayName("11. Customer can view own invoice")
    public void testViewOwnInvoice() {
        Invoice i = new Invoice();
        i.setTenantId(tenantId);
        i.setInvoiceNumber("INV-TEST-001");
        i.setBookingId(UUID.randomUUID());
        i.setCustomerId(emilyCustomerId);
        i.setCustomerName("Emily Brown");
        i.setIssueDate(LocalDate.now());
        i.setDueDate(LocalDate.now().plusDays(14));
        i.setSubtotal(new BigDecimal("1500.00"));
        i.setTotalAmount(new BigDecimal("1623.75"));
        i.setAmountPaid(BigDecimal.ZERO);
        i.setBalanceDue(new BigDecimal("1623.75"));
        i.setStatus(InvoiceStatus.SENT);
        i = invoiceRepository.save(i);

        CustomerPortalInvoiceDTO dto = portalService.getInvoiceDetail(tenantId, emilyCustomerId, i.getId());
        assertNotNull(dto);
        assertEquals("INV-TEST-001", dto.getInvoiceNumber());
    }

    @Test
    @DisplayName("12. Customer CANNOT view another customer's invoice")
    public void testCannotViewOtherCustomerInvoice() {
        Invoice iB = new Invoice();
        iB.setTenantId(tenantId);
        iB.setInvoiceNumber("INV-TEST-CUSTB");
        iB.setBookingId(UUID.randomUUID());
        iB.setCustomerId(customerBId);
        iB.setCustomerName("Robert Vance");
        iB.setIssueDate(LocalDate.now());
        iB.setDueDate(LocalDate.now().plusDays(14));
        iB.setStatus(InvoiceStatus.SENT);
        iB = invoiceRepository.save(iB);

        final UUID invoiceBId = iB.getId();

        assertThrows(SecurityException.class, () -> {
            portalService.getInvoiceDetail(tenantId, emilyCustomerId, invoiceBId);
        });
    }

    @Test
    @DisplayName("13. Customer can create support request message")
    public void testCreateSupportRequest() {
        CreateCustomerRequestDTO dto = new CreateCustomerRequestDTO();
        dto.setType(RequestType.DELIVERY_QUESTION);
        dto.setSubject("Delivery Timing Request");
        dto.setMessage("Can delivery happen before 10 AM on August 30?");

        CustomerRequestDTO created = portalService.createCustomerRequest(tenantId, emilyCustomerId, dto);
        assertNotNull(created);
        assertEquals(RequestType.DELIVERY_QUESTION, created.getRequestType());
        assertEquals("Delivery Timing Request", created.getSubject());
        assertEquals(RequestStatus.OPEN, created.getStatus());
    }

    @Test
    @DisplayName("14. Cross-tenant access is rejected")
    public void testCrossTenantAccessRejected() {
        assertThrows(SecurityException.class, () -> {
            portalService.getDashboard("other-tenant-9999", emilyCustomerId);
        });
    }
}
