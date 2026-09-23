package com.rentflow.migration;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.security.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DatabaseConstraintsAndPaginationTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TestJwtFactory testJwtFactory;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        // Ensure tenant records exist in tenant table for foreign keys
        jdbcTemplate.update("MERGE INTO tenant (id, name) KEY(id) VALUES (?, ?)", UUID.fromString(TENANT_A), "Tenant Alpha");
        jdbcTemplate.update("MERGE INTO tenant (id, name) KEY(id) VALUES (?, ?)", UUID.fromString(TENANT_B), "Tenant Beta");
    }

    private Booking createBooking(String tenantId, String bookingNumber, UUID customerId) {
        Booking b = new Booking();
        b.setTenantId(tenantId);
        b.setBookingNumber(bookingNumber);
        b.setCustomerId(customerId != null ? customerId : UUID.randomUUID());
        b.setEventId(UUID.randomUUID());
        b.setQuoteId(UUID.randomUUID());
        b.setStatus(BookingStatus.CONFIRMED);
        b.setBookingDate(LocalDate.now());
        b.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        b.setRentalEndDateTime(LocalDateTime.now().plusDays(2));
        b.setSubtotal(BigDecimal.ZERO);
        b.setTotalAmount(BigDecimal.ZERO);
        b.setDepositPaid(BigDecimal.ZERO);
        b.setBalanceDue(BigDecimal.ZERO);
        return b;
    }

    private Quote createQuote(String tenantId, String quoteNumber) {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber(quoteNumber);
        q.setCustomerId(UUID.randomUUID());
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.DRAFT);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(30));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(2));
        q.setSubtotal(BigDecimal.ZERO);
        q.setTotalAmount(BigDecimal.ZERO);
        return q;
    }

    private Customer createCustomer(String tenantId, String customerNumber, String firstName) {
        Customer c = new Customer();
        c.setTenantId(tenantId);
        c.setCustomerNumber(customerNumber);
        c.setFirstName(firstName);
        c.setLastName("Tester");
        c.setEmail(firstName.toLowerCase() + "." + customerNumber.toLowerCase() + "@test.com");
        c.setCustomerType(CustomerType.INDIVIDUAL);
        return c;
    }

    private Product createProduct(String tenantId, String sku, String name) {
        Product p = new Product();
        p.setTenantId(tenantId);
        p.setSku(sku);
        p.setName(name);
        p.setProductType(ProductType.RENTAL_ITEM);
        p.setStatus(ProductStatus.ACTIVE);
        p.setQuantityOwned(10);
        p.setRentalPrice(new BigDecimal("15.00"));
        p.setReplacementCost(new BigDecimal("100.00"));
        return p;
    }

    // =========================================================================
    // 1. MULTI-TENANT UNIQUENESS CONSTRAINTS
    // =========================================================================

    @Test
    @DisplayName("Constraint: Duplicate booking_number within the SAME tenant is rejected")
    void testDuplicateBookingNumberSameTenantRejected() {
        String bkgNum = "BKG-UNIQUE-TEST-" + UUID.randomUUID().toString().substring(0, 5);
        Booking b1 = createBooking(TENANT_A, bkgNum, null);
        bookingRepository.saveAndFlush(b1);

        Booking b2 = createBooking(TENANT_A, bkgNum, null);
        assertThrows(Exception.class, () -> bookingRepository.saveAndFlush(b2),
                "Duplicate booking_number within same tenant must violate uq_booking_tenant_number");
    }

    @Test
    @DisplayName("Constraint: Duplicate quote_number within the SAME tenant is rejected")
    void testDuplicateQuoteNumberSameTenantRejected() {
        String quoteNum = "QT-UNIQUE-TEST-" + UUID.randomUUID().toString().substring(0, 5);
        Quote q1 = createQuote(TENANT_A, quoteNum);
        quoteRepository.saveAndFlush(q1);

        Quote q2 = createQuote(TENANT_A, quoteNum);
        assertThrows(Exception.class, () -> quoteRepository.saveAndFlush(q2),
                "Duplicate quote_number within same tenant must violate uq_quotes_tenant_number");
    }

    @Test
    @DisplayName("Constraint: Duplicate customer_number within the SAME tenant is rejected")
    void testDuplicateCustomerNumberSameTenantRejected() {
        String custNum = "CUS-UNIQUE-TEST-" + UUID.randomUUID().toString().substring(0, 5);
        Customer c1 = createCustomer(TENANT_A, custNum, "Alice");
        customerRepository.saveAndFlush(c1);

        Customer c2 = createCustomer(TENANT_A, custNum, "Bob");
        assertThrows(Exception.class, () -> customerRepository.saveAndFlush(c2),
                "Duplicate customer_number within same tenant must violate uq_customers_tenant_number");
    }

    @Test
    @DisplayName("Constraint: Duplicate SKU within the SAME tenant is rejected")
    void testDuplicateProductSkuSameTenantRejected() {
        String sku = "SKU-UNIQUE-TEST-" + UUID.randomUUID().toString().substring(0, 5);
        Product p1 = createProduct(TENANT_A, sku, "Chairs");
        productRepository.saveAndFlush(p1);

        Product p2 = createProduct(TENANT_A, sku, "Tables");
        assertThrows(Exception.class, () -> productRepository.saveAndFlush(p2),
                "Duplicate SKU within same tenant must violate uq_products_tenant_sku");
    }

    @Test
    @DisplayName("Constraint: Same business number across DIFFERENT tenants is allowed")
    void testCrossTenantSameBusinessNumberAllowed() {
        String sharedBookingNumber = "BKG-SHARED-001";
        String sharedQuoteNumber = "QT-SHARED-001";
        String sharedCustomerNumber = "CUS-SHARED-001";
        String sharedSku = "SKU-SHARED-001";

        // Tenant A saves records
        Booking bA = createBooking(TENANT_A, sharedBookingNumber, null);
        bookingRepository.saveAndFlush(bA);

        Quote qA = createQuote(TENANT_A, sharedQuoteNumber);
        quoteRepository.saveAndFlush(qA);

        Customer cA = createCustomer(TENANT_A, sharedCustomerNumber, "Alice");
        customerRepository.saveAndFlush(cA);

        Product pA = createProduct(TENANT_A, sharedSku, "Sound System A");
        productRepository.saveAndFlush(pA);

        // Tenant B saves the EXACT SAME numbers/SKUs - MUST SUCCEED
        Booking bB = createBooking(TENANT_B, sharedBookingNumber, null);
        assertDoesNotThrow(() -> bookingRepository.saveAndFlush(bB));

        Quote qB = createQuote(TENANT_B, sharedQuoteNumber);
        assertDoesNotThrow(() -> quoteRepository.saveAndFlush(qB));

        Customer cB = createCustomer(TENANT_B, sharedCustomerNumber, "Bob");
        assertDoesNotThrow(() -> customerRepository.saveAndFlush(cB));

        Product pB = createProduct(TENANT_B, sharedSku, "Sound System B");
        assertDoesNotThrow(() -> productRepository.saveAndFlush(pB));
    }

    // =========================================================================
    // 2. CHECK CONSTRAINTS ENFORCEMENT
    // =========================================================================

    @Test
    @DisplayName("CHECK Constraint: chk_booking_rental_dates rejects rental_end < rental_start")
    void testCheckConstraintBookingRentalDates() {
        UUID bkgId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime invalidEnd = LocalDateTime.now().plusDays(2); // End before start

        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO booking (id, tenant_id, booking_number, status, rental_start_date_time, rental_end_date_time, customer_id, event_id, quote_id, booking_date) " +
                "VALUES (?, ?, ?, 'CONFIRMED', ?, ?, ?, ?, ?, CURRENT_DATE)",
                bkgId, TENANT_A, "BKG-CHK-DATES", start, invalidEnd, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );
        }, "Inserting booking with rental_end < rental_start must violate chk_booking_rental_dates");
    }

    @Test
    @DisplayName("CHECK Constraint: chk_booking_item_quantity rejects quantity <= 0")
    void testCheckConstraintBookingItemQuantity() {
        UUID bkgId = UUID.randomUUID();
        UUID custId = UUID.randomUUID();
        UUID evtId = UUID.randomUUID();
        UUID qteId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO booking (id, tenant_id, booking_number, status, rental_start_date_time, rental_end_date_time, customer_id, event_id, quote_id, booking_date) " +
            "VALUES (?, ?, ?, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, CURRENT_DATE)",
            bkgId, TENANT_A, "BKG-CHK-ITEM-QTY", custId, evtId, qteId
        );

        UUID itemId = UUID.randomUUID();
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO booking_item (id, booking_id, description, quantity, unit_price, rental_start_date_time, rental_end_date_time) " +
                "VALUES (?, ?, 'Folding Chair', 0, 5.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                itemId, bkgId
            );
        }, "Inserting booking item with quantity <= 0 must violate chk_booking_item_quantity");
    }

    @Test
    @DisplayName("CHECK Constraint: chk_payment_amount_positive rejects non-positive payment amounts")
    void testCheckConstraintPaymentAmountPositive() {
        UUID paymentId = UUID.randomUUID();
        UUID bkgId = UUID.randomUUID();
        UUID custId = UUID.randomUUID();
        UUID evtId = UUID.randomUUID();
        UUID qteId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO booking (id, tenant_id, booking_number, status, rental_start_date_time, rental_end_date_time, customer_id, event_id, quote_id, booking_date) " +
            "VALUES (?, ?, ?, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, CURRENT_DATE)",
            bkgId, TENANT_A, "BKG-CHK-PAY", custId, evtId, qteId
        );

        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO payment (id, tenant_id, booking_id, customer_id, amount, payment_method, payment_status, payment_date, transaction_reference, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, -50.00, 'CREDIT_CARD', 'COMPLETED', CURRENT_DATE, 'TX-FAIL-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                paymentId, TENANT_A, bkgId, custId
            );
        }, "Inserting payment with negative amount must violate chk_payment_amount_positive");
    }

    // =========================================================================
    // 3. API PAGINATION BOUNDS & VALIDATION
    // =========================================================================

    @Test
    @DisplayName("Pagination: Excessive page size (> 100) returns HTTP 400 Bad Request")
    void testExcessivePageSizeRejected() throws Exception {
        String token = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        mockMvc.perform(get("/api/bookings")
                .param("page", "0")
                .param("size", "1000000")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Page size cannot exceed")));
    }

    @Test
    @DisplayName("Pagination: Negative page index (< 0) returns HTTP 400 Bad Request")
    void testNegativePageIndexRejected() throws Exception {
        String token = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        mockMvc.perform(get("/api/bookings")
                .param("page", "-1")
                .param("size", "20")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Page index must not be negative")));
    }

    @Test
    @DisplayName("Pagination: Disallowed sort field (SQL injection / property tamper) returns HTTP 400")
    void testDisallowedSortFieldRejected() throws Exception {
        String token = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        mockMvc.perform(get("/api/bookings")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "passwordHash")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Invalid sort property")));
    }

    @Test
    @DisplayName("Pagination: Default request returns bounded 20 rows max and metadata")
    void testDefaultPaginationReturnsBoundedResults() throws Exception {
        // Seed 25 bookings for Tenant A
        for (int i = 1; i <= 25; i++) {
            Booking b = createBooking(TENANT_A, "BKG-PAGE-" + i, null);
            b.setRentalStartDateTime(LocalDateTime.now().plusDays(i));
            b.setRentalEndDateTime(LocalDateTime.now().plusDays(i + 1));
            bookingRepository.save(b);
        }
        bookingRepository.flush();

        String token = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");

        // Request with default params
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20))) // Default size is 20
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(20)));
    }

    // =========================================================================
    // 4. TENANT AND CUSTOMER PORTAL COUNT ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Isolation: TotalElements and content are strictly isolated across tenants")
    void testTenantCountIsolation() throws Exception {
        // Tenant A: 3 bookings
        for (int i = 1; i <= 3; i++) {
            bookingRepository.save(createBooking(TENANT_A, "BKG-ISO-A-" + i, null));
        }

        // Tenant B: 7 bookings
        for (int i = 1; i <= 7; i++) {
            bookingRepository.save(createBooking(TENANT_B, "BKG-ISO-B-" + i, null));
        }
        bookingRepository.flush();

        String tokenA = testJwtFactory.createStaffToken(TENANT_A, "ADMIN");
        String tokenB = testJwtFactory.createStaffToken(TENANT_B, "ADMIN");

        // Tenant A query
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content[*].bookingNumber", everyItem(startsWith("BKG-ISO-A"))));

        // Tenant B query
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(7)))
                .andExpect(jsonPath("$.content[*].bookingNumber", everyItem(startsWith("BKG-ISO-B"))));
    }

    @Test
    @DisplayName("Isolation: Customer Portal TotalElements is strictly isolated to customer's own records")
    void testCustomerPortalCountIsolation() throws Exception {
        Customer c1 = createCustomer(TENANT_A, "CUST-P-1", "Carol");
        c1 = customerRepository.saveAndFlush(c1);
        UUID cust1 = c1.getId();

        Customer c2 = createCustomer(TENANT_A, "CUST-P-2", "Dave");
        c2 = customerRepository.saveAndFlush(c2);
        UUID cust2 = c2.getId();

        // Customer 1: 2 bookings
        for (int i = 1; i <= 2; i++) {
            bookingRepository.save(createBooking(TENANT_A, "BKG-PORTAL-C1-" + i, cust1));
        }

        // Customer 2: 4 bookings
        for (int i = 1; i <= 4; i++) {
            bookingRepository.save(createBooking(TENANT_A, "BKG-PORTAL-C2-" + i, cust2));
        }
        bookingRepository.flush();

        String custToken1 = testJwtFactory.createCustomerToken(TENANT_A, cust1);

        // Customer 1 portal query
        mockMvc.perform(get("/api/portal/bookings")
                .header("Authorization", "Bearer " + custToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[*].bookingNumber", everyItem(startsWith("BKG-PORTAL-C1"))));
    }
}
