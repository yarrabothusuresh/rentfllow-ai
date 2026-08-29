package com.rentflow.calendar.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.calendar.dto.*;
import com.rentflow.calendar.model.CalendarEventType;
import com.rentflow.calendar.model.OperationalConflict;
import com.rentflow.calendar.repository.OperationalConflictRepository;
import com.rentflow.claims.model.RepairOrder;
import com.rentflow.claims.repository.RepairOrderRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryType;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.returns.model.ReturnOrder;
import com.rentflow.returns.repository.ReturnOrderRepository;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final BookingRepository bookingRepository;
    private final DeliveryRepository deliveryRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final OperationalConflictRepository conflictRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final NotificationService notificationService;

    public CalendarService(BookingRepository bookingRepository,
                           DeliveryRepository deliveryRepository,
                           ReturnOrderRepository returnOrderRepository,
                           RepairOrderRepository repairOrderRepository,
                           WarehouseOrderRepository warehouseOrderRepository,
                           DriverRepository driverRepository,
                           VehicleRepository vehicleRepository,
                           EventRepository eventRepository,
                           CustomerRepository customerRepository,
                           OperationalConflictRepository conflictRepository,
                           ConflictDetectionService conflictDetectionService,
                           NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.deliveryRepository = deliveryRepository;
        this.returnOrderRepository = returnOrderRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.eventRepository = eventRepository;
        this.customerRepository = customerRepository;
        this.conflictRepository = conflictRepository;
        this.conflictDetectionService = conflictDetectionService;
        this.notificationService = notificationService;
    }

    public List<CalendarEventDTO> getCalendarEvents(String tenantId, LocalDateTime start, LocalDateTime end,
                                                   List<CalendarEventType> eventTypes, String resourceType,
                                                   String resourceId, String status, UUID warehouseId) {
        if (start == null) start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        if (end == null) end = start.plusMonths(1);

        List<CalendarEventDTO> events = new ArrayList<>();
        Set<CalendarEventType> typesToInclude = (eventTypes != null && !eventTypes.isEmpty())
                ? new HashSet<>(eventTypes)
                : EnumSet.allOf(CalendarEventType.class);

        Map<UUID, Customer> customerMap = customerRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));
        Map<UUID, Driver> driverMap = driverRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Driver::getId, d -> d, (a, b) -> a));
        Map<UUID, Vehicle> vehicleMap = vehicleRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v, (a, b) -> a));

        LocalDateTime finalStart = start;
        LocalDateTime finalEnd = end;

        // 1. Bookings
        if (typesToInclude.contains(CalendarEventType.BOOKING)) {
            List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
            for (Booking b : bookings) {
                if (b.getRentalStartDateTime() != null && b.getRentalEndDateTime() != null) {
                    if (b.getRentalStartDateTime().isBefore(finalEnd) && b.getRentalEndDateTime().isAfter(finalStart)) {
                        CalendarEventDTO dto = new CalendarEventDTO();
                        dto.setId("BOOK-" + b.getId());
                        dto.setTenantId(tenantId);
                        dto.setEventType(CalendarEventType.BOOKING);
                        dto.setReferenceType("BOOKING");
                        dto.setReferenceId(b.getId().toString());
                        Customer cust = customerMap.get(b.getCustomerId());
                        String custName = cust != null ? (cust.getCompanyName() != null && !cust.getCompanyName().isBlank() ? cust.getCompanyName() : cust.getFirstName() + " " + cust.getLastName()) : "Customer";
                        dto.setTitle(b.getBookingNumber() + " - " + custName);
                        dto.setStart(b.getRentalStartDateTime());
                        dto.setEnd(b.getRentalEndDateTime());
                        dto.setStatus(b.getStatus().name());
                        dto.setCustomerName(custName);
                        dto.setNotes(b.getNotes());
                        events.add(dto);
                    }
                }
            }
        }

        // 2. Deliveries & Pickups (and Driver/Vehicle assignments)
        List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId);
        for (Delivery d : deliveries) {
            if (d.getScheduledDate() != null) {
                LocalTime sTime = parseTime(d.getScheduledStartTime(), LocalTime.of(9, 0));
                LocalTime eTime = parseTime(d.getScheduledEndTime(), sTime.plusHours(2));
                LocalDateTime dStart = d.getScheduledDate().atTime(sTime);
                LocalDateTime dEnd = d.getScheduledDate().atTime(eTime);

                if (dStart.isBefore(finalEnd) && dEnd.isAfter(finalStart)) {
                    Customer cust = customerMap.get(d.getCustomerId());
                    Driver drv = d.getDriverId() != null ? driverMap.get(d.getDriverId()) : null;
                    Vehicle veh = d.getVehicleId() != null ? vehicleMap.get(d.getVehicleId()) : null;
                    String custName = cust != null ? (cust.getCompanyName() != null && !cust.getCompanyName().isBlank() ? cust.getCompanyName() : cust.getFirstName() + " " + cust.getLastName()) : "Customer";

                    CalendarEventType primaryType = (d.getDeliveryType() == DeliveryType.PICKUP) ? CalendarEventType.PICKUP : CalendarEventType.DELIVERY;

                    if (typesToInclude.contains(primaryType)) {
                        CalendarEventDTO dto = new CalendarEventDTO();
                        dto.setId("DEL-" + d.getId());
                        dto.setTenantId(tenantId);
                        dto.setEventType(primaryType);
                        dto.setReferenceType("DELIVERY");
                        dto.setReferenceId(d.getId().toString());
                        dto.setTitle(d.getDeliveryNumber() + " (" + primaryType.name() + ") - " + custName);
                        dto.setStart(dStart);
                        dto.setEnd(dEnd);
                        dto.setStatus(d.getStatus().name());
                        dto.setCustomerName(custName);
                        dto.setDriverName(drv != null ? drv.getName() : "Unassigned");
                        dto.setVehicleName(veh != null ? veh.getName() + " (" + veh.getVehicleNumber() + ")" : "Unassigned");
                        dto.setLocation(d.getDeliveryAddressSnapshot());
                        events.add(dto);
                    }

                    // Driver resource assignment event
                    if (typesToInclude.contains(CalendarEventType.DRIVER_ASSIGNMENT) && drv != null) {
                        if (resourceId == null || resourceId.equals(drv.getId().toString())) {
                            CalendarEventDTO drvDto = new CalendarEventDTO();
                            drvDto.setId("DRV-" + d.getId());
                            drvDto.setTenantId(tenantId);
                            drvDto.setEventType(CalendarEventType.DRIVER_ASSIGNMENT);
                            drvDto.setReferenceType("DELIVERY");
                            drvDto.setReferenceId(d.getId().toString());
                            drvDto.setResourceType("DRIVER");
                            drvDto.setResourceId(drv.getId().toString());
                            drvDto.setResourceName(drv.getName());
                            drvDto.setTitle("Driver: " + drv.getName() + " (" + d.getDeliveryNumber() + ")");
                            drvDto.setStart(dStart);
                            drvDto.setEnd(dEnd);
                            drvDto.setStatus(d.getStatus().name());
                            drvDto.setCustomerName(custName);
                            events.add(drvDto);
                        }
                    }

                    // Vehicle resource assignment event
                    if (typesToInclude.contains(CalendarEventType.VEHICLE_ASSIGNMENT) && veh != null) {
                        if (resourceId == null || resourceId.equals(veh.getId().toString())) {
                            CalendarEventDTO vehDto = new CalendarEventDTO();
                            vehDto.setId("VEH-" + d.getId());
                            vehDto.setTenantId(tenantId);
                            vehDto.setEventType(CalendarEventType.VEHICLE_ASSIGNMENT);
                            vehDto.setReferenceType("DELIVERY");
                            vehDto.setReferenceId(d.getId().toString());
                            vehDto.setResourceType("VEHICLE");
                            vehDto.setResourceId(veh.getId().toString());
                            vehDto.setResourceName(veh.getName());
                            vehDto.setTitle("Vehicle: " + veh.getVehicleNumber() + " (" + d.getDeliveryNumber() + ")");
                            vehDto.setStart(dStart);
                            vehDto.setEnd(dEnd);
                            vehDto.setStatus(d.getStatus().name());
                            vehDto.setCustomerName(custName);
                            events.add(vehDto);
                        }
                    }
                }
            }
        }

        // 3. Returns
        if (typesToInclude.contains(CalendarEventType.RETURN)) {
            List<ReturnOrder> returns = returnOrderRepository.findByTenantId(tenantId);
            for (ReturnOrder r : returns) {
                LocalDateTime rStart = r.getScheduledDate() != null ? r.getScheduledDate().atTime(9, 0) : r.getCreatedAt();
                if (rStart != null && rStart.isBefore(finalEnd) && rStart.isAfter(finalStart.minusDays(1))) {
                    CalendarEventDTO dto = new CalendarEventDTO();
                    dto.setId("RET-" + r.getId());
                    dto.setTenantId(tenantId);
                    dto.setEventType(CalendarEventType.RETURN);
                    dto.setReferenceType("RETURN_ORDER");
                    dto.setReferenceId(r.getId().toString());
                    dto.setTitle("Return: " + r.getReturnNumber());
                    dto.setStart(rStart);
                    dto.setEnd(rStart.plusHours(2));
                    dto.setStatus(r.getStatus().name());
                    events.add(dto);
                }
            }
        }

        // 4. Maintenance / Repairs
        if (typesToInclude.contains(CalendarEventType.MAINTENANCE)) {
            List<RepairOrder> repairs = repairOrderRepository.findByTenantId(tenantId);
            for (RepairOrder rep : repairs) {
                LocalDateTime repStart = rep.getCreatedAt();
                LocalDateTime repEnd = rep.getCompletedAt() != null ? rep.getCompletedAt() : repStart.plusDays(2);
                if (repStart.isBefore(finalEnd) && repEnd.isAfter(finalStart)) {
                    CalendarEventDTO dto = new CalendarEventDTO();
                    dto.setId("REP-" + rep.getId());
                    dto.setTenantId(tenantId);
                    dto.setEventType(CalendarEventType.MAINTENANCE);
                    dto.setReferenceType("REPAIR_ORDER");
                    dto.setReferenceId(rep.getId().toString());
                    dto.setTitle("Maintenance: " + rep.getRepairNumber());
                    dto.setStart(repStart);
                    dto.setEnd(repEnd);
                    dto.setStatus(rep.getStatus().name());
                    events.add(dto);
                }
            }
        }

        // 5. Warehouse Tasks
        if (typesToInclude.contains(CalendarEventType.WAREHOUSE_TASK)) {
            List<WarehouseOrder> whOrders = warehouseOrderRepository.findByTenantId(tenantId);
            for (WarehouseOrder wo : whOrders) {
                LocalDateTime woStart = wo.getScheduledDate() != null ? wo.getScheduledDate() : wo.getCreatedAt();
                if (woStart != null && woStart.isBefore(finalEnd) && woStart.isAfter(finalStart.minusDays(1))) {
                    CalendarEventDTO dto = new CalendarEventDTO();
                    dto.setId("WHO-" + wo.getId());
                    dto.setTenantId(tenantId);
                    dto.setEventType(CalendarEventType.WAREHOUSE_TASK);
                    dto.setReferenceType("WAREHOUSE_ORDER");
                    dto.setReferenceId(wo.getId().toString());
                    dto.setTitle("Warehouse Task: " + wo.getOrderNumber() + " (" + wo.getStatus() + ")");
                    dto.setStart(woStart);
                    dto.setEnd(woStart.plusHours(1));
                    dto.setStatus(wo.getStatus().name());
                    events.add(dto);
                }
            }
        }

        // 6. Events
        if (typesToInclude.contains(CalendarEventType.EVENT)) {
            List<Event> evList = eventRepository.findByTenantId(tenantId);
            for (Event ev : evList) {
                if (ev.getEventDate() != null) {
                    LocalTime evStart = parseTime(ev.getStartTime(), LocalTime.of(10, 0));
                    LocalTime evEnd = parseTime(ev.getEndTime(), evStart.plusHours(4));
                    LocalDateTime eStart = ev.getEventDate().atTime(evStart);
                    LocalDateTime eEnd = ev.getEventDate().atTime(evEnd);

                    if (eStart.isBefore(finalEnd) && eEnd.isAfter(finalStart)) {
                        CalendarEventDTO dto = new CalendarEventDTO();
                        dto.setId("EVT-" + ev.getId());
                        dto.setTenantId(tenantId);
                        dto.setEventType(CalendarEventType.EVENT);
                        dto.setReferenceType("EVENT");
                        dto.setReferenceId(ev.getId().toString());
                        dto.setTitle("Event: " + ev.getEventName());
                        dto.setStart(eStart);
                        dto.setEnd(eEnd);
                        dto.setStatus(ev.getStatus().name());
                        events.add(dto);
                    }
                }
            }
        }

        return events;
    }

    @Transactional
    public CalendarEventDTO rescheduleEvent(String tenantId, RescheduleRequestDTO request, String username) {
        if (request == null || request.getReferenceId() == null || request.getNewStart() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reschedule request");
        }

        LocalDateTime newEnd = request.getNewEnd() != null ? request.getNewEnd() : request.getNewStart().plusHours(2);
        CalendarEventDTO updatedEvent = new CalendarEventDTO();

        if (request.getEventType() == CalendarEventType.DELIVERY || request.getEventType() == CalendarEventType.PICKUP || "DELIVERY".equals(request.getReferenceId())) {
            UUID deliveryId;
            try {
                deliveryId = UUID.fromString(request.getReferenceId());
            } catch (Exception e) {
                deliveryId = deliveryRepository.findByTenantIdAndDeliveryNumber(tenantId, request.getReferenceId())
                        .map(Delivery::getId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found: " + request.getReferenceId()));
            }

            final UUID searchDeliveryId = deliveryId;
            Delivery del = deliveryRepository.findByTenantIdAndId(tenantId, searchDeliveryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found: " + searchDeliveryId));

            // Conflict Check for Driver & Vehicle
            if (del.getDriverId() != null) {
                ConflictDTO drvConflict = conflictDetectionService.checkDriverConflict(tenantId, del.getDriverId(), request.getNewStart(), newEnd, del.getBookingId());
                if (drvConflict != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, drvConflict.getMessage());
                }
            }

            if (del.getVehicleId() != null) {
                ConflictDTO vehConflict = conflictDetectionService.checkVehicleConflict(tenantId, del.getVehicleId(), request.getNewStart(), newEnd, del.getBookingId());
                if (vehConflict != null) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, vehConflict.getMessage());
                }
            }

            del.setScheduledDate(request.getNewStart().toLocalDate());
            del.setScheduledStartTime(formatLocalTime(request.getNewStart().toLocalTime()));
            del.setScheduledEndTime(formatLocalTime(newEnd.toLocalTime()));
            del = deliveryRepository.save(del);

            // Notify Customer
            try {
                NotificationRequestDTO notif = new NotificationRequestDTO();
                notif.setTenantId(tenantId);
                notif.setRecipientCustomerId(del.getCustomerId());
                notif.setType(NotificationType.DELIVERY_SCHEDULED);
                notif.setChannel(NotificationChannel.IN_APP);
                notif.setReferenceType("DELIVERY");
                notif.setReferenceId(del.getId().toString());
                notif.setCustomTitle("Delivery Rescheduled");
                notif.setCustomMessage("Your delivery " + del.getDeliveryNumber() + " has been rescheduled to " + del.getScheduledDate() + " between " + del.getScheduledStartTime() + " and " + del.getScheduledEndTime() + ".");
                notificationService.sendNotification(notif);
            } catch (Exception ignored) {}

            updatedEvent.setId("DEL-" + del.getId());
            updatedEvent.setTenantId(tenantId);
            updatedEvent.setEventType(del.getDeliveryType() == DeliveryType.PICKUP ? CalendarEventType.PICKUP : CalendarEventType.DELIVERY);
            updatedEvent.setReferenceId(del.getId().toString());
            updatedEvent.setTitle(del.getDeliveryNumber() + " (Rescheduled)");
            updatedEvent.setStart(request.getNewStart());
            updatedEvent.setEnd(newEnd);
            updatedEvent.setStatus(del.getStatus().name());

        } else if (request.getEventType() == CalendarEventType.BOOKING) {
            UUID bookingId = UUID.fromString(request.getReferenceId());
            Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

            booking.setRentalStartDateTime(request.getNewStart());
            booking.setRentalEndDateTime(newEnd);
            bookingRepository.save(booking);

            updatedEvent.setId("BOOK-" + booking.getId());
            updatedEvent.setTenantId(tenantId);
            updatedEvent.setEventType(CalendarEventType.BOOKING);
            updatedEvent.setReferenceId(booking.getId().toString());
            updatedEvent.setTitle(booking.getBookingNumber() + " (Rescheduled)");
            updatedEvent.setStart(request.getNewStart());
            updatedEvent.setEnd(newEnd);
            updatedEvent.setStatus(booking.getStatus().name());
        }

        return updatedEvent;
    }

    public CalendarDashboardDTO getDashboardMetrics(String tenantId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.atTime(23, 59, 59);

        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        long bCount = bookings.stream()
                .filter(b -> b.getRentalStartDateTime() != null && !b.getRentalStartDateTime().isBefore(startToday) && !b.getRentalStartDateTime().isAfter(endToday))
                .count();

        List<Delivery> deliveries = deliveryRepository.findByTenantIdAndScheduledDate(tenantId, today);
        long dCount = deliveries.stream().filter(d -> d.getDeliveryType() == DeliveryType.DELIVERY).count();
        long pCount = deliveries.stream().filter(d -> d.getDeliveryType() == DeliveryType.PICKUP).count();

        long rCount = returnOrderRepository.findByTenantId(tenantId).stream()
                .filter(r -> r.getScheduledDate() != null && r.getScheduledDate().equals(today))
                .count();

        long mCount = repairOrderRepository.findByTenantId(tenantId).stream()
                .filter(rep -> rep.getCreatedAt() != null && !rep.getCreatedAt().isBefore(startToday) && !rep.getCreatedAt().isAfter(endToday))
                .count();

        long activeDrivers = driverRepository.countByTenantId(tenantId);
        long activeVehicles = vehicleRepository.countByTenantId(tenantId);
        long openConflicts = conflictRepository.countByTenantIdAndStatus(tenantId, "OPEN");
        long warnings = conflictRepository.countByTenantIdAndStatus(tenantId, "WARNING");

        CalendarDashboardDTO dto = new CalendarDashboardDTO();
        dto.setTotalBookingsToday(bCount);
        dto.setTotalDeliveriesToday(dCount);
        dto.setTotalPickupsToday(pCount);
        dto.setTotalReturnsToday(rCount);
        dto.setTotalMaintenanceToday(mCount);
        dto.setActiveDrivers(activeDrivers);
        dto.setActiveVehicles(activeVehicles);
        dto.setOpenConflictsCount(openConflicts);
        dto.setCapacityWarningsCount(warnings);
        return dto;
    }

    public List<OperationalConflict> getConflicts(String tenantId) {
        return conflictRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional
    public OperationalConflict overrideConflict(String tenantId, UUID conflictId, String reason, String username) {
        OperationalConflict conflict = conflictRepository.findByTenantIdAndId(tenantId, conflictId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conflict not found with ID: " + conflictId));

        conflict.setStatus("OVERRIDDEN");
        conflict.setOverriddenBy(username != null ? username : "Operations Manager");
        conflict.setOverrideReason(reason != null && !reason.isBlank() ? reason : "Operational override approved by manager");
        conflict.setOverriddenAt(LocalDateTime.now());
        return conflictRepository.save(conflict);
    }

    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        if (timeStr == null || timeStr.isBlank()) return defaultTime;
        try {
            String clean = timeStr.trim();
            if (clean.contains(":")) {
                String[] parts = clean.split(":");
                int hr = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                if (clean.toLowerCase().contains("pm") && hr < 12) hr += 12;
                if (clean.toLowerCase().contains("am") && hr == 12) hr = 0;
                return LocalTime.of(hr, min);
            }
        } catch (Exception ignored) {}
        return defaultTime;
    }

    private String formatLocalTime(LocalTime time) {
        if (time == null) return "09:00 AM";
        int hr = time.getHour();
        String ampm = hr >= 12 ? "PM" : "AM";
        int displayHr = hr % 12;
        if (displayHr == 0) displayHr = 12;
        return String.format("%02d:%02d %s", displayHr, time.getMinute(), ampm);
    }
}
