package com.rentflow.warehouse;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.delivery.dto.DeliveryDTO;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.delivery.service.DeliveryService;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.warehouse.dto.*;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import com.rentflow.warehouse.service.WarehouseFulfillmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WarehouseFulfillmentTest {

    @Autowired
    private WarehouseFulfillmentService fulfillmentService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private WarehouseOrderRepository orderRepository;

    @Autowired
    private WarehouseOrderItemRepository orderItemRepository;

    @Autowired
    private PickListRepository pickListRepository;

    @Autowired
    private PickListItemRepository pickListItemRepository;

    @Autowired
    private PackListRepository packListRepository;

    @Autowired
    private PackListItemRepository packListItemRepository;

    @Autowired
    private LoadListRepository loadListRepository;

    @Autowired
    private LoadListItemRepository loadListItemRepository;

    @Autowired
    private PackingContainerRepository containerRepository;

    @Autowired
    private KitDefinitionRepository kitDefinitionRepository;

    @Autowired
    private KitComponentRepository kitComponentRepository;

    @Autowired
    private WarehouseExceptionRepository exceptionRepository;

    @Autowired
    private WarehouseSubstitutionRepository substitutionRepository;

    @Autowired
    private WarehouseLocationRepository locationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private String tenantA = "tenant-test-wh-a";
    private String tenantB = "tenant-test-wh-b";

    private UUID warehouseAId;
    private UUID warehouseBId;
    private UUID chairProductId;
    private UUID foldingChairProductId;
    private UUID tableProductId;
    private UUID linenProductId;
    private UUID lightingKitProductId;
    private UUID uplightProductId;
    private UUID controllerProductId;
    private UUID cableProductId;

    private UUID testBookingId;
    private UUID testWarehouseOrderId;
    private UUID testVehicleId;
    private UUID testDriverId;
    private UUID testDeliveryId;

    @BeforeEach
    void setUp() {
        // 1. Warehouses
        Warehouse whA = warehouseRepository.save(new Warehouse(UUID.randomUUID(), tenantA, "WH-A", "Main Hub A", "100 Warehouse Way, Dallas, TX 75201", 50, 50, 50, true));
        warehouseAId = whA.getId();

        Warehouse whB = warehouseRepository.save(new Warehouse(UUID.randomUUID(), tenantB, "WH-B", "Main Hub B", "200 Warehouse Way, Austin, TX 78701", 50, 50, 50, true));
        warehouseBId = whB.getId();

        // 2. Locations
        locationRepository.save(new WarehouseLocation(UUID.randomUUID(), tenantA, "A-01-01", "Aisle A, Rack 01", "Zone A", true));
        locationRepository.save(new WarehouseLocation(UUID.randomUUID(), tenantA, "A-01-02", "Aisle A, Rack 02", "Zone A", true));
        locationRepository.save(new WarehouseLocation(UUID.randomUUID(), tenantA, "B-01-01", "Aisle B, Rack 01", "Zone B", true));

        // 3. Products
        Product chair = new Product(UUID.randomUUID(), tenantA, "TEST-CHI-01", "Gold Chiavari Chair", "Gold chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("8.00"), new BigDecimal("95.00"), 200, 0, 0, 0);
        chair.setTrackingType(ProductTrackingType.SERIALIZED);
        chairProductId = productRepository.save(chair).getId();

        Product foldingChair = new Product(UUID.randomUUID(), tenantA, "TEST-FLD-01", "White Folding Chair", "Folding chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("4.50"), new BigDecimal("45.00"), 300, 0, 0, 0);
        foldingChair.setTrackingType(ProductTrackingType.QUANTITY);
        foldingChairProductId = productRepository.save(foldingChair).getId();

        Product table = new Product(UUID.randomUUID(), tenantA, "TEST-TBL-01", "Round Table 60in", "Round table", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("15.00"), new BigDecimal("160.00"), 50, 0, 0, 0);
        table.setTrackingType(ProductTrackingType.SERIALIZED);
        tableProductId = productRepository.save(table).getId();

        Product linen = new Product(UUID.randomUUID(), tenantA, "TEST-LIN-01", "White Table Linen", "Linen", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("10.00"), new BigDecimal("35.00"), 100, 0, 0, 0);
        linen.setTrackingType(ProductTrackingType.QUANTITY);
        linenProductId = productRepository.save(linen).getId();

        Product lightingKit = new Product(UUID.randomUUID(), tenantA, "TEST-LGT-KIT", "Stage Lighting Kit", "Kit package", null, ProductType.PACKAGE, ProductStatus.ACTIVE, new BigDecimal("75.00"), new BigDecimal("450.00"), 10, 0, 0, 0);
        lightingKitProductId = productRepository.save(lightingKit).getId();

        Product uplight = productRepository.save(new Product(UUID.randomUUID(), tenantA, "TEST-LGT-UP", "Uplight Pod", "Uplight", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("15.00"), new BigDecimal("90.00"), 40, 0, 0, 0));
        uplightProductId = uplight.getId();

        Product controller = productRepository.save(new Product(UUID.randomUUID(), tenantA, "TEST-LGT-CTR", "Controller", "Controller", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("25.00"), new BigDecimal("150.00"), 10, 0, 0, 0));
        controllerProductId = controller.getId();

        Product cable = productRepository.save(new Product(UUID.randomUUID(), tenantA, "TEST-LGT-CBL", "Cable", "Cable", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, new BigDecimal("5.00"), new BigDecimal("20.00"), 50, 0, 0, 0));
        cableProductId = cable.getId();

        // Kit Definition
        KitDefinition kd = kitDefinitionRepository.save(new KitDefinition(null, tenantA, lightingKitProductId, "Stage Lighting Kit", "Components"));
        kitComponentRepository.save(new KitComponent(null, tenantA, kd.getId(), uplightProductId, "Uplight Pod", "TEST-LGT-UP", 4));
        kitComponentRepository.save(new KitComponent(null, tenantA, kd.getId(), controllerProductId, "Controller", "TEST-LGT-CTR", 1));
        kitComponentRepository.save(new KitComponent(null, tenantA, kd.getId(), cableProductId, "Cable", "TEST-LGT-CBL", 4));

        // 4. Physical Serialized Assets
        inventoryItemRepository.save(new InventoryItem(null, tenantA, chairProductId, warehouseAId, "CHR-000123", "SN-123", "CHR-000123", "QR-CHR-123", AssetStatus.AVAILABLE, AssetCondition.GOOD, new BigDecimal("95.00"), "A-01-01"));
        inventoryItemRepository.save(new InventoryItem(null, tenantA, chairProductId, warehouseAId, "CHR-000124", "SN-124", "CHR-000124", "QR-CHR-124", AssetStatus.AVAILABLE, AssetCondition.GOOD, new BigDecimal("95.00"), "A-01-01"));
        inventoryItemRepository.save(new InventoryItem(null, tenantB, chairProductId, warehouseBId, "CHR-TENANT-B", "SN-TB", "CHR-TENANT-B", "QR-TB", AssetStatus.AVAILABLE, AssetCondition.GOOD, new BigDecimal("95.00"), "B-01-01"));

        // 5. Booking & Warehouse Order
        Booking booking = new Booking();
        booking.setTenantId(tenantA);
        booking.setBookingNumber("TEST-BKG-2301");
        booking.setQuoteId(UUID.randomUUID());
        booking.setCustomerId(UUID.randomUUID());
        booking.setEventId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        booking.setRentalEndDateTime(LocalDateTime.now().plusDays(2));
        Booking savedBkg = bookingRepository.save(booking);
        testBookingId = savedBkg.getId();

        BookingItem bi1 = new BookingItem();
        bi1.setBookingId(testBookingId);
        bi1.setProductId(chairProductId);
        bi1.setDescription("Gold Chiavari Chair");
        bi1.setQuantity(100);
        bi1.setUnitPrice(new BigDecimal("8.00"));
        bi1.setRentalStartDateTime(savedBkg.getRentalStartDateTime());
        bi1.setRentalEndDateTime(savedBkg.getRentalEndDateTime());
        bookingItemRepository.save(bi1);

        BookingItem bi2 = new BookingItem();
        bi2.setBookingId(testBookingId);
        bi2.setProductId(linenProductId);
        bi2.setDescription("White Table Linen");
        bi2.setQuantity(20);
        bi2.setUnitPrice(new BigDecimal("10.00"));
        bi2.setRentalStartDateTime(savedBkg.getRentalStartDateTime());
        bi2.setRentalEndDateTime(savedBkg.getRentalEndDateTime());
        bookingItemRepository.save(bi2);

        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(tenantA);
        order.setBookingId(testBookingId);
        order.setCustomerId(savedBkg.getCustomerId());
        order.setEventId(savedBkg.getEventId());
        order.setOrderNumber("WH-TEST-2301");
        order.setStatus(WarehouseOrderStatus.READY_TO_PICK);
        order.setPriority(WarehouseOrderPriority.HIGH);
        order.setAssignedTo("Tester Operator");
        WarehouseOrder savedOrder = orderRepository.save(order);
        testWarehouseOrderId = savedOrder.getId();

        WarehouseOrderItem woi1 = new WarehouseOrderItem();
        woi1.setWarehouseOrderId(testWarehouseOrderId);
        woi1.setBookingItemId(bi1.getId());
        woi1.setProductId(chairProductId);
        woi1.setProductNameSnapshot("Gold Chiavari Chair");
        woi1.setSkuSnapshot("TEST-CHI-01");
        woi1.setLocationSnapshot("A-01-01");
        woi1.setQuantityRequired(100);
        woi1.setQuantityPicked(0);
        woi1.setQuantityPacked(0);
        orderItemRepository.save(woi1);

        WarehouseOrderItem woi2 = new WarehouseOrderItem();
        woi2.setWarehouseOrderId(testWarehouseOrderId);
        woi2.setBookingItemId(bi2.getId());
        woi2.setProductId(linenProductId);
        woi2.setProductNameSnapshot("White Table Linen");
        woi2.setSkuSnapshot("TEST-LIN-01");
        woi2.setLocationSnapshot("B-01-01");
        woi2.setQuantityRequired(20);
        woi2.setQuantityPicked(0);
        woi2.setQuantityPacked(0);
        orderItemRepository.save(woi2);

        // Vehicle & Driver
        Vehicle v = new Vehicle();
        v.setTenantId(tenantA);
        v.setVehicleNumber("VAN-01");
        v.setName("Delivery Van 01");
        v.setType("Van");
        v.setCapacity(1500);
        Vehicle savedV = vehicleRepository.save(v);
        testVehicleId = savedV.getId();

        Driver d = new Driver();
        d.setTenantId(tenantA);
        d.setName("John Driver");
        d.setPhone("+1 555-010-9988");
        Driver savedD = driverRepository.save(d);
        testDriverId = savedD.getId();

        // Delivery
        Delivery del = new Delivery();
        del.setTenantId(tenantA);
        del.setDeliveryNumber("DEL-TEST-2301");
        del.setBookingId(testBookingId);
        del.setWarehouseOrderId(testWarehouseOrderId);
        del.setCustomerId(savedBkg.getCustomerId());
        del.setEventId(savedBkg.getEventId());
        del.setVehicleId(testVehicleId);
        del.setDriverId(testDriverId);
        del.setStatus(com.rentflow.delivery.model.DeliveryStatus.ASSIGNED);
        del.setScheduledDate(LocalDate.now());
        Delivery savedDel = deliveryRepository.save(del);
        testDeliveryId = savedDel.getId();
    }

    @Test
    @DisplayName("1. Pick List Generation & Sequence Ordering")
    void testPickListGenerationAndSequence() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        assertNotNull(pl);
        assertNotNull(pl.getId());
        assertEquals("WH-TEST-2301", pl.getWarehouseOrderNumber());
        assertEquals(2, pl.getItems().size());

        // Check Sequence Number Traversal
        assertEquals(1, pl.getItems().get(0).getSequenceNumber());
        assertEquals(2, pl.getItems().get(1).getSequenceNumber());

        // Duplicate prevention
        PickListDTO duplicatePl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        assertEquals(pl.getId(), duplicatePl.getId());
    }

    @Test
    @DisplayName("2. Serialized Asset Scan & Duplicate Scan Rejection")
    void testSerializedScanAndDuplicateRejection() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");

        // Scan valid asset CHR-000123
        PickScanResponseDTO scan1 = fulfillmentService.scanPickItem(tenantA, pl.getId(), "CHR-000123", "WAREHOUSE_OPERATOR");
        assertTrue(scan1.isSuccess());
        assertEquals("CHR-000123", scan1.getAssetCode());
        assertEquals(1, scan1.getPickedQuantity());

        // Verify physical asset transitioned to PICKED
        InventoryItem asset = inventoryItemRepository.findByTenantIdAndAssetCode(tenantA, "CHR-000123").orElseThrow();
        assertEquals(AssetStatus.PICKED, asset.getStatus());
        assertEquals(testBookingId, asset.getCurrentBookingId());

        // Re-scan same asset -> must reject
        assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.scanPickItem(tenantA, pl.getId(), "CHR-000123", "WAREHOUSE_OPERATOR"));
    }

    @Test
    @DisplayName("3. Wrong Product & Wrong Warehouse Scan Rejection")
    void testWrongProductAndWrongWarehouseScan() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");

        // Wrong product (table is not on this booking item)
        Product unbooked = productRepository.save(new Product(UUID.randomUUID(), tenantA, "UNBOOKED-01", "Unbooked Item", null, null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.TEN, BigDecimal.TEN, 10, 0, 0, 0));
        inventoryItemRepository.save(new InventoryItem(null, tenantA, unbooked.getId(), warehouseAId, "UNBOOKED-001", "SN-U1", "UNBOOKED-001", "QR-U1", AssetStatus.AVAILABLE, AssetCondition.GOOD, BigDecimal.TEN, "A-01-01"));

        ResponseStatusException exWrongProd = assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.scanPickItem(tenantA, pl.getId(), "UNBOOKED-001", "WAREHOUSE_OPERATOR"));
        assertTrue(exWrongProd.getReason().contains("not required for this pick list"));

        // Tenant B asset scan -> must reject
        assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.scanPickItem(tenantA, pl.getId(), "CHR-TENANT-B", "WAREHOUSE_OPERATOR"));
    }

    @Test
    @DisplayName("4. Quantity Bulk Picking & Exceeding Limit Rejection")
    void testQuantityPicking() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        PickListItemDTO linenItem = pl.getItems().stream().filter(i -> i.getProductId().equals(linenProductId)).findFirst().orElseThrow();

        // Pick 15 out of 20
        PickListDTO updated = fulfillmentService.pickQuantity(tenantA, pl.getId(), linenItem.getId(), 15, "First batch", "WAREHOUSE_OPERATOR");
        PickListItemDTO updatedItem = updated.getItems().stream().filter(i -> i.getId().equals(linenItem.getId())).findFirst().orElseThrow();
        assertEquals(15, updatedItem.getPickedQuantity());
        assertEquals(5, updatedItem.getRemainingQuantity());
        assertEquals(PickListItemStatus.PARTIAL, updatedItem.getStatus());

        // Exceeding quantity limit (10 more exceeds required 20)
        assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.pickQuantity(tenantA, pl.getId(), linenItem.getId(), 10, null, "WAREHOUSE_OPERATOR"));
    }

    @Test
    @DisplayName("5. Short Pick Reporting & Blocking Pick Completion")
    void testShortPickAndCompletionBlocking() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        PickListItemDTO chairItem = pl.getItems().stream().filter(i -> i.getProductId().equals(chairProductId)).findFirst().orElseThrow();

        // Report shortage of 5 chairs
        WarehouseExceptionDTO exc = fulfillmentService.reportShortage(tenantA, pl.getId(), chairItem.getId(), 5, "NOT_FOUND", "5 chairs broken/missing in bay", "WAREHOUSE_OPERATOR");
        assertNotNull(exc);
        assertEquals(WarehouseExceptionSeverity.BLOCKING, exc.getSeverity());
        assertEquals(WarehouseExceptionStatus.OPEN, exc.getStatus());

        // Pick completion should be blocked because unresolved blocking exception exists
        ResponseStatusException exBlocked = assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.completePickList(tenantA, pl.getId(), "WAREHOUSE_OPERATOR"));
        assertTrue(exBlocked.getReason().contains("blocking warehouse exceptions must be resolved"));

        // Manager resolves exception
        fulfillmentService.resolveException(tenantA, exc.getId(), null, "OPERATIONS_MANAGER");

        // Pick remaining quantities
        PickListItemDTO linenItem = pl.getItems().stream().filter(i -> i.getProductId().equals(linenProductId)).findFirst().orElseThrow();
        fulfillmentService.pickQuantity(tenantA, pl.getId(), chairItem.getId(), 95, null, "WAREHOUSE_OPERATOR");
        fulfillmentService.pickQuantity(tenantA, pl.getId(), linenItem.getId(), 20, null, "WAREHOUSE_OPERATOR");

        // Now completion succeeds
        PickListDTO completedPl = fulfillmentService.completePickList(tenantA, pl.getId(), "WAREHOUSE_OPERATOR");
        assertEquals(PickListStatus.COMPLETED, completedPl.getStatus());
    }

    @Test
    @DisplayName("6. Controlled Substitution Proposal & Approval")
    void testControlledSubstitutionFlow() {
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");

        // Propose replacing 5 Gold Chiavari Chairs with 5 White Folding Chairs
        ProposeSubstitutionRequestDTO req = new ProposeSubstitutionRequestDTO();
        req.setWarehouseOrderId(testWarehouseOrderId);
        req.setOriginalProductId(chairProductId);
        req.setReplacementProductId(foldingChairProductId);
        req.setOriginalQuantity(5);
        req.setReplacementQuantity(5);
        req.setReason("Gold chairs out of stock");

        WarehouseSubstitutionDTO sub = fulfillmentService.proposeSubstitution(tenantA, req, "WAREHOUSE_OPERATOR");
        assertEquals(SubstitutionStatus.PROPOSED, sub.getStatus());

        // Approve substitution
        WarehouseSubstitutionDTO approved = fulfillmentService.approveSubstitution(tenantA, sub.getId(), "OPERATIONS_MANAGER");
        assertEquals(SubstitutionStatus.APPROVED, approved.getStatus());
        assertEquals("OPERATIONS_MANAGER", approved.getApprovedBy());
    }

    @Test
    @DisplayName("7. Pack List, Container Allocation & Kit Verification")
    void testPackListAndKitVerification() {
        // Complete pick phase
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        for (PickListItemDTO item : pl.getItems()) {
            fulfillmentService.pickQuantity(tenantA, pl.getId(), item.getId(), item.getRequiredQuantity(), null, "WAREHOUSE_OPERATOR");
        }
        fulfillmentService.completePickList(tenantA, pl.getId(), "WAREHOUSE_OPERATOR");
        fulfillmentService.verifyPickList(tenantA, pl.getId(), null, "WAREHOUSE_MANAGER");

        // Generate Pack List
        PackListDTO packList = fulfillmentService.generatePackList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        assertNotNull(packList);
        assertEquals(2, packList.getItems().size());

        // Start packing
        fulfillmentService.startPacking(tenantA, packList.getId(), "WAREHOUSE_OPERATOR");

        // Pack linens into BAG-001
        PackListItemDTO linenPackItem = packList.getItems().stream().filter(i -> i.getProductId().equals(linenProductId)).findFirst().orElseThrow();
        fulfillmentService.packItem(tenantA, packList.getId(), linenPackItem.getId(), 20, "BAG-001", "Sealed in bag", "WAREHOUSE_OPERATOR");

        // Pack chairs into CART-001
        PackListItemDTO chairPackItem = packList.getItems().stream().filter(i -> i.getProductId().equals(chairProductId)).findFirst().orElseThrow();
        fulfillmentService.packItem(tenantA, packList.getId(), chairPackItem.getId(), 100, "CART-001", "Stacked on rolling cart", "WAREHOUSE_OPERATOR");

        // Complete packing
        PackListDTO completedPack = fulfillmentService.completePackList(tenantA, packList.getId(), "WAREHOUSE_OPERATOR");
        assertEquals(PackListStatus.COMPLETED, completedPack.getStatus());
        assertEquals(2, completedPack.getContainerCount());
    }

    @Test
    @DisplayName("8. Load List, Capacity Warning, Driver Handoff & Delivery Start Integration")
    void testLoadListDriverHandoffAndDeliveryStart() {
        // Setup through pack completed
        PickListDTO pl = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        for (PickListItemDTO item : pl.getItems()) {
            fulfillmentService.pickQuantity(tenantA, pl.getId(), item.getId(), item.getRequiredQuantity(), null, "WAREHOUSE_OPERATOR");
        }
        fulfillmentService.completePickList(tenantA, pl.getId(), "WAREHOUSE_OPERATOR");
        fulfillmentService.verifyPickList(tenantA, pl.getId(), null, "WAREHOUSE_MANAGER");

        PackListDTO packList = fulfillmentService.generatePackList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");
        for (PackListItemDTO pi : packList.getItems()) {
            fulfillmentService.packItem(tenantA, packList.getId(), pi.getId(), pi.getRequiredQuantity(), "CART-001", null, "WAREHOUSE_OPERATOR");
        }
        fulfillmentService.completePackList(tenantA, packList.getId(), "WAREHOUSE_OPERATOR");

        // Generate Load List
        LoadListDTO loadList = fulfillmentService.generateLoadList(tenantA, testWarehouseOrderId, testDeliveryId, testVehicleId, testDriverId, "WAREHOUSE_OPERATOR");
        assertNotNull(loadList);
        assertEquals(2, loadList.getItems().size());

        // Delivery cannot start yet because warehouse handoff is not complete
        assertThrows(ResponseStatusException.class, () ->
                deliveryService.startDelivery(tenantA, testDeliveryId, "Driver John"));

        // Load items
        fulfillmentService.startLoading(tenantA, loadList.getId(), "WAREHOUSE_OPERATOR");
        for (LoadListItemDTO li : loadList.getItems()) {
            fulfillmentService.loadItem(tenantA, loadList.getId(), li.getId(), li.getRequiredQuantity(), null, "WAREHOUSE_OPERATOR");
        }

        // Verify Load
        fulfillmentService.verifyLoad(tenantA, loadList.getId(), "WAREHOUSE_MANAGER");

        // Driver Handoff
        LoadHandoffRequestDTO handoffReq = new LoadHandoffRequestDTO();
        handoffReq.setDriverName("John Driver");
        handoffReq.setDriverNotes("All 100 chairs and 20 linens checked and secured.");
        LoadListDTO handedOff = fulfillmentService.driverHandoff(tenantA, loadList.getId(), handoffReq, "DRIVER");
        assertEquals(LoadListStatus.HANDED_OFF, handedOff.getStatus());

        // Warehouse Order is now HANDED_TO_DRIVER
        WarehouseOrder finalOrder = orderRepository.findByTenantIdAndId(tenantA, testWarehouseOrderId).orElseThrow();
        assertEquals(WarehouseOrderStatus.HANDED_TO_DRIVER, finalOrder.getStatus());

        // Now Delivery can start successfully!
        DeliveryDTO del = deliveryService.startDelivery(tenantA, testDeliveryId, "Driver John");
        assertEquals(com.rentflow.delivery.model.DeliveryStatus.OUT_FOR_DELIVERY, del.getStatus());
    }

    @Test
    @DisplayName("9. Tenant Isolation & Checklist Toggle")
    void testTenantIsolationAndChecklist() {
        PickListDTO plA = fulfillmentService.generatePickList(tenantA, testWarehouseOrderId, "WAREHOUSE_OPERATOR");

        // Tenant B cannot access Tenant A's pick list
        assertThrows(ResponseStatusException.class, () ->
                fulfillmentService.getPickListById(tenantB, plA.getId(), "WAREHOUSE_OPERATOR"));

        // Checklists for Tenant A
        List<WarehouseChecklistDTO> checklists = fulfillmentService.getOrderChecklists(tenantA, testWarehouseOrderId);
        assertFalse(checklists.isEmpty());

        WarehouseChecklistDTO toggled = fulfillmentService.toggleChecklistItem(tenantA, checklists.get(0).getId(), true, "WAREHOUSE_OPERATOR");
        assertTrue(toggled.isCompleted());
        assertEquals("WAREHOUSE_OPERATOR", toggled.getCompletedBy());
    }
}
