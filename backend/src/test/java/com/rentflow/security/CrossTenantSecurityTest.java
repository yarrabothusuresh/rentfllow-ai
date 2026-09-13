package com.rentflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.repository.QuoteRepository;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Day 32 Security Test Suite: Multi-Tenant Isolation & Anti-Spoofing.
 * Proves that Tenant A can never read, modify, delete, or inject relationships
 * into Tenant B's data, even when sending spoofed headers (X-Tenant-Id, X-User-Role).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CrossTenantSecurityTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwtFactory testJwtFactory;

    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private EventRepository eventRepository;

    private Product productA;
    private Product productB;
    private Customer customerA;
    private Customer customerB;
    private Event eventA;
    private Quote quoteB;
    private Booking bookingB;

    @BeforeEach
    public void setUp() {
        SecurityUtils.clearContext();

        // Seed Tenant A Product
        productA = new Product();
        productA.setTenantId(TENANT_A);
        productA.setName("Tenant A Premium Sound System");
        productA.setSku("SKU-SND-A");
        productA.setRentalPrice(BigDecimal.valueOf(120.00));
        productA.setProductType(ProductType.RENTAL_ITEM);
        productA.setStatus(ProductStatus.ACTIVE);
        productA = productRepository.save(productA);

        // Seed Tenant B Product
        productB = new Product();
        productB.setTenantId(TENANT_B);
        productB.setName("Tenant B Ultra Stage Lighting");
        productB.setSku("SKU-LGT-B");
        productB.setRentalPrice(BigDecimal.valueOf(250.00));
        productB.setProductType(ProductType.RENTAL_ITEM);
        productB.setStatus(ProductStatus.ACTIVE);
        productB = productRepository.save(productB);

        // Seed Tenant A Customer
        customerA = new Customer();
        customerA.setTenantId(TENANT_A);
        customerA.setCustomerNumber("CUST-A-001");
        customerA.setFirstName("Alice");
        customerA.setLastName("TenantA");
        customerA.setEmail("alice@tenanta.com");
        customerA.setCustomerType(CustomerType.INDIVIDUAL);
        customerA.setStatus(CustomerStatus.ACTIVE);
        customerA = customerRepository.save(customerA);

        // Seed Tenant A Event
        eventA = new Event();
        eventA.setTenantId(TENANT_A);
        eventA.setCustomerId(customerA.getId());
        eventA.setEventName("Tenant A Corporate Gala");
        eventA.setEventDate(LocalDate.now().plusDays(10));
        eventA.setGuestCount(100);
        eventA.setStatus(EventStatus.PLANNING);
        eventA = eventRepository.save(eventA);

        // Seed Tenant B Customer
        customerB = new Customer();
        customerB.setTenantId(TENANT_B);
        customerB.setCustomerNumber("CUST-B-002");
        customerB.setFirstName("Bob");
        customerB.setLastName("TenantB");
        customerB.setEmail("bob@tenantb.com");
        customerB.setCustomerType(CustomerType.INDIVIDUAL);
        customerB.setStatus(CustomerStatus.ACTIVE);
        customerB = customerRepository.save(customerB);

        // Seed Tenant B Quote
        quoteB = new Quote();
        quoteB.setTenantId(TENANT_B);
        quoteB.setQuoteNumber("Q-TENANT-B-99");
        quoteB.setCustomerId(customerB.getId());
        quoteB.setEventId(UUID.randomUUID());
        quoteB.setStatus(QuoteStatus.DRAFT);
        quoteB.setQuoteDate(LocalDate.now());
        quoteB.setValidUntil(LocalDate.now().plusDays(14));
        quoteB.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        quoteB.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        quoteB.setTotalAmount(BigDecimal.valueOf(1500.00));
        quoteB = quoteRepository.save(quoteB);

        // Seed Tenant B Booking
        bookingB = new Booking();
        bookingB.setTenantId(TENANT_B);
        bookingB.setBookingNumber("BK-TENANT-B-99");
        bookingB.setQuoteId(quoteB.getId());
        bookingB.setCustomerId(customerB.getId());
        bookingB.setEventId(quoteB.getEventId());
        bookingB.setStatus(BookingStatus.CONFIRMED);
        bookingB.setBookingDate(LocalDate.now());
        bookingB.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        bookingB.setRentalEndDateTime(LocalDateTime.now().plusDays(7));
        bookingB.setTotalAmount(BigDecimal.valueOf(1500.00));
        bookingB = bookingRepository.save(bookingB);
    }

    @Test
    @DisplayName("Test 1: Unauthenticated request with spoofed X-Tenant-Id and X-User-Role is rejected with 401")
    public void unauthenticatedRequestWithSpoofedHeaders_isRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/customers")
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "OWNER"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/bookings")
                .header("X-Tenant-Id", TENANT_A)
                .header("X-User-Role", "OWNER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 2: Tenant A JWT with spoofed X-Tenant-Id: Tenant B operates strictly on Tenant A")
    public void authenticatedTenantA_withSpoofedTenantBHeader_operatesStrictlyOnTenantA() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        ProductDTO createDto = new ProductDTO();
        createDto.setName("Tenant A Newly Created Item");
        createDto.setSku("SKU-A-NEW-01");
        createDto.setRentalPrice(BigDecimal.valueOf(45.00));
        createDto.setProductType(ProductType.RENTAL_ITEM);
        createDto.setStatus(ProductStatus.ACTIVE);

        // Attacker sends Tenant A JWT but attempts to spoof Tenant B in the header
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + tokenA)
                .header("X-Tenant-Id", TENANT_B) // Spoofed! Must be completely ignored
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-A-NEW-01"));

        // Verify product was created under Tenant A, NOT Tenant B
        Product saved = productRepository.findByTenantIdAndSkuIgnoreCase(TENANT_A, "SKU-A-NEW-01")
                .orElse(null);
        assertNotNull(saved, "Product must be stamped with Tenant A ID from JWT");
        assertEquals(TENANT_A, saved.getTenantId());

        assertFalse(productRepository.findByTenantIdAndSkuIgnoreCase(TENANT_B, "SKU-A-NEW-01").isPresent(),
                "Product must NEVER be saved under Tenant B");
    }

    @Test
    @DisplayName("Test 3: Cross-Tenant Read returns 404 Not Found (zero information disclosure)")
    public void crossTenantRead_returns404NotFound() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        // Tenant A tries to read Tenant B's product
        mockMvc.perform(get("/api/products/" + productB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Tenant A tries to read Tenant B's customer
        mockMvc.perform(get("/api/customers/" + customerB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Tenant A tries to read Tenant B's quote
        mockMvc.perform(get("/api/quotes/" + quoteB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Tenant A tries to read Tenant B's booking
        mockMvc.perform(get("/api/bookings/" + bookingB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test 4: Cross-Tenant Update and Delete returns 404 Not Found")
    public void crossTenantUpdateAndDelete_returns404NotFound() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "OWNER");

        ProductDTO updateDto = new ProductDTO();
        updateDto.setName("Maliciously Hijacked Name");
        updateDto.setSku(productB.getSku());
        updateDto.setRentalPrice(BigDecimal.valueOf(1.00));

        // Tenant A tries to update Tenant B's product -> 404
        mockMvc.perform(put("/api/products/" + productB.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        // Verify product B was NOT modified
        Product original = productRepository.findById(productB.getId()).orElseThrow();
        assertEquals("Tenant B Ultra Stage Lighting", original.getName());

        // Tenant A tries to delete Tenant B's product -> 404
        mockMvc.perform(delete("/api/products/" + productB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Verify product B still exists
        assertTrue(productRepository.findById(productB.getId()).isPresent());
    }

    @Test
    @DisplayName("Test 5: Cross-Tenant Relationship Injection is rejected with 400 Bad Request")
    public void crossTenantRelationshipInjection_isRejected() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "SALES");

        // Tenant A attempts to create a quote referencing Tenant B's customer
        QuoteDTO maliciousQuote = new QuoteDTO();
        maliciousQuote.setEventId(eventA.getId());
        maliciousQuote.setCustomerId(customerB.getId()); // Foreign tenant customer!
        maliciousQuote.setQuoteDate(LocalDate.now());
        maliciousQuote.setValidUntil(LocalDate.now().plusDays(7));
        maliciousQuote.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        maliciousQuote.setRentalEndDateTime(LocalDateTime.now().plusDays(4));

        mockMvc.perform(post("/api/quotes")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousQuote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Customer does not belong to tenant")));
    }

    @Test
    @DisplayName("Test 6: Cross-Tenant List and Search returns only caller tenant records")
    public void crossTenantListAndSearch_strictlyScopedToCallerTenant() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        // List products
        mockMvc.perform(get("/api/products")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(productA.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(productB.getId().toString()))));

        // List customers
        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(customerA.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(customerB.getId().toString()))));

        // Search products for 'Stage' (which only exists in Tenant B)
        mockMvc.perform(get("/api/products/search")
                .param("query", "Stage")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Test 7: ThreadLocal cleanup ensures tenant context is cleared after request")
    public void threadLocalCleanup_requestDoesNotLeakAcrossThreads() throws Exception {
        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        mockMvc.perform(get("/api/customers")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // Context must be cleared in finally block of TenantContextFilter
        assertFalse(SecurityUtils.hasExplicitTenantContext(), "ThreadLocal/Security context must be completely empty after request execution");
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                SecurityUtils::getCurrentTenantId,
                "Accessing tenant after request execution must throw AuthenticationCredentialsNotFoundException");
    }
}
