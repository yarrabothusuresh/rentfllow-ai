package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.InventoryReservation;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return checkAvailabilityExcludingBooking(tenantId, productId, requestedQuantity, startDateTime, endDateTime, null);
    }

    public AvailabilityResultDTO checkAvailabilityExcludingBooking(String tenantId, UUID productId, int requestedQuantity,
                                                                   LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                   UUID excludeBookingId) {
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        int turnaroundMinutes = product.getDefaultTurnaroundMinutes() > 0 ? product.getDefaultTurnaroundMinutes() : 0;
        // Expand search interval by turnaround minutes
        LocalDateTime queryStart = startDateTime.minusMinutes(turnaroundMinutes);
        LocalDateTime queryEnd = endDateTime.plusMinutes(turnaroundMinutes);

        List<InventoryReservation> overlapping;
        if (excludeBookingId != null) {
            overlapping = reservationRepository.findOverlappingReservationsExcludingBooking(
                    tenantId, productId, queryStart, queryEnd, excludeBookingId);
        } else {
            overlapping = reservationRepository.findOverlappingReservations(
                    tenantId, productId, queryStart, queryEnd);
        }

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

    public BulkAvailabilityResultDTO checkBulkAvailability(String tenantId, BulkAvailabilityRequestDTO request) {
        if (request == null || request.getItems() == null) {
            BulkAvailabilityResultDTO empty = new BulkAvailabilityResultDTO();
            empty.setAvailable(true);
            empty.setItems(List.of());
            return empty;
        }

        LocalDateTime start = request.getStartDateTime() != null ? request.getStartDateTime() : LocalDateTime.now();
        LocalDateTime end = request.getEndDateTime() != null ? request.getEndDateTime() : start.plusDays(1);

        boolean overallAvailable = true;
        List<BulkAvailabilityResultDTO.ItemResult> itemResults = new ArrayList<>();

        for (BulkAvailabilityRequestDTO.ItemRequest item : request.getItems()) {
            AvailabilityResultDTO single = checkAvailability(tenantId, item.getProductId(), item.getQuantity(), start, end);
            BulkAvailabilityResultDTO.ItemResult res = new BulkAvailabilityResultDTO.ItemResult();
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

        BulkAvailabilityResultDTO bulkResult = new BulkAvailabilityResultDTO();
        bulkResult.setAvailable(overallAvailable);
        bulkResult.setItems(itemResults);
        return bulkResult;
    }

    public List<AvailabilityResultDTO> getProductAvailabilityTimeline(String tenantId, UUID productId, int days) {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<AvailabilityResultDTO> timeline = new ArrayList<>();

        for (int i = 0; i < Math.max(1, days); i++) {
            LocalDateTime dayStart = now.plusDays(i).withHour(8);
            LocalDateTime dayEnd = now.plusDays(i).withHour(22);
            AvailabilityResultDTO dayResult = checkAvailability(tenantId, productId, 1, dayStart, dayEnd);
            timeline.add(dayResult);
        }
        return timeline;
    }

    public AvailabilityMatrixDTO getAvailabilityMatrix(String tenantId) {
        return getAvailabilityMatrix(tenantId, null, null);
    }

    public AvailabilityMatrixDTO getAvailabilityMatrix(String tenantId, LocalDateTime start, LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (end == null) end = start.plusDays(7);

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start.toLocalDate();
        LocalDate lastDate = end.toLocalDate();
        while (!current.isAfter(lastDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        List<Product> products = productRepository.findByTenantId(tenantId);
        List<AvailabilityMatrixDTO.ProductMatrixRow> rows = new ArrayList<>();

        for (Product product : products) {
            AvailabilityMatrixDTO.ProductMatrixRow row = new AvailabilityMatrixDTO.ProductMatrixRow();
            row.setProductId(product.getId());
            row.setProductName(product.getName());
            row.setSku(product.getSku());
            row.setTotalOwned(product.getQuantityOwned());

            List<Integer> availByDate = new ArrayList<>();
            for (LocalDate date : dates) {
                LocalDateTime dayStart = date.atTime(8, 0);
                LocalDateTime dayEnd = date.atTime(22, 0);
                AvailabilityResultDTO res = checkAvailability(tenantId, product.getId(), 1, dayStart, dayEnd);
                availByDate.add(res.getAvailableQuantity());
            }
            row.setAvailableByDate(availByDate);
            rows.add(row);
        }

        AvailabilityMatrixDTO matrix = new AvailabilityMatrixDTO();
        matrix.setDates(dates);
        matrix.setProducts(rows);
        return matrix;
    }

    public AlternativeSuggestionsDTO getAlternativeSuggestions(String tenantId, UUID productId, int requestedQuantity,
                                                               LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

        AlternativeSuggestionsDTO dto = new AlternativeSuggestionsDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setRequestedQuantity(requestedQuantity);

        // 1. Find alternative dates (shifts of +1 to +7 days)
        long durationHours = java.time.Duration.between(startDateTime, endDateTime).toHours();
        if (durationHours <= 0) durationHours = 24;

        List<AlternativeSuggestionsDTO.DateOption> dateOptions = new ArrayList<>();
        for (int dayShift = 1; dayShift <= 14; dayShift++) {
            LocalDateTime altStart = startDateTime.plusDays(dayShift);
            LocalDateTime altEnd = altStart.plusHours(durationHours);
            AvailabilityResultDTO check = checkAvailability(tenantId, productId, requestedQuantity, altStart, altEnd);
            if (check.isAvailable()) {
                dateOptions.add(new AlternativeSuggestionsDTO.DateOption(altStart, altEnd, check.getAvailableQuantity()));
                if (dateOptions.size() >= 3) break;
            }
        }
        dto.setAlternativeDates(dateOptions);

        // 2. Find alternative products in same category or overall
        List<Product> candidates;
        if (product.getCategoryId() != null) {
            candidates = productRepository.findByTenantIdAndCategoryId(tenantId, product.getCategoryId());
        } else {
            candidates = productRepository.findByTenantId(tenantId);
        }

        List<AlternativeSuggestionsDTO.ProductOption> productOptions = new ArrayList<>();
        for (Product cand : candidates) {
            if (cand.getId().equals(productId)) continue;
            AvailabilityResultDTO check = checkAvailability(tenantId, cand.getId(), requestedQuantity, startDateTime, endDateTime);
            if (check.isAvailable()) {
                productOptions.add(new AlternativeSuggestionsDTO.ProductOption(
                        cand.getId(), cand.getName(), cand.getSku(), check.getAvailableQuantity()));
                if (productOptions.size() >= 3) break;
            }
        }
        dto.setAlternativeProducts(productOptions);

        return dto;
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
