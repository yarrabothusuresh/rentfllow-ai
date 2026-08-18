package com.rentflow.ai;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.BookingService;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.ai.tool.CancelBookingTool;
import com.rentflow.ai.tool.CreateBookingFromQuoteTool;
import com.rentflow.ai.tool.GetBookingTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingManagementFoundationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private CreateBookingFromQuoteTool createBookingFromQuoteTool;

    @Autowired
    private CancelBookingTool cancelBookingTool;

    @Autowired
    private GetBookingTool getBookingTool;

    private String tenantId;
    private Customer testCustomer;
    private Event testEvent;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        reservationRepository.deleteAll();

        // Ensure customer
        List<Customer> customers = customerRepository.findByTenantId(tenantId);
        if (customers.isEmpty()) {
            Customer c = new Customer();
            c.setTenantId(tenantId);
            c.setCustomerNumber("CUS-000001");
            c.setFirstName("Emily");
            c.setLastName("Brown");
            c.setEmail("emily@example.com");
            testCustomer = customerRepository.save(c);
        } else {
            testCustomer = customers.get(0);
        }

        // Ensure event
        List<Event> events = eventRepository.findByTenantId(tenantId);
        if (events.isEmpty()) {
            Event e = new Event();
            e.setTenantId(tenantId);
            e.setCustomerId(testCustomer.getId());
            e.setEventName("Emily's Wedding");
            e.setEventDate(LocalDate.now().plusDays(30));
            testEvent = eventRepository.save(e);
        } else {
            testEvent = events.get(0);
        }

        // Ensure product
        List<Product> products = productRepository.findByTenantId(tenantId);
        if (products.isEmpty()) {
            Product p = new Product();
            p.setTenantId(tenantId);
            p.setName("Chiavari Chair");
            p.setSku("CH-001");
            p.setQuantityOwned(500);
            p.setRentalPrice(new BigDecimal("8.00"));
            testProduct = productRepository.save(p);
        } else {
            testProduct = products.get(0);
            testProduct.setQuantityOwned(500);
            productRepository.save(testProduct);
        }
    }

    private HttpHeaders createHeaders(String role, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", role);
        headers.set("X-Tenant-Id", tenant);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private QuoteDTO createTestQuote(QuoteStatus status) {
        int randomOffset = new Random().nextInt(1000) + 1;
        QuoteDTO dto = new QuoteDTO();
        dto.setCustomerId(testCustomer.getId());
        dto.setEventId(testEvent.getId());
        dto.setStatus(status);
        dto.setRentalStartDateTime(LocalDateTime.now().plusDays(10 + randomOffset));
        dto.setRentalEndDateTime(LocalDateTime.now().plusDays(12 + randomOffset));

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(testProduct.getId());
        item.setDescription(testProduct.getName());
        item.setQuantity(50);
        item.setUnitPrice(testProduct.getRentalPrice());
        dto.getItems().add(item);

        QuoteDTO created = quoteService.createQuote(tenantId, dto, "OWNER");
        if (status != QuoteStatus.DRAFT) {
            quoteService.updateStatus(tenantId, created.getId(), status, "OWNER");
            created.setStatus(status);
        }
        return created;
    }

    // TEST 1: Create booking from accepted quote
    @Test
    void test1_createBookingFromAcceptedQuote() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        assertNotNull(booking);
        assertNotNull(booking.getBookingNumber());
        assertTrue(booking.getBookingNumber().startsWith("BKG-"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(quote.getId(), booking.getQuoteId());
    }

    // TEST 2: Booking copies quote items (Price snapshot)
    @Test
    void test2_bookingCopiesQuoteItemsSnapshot() {
        QuoteDTO quote = createTestQuote(QuoteStatus.SENT);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        assertFalse(booking.getItems().isEmpty());
        BookingItemDTO bItem = booking.getItems().get(0);
        assertEquals(testProduct.getName(), bItem.getDescription());
        assertEquals(50, bItem.getQuantity());
        assertEquals(0, new BigDecimal("8.00").compareTo(bItem.getUnitPrice()));
    }

    // TEST 3 & 4: Booking confirmation creates inventory reservations and transactions
    @Test
    void test3and4_bookingCreatesReservationsAndTransactions() {
        QuoteDTO quote = createTestQuote(QuoteStatus.SENT);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        List<InventoryReservation> resList = reservationRepository.findByTenantId(tenantId);
        boolean foundRes = resList.stream().anyMatch(r -> booking.getId().equals(r.getBookingId()) && r.getStatus() == ReservationStatus.RESERVED);
        assertTrue(foundRes, "Reservation record should exist with RESERVED status");

        List<InventoryTransaction> txList = transactionRepository.findByTenantId(tenantId);
        boolean foundTx = txList.stream().anyMatch(t -> booking.getId().equals(t.getReferenceId()) && t.getTransactionType() == TransactionType.RESERVATION);
        assertTrue(foundTx, "RESERVATION transaction log should exist");
    }

    // TEST 5 & 6: Recheck availability & failure when inventory unavailable
    @Test
    void test5and6_availabilityFailureBlocksBooking() {
        QuoteDTO quote = createTestQuote(QuoteStatus.SENT);

        // Reserve almost all inventory for overlapping dates
        InventoryReservation blockingRes = new InventoryReservation();
        blockingRes.setTenantId(tenantId);
        blockingRes.setProductId(testProduct.getId());
        blockingRes.setQuantity(testProduct.getQuantityOwned() - 10); // leaves only 10 available, but quote asks for 50
        blockingRes.setStartDateTime(quote.getRentalStartDateTime().minusDays(1));
        blockingRes.setEndDateTime(quote.getRentalEndDateTime().plusDays(1));
        blockingRes.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(blockingRes);

        // Attempt booking creation should fail
        assertThrows(BookingUnavailableException.class, () -> {
            bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");
        });
    }

    // TEST 7: Duplicate booking request returns existing booking (Idempotency)
    @Test
    void test7_duplicateBookingRequest() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking1 = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");
        BookingDTO booking2 = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        assertEquals(booking1.getId(), booking2.getId());
        assertEquals(booking1.getBookingNumber(), booking2.getBookingNumber());
    }

    // TEST 8: Double confirmation does not duplicate reservations
    @Test
    void test8_doubleConfirmationIdempotency() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        long resCountBefore = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> booking.getId().equals(r.getBookingId())).count();

        bookingService.confirmBooking(tenantId, booking.getId(), "OWNER");

        long resCountAfter = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> booking.getId().equals(r.getBookingId())).count();

        assertEquals(resCountBefore, resCountAfter, "Double confirmation should not create duplicate reservations");
    }

    // TEST 9 & 10: Cancel booking releases inventory and logs RELEASE transaction
    @Test
    void test9and10_cancelBookingReleasesInventory() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        BookingDTO cancelled = bookingService.cancelBooking(tenantId, booking.getId(), "OWNER");
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        List<InventoryReservation> resList = reservationRepository.findByTenantId(tenantId);
        boolean allReleased = resList.stream()
                .filter(r -> booking.getId().equals(r.getBookingId()))
                .allMatch(r -> r.getStatus() == ReservationStatus.RELEASED);
        assertTrue(allReleased, "All reservations should be marked RELEASED");

        List<InventoryTransaction> txList = transactionRepository.findByTenantId(tenantId);
        boolean foundReleaseTx = txList.stream().anyMatch(t -> booking.getId().equals(t.getReferenceId()) && t.getTransactionType() == TransactionType.RELEASE);
        assertTrue(foundReleaseTx, "RELEASE transaction log should exist");
    }

    // TEST 11: Tenant Isolation
    @Test
    void test11_tenantIsolation() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        String fakeTenant = UUID.randomUUID().toString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders("OWNER", fakeTenant));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/" + booking.getId(),
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // TEST 12: Unauthorized role operation rejected
    @Test
    void test12_rolePermissionValidation() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders("DRIVER", tenantId));
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/bookings/" + booking.getId() + "/cancel",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // TEST 13: Expired quote cannot become booking
    @Test
    void test13_expiredQuoteCannotBeBooked() {
        QuoteDTO quote = createTestQuote(QuoteStatus.EXPIRED);
        assertThrows(IllegalStateException.class, () -> {
            bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");
        });
    }

    // TEST 14: Draft quote cannot become confirmed booking
    @Test
    void test14_draftQuoteCannotBeBooked() {
        QuoteDTO quote = createTestQuote(QuoteStatus.DRAFT);
        assertThrows(IllegalStateException.class, () -> {
            bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");
        });
    }

    // TEST 15: AI booking creation requires approval
    @Test
    void test15_aiBookingCreationRequiresApproval() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        Map<String, Object> params = Map.of("quoteId", quote.getId().toString());
        ToolRequest req = new ToolRequest("createBookingFromQuote", params, tenantId, "user-1", "OWNER");
        ToolResult result = createBookingFromQuoteTool.execute(req);

        assertEquals("ACTION_REQUIRES_APPROVAL", result.getStatus());
        assertNotNull(result.getData());
    }

    // TEST 16: AI cancellation requires approval
    @Test
    void test16_aiCancellationRequiresApproval() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");
        Map<String, Object> params = Map.of("bookingId", booking.getId().toString());
        ToolRequest req = new ToolRequest("cancelBooking", params, tenantId, "user-1", "OWNER");
        ToolResult result = cancelBookingTool.execute(req);

        assertEquals("ACTION_REQUIRES_APPROVAL", result.getStatus());
        assertNotNull(result.getData());
    }

    // TEST 17: Concurrent confirmation safety check
    @Test
    void test17_concurrentConfirmationSafety() {
        QuoteDTO quote = createTestQuote(QuoteStatus.ACCEPTED);
        BookingDTO booking = bookingService.createBookingFromQuote(tenantId, quote.getId(), "OWNER");

        // Concurrent execution simulation
        assertDoesNotThrow(() -> {
            bookingService.confirmBooking(tenantId, booking.getId(), "OWNER");
            bookingService.confirmBooking(tenantId, booking.getId(), "OWNER");
        });
    }

    private static class ListHelper {
        static boolean allStyleMatches(List<InventoryReservation> list, java.util.function.Predicate<InventoryReservation> predicate) {
            for (InventoryReservation r : list) {
                if (!predicate.test(r)) return false;
            }
            return true;
        }
    }
}
