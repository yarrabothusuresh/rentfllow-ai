package com.rentflow.calendar;

import com.rentflow.ai.dto.AlternativeSuggestionsDTO;
import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.model.InventoryReservation;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.model.ProductType;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class AvailabilityServiceTest {

    private ProductRepository productRepository;
    private InventoryReservationRepository reservationRepository;
    private EventRepository eventRepository;
    private AvailabilityService availabilityService;

    private String tenantId = "tenant-dev";
    private UUID productId = UUID.randomUUID();
    private Product product;

    @BeforeEach
    public void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        reservationRepository = Mockito.mock(InventoryReservationRepository.class);
        eventRepository = Mockito.mock(EventRepository.class);

        availabilityService = new AvailabilityService(productRepository, reservationRepository, eventRepository);

        product = new Product(
                productId, tenantId, "CHAIR-001", "Chiavary Chair", "Gold Chair", null,
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("12.50"), new BigDecimal("85.00"),
                500, 0, 0, 0
        );
        product.setDefaultTurnaroundMinutes(60);

        when(productRepository.findByTenantIdAndId(tenantId, productId)).thenReturn(Optional.of(product));
    }

    @Test
    public void testCheckAvailabilitySuccess() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 12, 17, 0);

        when(reservationRepository.findOverlappingReservations(eq(tenantId), eq(productId), any(), any()))
                .thenReturn(List.of());

        AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, 200, start, end);

        assertTrue(result.isAvailable());
        assertEquals(500, result.getAvailableQuantity());
        assertEquals(0, result.getShortage());
    }

    @Test
    public void testInventoryHardConflict() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 12, 17, 0);

        InventoryReservation existingRes = new InventoryReservation();
        existingRes.setQuantity(400);
        existingRes.setBookingId(UUID.randomUUID());

        when(reservationRepository.findOverlappingReservations(eq(tenantId), eq(productId), any(), any()))
                .thenReturn(List.of(existingRes));

        AvailabilityResultDTO result = availabilityService.checkAvailability(tenantId, productId, 200, start, end);

        assertFalse(result.isAvailable());
        assertEquals(100, result.getAvailableQuantity());
        assertEquals(100, result.getShortage());
    }

    @Test
    public void testCheckAvailabilityExcludingBooking() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 12, 17, 0);
        UUID currentBookingId = UUID.randomUUID();

        when(reservationRepository.findOverlappingReservationsExcludingBooking(eq(tenantId), eq(productId), any(), any(), eq(currentBookingId)))
                .thenReturn(List.of());

        AvailabilityResultDTO result = availabilityService.checkAvailabilityExcludingBooking(
                tenantId, productId, 200, start, end, currentBookingId);

        assertTrue(result.isAvailable());
        assertEquals(500, result.getAvailableQuantity());
    }

    @Test
    public void testAlternativeSuggestions() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 12, 17, 0);

        when(reservationRepository.findOverlappingReservations(eq(tenantId), eq(productId), any(), any()))
                .thenReturn(List.of());
        when(productRepository.findByTenantId(tenantId)).thenReturn(List.of(product));

        AlternativeSuggestionsDTO alternatives = availabilityService.getAlternativeSuggestions(tenantId, productId, 100, start, end);

        assertNotNull(alternatives);
        assertEquals(productId, alternatives.getProductId());
        assertNotNull(alternatives.getAlternativeDates());
    }
}
