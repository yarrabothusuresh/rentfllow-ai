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
