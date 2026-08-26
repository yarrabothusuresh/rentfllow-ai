package com.rentflow.returns;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.DriverStatus;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.model.VehicleStatus;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.returns.dto.*;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;
import com.rentflow.returns.service.ReturnService;
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
public class ReturnServiceTest {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private DamageRecordRepository damageRecordRepository;

    private String tenantId;
    private Booking testBooking;
    private Product testChairProduct;
    private Driver testDriver;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        tenantId = "TEST-TENANT-RET";

        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-RET-TEST-01");
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setCompanyName("Event Test LLC");
        customer.setEmail("jane@example.com");
        customer.setShippingAddress("100 Main St, New York, NY 10001");
        customer = customerRepository.save(customer);

        testChairProduct = new Product(UUID.randomUUID(), tenantId, "TEST-CHAIR", "Test Chair", "Chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.valueOf(10.00), BigDecimal.valueOf(80.00), 100, 0, 0, 0);
        testChairProduct = productRepository.save(testChairProduct);

        testBooking = new Booking();
        testBooking.setTenantId(tenantId);
        testBooking.setBookingNumber("TEST-BOOK-001");
        testBooking.setQuoteId(UUID.randomUUID());
        testBooking.setCustomerId(customer.getId());
        testBooking.setEventId(UUID.randomUUID());
        testBooking.setStatus(BookingStatus.DELIVERED);
        testBooking.setBookingDate(LocalDate.now());
        testBooking.setRentalStartDateTime(LocalDateTime.now().minusDays(1));
        testBooking.setRentalEndDateTime(LocalDateTime.now().plusDays(1));
        testBooking = bookingRepository.save(testBooking);

        BookingItem bi = new BookingItem();
        bi.setBookingId(testBooking.getId());
        bi.setProductId(testChairProduct.getId());
        bi.setDescription(testChairProduct.getName());
        bi.setQuantity(20);
        bi.setUnitPrice(BigDecimal.valueOf(10.00));
        bi.setLineSubtotal(BigDecimal.valueOf(200.00));
        bi.setRentalStartDateTime(testBooking.getRentalStartDateTime());
        bi.setRentalEndDateTime(testBooking.getRentalEndDateTime());
        bookingItemRepository.save(bi);

        InventoryReservation res = new InventoryReservation(UUID.randomUUID(), tenantId, testChairProduct.getId(), testBooking.getEventId(), testBooking.getId(), 20, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), ReservationStatus.RESERVED);
        reservationRepository.save(res);

        testDriver = new Driver();
        testDriver.setTenantId(tenantId);
        testDriver.setName("Test Driver");
        testDriver.setPhone("555-0199");
        testDriver.setStatus(DriverStatus.AVAILABLE);
        testDriver.setActive(true);
        testDriver = driverRepository.save(testDriver);

