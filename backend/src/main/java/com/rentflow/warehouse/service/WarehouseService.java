package com.rentflow.warehouse.service;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.notification.dto.NotificationRequestDTO;
import com.rentflow.notification.model.NotificationPriority;
import com.rentflow.notification.model.NotificationType;
import com.rentflow.notification.service.NotificationService;
import com.rentflow.warehouse.dto.*;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseOrderRepository orderRepository;
    private final WarehouseOrderItemRepository itemRepository;
    private final WarehouseLocationRepository locationRepository;
    private final WarehouseAuditRepository auditRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final InventoryReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public WarehouseService(WarehouseOrderRepository orderRepository,
                            WarehouseOrderItemRepository itemRepository,
                            WarehouseLocationRepository locationRepository,
                            WarehouseAuditRepository auditRepository,
                            BookingRepository bookingRepository,
                            BookingItemRepository bookingItemRepository,
                            ProductRepository productRepository,
                            CustomerRepository customerRepository,
                            EventRepository eventRepository,
                            InventoryReservationRepository reservationRepository,
                            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.auditRepository = auditRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    public synchronized String generateOrderNumber(String tenantId) {
        long count = orderRepository.countByTenantId(tenantId) + 1;
        return String.format("WH-%06d", count);
    }

    @Transactional
    public WarehouseOrderDTO createOrderFromBooking(String tenantId, UUID bookingId, String userRole) {
        // 1. Validate booking & tenant
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot create warehouse order for a cancelled booking.");
        }

        // 2. Prevent duplicate work orders
        Optional<WarehouseOrder> existing = orderRepository.findByTenantIdAndBookingId(tenantId, bookingId);
        if (existing.isPresent() && existing.get().getStatus() != WarehouseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Warehouse order already exists for this booking.");
        }

        // 3. Verify inventory reservation exists or is ready
        List<InventoryReservation> reservations = reservationRepository.findByTenantIdAndBookingId(tenantId, bookingId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.PENDING)
                .collect(Collectors.toList());

        if (reservations.isEmpty() && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Inventory reservation is required before warehouse picking.");
        }

        // 4. Load booking items
        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(bookingId);
        if (bookingItems.isEmpty()) {
            throw new IllegalStateException("Cannot create warehouse order for booking with no items.");
        }

        // 5. Create Warehouse Order
        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(tenantId);
        order.setBookingId(bookingId);
        order.setEventId(booking.getEventId());
        order.setCustomerId(booking.getCustomerId());
        order.setOrderNumber(generateOrderNumber(tenantId));
        order.setScheduledDate(booking.getRentalStartDateTime());

        // Determine priority based on event date
        if (booking.getRentalStartDateTime() != null) {
            LocalDate eventDate = booking.getRentalStartDateTime().toLocalDate();
            LocalDate today = LocalDate.now();
            if (eventDate.isEqual(today)) {
                order.setPriority(WarehouseOrderPriority.URGENT);
            } else if (eventDate.isEqual(today.plusDays(1))) {
                order.setPriority(WarehouseOrderPriority.HIGH);
            } else {
                order.setPriority(WarehouseOrderPriority.NORMAL);
            }
        } else {
            order.setPriority(WarehouseOrderPriority.NORMAL);
        }

        order.setStatus(WarehouseOrderStatus.READY_TO_PICK);
        order.setNotes(booking.getNotes());
        order.setCreatedBy(userRole != null ? userRole : "System");

        WarehouseOrder savedOrder = orderRepository.save(order);

        // 6. Create Warehouse Order Items (Snapshotting)
        List<WarehouseLocation> locations = locationRepository.findByTenantIdAndActiveTrue(tenantId);
        int locIdx = 0;

        for (BookingItem bItem : bookingItems) {
            WarehouseOrderItem wItem = new WarehouseOrderItem();
            wItem.setWarehouseOrderId(savedOrder.getId());
            wItem.setBookingItemId(bItem.getId());
            wItem.setProductId(bItem.getProductId());
            wItem.setQuantityRequired(bItem.getQuantity());
            wItem.setQuantityPicked(0);
            wItem.setQuantityPacked(0);
            wItem.setStatus(WarehouseOrderItemStatus.PENDING);

            // Snapshot Product details
            if (bItem.getProductId() != null) {
                productRepository.findById(bItem.getProductId()).ifPresent(p -> {
                    wItem.setProductNameSnapshot(p.getName());
                    wItem.setSkuSnapshot(p.getSku());
                });
            }
            if (wItem.getProductNameSnapshot() == null) {
                wItem.setProductNameSnapshot(bItem.getDescription());
            }

            // Snapshot Location details
            if (!locations.isEmpty()) {
                WarehouseLocation loc = locations.get(locIdx % locations.size());
                wItem.setLocationSnapshot(loc.getCode());
                locIdx++;
            } else {
                wItem.setLocationSnapshot("A-01-01");
            }

            itemRepository.save(wItem);
        }

        // 7. Audit Event
        recordAudit(tenantId, savedOrder.getId(), bookingId, "WAREHOUSE_ORDER_CREATED",
                userRole, "Warehouse order " + savedOrder.getOrderNumber() + " created for booking " + booking.getBookingNumber());

        // 8. Notification
        sendNotification(tenantId, NotificationType.WAREHOUSE_ORDER_CREATED,
                "New Warehouse Order " + savedOrder.getOrderNumber(),
                "Warehouse order " + savedOrder.getOrderNumber() + " has been created and is ready to pick.",
                NotificationPriority.NORMAL);

        return mapToDTO(savedOrder, userRole);
    }

    @Transactional
    public WarehouseOrderDTO startPicking(String tenantId, UUID orderId, String userId, String userRole) {
        enforceWarehouseAccess(userRole, false);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        if (order.getStatus() == WarehouseOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pick a cancelled warehouse order.");
        }

        order.setStatus(WarehouseOrderStatus.PICKING);
        if (userId != null && !userId.isBlank()) {
            order.setAssignedTo(userId);
        }
        WarehouseOrder saved = orderRepository.save(order);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_PICKING_STARTED",
                userRole, "Started picking for order " + order.getOrderNumber());

        return mapToDTO(saved, userRole);
    }

    @Transactional
    public WarehouseOrderDTO pickItem(String tenantId, UUID orderId, UUID itemId, int quantity, String notes, boolean isShortage, String userRole) {
        enforceWarehouseAccess(userRole, false);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        WarehouseOrderItem item = itemRepository.findByWarehouseOrderIdAndId(orderId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order item not found with ID: " + itemId));

        if (quantity < 0) {
            throw new IllegalArgumentException("Picked quantity must be non-negative.");
        }

        if (item.getQuantityPicked() + quantity > item.getQuantityRequired()) {
            throw new IllegalArgumentException("Picked quantity cannot exceed required quantity (" + item.getQuantityRequired() + ").");
        }

        item.setQuantityPicked(item.getQuantityPicked() + quantity);
        if (notes != null && !notes.isBlank()) {
            item.setNotes(notes);
        }

        if (item.getQuantityPicked() == item.getQuantityRequired()) {
            item.setStatus(WarehouseOrderItemStatus.PICKED);
        } else if (item.getQuantityPicked() > 0) {
            item.setStatus(WarehouseOrderItemStatus.PARTIALLY_PICKED);
        }

        if (isShortage || (notes != null && notes.toLowerCase().contains("short"))) {
            item.setStatus(WarehouseOrderItemStatus.SHORT);
            recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_ITEM_SHORT",
                    userRole, "Item shortage reported for " + item.getProductNameSnapshot() + ": " + notes);

            sendNotification(tenantId, NotificationType.WAREHOUSE_ITEM_SHORT,
                    "Item Shortage Alert: " + order.getOrderNumber(),
                    "Shortage reported for " + item.getProductNameSnapshot() + " on order " + order.getOrderNumber() + ". Note: " + notes,
                    NotificationPriority.HIGH);
        } else {
            recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_ITEM_PICKED",
                    userRole, "Picked " + quantity + " units of " + item.getProductNameSnapshot());
        }

        itemRepository.save(item);

        if (order.getStatus() == WarehouseOrderStatus.READY_TO_PICK) {
            order.setStatus(WarehouseOrderStatus.PICKING);
            orderRepository.save(order);
        }

        return mapToDTO(order, userRole);
    }

    @Transactional
    public WarehouseOrderDTO completePicking(String tenantId, UUID orderId, boolean confirmShortage, String userRole) {
        enforceWarehouseAccess(userRole, true); // Requires Manager/Admin authorization if shortages exist

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        List<WarehouseOrderItem> items = itemRepository.findByWarehouseOrderId(orderId);
        boolean hasShortage = items.stream().anyMatch(i -> i.getQuantityPicked() < i.getQuantityRequired() || i.getStatus() == WarehouseOrderItemStatus.SHORT);

        if (hasShortage && !confirmShortage) {
            throw new IllegalStateException("Items are short. Manager confirmation is required to complete picking.");
        }

        order.setStatus(WarehouseOrderStatus.PICKED);
        WarehouseOrder saved = orderRepository.save(order);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_PICKING_COMPLETED",
                userRole, "Completed picking phase for order " + order.getOrderNumber() + (hasShortage ? " (Shortages confirmed)" : ""));

        return mapToDTO(saved, userRole);
    }

    @Transactional
    public WarehouseOrderDTO startPacking(String tenantId, UUID orderId, String userRole) {
        enforceWarehouseAccess(userRole, false);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        if (order.getStatus() != WarehouseOrderStatus.PICKED && order.getStatus() != WarehouseOrderStatus.PACKING) {
            throw new IllegalStateException("Order must be in PICKED status before packing can start.");
        }

        order.setStatus(WarehouseOrderStatus.PACKING);
        WarehouseOrder saved = orderRepository.save(order);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_PACKING_STARTED",
                userRole, "Started packing phase for order " + order.getOrderNumber());

        return mapToDTO(saved, userRole);
    }

    @Transactional
    public WarehouseOrderDTO packItem(String tenantId, UUID orderId, UUID itemId, int quantity, String notes, String userRole) {
        enforceWarehouseAccess(userRole, false);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        WarehouseOrderItem item = itemRepository.findByWarehouseOrderIdAndId(orderId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order item not found with ID: " + itemId));

        if (quantity < 0) {
            throw new IllegalArgumentException("Packed quantity must be non-negative.");
        }

        if (item.getQuantityPacked() + quantity > item.getQuantityPicked()) {
            throw new IllegalStateException("Packed quantity cannot exceed picked quantity (" + item.getQuantityPicked() + ").");
        }

        item.setQuantityPacked(item.getQuantityPacked() + quantity);
        if (notes != null && !notes.isBlank()) {
            item.setNotes(notes);
        }

        if (item.getQuantityPacked() == item.getQuantityRequired()) {
            item.setStatus(WarehouseOrderItemStatus.PACKED);
        }

        itemRepository.save(item);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_ITEM_PACKED",
                userRole, "Packed " + quantity + " units of " + item.getProductNameSnapshot());

        return mapToDTO(order, userRole);
    }

    @Transactional
    public WarehouseOrderDTO completePacking(String tenantId, UUID orderId, boolean confirmShortage, String userRole) {
        enforceWarehouseAccess(userRole, true);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        List<WarehouseOrderItem> items = itemRepository.findByWarehouseOrderId(orderId);
        boolean hasUnpackedShortage = items.stream().anyMatch(i -> i.getQuantityPacked() < i.getQuantityRequired());

        if (hasUnpackedShortage && !confirmShortage) {
            throw new IllegalStateException("Not all required items are packed. Confirmation required to proceed.");
        }

        order.setStatus(WarehouseOrderStatus.READY_FOR_DELIVERY);
        order.setCompletedAt(LocalDateTime.now());
        WarehouseOrder saved = orderRepository.save(order);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_PACKING_COMPLETED",
                userRole, "Completed packing phase for order " + order.getOrderNumber());

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_READY_FOR_DELIVERY",
                userRole, "Order " + order.getOrderNumber() + " is ready for delivery.");

        sendNotification(tenantId, NotificationType.WAREHOUSE_ORDER_READY,
                "Order Ready for Delivery: " + order.getOrderNumber(),
                "Warehouse order " + order.getOrderNumber() + " is packed and ready for delivery.",
                NotificationPriority.HIGH);

        return mapToDTO(saved, userRole);
    }

    @Transactional
    public WarehouseOrderDTO assignOrder(String tenantId, UUID orderId, String userId, String userRole) {
        enforceWarehouseAccess(userRole, true);

        WarehouseOrder order = orderRepository.findByTenantIdAndId(tenantId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse order not found with ID: " + orderId));

        order.setAssignedTo(userId);
        WarehouseOrder saved = orderRepository.save(order);

        recordAudit(tenantId, orderId, order.getBookingId(), "WAREHOUSE_ORDER_ASSIGNED",
                userRole, "Assigned order " + order.getOrderNumber() + " to user " + userId);

        return mapToDTO(saved, userRole);
    }

    public List<WarehouseOrderDTO> getOrders(String tenantId, String status, String priority, String search, String userRole) {
        enforceReadAccess(userRole);

        List<WarehouseOrder> list = orderRepository.findByTenantId(tenantId);

        return list.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equalsIgnoreCase(status))
                .filter(o -> priority == null || priority.isBlank() || o.getPriority().name().equalsIgnoreCase(priority))
                .filter(o -> {
                    if (search == null || search.isBlank()) return true;
                    String s = search.toLowerCase();
                    return (o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(s)) ||
                           (o.getNotes() != null && o.getNotes().toLowerCase().contains(s));
                })
                .map(o -> mapToDTO(o, userRole))
                .collect(Collectors.toList());
    }

    public Optional<WarehouseOrderDTO> getOrderById(String tenantId, UUID id, String userRole) {
        enforceReadAccess(userRole);
        return orderRepository.findByTenantIdAndId(tenantId, id)
                .map(o -> mapToDTO(o, userRole));
    }

    public WarehouseDashboardDTO getDashboard(String tenantId, String userRole) {
        enforceReadAccess(userRole);

        List<WarehouseOrder> all = orderRepository.findByTenantId(tenantId);

        WarehouseDashboardDTO dto = new WarehouseDashboardDTO();
        dto.setTodaysOrders(all.stream().filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().isEqual(LocalDate.now())).count());
        dto.setReadyToPick(all.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.READY_TO_PICK).count());
        dto.setPicking(all.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.PICKING).count());
        dto.setPacking(all.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.PACKING).count());
        dto.setReadyForDelivery(all.stream().filter(o -> o.getStatus() == WarehouseOrderStatus.READY_FOR_DELIVERY).count());

        long shortCount = 0;
        for (WarehouseOrder o : all) {
            List<WarehouseOrderItem> items = itemRepository.findByWarehouseOrderId(o.getId());
            shortCount += items.stream().filter(i -> i.getStatus() == WarehouseOrderItemStatus.SHORT).count();
        }
        dto.setShortItems(shortCount);

        List<WarehouseOrderDTO> priorities = all.stream()
                .filter(o -> o.getPriority() == WarehouseOrderPriority.HIGH || o.getPriority() == WarehouseOrderPriority.URGENT)
                .map(o -> mapToDTO(o, userRole))
                .collect(Collectors.toList());
        dto.setTodaysPriorities(priorities);

        return dto;
    }

    public List<WarehouseLocationDTO> getLocations(String tenantId) {
        return locationRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                .map(this::mapLocationToDTO)
                .collect(Collectors.toList());
    }

    public WarehouseOrderDTO mapToDTO(WarehouseOrder order, String userRole) {
        WarehouseOrderDTO dto = new WarehouseOrderDTO();
        dto.setId(order.getId());
        dto.setTenantId(order.getTenantId());
        dto.setBookingId(order.getBookingId());
        dto.setEventId(order.getEventId());
        dto.setCustomerId(order.getCustomerId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setScheduledDate(order.getScheduledDate());
        dto.setPriority(order.getPriority());
        dto.setStatus(order.getStatus());
        dto.setCustomerFacingStatus(mapToCustomerFacingStatus(order.getStatus()));
        dto.setAssignedTo(order.getAssignedTo());
        dto.setNotes(order.getNotes());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCompletedAt(order.getCompletedAt());

        // Map Booking Info
        if (order.getBookingId() != null) {
            bookingRepository.findById(order.getBookingId()).ifPresent(b -> dto.setBookingNumber(b.getBookingNumber()));
        }

        // Map Event Info
        if (order.getEventId() != null) {
            eventRepository.findById(order.getEventId()).ifPresent(e -> {
                dto.setEventName(e.getEventName());
                if (e.getEventDate() != null) {
                    dto.setEventDate(e.getEventDate().atStartOfDay());
                }
                dto.setVenueName(e.getVenueName());
            });
        }

        // Map Customer Info
        if (order.getCustomerId() != null) {
            customerRepository.findById(order.getCustomerId()).ifPresent(c -> {
                String name = (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
                dto.setCustomerName(!name.isBlank() ? name : c.getCompanyName());
            });
        }

        // Map Items
        List<WarehouseOrderItem> items = itemRepository.findByWarehouseOrderId(order.getId());
        List<WarehouseOrderItemDTO> itemDTOs = new ArrayList<>();
        int reqSum = 0;
        int pickSum = 0;
        int packSum = 0;

        for (WarehouseOrderItem item : items) {
            WarehouseOrderItemDTO idto = new WarehouseOrderItemDTO();
            idto.setId(item.getId());
            idto.setWarehouseOrderId(item.getWarehouseOrderId());
            idto.setBookingItemId(item.getBookingItemId());
            idto.setProductId(item.getProductId());
            idto.setProductNameSnapshot(item.getProductNameSnapshot());
            idto.setSkuSnapshot(item.getSkuSnapshot());
            idto.setLocationSnapshot(item.getLocationSnapshot());
            idto.setQuantityRequired(item.getQuantityRequired());
            idto.setQuantityPicked(item.getQuantityPicked());
            idto.setQuantityPacked(item.getQuantityPacked());
            idto.setStatus(item.getStatus());
            idto.setNotes(item.getNotes());
            idto.setCreatedAt(item.getCreatedAt());
            idto.setUpdatedAt(item.getUpdatedAt());

            reqSum += item.getQuantityRequired();
            pickSum += item.getQuantityPicked();
            packSum += item.getQuantityPacked();

            itemDTOs.add(idto);
        }

        dto.setItems(itemDTOs);
        dto.setTotalQuantityRequired(reqSum);
        dto.setTotalQuantityPicked(pickSum);
        dto.setTotalQuantityPacked(packSum);
        dto.setPickingProgressPercentage(reqSum > 0 ? ((double) pickSum / reqSum) * 100.0 : 0.0);

        return dto;
    }

    private String mapToCustomerFacingStatus(WarehouseOrderStatus status) {
        if (status == null) return "PREPARING";
        return switch (status) {
            case PACKED, READY_FOR_DELIVERY -> "READY_FOR_DELIVERY";
            case CANCELLED -> "CANCELLED";
            default -> "PREPARING";
        };
    }

    private WarehouseLocationDTO mapLocationToDTO(WarehouseLocation loc) {
        WarehouseLocationDTO dto = new WarehouseLocationDTO();
        dto.setId(loc.getId());
        dto.setTenantId(loc.getTenantId());
        dto.setCode(loc.getCode());
        dto.setName(loc.getName());
        dto.setDescription(loc.getDescription());
        dto.setActive(loc.isActive());
        dto.setCreatedAt(loc.getCreatedAt());
        dto.setUpdatedAt(loc.getUpdatedAt());
        return dto;
    }

    private void enforceReadAccess(String role) {
        if (role == null) return;
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            throw new SecurityException("Customer has no internal warehouse access.");
        }
    }

    private void enforceWarehouseAccess(String role, boolean requireManager) {
        if (role == null) return;
        String r = role.toUpperCase();
        if ("CUSTOMER".equals(r) || "FINANCE".equals(r) || "SALES".equals(r)) {
            throw new SecurityException("Role " + role + " is not authorized for warehouse operations.");
        }
        if (requireManager && "WAREHOUSE_OPERATOR".equals(r)) {
            throw new SecurityException("Operation requires WAREHOUSE_MANAGER, ADMIN or OWNER authorization.");
        }
    }

    private void recordAudit(String tenantId, UUID orderId, UUID bookingId, String action, String performedBy, String details) {
        WarehouseAudit audit = new WarehouseAudit(tenantId, orderId, bookingId, action,
                performedBy != null ? performedBy : "System", details);
        auditRepository.save(audit);
    }

    private void sendNotification(String tenantId, NotificationType type, String title, String body, NotificationPriority priority) {
        try {
            NotificationRequestDTO req = new NotificationRequestDTO();
            req.setTenantId(tenantId);
            req.setType(type);
            req.setPriority(priority);
            req.setCustomTitle(title);
            req.setCustomMessage(body);

            notificationService.sendNotification(req);
        } catch (Exception e) {
            System.err.println("⚠️ Warehouse Notification warning: " + e.getMessage());
        }
    }
}
