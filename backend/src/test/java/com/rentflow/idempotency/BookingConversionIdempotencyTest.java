package com.rentflow.idempotency;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingConversionIdempotencyTest {

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
    private Quote quote;

    @BeforeEach
    public void setUp() {
        tenantId = "tenant-bkg-" + UUID.randomUUID();

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUS-" + UUID.randomUUID().toString().substring(0, 6));
        customer.setFirstName("Clark");
        customer.setLastName("Kent");
        customer.setEmail("clark@dailyplanet.demo");
        customer = customerRepository.save(customer);

        event = new Event();
        event.setTenantId(tenantId);
        event.setEventName("Metropolis Press Gala");
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setCustomerId(customer.getId());
        event = eventRepository.save(event);

        product = new Product();
        product.setTenantId(tenantId);
        product.setName("Grand Ballroom Chair");
        product.setSku("CHAIR-GB-01");
        product.setQuantityOwned(20);
        product.setRentalPrice(new BigDecimal("10.00"));
        product = productRepository.save(product);

        // Create accepted quote requesting 10 chairs
        QuoteDTO qDto = new QuoteDTO();
        qDto.setTenantId(tenantId);
        qDto.setCustomerId(customer.getId());
        qDto.setEventId(event.getId());
        qDto.setStatus(QuoteStatus.SENT);
        qDto.setRentalStartDateTime(LocalDateTime.now().plusDays(5));
        qDto.setRentalEndDateTime(LocalDateTime.now().plusDays(7));

        QuoteItemDTO item = new QuoteItemDTO();
        item.setProductId(product.getId());
        item.setQuantity(10);
        item.setRentalDays(2);
        item.setUnitPrice(product.getRentalPrice());
        item.setStandardUnitPrice(product.getRentalPrice());
        item.setLineTotal(new BigDecimal("200.00"));
        qDto.setItems(List.of(item));

        QuoteDTO createdQ = quoteService.createQuote(tenantId, qDto, "SALES");
        quote = quoteRepository.findById(createdQ.getId()).orElseThrow();
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote = quoteRepository.save(quote);
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

    @Test
    @DisplayName("45. Booking Replay: Converting quote 5 times yields same booking & reserves inventory once")
    public void testConvertQuoteFiveTimes_ExactlyOneBookingAndReservation() {
        BookingDTO first = bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
        assertNotNull(first.getId());

        for (int i = 0; i < 4; i++) {
            BookingDTO replay = bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
            assertEquals(first.getId(), replay.getId(), "Replay must return the same original booking");
            assertEquals(first.getBookingNumber(), replay.getBookingNumber());
        }

        // Verify Database
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        assertEquals(1, bookings.size(), "Only 1 booking must exist for the quote");

        List<InventoryReservation> reservations = reservationRepository.findByTenantIdAndBookingId(tenantId, first.getId());
        assertEquals(1, reservations.size(), "Exactly 1 reservation record must exist");
        assertEquals(10, reservations.get(0).getQuantity(), "Reserved quantity must be 10, not multiplied by 5");
    }

    @Test
    @DisplayName("25. Booking Cancellation Edge Case: Reconversion of a quote whose booking was cancelled is rejected")
    public void testReconversionAfterCancellation_IsRejected() {
        BookingDTO bkg = bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
        bookingService.cancelBooking(tenantId, bkg.getId(), "OWNER");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.createBookingFromQuote(tenantId, quote.getId(), "SALES");
        });

        assertTrue(ex.getMessage().contains("cancelled"), "Expected rejection mentioning cancellation");
    }
}
