package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.repository.QuoteRepository;
import com.rentflow.event.Event;
import com.rentflow.event.EventRepository;
import com.rentflow.event.dto.CreateEventRequest;
import com.rentflow.event.dto.EventDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.portal.model.CustomerUser;
import com.rentflow.portal.repository.CustomerUserRepository;
import com.rentflow.workflow.model.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Adversarial Penetration Test Suite for Multi-Tenant Security & Object-Level Authorization.
 * 
 * Verifies core security invariant:
 * TENANT A MUST NEVER READ, MODIFY, DELETE, LINK TO, OR ACT UPON TENANT B'S DATA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MultiTenantIsolationSecurityTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("crmEventRepository")
    private com.rentflow.ai.repository.EventRepository crmEventRepository;
    @Autowired private CustomerUserRepository customerUserRepository;

    private Product productA;
    private Customer customerA;
    private Quote quoteA;
    private Booking bookingA;
    private Invoice invoiceA;
    private Event eventA;

    @BeforeEach
    public void setUp() {
        SecurityUtils.clearContext();

        // 1. Seed Tenant A Product
        productA = new Product();
        productA.setId(UUID.randomUUID());
        productA.setTenantId(TENANT_A);
        productA.setName("Tenant A Confidential Laser Projector");
        productA.setSku("SKU-TA-PROJ-" + UUID.randomUUID().toString().substring(0, 5));
        productA.setProductType(ProductType.RENTAL_ITEM);
        productA.setStatus(ProductStatus.ACTIVE);
        productA.setQuantityOwned(8);
        productA.setRentalPrice(BigDecimal.valueOf(1850));
        productA = productRepository.save(productA);

        // 2. Seed Tenant A Customer
        customerA = new Customer();
        customerA.setId(UUID.randomUUID());
        customerA.setTenantId(TENANT_A);
        customerA.setCustomerNumber("CUST-TA-" + UUID.randomUUID().toString().substring(0, 5));
        customerA.setFirstName("Alice");
        customerA.setLastName("TenantA");
        customerA.setEmail("alice.tenanta." + UUID.randomUUID().toString().substring(0, 5) + "@example.com");
        customerA.setStatus(CustomerStatus.ACTIVE);
        customerA = customerRepository.save(customerA);

        // 3. Seed Tenant A Event
        eventA = new Event();
        eventA.setId(UUID.randomUUID());
        eventA.setTenantId(UUID.fromString(TENANT_A));
        eventA.setCustomerId(customerA.getId());
        eventA.setEventName("Tenant A Secret Board Summit");
        eventA.setEventType("Corporate Summit");
        eventA.setEventDate(LocalDate.now().plusDays(20));
        eventA.setStartTime(LocalTime.of(9, 0));
        eventA.setEndTime(LocalTime.of(18, 0));
        eventA.setGuestCount(120);
        eventA.setVenueName("Skyline Executive Suites");
        eventA.setVenueAddress("100 Executive Blvd");
        eventA.setStatus(EventStatus.BOOKED);
        eventA = eventRepository.save(eventA);

        // 3. Seed Tenant A CRM Event for Quote
        com.rentflow.ai.model.Event crmEventA = new com.rentflow.ai.model.Event();
        crmEventA.setTenantId(TENANT_A);
        crmEventA.setCustomerId(customerA.getId());
        crmEventA.setEventName("Tenant A CRM Event");
        crmEventA.setEventDate(LocalDate.now().plusDays(20));
        crmEventA = crmEventRepository.save(crmEventA);

        // 4. Seed Tenant A Quote
        quoteA = new Quote();
        quoteA.setId(UUID.randomUUID());
        quoteA.setTenantId(TENANT_A);
        quoteA.setQuoteNumber("Q-TA-" + UUID.randomUUID().toString().substring(0, 5));
        quoteA.setCustomerId(customerA.getId());
        quoteA.setEventId(crmEventA.getId());
        quoteA.setStatus(QuoteStatus.SENT);
        quoteA.setQuoteDate(LocalDate.now());
        quoteA.setValidUntil(LocalDate.now().plusDays(14));
        quoteA.setRentalStartDateTime(LocalDateTime.now().plusDays(19));
        quoteA.setRentalEndDateTime(LocalDateTime.now().plusDays(21));
        quoteA.setSubtotal(BigDecimal.valueOf(3700));
        quoteA.setTotalAmount(BigDecimal.valueOf(4000));
        quoteA = quoteRepository.save(quoteA);

        // 5. Seed Tenant A Booking
        bookingA = new Booking();
        bookingA.setId(UUID.randomUUID());
        bookingA.setTenantId(TENANT_A);
        bookingA.setBookingNumber("BKG-TA-" + UUID.randomUUID().toString().substring(0, 5));
        bookingA.setQuoteId(quoteA.getId());
        bookingA.setCustomerId(customerA.getId());
        bookingA.setEventId(eventA.getId());
        bookingA.setStatus(BookingStatus.CONFIRMED);
        bookingA.setBookingDate(LocalDate.now());
        bookingA.setRentalStartDateTime(LocalDateTime.now().plusDays(19));
        bookingA.setRentalEndDateTime(LocalDateTime.now().plusDays(21));
        bookingA.setSubtotal(BigDecimal.valueOf(3700));
        bookingA.setTotalAmount(BigDecimal.valueOf(4000));
        bookingA = bookingRepository.save(bookingA);

        // 6. Seed Tenant A Invoice
        invoiceA = new Invoice();
        invoiceA.setTenantId(TENANT_A);
        invoiceA.setBookingId(bookingA.getId());
        invoiceA.setCustomerId(customerA.getId());
        invoiceA.setInvoiceNumber("INV-TA-" + UUID.randomUUID().toString().substring(0, 5));
        invoiceA.setIssueDate(LocalDate.now());
        invoiceA.setDueDate(LocalDate.now().plusDays(15));
        invoiceA.setStatus(InvoiceStatus.SENT);
        invoiceA.setTotalAmount(BigDecimal.valueOf(4000));
        invoiceA.setBalanceDue(BigDecimal.valueOf(4000));
        invoiceA = invoiceRepository.save(invoiceA);
    }

    // =========================================================================
    // 1. DIRECT OBJECT REFERENCE (BOLA / IDOR) READ TESTS
    // =========================================================================

    @Test
    @DisplayName("PEN-01: Tenant B cannot read Tenant A's Product by ID (404 Not Found)")
    public void testDirectObjectReferenceProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/" + productA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PEN-02: Tenant B cannot read Tenant A's Customer by ID (404 Not Found)")
    public void testDirectObjectReferenceCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/" + customerA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PEN-03: Tenant B cannot read Tenant A's Quote by ID (404 Not Found)")
    public void testDirectObjectReferenceQuoteNotFound() throws Exception {
        mockMvc.perform(get("/api/quotes/" + quoteA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PEN-04: Tenant B cannot read Tenant A's Booking by ID (404 Not Found)")
    public void testDirectObjectReferenceBookingNotFound() throws Exception {
        mockMvc.perform(get("/api/bookings/" + bookingA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PEN-05: Tenant B cannot read Tenant A's Invoice by ID (404 Not Found)")
    public void testDirectObjectReferenceInvoiceNotFound() throws Exception {
        mockMvc.perform(get("/api/invoices/" + invoiceA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PEN-06: Tenant B cannot read Tenant A's Event by ID (404 Not Found)")
    public void testDirectObjectReferenceEventNotFound() throws Exception {
        mockMvc.perform(get("/api/events/" + eventA.getId())
                .header("X-Tenant-Id", TENANT_B))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // 2. CROSS-TENANT MUTATION & DELETION ATTACKS
    // =========================================================================

    @Test
    @DisplayName("PEN-07: Tenant B cannot modify Tenant A's Event (404 Not Found)")
    public void testCrossTenantEventUpdateRejected() throws Exception {
        EventDTO hostileUpdate = new EventDTO();
        hostileUpdate.setEventName("HIJACKED BY TENANT B");
        hostileUpdate.setGuestCount(999);

        mockMvc.perform(put("/api/events/" + eventA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hostileUpdate)))
                .andExpect(status().isNotFound());

        // Verify database entity is completely untampered
        Event fresh = eventRepository.findById(eventA.getId()).orElseThrow();
        assertEquals("Tenant A Secret Board Summit", fresh.getEventName());
        assertEquals(120, fresh.getGuestCount());
    }

    @Test
    @DisplayName("PEN-08: Tenant B cannot delete Tenant A's Event (404 Not Found)")
    public void testCrossTenantEventDeleteRejected() throws Exception {
        mockMvc.perform(delete("/api/events/" + eventA.getId())
                .header("X-Tenant-Id", TENANT_B))
                .andExpect(status().isNotFound());

        // Verify event still exists
        assertTrue(eventRepository.existsById(eventA.getId()), "Event belonging to Tenant A must NOT be deleted by Tenant B");
    }

    @Test
    @DisplayName("PEN-09: Tenant B cannot delete Tenant A's Quote (404 Not Found)")
    public void testCrossTenantQuoteDeleteRejected() throws Exception {
        mockMvc.perform(delete("/api/quotes/" + quoteA.getId())
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());

        assertTrue(quoteRepository.existsById(quoteA.getId()), "Quote belonging to Tenant A must NOT be deleted by Tenant B");
    }

    // =========================================================================
    // 3. HEADER VS BODY / QUERY PARAMETER SPOOFING ATTACKS
    // =========================================================================

    @Test
    @DisplayName("PEN-10: Event creation forces caller's tenantId; body tenantId injection is ignored")
    public void testEventCreationTenantHeaderEnforcement() throws Exception {
        CreateEventRequest req = new CreateEventRequest();
        req.setTenantId(UUID.fromString(TENANT_A)); // Malicious injection attempting to write to Tenant A
        req.setEventName("Tenant B Malicious Event");
        req.setEventType("Conference");
        req.setEventDate(LocalDate.now().plusDays(30));
        req.setGuestCount(50);

        mockMvc.perform(post("/api/events")
                .header("X-Tenant-Id", TENANT_B)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(TENANT_B));

        // Verify database: the event is stored under TENANT_B, never TENANT_A
        List<Event> tenantBEvents = eventRepository.findByTenantId(UUID.fromString(TENANT_B));
        assertTrue(tenantBEvents.stream().anyMatch(e -> "Tenant B Malicious Event".equals(e.getEventName())),
                "Event must be created under authenticated Tenant B");

        List<Event> tenantAEvents = eventRepository.findByTenantId(UUID.fromString(TENANT_A));
        assertFalse(tenantAEvents.stream().anyMatch(e -> "Tenant B Malicious Event".equals(e.getEventName())),
                "Tenant A must NEVER have received the spoofed event");
    }

    @Test
    @DisplayName("PEN-11: GET /api/events query param ?tenantId=TENANT_A is ignored when X-Tenant-Id is TENANT_B")
    public void testEventListQueryParamTenantOverrideIgnored() throws Exception {
        // Tenant B requests events with explicit query param trying to read Tenant A
        mockMvc.perform(get("/api/events")
                .param("tenantId", TENANT_A)
                .header("X-Tenant-Id", TENANT_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventName == 'Tenant A Secret Board Summit')]").doesNotExist());
    }

    // =========================================================================
    // 4. CROSS-TENANT REFERENCE INJECTION ATTACKS
    // =========================================================================

    @Test
    @DisplayName("PEN-12: Tenant B Quote cannot reference Tenant A's Customer (400 Bad Request)")
    public void testQuoteReferenceInjectionCustomerRejected() throws Exception {
        // Create valid Tenant B CRM Event
        com.rentflow.ai.model.Event crmEventB = new com.rentflow.ai.model.Event();
        crmEventB.setTenantId(TENANT_B);
        crmEventB.setCustomerId(UUID.randomUUID());
        crmEventB.setEventName("Tenant B CRM Event");
        crmEventB.setEventDate(LocalDate.now().plusDays(10));
        crmEventB = crmEventRepository.save(crmEventB);

        QuoteDTO hostileQuote = new QuoteDTO();
        hostileQuote.setCustomerId(customerA.getId()); // Injected Tenant A customer!
        hostileQuote.setEventId(crmEventB.getId());
        hostileQuote.setStatus(QuoteStatus.DRAFT);
        hostileQuote.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        hostileQuote.setRentalEndDateTime(LocalDateTime.now().plusDays(12));

        mockMvc.perform(post("/api/quotes")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hostileQuote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("does not belong to tenant")));
    }

    @Test
    @DisplayName("PEN-13: Tenant B Quote cannot reference Tenant A's Product (400 Bad Request)")
    public void testQuoteReferenceInjectionProductRejected() throws Exception {
        // Create valid Tenant B Customer & Event
        Customer customerB = new Customer();
        customerB.setId(UUID.randomUUID());
        customerB.setTenantId(TENANT_B);
        customerB.setCustomerNumber("CUST-TB-" + UUID.randomUUID().toString().substring(0, 5));
        customerB.setFirstName("Bob");
        customerB.setLastName("TenantB");
        customerB.setEmail("bob.tenantb@example.com");
        customerB.setStatus(CustomerStatus.ACTIVE);
        customerB = customerRepository.save(customerB);

        // Create valid Tenant B CRM Event
        com.rentflow.ai.model.Event crmEventB = new com.rentflow.ai.model.Event();
        crmEventB.setTenantId(TENANT_B);
        crmEventB.setCustomerId(customerB.getId());
        crmEventB.setEventName("Tenant B Product Test CRM Event");
        crmEventB.setEventDate(LocalDate.now().plusDays(10));
        crmEventB = crmEventRepository.save(crmEventB);

        QuoteDTO hostileQuote = new QuoteDTO();
        hostileQuote.setCustomerId(customerB.getId());
        hostileQuote.setEventId(crmEventB.getId());
        hostileQuote.setStatus(QuoteStatus.DRAFT);
        hostileQuote.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        hostileQuote.setRentalEndDateTime(LocalDateTime.now().plusDays(12));

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(productA.getId()); // Injected Tenant A product!
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(1850));
        hostileQuote.setItems(List.of(item));

        mockMvc.perform(post("/api/quotes")
                .header("X-Tenant-Id", TENANT_B)
                .header("X-User-Role", "SALES")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hostileQuote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("does not belong to tenant")));
    }

    // =========================================================================
    // 5. CUSTOMER PORTAL SAME-TENANT IDOR ATTACK
    // =========================================================================

    @Test
    @DisplayName("PEN-14: Customer 1 in Tenant A cannot access Customer 2's Quote via portal (403 Forbidden)")
    public void testCustomerPortalSameTenantIdorForbidden() throws Exception {
        // Customer 2 in Tenant A
        Customer customerA2 = new Customer();
        customerA2.setId(UUID.randomUUID());
        customerA2.setTenantId(TENANT_A);
        customerA2.setCustomerNumber("CUST-TA2-" + UUID.randomUUID().toString().substring(0, 5));
        customerA2.setFirstName("Bob");
        customerA2.setLastName("TenantA2");
        customerA2.setEmail("bob.tenanta2@example.com");
        customerA2.setStatus(CustomerStatus.ACTIVE);
        customerA2 = customerRepository.save(customerA2);

        Quote quoteA2 = new Quote();
        quoteA2.setId(UUID.randomUUID());
        quoteA2.setTenantId(TENANT_A);
        quoteA2.setQuoteNumber("Q-TA2-" + UUID.randomUUID().toString().substring(0, 5));
        quoteA2.setCustomerId(customerA2.getId());
        quoteA2.setEventId(eventA.getId());
        quoteA2.setStatus(QuoteStatus.SENT);
        quoteA2.setQuoteDate(LocalDate.now());
        quoteA2.setValidUntil(LocalDate.now().plusDays(14));
        quoteA2.setRentalStartDateTime(LocalDateTime.now().plusDays(10));
        quoteA2.setRentalEndDateTime(LocalDateTime.now().plusDays(12));
        quoteA2 = quoteRepository.save(quoteA2);

        // CustomerUser login profile for Customer 1
        CustomerUser user1 = new CustomerUser();
        user1.setTenantId(TENANT_A);
        user1.setCustomerId(customerA.getId());
        user1.setUserId(UUID.randomUUID());
        user1.setEmail(customerA.getEmail());
        user1.setPasswordHash("pass");
        user1.setActive(true);
        customerUserRepository.save(user1);

        // Customer 1 tries to view Customer 2's quote
        mockMvc.perform(get("/api/portal/quotes/" + quoteA2.getId())
                .header("X-Tenant-Id", TENANT_A)
                .header("X-Customer-Id", customerA.getId().toString()))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 6. REPOSITORY & LIST QUERY ISOLATION VERIFICATION
    // =========================================================================

    @Test
    @DisplayName("PEN-15: Repository queries scoped to Tenant B return zero results for Tenant A entities")
    public void testRepositoryZeroLeakage() {
        // Products
        assertTrue(productRepository.findByTenantIdAndId(TENANT_B, productA.getId()).isEmpty());
        assertFalse(productRepository.findByTenantId(TENANT_B).stream().anyMatch(p -> p.getId().equals(productA.getId())));

        // Customers
        assertTrue(customerRepository.findByTenantIdAndId(TENANT_B, customerA.getId()).isEmpty());
        assertFalse(customerRepository.findByTenantId(TENANT_B).stream().anyMatch(c -> c.getId().equals(customerA.getId())));

        // Quotes
        assertTrue(quoteRepository.findByTenantIdAndId(TENANT_B, quoteA.getId()).isEmpty());
        assertFalse(quoteRepository.findByTenantId(TENANT_B).stream().anyMatch(q -> q.getId().equals(quoteA.getId())));

        // Bookings
        assertTrue(bookingRepository.findByTenantIdAndId(TENANT_B, bookingA.getId()).isEmpty());
        assertFalse(bookingRepository.findByTenantId(TENANT_B).stream().anyMatch(b -> b.getId().equals(bookingA.getId())));

        // Events
        assertTrue(eventRepository.findByIdAndTenantId(eventA.getId(), UUID.fromString(TENANT_B)).isEmpty());
        assertFalse(eventRepository.findByTenantId(UUID.fromString(TENANT_B)).stream().anyMatch(e -> e.getId().equals(eventA.getId())));

        // Invoices
        assertTrue(invoiceRepository.findByTenantIdAndId(TENANT_B, invoiceA.getId()).isEmpty());
        assertFalse(invoiceRepository.findByTenantId(TENANT_B).stream().anyMatch(i -> i.getId().equals(invoiceA.getId())));
    }
}
