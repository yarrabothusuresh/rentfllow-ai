package com.rentflow.warehouse.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class WarehouseDataInitializer implements CommandLineRunner {

    private final WarehouseLocationRepository locationRepository;
    private final WarehouseOrderRepository orderRepository;
    private final WarehouseOrderItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final WarehouseAuditRepository auditRepository;

    public static final UUID DEMO_BOOKING_ID = UUID.fromString("b0000000-0000-0000-0000-000000000123");
    public static final UUID DEMO_SHORTAGE_BOOKING_ID = UUID.fromString("b0000000-0000-0000-0000-000000000124");

    public WarehouseDataInitializer(WarehouseLocationRepository locationRepository,
                                    WarehouseOrderRepository orderRepository,
                                    WarehouseOrderItemRepository itemRepository,
                                    CustomerRepository customerRepository,
                                    EventRepository eventRepository,
                                    BookingRepository bookingRepository,
                                    BookingItemRepository bookingItemRepository,
                                    InventoryReservationRepository reservationRepository,
                                    ProductRepository productRepository,
                                    WarehouseAuditRepository auditRepository) {
        this.locationRepository = locationRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.reservationRepository = reservationRepository;
        this.productRepository = productRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        // 1. Seed Locations
        if (locationRepository.count() == 0) {
            WarehouseLocation loc1 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000001"), tenantId, "A-01-01", "Aisle A, Rack 01, Shelf 01", "Chiavari Seating Bay", true);
            WarehouseLocation loc2 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000002"), tenantId, "A-01-02", "Aisle A, Rack 01, Shelf 02", "Folding Seating Bay", true);
            WarehouseLocation loc3 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000003"), tenantId, "A-01-03", "Aisle A, Rack 01, Shelf 03", "Premium Chair Staging", true);
            WarehouseLocation loc4 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000004"), tenantId, "B-01-01", "Aisle B, Rack 01, Shelf 01", "Round Banquet Tables", true);
            WarehouseLocation loc5 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000005"), tenantId, "B-02-01", "Aisle B, Rack 02, Shelf 01", "Cocktail & Bistro Tables", true);
            WarehouseLocation loc6 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000006"), tenantId, "C-01-01", "Aisle C, Rack 01, Shelf 01", "Linen & Decor Staging", true);

            locationRepository.saveAll(List.of(loc1, loc2, loc3, loc4, loc5, loc6));
            System.out.println("✅ Seeded Day 15 Warehouse Locations.");
        }

        if (orderRepository.count() > 0) {
            return;
        }

        // 2. Seed Demo Customer: ABC Events LLC
        UUID customerId = UUID.fromString("c3333333-3333-3333-3333-333333333333");
        if (customerRepository.findById(customerId).isEmpty()) {
            Customer c = new Customer();
            c.setId(customerId);
            c.setTenantId(tenantId);
            c.setCustomerNumber("CUS-000123");
            c.setFirstName("ABC Events");
            c.setLastName("LLC");
            c.setCompanyName("ABC Events LLC");
            c.setEmail("contact@abcevents-demo.com");
            c.setPhone("+1 555-019-9988");
            c.setCustomerType(CustomerType.CORPORATE);
            c.setStatus(CustomerStatus.ACTIVE);
            customerRepository.save(c);
        }

        // 3. Seed Demo Event: Wedding Reception (Aug 30, 2026)
        UUID eventId = UUID.fromString("e4444444-4444-4444-4444-444444444444");
        if (eventRepository.findById(eventId).isEmpty()) {
            Event e = new Event();
            e.setId(eventId);
            e.setTenantId(tenantId);
            e.setCustomerId(customerId);
            e.setEventName("Wedding Reception");
            e.setEventType(EventType.WEDDING);
            e.setEventDate(LocalDate.of(2026, 8, 30));
            e.setStartTime("10:00");
            e.setEndTime("22:00");
            e.setGuestCount(100);
            e.setVenueName("Grand Ballroom");
            e.setVenueAddress("500 Celebration Blvd");
            e.setCity("Dallas");
            e.setState("TX");
            e.setZipCode("75202");
            e.setStatus(EventStatus.BOOKED);
            eventRepository.save(e);
        }

        // 4. Find products or fallback
        List<Product> products = productRepository.findByTenantId(tenantId);
        Product chiavari = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("CHI")).findFirst().orElse(null);
        Product roundTable = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("TBL")).findFirst().orElse(null);
        Product linen = products.stream().filter(p -> p.getSku() != null && p.getSku().contains("LIN")).findFirst().orElse(null);

        UUID chiavariId = chiavari != null ? chiavari.getId() : UUID.fromString("00000000-0000-0000-0000-000000000091");
        UUID roundTableId = roundTable != null ? roundTable.getId() : UUID.fromString("00000000-0000-0000-0000-000000000092");
        UUID linenId = linen != null ? linen.getId() : UUID.fromString("00000000-0000-0000-0000-000000000093");

        // 5. Seed Demo Booking BOOK-000123
        Booking b1 = new Booking();
        b1.setId(DEMO_BOOKING_ID);
        b1.setTenantId(tenantId);
        b1.setBookingNumber("BOOK-000123");
        b1.setQuoteId(UUID.randomUUID());
        b1.setCustomerId(customerId);
        b1.setEventId(eventId);
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setBookingDate(LocalDate.of(2026, 8, 20));
        b1.setRentalStartDateTime(LocalDateTime.of(2026, 8, 30, 8, 0));
        b1.setRentalEndDateTime(LocalDateTime.of(2026, 8, 30, 23, 0));
        b1.setSubtotal(new BigDecimal("1150.00"));
        b1.setTotalAmount(new BigDecimal("1244.88"));
        b1.setDepositPaid(new BigDecimal("1244.88"));
        b1.setBalanceDue(BigDecimal.ZERO);
        b1.setNotes("High priority wedding reception rental.");
        bookingRepository.save(b1);

        // Booking Items
        BookingItem bi1 = new BookingItem();
        bi1.setBookingId(b1.getId());
        bi1.setProductId(chiavariId);
        bi1.setDescription("Chiavari Chair (Gold)");
        bi1.setQuantity(100);
        bi1.setUnitPrice(new BigDecimal("8.00"));
        bi1.setRentalStartDateTime(b1.getRentalStartDateTime());
        bi1.setRentalEndDateTime(b1.getRentalEndDateTime());
        bi1.setLineSubtotal(new BigDecimal("800.00"));
        bookingItemRepository.save(bi1);

        BookingItem bi2 = new BookingItem();
        bi2.setBookingId(b1.getId());
        bi2.setProductId(roundTableId);
        bi2.setDescription("Round Banquet Table 60\"");
        bi2.setQuantity(10);
        bi2.setUnitPrice(new BigDecimal("15.00"));
        bi2.setRentalStartDateTime(b1.getRentalStartDateTime());
        bi2.setRentalEndDateTime(b1.getRentalEndDateTime());
        bi2.setLineSubtotal(new BigDecimal("150.00"));
        bookingItemRepository.save(bi2);

        BookingItem bi3 = new BookingItem();
        bi3.setBookingId(b1.getId());
        bi3.setProductId(linenId);
        bi3.setDescription("White Table Linen 120\" Round");
        bi3.setQuantity(20);
        bi3.setUnitPrice(new BigDecimal("10.00"));
        bi3.setRentalStartDateTime(b1.getRentalStartDateTime());
        bi3.setRentalEndDateTime(b1.getRentalEndDateTime());
        bi3.setLineSubtotal(new BigDecimal("200.00"));
        bookingItemRepository.save(bi3);

        // Seed Inventory Reservations for BOOK-000123
        reservationRepository.save(new InventoryReservation(UUID.randomUUID(), tenantId, chiavariId, eventId, b1.getId(), 100, b1.getRentalStartDateTime(), b1.getRentalEndDateTime(), ReservationStatus.RESERVED));

        // 6. Seed Demo Warehouse Order WH-000123
        WarehouseOrder wo1 = new WarehouseOrder();
        wo1.setId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        wo1.setTenantId(tenantId);
        wo1.setBookingId(b1.getId());
        wo1.setEventId(eventId);
        wo1.setCustomerId(customerId);
        wo1.setOrderNumber("WH-000123");
        wo1.setScheduledDate(b1.getRentalStartDateTime());
        wo1.setPriority(WarehouseOrderPriority.HIGH);
        wo1.setStatus(WarehouseOrderStatus.READY_TO_PICK);
        wo1.setNotes("Ensure pristine gold finish on Chiavari chairs.");
        wo1.setCreatedBy("System");

        WarehouseOrder savedWo1 = orderRepository.save(wo1);

        // Warehouse Items for WH-000123
        WarehouseOrderItem woi1 = new WarehouseOrderItem();
        woi1.setWarehouseOrderId(savedWo1.getId());
        woi1.setBookingItemId(bi1.getId());
        woi1.setProductId(chiavariId);
        woi1.setProductNameSnapshot("Chiavari Chair");
        woi1.setSkuSnapshot(chiavari != null ? chiavari.getSku() : "CHI-001");
        woi1.setLocationSnapshot("A-01-03");
        woi1.setQuantityRequired(100);
        woi1.setQuantityPicked(0);
        woi1.setQuantityPacked(0);
        woi1.setStatus(WarehouseOrderItemStatus.PENDING);
        itemRepository.save(woi1);

        WarehouseOrderItem woi2 = new WarehouseOrderItem();
        woi2.setWarehouseOrderId(savedWo1.getId());
        woi2.setBookingItemId(bi2.getId());
        woi2.setProductId(roundTableId);
        woi2.setProductNameSnapshot("Round Table");
        woi2.setSkuSnapshot(roundTable != null ? roundTable.getSku() : "TBL-060");
        woi2.setLocationSnapshot("B-02-01");
        woi2.setQuantityRequired(10);
        woi2.setQuantityPicked(0);
        woi2.setQuantityPacked(0);
        woi2.setStatus(WarehouseOrderItemStatus.PENDING);
        itemRepository.save(woi2);

        WarehouseOrderItem woi3 = new WarehouseOrderItem();
        woi3.setWarehouseOrderId(savedWo1.getId());
        woi3.setBookingItemId(bi3.getId());
        woi3.setProductId(linenId);
        woi3.setProductNameSnapshot("White Linen");
        woi3.setSkuSnapshot(linen != null ? linen.getSku() : "LIN-WHT");
        woi3.setLocationSnapshot("C-01-02");
        woi3.setQuantityRequired(20);
        woi3.setQuantityPicked(0);
        woi3.setQuantityPacked(0);
        woi3.setStatus(WarehouseOrderItemStatus.PENDING);
        itemRepository.save(woi3);

        auditRepository.save(new WarehouseAudit(tenantId, savedWo1.getId(), b1.getId(), "WAREHOUSE_ORDER_CREATED", "System", "Seeded Demo Order WH-000123"));

        // 7. Seed Demo Shortage Scenario Order WH-000124
        Booking b2 = new Booking();
        b2.setId(DEMO_SHORTAGE_BOOKING_ID);
        b2.setTenantId(tenantId);
        b2.setBookingNumber("BOOK-000124");
        b2.setQuoteId(UUID.randomUUID());
        b2.setCustomerId(customerId);
        b2.setEventId(eventId);
        b2.setStatus(BookingStatus.CONFIRMED);
        b2.setBookingDate(LocalDate.of(2026, 8, 21));
        b2.setRentalStartDateTime(LocalDateTime.of(2026, 8, 31, 9, 0));
        b2.setRentalEndDateTime(LocalDateTime.of(2026, 8, 31, 22, 0));
        b2.setSubtotal(new BigDecimal("800.00"));
        b2.setTotalAmount(new BigDecimal("866.00"));
        b2.setDepositPaid(new BigDecimal("866.00"));
        bookingRepository.save(b2);

        WarehouseOrder wo2 = new WarehouseOrder();
        wo2.setId(UUID.fromString("22222222-2222-2222-2222-222222222124"));
        wo2.setTenantId(tenantId);
        wo2.setBookingId(b2.getId());
        wo2.setEventId(eventId);
        wo2.setCustomerId(customerId);
        wo2.setOrderNumber("WH-000124");
        wo2.setScheduledDate(b2.getRentalStartDateTime());
        wo2.setPriority(WarehouseOrderPriority.URGENT);
        wo2.setStatus(WarehouseOrderStatus.PICKING);
        wo2.setNotes("Corporate Event - Chair Shortage Test");
        wo2.setCreatedBy("System");
        WarehouseOrder savedWo2 = orderRepository.save(wo2);

        WarehouseOrderItem woi2_1 = new WarehouseOrderItem();
        woi2_1.setWarehouseOrderId(savedWo2.getId());
        woi2_1.setBookingItemId(UUID.randomUUID());
        woi2_1.setProductId(chiavariId);
        woi2_1.setProductNameSnapshot("Chiavari Chair");
        woi2_1.setSkuSnapshot(chiavari != null ? chiavari.getSku() : "CHI-001");
        woi2_1.setLocationSnapshot("A-01-03");
        woi2_1.setQuantityRequired(100);
        woi2_1.setQuantityPicked(95);
        woi2_1.setQuantityPacked(0);
        woi2_1.setStatus(WarehouseOrderItemStatus.SHORT);
        woi2_1.setNotes("5 chairs unavailable in aisle A-01-03.");
        itemRepository.save(woi2_1);

        auditRepository.save(new WarehouseAudit(tenantId, savedWo2.getId(), b2.getId(), "WAREHOUSE_ITEM_SHORT", "Staff", "5 chairs unavailable."));

        System.out.println("✅ Seeded Day 15 Demo Warehouse Orders WH-000123 & WH-000124 (Shortage Demo).");
    }
}
