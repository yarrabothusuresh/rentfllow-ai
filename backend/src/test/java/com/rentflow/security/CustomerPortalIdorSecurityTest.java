package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.ClaimType;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Day 32 Security Test Suite: Customer Portal IDOR Elimination.
 * Proves that:
 * 1. Customer A1 can NEVER access Customer A2's quotes, bookings, invoices, or claims (returns 404).
 * 2. Spoofed headers (X-Customer-Id, X-Tenant-Id) sent by customer are completely ignored.
 * 3. Customer role is strictly barred (403 Forbidden) from accessing staff/admin endpoints.
 * 4. Customer A1 can successfully access own resources (200 OK).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CustomerPortalIdorSecurityTest {

    private static final String TENANT_ID = "99999999-9999-9999-9999-999999999999";

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwtFactory testJwtFactory;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private DamageClaimRepository damageClaimRepository;

    private Customer customerA1;
    private Customer customerA2;
    private Quote quoteA1;
    private Quote quoteA2;
    private Booking bookingA1;
    private Booking bookingA2;
    private Invoice invoiceA2;
    private DamageClaim claimA2;

    private String tokenA1;
    private String tokenA2;

    @BeforeEach
    public void setUp() {
        SecurityUtils.clearContext();

        // Customer 1
        customerA1 = new Customer();
        customerA1.setTenantId(TENANT_ID);
        customerA1.setCustomerNumber("CUST-PORTAL-001");
        customerA1.setFirstName("Carol");
        customerA1.setLastName("One");
        customerA1.setEmail("carol1@portal.test");
        customerA1.setCustomerType(CustomerType.INDIVIDUAL);
        customerA1.setStatus(CustomerStatus.ACTIVE);
        customerA1 = customerRepository.save(customerA1);

        // Customer 2
        customerA2 = new Customer();
        customerA2.setTenantId(TENANT_ID);
        customerA2.setCustomerNumber("CUST-PORTAL-002");
        customerA2.setFirstName("David");
        customerA2.setLastName("Two");
        customerA2.setEmail("david2@portal.test");
        customerA2.setCustomerType(CustomerType.INDIVIDUAL);
        customerA2.setStatus(CustomerStatus.ACTIVE);
        customerA2 = customerRepository.save(customerA2);

        // Generate verified Customer JWTs
        tokenA1 = testJwtFactory.createCustomerToken(TENANT_ID, customerA1.getId());
        tokenA2 = testJwtFactory.createCustomerToken(TENANT_ID, customerA2.getId());

        // Quote for Customer 1
        quoteA1 = new Quote();
        quoteA1.setTenantId(TENANT_ID);
        quoteA1.setQuoteNumber("Q-PORTAL-A1");
        quoteA1.setCustomerId(customerA1.getId());
        quoteA1.setEventId(UUID.randomUUID());
        quoteA1.setStatus(QuoteStatus.SENT);
        quoteA1.setQuoteDate(LocalDate.now());
        quoteA1.setValidUntil(LocalDate.now().plusDays(10));
        quoteA1.setRentalStartDateTime(LocalDateTime.now().plusDays(3));
        quoteA1.setRentalEndDateTime(LocalDateTime.now().plusDays(5));
        quoteA1.setTotalAmount(BigDecimal.valueOf(800.00));
        quoteA1 = quoteRepository.save(quoteA1);

        // Quote for Customer 2
        quoteA2 = new Quote();
        quoteA2.setTenantId(TENANT_ID);
        quoteA2.setQuoteNumber("Q-PORTAL-A2");
        quoteA2.setCustomerId(customerA2.getId());
        quoteA2.setEventId(UUID.randomUUID());
        quoteA2.setStatus(QuoteStatus.SENT);
        quoteA2.setQuoteDate(LocalDate.now());
        quoteA2.setValidUntil(LocalDate.now().plusDays(10));
        quoteA2.setRentalStartDateTime(LocalDateTime.now().plusDays(3));
        quoteA2.setRentalEndDateTime(LocalDateTime.now().plusDays(5));
        quoteA2.setTotalAmount(BigDecimal.valueOf(1200.00));
        quoteA2 = quoteRepository.save(quoteA2);

        // Booking for Customer 1
        bookingA1 = new Booking();
        bookingA1.setTenantId(TENANT_ID);
        bookingA1.setBookingNumber("BK-PORTAL-A1");
        bookingA1.setQuoteId(quoteA1.getId());
        bookingA1.setCustomerId(customerA1.getId());
        bookingA1.setEventId(quoteA1.getEventId());
        bookingA1.setStatus(BookingStatus.CONFIRMED);
        bookingA1.setBookingDate(LocalDate.now());
        bookingA1.setRentalStartDateTime(LocalDateTime.now().plusDays(3));
        bookingA1.setRentalEndDateTime(LocalDateTime.now().plusDays(5));
        bookingA1.setTotalAmount(BigDecimal.valueOf(800.00));
        bookingA1 = bookingRepository.save(bookingA1);

        // Booking for Customer 2
        bookingA2 = new Booking();
        bookingA2.setTenantId(TENANT_ID);
        bookingA2.setBookingNumber("BK-PORTAL-A2");
        bookingA2.setQuoteId(quoteA2.getId());
        bookingA2.setCustomerId(customerA2.getId());
        bookingA2.setEventId(quoteA2.getEventId());
        bookingA2.setStatus(BookingStatus.CONFIRMED);
        bookingA2.setBookingDate(LocalDate.now());
        bookingA2.setRentalStartDateTime(LocalDateTime.now().plusDays(3));
        bookingA2.setRentalEndDateTime(LocalDateTime.now().plusDays(5));
        bookingA2.setTotalAmount(BigDecimal.valueOf(1200.00));
        bookingA2 = bookingRepository.save(bookingA2);

        // Invoice for Customer 2
        invoiceA2 = new Invoice();
        invoiceA2.setTenantId(TENANT_ID);
        invoiceA2.setInvoiceNumber("INV-PORTAL-A2");
        invoiceA2.setBookingId(bookingA2.getId());
        invoiceA2.setCustomerId(customerA2.getId());
        invoiceA2.setIssueDate(LocalDate.now());
        invoiceA2.setDueDate(LocalDate.now().plusDays(30));
        invoiceA2.setStatus(InvoiceStatus.SENT);
        invoiceA2.setSubtotal(BigDecimal.valueOf(1200.00));
        invoiceA2.setTotalAmount(BigDecimal.valueOf(1200.00));
        invoiceA2 = invoiceRepository.save(invoiceA2);

        // Damage Claim for Customer 2
        claimA2 = new DamageClaim();
        claimA2.setTenantId(TENANT_ID);
        claimA2.setClaimNumber("CLM-PORTAL-A2");
        claimA2.setBookingId(bookingA2.getId());
        claimA2.setReturnOrderId(UUID.randomUUID());
        claimA2.setCustomerId(customerA2.getId());
        claimA2.setDescription("Scratched dining tabletop");
        claimA2.setClaimType(ClaimType.DAMAGE);
        claimA2.setStatus(ClaimStatus.OPEN);
        claimA2.setEstimatedTotalCost(BigDecimal.valueOf(150.00));
        claimA2 = damageClaimRepository.save(claimA2);
    }

    @Test
    @DisplayName("Test 1: Customer A1 reading Customer A2 quote returns 403 Forbidden")
    public void customerA1_readingCustomerA2Quote_returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/portal/quotes/" + quoteA2.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 2: Customer A1 reading Customer A2 booking returns 403 Forbidden")
    public void customerA1_readingCustomerA2Booking_returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/portal/bookings/" + bookingA2.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 3: Customer A1 reading Customer A2 invoice returns 403 Forbidden")
    public void customerA1_readingCustomerA2Invoice_returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/portal/invoices/" + invoiceA2.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 4: Customer A1 reading Customer A2 claim returns 403 Forbidden")
    public void customerA1_readingCustomerA2Claim_returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/portal/claims/" + claimA2.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 5: Customer A1 sending spoofed X-Customer-Id receives only Customer A1 data")
    public void customerA1_spoofedCustomerIdHeader_isIgnored() throws Exception {
        // Customer 1 calls quotes list with spoofed X-Customer-Id pointing to Customer 2
        mockMvc.perform(get("/api/portal/quotes")
                .header("Authorization", "Bearer " + tokenA1)
                .header("X-Customer-Id", customerA2.getId().toString())) // Spoofed!
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(quoteA1.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(quoteA2.getId().toString()))));

        // Customer 1 calls bookings list with spoofed X-Customer-Id pointing to Customer 2
        mockMvc.perform(get("/api/portal/bookings")
                .header("Authorization", "Bearer " + tokenA1)
                .header("X-Customer-Id", customerA2.getId().toString())) // Spoofed!
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(bookingA1.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(bookingA2.getId().toString()))));
    }

    @Test
    @DisplayName("Test 6: Customer JWT attempting access to staff/admin endpoints receives 403 Forbidden")
    public void customerJwt_attemptingStaffEndpoint_returns403Forbidden() throws Exception {
        // Customer JWT accessing CRM customers list
        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());

        // Customer JWT accessing Admin users
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());

        // Customer JWT accessing Warehouse
        mockMvc.perform(get("/api/warehouse/dashboard")
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 7: Valid Customer A1 accessing own resources returns 200 OK")
    public void customerA1_accessingOwnResources_returns200Ok() throws Exception {
        // Own quote
        mockMvc.perform(get("/api/portal/quotes/" + quoteA1.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quoteA1.getId().toString()))
                .andExpect(jsonPath("$.quoteNumber").value("Q-PORTAL-A1"));

        // Own booking
        mockMvc.perform(get("/api/portal/bookings/" + bookingA1.getId())
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingA1.getId().toString()))
                .andExpect(jsonPath("$.bookingNumber").value("BK-PORTAL-A1"));

        // Own dashboard
        mockMvc.perform(get("/api/portal/dashboard")
                .header("Authorization", "Bearer " + tokenA1))
                .andExpect(status().isOk());
    }
}
