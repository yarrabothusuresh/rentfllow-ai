package com.rentflow.idempotency;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.BookingService;
import com.rentflow.ai.service.QuoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Qualifier("crmEventRepository")
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    private String tenantId;
    private Customer customer;
    private Event event;
    private Product product;

    @BeforeEach
    public void setUp() {
        tenantId = "tenant-bkg-conc-" + UUID.randomUUID();

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUS-" + UUID.randomUUID().toString().substring(0, 6));
        customer.setFirstName("Diana");
        customer.setLastName("Prince");
        customer.setEmail("diana@themyscira.demo");
        customer = customerRepository.save(customer);

        event = new Event();
        event.setTenantId(tenantId);
        event.setEventName("Amazonian Cultural Summit");
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setCustomerId(customer.getId());
        event = eventRepository.save(event);

        product = new Product();
        product.setTenantId(tenantId);
        product.setName("Imperial Arch Pavilion");
        product.setSku("ARCH-IMP-01");
        product.setQuantityOwned(10);
        product.setRentalPrice(new BigDecimal("500.00"));
        product = productRepository.save(product);
    }

    @AfterEach
    public void tearDown() {
        if (tenantId != null) {
            reservationRepository.deleteAll(reservationRepository.findByTenantId(tenantId));
            bookingRepository.deleteAll(bookingRepository.findByTenantId(tenantId));
            quoteRepository.deleteAll(quoteRepository.findByTenantId(tenantId));
            eventRepository.deleteById(event.getId());
            customerRepository.deleteById(customer.getId());
            productRepository.deleteById(product.getId());
        }
    }

    private Quote createAcceptedQuote(int quantity) {
        QuoteDTO qDto = new QuoteDTO();
        qDto.setTenantId(tenantId);
        qDto.setCustomerId(customer.getId());
        qDto.setEventId(event.getId());
        qDto.setStatus(QuoteStatus.SENT);
        qDto.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        qDto.setRentalEndDateTime(LocalDateTime.now().plusDays(7));

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(product.getId());
        item.setQuantity(quantity);
        item.setRentalDays(2);
        item.setUnitPrice(product.getRentalPrice());
        item.setStandardUnitPrice(product.getRentalPrice());
        item.setLineTotal(product.getRentalPrice().multiply(BigDecimal.valueOf(quantity * 2)));
        qDto.setItems(List.of(item));

        QuoteDTO createdQ = quoteService.createQuote(tenantId, qDto, "SALES");
        Quote q = quoteRepository.findById(createdQ.getId()).orElseThrow();
        q.setStatus(QuoteStatus.ACCEPTED);
        return quoteRepository.save(q);
    }

    @Test
    @DisplayName("46. Concurrent Conversion: 10 concurrent requests for same quote create 1 booking & reserve once")
    public void testTenConcurrentConversions_SameQuote_CreatesOneBooking() throws Exception {
        Quote quote = createAcceptedQuote(10);

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        AtomicInteger successes = new AtomicInteger(0);
        ConcurrentLinkedQueue<UUID> bookingIds = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    BookingDTO bkg = bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
                    if (bkg != null && bkg.getId() != null) {
                        bookingIds.add(bkg.getId());
                        successes.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads, successes.get(), "All threads should succeed (initial creation or idempotent replay)");
        UUID firstId = bookingIds.peek();
        for (UUID id : bookingIds) {
            assertEquals(firstId, id, "All threads must receive the same Booking ID");
        }

        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        assertEquals(1, bookings.size(), "Database must have exactly 1 booking");

        List<InventoryReservation> reservations = reservationRepository.findByTenantIdAndBookingId(tenantId, firstId);
        assertEquals(1, reservations.size(), "Exactly 1 reservation record must be created");
        assertEquals(10, reservations.get(0).getQuantity());
    }

    @Test
    @DisplayName("47. Competing Quotes: Two quotes competing for limited stock -> exactly 1 succeeds, 1 gets shortage error")
    public void testCompetingQuotes_ConcurrencyOversellingPrevented() throws Exception {
        Quote quoteA = createAcceptedQuote(10);
        Quote quoteB = createAcceptedQuote(10);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger shortageCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBookingFromQuote(tenantId, quoteA.getId(), "SALES");
                successCount.incrementAndGet();
            } catch (BookingUnavailableException e) {
                shortageCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBookingFromQuote(tenantId, quoteB.getId(), "SALES");
                successCount.incrementAndGet();
            } catch (BookingUnavailableException e) {
                shortageCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly one quote should convert to booking");
        assertEquals(1, shortageCount.get(), "Competing quote must fail with shortage error");

        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        assertEquals(1, bookings.size(), "Total bookings must be 1");

        List<InventoryReservation> reservations = reservationRepository.findByTenantId(tenantId);
        int totalReserved = reservations.stream().mapToInt(InventoryReservation::getQuantity).sum();
        assertEquals(10, totalReserved, "Total reserved stock must be exactly 10, preventing overselling");
    }
}
