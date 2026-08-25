package com.rentflow.delivery.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.delivery.dto.*;
import com.rentflow.delivery.model.*;
import com.rentflow.delivery.repository.*;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
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
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryRouteRepository routeRepository;
    private final DeliveryRouteStopRepository routeStopRepository;
    private final DeliveryAuditRepository auditRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final InventoryReservationRepository reservationRepository;
    private final DeliveryStatusTransitionService transitionService;
    private final RouteOptimizationService routeOptimizationService;
    private final GeocodingService geocodingService;
    private final NotificationService notificationService;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            DeliveryRouteRepository routeRepository,
            DeliveryRouteStopRepository routeStopRepository,
            DeliveryAuditRepository auditRepository,
            WarehouseOrderRepository warehouseOrderRepository,
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            CustomerRepository customerRepository,
            InventoryReservationRepository reservationRepository,
            DeliveryStatusTransitionService transitionService,
            RouteOptimizationService routeOptimizationService,
            GeocodingService geocodingService,
            NotificationService notificationService) {
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.auditRepository = auditRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
        this.transitionService = transitionService;
        this.routeOptimizationService = routeOptimizationService;
        this.geocodingService = geocodingService;
        this.notificationService = notificationService;
    }

    // ==========================================
    // 1. CREATE DELIVERY FROM WAREHOUSE ORDER
    // ==========================================

    @Transactional
    public DeliveryDTO createFromWarehouseOrder(String tenantId, UUID warehouseOrderId, String createdBy) {
        WarehouseOrder order = warehouseOrderRepository.findByTenantIdAndId(tenantId, warehouseOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse order not found"));

        if (order.getStatus() != WarehouseOrderStatus.PACKED && order.getStatus() != WarehouseOrderStatus.READY_FOR_DELIVERY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery cannot be created until warehouse preparation is complete.");
        }

        Optional<Delivery> existing = deliveryRepository.findByTenantIdAndWarehouseOrderId(tenantId, warehouseOrderId);
        if (existing.isPresent() && existing.get().getStatus() != DeliveryStatus.CANCELLED) {
            return mapToDTO(existing.get());
        }

        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, order.getBookingId()).orElse(null);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, order.getCustomerId()).orElse(null);
        Event event = eventRepository.findByTenantIdAndId(tenantId, order.getEventId()).orElse(null);

        Delivery delivery = new Delivery();
        delivery.setTenantId(tenantId);
        delivery.setDeliveryNumber(generateDeliveryNumber(tenantId));
        delivery.setBookingId(order.getBookingId());
        delivery.setWarehouseOrderId(order.getId());
        delivery.setCustomerId(order.getCustomerId());
        delivery.setEventId(order.getEventId());
        delivery.setDeliveryType(DeliveryType.DELIVERY);
        delivery.setStatus(DeliveryStatus.PENDING);

        // Priority determination
        LocalDate scheduledDate = order.getScheduledDate() != null ? order.getScheduledDate().toLocalDate() : LocalDate.now();
        delivery.setScheduledDate(scheduledDate);
        if (scheduledDate.equals(LocalDate.now())) {
            delivery.setPriority(DeliveryPriority.HIGH);
        } else {
            delivery.setPriority(DeliveryPriority.NORMAL);
        }

        // Build address snapshot
        String addressSnapshot = buildAddressSnapshot(customer, event);
        delivery.setDeliveryAddressSnapshot(addressSnapshot);

        GeocodingService.Coordinates coords = geocodingService.geocodeAddress(addressSnapshot);
        delivery.setLatitude(coords.getLatitude());
        delivery.setLongitude(coords.getLongitude());

        // Default time window
        delivery.setScheduledStartTime("10:00");
        delivery.setScheduledEndTime("12:00");

        Delivery saved = deliveryRepository.save(delivery);

        logAudit(tenantId, saved.getId(), null, "DELIVERY_CREATED", createdBy, "Created delivery " + saved.getDeliveryNumber() + " from warehouse order " + order.getOrderNumber());

        return mapToDTO(saved);
    }

    // ==========================================
    // 2. QUERY DELIVERIES
    // ==========================================

    @Transactional(readOnly = true)
    public List<DeliveryDTO> getDeliveries(String tenantId, LocalDate date, DeliveryStatus status, UUID driverId, UUID vehicleId) {
        List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId);
        return deliveries.stream()
                .filter(d -> date == null || date.equals(d.getScheduledDate()))
                .filter(d -> status == null || status == d.getStatus())
                .filter(d -> driverId == null || driverId.equals(d.getDriverId()))
                .filter(d -> vehicleId == null || vehicleId.equals(d.getVehicleId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeliveryDTO getDeliveryById(String tenantId, UUID id) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
        return mapToDTO(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryDTO getSanitizedDeliveryForCustomer(String tenantId, UUID customerId, UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!delivery.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to customer delivery");
        }

        DeliveryDTO dto = mapToDTO(delivery);
        // Sanitize sensitive operational data
        dto.setNotes(null);
        dto.setDriverId(null);
        dto.setVehicleId(null);
        dto.setVehicleNumber(null);
        dto.setFailureNotes(null);
        return dto;
    }

    // ==========================================
    // 3. SCHEDULING & ASSIGNMENTS
    // ==========================================

    @Transactional
    public DeliveryDTO scheduleDelivery(String tenantId, UUID deliveryId, ScheduleDeliveryDTO request, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.SCHEDULED);

        delivery.setScheduledDate(request.getDate());
        delivery.setScheduledStartTime(request.getStartTime());
        delivery.setScheduledEndTime(request.getEndTime());
        delivery.setStatus(DeliveryStatus.SCHEDULED);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DELIVERY_SCHEDULED", performedBy, "Scheduled for " + request.getDate() + " " + request.getStartTime() + "-" + request.getEndTime());
        sendNotification(tenantId, updated, NotificationType.DELIVERY_SCHEDULED, "Your rental delivery is scheduled for " + request.getDate() + " between " + request.getStartTime() + " and " + request.getEndTime() + ".");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO assignDriver(String tenantId, UUID deliveryId, UUID driverId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        Driver driver = driverRepository.findByTenantIdAndId(tenantId, driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        if (!Boolean.TRUE.equals(driver.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver is inactive");
        }

        // Overlap collision check for driver
        LocalDate date = delivery.getScheduledDate() != null ? delivery.getScheduledDate() : LocalDate.now();
        List<DeliveryStatus> inactiveStatuses = List.of(DeliveryStatus.CANCELLED, DeliveryStatus.FAILED, DeliveryStatus.DELIVERED);
        List<Delivery> activeDriverDeliveries = deliveryRepository.findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(tenantId, driverId, date, inactiveStatuses);

        for (Delivery existing : activeDriverDeliveries) {
            if (!existing.getId().equals(delivery.getId()) && timeWindowsOverlap(delivery, existing)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver " + driver.getName() + " is already assigned to another delivery during this time.");
            }
        }

        delivery.setDriverId(driverId);
        if (delivery.getStatus() == DeliveryStatus.SCHEDULED || delivery.getStatus() == DeliveryStatus.PENDING) {
            delivery.setStatus(DeliveryStatus.ASSIGNED);
        }

        driver.setStatus(DriverStatus.ASSIGNED);
        driverRepository.save(driver);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DRIVER_ASSIGNED", performedBy, "Assigned driver " + driver.getName());
        sendNotification(tenantId, updated, NotificationType.DELIVERY_ASSIGNED, "Driver " + driver.getName() + " has been assigned to your delivery.");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO assignVehicle(String tenantId, UUID deliveryId, UUID vehicleId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (!Boolean.TRUE.equals(vehicle.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle is inactive");
        }

        // Overlap collision check for vehicle
        LocalDate date = delivery.getScheduledDate() != null ? delivery.getScheduledDate() : LocalDate.now();
        List<DeliveryStatus> inactiveStatuses = List.of(DeliveryStatus.CANCELLED, DeliveryStatus.FAILED, DeliveryStatus.DELIVERED);
        List<Delivery> activeVehicleDeliveries = deliveryRepository.findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(tenantId, vehicleId, date, inactiveStatuses);

        for (Delivery existing : activeVehicleDeliveries) {
            if (!existing.getId().equals(delivery.getId()) && timeWindowsOverlap(delivery, existing)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle " + vehicle.getVehicleNumber() + " is already assigned during this delivery window.");
            }
        }

        delivery.setVehicleId(vehicleId);
        vehicle.setStatus(VehicleStatus.ASSIGNED);
        vehicleRepository.save(vehicle);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "VEHICLE_ASSIGNED", performedBy, "Assigned vehicle " + vehicle.getVehicleNumber());

        return mapToDTO(updated);
    }

    // ==========================================
    // 4. LIFECYCLE OPERATIONS
    // ==========================================

    @Transactional
    public DeliveryDTO startDelivery(String tenantId, UUID deliveryId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (delivery.getDriverId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver must be assigned before starting delivery.");
        }
        if (delivery.getVehicleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle must be assigned before starting delivery.");
        }

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.OUT_FOR_DELIVERY);

        delivery.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        delivery.setActualStartTime(LocalDateTime.now());

        Driver driver = driverRepository.findByTenantIdAndId(tenantId, delivery.getDriverId()).orElse(null);
        if (driver != null) {
            driver.setStatus(DriverStatus.ON_DELIVERY);
            driverRepository.save(driver);
        }

        Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, delivery.getVehicleId()).orElse(null);
        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.IN_USE);
            vehicleRepository.save(vehicle);
        }

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DELIVERY_STARTED", performedBy, "Delivery started and out for delivery");
        sendNotification(tenantId, updated, NotificationType.DELIVERY_STARTED, "Your rental delivery is on the way.");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO arriveDelivery(String tenantId, UUID deliveryId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.ARRIVED);

        delivery.setStatus(DeliveryStatus.ARRIVED);
        delivery.setActualArrivalTime(LocalDateTime.now());

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DELIVERY_ARRIVED", performedBy, "Delivery arrived at destination");
        sendNotification(tenantId, updated, NotificationType.DELIVERY_ARRIVED, "Your rental delivery has arrived.");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO startSetup(String tenantId, UUID deliveryId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.SETUP_IN_PROGRESS);

        delivery.setStatus(DeliveryStatus.SETUP_IN_PROGRESS);
        delivery.setSetupRequired(true);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "SETUP_STARTED", performedBy, "Equipment setup in progress");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO completeDelivery(String tenantId, UUID deliveryId, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.DELIVERED);

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setActualCompletionTime(LocalDateTime.now());

        // Release driver and vehicle
        releaseDriverAndVehicle(tenantId, delivery);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DELIVERY_COMPLETED", performedBy, "Delivery completed successfully");
        sendNotification(tenantId, updated, NotificationType.DELIVERY_COMPLETED, "Your rental delivery has been completed.");

        return mapToDTO(updated);
    }

    @Transactional
    public DeliveryDTO failDelivery(String tenantId, UUID deliveryId, FailDeliveryDTO request, String performedBy) {
        Delivery delivery = deliveryRepository.findByTenantIdAndId(tenantId, deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        transitionService.validateTransition(delivery.getStatus(), DeliveryStatus.FAILED);

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setFailureReason(request.getReason());
        delivery.setFailureNotes(request.getNotes());

        releaseDriverAndVehicle(tenantId, delivery);

        Delivery updated = deliveryRepository.save(delivery);

        logAudit(tenantId, updated.getId(), null, "DELIVERY_FAILED", performedBy, "Delivery failed: " + request.getReason());
        sendNotification(tenantId, updated, NotificationType.DELIVERY_FAILED, "Delivery attempt failed: " + request.getReason());

        return mapToDTO(updated);
    }

    // ==========================================
    // 5. DRIVERS, VEHICLES & DASHBOARD
    // ==========================================

    @Transactional(readOnly = true)
    public List<DriverDTO> getDrivers(String tenantId) {
        return driverRepository.findByTenantId(tenantId).stream().map(this::mapDriverToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getVehicles(String tenantId) {
        return vehicleRepository.findByTenantId(tenantId).stream().map(this::mapVehicleToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeliveryDashboardDTO getDashboardMetrics(String tenantId) {
        LocalDate today = LocalDate.now();
        List<Delivery> todaysDeliveriesList = deliveryRepository.findByTenantIdAndScheduledDate(tenantId, today);

        DeliveryDashboardDTO dto = new DeliveryDashboardDTO();
        dto.setTodaysDeliveries(todaysDeliveriesList.size());
        dto.setUnassigned(todaysDeliveriesList.stream().filter(d -> d.getDriverId() == null).count());
        dto.setScheduled(todaysDeliveriesList.stream().filter(d -> d.getStatus() == DeliveryStatus.SCHEDULED).count());
        dto.setOutForDelivery(todaysDeliveriesList.stream().filter(d -> d.getStatus() == DeliveryStatus.OUT_FOR_DELIVERY).count());
        dto.setCompleted(todaysDeliveriesList.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count());
        dto.setUrgent(todaysDeliveriesList.stream().filter(d -> d.getPriority() == DeliveryPriority.URGENT || d.getPriority() == DeliveryPriority.HIGH).count());
        dto.setFailed(todaysDeliveriesList.stream().filter(d -> d.getStatus() == DeliveryStatus.FAILED).count());

        dto.setTodaysQueue(todaysDeliveriesList.stream().map(this::mapToDTO).collect(Collectors.toList()));
        return dto;
    }

    // ==========================================
    // 6. ROUTE PLANNING
    // ==========================================

    @Transactional
    public DeliveryRouteDTO createRoute(String tenantId, CreateRouteDTO request, String createdBy) {
        if (request.getDeliveryIds() == null || request.getDeliveryIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route must contain at least one delivery.");
        }

        DeliveryRoute route = new DeliveryRoute();
        route.setTenantId(tenantId);
        route.setRouteNumber("RTE-" + (routeRepository.countByTenantId(tenantId) + 1001));
        route.setRouteDate(request.getDate());
        route.setDriverId(request.getDriverId());
        route.setVehicleId(request.getVehicleId());
        route.setStatus(DeliveryRouteStatus.PLANNED);
        route.setTotalDeliveries(request.getDeliveryIds().size());

        DeliveryRoute savedRoute = routeRepository.save(route);

        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < request.getDeliveryIds().size(); i++) {
            UUID delId = request.getDeliveryIds().get(i);
            Delivery del = deliveryRepository.findByTenantIdAndId(tenantId, delId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery " + delId + " not found"));
            del.setSequenceNumber(i + 1);
            deliveryRepository.save(del);
            deliveries.add(del);

            DeliveryRouteStop stop = new DeliveryRouteStop();
            stop.setRouteId(savedRoute.getId());
            stop.setDeliveryId(del.getId());
            stop.setSequenceNumber(i + 1);
            stop.setStatus(del.getStatus());
            routeStopRepository.save(stop);
        }

        logAudit(tenantId, null, savedRoute.getId(), "ROUTE_CREATED", createdBy, "Created route " + savedRoute.getRouteNumber() + " with " + deliveries.size() + " stops");

        return mapRouteToDTO(savedRoute);
    }

    @Transactional(readOnly = true)
    public List<DeliveryRouteDTO> getRoutes(String tenantId, LocalDate date) {
        List<DeliveryRoute> routes = date != null ?
                routeRepository.findByTenantIdAndRouteDate(tenantId, date) :
                routeRepository.findByTenantId(tenantId);
        return routes.stream().map(this::mapRouteToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeliveryRouteDTO getRouteById(String tenantId, UUID routeId) {
        DeliveryRoute route = routeRepository.findByTenantIdAndId(tenantId, routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
        return mapRouteToDTO(route);
    }

    @Transactional
    public DeliveryRouteDTO updateRouteSequence(String tenantId, UUID routeId, SequenceUpdateDTO request, String updatedBy) {
        DeliveryRoute route = routeRepository.findByTenantIdAndId(tenantId, routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        routeStopRepository.deleteByRouteId(routeId);

        for (int i = 0; i < request.getDeliveryIds().size(); i++) {
            UUID delId = request.getDeliveryIds().get(i);
            Delivery del = deliveryRepository.findByTenantIdAndId(tenantId, delId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
            del.setSequenceNumber(i + 1);
            deliveryRepository.save(del);

            DeliveryRouteStop stop = new DeliveryRouteStop();
            stop.setRouteId(route.getId());
            stop.setDeliveryId(del.getId());
            stop.setSequenceNumber(i + 1);
            stop.setStatus(del.getStatus());
            routeStopRepository.save(stop);
        }

        logAudit(tenantId, null, route.getId(), "ROUTE_UPDATED", updatedBy, "Reordered route stops");

        return mapRouteToDTO(route);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private String generateDeliveryNumber(String tenantId) {
        long count = deliveryRepository.countByTenantId(tenantId) + 1;
        return String.format("DEL-%06d", count);
    }

    private String buildAddressSnapshot(Customer customer, Event event) {
        if (event != null && event.getVenueAddress() != null && !event.getVenueAddress().trim().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (event.getVenueName() != null) sb.append(event.getVenueName()).append(", ");
            sb.append(event.getVenueAddress());
            if (event.getCity() != null) sb.append(", ").append(event.getCity());
            if (event.getState() != null) sb.append(", ").append(event.getState());
            if (event.getZipCode() != null) sb.append(" ").append(event.getZipCode());
            return sb.toString();
        } else if (customer != null) {
            StringBuilder sb = new StringBuilder();
            if (customer.getShippingAddress() != null) sb.append(customer.getShippingAddress());
            else if (customer.getBillingAddress() != null) sb.append(customer.getBillingAddress());
            if (customer.getCity() != null) sb.append(", ").append(customer.getCity());
            if (customer.getState() != null) sb.append(", ").append(customer.getState());
            if (customer.getZipCode() != null) sb.append(" ").append(customer.getZipCode());
            return sb.length() > 0 ? sb.toString() : "123 Main Street, New York, NY 10001";
        }
        return "123 Main Street, New York, NY 10001";
    }

    private boolean timeWindowsOverlap(Delivery d1, Delivery d2) {
        if (d1.getScheduledStartTime() == null || d1.getScheduledEndTime() == null ||
            d2.getScheduledStartTime() == null || d2.getScheduledEndTime() == null) {
            return true; // Default fallback overlap check if times unspecified
        }
        LocalTime start1 = LocalTime.parse(d1.getScheduledStartTime());
        LocalTime end1 = LocalTime.parse(d1.getScheduledEndTime());
        LocalTime start2 = LocalTime.parse(d2.getScheduledStartTime());
        LocalTime end2 = LocalTime.parse(d2.getScheduledEndTime());

        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void releaseDriverAndVehicle(String tenantId, Delivery delivery) {
        if (delivery.getDriverId() != null) {
            Driver driver = driverRepository.findByTenantIdAndId(tenantId, delivery.getDriverId()).orElse(null);
            if (driver != null) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.save(driver);
            }
        }
        if (delivery.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, delivery.getVehicleId()).orElse(null);
            if (vehicle != null) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
        }
    }

    private void logAudit(String tenantId, UUID deliveryId, UUID routeId, String action, String performedBy, String details) {
        DeliveryAudit audit = new DeliveryAudit();
        audit.setTenantId(tenantId);
        audit.setDeliveryId(deliveryId);
        audit.setRouteId(routeId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        audit.setDetails(details);
        auditRepository.save(audit);
    }

    private void sendNotification(String tenantId, Delivery delivery, NotificationType type, String message) {
        try {
            Customer customer = customerRepository.findByTenantIdAndId(tenantId, delivery.getCustomerId()).orElse(null);
            NotificationRequestDTO request = new NotificationRequestDTO();
            request.setTenantId(tenantId);
            request.setRecipientCustomerId(delivery.getCustomerId());
            request.setRecipientEmail(customer != null ? customer.getEmail() : "customer@example.com");
            request.setType(type);
            request.setChannel(NotificationChannel.EMAIL);
            request.setCustomTitle("RentFlow Delivery Update: " + delivery.getDeliveryNumber());
            request.setCustomMessage(message);

            notificationService.sendNotification(request);
        } catch (Exception e) {
            // Log notification error silently without breaking delivery operation
        }
    }

    private DeliveryDTO mapToDTO(Delivery delivery) {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(delivery.getId());
        dto.setTenantId(delivery.getTenantId());
        dto.setDeliveryNumber(delivery.getDeliveryNumber());
        dto.setBookingId(delivery.getBookingId());
        dto.setWarehouseOrderId(delivery.getWarehouseOrderId());
        dto.setCustomerId(delivery.getCustomerId());
        dto.setEventId(delivery.getEventId());
        dto.setDeliveryType(delivery.getDeliveryType());
        dto.setStatus(delivery.getStatus());
        dto.setPriority(delivery.getPriority());
        dto.setScheduledDate(delivery.getScheduledDate());
        dto.setScheduledStartTime(delivery.getScheduledStartTime());
        dto.setScheduledEndTime(delivery.getScheduledEndTime());
        dto.setDeliveryAddressSnapshot(delivery.getDeliveryAddressSnapshot());
        dto.setLatitude(delivery.getLatitude());
        dto.setLongitude(delivery.getLongitude());
        dto.setDriverId(delivery.getDriverId());
        dto.setVehicleId(delivery.getVehicleId());
        dto.setSequenceNumber(delivery.getSequenceNumber());
        dto.setEstimatedDistance(delivery.getEstimatedDistance());
        dto.setEstimatedDuration(delivery.getEstimatedDuration());
        dto.setActualStartTime(delivery.getActualStartTime());
        dto.setActualArrivalTime(delivery.getActualArrivalTime());
        dto.setActualCompletionTime(delivery.getActualCompletionTime());
        dto.setNotes(delivery.getNotes());
        dto.setCustomerNotes(delivery.getCustomerNotes());
        dto.setSetupRequired(delivery.getSetupRequired());
        dto.setSetupDurationMinutes(delivery.getSetupDurationMinutes());
        dto.setFailureReason(delivery.getFailureReason());
        dto.setFailureNotes(delivery.getFailureNotes());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());

        // Resolve relations for display
        if (delivery.getDriverId() != null) {
            driverRepository.findByTenantIdAndId(delivery.getTenantId(), delivery.getDriverId())
                    .ifPresent(drv -> dto.setDriverName(drv.getName()));
        }
        if (delivery.getVehicleId() != null) {
            vehicleRepository.findByTenantIdAndId(delivery.getTenantId(), delivery.getVehicleId())
                    .ifPresent(veh -> dto.setVehicleNumber(veh.getVehicleNumber()));
        }
        if (delivery.getCustomerId() != null) {
            customerRepository.findByTenantIdAndId(delivery.getTenantId(), delivery.getCustomerId())
                    .ifPresent(cust -> dto.setCustomerName(cust.getFirstName() + " " + cust.getLastName()));
        }
        if (delivery.getEventId() != null) {
            eventRepository.findByTenantIdAndId(delivery.getTenantId(), delivery.getEventId())
                    .ifPresent(ev -> dto.setEventName(ev.getEventName()));
        }
        if (delivery.getBookingId() != null) {
            bookingRepository.findByTenantIdAndId(delivery.getTenantId(), delivery.getBookingId())
                    .ifPresent(b -> dto.setBookingNumber(b.getBookingNumber()));
        }

        return dto;
    }

    private DriverDTO mapDriverToDTO(Driver driver) {
        DriverDTO dto = new DriverDTO();
        dto.setId(driver.getId());
        dto.setTenantId(driver.getTenantId());
        dto.setUserId(driver.getUserId());
        dto.setName(driver.getName());
        dto.setPhone(driver.getPhone());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setStatus(driver.getStatus());
        dto.setActive(driver.getActive());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setUpdatedAt(driver.getUpdatedAt());
        return dto;
    }

    private VehicleDTO mapVehicleToDTO(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setTenantId(vehicle.getTenantId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setName(vehicle.getName());
        dto.setType(vehicle.getType());
        dto.setCapacity(vehicle.getCapacity());
        dto.setActive(vehicle.getActive());
        dto.setStatus(vehicle.getStatus());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        return dto;
    }

    private DeliveryRouteDTO mapRouteToDTO(DeliveryRoute route) {
        DeliveryRouteDTO dto = new DeliveryRouteDTO();
        dto.setId(route.getId());
        dto.setTenantId(route.getTenantId());
        dto.setRouteNumber(route.getRouteNumber());
        dto.setDriverId(route.getDriverId());
        dto.setVehicleId(route.getVehicleId());
        dto.setRouteDate(route.getRouteDate());
        dto.setStatus(route.getStatus());
        dto.setTotalDeliveries(route.getTotalDeliveries());
        dto.setEstimatedDistance(route.getEstimatedDistance());
        dto.setEstimatedDuration(route.getEstimatedDuration());
        dto.setCreatedAt(route.getCreatedAt());
        dto.setUpdatedAt(route.getUpdatedAt());

        if (route.getDriverId() != null) {
            driverRepository.findByTenantIdAndId(route.getTenantId(), route.getDriverId())
                    .ifPresent(d -> dto.setDriverName(d.getName()));
        }
        if (route.getVehicleId() != null) {
            vehicleRepository.findByTenantIdAndId(route.getTenantId(), route.getVehicleId())
                    .ifPresent(v -> dto.setVehicleNumber(v.getVehicleNumber()));
        }

        List<DeliveryRouteStop> stops = routeStopRepository.findByRouteIdOrderBySequenceNumberAsc(route.getId());
        dto.setStops(stops.stream().map(s -> {
            DeliveryRouteStopDTO stopDto = new DeliveryRouteStopDTO();
            stopDto.setId(s.getId());
            stopDto.setRouteId(s.getRouteId());
            stopDto.setDeliveryId(s.getDeliveryId());
            stopDto.setSequenceNumber(s.getSequenceNumber());
            stopDto.setEstimatedArrival(s.getEstimatedArrival());
            stopDto.setActualArrival(s.getActualArrival());
            stopDto.setStatus(s.getStatus());

            deliveryRepository.findByTenantIdAndId(route.getTenantId(), s.getDeliveryId()).ifPresent(del -> {
                stopDto.setDeliveryNumber(del.getDeliveryNumber());
                stopDto.setDeliveryAddress(del.getDeliveryAddressSnapshot());
                customerRepository.findByTenantIdAndId(route.getTenantId(), del.getCustomerId())
                        .ifPresent(c -> stopDto.setCustomerName(c.getFirstName() + " " + c.getLastName()));
            });
            return stopDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
