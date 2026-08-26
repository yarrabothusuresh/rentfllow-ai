package com.rentflow.returns.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;
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
@Order(7)
public class ReturnDataInitializer implements CommandLineRunner {

    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnOrderItemRepository returnOrderItemRepository;
    private final InspectionRepository inspectionRepository;
    private final DamageRecordRepository damageRecordRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public ReturnDataInitializer(
            ReturnOrderRepository returnOrderRepository,
            ReturnOrderItemRepository returnOrderItemRepository,
            InspectionRepository inspectionRepository,
            DamageRecordRepository damageRecordRepository,
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            CustomerRepository customerRepository,
            EventRepository eventRepository,
            ProductRepository productRepository,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository) {
        this.returnOrderRepository = returnOrderRepository;
        this.returnOrderItemRepository = returnOrderItemRepository;
        this.inspectionRepository = inspectionRepository;
        this.damageRecordRepository = damageRecordRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.productRepository = productRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (returnOrderRepository.countByTenantId(tenantId) > 0) {
            return;
        }

        // Find or create customer
        List<Customer> customers = customerRepository.findByTenantId(tenantId);
        Customer customer = customers.stream().filter(c -> "ABC Events LLC".equalsIgnoreCase(c.getCompanyName())).findFirst().orElse(null);
        if (customer == null && !customers.isEmpty()) {
            customer = customers.get(0);
        }

        // Find or create products
        List<Product> products = productRepository.findByTenantId(tenantId);
        Product chairProduct = products.stream().filter(p -> "SKU-RET-CHAIR-01".equals(p.getSku())).findFirst().orElse(null);
        Product tableProduct = products.stream().filter(p -> "SKU-RET-TBL-60".equals(p.getSku())).findFirst().orElse(null);
        Product linenProduct = products.stream().filter(p -> "SKU-RET-LIN-WHT".equals(p.getSku())).findFirst().orElse(null);

        if (chairProduct == null) {
            chairProduct = new Product(UUID.randomUUID(), tenantId, "SKU-RET-CHAIR-01", "Chiavari Chair (Return Demo)", "White Chiavari Chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.valueOf(10.00), BigDecimal.valueOf(75.00), 200, 0, 0, 0);
            productRepository.save(chairProduct);
        }
        if (tableProduct == null) {
            tableProduct = new Product(UUID.randomUUID(), tenantId, "SKU-RET-TBL-60", "Round Table (Return Demo)", "60 inch Round Banquet Table", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.valueOf(25.00), BigDecimal.valueOf(250.00), 50, 0, 0, 0);
            productRepository.save(tableProduct);
        }
        if (linenProduct == null) {
            linenProduct = new Product(UUID.randomUUID(), tenantId, "SKU-RET-LIN-WHT", "White Linen (Return Demo)", "Standard White Tablecloth", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.valueOf(15.00), BigDecimal.valueOf(45.00), 100, 0, 0, 0);
            productRepository.save(linenProduct);
        }

        final UUID demoCustomerId = customer != null ? customer.getId() : UUID.randomUUID();

        // Create Demo Event
        Event event = new Event();
        event.setTenantId(tenantId);
        event.setEventName("Wedding Reception");
        event.setEventType(EventType.WEDDING);
        event.setEventDate(LocalDate.now());
        event.setGuestCount(100);
        event.setVenueName("Grand Ballroom");
        event.setVenueAddress("500 Grand Ave");
        event.setCity("New York");
        event.setState("NY");
        event.setZipCode("10001");
        event.setStatus(EventStatus.COMPLETED);
        event.setCustomerId(demoCustomerId);
        Event savedEvent = eventRepository.save(event);

        // Find Driver and Vehicle
        List<Driver> drivers = driverRepository.findByTenantId(tenantId);
        Driver driver = !drivers.isEmpty() ? drivers.get(0) : null;

        List<Vehicle> vehicles = vehicleRepository.findByTenantId(tenantId);
        Vehicle vehicle = !vehicles.isEmpty() ? vehicles.get(0) : null;

        // ====================================================
        // STEP 47 DEMO DATA: RET-000123
        // Booking BOOK-000123: 100 Chairs, 10 Tables, 20 Linens
        // Return RET-000123: Received 100 Chairs, 10 Tables, 18 Linens (2 Missing)
        // Inspection: 98 Good Chairs, 2 Damaged Chairs; 10 Good Tables; 18 Good Linens
        // ====================================================

        Booking savedBooking1 = bookingRepository.findByBookingNumber("BOOK-000123").orElseGet(() -> {
            Booking b = new Booking();
            b.setTenantId(tenantId);
            b.setBookingNumber("BOOK-000123");
            b.setQuoteId(UUID.randomUUID());
            b.setCustomerId(demoCustomerId);
            b.setEventId(savedEvent.getId());
            b.setStatus(BookingStatus.RETURNED);
            b.setBookingDate(LocalDate.now().minusDays(5));
            b.setRentalStartDateTime(LocalDateTime.now().minusDays(3));
            b.setRentalEndDateTime(LocalDateTime.now().minusDays(1));
            return bookingRepository.save(b);
        });

        BookingItem bi1 = new BookingItem();
        bi1.setBookingId(savedBooking1.getId());
        bi1.setProductId(chairProduct.getId());
        bi1.setDescription(chairProduct.getName());
        bi1.setQuantity(100);
        bi1.setUnitPrice(BigDecimal.valueOf(10.00));
        bi1.setLineSubtotal(BigDecimal.valueOf(1000.00));
        bi1.setRentalStartDateTime(savedBooking1.getRentalStartDateTime());
        bi1.setRentalEndDateTime(savedBooking1.getRentalEndDateTime());

        BookingItem bi2 = new BookingItem();
        bi2.setBookingId(savedBooking1.getId());
        bi2.setProductId(tableProduct.getId());
        bi2.setDescription(tableProduct.getName());
        bi2.setQuantity(10);
        bi2.setUnitPrice(BigDecimal.valueOf(25.00));
        bi2.setLineSubtotal(BigDecimal.valueOf(250.00));
        bi2.setRentalStartDateTime(savedBooking1.getRentalStartDateTime());
        bi2.setRentalEndDateTime(savedBooking1.getRentalEndDateTime());

        BookingItem bi3 = new BookingItem();
        bi3.setBookingId(savedBooking1.getId());
        bi3.setProductId(linenProduct.getId());
        bi3.setDescription(linenProduct.getName());
        bi3.setQuantity(20);
        bi3.setUnitPrice(BigDecimal.valueOf(15.00));
        bi3.setLineSubtotal(BigDecimal.valueOf(300.00));
        bi3.setRentalStartDateTime(savedBooking1.getRentalStartDateTime());
        bi3.setRentalEndDateTime(savedBooking1.getRentalEndDateTime());

        bookingItemRepository.saveAll(List.of(bi1, bi2, bi3));

        ReturnOrder ret1 = new ReturnOrder();
        ret1.setTenantId(tenantId);
        ret1.setReturnNumber("RET-000123");
        ret1.setBookingId(savedBooking1.getId());
        ret1.setCustomerId(savedBooking1.getCustomerId());
        ret1.setEventId(savedBooking1.getEventId());
        ret1.setStatus(ReturnOrderStatus.COMPLETED);
        ret1.setPriority(ReturnPriority.NORMAL);
        ret1.setScheduledDate(LocalDate.now());
        ret1.setScheduledStartTime("10:00");
        ret1.setScheduledEndTime("12:00");
        ret1.setPickupAddressSnapshot("Grand Ballroom, 500 Grand Ave, New York, NY 10001");
        if (driver != null) ret1.setDriverId(driver.getId());
        if (vehicle != null) ret1.setVehicleId(vehicle.getId());
        ret1.setActualPickupStartTime(LocalDateTime.now().minusHours(4));
        ret1.setActualArrivalTime(LocalDateTime.now().minusHours(3));
        ret1.setActualPickupTime(LocalDateTime.now().minusHours(2));
        ret1.setActualCheckInTime(LocalDateTime.now().minusHours(1));
        ret1.setActualInspectionTime(LocalDateTime.now().minusMinutes(30));
        ret1.setCompletedAt(LocalDateTime.now().minusMinutes(10));
        ReturnOrder savedRet1 = returnOrderRepository.save(ret1);

        ReturnOrderItem roi1 = new ReturnOrderItem();
        roi1.setTenantId(tenantId);
        roi1.setReturnOrderId(savedRet1.getId());
        roi1.setBookingItemId(bi1.getId());
        roi1.setProductId(chairProduct.getId());
        roi1.setProductNameSnapshot(chairProduct.getName());
        roi1.setSkuSnapshot(chairProduct.getSku());
        roi1.setQuantityExpected(100);
        roi1.setQuantityReceived(100);
        roi1.setQuantityMissing(0);
        roi1.setQuantityDamaged(2);
        roi1.setQuantityGood(98);
        roi1.setStatus(ReturnOrderItemStatus.INSPECTED);
        returnOrderItemRepository.save(roi1);

        ReturnOrderItem roi2 = new ReturnOrderItem();
        roi2.setTenantId(tenantId);
        roi2.setReturnOrderId(savedRet1.getId());
        roi2.setBookingItemId(bi2.getId());
        roi2.setProductId(tableProduct.getId());
        roi2.setProductNameSnapshot(tableProduct.getName());
        roi2.setSkuSnapshot(tableProduct.getSku());
        roi2.setQuantityExpected(10);
        roi2.setQuantityReceived(10);
        roi2.setQuantityMissing(0);
        roi2.setQuantityDamaged(0);
        roi2.setQuantityGood(10);
        roi2.setStatus(ReturnOrderItemStatus.INSPECTED);
        returnOrderItemRepository.save(roi2);

        ReturnOrderItem roi3 = new ReturnOrderItem();
        roi3.setTenantId(tenantId);
        roi3.setReturnOrderId(savedRet1.getId());
        roi3.setBookingItemId(bi3.getId());
        roi3.setProductId(linenProduct.getId());
        roi3.setProductNameSnapshot(linenProduct.getName());
        roi3.setSkuSnapshot(linenProduct.getSku());
        roi3.setQuantityExpected(20);
        roi3.setQuantityReceived(18);
        roi3.setQuantityMissing(2);
        roi3.setQuantityDamaged(0);
        roi3.setQuantityGood(18);
        roi3.setStatus(ReturnOrderItemStatus.MISSING);
        returnOrderItemRepository.save(roi3);

        // Inspection for chairs
        Inspection insp1 = new Inspection();
        insp1.setTenantId(tenantId);
        insp1.setReturnOrderId(savedRet1.getId());
        insp1.setReturnOrderItemId(roi1.getId());
        insp1.setProductId(chairProduct.getId());
        insp1.setInspectedQuantity(100);
        insp1.setGoodQuantity(98);
        insp1.setDamagedQuantity(2);
        insp1.setMissingQuantity(0);
        insp1.setCondition(InspectionCondition.MINOR_DAMAGE);
        insp1.setNotes("2 chairs have surface scratches on legs");
        insp1.setInspectedBy("Warehouse Tech");
        Inspection savedInsp1 = inspectionRepository.save(insp1);

        DamageRecord dr1 = new DamageRecord();
        dr1.setTenantId(tenantId);
        dr1.setInspectionId(savedInsp1.getId());
        dr1.setReturnItemId(roi1.getId());
        dr1.setProductId(chairProduct.getId());
        dr1.setQuantity(2);
        dr1.setCategory(DamageCategory.SCRATCHED);
        dr1.setSeverity(DamageSeverity.MINOR);
        dr1.setDescription("Scratched wooden leg finish");
        dr1.setEstimatedRepairCost(BigDecimal.valueOf(30.00));
        dr1.setEstimatedReplacementCost(BigDecimal.valueOf(150.00));
        dr1.setStatus("OPEN");
        damageRecordRepository.save(dr1);

        // Update product counts for Step 47
        chairProduct.setQuantityInMaintenance(chairProduct.getQuantityInMaintenance() + 2);
        productRepository.save(chairProduct);

        linenProduct.setQuantityLost(linenProduct.getQuantityLost() + 2);
        productRepository.save(linenProduct);

        // ====================================================
        // STEP 48 MIXED EXCEPTION DEMO DATA: RET-000124
        // 100 Chairs Expected, 95 Received, 90 Good, 5 Damaged, 5 Missing
        // System result: 90 AVAILABLE, 5 DAMAGED, 5 LOST
        // ====================================================

        Booking savedBooking2 = bookingRepository.findByBookingNumber("BOOK-000124").orElseGet(() -> {
            Booking b = new Booking();
            b.setTenantId(tenantId);
            b.setBookingNumber("BOOK-000124");
            b.setQuoteId(UUID.randomUUID());
            b.setCustomerId(demoCustomerId);
            b.setEventId(savedEvent.getId());
            b.setStatus(BookingStatus.INSPECTING);
            b.setBookingDate(LocalDate.now().minusDays(3));
            b.setRentalStartDateTime(LocalDateTime.now().minusDays(2));
            b.setRentalEndDateTime(LocalDateTime.now());
            return bookingRepository.save(b);
        });

        BookingItem bi4 = new BookingItem();
        bi4.setBookingId(savedBooking2.getId());
        bi4.setProductId(chairProduct.getId());
        bi4.setDescription(chairProduct.getName());
        bi4.setQuantity(100);
        bi4.setUnitPrice(BigDecimal.valueOf(10.00));
        bi4.setLineSubtotal(BigDecimal.valueOf(1000.00));
        bi4.setRentalStartDateTime(savedBooking2.getRentalStartDateTime());
        bi4.setRentalEndDateTime(savedBooking2.getRentalEndDateTime());
        bookingItemRepository.save(bi4);

        ReturnOrder ret2 = new ReturnOrder();
        ret2.setTenantId(tenantId);
        ret2.setReturnNumber("RET-000124");
        ret2.setBookingId(savedBooking2.getId());
        ret2.setCustomerId(savedBooking2.getCustomerId());
        ret2.setEventId(savedBooking2.getEventId());
        ret2.setStatus(ReturnOrderStatus.INSPECTION);
        ret2.setPriority(ReturnPriority.HIGH);
        ret2.setScheduledDate(LocalDate.now());
        ret2.setScheduledStartTime("14:00");
        ret2.setScheduledEndTime("16:00");
        ret2.setPickupAddressSnapshot("Grand Ballroom, 500 Grand Ave, New York, NY 10001");
        if (driver != null) ret2.setDriverId(driver.getId());
        if (vehicle != null) ret2.setVehicleId(vehicle.getId());
        ret2.setActualPickupStartTime(LocalDateTime.now().minusHours(2));
        ret2.setActualArrivalTime(LocalDateTime.now().minusHours(1));
        ret2.setActualPickupTime(LocalDateTime.now().minusMinutes(45));
        ret2.setActualCheckInTime(LocalDateTime.now().minusMinutes(30));
        ret2.setActualInspectionTime(LocalDateTime.now().minusMinutes(15));
        ReturnOrder savedRet2 = returnOrderRepository.save(ret2);

        ReturnOrderItem roi4 = new ReturnOrderItem();
        roi4.setTenantId(tenantId);
        roi4.setReturnOrderId(savedRet2.getId());
        roi4.setBookingItemId(bi4.getId());
        roi4.setProductId(chairProduct.getId());
        roi4.setProductNameSnapshot(chairProduct.getName());
        roi4.setSkuSnapshot(chairProduct.getSku());
        roi4.setQuantityExpected(100);
        roi4.setQuantityReceived(95);
        roi4.setQuantityMissing(5);
        roi4.setQuantityDamaged(5);
        roi4.setQuantityGood(90);
        roi4.setStatus(ReturnOrderItemStatus.DAMAGED);
        returnOrderItemRepository.save(roi4);

        Inspection insp2 = new Inspection();
        insp2.setTenantId(tenantId);
        insp2.setReturnOrderId(savedRet2.getId());
        insp2.setReturnOrderItemId(roi4.getId());
        insp2.setProductId(chairProduct.getId());
        insp2.setInspectedQuantity(95);
        insp2.setGoodQuantity(90);
        insp2.setDamagedQuantity(5);
        insp2.setMissingQuantity(5);
        insp2.setCondition(InspectionCondition.MAJOR_DAMAGE);
        insp2.setNotes("5 broken seats, 5 chairs missing from venue");
        insp2.setInspectedBy("Warehouse Lead");
        Inspection savedInsp2 = inspectionRepository.save(insp2);

        DamageRecord dr2 = new DamageRecord();
        dr2.setTenantId(tenantId);
        dr2.setInspectionId(savedInsp2.getId());
        dr2.setReturnItemId(roi4.getId());
        dr2.setProductId(chairProduct.getId());
        dr2.setQuantity(5);
        dr2.setCategory(DamageCategory.BROKEN);
        dr2.setSeverity(DamageSeverity.MAJOR);
        dr2.setDescription("Broken frame components on 5 chairs");
        dr2.setEstimatedRepairCost(BigDecimal.valueOf(125.00));
        dr2.setEstimatedReplacementCost(BigDecimal.valueOf(375.00));
        dr2.setStatus("OPEN");
        damageRecordRepository.save(dr2);
    }
}
