package com.rentflow.returns.service;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.DriverStatus;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.model.VehicleStatus;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.delivery.service.DeliveryService;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationChannel;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;

import com.rentflow.returns.dto.*;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReturnService {

    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnOrderItemRepository returnOrderItemRepository;
    private final InspectionRepository inspectionRepository;
    private final DamageRecordRepository damageRecordRepository;
    private final ReturnAuditRepository auditRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final DeliveryService deliveryService;
    private final ReturnStatusTransitionService transitionService;
    private final NotificationService notificationService;
    private final InspectionRecommendationService recommendationService;

    public ReturnService(
            ReturnOrderRepository returnOrderRepository,
            ReturnOrderItemRepository returnOrderItemRepository,
            InspectionRepository inspectionRepository,
            DamageRecordRepository damageRecordRepository,
            ReturnAuditRepository auditRepository,
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            EventRepository eventRepository,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            InventoryReservationRepository reservationRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            DeliveryService deliveryService,
            ReturnStatusTransitionService transitionService,
            NotificationService notificationService,
            InspectionRecommendationService recommendationService) {
        this.returnOrderRepository = returnOrderRepository;
        this.returnOrderItemRepository = returnOrderItemRepository;
        this.inspectionRepository = inspectionRepository;
        this.damageRecordRepository = damageRecordRepository;
        this.auditRepository = auditRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.deliveryService = deliveryService;
        this.transitionService = transitionService;
        this.notificationService = notificationService;
        this.recommendationService = recommendationService;
    }

    // ==========================================
    // 1. CREATE RETURN FROM BOOKING
    // ==========================================

    @Transactional
    public ReturnOrderDTO createFromBooking(String tenantId, UUID bookingId, String createdBy) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        // Check if an active return order already exists
        List<ReturnOrderStatus> inactiveStatuses = List.of(ReturnOrderStatus.CANCELLED, ReturnOrderStatus.FAILED);
        Optional<ReturnOrder> existing = returnOrderRepository.findByTenantIdAndBookingIdAndStatusNotIn(tenantId, bookingId, inactiveStatuses);
        if (existing.isPresent()) {
            return mapToDTO(existing.get());
        }

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, booking.getCustomerId()).orElse(null);
        Event event = eventRepository.findByTenantIdAndId(tenantId, booking.getEventId()).orElse(null);

        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setTenantId(tenantId);
        returnOrder.setReturnNumber(generateReturnNumber(tenantId));
        returnOrder.setBookingId(booking.getId());
        returnOrder.setCustomerId(booking.getCustomerId());
        returnOrder.setEventId(booking.getEventId());
        returnOrder.setStatus(ReturnOrderStatus.PENDING);
        returnOrder.setPriority(ReturnPriority.NORMAL);

        LocalDate scheduledDate = booking.getRentalEndDateTime() != null ? booking.getRentalEndDateTime().toLocalDate() : LocalDate.now();
        returnOrder.setScheduledDate(scheduledDate);
        returnOrder.setScheduledStartTime("10:00");
        returnOrder.setScheduledEndTime("12:00");

        String addressSnapshot = buildAddressSnapshot(customer, event);
        returnOrder.setPickupAddressSnapshot(addressSnapshot);

        ReturnOrder saved = returnOrderRepository.save(returnOrder);

        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(bookingId);
        for (BookingItem bi : bookingItems) {
            ReturnOrderItem roi = new ReturnOrderItem();
            roi.setTenantId(tenantId);
            roi.setReturnOrderId(saved.getId());
            roi.setBookingItemId(bi.getId());
            roi.setProductId(bi.getProductId());

            Product product = productRepository.findByTenantIdAndId(tenantId, bi.getProductId()).orElse(null);
            roi.setProductNameSnapshot(product != null ? product.getName() : "Item " + bi.getProductId());
            roi.setSkuSnapshot(product != null ? product.getSku() : "SKU-" + bi.getProductId());

            roi.setQuantityExpected(bi.getQuantity());
            roi.setQuantityReceived(0);
            roi.setQuantityMissing(0);
            roi.setQuantityDamaged(0);
            roi.setQuantityGood(0);
            roi.setStatus(ReturnOrderItemStatus.PENDING);

            returnOrderItemRepository.save(roi);
        }

        logAudit(tenantId, saved.getId(), "RETURN_CREATED", createdBy, "Created return order " + saved.getReturnNumber() + " for booking " + booking.getBookingNumber());

        return mapToDTO(saved);
    }

    // ==========================================
    // 2. QUERY RETURNS
    // ==========================================

    @Transactional(readOnly = true)
    public List<ReturnOrderDTO> getReturns(String tenantId, LocalDate date, ReturnOrderStatus status, UUID driverId, UUID customerId, ReturnPriority priority, String search) {
        List<ReturnOrder> returns = returnOrderRepository.findByTenantId(tenantId);
        return returns.stream()
                .filter(r -> date == null || date.equals(r.getScheduledDate()))
                .filter(r -> status == null || status == r.getStatus())
                .filter(r -> driverId == null || driverId.equals(r.getDriverId()))
                .filter(r -> customerId == null || customerId.equals(r.getCustomerId()))
                .filter(r -> priority == null || priority == r.getPriority())
                .map(this::mapToDTO)
                .filter(dto -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.toLowerCase();
                    return (dto.getReturnNumber() != null && dto.getReturnNumber().toLowerCase().contains(q)) ||
                           (dto.getBookingNumber() != null && dto.getBookingNumber().toLowerCase().contains(q)) ||
                           (dto.getCustomerName() != null && dto.getCustomerName().toLowerCase().contains(q)) ||
                           (dto.getEventName() != null && dto.getEventName().toLowerCase().contains(q));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReturnOrderDTO getReturnById(String tenantId, UUID id) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));
        return mapToDTO(returnOrder);
    }

    // ==========================================
    // 3. SCHEDULING & ASSIGNMENTS
    // ==========================================

    @Transactional
    public ReturnOrderDTO scheduleReturn(String tenantId, UUID returnId, ScheduleReturnDTO request, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.SCHEDULED);

        returnOrder.setScheduledDate(request.getDate());
        returnOrder.setScheduledStartTime(request.getStartTime());
        returnOrder.setScheduledEndTime(request.getEndTime());
        returnOrder.setStatus(ReturnOrderStatus.SCHEDULED);

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_SCHEDULED", performedBy, "Scheduled pickup for " + request.getDate() + " " + request.getStartTime() + "-" + request.getEndTime());
        sendNotification(tenantId, updated, NotificationType.RETURN_SCHEDULED, "Pickup for your rental return is scheduled for " + request.getDate() + " between " + request.getStartTime() + " and " + request.getEndTime() + ".");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO assignDriver(String tenantId, UUID returnId, UUID driverId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        Driver driver = driverRepository.findByTenantIdAndId(tenantId, driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        if (!Boolean.TRUE.equals(driver.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver is inactive");
        }

        LocalDate date = returnOrder.getScheduledDate() != null ? returnOrder.getScheduledDate() : LocalDate.now();
        String startTime = returnOrder.getScheduledStartTime() != null ? returnOrder.getScheduledStartTime() : "10:00";
        String endTime = returnOrder.getScheduledEndTime() != null ? returnOrder.getScheduledEndTime() : "12:00";

        // Check return order conflicts for driver
        List<ReturnOrderStatus> inactiveReturnStatuses = List.of(ReturnOrderStatus.CANCELLED, ReturnOrderStatus.FAILED, ReturnOrderStatus.COMPLETED);
        List<ReturnOrder> activeReturnOrders = returnOrderRepository.findByTenantIdAndDriverIdAndScheduledDateAndStatusNotIn(tenantId, driverId, date, inactiveReturnStatuses);

        for (ReturnOrder existing : activeReturnOrders) {
            if (!existing.getId().equals(returnOrder.getId()) && deliveryService.timeWindowsOverlap(startTime, endTime, existing.getScheduledStartTime(), existing.getScheduledEndTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver " + driver.getName() + " is already assigned to another pickup during this time.");
            }
        }

        // Cross conflict check against active Deliveries from Day 17!
        if (deliveryService.hasDeliveryDriverConflict(tenantId, driverId, date, startTime, endTime, null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver " + driver.getName() + " is already assigned to another delivery during this time.");
        }

        returnOrder.setDriverId(driverId);
        if (returnOrder.getStatus() == ReturnOrderStatus.SCHEDULED || returnOrder.getStatus() == ReturnOrderStatus.PENDING) {
            returnOrder.setStatus(ReturnOrderStatus.ASSIGNED);
        }

        driver.setStatus(DriverStatus.ASSIGNED);
        driverRepository.save(driver);

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_DRIVER_ASSIGNED", performedBy, "Assigned driver " + driver.getName());
        sendNotification(tenantId, updated, NotificationType.RETURN_ASSIGNED, "Driver " + driver.getName() + " has been assigned for equipment pickup.");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO assignVehicle(String tenantId, UUID returnId, UUID vehicleId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (!Boolean.TRUE.equals(vehicle.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle is inactive");
        }

        LocalDate date = returnOrder.getScheduledDate() != null ? returnOrder.getScheduledDate() : LocalDate.now();
        String startTime = returnOrder.getScheduledStartTime() != null ? returnOrder.getScheduledStartTime() : "10:00";
        String endTime = returnOrder.getScheduledEndTime() != null ? returnOrder.getScheduledEndTime() : "12:00";

        // Check return order conflicts for vehicle
        List<ReturnOrderStatus> inactiveReturnStatuses = List.of(ReturnOrderStatus.CANCELLED, ReturnOrderStatus.FAILED, ReturnOrderStatus.COMPLETED);
        List<ReturnOrder> activeReturnOrders = returnOrderRepository.findByTenantIdAndVehicleIdAndScheduledDateAndStatusNotIn(tenantId, vehicleId, date, inactiveReturnStatuses);

        for (ReturnOrder existing : activeReturnOrders) {
            if (!existing.getId().equals(returnOrder.getId()) && deliveryService.timeWindowsOverlap(startTime, endTime, existing.getScheduledStartTime(), existing.getScheduledEndTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle " + vehicle.getVehicleNumber() + " is already assigned during this pickup window.");
            }
        }

        // Cross conflict check against active Deliveries
        if (deliveryService.hasDeliveryVehicleConflict(tenantId, vehicleId, date, startTime, endTime, null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle " + vehicle.getVehicleNumber() + " is already assigned during this window.");
        }

        returnOrder.setVehicleId(vehicleId);
        vehicle.setStatus(VehicleStatus.ASSIGNED);
        vehicleRepository.save(vehicle);

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_VEHICLE_ASSIGNED", performedBy, "Assigned vehicle " + vehicle.getVehicleNumber());

        return mapToDTO(updated);
    }

    // ==========================================
    // 4. PICKUP WORKFLOW (DRIVER MOBILE)
    // ==========================================

    @Transactional
    public ReturnOrderDTO startPickup(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        if (returnOrder.getDriverId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver must be assigned before starting pickup.");
        }
        if (returnOrder.getVehicleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle must be assigned before starting pickup.");
        }

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.OUT_FOR_PICKUP);

        returnOrder.setStatus(ReturnOrderStatus.OUT_FOR_PICKUP);
        returnOrder.setActualPickupStartTime(LocalDateTime.now());

        Driver driver = driverRepository.findByTenantIdAndId(tenantId, returnOrder.getDriverId()).orElse(null);
        if (driver != null) {
            driver.setStatus(DriverStatus.ON_DELIVERY);
            driverRepository.save(driver);
        }

        Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, returnOrder.getVehicleId()).orElse(null);
        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.IN_USE);
            vehicleRepository.save(vehicle);
        }

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_PICKUP_STARTED", performedBy, "Pickup started and driver is on route.");
        sendNotification(tenantId, updated, NotificationType.RETURN_PICKUP_STARTED, "Driver is on the way for equipment pickup.");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO arrivePickup(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.ARRIVED);

        returnOrder.setStatus(ReturnOrderStatus.ARRIVED);
        returnOrder.setActualArrivalTime(LocalDateTime.now());

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_PICKUP_ARRIVED", performedBy, "Driver arrived at pickup location.");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO pickupComplete(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.PICKED_UP);

        returnOrder.setStatus(ReturnOrderStatus.PICKED_UP);
        returnOrder.setActualPickupTime(LocalDateTime.now());

        // Release driver and vehicle
        releaseDriverAndVehicle(tenantId, returnOrder);

        // Update booking status to PICKED_UP
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, returnOrder.getBookingId()).orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.PICKED_UP);
            bookingRepository.save(booking);
        }

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_PICKED_UP", performedBy, "Driver confirmed equipment pickup complete.");
        sendNotification(tenantId, updated, NotificationType.RETURN_PICKED_UP, "Equipment pickup has been completed.");

        return mapToDTO(updated);
    }

    // ==========================================
    // 5. WAREHOUSE CHECK-IN WORKFLOW
    // ==========================================

    @Transactional
    public ReturnOrderDTO startCheckIn(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.CHECK_IN);

        returnOrder.setStatus(ReturnOrderStatus.CHECK_IN);
        returnOrder.setActualCheckInTime(LocalDateTime.now());

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_CHECK_IN_STARTED", performedBy, "Warehouse started equipment check-in.");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO recordCheckIn(String tenantId, UUID returnId, CheckInRequestDTO request, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        if (request == null || request.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in item data is required.");
        }

        List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, returnId);
        Map<UUID, ReturnOrderItem> itemMap = items.stream().collect(Collectors.toMap(ReturnOrderItem::getId, i -> i));

        boolean hasMissingItems = false;

        for (CheckInRequestDTO.ItemQuantity entry : request.getItems()) {
            ReturnOrderItem item = itemMap.get(entry.getReturnItemId());
            if (item == null) continue;

            int received = entry.getQuantityReceived();
            if (received > item.getQuantityExpected()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received quantity cannot exceed expected quantity.");
            }

            int missing = Math.max(0, item.getQuantityExpected() - received);
            item.setQuantityReceived(received);
            item.setQuantityMissing(missing);

            if (missing > 0) {
                item.setStatus(ReturnOrderItemStatus.MISSING);
                hasMissingItems = true;
                logAudit(tenantId, returnId, "RETURN_ITEM_MISSING", performedBy, "Item " + item.getProductNameSnapshot() + " missing " + missing + " units (expected " + item.getQuantityExpected() + ", received " + received + ").");
            } else {
                item.setStatus(ReturnOrderItemStatus.RECEIVED);
            }

            returnOrderItemRepository.save(item);
        }

        if (hasMissingItems) {
            sendNotification(tenantId, returnOrder, NotificationType.RETURN_ITEM_MISSING, "Missing items identified during check-in for Return " + returnOrder.getReturnNumber() + ".");
        }

        logAudit(tenantId, returnId, "RETURN_CHECK_IN_RECORDED", performedBy, "Recorded received quantities for return check-in.");

        return mapToDTO(returnOrder);
    }

    // ==========================================
    // 6. INSPECTION WORKFLOW
    // ==========================================

    @Transactional
    public ReturnOrderDTO startInspection(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.INSPECTION);

        returnOrder.setStatus(ReturnOrderStatus.INSPECTION);
        returnOrder.setActualInspectionTime(LocalDateTime.now());

        // Update booking status to INSPECTING
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, returnOrder.getBookingId()).orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.INSPECTING);
            bookingRepository.save(booking);
        }

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_INSPECTION_STARTED", performedBy, "Warehouse started inspection.");
        sendNotification(tenantId, updated, NotificationType.RETURN_READY_FOR_INSPECTION, "Return equipment is now under inspection.");

        return mapToDTO(updated);
    }

    @Transactional
    public ReturnOrderDTO recordInspection(String tenantId, UUID returnId, InspectionRequestDTO request, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        if (request == null || request.getItems() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inspection item data is required.");
        }

        List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, returnId);
        Map<UUID, ReturnOrderItem> itemMap = items.stream().collect(Collectors.toMap(ReturnOrderItem::getId, i -> i));

        boolean hasDamagedItems = false;

        for (InspectionRequestDTO.ItemInspection entry : request.getItems()) {
            ReturnOrderItem item = itemMap.get(entry.getReturnItemId());
            if (item == null) continue;

            int good = entry.getGoodQuantity();
            int damaged = entry.getDamagedQuantity();
            int received = item.getQuantityReceived();

            // Strict condition validation rule (Step 20)
            if (good + damaged != received) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Good and damaged quantities must equal received quantity.");
            }

            item.setQuantityGood(good);
            item.setQuantityDamaged(damaged);
            if (damaged > 0) {
                item.setStatus(ReturnOrderItemStatus.DAMAGED);
                hasDamagedItems = true;
            } else {
                item.setStatus(ReturnOrderItemStatus.INSPECTED);
            }
            returnOrderItemRepository.save(item);

            // Save Inspection record
            Inspection inspection = inspectionRepository.findByTenantIdAndReturnOrderItemId(tenantId, item.getId())
                    .orElse(new Inspection());
            inspection.setTenantId(tenantId);
            inspection.setReturnOrderId(returnId);
            inspection.setReturnOrderItemId(item.getId());
            inspection.setProductId(item.getProductId());
            inspection.setInspectedQuantity(received);
            inspection.setGoodQuantity(good);
            inspection.setDamagedQuantity(damaged);
            inspection.setMissingQuantity(item.getQuantityMissing());
            inspection.setCondition(entry.getCondition() != null ? entry.getCondition() : (damaged > 0 ? InspectionCondition.MINOR_DAMAGE : InspectionCondition.GOOD));
            inspection.setNotes(entry.getNotes());
            inspection.setInspectedBy(performedBy != null ? performedBy : "Warehouse Staff");
            inspection.setInspectedAt(LocalDateTime.now());
            Inspection savedInspection = inspectionRepository.save(inspection);

            // Create DamageRecord if damaged
            if (damaged > 0) {
                DamageRecord dr = new DamageRecord();
                dr.setTenantId(tenantId);
                dr.setInspectionId(savedInspection.getId());
                dr.setReturnItemId(item.getId());
                dr.setProductId(item.getProductId());
                dr.setQuantity(damaged);
                dr.setCategory(entry.getDamageCategory() != null ? entry.getDamageCategory() : DamageCategory.OTHER);
                dr.setSeverity(entry.getDamageSeverity() != null ? entry.getDamageSeverity() : DamageSeverity.MINOR);
                dr.setDescription(entry.getDamageDescription() != null ? entry.getDamageDescription() : entry.getNotes());
                dr.setEstimatedRepairCost(entry.getEstimatedRepairCost() != null ? entry.getEstimatedRepairCost() : BigDecimal.ZERO);
                dr.setEstimatedReplacementCost(entry.getEstimatedReplacementCost() != null ? entry.getEstimatedReplacementCost() : BigDecimal.ZERO);
                dr.setStatus("OPEN");
                damageRecordRepository.save(dr);

                logAudit(tenantId, returnId, "RETURN_ITEM_DAMAGED", performedBy, "Item " + item.getProductNameSnapshot() + " recorded " + damaged + " damaged units (" + dr.getCategory() + ").");
            }
        }

        if (hasDamagedItems) {
            sendNotification(tenantId, returnOrder, NotificationType.RETURN_ITEM_DAMAGED, "Damaged items identified during inspection for Return " + returnOrder.getReturnNumber() + ".");
        }

        logAudit(tenantId, returnId, "RETURN_INSPECTION_RECORDED", performedBy, "Completed inspection recording.");

        return mapToDTO(returnOrder);
    }

    // ==========================================
    // 7. COMPLETE RETURN & INVENTORY RECONCILIATION
    // ==========================================

    @Transactional
    public ReturnOrderDTO completeReturn(String tenantId, UUID returnId, String performedBy) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        transitionService.validateTransition(returnOrder.getStatus(), ReturnOrderStatus.COMPLETED);

        List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, returnId);

        // Transactional Inventory State Updates (Step 23 & 24)
        for (ReturnOrderItem item : items) {
            Product product = productRepository.findByTenantIdAndId(tenantId, item.getProductId()).orElse(null);
            if (product == null) continue;

            // 1. Good quantity -> AVAILABLE
            int good = item.getQuantityGood();
            if (good > 0) {
                InventoryTransaction txGood = new InventoryTransaction();
                txGood.setTenantId(tenantId);
                txGood.setProductId(product.getId());
                txGood.setTransactionType(TransactionType.RETURN);
                txGood.setQuantity(good);
                txGood.setReferenceType("RETURN_ORDER");
                txGood.setReferenceId(returnId);
                txGood.setNotes("Return completed: " + good + " good units available");
                txGood.setCreatedBy(performedBy);
                inventoryTransactionRepository.save(txGood);
                logAudit(tenantId, returnId, "INVENTORY_RETURNED_TO_AVAILABLE", performedBy, "Product " + product.getName() + ": " + good + " units returned to AVAILABLE");
            }

            // 2. Damaged quantity -> MAINTENANCE / DAMAGED
            int damaged = item.getQuantityDamaged();
            if (damaged > 0) {
                // Find inspection to determine condition
                Optional<Inspection> insp = inspectionRepository.findByTenantIdAndReturnOrderItemId(tenantId, item.getId());
                InspectionCondition cond = insp.map(Inspection::getCondition).orElse(InspectionCondition.MINOR_DAMAGE);

                if (cond == InspectionCondition.UNUSABLE || cond == InspectionCondition.MAJOR_DAMAGE) {
                    product.setQuantityDamaged(product.getQuantityDamaged() + damaged);
                    InventoryTransaction txDam = new InventoryTransaction();
                    txDam.setTenantId(tenantId);
                    txDam.setProductId(product.getId());
                    txDam.setTransactionType(TransactionType.DAMAGE);
                    txDam.setQuantity(damaged);
                    txDam.setReferenceType("RETURN_ORDER");
                    txDam.setReferenceId(returnId);
                    txDam.setNotes("Return completed: " + damaged + " units marked DAMAGED (" + cond + ")");
                    txDam.setCreatedBy(performedBy);
                    inventoryTransactionRepository.save(txDam);
                    logAudit(tenantId, returnId, "INVENTORY_MARKED_DAMAGED", performedBy, "Product " + product.getName() + ": " + damaged + " units marked DAMAGED");
                } else {
                    product.setQuantityInMaintenance(product.getQuantityInMaintenance() + damaged);
                    InventoryTransaction txMaint = new InventoryTransaction();
                    txMaint.setTenantId(tenantId);
                    txMaint.setProductId(product.getId());
                    txMaint.setTransactionType(TransactionType.MAINTENANCE);
                    txMaint.setQuantity(damaged);
                    txMaint.setReferenceType("RETURN_ORDER");
                    txMaint.setReferenceId(returnId);
                    txMaint.setNotes("Return completed: " + damaged + " units marked MAINTENANCE (" + cond + ")");
                    txMaint.setCreatedBy(performedBy);
                    inventoryTransactionRepository.save(txMaint);
                    logAudit(tenantId, returnId, "INVENTORY_MARKED_MAINTENANCE", performedBy, "Product " + product.getName() + ": " + damaged + " units marked MAINTENANCE");
                }
            }

            // 3. Missing quantity -> LOST
            int missing = item.getQuantityMissing();
            if (missing > 0) {
                product.setQuantityLost(product.getQuantityLost() + missing);
                InventoryTransaction txLost = new InventoryTransaction();
                txLost.setTenantId(tenantId);
                txLost.setProductId(product.getId());
                txLost.setTransactionType(TransactionType.LOSS);
                txLost.setQuantity(missing);
                txLost.setReferenceType("RETURN_ORDER");
                txLost.setReferenceId(returnId);
                txLost.setNotes("Return completed: " + missing + " units marked LOST");
                txLost.setCreatedBy(performedBy);
                inventoryTransactionRepository.save(txLost);
                logAudit(tenantId, returnId, "INVENTORY_MARKED_LOST", performedBy, "Product " + product.getName() + ": " + missing + " units marked LOST");
            }

            productRepository.save(product);
        }

        // Release inventory reservations for this booking
        List<InventoryReservation> reservations = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> returnOrder.getBookingId().equals(r.getBookingId()) &&
                             (r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING))
                .collect(Collectors.toList());

        for (InventoryReservation res : reservations) {
            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);
        }

        // Update Driver & Vehicle to AVAILABLE
        releaseDriverAndVehicle(tenantId, returnOrder);

        // Update Booking Status to RETURNED (Step 25 & 26)
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, returnOrder.getBookingId()).orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.RETURNED);
            bookingRepository.save(booking);
        }

        returnOrder.setStatus(ReturnOrderStatus.COMPLETED);
        returnOrder.setCompletedAt(LocalDateTime.now());

        ReturnOrder updated = returnOrderRepository.save(returnOrder);

        logAudit(tenantId, updated.getId(), "RETURN_COMPLETED", performedBy, "Return processing completed and inventory reconciled.");
        sendNotification(tenantId, updated, NotificationType.RETURN_COMPLETED, "Rental return " + updated.getReturnNumber() + " completed.");

        return mapToDTO(updated);
    }

    // ==========================================
    // 8. DASHBOARDS & SUMMARIES
    // ==========================================

    @Transactional(readOnly = true)
    public ReturnsDashboardDTO getReturnsDashboard(String tenantId) {
        LocalDate today = LocalDate.now();
        List<ReturnOrder> todaysReturns = returnOrderRepository.findByTenantIdAndScheduledDate(tenantId, today);
        List<ReturnOrder> allReturns = returnOrderRepository.findByTenantId(tenantId);

        ReturnsDashboardDTO dto = new ReturnsDashboardDTO();
        dto.setTodaysPickups(todaysReturns.size());
        dto.setUnassigned(todaysReturns.stream().filter(r -> r.getDriverId() == null).count());
        dto.setScheduled(todaysReturns.stream().filter(r -> r.getStatus() == ReturnOrderStatus.SCHEDULED).count());
        dto.setOutForPickup(todaysReturns.stream().filter(r -> r.getStatus() == ReturnOrderStatus.OUT_FOR_PICKUP).count());
        dto.setPickedUp(todaysReturns.stream().filter(r -> r.getStatus() == ReturnOrderStatus.PICKED_UP).count());
        dto.setPendingInspection(allReturns.stream().filter(r -> r.getStatus() == ReturnOrderStatus.CHECK_IN || r.getStatus() == ReturnOrderStatus.INSPECTION).count());

        long missingCount = 0;
        long damagedCount = 0;
        for (ReturnOrder ro : allReturns) {
            List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, ro.getId());
            missingCount += items.stream().mapToInt(ReturnOrderItem::getQuantityMissing).sum();
            damagedCount += items.stream().mapToInt(ReturnOrderItem::getQuantityDamaged).sum();
        }
        dto.setMissingItems(missingCount);
        dto.setDamagedItems(damagedCount);

        dto.setTodaysQueue(todaysReturns.stream().map(this::mapToDTO).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public InspectionDashboardDTO getInspectionDashboard(String tenantId) {
        List<ReturnOrder> allReturns = returnOrderRepository.findByTenantId(tenantId);
        List<ReturnOrder> pending = allReturns.stream()
                .filter(r -> r.getStatus() == ReturnOrderStatus.CHECK_IN || r.getStatus() == ReturnOrderStatus.INSPECTION || r.getStatus() == ReturnOrderStatus.PICKED_UP)
                .collect(Collectors.toList());

        List<Inspection> inspections = inspectionRepository.findByTenantId(tenantId);
        long goodSum = inspections.stream().mapToLong(Inspection::getGoodQuantity).sum();
        long damagedSum = inspections.stream().mapToLong(Inspection::getDamagedQuantity).sum();
        long missingSum = inspections.stream().mapToLong(Inspection::getMissingQuantity).sum();

        List<Product> products = productRepository.findByTenantId(tenantId);
        long maintSum = products.stream().mapToLong(Product::getQuantityInMaintenance).sum();

        InspectionDashboardDTO dto = new InspectionDashboardDTO();
        dto.setPendingInspection(pending.size());
        dto.setGoodItems(goodSum);
        dto.setDamagedItems(damagedSum);
        dto.setMissingItems(missingSum);
        dto.setMaintenanceRequired(maintSum);

        dto.setPendingInspectionList(pending.stream().map(this::mapToDTO).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DamageRecordDTO> getDamageRecords(String tenantId) {
        List<DamageRecord> records = damageRecordRepository.findByTenantId(tenantId);
        return records.stream().map(r -> {
            DamageRecordDTO dto = new DamageRecordDTO();
            dto.setId(r.getId());
            dto.setTenantId(r.getTenantId());
            dto.setInspectionId(r.getInspectionId());
            dto.setReturnItemId(r.getReturnItemId());
            dto.setProductId(r.getProductId());
            dto.setQuantity(r.getQuantity());
            dto.setCategory(r.getCategory());
            dto.setSeverity(r.getSeverity());
            dto.setDescription(r.getDescription());
            dto.setEstimatedRepairCost(r.getEstimatedRepairCost());
            dto.setEstimatedReplacementCost(r.getEstimatedReplacementCost());
            dto.setStatus(r.getStatus());
            dto.setCreatedAt(r.getCreatedAt());

            productRepository.findByTenantIdAndId(tenantId, r.getProductId()).ifPresent(p -> {
                dto.setProductName(p.getName());
                dto.setProductSku(p.getSku());
            });

            if (r.getReturnItemId() != null) {
                returnOrderItemRepository.findByTenantIdAndId(tenantId, r.getReturnItemId()).ifPresent(item -> {
                    returnOrderRepository.findByTenantIdAndId(tenantId, item.getReturnOrderId()).ifPresent(ro -> {
                        dto.setReturnNumber(ro.getReturnNumber());
                        customerRepository.findByTenantIdAndId(tenantId, ro.getCustomerId()).ifPresent(c -> {
                            dto.setCustomerName(c.getFirstName() + " " + c.getLastName());
                        });
                    });
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerReturnSummaryDTO getCustomerReturnSummary(String tenantId, UUID returnId) {
        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found"));

        CustomerReturnSummaryDTO summary = new CustomerReturnSummaryDTO();
        summary.setReturnOrderId(returnOrder.getId());
        summary.setReturnNumber(returnOrder.getReturnNumber());
        summary.setStatus(returnOrder.getStatus());
        summary.setReturnDate(returnOrder.getScheduledDate());
        summary.setCompletedAt(returnOrder.getCompletedAt());

        bookingRepository.findByTenantIdAndId(tenantId, returnOrder.getBookingId())
                .ifPresent(b -> summary.setBookingNumber(b.getBookingNumber()));
        eventRepository.findByTenantIdAndId(tenantId, returnOrder.getEventId())
                .ifPresent(e -> summary.setEventName(e.getEventName()));

        List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(tenantId, returnId);
        summary.setItems(items.stream().map(i -> {
            CustomerReturnSummaryDTO.CustomerReturnItemSummary itemSummary = new CustomerReturnSummaryDTO.CustomerReturnItemSummary();
            itemSummary.setProductName(i.getProductNameSnapshot());
            itemSummary.setQuantityExpected(i.getQuantityExpected());
            itemSummary.setQuantityReturned(i.getQuantityReceived());
            itemSummary.setQuantityMissing(i.getQuantityMissing());
            return itemSummary;
        }).collect(Collectors.toList()));

        return summary;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private String generateReturnNumber(String tenantId) {
        long count = returnOrderRepository.countByTenantId(tenantId) + 1;
        return String.format("RET-%06d", count);
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

    private void releaseDriverAndVehicle(String tenantId, ReturnOrder returnOrder) {
        if (returnOrder.getDriverId() != null) {
            Driver driver = driverRepository.findByTenantIdAndId(tenantId, returnOrder.getDriverId()).orElse(null);
            if (driver != null) {
                driver.setStatus(DriverStatus.AVAILABLE);
                driverRepository.save(driver);
            }
        }
        if (returnOrder.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findByTenantIdAndId(tenantId, returnOrder.getVehicleId()).orElse(null);
            if (vehicle != null) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
        }
    }

    private void logAudit(String tenantId, UUID returnOrderId, String action, String performedBy, String details) {
        ReturnAudit audit = new ReturnAudit();
        audit.setTenantId(tenantId);
        audit.setReturnOrderId(returnOrderId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        audit.setDetails(details);
        auditRepository.save(audit);
    }

    private void sendNotification(String tenantId, ReturnOrder returnOrder, NotificationType type, String message) {
        try {
            Customer customer = customerRepository.findByTenantIdAndId(tenantId, returnOrder.getCustomerId()).orElse(null);
            NotificationRequestDTO request = new NotificationRequestDTO();
            request.setTenantId(tenantId);
            request.setRecipientCustomerId(returnOrder.getCustomerId());
            request.setRecipientEmail(customer != null ? customer.getEmail() : "customer@example.com");
            request.setType(type);
            request.setChannel(NotificationChannel.EMAIL);
            request.setCustomTitle("RentFlow Return Update: " + returnOrder.getReturnNumber());
            request.setCustomMessage(message);

            notificationService.sendNotification(request);
        } catch (Exception e) {
            // Log notification error silently
        }
    }

    private ReturnOrderDTO mapToDTO(ReturnOrder ro) {
        ReturnOrderDTO dto = new ReturnOrderDTO();
        dto.setId(ro.getId());
        dto.setTenantId(ro.getTenantId());
        dto.setReturnNumber(ro.getReturnNumber());
        dto.setBookingId(ro.getBookingId());
        dto.setDeliveryId(ro.getDeliveryId());
        dto.setCustomerId(ro.getCustomerId());
        dto.setEventId(ro.getEventId());
        dto.setStatus(ro.getStatus());
        dto.setPriority(ro.getPriority());
        dto.setScheduledDate(ro.getScheduledDate());
        dto.setScheduledStartTime(ro.getScheduledStartTime());
        dto.setScheduledEndTime(ro.getScheduledEndTime());
        dto.setPickupAddressSnapshot(ro.getPickupAddressSnapshot());
        dto.setDriverId(ro.getDriverId());
        dto.setVehicleId(ro.getVehicleId());
        dto.setNotes(ro.getNotes());
        dto.setActualPickupStartTime(ro.getActualPickupStartTime());
        dto.setActualArrivalTime(ro.getActualArrivalTime());
        dto.setActualPickupTime(ro.getActualPickupTime());
        dto.setActualCheckInTime(ro.getActualCheckInTime());
        dto.setActualInspectionTime(ro.getActualInspectionTime());
        dto.setCompletedAt(ro.getCompletedAt());
        dto.setCreatedAt(ro.getCreatedAt());
        dto.setUpdatedAt(ro.getUpdatedAt());

        if (ro.getDriverId() != null) {
            driverRepository.findByTenantIdAndId(ro.getTenantId(), ro.getDriverId())
                    .ifPresent(d -> dto.setDriverName(d.getName()));
        }
        if (ro.getVehicleId() != null) {
            vehicleRepository.findByTenantIdAndId(ro.getTenantId(), ro.getVehicleId())
                    .ifPresent(v -> dto.setVehicleNumber(v.getVehicleNumber()));
        }
        if (ro.getCustomerId() != null) {
            customerRepository.findByTenantIdAndId(ro.getTenantId(), ro.getCustomerId())
                    .ifPresent(c -> dto.setCustomerName(c.getFirstName() + " " + c.getLastName()));
        }
        if (ro.getEventId() != null) {
            eventRepository.findByTenantIdAndId(ro.getTenantId(), ro.getEventId())
                    .ifPresent(e -> dto.setEventName(e.getEventName()));
        }
        if (ro.getBookingId() != null) {
            bookingRepository.findByTenantIdAndId(ro.getTenantId(), ro.getBookingId())
                    .ifPresent(b -> dto.setBookingNumber(b.getBookingNumber()));
        }

        List<ReturnOrderItem> items = returnOrderItemRepository.findByTenantIdAndReturnOrderId(ro.getTenantId(), ro.getId());
        List<ReturnOrderItemDTO> itemDtos = items.stream().map(i -> {
            ReturnOrderItemDTO idto = new ReturnOrderItemDTO();
            idto.setId(i.getId());
            idto.setTenantId(i.getTenantId());
            idto.setReturnOrderId(i.getReturnOrderId());
            idto.setBookingItemId(i.getBookingItemId());
            idto.setProductId(i.getProductId());
            idto.setProductNameSnapshot(i.getProductNameSnapshot());
            idto.setSkuSnapshot(i.getSkuSnapshot());
            idto.setQuantityExpected(i.getQuantityExpected());
            idto.setQuantityReceived(i.getQuantityReceived());
            idto.setQuantityMissing(i.getQuantityMissing());
            idto.setQuantityDamaged(i.getQuantityDamaged());
            idto.setQuantityGood(i.getQuantityGood());
            idto.setStatus(i.getStatus());
            idto.setNotes(i.getNotes());
            idto.setCreatedAt(i.getCreatedAt());
            idto.setUpdatedAt(i.getUpdatedAt());
            return idto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        dto.setTotalExpectedItems(itemDtos.stream().mapToInt(ReturnOrderItemDTO::getQuantityExpected).sum());
        dto.setTotalReceivedItems(itemDtos.stream().mapToInt(ReturnOrderItemDTO::getQuantityReceived).sum());
        dto.setTotalMissingItems(itemDtos.stream().mapToInt(ReturnOrderItemDTO::getQuantityMissing).sum());
        dto.setTotalDamagedItems(itemDtos.stream().mapToInt(ReturnOrderItemDTO::getQuantityDamaged).sum());

        return dto;
    }
}
