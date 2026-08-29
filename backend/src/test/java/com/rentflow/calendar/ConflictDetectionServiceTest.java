package com.rentflow.calendar;

import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.calendar.dto.ConflictDTO;
import com.rentflow.calendar.model.ConflictSeverity;
import com.rentflow.calendar.model.ConflictType;
import com.rentflow.calendar.repository.OperationalConflictRepository;
import com.rentflow.calendar.service.ConflictDetectionService;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.DriverStatus;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.model.VehicleStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ConflictDetectionServiceTest {

    private AvailabilityService availabilityService;
    private ProductRepository productRepository;
    private InventoryReservationRepository reservationRepository;
    private EventRepository eventRepository;
    private DriverRepository driverRepository;
    private VehicleRepository vehicleRepository;
    private DeliveryRepository deliveryRepository;
    private WarehouseRepository warehouseRepository;
    private WarehouseOrderRepository warehouseOrderRepository;
    private OperationalConflictRepository conflictRepository;
    private ConflictDetectionService conflictDetectionService;

    private String tenantId = "tenant-dev";
    private UUID driverId = UUID.randomUUID();
    private UUID vehicleId = UUID.randomUUID();
    private UUID warehouseId = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        reservationRepository = Mockito.mock(InventoryReservationRepository.class);
        eventRepository = Mockito.mock(EventRepository.class);
        driverRepository = Mockito.mock(DriverRepository.class);
        vehicleRepository = Mockito.mock(VehicleRepository.class);
        deliveryRepository = Mockito.mock(DeliveryRepository.class);
        warehouseRepository = Mockito.mock(WarehouseRepository.class);
        warehouseOrderRepository = Mockito.mock(WarehouseOrderRepository.class);
        conflictRepository = Mockito.mock(OperationalConflictRepository.class);

        availabilityService = new AvailabilityService(productRepository, reservationRepository, eventRepository);
        conflictDetectionService = new ConflictDetectionService(
                availabilityService, productRepository, driverRepository, vehicleRepository,
                deliveryRepository, warehouseRepository, warehouseOrderRepository, conflictRepository
        );

        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setTenantId(tenantId);
        driver.setName("John Smith");
        driver.setStatus(DriverStatus.AVAILABLE);
        when(driverRepository.findByTenantIdAndId(tenantId, driverId)).thenReturn(Optional.of(driver));

        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setTenantId(tenantId);
        vehicle.setVehicleNumber("VAN-01");
        vehicle.setName("Ford Transit 350");
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        when(vehicleRepository.findByTenantIdAndId(tenantId, vehicleId)).thenReturn(Optional.of(vehicle));

        Warehouse wh = new Warehouse(warehouseId, tenantId, "WH-01", "Main Warehouse", "Address", 20, 20, 20, true);
        when(warehouseRepository.findByTenantIdAndId(tenantId, warehouseId)).thenReturn(Optional.of(wh));
    }

    @Test
    public void testDriverDoubleBookingConflict() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 13, 0);

        Delivery existingDel = new Delivery();
        existingDel.setDeliveryNumber("DEL-000123");
        existingDel.setScheduledDate(LocalDate.of(2026, 9, 10));
        existingDel.setScheduledStartTime("10:00 AM");
        existingDel.setScheduledEndTime("12:00 PM");

        when(deliveryRepository.findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(eq(tenantId), eq(driverId), any(), any()))
                .thenReturn(List.of(existingDel));

        ConflictDTO conflict = conflictDetectionService.checkDriverConflict(tenantId, driverId, start, end, null);

        assertNotNull(conflict);
        assertEquals(ConflictType.DRIVER, conflict.getType());
        assertEquals(ConflictSeverity.HARD_CONFLICT, conflict.getSeverity());
        assertTrue(conflict.getMessage().contains("John Smith is already assigned"));
    }

    @Test
    public void testVehicleDoubleBookingConflict() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 10, 13, 0);

        Delivery existingDel = new Delivery();
        existingDel.setDeliveryNumber("DEL-000123");
        existingDel.setScheduledDate(LocalDate.of(2026, 9, 10));
        existingDel.setScheduledStartTime("10:00 AM");
        existingDel.setScheduledEndTime("12:00 PM");

        when(deliveryRepository.findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(eq(tenantId), eq(vehicleId), any(), any()))
                .thenReturn(List.of(existingDel));

        ConflictDTO conflict = conflictDetectionService.checkVehicleConflict(tenantId, vehicleId, start, end, null);

        assertNotNull(conflict);
        assertEquals(ConflictType.VEHICLE, conflict.getType());
        assertEquals(ConflictSeverity.HARD_CONFLICT, conflict.getSeverity());
    }

    @Test
    public void testWarehouseCapacityWarning() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        when(warehouseOrderRepository.findByTenantId(tenantId)).thenReturn(List.of());

        ConflictDTO warning = conflictDetectionService.checkWarehouseConflict(tenantId, warehouseId, date, 25);

        assertNotNull(warning);
        assertEquals(ConflictType.WAREHOUSE, warning.getType());
        assertEquals(ConflictSeverity.WARNING, warning.getSeverity());
        assertTrue(warning.getMessage().contains("capacity"));
    }
}
