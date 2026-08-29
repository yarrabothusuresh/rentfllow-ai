package com.rentflow.calendar.service;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.dto.BookingConflictCheckDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.calendar.dto.ConflictDTO;
import com.rentflow.calendar.dto.ConflictResponseDTO;
import com.rentflow.calendar.model.ConflictSeverity;
import com.rentflow.calendar.model.ConflictType;
import com.rentflow.calendar.model.OperationalConflict;
import com.rentflow.calendar.repository.OperationalConflictRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConflictDetectionService {

    private final AvailabilityService availabilityService;
    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryRepository deliveryRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final OperationalConflictRepository conflictRepository;

    public ConflictDetectionService(AvailabilityService availabilityService,
                                   ProductRepository productRepository,
                                   DriverRepository driverRepository,
                                   VehicleRepository vehicleRepository,
                                   DeliveryRepository deliveryRepository,
                                   WarehouseRepository warehouseRepository,
                                   WarehouseOrderRepository warehouseOrderRepository,
                                   OperationalConflictRepository conflictRepository) {
        this.availabilityService = availabilityService;
        this.productRepository = productRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.deliveryRepository = deliveryRepository;
        this.warehouseRepository = warehouseRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.conflictRepository = conflictRepository;
    }

    public ConflictResponseDTO checkBookingConflict(String tenantId, BookingConflictCheckDTO request) {
        ConflictResponseDTO response = new ConflictResponseDTO();

        if (request == null) return response;

        // 1. Inventory Check
        if (request.getItems() != null && request.getStartDate() != null && request.getEndDate() != null) {
            for (BookingConflictCheckDTO.Item item : request.getItems()) {
                if (item.getProductId() == null || item.getQuantity() <= 0) continue;
                ConflictDTO invConflict = checkInventoryConflict(
                        tenantId, item.getProductId(), item.getQuantity(), request.getStartDate(), request.getEndDate(), request.getBookingId());
                if (invConflict != null) {
                    if (invConflict.getSeverity() == ConflictSeverity.HARD_CONFLICT) {
                        response.addHardConflict(invConflict);
                    } else {
                        response.addWarning(invConflict);
                    }
                }
            }
        }

        // 2. Driver Conflict
        if (request.getDriverId() != null) {
            LocalDateTime dStart = request.getDeliveryStart() != null ? request.getDeliveryStart() : request.getStartDate();
            LocalDateTime dEnd = request.getDeliveryEnd() != null ? request.getDeliveryEnd() : (dStart != null ? dStart.plusHours(2) : null);
            if (dStart != null && dEnd != null) {
                ConflictDTO driverConflict = checkDriverConflict(tenantId, request.getDriverId(), dStart, dEnd, request.getBookingId());
                if (driverConflict != null) {
                    response.addHardConflict(driverConflict);
                }
            }
        }

        // 3. Vehicle Conflict
        if (request.getVehicleId() != null) {
            LocalDateTime vStart = request.getDeliveryStart() != null ? request.getDeliveryStart() : request.getStartDate();
            LocalDateTime vEnd = request.getDeliveryEnd() != null ? request.getDeliveryEnd() : (vStart != null ? vStart.plusHours(2) : null);
            if (vStart != null && vEnd != null) {
                ConflictDTO vehicleConflict = checkVehicleConflict(tenantId, request.getVehicleId(), vStart, vEnd, request.getBookingId());
                if (vehicleConflict != null) {
                    response.addHardConflict(vehicleConflict);
                }
            }
        }

        // 4. Warehouse Capacity Warning
        if (request.getWarehouseId() != null && request.getStartDate() != null) {
            ConflictDTO whWarning = checkWarehouseConflict(tenantId, request.getWarehouseId(), request.getStartDate().toLocalDate(), 1);
            if (whWarning != null) {
                response.addWarning(whWarning);
            }
        }

        return response;
    }

    public ConflictDTO checkInventoryConflict(String tenantId, UUID productId, int requestedQuantity,
                                              LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeBookingId) {
        AvailabilityResultDTO avail = availabilityService.checkAvailabilityExcludingBooking(
                tenantId, productId, requestedQuantity, startDateTime, endDateTime, excludeBookingId);

        if (!avail.isAvailable()) {
            String prodName = avail.getProductName() != null ? avail.getProductName() : "Product " + productId;
            String msg = "Only " + avail.getAvailableQuantity() + " of requested " + requestedQuantity + " " + prodName + " are available.";
            String action = "Reduce quantity or choose different rental dates.";

            // Find conflicting booking number if available
            String conflictingRef = null;
            if (avail.getConflictingReservations() != null && !avail.getConflictingReservations().isEmpty()) {
                conflictingRef = avail.getConflictingReservations().get(0).getBookingId() != null
                        ? avail.getConflictingReservations().get(0).getBookingId().toString() : null;
            }

            return new ConflictDTO(
                    ConflictType.INVENTORY,
                    ConflictSeverity.HARD_CONFLICT,
                    prodName,
                    productId,
                    requestedQuantity,
                    avail.getAvailableQuantity(),
                    conflictingRef,
                    msg,
                    action
            );
        }
        return null;
    }

    public ConflictDTO checkDriverConflict(String tenantId, UUID driverId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeBookingId) {
        Optional<Driver> optDriver = driverRepository.findByTenantIdAndId(tenantId, driverId);
        String driverName = optDriver.map(Driver::getName).orElse("Driver " + driverId);

        LocalDate date = startDateTime.toLocalDate();
        List<Delivery> activeDeliveries = deliveryRepository.findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(
                tenantId, driverId, date, List.of(DeliveryStatus.CANCELLED, DeliveryStatus.FAILED));

        LocalTime reqStart = startDateTime.toLocalTime();
        LocalTime reqEnd = endDateTime.toLocalTime();

        for (Delivery d : activeDeliveries) {
            if (excludeBookingId != null && excludeBookingId.equals(d.getBookingId())) continue;

            LocalTime delStart = parseTime(d.getScheduledStartTime(), LocalTime.of(8, 0));
            LocalTime delEnd = parseTime(d.getScheduledEndTime(), delStart.plusHours(2));

            if (reqStart.isBefore(delEnd) && reqEnd.isAfter(delStart)) {
                String msg = driverName + " is already assigned from " + formatTime(delStart) + " to " + formatTime(delEnd) + ".";
                String action = "Assign a different driver or reschedule the delivery window.";
                return new ConflictDTO(
                        ConflictType.DRIVER,
                        ConflictSeverity.HARD_CONFLICT,
                        driverName,
                        driverId,
                        1,
                        0,
                        d.getDeliveryNumber(),
                        msg,
                        action
                );
            }
        }
        return null;
    }

    public ConflictDTO checkVehicleConflict(String tenantId, UUID vehicleId, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID excludeBookingId) {
        Optional<Vehicle> optVehicle = vehicleRepository.findByTenantIdAndId(tenantId, vehicleId);
        String vehicleName = optVehicle.map(v -> v.getName() + " (" + v.getVehicleNumber() + ")").orElse("Vehicle " + vehicleId);

        LocalDate date = startDateTime.toLocalDate();
        List<Delivery> activeDeliveries = deliveryRepository.findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(
                tenantId, vehicleId, date, List.of(DeliveryStatus.CANCELLED, DeliveryStatus.FAILED));

        LocalTime reqStart = startDateTime.toLocalTime();
        LocalTime reqEnd = endDateTime.toLocalTime();

        for (Delivery d : activeDeliveries) {
            if (excludeBookingId != null && excludeBookingId.equals(d.getBookingId())) continue;

            LocalTime delStart = parseTime(d.getScheduledStartTime(), LocalTime.of(8, 0));
            LocalTime delEnd = parseTime(d.getScheduledEndTime(), delStart.plusHours(2));

            if (reqStart.isBefore(delEnd) && reqEnd.isAfter(delStart)) {
                String msg = vehicleName + " is already assigned during this time (" + formatTime(delStart) + " - " + formatTime(delEnd) + ").";
                String action = "Select another vehicle or update the delivery schedule.";
                return new ConflictDTO(
                        ConflictType.VEHICLE,
                        ConflictSeverity.HARD_CONFLICT,
                        vehicleName,
                        vehicleId,
                        1,
                        0,
                        d.getDeliveryNumber(),
                        msg,
                        action
                );
            }
        }
        return null;
    }

    public ConflictDTO checkWarehouseConflict(String tenantId, UUID warehouseId, LocalDate date, int additionalOrders) {
        Optional<Warehouse> optWh = warehouseRepository.findByTenantIdAndId(tenantId, warehouseId);
        if (optWh.isEmpty()) return null;

        Warehouse wh = optWh.get();
        int capacity = wh.getDailyPickCapacity() > 0 ? wh.getDailyPickCapacity() : 20;

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        long existingOrders = warehouseOrderRepository.findByTenantId(tenantId).stream()
                .filter(w -> w.getScheduledDate() != null &&
                        !w.getScheduledDate().isBefore(startOfDay) &&
                        !w.getScheduledDate().isAfter(endOfDay))
                .count();

        long totalScheduled = existingOrders + additionalOrders;
        if (totalScheduled > capacity) {
            long excess = totalScheduled - capacity;
            String msg = wh.getName() + " pick capacity (" + capacity + " orders/day) may be exceeded by " + excess + " orders on " + date + ".";
            String action = "Schedule additional warehouse staff or override warning if extra capacity is arranged.";
            return new ConflictDTO(
                    ConflictType.WAREHOUSE,
                    ConflictSeverity.WARNING,
                    wh.getName(),
                    warehouseId,
                    (int) totalScheduled,
                    capacity,
                    null,
                    msg,
                    action
            );
        }
        return null;
    }

    @Transactional
    public OperationalConflict logConflict(String tenantId, ConflictDTO conflict, String referenceId, String referenceType) {
        OperationalConflict entity = new OperationalConflict(
                tenantId,
                conflict.getType(),
                conflict.getSeverity(),
                conflict.getResource(),
                conflict.getResourceId() != null ? conflict.getResourceId().toString() : null,
                referenceId,
                referenceType,
                conflict.getMessage(),
                conflict.getSuggestedAction()
        );
        return conflictRepository.save(entity);
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

    private String formatTime(LocalTime time) {
        if (time == null) return "";
        int hour = time.getHour();
        String ampm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format("%02d:%02d %s", displayHour, time.getMinute(), ampm);
    }
}
