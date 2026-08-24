package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryReservationRepository reservationRepository;
    private final AvailabilityService availabilityService;

    public InventoryService(ProductRepository productRepository,
                            InventoryTransactionRepository transactionRepository,
                            InventoryReservationRepository reservationRepository,
                            AvailabilityService availabilityService) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.reservationRepository = reservationRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional(readOnly = true)
    public InventorySummaryDTO getSummary(String tenantId) {
        List<Product> products = productRepository.findByTenantId(tenantId);
        List<InventoryReservation> activeReservations = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.PENDING)
                .collect(Collectors.toList());

        int totalProducts = products.size();
        int totalUnits = products.stream().mapToInt(Product::getQuantityOwned).sum();
        int maintenanceUnits = products.stream().mapToInt(Product::getQuantityInMaintenance).sum();
        int damagedUnits = products.stream().mapToInt(Product::getQuantityDamaged).sum();
        int lostUnits = products.stream().mapToInt(Product::getQuantityLost).sum();
        int reservedUnits = activeReservations.stream().mapToInt(InventoryReservation::getQuantity).sum();

        int availableUnits = Math.max(0, totalUnits - maintenanceUnits - damagedUnits - lostUnits - reservedUnits);

        int lowStockProducts = 0;
        for (Product p : products) {
            int avail = p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost();
            if (p.getQuantityOwned() > 0 && ((double) avail / p.getQuantityOwned() < 0.25)) {
                lowStockProducts++;
            }
        }

        InventorySummaryDTO summary = new InventorySummaryDTO();
        summary.setTotalProducts(totalProducts);
        summary.setTotalUnits(totalUnits);
        summary.setAvailableUnits(availableUnits);
        summary.setReservedUnits(reservedUnits);
        summary.setMaintenanceUnits(maintenanceUnits);
        summary.setDamagedUnits(damagedUnits);
        summary.setLostUnits(lostUnits);
        summary.setLowStockProducts(lowStockProducts);
        return summary;
    }

    public ProductDTO adjustInventory(String tenantId, UUID productId, InventoryAdjustmentRequest req, String createdBy) {
        Product p = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        TransactionType type = req.getType() != null ? req.getType() : TransactionType.ADJUSTMENT;
        int qty = req.getQuantity();

        switch (type) {
            case PURCHASE -> p.setQuantityOwned(p.getQuantityOwned() + qty);
            case ADJUSTMENT -> p.setQuantityOwned(Math.max(0, p.getQuantityOwned() + qty));
            case DAMAGE -> {
                p.setQuantityDamaged(p.getQuantityDamaged() + qty);
            }
            case LOSS -> {
                p.setQuantityLost(p.getQuantityLost() + qty);
            }
            case MAINTENANCE -> {
                p.setQuantityInMaintenance(p.getQuantityInMaintenance() + qty);
            }
            case RESTORED -> {
                p.setQuantityInMaintenance(Math.max(0, p.getQuantityInMaintenance() - qty));
                p.setQuantityDamaged(Math.max(0, p.getQuantityDamaged() - qty));
            }
            default -> {}
        }

        Product saved = productRepository.save(p);

        // Record Audit Transaction
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTenantId(tenantId);
        tx.setProductId(productId);
        tx.setTransactionType(type);
        tx.setQuantity(qty);
        tx.setNotes(req.getReason());
        tx.setCreatedBy(createdBy != null ? createdBy : "System");
        transactionRepository.save(tx);

        return mapProductToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionDTO> getTransactions(String tenantId, UUID productId) {
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return transactionRepository.findByTenantIdAndProductIdOrderByCreatedAtDesc(tenantId, productId).stream()
                .map(tx -> {
                    InventoryTransactionDTO dto = new InventoryTransactionDTO();
                    dto.setId(tx.getId());
                    dto.setTenantId(tx.getTenantId());
                    dto.setProductId(tx.getProductId());
                    dto.setProductName(product.getName());
                    dto.setTransactionType(tx.getTransactionType());
                    dto.setQuantity(tx.getQuantity());
                    dto.setReferenceType(tx.getReferenceType());
                    dto.setReferenceId(tx.getReferenceId());
                    dto.setNotes(tx.getNotes());
                    dto.setCreatedBy(tx.getCreatedBy());
                    dto.setCreatedAt(tx.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public InventoryReservationDTO createReservation(String tenantId, InventoryReservationDTO dto, String createdBy) {
        Product product = productRepository.findByTenantIdAndId(tenantId, dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        LocalDateTime start = dto.getStartDateTime() != null ? dto.getStartDateTime() : LocalDateTime.now();
        LocalDateTime end = dto.getEndDateTime() != null ? dto.getEndDateTime() : start.plusDays(2);

        // Atomic Availability Check
        AvailabilityResultDTO avail = availabilityService.checkAvailability(tenantId, dto.getProductId(), dto.getQuantity(), start, end);
        if (avail.getAvailableQuantity() < dto.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only " + avail.getAvailableQuantity() + " units are available for the selected rental period.");
        }

        InventoryReservation r = new InventoryReservation();
        r.setTenantId(tenantId);
        r.setProductId(dto.getProductId());
        r.setEventId(dto.getEventId());
        r.setBookingId(dto.getBookingId());
        r.setQuantity(dto.getQuantity());
        r.setStartDateTime(start);
        r.setEndDateTime(end);
        r.setStatus(dto.getStatus() != null ? dto.getStatus() : ReservationStatus.RESERVED);
        r.setReservationType(dto.getReservationType() != null ? dto.getReservationType() : ReservationType.BOOKING);
        r.setCreatedBy(createdBy != null ? createdBy : "System");
        r.setExpiresAt(dto.getExpiresAt());

        InventoryReservation saved = reservationRepository.save(r);

        // Record Transaction
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTenantId(tenantId);
        tx.setProductId(dto.getProductId());
        tx.setTransactionType(TransactionType.RESERVATION);
        tx.setQuantity(dto.getQuantity());
        tx.setReferenceType("EVENT");
        tx.setReferenceId(dto.getEventId());
        tx.setNotes("Reservation created for product " + product.getName());
        tx.setCreatedBy(createdBy != null ? createdBy : "System");
        transactionRepository.save(tx);

        return availabilityService.mapReservationToDTO(saved);
    }

    public List<InventoryReservationDTO> createBatchReservations(String tenantId, BatchReservationRequestDTO request, String createdBy) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return List.of();
        }

        LocalDateTime start = request.getStartDateTime() != null ? request.getStartDateTime() : LocalDateTime.now();
        LocalDateTime end = request.getEndDateTime() != null ? request.getEndDateTime() : start.plusDays(2);

        // Step 1: Pre-validate all items
        for (BatchReservationRequestDTO.ItemRequest item : request.getItems()) {
            AvailabilityResultDTO avail = availabilityService.checkAvailability(tenantId, item.getProductId(), item.getQuantity(), start, end);
            if (avail.getAvailableQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only " + avail.getAvailableQuantity() + " units of " + avail.getProductName() + " are available for the selected rental period.");
            }
        }

        // Step 2: Create reservations atomically
        List<InventoryReservationDTO> createdList = new java.util.ArrayList<>();
        for (BatchReservationRequestDTO.ItemRequest item : request.getItems()) {
            InventoryReservationDTO dto = new InventoryReservationDTO();
            dto.setProductId(item.getProductId());
            dto.setBookingId(request.getBookingId());
            dto.setEventId(request.getEventId());
            dto.setQuantity(item.getQuantity());
            dto.setStartDateTime(start);
            dto.setEndDateTime(end);
            dto.setStatus(ReservationStatus.RESERVED);
            dto.setReservationType(request.getReservationType() != null ? request.getReservationType() : ReservationType.BOOKING);
            createdList.add(createReservation(tenantId, dto, createdBy));
        }

        return createdList;
    }

    @Transactional(readOnly = true)
    public List<InventoryReservationDTO> getReservations(String tenantId, String status, String search) {
        List<InventoryReservation> list = reservationRepository.findByTenantId(tenantId);

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                ReservationStatus reqStatus = ReservationStatus.valueOf(status.toUpperCase());
                list = list.stream().filter(r -> r.getStatus() == reqStatus).collect(Collectors.toList());
            } catch (Exception ignored) {}
        }

        return list.stream()
                .map(availabilityService::mapReservationToDTO)
                .filter(dto -> {
                    if (search == null || search.isBlank()) return true;
                    String s = search.toLowerCase();
                    return (dto.getProductName() != null && dto.getProductName().toLowerCase().contains(s)) ||
                           (dto.getEventName() != null && dto.getEventName().toLowerCase().contains(s)) ||
                           (dto.getId() != null && dto.getId().toString().toLowerCase().contains(s));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<InventoryReservationDTO> getReservationById(String tenantId, UUID id) {
        return reservationRepository.findByTenantIdAndId(tenantId, id)
                .map(availabilityService::mapReservationToDTO);
    }

    @Transactional(readOnly = true)
    public List<InventoryConflictDTO> getConflicts(String tenantId) {
        List<Product> products = productRepository.findByTenantId(tenantId);
        List<InventoryConflictDTO> conflicts = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime horizon = now.plusDays(30);

        for (Product p : products) {
            AvailabilityResultDTO avail = availabilityService.checkAvailability(tenantId, p.getId(), 1, now, horizon);
            if (avail.getQuantityReserved() > p.getQuantityOwned() || avail.getShortage() > 0) {
                InventoryConflictDTO conflict = new InventoryConflictDTO();
                conflict.setProductId(p.getId());
                conflict.setProductName(p.getName());
                conflict.setSku(p.getSku());
                conflict.setRequestedQuantity(avail.getRequestedQuantity());
                conflict.setAvailableQuantity(avail.getAvailableQuantity());
                conflict.setShortageQuantity(Math.max(1, avail.getShortage()));
                conflict.setEventDate(now);
                conflict.setPriority("HIGH");
                conflicts.add(conflict);
            }
        }
        return conflicts;
    }

    public Optional<InventoryReservationDTO> releaseReservation(String tenantId, UUID reservationId, String createdBy) {
        return reservationRepository.findByTenantIdAndId(tenantId, reservationId)
                .map(r -> {
                    r.setStatus(ReservationStatus.RELEASED);
                    InventoryReservation saved = reservationRepository.save(r);

                    InventoryTransaction tx = new InventoryTransaction();
                    tx.setTenantId(tenantId);
                    tx.setProductId(r.getProductId());
                    tx.setTransactionType(TransactionType.RELEASE);
                    tx.setQuantity(r.getQuantity());
                    tx.setReferenceType("RESERVATION");
                    tx.setReferenceId(reservationId);
                    tx.setNotes("Reservation released");
                    tx.setCreatedBy(createdBy != null ? createdBy : "System");
                    transactionRepository.save(tx);

                    return availabilityService.mapReservationToDTO(saved);
                });
    }

    public Optional<InventoryReservationDTO> cancelReservation(String tenantId, UUID reservationId, String createdBy) {
        return reservationRepository.findByTenantIdAndId(tenantId, reservationId)
                .map(r -> {
                    r.setStatus(ReservationStatus.CANCELLED);
                    InventoryReservation saved = reservationRepository.save(r);

                    InventoryTransaction tx = new InventoryTransaction();
                    tx.setTenantId(tenantId);
                    tx.setProductId(r.getProductId());
                    tx.setTransactionType(TransactionType.RELEASE);
                    tx.setQuantity(r.getQuantity());
                    tx.setReferenceType("RESERVATION");
                    tx.setReferenceId(reservationId);
                    tx.setNotes("Reservation cancelled");
                    tx.setCreatedBy(createdBy != null ? createdBy : "System");
                    transactionRepository.save(tx);

                    return availabilityService.mapReservationToDTO(saved);
                });
    }

    private ProductDTO mapProductToDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setCategoryId(p.getCategoryId());
        dto.setProductType(p.getProductType());
        dto.setStatus(p.getStatus());
        dto.setRentalPrice(p.getRentalPrice());
        dto.setReplacementCost(p.getReplacementCost());
        dto.setQuantityOwned(p.getQuantityOwned());
        dto.setQuantityInMaintenance(p.getQuantityInMaintenance());
        dto.setQuantityDamaged(p.getQuantityDamaged());
        dto.setQuantityLost(p.getQuantityLost());
        int avail = p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost();
        dto.setAvailableQuantity(Math.max(0, avail));
        return dto;
    }
}
