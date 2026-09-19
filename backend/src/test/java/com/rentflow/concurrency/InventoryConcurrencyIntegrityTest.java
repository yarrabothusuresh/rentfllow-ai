package com.rentflow.concurrency;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.AvailabilityService;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class InventoryConcurrencyIntegrityTest {

    @Autowired private BookingService bookingService;
    @Autowired private AvailabilityService availabilityService;
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

    @BeforeEach
    void setUp() {
        tenantId = "tenant-day37-" + UUID.randomUUID().toString().substring(0, 8);

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 6));
        customer.setFirstName("Sarah");
        customer.setLastName("Connor");
        customer.setEmail("sarah@resistance.demo");
        customer = customerRepository.save(customer);

        event = new Event();
        event.setTenantId(tenantId);
        event.setCustomerId(customer.getId());
        event.setEventName("Victory Summit 2026");
        event.setEventDate(LocalDate.now().plusDays(15));
        event = eventRepository.save(event);
    }

    @AfterEach
    void tearDown() {
        if (tenantId != null) {
            reservationRepository.deleteAll(reservationRepository.findByTenantId(tenantId));
            bookingRepository.deleteAll(bookingRepository.findByTenantId(tenantId));
            quoteItemRepository.deleteAll();
            quoteRepository.deleteAll(quoteRepository.findByTenantId(tenantId));
            eventRepository.deleteById(event.getId());
            customerRepository.deleteById(customer.getId());
            productRepository.deleteAll(productRepository.findByTenantId(tenantId));
        }
    }

    private Product createProduct(String name, String sku, int qtyOwned, int inMaint, int damaged, int turnaround) {
        Product p = new Product();
        p.setTenantId(tenantId);
        p.setName(name);
        p.setSku(sku);
        p.setStatus(ProductStatus.ACTIVE);
        p.setQuantityOwned(qtyOwned);
        p.setQuantityInMaintenance(inMaint);
        p.setQuantityDamaged(damaged);
        p.setQuantityLost(0);
        p.setDefaultTurnaroundMinutes(turnaround);
        p.setRentalPrice(new BigDecimal("25.00"));
        return productRepository.save(p);
    }

    private Quote createQuote(String quoteNum, LocalDateTime start, LocalDateTime end, Map<Product, Integer> items) {
        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber(quoteNum);
        q.setCustomerId(customer.getId());
        q.setEventId(event.getId());
        q.setStatus(QuoteStatus.SENT);
        q.setRentalStartDateTime(start);
        q.setRentalEndDateTime(end);
        q.setValidUntil(LocalDate.now().plusDays(30));

        BigDecimal subtotal = BigDecimal.ZERO;
        q = quoteRepository.save(q);

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            QuoteItem item = new QuoteItem();
            item.setQuoteId(q.getId());
            item.setProductId(p.getId());
            item.setDescription(p.getName());
            item.setQuantity(qty);
            item.setUnitPrice(p.getRentalPrice());
            BigDecimal lineSub = p.getRentalPrice().multiply(BigDecimal.valueOf(qty));
            item.setLineSubtotal(lineSub);
            quoteItemRepository.save(item);
            subtotal = subtotal.add(lineSub);
        }

        q.setSubtotal(subtotal);
        q.setTotalAmount(subtotal);
        q.setDepositAmount(subtotal.multiply(new BigDecimal("0.30")));
        return quoteRepository.save(q);
    }

    @Test
    @DisplayName("01. Same Product Concurrent Bookings: 100 stock, requests 60 & 50 -> exactly one succeeds, zero oversell")
    void testTwoOverlappingBookingsExceedStock() throws Exception {
        Product chairs = createProduct("Banquet Chair", "CHAIR-BNQ-100", 100, 0, 0, 0);

        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(12).withHour(18).withMinute(0);

        Quote quoteA = createQuote("Q-60", start, end, Map.of(chairs, 60));
        Quote quoteB = createQuote("Q-50", start, end, Map.of(chairs, 50));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger shortageCount = new AtomicInteger(0);
        List<Future<BookingDTO>> futures = new ArrayList<>();

        for (UUID qId : List.of(quoteA.getId(), quoteB.getId())) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
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

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();

        for (Future<BookingDTO> f : futures) {
            try {
                f.get(15, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof BookingUnavailableException);
            }
        }
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one concurrent booking must succeed");
        assertEquals(1, shortageCount.get(), "Competing booking must fail with shortage");

        List<InventoryReservation> active = reservationRepository.findOverlappingReservations(tenantId, chairs.getId(), start, end);
        int totalReserved = active.stream().mapToInt(InventoryReservation::getQuantity).sum();
        assertTrue(totalReserved <= 100, "Total reserved stock (" + totalReserved + ") must not exceed stock (100)");
        assertEquals(1, active.size(), "Only one reservation record should exist");
    }

    @Test
    @DisplayName("02. Ten Simultaneous Bookings: 10 stock, 10 threads request 3 each -> never oversells stock")
    void testTenSimultaneousBookingsNeverOversell() throws Exception {
        Product chairs = createProduct("Stacking Chair", "CHAIR-STK-10", 10, 0, 0, 0);

        LocalDateTime start = LocalDateTime.now().plusDays(20).withHour(9).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(22).withHour(17).withMinute(0);

        int threads = 10;
        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            quotes.add(createQuote("Q-THREAD-" + i, start, end, Map.of(chairs, 3)));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger shortageCount = new AtomicInteger(0);
        List<Future<BookingDTO>> futures = new ArrayList<>();

        for (Quote q : quotes) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    BookingDTO b = bookingService.createBookingFromQuote(tenantId, q.getId(), "CUSTOMER");
                    successCount.incrementAndGet();
                    return b;
                } catch (BookingUnavailableException bue) {
                    shortageCount.incrementAndGet();
                    throw bue;
                }
            }));
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();

        for (Future<BookingDTO> f : futures) {
            try {
                f.get(15, TimeUnit.SECONDS);
            } catch (ExecutionException ignored) {}
        }
        executor.shutdown();

        // 10 stock / 3 per booking = at most 3 can succeed (3 * 3 = 9)
        assertEquals(3, successCount.get(), "At most 3 bookings of 3 chairs can succeed with 10 stock");
        assertEquals(7, shortageCount.get(), "Remaining 7 requests must be rejected with shortage");

        List<InventoryReservation> active = reservationRepository.findOverlappingReservations(tenantId, chairs.getId(), start, end);
        int totalReserved = active.stream().mapToInt(InventoryReservation::getQuantity).sum();
        assertEquals(9, totalReserved, "Total reserved stock must be exactly 9 (<= 10)");
    }

    @Test
    @DisplayName("03. Same Quote Replay: 10 concurrent conversions produce exactly 1 booking & 1 reservation effect")
    void testSameQuoteConversionConcurrentlyCreatesOneBooking() throws Exception {
        Product chairs = createProduct("VIP Leather Armchair", "CHAIR-VIP-01", 10, 0, 0, 0);
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(8).withMinute(0);
        LocalDateTime end = LocalDateTime.now().plusDays(7).withHour(20).withMinute(0);

        Quote quote = createQuote("Q-REPLAY-CONC", start, end, Map.of(chairs, 10));

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);

        ConcurrentLinkedQueue<UUID> returnedBookingIds = new ConcurrentLinkedQueue<>();
        List<Future<BookingDTO>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                BookingDTO b = bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
                returnedBookingIds.add(b.getId());
                return b;
            }));
        }

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();

        for (Future<BookingDTO> f : futures) {
            assertNotNull(f.get(15, TimeUnit.SECONDS));
        }
        executor.shutdown();

        UUID firstId = returnedBookingIds.peek();
        for (UUID id : returnedBookingIds) {
            assertEquals(firstId, id, "All 10 threads must receive the exact same Booking ID");
        }

        List<Booking> bookingsInDb = bookingRepository.findByTenantId(tenantId);
        assertEquals(1, bookingsInDb.size(), "Only 1 booking record must exist in DB");

        List<InventoryReservation> reservations = reservationRepository.findByTenantIdAndBookingId(tenantId, firstId);
        assertEquals(1, reservations.size(), "Exactly 1 reservation record must exist in DB");
        assertEquals(10, reservations.get(0).getQuantity());
    }

    @Test
    @DisplayName("04. Non-Overlapping Dates: Both bookings succeed for full stock because rental periods do not conflict")
    void testNonOverlappingRentalDatesBothSucceed() {
        Product tent = createProduct("Canopy Tent 20x20", "TENT-20-20", 5, 0, 0, 0);

        // Booking 1: Oct 10 to Oct 12
        LocalDateTime start1 = LocalDateTime.of(2026, 10, 10, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2026, 10, 12, 17, 0);
        Quote q1 = createQuote("Q-TENT-1", start1, end1, Map.of(tent, 5));

        // Booking 2: Oct 15 to Oct 17
        LocalDateTime start2 = LocalDateTime.of(2026, 10, 15, 9, 0);
        LocalDateTime end2 = LocalDateTime.of(2026, 10, 17, 17, 0);
        Quote q2 = createQuote("Q-TENT-2", start2, end2, Map.of(tent, 5));

        BookingDTO b1 = bookingService.createBookingFromQuote(tenantId, q1.getId(), "SALES");
        BookingDTO b2 = bookingService.createBookingFromQuote(tenantId, q2.getId(), "SALES");

        assertNotNull(b1);
        assertNotNull(b2);
        assertEquals(BookingStatus.CONFIRMED, b1.getStatus());
        assertEquals(BookingStatus.CONFIRMED, b2.getStatus());

        List<InventoryReservation> r1 = reservationRepository.findOverlappingReservations(tenantId, tent.getId(), start1, end1);
        assertEquals(1, r1.size());
        assertEquals(5, r1.get(0).getQuantity());

        List<InventoryReservation> r2 = reservationRepository.findOverlappingReservations(tenantId, tent.getId(), start2, end2);
        assertEquals(1, r2.size());
        assertEquals(5, r2.get(0).getQuantity());
    }

    @Test
    @DisplayName("05. Overlapping Dates: Partial overlap prevents overselling stock across conflicting ranges")
    void testOverlappingRentalDatesAvailabilityEnforced() {
        Product stage = createProduct("Mobile Stage Unit", "STAGE-MOB-01", 10, 0, 0, 0);

        // Booking 1: Oct 10 to Oct 12, takes 7 units
        LocalDateTime start1 = LocalDateTime.of(2026, 10, 10, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2026, 10, 12, 17, 0);
        Quote q1 = createQuote("Q-STAGE-1", start1, end1, Map.of(stage, 7));
        BookingDTO b1 = bookingService.createBookingFromQuote(tenantId, q1.getId(), "SALES");
        assertNotNull(b1);

        // Booking 2: Oct 11 to Oct 13, requests 5 units (11-12 overlaps -> 7 + 5 = 12 > 10)
        LocalDateTime start2 = LocalDateTime.of(2026, 10, 11, 9, 0);
        LocalDateTime end2 = LocalDateTime.of(2026, 10, 13, 17, 0);
        Quote q2 = createQuote("Q-STAGE-2", start2, end2, Map.of(stage, 5));

        BookingUnavailableException ex = assertThrows(BookingUnavailableException.class, () ->
                bookingService.createBookingFromQuote(tenantId, q2.getId(), "SALES"));

        assertEquals(1, ex.getErrorDetails().getItems().size());
        assertEquals(stage.getId(), ex.getErrorDetails().getItems().get(0).getProductId());
        assertEquals(3, ex.getErrorDetails().getItems().get(0).getAvailableQuantity());
        assertEquals(2, ex.getErrorDetails().getItems().get(0).getShortage());
    }

    @Test
    @DisplayName("06. Adjacent Date Boundaries: Zero turnaround allows contiguous booking; Turnaround buffer blocks conflict")
    void testAdjacentDateBoundariesWithAndWithoutTurnaround() {
        // 1. Zero turnaround: A ends at 12:00, B starts at 12:00 -> Both succeed (end boundary is exclusive)
        Product projector = createProduct("HD Projector", "PROJ-HD-01", 1, 0, 0, 0);
        LocalDateTime startA = LocalDateTime.of(2026, 11, 1, 8, 0);
        LocalDateTime endA = LocalDateTime.of(2026, 11, 1, 12, 0);
        LocalDateTime startB = LocalDateTime.of(2026, 11, 1, 12, 0);
        LocalDateTime endB = LocalDateTime.of(2026, 11, 1, 16, 0);

        Quote qA = createQuote("Q-PROJ-A", startA, endA, Map.of(projector, 1));
        BookingDTO bA = bookingService.createBookingFromQuote(tenantId, qA.getId(), "SALES");
        assertNotNull(bA);

        Quote qB = createQuote("Q-PROJ-B", startB, endB, Map.of(projector, 1));
        BookingDTO bB = bookingService.createBookingFromQuote(tenantId, qB.getId(), "SALES");
        assertNotNull(bB); // Both allowed when turnaround = 0

        // 2. Turnaround of 120 minutes: C ends at 12:00, D starts at 13:00 (within 2h turnaround) -> D blocked
        Product drone = createProduct("Inspection Drone", "DRONE-01", 1, 0, 0, 120);
        LocalDateTime startC = LocalDateTime.of(2026, 11, 5, 8, 0);
        LocalDateTime endC = LocalDateTime.of(2026, 11, 5, 12, 0);
        LocalDateTime startD = LocalDateTime.of(2026, 11, 5, 13, 0); // 1h gap < 2h turnaround
        LocalDateTime endD = LocalDateTime.of(2026, 11, 5, 17, 0);

        Quote qC = createQuote("Q-DRONE-C", startC, endC, Map.of(drone, 1));
        bookingService.createBookingFromQuote(tenantId, qC.getId(), "SALES");

        Quote qD = createQuote("Q-DRONE-D", startD, endD, Map.of(drone, 1));
        assertThrows(BookingUnavailableException.class, () ->
                bookingService.createBookingFromQuote(tenantId, qD.getId(), "SALES"));
    }

    @Test
    @DisplayName("07. Multi-Product All-or-Nothing Rollback: One item short rejects entire booking; zero items reserved")
    void testMultiProductAllOrNothingRollback() {
        Product chairs = createProduct("Folding Chairs", "CHAIR-FLD-100", 100, 0, 0, 0);
        Product tables = createProduct("Banquet Tables", "TBL-BNQ-10", 10, 0, 0, 0);
        Product speakers = createProduct("Powered Speakers", "SPK-PWR-2", 2, 0, 0, 0);

        LocalDateTime start = LocalDateTime.now().plusDays(25);
        LocalDateTime end = LocalDateTime.now().plusDays(27);

        // Quote requests 50 chairs (available), 5 tables (available), 3 speakers (shortage: 2 in stock)
        Map<Product, Integer> itemMap = new LinkedHashMap<>();
        itemMap.put(chairs, 50);
        itemMap.put(tables, 5);
        itemMap.put(speakers, 3);

        Quote quote = createQuote("Q-MULTI-FAIL", start, end, itemMap);

        BookingUnavailableException ex = assertThrows(BookingUnavailableException.class, () ->
                bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES"));

        assertEquals(1, ex.getErrorDetails().getItems().size());
        assertEquals(speakers.getId(), ex.getErrorDetails().getItems().get(0).getProductId());

        // Verify All-or-Nothing Rollback: NO reservations created for chairs, tables, or speakers
        List<InventoryReservation> res = reservationRepository.findByTenantId(tenantId);
        assertTrue(res.isEmpty(), "Zero reservations must be created when any line item is unavailable");

        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        assertTrue(bookings.isEmpty(), "Zero bookings must be created when quote conversion fails");
    }

    @Test
    @DisplayName("08. Cross-Tenant Inventory Security: Tenant A cannot reserve or lock Tenant B products")
    void testCrossTenantInventoryAccessBlocked() {
        String tenantB = "tenant-b-" + UUID.randomUUID().toString().substring(0, 6);
        Product productB = new Product();
        productB.setTenantId(tenantB);
        productB.setName("Tenant B Private Equipment");
        productB.setSku("B-PRIV-01");
        productB.setStatus(ProductStatus.ACTIVE);
        productB.setQuantityOwned(20);
        productB.setRentalPrice(new BigDecimal("100.00"));
        productB = productRepository.save(productB);

        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(12);

        // Quote in Tenant A tries to reference Product B
        Quote quote = new Quote();
        quote.setTenantId(tenantId);
        quote.setQuoteNumber("Q-HACK-01");
        quote.setCustomerId(customer.getId());
        quote.setEventId(event.getId());
        quote.setStatus(QuoteStatus.SENT);
        quote.setRentalStartDateTime(start);
        quote.setRentalEndDateTime(end);
        final Quote savedQuote = quoteRepository.save(quote);

        QuoteItem maliciousItem = new QuoteItem();
        maliciousItem.setQuoteId(savedQuote.getId());
        maliciousItem.setProductId(productB.getId());
        maliciousItem.setDescription("Illicit Item");
        maliciousItem.setQuantity(5);
        maliciousItem.setUnitPrice(new BigDecimal("100.00"));
        maliciousItem.setLineSubtotal(new BigDecimal("500.00"));
        quoteItemRepository.save(maliciousItem);

        // Attempt booking in Tenant A
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingFromQuote(tenantId, savedQuote.getId(), "SALES"));
        assertTrue(ex.getMessage().contains("Product not found or access denied"));

        // Verify Tenant B's product stock and reservations remain untouched
        List<InventoryReservation> bRes = reservationRepository.findByTenantId(tenantB);
        assertTrue(bRes.isEmpty(), "Tenant B inventory must remain completely unaffected");
        Product pBReloaded = productRepository.findById(productB.getId()).orElseThrow();
        assertEquals(20, pBReloaded.getQuantityOwned());
    }

    @Test
    @DisplayName("09. Cancellation Replay Is Idempotent: Canceling twice releases reservation once with zero stock inflation")
    void testCancellationReplayIsIdempotent() {
        Product lighting = createProduct("Stage Lighting Kit", "LGT-STG-05", 5, 0, 0, 0);
        LocalDateTime start = LocalDateTime.now().plusDays(30);
        LocalDateTime end = LocalDateTime.now().plusDays(32);

        Quote q = createQuote("Q-CANCEL-TEST", start, end, Map.of(lighting, 5));
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, q.getId(), "SALES");
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        // Availability during rental dates is now 0
        AvailabilityResultDTO availBefore = availabilityService.checkAvailability(tenantId, lighting.getId(), 5, start, end);
        assertEquals(0, availBefore.getAvailableQuantity());

        // Cancel booking 1st time
        BookingDTO cancelled1 = bookingService.cancelBooking(tenantId, booking.getId(), "SALES");
        assertEquals(BookingStatus.CANCELLED, cancelled1.getStatus());

        // Stock is released back to 5
        AvailabilityResultDTO availAfter1 = availabilityService.checkAvailability(tenantId, lighting.getId(), 5, start, end);
        assertEquals(5, availAfter1.getAvailableQuantity());

        // Replay cancellation 2nd time
        BookingDTO cancelled2 = bookingService.cancelBooking(tenantId, booking.getId(), "SALES");
        assertEquals(BookingStatus.CANCELLED, cancelled2.getStatus());

        // Stock must STILL be 5 (no double release or inflation)
        AvailabilityResultDTO availAfter2 = availabilityService.checkAvailability(tenantId, lighting.getId(), 5, start, end);
        assertEquals(5, availAfter2.getAvailableQuantity());
    }

    @Test
    @DisplayName("10. Damaged & Maintenance Stock Deducted: Unavailable stock is never rentable")
    void testDamagedAndMaintenanceStockExcludedFromRentableInventory() {
        // 10 owned, 3 damaged, 1 in maintenance -> available = 10 - 3 - 1 = 6
        Product generators = createProduct("Diesel Generator", "GEN-DSL-10", 10, 1, 3, 0);
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = LocalDateTime.now().plusDays(12);

        AvailabilityResultDTO avail = availabilityService.checkAvailability(tenantId, generators.getId(), 6, start, end);
        assertEquals(6, avail.getAvailableQuantity());
        assertTrue(avail.isAvailable());

        // Requesting 7 units exceeds the 6 available rentable units
        Quote q = createQuote("Q-GEN-OVER", start, end, Map.of(generators, 7));
        BookingUnavailableException ex = assertThrows(BookingUnavailableException.class, () ->
                bookingService.createBookingFromQuote(tenantId, q.getId(), "SALES"));

        assertEquals(6, ex.getErrorDetails().getItems().get(0).getAvailableQuantity());
        assertEquals(1, ex.getErrorDetails().getItems().get(0).getShortage());
    }

    @Test
    @DisplayName("11. Stale Availability Revalidation: Confirms under lock that unavailable stock cannot be confirmed")
    void testStaleAvailabilityRevalidatedUnderLock() {
        Product mixer = createProduct("Audio Mixer", "MIX-AUD-01", 1, 0, 0, 0);
        LocalDateTime start = LocalDateTime.now().plusDays(14);
        LocalDateTime end = LocalDateTime.now().plusDays(16);

        // Quote 1 & Quote 2 both see 1 available initially
        Quote q1 = createQuote("Q-MIX-1", start, end, Map.of(mixer, 1));
        Quote q2 = createQuote("Q-MIX-2", start, end, Map.of(mixer, 1));

        // Quote 1 confirms booking first
        BookingDTO b1 = bookingService.createBookingFromQuote(tenantId, q1.getId(), "CUSTOMER");
        assertNotNull(b1);

        // Quote 2 submits with stale belief that 1 is available -> revalidation under lock rejects it
        BookingUnavailableException ex = assertThrows(BookingUnavailableException.class, () ->
                bookingService.createBookingFromQuote(tenantId, q2.getId(), "CUSTOMER"));

        assertEquals(0, ex.getErrorDetails().getItems().get(0).getAvailableQuantity());
        assertEquals(1, ex.getErrorDetails().getItems().get(0).getShortage());
    }
}