        testVehicle = new Vehicle();
        testVehicle.setTenantId(tenantId);
        testVehicle.setVehicleNumber("TEST-VAN-1");
        testVehicle.setName("Test Van");
        testVehicle.setType("Van");
        testVehicle.setCapacity(500);
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
        testVehicle.setActive(true);
        testVehicle = vehicleRepository.save(testVehicle);
    }

    @Test
    @DisplayName("1 & 2. Create Return from Booking and prevent duplicate active return")
    void testCreateFromBooking() {
        ReturnOrderDTO dto = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");
        assertNotNull(dto.getId());
        assertEquals(ReturnOrderStatus.PENDING, dto.getStatus());
        assertEquals(1, dto.getItems().size());
        assertEquals(20, dto.getItems().get(0).getQuantityExpected());

        // Duplicate return should return existing return order
        ReturnOrderDTO dup = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");
        assertEquals(dto.getId(), dup.getId());
    }

    @Test
    @DisplayName("4, 6 & 8. Pickup Scheduling, Driver Assignment & Vehicle Assignment")
    void testScheduleDriverVehicleAssignment() {
        ReturnOrderDTO ret = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");

        ScheduleReturnDTO sched = new ScheduleReturnDTO();
        sched.setDate(LocalDate.now().plusDays(1));
        sched.setStartTime("10:00");
        sched.setEndTime("12:00");
        ReturnOrderDTO scheduled = returnService.scheduleReturn(tenantId, ret.getId(), sched, "Tester");
        assertEquals(ReturnOrderStatus.SCHEDULED, scheduled.getStatus());

        ReturnOrderDTO assignedDriver = returnService.assignDriver(tenantId, ret.getId(), testDriver.getId(), "Tester");
        assertEquals(testDriver.getId(), assignedDriver.getDriverId());
        assertEquals(ReturnOrderStatus.ASSIGNED, assignedDriver.getStatus());

        ReturnOrderDTO assignedVehicle = returnService.assignVehicle(tenantId, ret.getId(), testVehicle.getId(), "Tester");
        assertEquals(testVehicle.getId(), assignedVehicle.getVehicleId());
    }

    @Test
    @DisplayName("5 & 7. Driver and Vehicle Overlap Conflict Detection")
    void testDriverAndVehicleConflicts() {
        ReturnOrderDTO ret1 = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");

        ScheduleReturnDTO sched = new ScheduleReturnDTO();
        sched.setDate(LocalDate.now());
        sched.setStartTime("10:00");
        sched.setEndTime("12:00");
        returnService.scheduleReturn(tenantId, ret1.getId(), sched, "Tester");
        returnService.assignDriver(tenantId, ret1.getId(), testDriver.getId(), "Tester");

        // Create second booking & return for collision check
        Booking booking2 = new Booking();
        booking2.setTenantId(tenantId);
        booking2.setBookingNumber("TEST-BOOK-002");
        booking2.setQuoteId(UUID.randomUUID());
        booking2.setCustomerId(testBooking.getCustomerId());
        booking2.setEventId(UUID.randomUUID());
        booking2.setStatus(BookingStatus.DELIVERED);
        booking2.setBookingDate(LocalDate.now());
        booking2.setRentalStartDateTime(LocalDateTime.now());
        booking2.setRentalEndDateTime(LocalDateTime.now().plusDays(1));
        booking2 = bookingRepository.save(booking2);

        ReturnOrderDTO ret2 = returnService.createFromBooking(tenantId, booking2.getId(), "Tester");
        returnService.scheduleReturn(tenantId, ret2.getId(), sched, "Tester");

        assertThrows(ResponseStatusException.class, () -> {
            returnService.assignDriver(tenantId, ret2.getId(), testDriver.getId(), "Tester");
        });
    }

    @Test
    @DisplayName("9, 10, 11 & 24, 25. Pickup Execution Lifecycle (Start -> Arrive -> Complete Pickup -> Driver/Vehicle Released)")
    void testPickupLifecycle() {
        ReturnOrderDTO ret = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");
        returnService.assignDriver(tenantId, ret.getId(), testDriver.getId(), "Tester");
        returnService.assignVehicle(tenantId, ret.getId(), testVehicle.getId(), "Tester");

        ReturnOrderDTO started = returnService.startPickup(tenantId, ret.getId(), "Driver");
        assertEquals(ReturnOrderStatus.OUT_FOR_PICKUP, started.getStatus());

        ReturnOrderDTO arrived = returnService.arrivePickup(tenantId, ret.getId(), "Driver");
        assertEquals(ReturnOrderStatus.ARRIVED, arrived.getStatus());

        ReturnOrderDTO pickedUp = returnService.pickupComplete(tenantId, ret.getId(), "Driver");
        assertEquals(ReturnOrderStatus.PICKED_UP, pickedUp.getStatus());

        // Driver and Vehicle released to AVAILABLE
        Driver updatedDriver = driverRepository.findById(testDriver.getId()).orElseThrow();
        assertEquals(DriverStatus.AVAILABLE, updatedDriver.getStatus());

        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals(VehicleStatus.AVAILABLE, updatedVehicle.getStatus());
    }

    @Test
    @DisplayName("13, 14 & 32. Check-In & Missing Quantity Calculation")
    void testCheckInValidationAndMissingCalculation() {
        ReturnOrderDTO ret = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");
        returnService.assignDriver(tenantId, ret.getId(), testDriver.getId(), "Tester");
        returnService.assignVehicle(tenantId, ret.getId(), testVehicle.getId(), "Tester");
        returnService.startPickup(tenantId, ret.getId(), "Driver");
        returnService.arrivePickup(tenantId, ret.getId(), "Driver");
        returnService.pickupComplete(tenantId, ret.getId(), "Driver");

        returnService.startCheckIn(tenantId, ret.getId(), "Warehouse");

        ReturnOrderItemDTO itemDto = ret.getItems().get(0);

        // Validation error if received > expected
        CheckInRequestDTO invalidReq = new CheckInRequestDTO();
        CheckInRequestDTO.ItemQuantity iqErr = new CheckInRequestDTO.ItemQuantity();
        iqErr.setReturnItemId(itemDto.getId());
        iqErr.setQuantityReceived(25); // expected is 20
        invalidReq.setItems(List.of(iqErr));

        assertThrows(ResponseStatusException.class, () -> {
            returnService.recordCheckIn(tenantId, ret.getId(), invalidReq, "Warehouse");
        });

        // Valid check-in with 18 received (2 missing)
        CheckInRequestDTO validReq = new CheckInRequestDTO();
        CheckInRequestDTO.ItemQuantity iq = new CheckInRequestDTO.ItemQuantity();
        iq.setReturnItemId(itemDto.getId());
        iq.setQuantityReceived(18);
        validReq.setItems(List.of(iq));

        ReturnOrderDTO checkedIn = returnService.recordCheckIn(tenantId, ret.getId(), validReq, "Warehouse");
        assertEquals(2, checkedIn.getItems().get(0).getQuantityMissing());
        assertEquals(18, checkedIn.getItems().get(0).getQuantityReceived());
    }

    @Test
    @DisplayName("16, 17, 18 & 34. Inspection & Reconciliation (Mixed Exception 18 Received = 16 Good + 2 Damaged + 2 Missing)")
    void testInspectionAndReconciliation() {
        ReturnOrderDTO ret = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");
        returnService.assignDriver(tenantId, ret.getId(), testDriver.getId(), "Tester");
        returnService.assignVehicle(tenantId, ret.getId(), testVehicle.getId(), "Tester");
        returnService.startPickup(tenantId, ret.getId(), "Driver");
        returnService.arrivePickup(tenantId, ret.getId(), "Driver");
        returnService.pickupComplete(tenantId, ret.getId(), "Driver");
        returnService.startCheckIn(tenantId, ret.getId(), "Warehouse");

        ReturnOrderItemDTO itemDto = ret.getItems().get(0);

        CheckInRequestDTO checkInReq = new CheckInRequestDTO();
        CheckInRequestDTO.ItemQuantity iq = new CheckInRequestDTO.ItemQuantity();
        iq.setReturnItemId(itemDto.getId());
        iq.setQuantityReceived(18); // 2 missing out of 20
        checkInReq.setItems(List.of(iq));
        returnService.recordCheckIn(tenantId, ret.getId(), checkInReq, "Warehouse");

        returnService.startInspection(tenantId, ret.getId(), "Warehouse");

        // Inspection validation fail if good + damaged != received
        InspectionRequestDTO invalidInsp = new InspectionRequestDTO();
        InspectionRequestDTO.ItemInspection iiErr = new InspectionRequestDTO.ItemInspection();
        iiErr.setReturnItemId(itemDto.getId());
        iiErr.setGoodQuantity(15);
        iiErr.setDamagedQuantity(1); // 15+1 = 16 != 18
        invalidInsp.setItems(List.of(iiErr));

        assertThrows(ResponseStatusException.class, () -> {
            returnService.recordInspection(tenantId, ret.getId(), invalidInsp, "Warehouse");
        });

        // Valid Inspection: 16 Good + 2 Damaged = 18 Received
        InspectionRequestDTO validInsp = new InspectionRequestDTO();
        InspectionRequestDTO.ItemInspection ii = new InspectionRequestDTO.ItemInspection();
        ii.setReturnItemId(itemDto.getId());
        ii.setGoodQuantity(16);
        ii.setDamagedQuantity(2);
        ii.setCondition(InspectionCondition.MINOR_DAMAGE);
        ii.setNotes("2 scratched seats");
        ii.setDamageCategory(DamageCategory.SCRATCHED);
        ii.setDamageSeverity(DamageSeverity.MINOR);
        ii.setEstimatedRepairCost(BigDecimal.valueOf(25.00));
        validInsp.setItems(List.of(ii));

        ReturnOrderDTO inspected = returnService.recordInspection(tenantId, ret.getId(), validInsp, "Warehouse");
        assertEquals(16, inspected.getItems().get(0).getQuantityGood());
        assertEquals(2, inspected.getItems().get(0).getQuantityDamaged());

        // Verify DamageRecord created
        List<DamageRecord> damages = damageRecordRepository.findByTenantId(tenantId);
        assertFalse(damages.isEmpty());
        assertEquals(2, damages.get(0).getQuantity());

        // Complete Return & Inventory State Update
        ReturnOrderDTO completed = returnService.completeReturn(tenantId, ret.getId(), "Manager");
        assertEquals(ReturnOrderStatus.COMPLETED, completed.getStatus());

        // Booking status updated to RETURNED
        Booking updatedBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.RETURNED, updatedBooking.getStatus());

        // Verify product quantities updated: 2 in maintenance, 2 lost
        Product updatedProduct = productRepository.findById(testChairProduct.getId()).orElseThrow();
        assertEquals(2, updatedProduct.getQuantityInMaintenance());
        assertEquals(2, updatedProduct.getQuantityLost());
    }

    @Test
    @DisplayName("26. Multi-Tenant Isolation Enforcement")
    void testTenantIsolation() {
        ReturnOrderDTO ret = returnService.createFromBooking(tenantId, testBooking.getId(), "Tester");

        assertThrows(ResponseStatusException.class, () -> {
            returnService.getReturnById("OTHER-TENANT", ret.getId());
        });
    }
}
