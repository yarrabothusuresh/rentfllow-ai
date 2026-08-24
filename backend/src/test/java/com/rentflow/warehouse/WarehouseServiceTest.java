package com.rentflow.warehouse;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.warehouse.dto.*;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import com.rentflow.warehouse.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseOrderRepository orderRepository;

    @Autowired
    private WarehouseOrderItemRepository itemRepository;

    @Autowired
    private WarehouseAuditRepository auditRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    private String tenantA = "tenant-test-a";
    private String tenantB = "tenant-test-b";
    private UUID testBookingId;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        // Create Test Product
        Product product = new Product(
                UUID.randomUUID(), tenantA, "TEST-SKU-01", "Test Chiavari Chair",
                "Test description", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("10.00"), new BigDecimal("100.00"), 200, 0, 0, 0
        );
        productRepository.save(product);
        testProductId = product.getId();

        // Create Test Booking
        Booking booking = new Booking();
        booking.setTenantId(tenantA);
        booking.setBookingNumber("TEST-BKG-001");
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        booking.setSubtotal(new BigDecimal("500.00"));
        booking.setTotalAmount(new BigDecimal("500.00"));
        Booking savedBooking = bookingRepository.save(booking);
        testBookingId = savedBooking.getId();

        // Create Booking Item
        BookingItem item = new BookingItem();
        item.setBookingId(testBookingId);
        item.setProductId(testProductId);
        item.setDescription("Test Chiavari Chair");
        item.setQuantity(50);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setRentalStartDateTime(booking.getRentalStartDateTime());
        item.setRentalEndDateTime(booking.getRentalEndDateTime());
        bookingItemRepository.save(item);

        // Create Inventory Reservation
        InventoryReservation res = new InventoryReservation(
                UUID.randomUUID(), tenantA, testProductId, booking.getEventId(), testBookingId,
                50, booking.getRentalStartDateTime(), booking.getRentalEndDateTime(), ReservationStatus.RESERVED
        );
        reservationRepository.save(res);
    }

    @Test
    @DisplayName("1 & 3-5. Create Warehouse Order from Booking with snapshots")
    void testCreateWarehouseOrderFromBooking() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");

        assertNotNull(order);
        assertNotNull(order.getId());
        assertTrue(order.getOrderNumber().startsWith("WH-"));
        assertEquals(WarehouseOrderStatus.READY_TO_PICK, order.getStatus());
        assertEquals(1, order.getItems().size());

        WarehouseOrderItemDTO item = order.getItems().get(0);
        assertEquals("Test Chiavari Chair", item.getProductNameSnapshot());
        assertEquals("TEST-SKU-01", item.getSkuSnapshot());
        assertNotNull(item.getLocationSnapshot());
        assertEquals(50, item.getQuantityRequired());
        assertEquals(0, item.getQuantityPicked());

        // Test Audit Event Generated
        List<WarehouseAudit> audits = auditRepository.findByTenantIdAndWarehouseOrderIdOrderByTimestampDesc(tenantA, order.getId());
        assertFalse(audits.isEmpty());
        assertEquals("WAREHOUSE_ORDER_CREATED", audits.get(0).getAction());
    }

    @Test
    @DisplayName("2. Prevent Duplicate Warehouse Orders")
    void testPreventDuplicateWarehouseOrders() {
        warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");
        });
        assertTrue(exception.getMessage().contains("Warehouse order already exists"));
    }

    @Test
    @DisplayName("6 & 7 & 8. Start Picking, Pick Quantity, Partial Pick")
    void testPickingWorkflow() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");
        WarehouseOrderItemDTO item = order.getItems().get(0);

        // Start picking
        WarehouseOrderDTO pickingOrder = warehouseService.startPicking(tenantA, order.getId(), "user-123", "WAREHOUSE_OPERATOR");
        assertEquals(WarehouseOrderStatus.PICKING, pickingOrder.getStatus());
        assertEquals("user-123", pickingOrder.getAssignedTo());

        // Pick partial quantity (20 of 50)
        PickItemRequestDTO pickReq1 = new PickItemRequestDTO();
        pickReq1.setQuantity(20);
        WarehouseOrderDTO updatedOrder1 = warehouseService.pickItem(tenantA, order.getId(), item.getId(), 20, "Picked 20", false, "WAREHOUSE_OPERATOR");

        WarehouseOrderItemDTO updatedItem1 = updatedOrder1.getItems().get(0);
        assertEquals(20, updatedItem1.getQuantityPicked());
        assertEquals(WarehouseOrderItemStatus.PARTIALLY_PICKED, updatedItem1.getStatus());

        // Pick remaining quantity (30 more)
        WarehouseOrderDTO updatedOrder2 = warehouseService.pickItem(tenantA, order.getId(), item.getId(), 30, "Picked remaining", false, "WAREHOUSE_OPERATOR");
        WarehouseOrderItemDTO updatedItem2 = updatedOrder2.getItems().get(0);
        assertEquals(50, updatedItem2.getQuantityPicked());
        assertEquals(WarehouseOrderItemStatus.PICKED, updatedItem2.getStatus());
    }

    @Test
    @DisplayName("9. Pick Quantity Cannot Exceed Required")
    void testPickQuantityCannotExceedRequired() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");
        WarehouseOrderItemDTO item = order.getItems().get(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.pickItem(tenantA, order.getId(), item.getId(), 60, "Excess", false, "WAREHOUSE_OPERATOR");
        });
        assertTrue(exception.getMessage().contains("cannot exceed required quantity"));
    }

    @Test
    @DisplayName("10 & 11 & 12. Shortage Recording & Picking Completion Confirmation")
    void testShortageAndPickingCompletion() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");
        WarehouseOrderItemDTO item = order.getItems().get(0);

        // Pick 45 out of 50 and report shortage of 5
        warehouseService.pickItem(tenantA, order.getId(), item.getId(), 45, "5 chairs missing in rack", true, "WAREHOUSE_OPERATOR");

        // Complete picking without confirmation -> Should fail
        assertThrows(IllegalStateException.class, () -> {
            warehouseService.completePicking(tenantA, order.getId(), false, "WAREHOUSE_MANAGER");
        });

        // Complete picking with confirmation -> Should succeed
        WarehouseOrderDTO completed = warehouseService.completePicking(tenantA, order.getId(), true, "WAREHOUSE_MANAGER");
        assertEquals(WarehouseOrderStatus.PICKED, completed.getStatus());
    }

    @Test
    @DisplayName("13 & 14 & 15 & 16. Packing Workflow & Ready for Delivery Status")
    void testPackingWorkflow() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");
        WarehouseOrderItemDTO item = order.getItems().get(0);

        // Pick full 50
        warehouseService.pickItem(tenantA, order.getId(), item.getId(), 50, "Full pick", false, "WAREHOUSE_OPERATOR");
        warehouseService.completePicking(tenantA, order.getId(), false, "WAREHOUSE_MANAGER");

        // Start packing
        WarehouseOrderDTO packingOrder = warehouseService.startPacking(tenantA, order.getId(), "WAREHOUSE_OPERATOR");
        assertEquals(WarehouseOrderStatus.PACKING, packingOrder.getStatus());

        // Cannot pack more than picked
        assertThrows(IllegalStateException.class, () -> {
            warehouseService.packItem(tenantA, order.getId(), item.getId(), 60, "Excess pack", "WAREHOUSE_OPERATOR");
        });

        // Pack 50
        warehouseService.packItem(tenantA, order.getId(), item.getId(), 50, "Packed all", "WAREHOUSE_OPERATOR");

        // Complete packing -> Transitions to READY_FOR_DELIVERY
        WarehouseOrderDTO readyOrder = warehouseService.completePacking(tenantA, order.getId(), false, "WAREHOUSE_MANAGER");
        assertEquals(WarehouseOrderStatus.READY_FOR_DELIVERY, readyOrder.getStatus());
        assertEquals("READY_FOR_DELIVERY", readyOrder.getCustomerFacingStatus());
        assertNotNull(readyOrder.getCompletedAt());
    }

    @Test
    @DisplayName("17. Warehouse Order Assignment")
    void testWarehouseOrderAssignment() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");

        WarehouseOrderDTO assigned = warehouseService.assignOrder(tenantA, order.getId(), "staff-john", "WAREHOUSE_MANAGER");
        assertEquals("staff-john", assigned.getAssignedTo());
    }

    @Test
    @DisplayName("18. Unauthorized Role Restriction")
    void testUnauthorizedRoleRestriction() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");

        assertThrows(SecurityException.class, () -> {
            warehouseService.startPicking(tenantA, order.getId(), "cust-1", "CUSTOMER");
        });
        assertThrows(SecurityException.class, () -> {
            warehouseService.startPicking(tenantA, order.getId(), "sales-1", "SALES");
        });
    }

    @Test
    @DisplayName("19. Cross-Tenant Isolation Enforced")
    void testCrossTenantIsolation() {
        WarehouseOrderDTO order = warehouseService.createOrderFromBooking(tenantA, testBookingId, "OWNER");

        // Tenant B attempting to access Tenant A's order -> throws IllegalArgumentException (Not Found)
        assertThrows(IllegalArgumentException.class, () -> {
            warehouseService.startPicking(tenantB, order.getId(), "user-b", "OWNER");
        });
    }

    @Test
    @DisplayName("22. Reservation Requirement Enforced")
    void testReservationRequirementEnforced() {
        // Create booking without reservation
        Booking unreservedBooking = new Booking();
        unreservedBooking.setTenantId(tenantA);
        unreservedBooking.setBookingNumber("TEST-BKG-UNRESERVED");
        unreservedBooking.setQuoteId(UUID.randomUUID());
        unreservedBooking.setCustomerId(UUID.randomUUID());
        unreservedBooking.setEventId(UUID.randomUUID());
        unreservedBooking.setStatus(BookingStatus.PENDING); // Not confirmed, no reservation
        unreservedBooking.setBookingDate(LocalDate.now());
        unreservedBooking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        unreservedBooking.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        Booking saved = bookingRepository.save(unreservedBooking);

        BookingItem item = new BookingItem();
        item.setBookingId(saved.getId());
        item.setProductId(testProductId);
        item.setDescription("Item");
        item.setQuantity(10);
        item.setRentalStartDateTime(saved.getRentalStartDateTime());
        item.setRentalEndDateTime(saved.getRentalEndDateTime());
        bookingItemRepository.save(item);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            warehouseService.createOrderFromBooking(tenantA, saved.getId(), "OWNER");
        });
        assertTrue(exception.getMessage().contains("Inventory reservation is required"));
    }
}
