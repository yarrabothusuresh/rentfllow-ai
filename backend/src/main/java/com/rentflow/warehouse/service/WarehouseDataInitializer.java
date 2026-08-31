package com.rentflow.warehouse.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(5)
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
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PackingContainerRepository containerRepository;
    private final KitDefinitionRepository kitDefinitionRepository;
    private final KitComponentRepository kitComponentRepository;
    private final PickListRepository pickListRepository;
    private final PickListItemRepository pickListItemRepository;
    private final PackListRepository packListRepository;
    private final PackListItemRepository packListItemRepository;
    private final LoadListRepository loadListRepository;
    private final LoadListItemRepository loadListItemRepository;
    private final WarehouseExceptionRepository exceptionRepository;
    private final WarehouseSubstitutionRepository substitutionRepository;
    private final WarehouseOrderChecklistRepository checklistRepository;
    private final WarehouseRepository warehouseRepository;

    public static final UUID DEMO_BOOKING_ID = UUID.fromString("b0000000-0000-0000-0000-000000000123");
    public static final UUID DEMO_SHORTAGE_BOOKING_ID = UUID.fromString("b0000000-0000-0000-0000-000000000124");
    public static final UUID DEMO_DAY23_BOOKING_ID = UUID.fromString("b0000000-0000-0000-0000-000000000456");

    public WarehouseDataInitializer(WarehouseLocationRepository locationRepository,
                                    WarehouseOrderRepository orderRepository,
                                    WarehouseOrderItemRepository itemRepository,
                                    CustomerRepository customerRepository,
                                    EventRepository eventRepository,
                                    BookingRepository bookingRepository,
                                    BookingItemRepository bookingItemRepository,
                                    InventoryReservationRepository reservationRepository,
                                    ProductRepository productRepository,
                                    WarehouseAuditRepository auditRepository,
                                    InventoryItemRepository inventoryItemRepository,
                                    StockMovementRepository stockMovementRepository,
                                    PackingContainerRepository containerRepository,
                                    KitDefinitionRepository kitDefinitionRepository,
                                    KitComponentRepository kitComponentRepository,
                                    PickListRepository pickListRepository,
                                    PickListItemRepository pickListItemRepository,
                                    PackListRepository packListRepository,
                                    PackListItemRepository packListItemRepository,
                                    LoadListRepository loadListRepository,
                                    LoadListItemRepository loadListItemRepository,
                                    WarehouseExceptionRepository exceptionRepository,
                                    WarehouseSubstitutionRepository substitutionRepository,
                                    WarehouseOrderChecklistRepository checklistRepository,
                                    WarehouseRepository warehouseRepository) {
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
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.containerRepository = containerRepository;
        this.kitDefinitionRepository = kitDefinitionRepository;
        this.kitComponentRepository = kitComponentRepository;
        this.pickListRepository = pickListRepository;
        this.pickListItemRepository = pickListItemRepository;
        this.packListRepository = packListRepository;
        this.packListItemRepository = packListItemRepository;
        this.loadListRepository = loadListRepository;
        this.loadListItemRepository = loadListItemRepository;
        this.exceptionRepository = exceptionRepository;
        this.substitutionRepository = substitutionRepository;
        this.checklistRepository = checklistRepository;
        this.warehouseRepository = warehouseRepository;
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
            WarehouseLocation loc7 = new WarehouseLocation(UUID.fromString("a1000000-0000-0000-0000-000000000007"), tenantId, "D-01-01", "Aisle D, Rack 01, Shelf 01", "Stage Lighting & Electronics", true);

            locationRepository.saveAll(List.of(loc1, loc2, loc3, loc4, loc5, loc6, loc7));
            System.out.println("✅ Seeded Day 23 Warehouse Locations.");
        }

        // 2. Seed Containers
        if (containerRepository.count() == 0) {
            PackingContainer c1 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000001"), tenantId, "BAG-001", ContainerType.BAG, ContainerStatus.AVAILABLE, null, "Heavy Duty Linen Transport Bag");
            PackingContainer c2 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000002"), tenantId, "BAG-002", ContainerType.BAG, ContainerStatus.AVAILABLE, null, "Heavy Duty Linen Transport Bag");
            PackingContainer c3 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000003"), tenantId, "CASE-001", ContainerType.CASE, ContainerStatus.AVAILABLE, null, "Padded Lighting & Controller Flight Case");
            PackingContainer c4 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000004"), tenantId, "CASE-002", ContainerType.CASE, ContainerStatus.AVAILABLE, null, "Audio & Cable Trunk Flight Case");
            PackingContainer c5 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000005"), tenantId, "CART-001", ContainerType.CART, ContainerStatus.AVAILABLE, null, "Banquet Table Rolling Cart");
            PackingContainer c6 = new PackingContainer(UUID.fromString("c0000000-0000-0000-0000-000000000006"), tenantId, "PALLET-001", ContainerType.PALLET, ContainerStatus.AVAILABLE, null, "Standard Wood Staging Pallet");

            containerRepository.saveAll(List.of(c1, c2, c3, c4, c5, c6));
            System.out.println("✅ Seeded Day 23 Packing Containers.");
        }

        // 3. Seed Demo Customer: ABC Events LLC
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

        // 4. Seed Demo Event: Wedding Reception
        UUID eventId = UUID.fromString("e4444444-4444-4444-4444-444444444444");
        if (eventRepository.findById(eventId).isEmpty()) {
            Event e = new Event();
            e.setId(eventId);
            e.setTenantId(tenantId);
            e.setCustomerId(customerId);
            e.setEventName("Wedding Reception");
            e.setEventType(EventType.WEDDING);
            e.setEventDate(LocalDate.now().plusDays(1));
            e.setStartTime("10:00");
            e.setEndTime("22:00");
            e.setGuestCount(150);
            e.setVenueName("Grand Ballroom");
            e.setVenueAddress("500 Celebration Blvd");
            e.setCity("Dallas");
            e.setState("TX");
            e.setZipCode("75202");
            e.setStatus(EventStatus.BOOKED);
            eventRepository.save(e);
        }

        // 5. Ensure Products exist (Chiavari Chair, Table, Linen, Lighting Kit, Replacement White Folding Chair)
        Product chiavari = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("CHI")).findFirst().orElse(null);
        if (chiavari == null) {
            chiavari = new Product(UUID.fromString("00000000-0000-0000-0000-000000000091"), tenantId, "CHI-001", "Chiavari Chair (Gold)", "Pristine gold chiavari chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("8.00"), new BigDecimal("95.00"), 250, 0, 0, 0);
            chiavari.setTrackingType(ProductTrackingType.SERIALIZED);
            chiavari = productRepository.save(chiavari);
        }

        Product foldingChair = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("FLD")).findFirst().orElse(null);
        if (foldingChair == null) {
            foldingChair = new Product(UUID.fromString("00000000-0000-0000-0000-000000000095"), tenantId, "FLD-001", "White Folding Chair", "Standard white folding chair (Substitution)", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("4.50"), new BigDecimal("45.00"), 300, 0, 0, 0);
            foldingChair.setTrackingType(ProductTrackingType.QUANTITY);
            foldingChair = productRepository.save(foldingChair);
        }

        Product roundTable = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("TBL")).findFirst().orElse(null);
        if (roundTable == null) {
            roundTable = new Product(UUID.fromString("00000000-0000-0000-0000-000000000092"), tenantId, "TBL-060", "Round Banquet Table 60\"", "Heavy duty banquet table", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("15.00"), new BigDecimal("160.00"), 50, 0, 0, 0);
            roundTable.setTrackingType(ProductTrackingType.SERIALIZED);
            roundTable = productRepository.save(roundTable);
        }

        Product linen = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("LIN")).findFirst().orElse(null);
        if (linen == null) {
            linen = new Product(UUID.fromString("00000000-0000-0000-0000-000000000093"), tenantId, "LIN-WHT", "White Table Linen 120\"", "Premium polyester white linen", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("10.00"), new BigDecimal("35.00"), 100, 0, 0, 0);
            linen.setTrackingType(ProductTrackingType.QUANTITY);
            linen = productRepository.save(linen);
        }

        Product lightingKit = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("LGT-KIT")).findFirst().orElse(null);
        if (lightingKit == null) {
            lightingKit = new Product(UUID.fromString("00000000-0000-0000-0000-000000000094"), tenantId, "LGT-KIT", "Stage Lighting Kit", "Complete LED Uplight Stage Kit (4 Lights, 1 Controller, 4 Cables)", null, ProductType.PACKAGE, ProductStatus.ACTIVE, new BigDecimal("75.00"), new BigDecimal("450.00"), 10, 0, 0, 0);
            lightingKit.setTrackingType(ProductTrackingType.SERIALIZED);
            lightingKit = productRepository.save(lightingKit);
        }

        Product uplightComp = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("LGT-UP")).findFirst().orElse(null);
        if (uplightComp == null) {
            uplightComp = new Product(UUID.fromString("00000000-0000-0000-0000-000000000096"), tenantId, "LGT-UP", "LED Uplight Pod", "High-power wireless LED uplight", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("15.00"), new BigDecimal("90.00"), 40, 0, 0, 0);
            uplightComp = productRepository.save(uplightComp);
        }

        Product dmxControllerComp = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("LGT-CTR")).findFirst().orElse(null);
        if (dmxControllerComp == null) {
            dmxControllerComp = new Product(UUID.fromString("00000000-0000-0000-0000-000000000097"), tenantId, "LGT-CTR", "DMX Lighting Controller", "Compact 16-channel wireless DMX controller", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("25.00"), new BigDecimal("150.00"), 10, 0, 0, 0);
            dmxControllerComp = productRepository.save(dmxControllerComp);
        }

        Product dmxCableComp = productRepository.findByTenantId(tenantId).stream().filter(p -> p.getSku() != null && p.getSku().contains("LGT-CBL")).findFirst().orElse(null);
        if (dmxCableComp == null) {
            dmxCableComp = new Product(UUID.fromString("00000000-0000-0000-0000-000000000098"), tenantId, "LGT-CBL", "DMX Link Cable 25ft", "Shielded 3-pin DMX link cable", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("5.00"), new BigDecimal("20.00"), 50, 0, 0, 0);
            dmxCableComp = productRepository.save(dmxCableComp);
        }

        // Seed Kit Definition for Stage Lighting Kit
        if (kitDefinitionRepository.findByTenantIdAndProductId(tenantId, lightingKit.getId()).isEmpty()) {
            KitDefinition kd = kitDefinitionRepository.save(new KitDefinition(UUID.fromString("ea000000-0000-0000-0000-000000000001"), tenantId, lightingKit.getId(), "Stage Lighting Kit", "4 Uplights, 1 Controller, 4 Cables"));
            kitComponentRepository.save(new KitComponent(null, tenantId, kd.getId(), uplightComp.getId(), "LED Uplight Pod", "LGT-UP", 4));
            kitComponentRepository.save(new KitComponent(null, tenantId, kd.getId(), dmxControllerComp.getId(), "DMX Lighting Controller", "LGT-CTR", 1));
            kitComponentRepository.save(new KitComponent(null, tenantId, kd.getId(), dmxCableComp.getId(), "DMX Link Cable 25ft", "LGT-CBL", 4));
            System.out.println("✅ Seeded Day 23 Kit Definitions & Components for Stage Lighting Kit.");
        }

        // 6. Seed Serialized Demo Assets with Barcodes (e.g. CHR-000101..120, TAB-000101..110, CHR-000123 for scan demo)
        Warehouse mainWh = warehouseRepository.findByTenantId(tenantId).stream().findFirst().orElse(null);
        UUID whId = mainWh != null ? mainWh.getId() : UUID.fromString("fa000000-0000-0000-0000-000000000001");

        if (inventoryItemRepository.findByTenantIdAndAssetCode(tenantId, "CHR-000123").isEmpty()) {
            // Seed 20 Chiavari Chairs
            for (int i = 101; i <= 123; i++) {
                String code = String.format("CHR-%06d", i);
                InventoryItem item = new InventoryItem(
                        null, tenantId, chiavari.getId(), whId, code, "SN-" + code, code, "QR-" + code,
                        AssetStatus.AVAILABLE, AssetCondition.GOOD, new BigDecimal("95.00"), "A-01-03"
                );
                inventoryItemRepository.save(item);
            }

            // Seed 10 Tables
            for (int i = 101; i <= 110; i++) {
                String code = String.format("TAB-%06d", i);
                InventoryItem item = new InventoryItem(
                        null, tenantId, roundTable.getId(), whId, code, "SN-" + code, code, "QR-" + code,
                        AssetStatus.AVAILABLE, AssetCondition.GOOD, new BigDecimal("160.00"), "B-01-01"
                );
                inventoryItemRepository.save(item);
            }
            System.out.println("✅ Seeded Day 23 Serialized Inventory Items (CHR-000101..123, TAB-000101..110).");
        }

        // 7. Seed Day 23 Flagship Demo Booking: BOOK-000456
        if (bookingRepository.findByTenantIdAndBookingNumber(tenantId, "BOOK-000456").isEmpty()) {
            Booking b456 = new Booking();
            b456.setId(DEMO_DAY23_BOOKING_ID);
            b456.setTenantId(tenantId);
            b456.setBookingNumber("BOOK-000456");
            b456.setQuoteId(UUID.randomUUID());
            b456.setCustomerId(customerId);
            b456.setEventId(eventId);
            b456.setStatus(BookingStatus.CONFIRMED);
            b456.setBookingDate(LocalDate.now());
            b456.setRentalStartDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            b456.setRentalEndDateTime(LocalDateTime.now().plusDays(1).withHour(22).withMinute(0));
            b456.setSubtotal(new BigDecimal("1300.00"));
            b456.setTotalAmount(new BigDecimal("1407.25"));
            b456.setDepositPaid(new BigDecimal("1407.25"));
            b456.setBalanceDue(BigDecimal.ZERO);
            b456.setNotes("Day 23 Warehouse Operations 2.0 Demo Order. Priority VIP wedding delivery.");
            bookingRepository.save(b456);

            // 4 Items: 100 Chairs, 10 Tables, 20 Linens, 2 Lighting Kits
            BookingItem bi1 = new BookingItem();
            bi1.setBookingId(b456.getId());
            bi1.setProductId(chiavari.getId());
            bi1.setDescription("Chiavari Chair (Gold)");
            bi1.setQuantity(100);
            bi1.setUnitPrice(new BigDecimal("8.00"));
            bi1.setRentalStartDateTime(b456.getRentalStartDateTime());
            bi1.setRentalEndDateTime(b456.getRentalEndDateTime());
            bi1.setLineSubtotal(new BigDecimal("800.00"));
            bookingItemRepository.save(bi1);

            BookingItem bi2 = new BookingItem();
            bi2.setBookingId(b456.getId());
            bi2.setProductId(roundTable.getId());
            bi2.setDescription("Round Banquet Table 60\"");
            bi2.setQuantity(10);
            bi2.setUnitPrice(new BigDecimal("15.00"));
            bi2.setRentalStartDateTime(b456.getRentalStartDateTime());
            bi2.setRentalEndDateTime(b456.getRentalEndDateTime());
            bi2.setLineSubtotal(new BigDecimal("150.00"));
            bookingItemRepository.save(bi2);

            BookingItem bi3 = new BookingItem();
            bi3.setBookingId(b456.getId());
            bi3.setProductId(linen.getId());
            bi3.setDescription("White Table Linen 120\"");
            bi3.setQuantity(20);
            bi3.setUnitPrice(new BigDecimal("10.00"));
            bi3.setRentalStartDateTime(b456.getRentalStartDateTime());
            bi3.setRentalEndDateTime(b456.getRentalEndDateTime());
            bi3.setLineSubtotal(new BigDecimal("200.00"));
            bookingItemRepository.save(bi3);

            BookingItem bi4 = new BookingItem();
            bi4.setBookingId(b456.getId());
            bi4.setProductId(lightingKit.getId());
            bi4.setDescription("Stage Lighting Kit (2 Kits = 8 Uplights, 2 Controllers, 8 Cables)");
            bi4.setQuantity(2);
            bi4.setUnitPrice(new BigDecimal("75.00"));
            bi4.setRentalStartDateTime(b456.getRentalStartDateTime());
            bi4.setRentalEndDateTime(b456.getRentalEndDateTime());
            bi4.setLineSubtotal(new BigDecimal("150.00"));
            bookingItemRepository.save(bi4);

            // Seed Inventory Reservations
            reservationRepository.save(new InventoryReservation(UUID.randomUUID(), tenantId, chiavari.getId(), eventId, b456.getId(), 100, b456.getRentalStartDateTime(), b456.getRentalEndDateTime(), ReservationStatus.RESERVED));
            reservationRepository.save(new InventoryReservation(UUID.randomUUID(), tenantId, roundTable.getId(), eventId, b456.getId(), 10, b456.getRentalStartDateTime(), b456.getRentalEndDateTime(), ReservationStatus.RESERVED));
            reservationRepository.save(new InventoryReservation(UUID.randomUUID(), tenantId, linen.getId(), eventId, b456.getId(), 20, b456.getRentalStartDateTime(), b456.getRentalEndDateTime(), ReservationStatus.RESERVED));
            reservationRepository.save(new InventoryReservation(UUID.randomUUID(), tenantId, lightingKit.getId(), eventId, b456.getId(), 2, b456.getRentalStartDateTime(), b456.getRentalEndDateTime(), ReservationStatus.RESERVED));

            // Warehouse Order WH-000456
            WarehouseOrder wo456 = new WarehouseOrder();
            wo456.setId(UUID.fromString("44444444-4444-4444-4444-444444444456"));
            wo456.setTenantId(tenantId);
            wo456.setBookingId(b456.getId());
            wo456.setEventId(eventId);
            wo456.setCustomerId(customerId);
            wo456.setOrderNumber("WH-000456");
            wo456.setScheduledDate(b456.getRentalStartDateTime());
            wo456.setPriority(WarehouseOrderPriority.HIGH);
            wo456.setStatus(WarehouseOrderStatus.READY_TO_PICK);
            wo456.setAssignedTo("John Warehouse");
            wo456.setNotes("Wedding Reception - Tomorrow 10 AM delivery. VIP Gold package.");
            wo456.setCreatedBy("System");
            WarehouseOrder savedWo = orderRepository.save(wo456);

            // Warehouse Order Items
            WarehouseOrderItem woi1 = new WarehouseOrderItem();
            woi1.setWarehouseOrderId(savedWo.getId());
            woi1.setBookingItemId(bi1.getId());
            woi1.setProductId(chiavari.getId());
            woi1.setProductNameSnapshot("Chiavari Chair (Gold)");
            woi1.setSkuSnapshot(chiavari.getSku());
            woi1.setLocationSnapshot("A-01-03");
            woi1.setQuantityRequired(100);
            woi1.setQuantityPicked(65);
            woi1.setQuantityPacked(0);
            woi1.setStatus(WarehouseOrderItemStatus.PARTIALLY_PICKED);
            itemRepository.save(woi1);

            WarehouseOrderItem woi2 = new WarehouseOrderItem();
            woi2.setWarehouseOrderId(savedWo.getId());
            woi2.setBookingItemId(bi2.getId());
            woi2.setProductId(roundTable.getId());
            woi2.setProductNameSnapshot("Round Table 60\"");
            woi2.setSkuSnapshot(roundTable.getSku());
            woi2.setLocationSnapshot("B-01-01");
            woi2.setQuantityRequired(10);
            woi2.setQuantityPicked(0);
            woi2.setQuantityPacked(0);
            woi2.setStatus(WarehouseOrderItemStatus.PENDING);
            itemRepository.save(woi2);

            WarehouseOrderItem woi3 = new WarehouseOrderItem();
            woi3.setWarehouseOrderId(savedWo.getId());
            woi3.setBookingItemId(bi3.getId());
            woi3.setProductId(linen.getId());
            woi3.setProductNameSnapshot("White Linen 120\"");
            woi3.setSkuSnapshot(linen.getSku());
            woi3.setLocationSnapshot("C-01-01");
            woi3.setQuantityRequired(20);
            woi3.setQuantityPicked(0);
            woi3.setQuantityPacked(0);
            woi3.setStatus(WarehouseOrderItemStatus.PENDING);
            itemRepository.save(woi3);

            WarehouseOrderItem woi4 = new WarehouseOrderItem();
            woi4.setWarehouseOrderId(savedWo.getId());
            woi4.setBookingItemId(bi4.getId());
            woi4.setProductId(lightingKit.getId());
            woi4.setProductNameSnapshot("Stage Lighting Kit");
            woi4.setSkuSnapshot(lightingKit.getSku());
            woi4.setLocationSnapshot("D-01-01");
            woi4.setQuantityRequired(2);
            woi4.setQuantityPicked(0);
            woi4.setQuantityPacked(0);
            woi4.setStatus(WarehouseOrderItemStatus.PENDING);
            itemRepository.save(woi4);

            // Seed Pick List PICK-000123 for WH-000456
            PickList pl = new PickList();
            pl.setId(UUID.fromString("ba111111-1111-1111-1111-111111111123"));
            pl.setTenantId(tenantId);
            pl.setPickListNumber("PICK-000123");
            pl.setWarehouseOrderId(savedWo.getId());
            pl.setWarehouseId(whId);
            pl.setStatus(PickListStatus.IN_PROGRESS);
            pl.setPriority(WarehouseOrderPriority.HIGH);
            pl.setAssignedTo("John Warehouse");
            pl.setStartedAt(LocalDateTime.now().minusMinutes(25));
            PickList savedPl = pickListRepository.save(pl);

            // Pick List Items in Ordered Traversal Sequence
            PickListItem pli1 = new PickListItem();
            pli1.setTenantId(tenantId);
            pli1.setPickListId(savedPl.getId());
            pli1.setBookingItemId(bi1.getId());
            pli1.setProductId(chiavari.getId());
            pli1.setProductNameSnapshot("Chiavari Chair (Gold)");
            pli1.setSkuSnapshot(chiavari.getSku());
            pli1.setLocationCodeSnapshot("A-01-03");
            pli1.setRequiredQuantity(100);
            pli1.setPickedQuantity(65);
            pli1.setStatus(PickListItemStatus.PARTIAL);
            pli1.setSequenceNumber(1);
            pickListItemRepository.save(pli1);

            PickListItem pli2 = new PickListItem();
            pli2.setTenantId(tenantId);
            pli2.setPickListId(savedPl.getId());
            pli2.setBookingItemId(bi2.getId());
            pli2.setProductId(roundTable.getId());
            pli2.setProductNameSnapshot("Round Table 60\"");
            pli2.setSkuSnapshot(roundTable.getSku());
            pli2.setLocationCodeSnapshot("B-01-01");
            pli2.setRequiredQuantity(10);
            pli2.setPickedQuantity(0);
            pli2.setStatus(PickListItemStatus.PENDING);
            pli2.setSequenceNumber(2);
            pickListItemRepository.save(pli2);

            PickListItem pli3 = new PickListItem();
            pli3.setTenantId(tenantId);
            pli3.setPickListId(savedPl.getId());
            pli3.setBookingItemId(bi3.getId());
            pli3.setProductId(linen.getId());
            pli3.setProductNameSnapshot("White Linen 120\"");
            pli3.setSkuSnapshot(linen.getSku());
            pli3.setLocationCodeSnapshot("C-01-01");
            pli3.setRequiredQuantity(20);
            pli3.setPickedQuantity(0);
            pli3.setStatus(PickListItemStatus.PENDING);
            pli3.setSequenceNumber(3);
            pickListItemRepository.save(pli3);

            PickListItem pli4 = new PickListItem();
            pli4.setTenantId(tenantId);
            pli4.setPickListId(savedPl.getId());
            pli4.setBookingItemId(bi4.getId());
            pli4.setProductId(lightingKit.getId());
            pli4.setProductNameSnapshot("Stage Lighting Kit");
            pli4.setSkuSnapshot(lightingKit.getSku());
            pli4.setLocationCodeSnapshot("D-01-01");
            pli4.setRequiredQuantity(2);
            pli4.setPickedQuantity(0);
            pli4.setStatus(PickListItemStatus.PENDING);
            pli4.setSequenceNumber(4);
            pickListItemRepository.save(pli4);

            // Seed Checklists
            checklistRepository.save(new WarehouseOrderChecklist(null, tenantId, savedWo.getId(), ChecklistStage.PICK, "Verify chair & table surface counts against wedding floor plan", true));
            checklistRepository.save(new WarehouseOrderChecklist(null, tenantId, savedWo.getId(), ChecklistStage.PICK, "Inspect Chiavari chair gold coating for scratches", false));
            checklistRepository.save(new WarehouseOrderChecklist(null, tenantId, savedWo.getId(), ChecklistStage.PACK, "Pack lighting fixtures into CASE-001 with custom foam inserts", true));
            checklistRepository.save(new WarehouseOrderChecklist(null, tenantId, savedWo.getId(), ChecklistStage.PACK, "Bag 20 white linens into sealed transport bags", true));
            checklistRepository.save(new WarehouseOrderChecklist(null, tenantId, savedWo.getId(), ChecklistStage.LOAD, "Verify driver John Smith receipt confirmation", true));

            auditRepository.save(new WarehouseAudit(tenantId, savedWo.getId(), b456.getId(), "PICK_LIST_CREATED", "System", "Generated Pick List PICK-000123 for BOOK-000456"));
            auditRepository.save(new WarehouseAudit(tenantId, savedWo.getId(), b456.getId(), "PICK_STARTED", "John Warehouse", "Operator started picking list PICK-000123"));

            System.out.println("✅ Seeded Day 23 Demo Booking BOOK-000456, Order WH-000456, and Pick List PICK-000123.");
        }
    }
}
