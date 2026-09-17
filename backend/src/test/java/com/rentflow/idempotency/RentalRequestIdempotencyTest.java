package com.rentflow.idempotency;

import com.rentflow.payment.exception.IdempotencyConflictException;
import com.rentflow.rentalrequest.dto.RentalRequestDTO;
import com.rentflow.rentalrequest.model.RentalRequest;
import com.rentflow.rentalrequest.repository.RentalRequestRepository;
import com.rentflow.rentalrequest.service.RentalRequestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalRequestIdempotencyTest {

    @Autowired
    private RentalRequestService rentalRequestService;

    @Autowired
    private RentalRequestRepository rentalRequestRepository;

    private String tenantA;
    private String tenantB;

    @BeforeEach
    public void setUp() {
        tenantA = "tenant-a-" + UUID.randomUUID();
        tenantB = "tenant-b-" + UUID.randomUUID();
    }

    @AfterEach
    public void tearDown() {
        if (tenantA != null) {
            rentalRequestRepository.deleteAll(rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA));
        }
        if (tenantB != null) {
            rentalRequestRepository.deleteAll(rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantB));
        }
    }

    private RentalRequestDTO createSampleDTO(String key, int quantity, BigDecimal unitPrice) {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setIdempotencyKey(key);
        dto.setCustomerName("Alice Wonderland");
        dto.setCustomerEmail("alice@example.com");
        dto.setCustomerPhone("555-123-4567");
        dto.setEventName("Corporate Summit");
        dto.setRentalStartDate(LocalDateTime.of(2026, 10, 10, 9, 0));
        dto.setRentalEndDate(LocalDateTime.of(2026, 10, 12, 18, 0));
        dto.setEstimatedTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));

        RentalRequestDTO.ItemDTO item = new RentalRequestDTO.ItemDTO();
        item.setProductId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        item.setProductName("Executive Banquet Chair");
        item.setSku("CHAIR-EXEC-01");
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setLineTotal(dto.getEstimatedTotal());
        dto.setItems(List.of(item));

        return dto;
    }

    @Test
    @DisplayName("39. Sequential Replay: Submitting identical rental request 5 times creates 1 record & returns cached result")
    public void testSequentialReplay_ReturnsSameResource() {
        String key = UUID.randomUUID().toString();
        RentalRequestDTO dto = createSampleDTO(key, 10, new BigDecimal("15.00"));

        RentalRequestDTO firstResult = rentalRequestService.createRentalRequest(tenantA, dto);
        assertNotNull(firstResult.getId());
        assertFalse(firstResult.isIdempotentReplay());

        for (int i = 0; i < 4; i++) {
            RentalRequestDTO replayDTO = createSampleDTO(key, 10, new BigDecimal("15.00"));
            RentalRequestDTO replayResult = rentalRequestService.createRentalRequest(tenantA, replayDTO);

            assertEquals(firstResult.getId(), replayResult.getId());
            assertEquals(firstResult.getRequestNumber(), replayResult.getRequestNumber());
            assertTrue(replayResult.isIdempotentReplay());
        }

        List<RentalRequest> records = rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA);
        assertEquals(1, records.size());
    }

    @Test
    @DisplayName("40. Concurrent Checkout: 10 concurrent threads with same key create exactly 1 rental request")
    public void testConcurrentCheckout_ExactlyOneCreated() throws Exception {
        String key = UUID.randomUUID().toString();
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        AtomicInteger successes = new AtomicInteger(0);
        ConcurrentLinkedQueue<UUID> returnedIds = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    RentalRequestDTO dto = createSampleDTO(key, 10, new BigDecimal("15.00"));
                    RentalRequestDTO res = rentalRequestService.createRentalRequest(tenantA, dto);
                    if (res != null && res.getId() != null) {
                        returnedIds.add(res.getId());
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

        assertEquals(threads, successes.get(), "All threads should succeed (either via initial insert or idempotent replay)");
        UUID firstId = returnedIds.peek();
        for (UUID id : returnedIds) {
            assertEquals(firstId, id, "All returned IDs must match the original record ID");
        }

        List<RentalRequest> records = rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA);
        assertEquals(1, records.size(), "Database must contain exactly 1 rental request");
    }

    @Test
    @DisplayName("41. Fingerprint Conflict: Same key with different payload throws IdempotencyConflictException (409)")
    public void testSameKeyDifferentPayload_ThrowsConflict() {
        String key = UUID.randomUUID().toString();
        RentalRequestDTO dto1 = createSampleDTO(key, 10, new BigDecimal("15.00"));
        rentalRequestService.createRentalRequest(tenantA, dto1);

        RentalRequestDTO dto2 = createSampleDTO(key, 25, new BigDecimal("15.00")); // Different quantity & total
        assertThrows(IdempotencyConflictException.class, () -> {
            rentalRequestService.createRentalRequest(tenantA, dto2);
        });

        List<RentalRequest> records = rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA);
        assertEquals(1, records.size());
    }

    @Test
    @DisplayName("42. Different Keys: Legitimate separate orders succeed with distinct keys")
    public void testDifferentKeys_CreateDistinctRecords() {
        String key1 = UUID.randomUUID().toString();
        String key2 = UUID.randomUUID().toString();

        RentalRequestDTO res1 = rentalRequestService.createRentalRequest(tenantA, createSampleDTO(key1, 10, new BigDecimal("15.00")));
        RentalRequestDTO res2 = rentalRequestService.createRentalRequest(tenantA, createSampleDTO(key2, 10, new BigDecimal("15.00")));

        assertNotEquals(res1.getId(), res2.getId());
        assertNotEquals(res1.getRequestNumber(), res2.getRequestNumber());

        List<RentalRequest> records = rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA);
        assertEquals(2, records.size());
    }

    @Test
    @DisplayName("36. Tenant Isolation: Same idempotency key in different tenants are completely isolated")
    public void testTenantIsolation_SameKeyDifferentTenants() {
        String key = "shared-idempotency-key-001";

        RentalRequestDTO resA = rentalRequestService.createRentalRequest(tenantA, createSampleDTO(key, 10, new BigDecimal("15.00")));
        RentalRequestDTO resB = rentalRequestService.createRentalRequest(tenantB, createSampleDTO(key, 10, new BigDecimal("15.00")));

        assertNotEquals(resA.getId(), resB.getId());
        assertEquals(tenantA, resA.getTenantId());
        assertEquals(tenantB, resB.getTenantId());

        assertEquals(1, rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantA).size());
        assertEquals(1, rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantB).size());
    }
}
