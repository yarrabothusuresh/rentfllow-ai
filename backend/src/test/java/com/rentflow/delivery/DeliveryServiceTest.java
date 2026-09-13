package com.rentflow.delivery;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.delivery.dto.*;
import com.rentflow.delivery.model.*;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.delivery.service.DeliveryDataInitializer;
import com.rentflow.delivery.service.DeliveryService;
import com.rentflow.warehouse.service.WarehouseDataInitializer;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private WarehouseOrderRepository warehouseOrderRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private com.rentflow.security.TestJwtFactory testJwtFactory;

    private String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

    private HttpHeaders createHeaders(String role, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        String effTenant = tenant != null ? tenant : tenantId;
        String effRole = role != null ? role : "OWNER";
        headers.set("Authorization", "Bearer " + testJwtFactory.createStaffToken(effTenant, effRole));
        return headers;
    }



    @Test
    void test01_DeliveryCreatedFromWarehouseOrder() {
        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(tenantId);
        order.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        order.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        order.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        order.setOrderNumber("WO-TST-001");
        order.setStatus(WarehouseOrderStatus.PACKED);

        WarehouseOrder savedOrder = warehouseOrderRepository.saveAndFlush(order);

        DeliveryDTO delivery = deliveryService.createFromWarehouseOrder(tenantId, savedOrder.getId(), "Test User");

        assertNotNull(delivery);
        assertNotNull(delivery.getId());
        assertTrue(delivery.getDeliveryNumber().startsWith("DEL-"));
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
    }

    @Test
    void test02_DeliveryRequiresWarehouseOrderToBeReady() {
        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(tenantId);
        order.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        order.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        order.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        order.setOrderNumber("WO-TST-002");
        order.setStatus(WarehouseOrderStatus.PICKING); // Not packed or ready

        WarehouseOrder savedOrder = warehouseOrderRepository.saveAndFlush(order);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            deliveryService.createFromWarehouseOrder(tenantId, savedOrder.getId(), "Test User");
        });

        assertTrue(ex.getReason().contains("Delivery cannot be created until warehouse preparation is complete."));
    }

    @Test
    void test03_DuplicateDeliveryPrevented() {
        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(tenantId);
        order.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        order.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        order.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        order.setOrderNumber("WO-TST-003");
        order.setStatus(WarehouseOrderStatus.READY_FOR_DELIVERY);

        WarehouseOrder savedOrder = warehouseOrderRepository.saveAndFlush(order);

        DeliveryDTO del1 = deliveryService.createFromWarehouseOrder(tenantId, savedOrder.getId(), "User 1");
        DeliveryDTO del2 = deliveryService.createFromWarehouseOrder(tenantId, savedOrder.getId(), "User 2");

        assertEquals(del1.getId(), del2.getId());
    }

    @Test
    void test04_DriverAssignmentWorks() {
        DeliveryDTO del = deliveryService.getDeliveryById(tenantId, DeliveryDataInitializer.DEMO_DELIVERY_3_ID);
        Driver john = driverRepository.findByTenantIdAndId(tenantId, DeliveryDataInitializer.DRIVER_JOHN_ID).orElseThrow();

        DeliveryDTO assigned = deliveryService.assignDriver(tenantId, del.getId(), john.getId(), "Manager");

        assertEquals(john.getId(), assigned.getDriverId());
    }

    @Test
    void test05_DriverCannotBeDoubleBooked() {
        LocalDate testDate = LocalDate.of(2026, 9, 15);

        Delivery d1 = new Delivery();
        d1.setTenantId(tenantId);
        d1.setDeliveryNumber("DEL-DBL-001");
        d1.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d1.setWarehouseOrderId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        d1.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d1.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d1.setStatus(DeliveryStatus.SCHEDULED);
        d1.setScheduledDate(testDate);
        d1.setScheduledStartTime("10:00");
        d1.setScheduledEndTime("12:00");
        d1.setDriverId(DeliveryDataInitializer.DRIVER_JOHN_ID);
        deliveryRepository.saveAndFlush(d1);

        Delivery d2 = new Delivery();
        d2.setTenantId(tenantId);
        d2.setDeliveryNumber("DEL-DBL-002");
        d2.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d2.setWarehouseOrderId(UUID.fromString("22222222-2222-2222-2222-222222222124"));
        d2.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d2.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d2.setStatus(DeliveryStatus.SCHEDULED);
        d2.setScheduledDate(testDate);
        d2.setScheduledStartTime("11:00");
        d2.setScheduledEndTime("13:00");
        deliveryRepository.saveAndFlush(d2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            deliveryService.assignDriver(tenantId, d2.getId(), DeliveryDataInitializer.DRIVER_JOHN_ID, "Manager");
        });

        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getReason();
        assertTrue(msg != null && msg.contains("already assigned"));
    }

    @Test
    void test06_VehicleAssignmentWorks() {
        DeliveryDTO del = deliveryService.getDeliveryById(tenantId, DeliveryDataInitializer.DEMO_DELIVERY_3_ID);
        Vehicle van = vehicleRepository.findByTenantIdAndId(tenantId, DeliveryDataInitializer.VEHICLE_VAN01_ID).orElseThrow();

        DeliveryDTO assigned = deliveryService.assignVehicle(tenantId, del.getId(), van.getId(), "Manager");

        assertEquals(van.getId(), assigned.getVehicleId());
    }

    @Test
    void test07_VehicleCannotBeDoubleBooked() {
        LocalDate testDate = LocalDate.of(2026, 9, 20);

        Delivery d1 = new Delivery();
        d1.setTenantId(tenantId);
        d1.setDeliveryNumber("DEL-VDBL-001");
        d1.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d1.setWarehouseOrderId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        d1.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d1.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d1.setStatus(DeliveryStatus.SCHEDULED);
        d1.setScheduledDate(testDate);
        d1.setScheduledStartTime("10:00");
        d1.setScheduledEndTime("12:00");
        d1.setVehicleId(DeliveryDataInitializer.VEHICLE_VAN01_ID);
        deliveryRepository.saveAndFlush(d1);

        Delivery d2 = new Delivery();
        d2.setTenantId(tenantId);
        d2.setDeliveryNumber("DEL-VDBL-002");
        d2.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d2.setWarehouseOrderId(UUID.fromString("22222222-2222-2222-2222-222222222124"));
        d2.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d2.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d2.setStatus(DeliveryStatus.SCHEDULED);
        d2.setScheduledDate(testDate);
        d2.setScheduledStartTime("11:00");
        d2.setScheduledEndTime("13:00");
        deliveryRepository.saveAndFlush(d2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            deliveryService.assignVehicle(tenantId, d2.getId(), DeliveryDataInitializer.VEHICLE_VAN01_ID, "Manager");
        });

        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getReason();
        assertTrue(msg != null && msg.contains("already assigned"));
    }

    @Test
    void test08_DeliverySchedulingWorks() {
        DeliveryDTO del = deliveryService.getDeliveryById(tenantId, DeliveryDataInitializer.DEMO_DELIVERY_3_ID);
        ScheduleDeliveryDTO req = new ScheduleDeliveryDTO(LocalDate.of(2026, 9, 1), "09:00", "11:00");

        DeliveryDTO scheduled = deliveryService.scheduleDelivery(tenantId, del.getId(), req, "Manager");

        assertEquals(DeliveryStatus.SCHEDULED, scheduled.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), scheduled.getScheduledDate());
        assertEquals("09:00", scheduled.getScheduledStartTime());
    }

    @Test
    void test09_StartDeliveryRequiresDriverAndVehicle() {
        Delivery d = new Delivery();
        d.setTenantId(tenantId);
        d.setDeliveryNumber("DEL-NODRV-001");
        d.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d.setWarehouseOrderId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        d.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d.setStatus(DeliveryStatus.ASSIGNED);
        deliveryRepository.saveAndFlush(d);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            deliveryService.startDelivery(tenantId, d.getId(), "Driver");
        });

        assertTrue(ex.getReason().contains("Driver must be assigned"));
    }

    @Test
    void test10_DeliveryLifecycleToCompletionFreesDriverAndVehicle() {
        Delivery d = new Delivery();
        d.setTenantId(tenantId);
        d.setDeliveryNumber("DEL-LIFE-001");
        d.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d.setWarehouseOrderId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        d.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d.setStatus(DeliveryStatus.ASSIGNED);
        d.setDriverId(DeliveryDataInitializer.DRIVER_JOHN_ID);
        d.setVehicleId(DeliveryDataInitializer.VEHICLE_VAN01_ID);
        d.setScheduledDate(LocalDate.now());
        d.setScheduledStartTime("08:00");
        d.setScheduledEndTime("10:00");
        Delivery saved = deliveryRepository.saveAndFlush(d);

        // Start
        DeliveryDTO started = deliveryService.startDelivery(tenantId, saved.getId(), "John");
        assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, started.getStatus());

        Driver drvStarted = driverRepository.findById(DeliveryDataInitializer.DRIVER_JOHN_ID).orElseThrow();
        assertEquals(DriverStatus.ON_DELIVERY, drvStarted.getStatus());

        // Arrive
        DeliveryDTO arrived = deliveryService.arriveDelivery(tenantId, saved.getId(), "John");
        assertEquals(DeliveryStatus.ARRIVED, arrived.getStatus());

        // Start setup
        DeliveryDTO setup = deliveryService.startSetup(tenantId, saved.getId(), "John");
        assertEquals(DeliveryStatus.SETUP_IN_PROGRESS, setup.getStatus());

        // Complete
        DeliveryDTO completed = deliveryService.completeDelivery(tenantId, saved.getId(), "John");
        assertEquals(DeliveryStatus.DELIVERED, completed.getStatus());

        Driver drvFreed = driverRepository.findById(DeliveryDataInitializer.DRIVER_JOHN_ID).orElseThrow();
        Vehicle vehFreed = vehicleRepository.findById(DeliveryDataInitializer.VEHICLE_VAN01_ID).orElseThrow();

        assertEquals(DriverStatus.AVAILABLE, drvFreed.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehFreed.getStatus());
    }

    @Test
    void test11_DeliveryFailureWorkflow() {
        Delivery d = new Delivery();
        d.setTenantId(tenantId);
        d.setDeliveryNumber("DEL-FAIL-001");
        d.setBookingId(WarehouseDataInitializer.DEMO_BOOKING_ID);
        d.setWarehouseOrderId(UUID.fromString("11111111-1111-1111-1111-111111111123"));
        d.setCustomerId(UUID.fromString("c3333333-3333-3333-3333-333333333333"));
        d.setEventId(UUID.fromString("e4444444-4444-4444-4444-444444444444"));
        d.setStatus(DeliveryStatus.ASSIGNED);
        d.setDriverId(DeliveryDataInitializer.DRIVER_MIKE_ID);
        d.setVehicleId(DeliveryDataInitializer.VEHICLE_VAN02_ID);
        Delivery saved = deliveryRepository.saveAndFlush(d);

        deliveryService.startDelivery(tenantId, saved.getId(), "Mike");

        FailDeliveryDTO failReq = new FailDeliveryDTO("CUSTOMER_NOT_AVAILABLE", "Venue gates were locked");
        DeliveryDTO failed = deliveryService.failDelivery(tenantId, saved.getId(), failReq, "Mike");

        assertEquals(DeliveryStatus.FAILED, failed.getStatus());
        assertEquals("CUSTOMER_NOT_AVAILABLE", failed.getFailureReason());

        Driver drv = driverRepository.findById(DeliveryDataInitializer.DRIVER_MIKE_ID).orElseThrow();
        assertEquals(DriverStatus.AVAILABLE, drv.getStatus());
    }

    @Test
    void test12_RouteCreationAndSequenceReordering() {
        CreateRouteDTO routeReq = new CreateRouteDTO();
        routeReq.setDate(LocalDate.of(2026, 8, 30));
        routeReq.setDriverId(DeliveryDataInitializer.DRIVER_JOHN_ID);
        routeReq.setVehicleId(DeliveryDataInitializer.VEHICLE_VAN01_ID);
        routeReq.setDeliveryIds(List.of(
                DeliveryDataInitializer.DEMO_DELIVERY_1_ID,
                DeliveryDataInitializer.DEMO_DELIVERY_2_ID
        ));

        DeliveryRouteDTO route = deliveryService.createRoute(tenantId, routeReq, "Dispatcher");

        assertNotNull(route);
        assertEquals(2, route.getStops().size());

        // Reorder stops
        SequenceUpdateDTO seqReq = new SequenceUpdateDTO();
        seqReq.setDeliveryIds(List.of(
                DeliveryDataInitializer.DEMO_DELIVERY_2_ID,
                DeliveryDataInitializer.DEMO_DELIVERY_1_ID
        ));

        DeliveryRouteDTO reordered = deliveryService.updateRouteSequence(tenantId, route.getId(), seqReq, "Dispatcher");
        assertEquals(DeliveryDataInitializer.DEMO_DELIVERY_2_ID, reordered.getStops().get(0).getDeliveryId());
    }

    @Test
    void test13_TenantIsolationEnforced() {
        String tenantB = "tenant-b-other-company";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            deliveryService.getDeliveryById(tenantB, DeliveryDataInitializer.DEMO_DELIVERY_1_ID);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void test14_CustomerPortalSanitizesSensitiveOperationalData() {
        UUID customerId = UUID.fromString("c3333333-3333-3333-3333-333333333333");
        DeliveryDTO dto = deliveryService.getSanitizedDeliveryForCustomer(tenantId, customerId, DeliveryDataInitializer.DEMO_DELIVERY_1_ID);

        assertNotNull(dto);
        assertNull(dto.getNotes());
        assertNull(dto.getDriverId());
        assertNull(dto.getVehicleId());
    }
}
