package com.rentflow.ai.service;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.dto.InventoryReservationDTO;
import com.rentflow.ai.model.InventoryReservation;
import com.rentflow.ai.model.Product;

import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public AvailabilityService(ProductRepository productRepository,
                               InventoryReservationRepository reservationRepository,
                               EventRepository eventRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    public AvailabilityResultDTO checkAvailability(String tenantId, UUID productId, int requestedQuantity,
                                                   LocalDateTime startDateTime, LocalDateTime endDateTime) {

        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        // Find overlapping reservations
        List<InventoryReservation> overlapping = reservationRepository.findOverlappingReservations(
                tenantId, productId, startDateTime, endDateTime);

        int quantityReserved = overlapping.stream()
                .mapToInt(InventoryReservation::getQuantity)
                .sum();

        int availableQuantity = product.getQuantityOwned()
                - product.getQuantityInMaintenance()
                - product.getQuantityDamaged()
                - product.getQuantityLost()
                - quantityReserved;

        int finalAvailableQuantity = Math.max(0, availableQuantity);
        boolean available = finalAvailableQuantity >= requestedQuantity;
        int shortage = available ? 0 : (requestedQuantity - finalAvailableQuantity);

        List<InventoryReservationDTO> conflictingDTOs = overlapping.stream()
                .map(this::mapReservationToDTO)
                .collect(Collectors.toList());

        AvailabilityResultDTO result = new AvailabilityResultDTO();
        result.setProductId(product.getId());
        result.setProductName(product.getName());
        result.setSku(product.getSku());
        result.setRequestedQuantity(requestedQuantity);
        result.setQuantityOwned(product.getQuantityOwned());
        result.setQuantityInMaintenance(product.getQuantityInMaintenance());
        result.setQuantityDamaged(product.getQuantityDamaged());
        result.setQuantityLost(product.getQuantityLost());
        result.setQuantityReserved(quantityReserved);
        result.setAvailableQuantity(finalAvailableQuantity);
        result.setAvailable(available);
        result.setShortage(shortage);
        result.setStartDateTime(startDateTime);
        result.setEndDateTime(endDateTime);
        result.setConflictingReservations(conflictingDTOs);

        return result;
    }

    public com.rentflow.ai.dto.BulkAvailabilityResultDTO checkBulkAvailability(String tenantId, com.rentflow.ai.dto.BulkAvailabilityRequestDTO request) {
        if (request == null || request.getItems() == null) {
            com.rentflow.ai.dto.BulkAvailabilityResultDTO empty = new com.rentflow.ai.dto.BulkAvailabilityResultDTO();
            empty.setAvailable(true);
            empty.setItems(List.of());
            return empty;
        }

        LocalDateTime start = request.getStartDateTime() != null ? request.getStartDateTime() : LocalDateTime.now();
        LocalDateTime end = request.getEndDateTime() != null ? request.getEndDateTime() : start.plusDays(1);

        boolean overallAvailable = true;
        List<com.rentflow.ai.dto.BulkAvailabilityResultDTO.ItemResult> itemResults = new java.util.ArrayList<>();

        for (com.rentflow.ai.dto.BulkAvailabilityRequestDTO.ItemRequest item : request.getItems()) {
            AvailabilityResultDTO single = checkAvailability(tenantId, item.getProductId(), item.getQuantity(), start, end);
            com.rentflow.ai.dto.BulkAvailabilityResultDTO.ItemResult res = new com.rentflow.ai.dto.BulkAvailabilityResultDTO.ItemResult();
            res.setProductId(single.getProductId());
            res.setProductName(single.getProductName());
            res.setSku(single.getSku());
            res.setRequestedQuantity(single.getRequestedQuantity());
            res.setAvailableQuantity(single.getAvailableQuantity());
            res.setShortageQuantity(single.getShortage());
            res.setAvailable(single.isAvailable());
            itemResults.add(res);

            if (!single.isAvailable()) {
                overallAvailable = false;
            }
        }

        com.rentflow.ai.dto.BulkAvailabilityResultDTO bulkResult = new com.rentflow.ai.dto.BulkAvailabilityResultDTO();
        bulkResult.setAvailable(overallAvailable);
        bulkResult.setItems(itemResults);
        return bulkResult;
    }

    public List<AvailabilityResultDTO> getProductAvailabilityTimeline(String tenantId, UUID productId, int days) {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<AvailabilityResultDTO> timeline = new java.util.ArrayList<>();

        for (int i = 0; i < Math.max(1, days); i++) {
            LocalDateTime dayStart = now.plusDays(i).withHour(8);
            LocalDateTime dayEnd = now.plusDays(i).withHour(22);
            AvailabilityResultDTO dayResult = checkAvailability(tenantId, productId, 1, dayStart, dayEnd);
            timeline.add(dayResult);
        }
        return timeline;
    }

    public InventoryReservationDTO mapReservationToDTO(InventoryReservation r) {
        InventoryReservationDTO dto = new InventoryReservationDTO();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setProductId(r.getProductId());
        dto.setEventId(r.getEventId());
        dto.setBookingId(r.getBookingId());
        dto.setQuantity(r.getQuantity());
        dto.setStartDateTime(r.getStartDateTime());
        dto.setEndDateTime(r.getEndDateTime());
        dto.setStatus(r.getStatus());
        dto.setInventoryItemId(r.getInventoryItemId());
        dto.setReservationType(r.getReservationType());
        dto.setCreatedBy(r.getCreatedBy());
        dto.setExpiresAt(r.getExpiresAt());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        if (r.getProductId() != null) {
            productRepository.findById(r.getProductId())
                    .ifPresent(p -> dto.setProductName(p.getName()));
        }

        if (r.getEventId() != null && eventRepository != null) {
            eventRepository.findById(r.getEventId())
                    .ifPresent(e -> dto.setEventName(e.getEventName()));
        }

        return dto;
    }
}
