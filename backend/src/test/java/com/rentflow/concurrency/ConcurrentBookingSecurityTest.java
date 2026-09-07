package com.rentflow.concurrency;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentBookingSecurityTest {

    @Autowired private BookingService bookingService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private QuoteItemRepository quoteItemRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private InventoryReservationRepository reservationRepository;

    private String tenantId;
    private Customer customer;
    private Event event;
    private Product product;
    private Product productB;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID().toString();

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8));
        customer.setFirstName("Acme");
        customer.setLastName("Events");
        customer.setEmail("ops@acme.com");
        customer = customerRepository.save(customer);

        event = new Event();
        event.setTenantId(tenantId);
        event.setCustomerId(customer.getId());
        event.setEventName("Concurrent Gala");
        event.setEventDate(LocalDate.now().plusDays(5));
        event = eventRepository.save(event);

        // Product with limited stock = 10
        product = new Product();
        product.setTenantId(tenantId);
        product.setName("Grand Stage Truss");
        product.setSku("TRUSS-" + UUID.randomUUID().toString().substring(0, 8));
        product.setStatus(ProductStatus.ACTIVE);
        product.setQuantityOwned(10);
        product.setQuantityInMaintenance(0);
        product.setQuantityDamaged(0);
        product.setQuantityLost(0);
        product.setRentalPrice(new BigDecimal("150.00"));
        product = productRepository.save(product);

        // Second product for multi-item deadlock prevention test
        productB = new Product();
        productB.setTenantId(tenantId);
        productB.setName("LED Video Wall");
        productB.setSku("LED-" + UUID.randomUUID().toString().substring(0, 8));
        productB.setStatus(ProductStatus.ACTIVE);
        productB.setQuantityOwned(10);
        productB.setQuantityInMaintenance(0);
        productB.setQuantityDamaged(0);
        productB.setQuantityLost(0);
        productB.setRentalPrice(new BigDecimal("300.00"));
        productB = productRepository.save(productB);
    }

    @AfterEach
    void tearDown() {
        // Cleanup data for this tenant
        reservationRepository.deleteAll(reservationRepository.findByTenantId(tenantId));
        bookingRepository.deleteAll(bookingRepository.findByTenantId(tenantId));
        quoteItemRepository.deleteAll();
        quoteRepository.deleteAll(quoteRepository.findByTenantId(tenantId));
        eventRepository.deleteAll(eventRepository.findByTenantId(tenantId));
        customerRepository.deleteAll(customerRepository.findByTenantId(tenantId));
        productRepository.deleteAll(productRepository.findByTenantId(tenantId));
    }

    @Test
    @DisplayName("Concurrent Checkouts for Last Available Stock: Exactly One Succeeds, Zero Overselling")
    void testConcurrentCheckoutOversellingPrevention() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(12).withHour(18).withMinute(0);

        // Quote 1 requests all 10 units
        Quote quote1 = createTestQuote("Q-RACE-001", start, end);
        createQuoteItem(quote1, product.getId(), 10, new BigDecimal("150.00"));

        // Quote 2 also requests all 10 units for the same period
        Quote quote2 = createTestQuote("Q-RACE-002", start, end);
        createQuoteItem(quote2, product.getId(), 10, new BigDecimal("150.00"));

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger shortageCount = new AtomicInteger(0);
        List<Future<BookingDTO>> futures = new ArrayList<>();

        UUID[] quoteIds = new UUID[]{quote1.getId(), quote2.getId()};

        for (int i = 0; i < threads; i++) {
            final UUID qId = quoteIds[i];
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                // Wait for all threads to be simultaneously ready
                startLatch.await();
                try {
                    BookingDTO b = bookingService.createBookingFromQuote(tenantId, qId, "CUSTOMER");
                    successCount.incrementAndGet();
                    return b;
                } catch (BookingUnavailableException bue) {
                    shortageCount.incrementAndGet();
                    throw bue;
                }
            }));
        }

        // Wait until both worker threads are spun up and waiting
        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        // Unleash both threads simultaneously
        startLatch.countDown();

        for (Future<BookingDTO> future : futures) {
            try {
                future.get(15, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                // One must fail with BookingUnavailableException
                assertTrue(e.getCause() instanceof BookingUnavailableException,
                        "Expected BookingUnavailableException but got: " + e.getCause());
            }
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // EXACTLY ONE booking must succeed and EXACTLY ONE must be rejected due to shortage
        assertEquals(1, successCount.get(), "Exactly one concurrent booking should succeed");
        assertEquals(1, shortageCount.get(), "Exactly one concurrent booking should fail with shortage");

        // Verify reservations in database
        List<InventoryReservation> reservations = reservationRepository.findByTenantId(tenantId);
        assertEquals(1, reservations.size(), "Only one reservation record should exist");
        assertEquals(10, reservations.get(0).getQuantity(), "Reserved quantity should equal exactly 10 (no double booking)");

        // Verify quotes status
        Quote q1Updated = quoteRepository.findById(quote1.getId()).orElseThrow();
        Quote q2Updated = quoteRepository.findById(quote2.getId()).orElseThrow();
        boolean q1Accepted = q1Updated.getStatus() == QuoteStatus.ACCEPTED;
        boolean q2Accepted = q2Updated.getStatus() == QuoteStatus.ACCEPTED;
        assertTrue(q1Accepted ^ q2Accepted, "Exactly one quote must be ACCEPTED and the other not accepted");
    }

    @Test
    @DisplayName("Deadlock-Free Canonical Lock Ordering on Multi-Product Concurrent Bookings")
    void testCanonicalLockOrderingPreventsDeadlock() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(20).withHour(9).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(22).withHour(18).withMinute(0);

        // Quote 1 requests [product, productB]
        Quote quote1 = createTestQuote("Q-MULTI-001", start, end);
        createQuoteItem(quote1, product.getId(), 5, new BigDecimal("150.00"));
        createQuoteItem(quote1, productB.getId(), 5, new BigDecimal("300.00"));

        // Quote 2 requests [productB, product] in reverse order
        Quote quote2 = createTestQuote("Q-MULTI-002", start, end);
        createQuoteItem(quote2, productB.getId(), 5, new BigDecimal("300.00"));
        createQuoteItem(quote2, product.getId(), 5, new BigDecimal("150.00"));

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<BookingDTO>> futures = new ArrayList<>();
        UUID[] quoteIds = new UUID[]{quote1.getId(), quote2.getId()};

        for (int i = 0; i < threads; i++) {
            final UUID qId = quoteIds[i];
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                BookingDTO b = bookingService.createBookingFromQuote(tenantId, qId, "SALES");
                successCount.incrementAndGet();
                return b;
            }));
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();

        for (Future<BookingDTO> future : futures) {
            BookingDTO b = future.get(15, TimeUnit.SECONDS);
            assertNotNull(b);
            assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Since total stock for each is 10 and each requested 5, BOTH should succeed concurrently without deadlock
        assertEquals(2, successCount.get(), "Both multi-item bookings should succeed concurrently without deadlock");

        List<InventoryReservation> reservations = reservationRepository.findByTenantId(tenantId);
        assertEquals(4, reservations.size(), "4 reservation records (2 per quote) should exist");
    }

    private Quote createTestQuote(String quoteNumber, LocalDateTime start, LocalDateTime end) {
        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setQuoteNumber(quoteNumber);
        quote.setCustomerId(customer.getId());
        quote.setEventId(event.getId());
        quote.setStatus(QuoteStatus.SENT);
        quote.setRentalStartDateTime(start);
        quote.setRentalEndDateTime(end);
        quote.setValidUntil(LocalDate.now().plusDays(30));
        quote.setSubtotal(new BigDecimal("1500.00"));
        quote.setTaxAmount(BigDecimal.ZERO);
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quote.setDepositAmount(new BigDecimal("500.00"));
        return quoteRepository.save(quote);
    }

    private QuoteItem createQuoteItem(Quote quote, UUID productId, int qty, BigDecimal unitPrice) {
        QuoteItem item = new QuoteItem();
        item.setQuoteId(quote.getId());
        item.setProductId(productId);
        item.setDescription("Test item");
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setLineSubtotal(unitPrice.multiply(BigDecimal.valueOf(qty)));
        return quoteItemRepository.save(item);
    }
}
