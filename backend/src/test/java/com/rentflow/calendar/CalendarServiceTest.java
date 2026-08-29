package com.rentflow.calendar;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.calendar.dto.CalendarDashboardDTO;
import com.rentflow.calendar.dto.CalendarEventDTO;
import com.rentflow.calendar.model.CalendarEventType;
import com.rentflow.calendar.model.OperationalConflict;
import com.rentflow.calendar.repository.OperationalConflictRepository;
import com.rentflow.calendar.service.CalendarService;
import com.rentflow.calendar.service.ConflictDetectionService;
import com.rentflow.claims.repository.RepairOrderRepository;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.returns.repository.ReturnOrderRepository;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CalendarServiceTest {

    private BookingRepository bookingRepository;
    private DeliveryRepository deliveryRepository;
    private ReturnOrderRepository returnOrderRepository;
    private RepairOrderRepository repairOrderRepository;
    private WarehouseOrderRepository warehouseOrderRepository;
    private WarehouseRepository warehouseRepository;
    private DriverRepository driverRepository;
    private VehicleRepository vehicleRepository;
    private EventRepository eventRepository;
    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private InventoryReservationRepository reservationRepository;
    private OperationalConflictRepository conflictRepository;
    private NotificationService notificationService;

    private AvailabilityService availabilityService;
    private ConflictDetectionService conflictDetectionService;
    private CalendarService calendarService;

    private String tenantId = "tenant-dev";

    @BeforeEach
    public void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        deliveryRepository = Mockito.mock(DeliveryRepository.class);
        returnOrderRepository = Mockito.mock(ReturnOrderRepository.class);
        repairOrderRepository = Mockito.mock(RepairOrderRepository.class);
        warehouseOrderRepository = Mockito.mock(WarehouseOrderRepository.class);
        warehouseRepository = Mockito.mock(WarehouseRepository.class);
        driverRepository = Mockito.mock(DriverRepository.class);
        vehicleRepository = Mockito.mock(VehicleRepository.class);
        eventRepository = Mockito.mock(EventRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        reservationRepository = Mockito.mock(InventoryReservationRepository.class);
        conflictRepository = Mockito.mock(OperationalConflictRepository.class);
        notificationService = null;

        availabilityService = new AvailabilityService(productRepository, reservationRepository, eventRepository);
        conflictDetectionService = new ConflictDetectionService(
                availabilityService, productRepository, driverRepository, vehicleRepository,
                deliveryRepository, warehouseRepository, warehouseOrderRepository, conflictRepository
        );

        calendarService = new CalendarService(
                bookingRepository, deliveryRepository, returnOrderRepository, repairOrderRepository,
                warehouseOrderRepository, driverRepository, vehicleRepository, eventRepository,
                customerRepository, conflictRepository, conflictDetectionService, notificationService
        );
    }

    @Test
    public void testGetCalendarEvents() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 30, 23, 59);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setTenantId(tenantId);
        booking.setBookingNumber("BOOK-000123");
        booking.setCustomerId(UUID.randomUUID());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRentalStartDateTime(LocalDateTime.of(2026, 9, 10, 10, 0));
        booking.setRentalEndDateTime(LocalDateTime.of(2026, 9, 12, 17, 0));

        when(bookingRepository.findByTenantId(tenantId)).thenReturn(List.of(booking));
        when(deliveryRepository.findByTenantId(tenantId)).thenReturn(List.of());
        when(returnOrderRepository.findByTenantId(tenantId)).thenReturn(List.of());
        when(repairOrderRepository.findByTenantId(tenantId)).thenReturn(List.of());
        when(warehouseOrderRepository.findByTenantId(tenantId)).thenReturn(List.of());
        when(eventRepository.findByTenantId(tenantId)).thenReturn(List.of());

        List<CalendarEventDTO> events = calendarService.getCalendarEvents(tenantId, start, end, null, null, null, null, null);

        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals("BOOK-" + booking.getId(), events.get(0).getId());
        assertEquals(CalendarEventType.BOOKING, events.get(0).getEventType());
    }

    @Test
    public void testGetDashboardMetrics() {
        when(bookingRepository.findByTenantId(tenantId)).thenReturn(List.of());
        when(deliveryRepository.findByTenantIdAndScheduledDate(eq(tenantId), any())).thenReturn(List.of());

        CalendarDashboardDTO metrics = calendarService.getDashboardMetrics(tenantId);

        assertNotNull(metrics);
        assertEquals(0, metrics.getTotalBookingsToday());
    }

    @Test
    public void testOverrideConflict() {
        UUID conflictId = UUID.randomUUID();
        OperationalConflict conflict = new OperationalConflict();
        conflict.setId(conflictId);
        conflict.setTenantId(tenantId);
        conflict.setStatus("OPEN");

        when(conflictRepository.findByTenantIdAndId(tenantId, conflictId)).thenReturn(Optional.of(conflict));
        when(conflictRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OperationalConflict result = calendarService.overrideConflict(tenantId, conflictId, "Extra staff arranged", "Operations Manager");

        assertNotNull(result);
        assertEquals("OVERRIDDEN", result.getStatus());
        assertEquals("Extra staff arranged", result.getOverrideReason());
        assertEquals("Operations Manager", result.getOverriddenBy());
    }
}
