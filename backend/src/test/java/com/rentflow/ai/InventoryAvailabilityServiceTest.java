package com.rentflow.ai;

import com.rentflow.BackendApplication;
import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.ai.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
@Transactional
public class InventoryAvailabilityServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private InventoryService inventoryService;

    private String tenantA = "tenant-a-test";
    private String tenantB = "tenant-b-test";
    private UUID testProductId;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    public void setUp() {
        start = LocalDateTime.of(2026, 9, 1, 10, 0);
        end = LocalDateTime.of(2026, 9, 3, 18, 0);

        Product p = new Product();
        p.setTenantId(tenantA);
        p.setSku("TEST-CHAIR");
        p.setName("Test Folding Chair");
        p.setQuantityOwned(100);
        p.setQuantityInMaintenance(10);
        p.setQuantityDamaged(5);
        p.setQuantityLost(5);
        p.setRentalPrice(new BigDecimal("5.00"));
        Product saved = productRepository.save(p);
        testProductId = saved.getId();
    }

    @Test
    public void test1_AvailabilityCalculationAccuracy() {
        // Base Available = 100 - 10 - 5 - 5 = 80
        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantA, testProductId, 10, start, end);
        assertEquals(100, res.getQuantityOwned());
        assertEquals(10, res.getQuantityInMaintenance());
        assertEquals(5, res.getQuantityDamaged());
        assertEquals(5, res.getQuantityLost());
        assertEquals(0, res.getQuantityReserved());
        assertEquals(80, res.getAvailableQuantity());
        assertTrue(res.isAvailable());
    }

    @Test
    public void test2_TotalVsReservedVsAvailableMath() {
        // Create 30 reserved units
        InventoryReservation r = new InventoryReservation();
        r.setTenantId(tenantA);
        r.setProductId(testProductId);
        r.setQuantity(30);
        r.setStartDateTime(start);
        r.setEndDateTime(end);
        r.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(r);

        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantA, testProductId, 10, start, end);
        assertEquals(30, res.getQuantityReserved());
        assertEquals(50, res.getAvailableQuantity()); // 80 - 30 = 50
    }

    @Test
    public void test3_DateOverlapLogic() {
        // Reservation: Sep 1 10:00 -> Sep 3 18:00
        InventoryReservation r = new InventoryReservation();
        r.setTenantId(tenantA);
        r.setProductId(testProductId);
        r.setQuantity(40);
        r.setStartDateTime(start);
        r.setEndDateTime(end);
        r.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(r);

        // Query overlapping period: Sep 2 10:00 -> Sep 4 10:00
        LocalDateTime overlapStart = LocalDateTime.of(2026, 9, 2, 10, 0);
        LocalDateTime overlapEnd = LocalDateTime.of(2026, 9, 4, 10, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantA, testProductId, 10, overlapStart, overlapEnd);
        assertEquals(40, res.getQuantityReserved());
        assertEquals(40, res.getAvailableQuantity()); // 80 - 40 = 40
    }

    @Test
    public void test4_NonOverlappingReservationsIndependent() {
        // Reservation: Sep 1 -> Sep 3
        InventoryReservation r = new InventoryReservation();
        r.setTenantId(tenantA);
        r.setProductId(testProductId);
        r.setQuantity(40);
        r.setStartDateTime(start);
        r.setEndDateTime(end);
        r.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(r);

        // Non-overlapping query: Sep 5 -> Sep 7
        LocalDateTime futureStart = LocalDateTime.of(2026, 9, 5, 10, 0);
        LocalDateTime futureEnd = LocalDateTime.of(2026, 9, 7, 10, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantA, testProductId, 10, futureStart, futureEnd);
        assertEquals(0, res.getQuantityReserved());
        assertEquals(80, res.getAvailableQuantity());
    }

    @Test
    public void test5_SuccessfulReservationWhenEnoughExists() {
        InventoryReservationDTO dto = new InventoryReservationDTO();
        dto.setProductId(testProductId);
        dto.setQuantity(50);
        dto.setStartDateTime(start);
        dto.setEndDateTime(end);

        InventoryReservationDTO created = inventoryService.createReservation(tenantA, dto, "Sales Admin");
        assertNotNull(created.getId());
        assertEquals(ReservationStatus.RESERVED, created.getStatus());
    }

    @Test
    public void test6_OverbookingRejectionWithUsefulBusinessError() {
        // Available is 80. Requesting 90 should throw BAD_REQUEST exception
        InventoryReservationDTO dto = new InventoryReservationDTO();
        dto.setProductId(testProductId);
        dto.setQuantity(90);
        dto.setStartDateTime(start);
        dto.setEndDateTime(end);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            inventoryService.createReservation(tenantA, dto, "Sales Admin");
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Only 80 units are available"));
    }

    @Test
    public void test7_ManualReservationReleaseRestoresAvailability() {
        InventoryReservationDTO dto = new InventoryReservationDTO();
        dto.setProductId(testProductId);
        dto.setQuantity(50);
        dto.setStartDateTime(start);
        dto.setEndDateTime(end);

        InventoryReservationDTO created = inventoryService.createReservation(tenantA, dto, "Sales Admin");
        AvailabilityResultDTO res1 = availabilityService.checkAvailability(tenantA, testProductId, 10, start, end);
        assertEquals(30, res1.getAvailableQuantity()); // 80 - 50 = 30

        // Release reservation
        inventoryService.releaseReservation(tenantA, created.getId(), "Sales Admin");
        AvailabilityResultDTO res2 = availabilityService.checkAvailability(tenantA, testProductId, 10, start, end);
        assertEquals(80, res2.getAvailableQuantity()); // Restored to 80
    }

    @Test
    public void test8_TenantIsolationEnforcement() {
        // Tenant A has product with 80 available
        AvailabilityResultDTO resA = availabilityService.checkAvailability(tenantA, testProductId, 10, start, end);
        assertEquals(80, resA.getAvailableQuantity());

        // Tenant B query for Tenant A product ID should throw NOT_FOUND
        assertThrows(ResponseStatusException.class, () -> {
            availabilityService.checkAvailability(tenantB, testProductId, 10, start, end);
        });
    }

    @Test
    public void test9_BulkAvailabilityCheck() {
        BulkAvailabilityRequestDTO req = new BulkAvailabilityRequestDTO();
        req.setStartDateTime(start);
        req.setEndDateTime(end);
        req.setItems(List.of(new BulkAvailabilityRequestDTO.ItemRequest(testProductId, 50)));

        BulkAvailabilityResultDTO res = availabilityService.checkBulkAvailability(tenantA, req);
        assertTrue(res.isAvailable());
        assertEquals(1, res.getItems().size());
        assertEquals(80, res.getItems().get(0).getAvailableQuantity());
    }

    @Test
    public void test10_ConflictDetection() {
        // Create overbooking condition
        InventoryReservation r = new InventoryReservation();
        r.setTenantId(tenantA);
        r.setProductId(testProductId);
        r.setQuantity(120); // Exceeds 80 available
        r.setStartDateTime(LocalDateTime.now());
        r.setEndDateTime(LocalDateTime.now().plusDays(2));
        r.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(r);

        List<InventoryConflictDTO> conflicts = inventoryService.getConflicts(tenantA);
        assertFalse(conflicts.isEmpty());
        assertEquals(testProductId, conflicts.get(0).getProductId());
    }
}
