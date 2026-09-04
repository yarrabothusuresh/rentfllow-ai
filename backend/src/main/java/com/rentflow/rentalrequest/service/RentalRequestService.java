package com.rentflow.rentalrequest.service;

import com.rentflow.rentalrequest.dto.RentalRequestDTO;
import com.rentflow.rentalrequest.model.RentalRequest;
import com.rentflow.rentalrequest.model.RentalRequestItem;
import com.rentflow.rentalrequest.model.RentalRequestStatus;
import com.rentflow.rentalrequest.repository.RentalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalRequestService {

    private final RentalRequestRepository rentalRequestRepository;

    public RentalRequestService(RentalRequestRepository rentalRequestRepository) {
        this.rentalRequestRepository = rentalRequestRepository;
    }

    public synchronized String generateRequestNumber(String tenantId) {
        long count = rentalRequestRepository.count() + 1;
        String candidate = String.format("REQ-%06d", count);
        while (rentalRequestRepository.findByTenantIdAndRequestNumber(tenantId, candidate).isPresent()) {
            count++;
            candidate = String.format("REQ-%06d", count);
        }
        return candidate;
    }

    public RentalRequestDTO createRentalRequest(String tenantId, RentalRequestDTO dto) {
        // Idempotency check: if idempotencyKey is supplied, check if request exists
        if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().trim().isEmpty()) {
            Optional<RentalRequest> existing = rentalRequestRepository.findByTenantIdAndIdempotencyKey(tenantId, dto.getIdempotencyKey().trim());
            if (existing.isPresent()) {
                return mapToDTO(existing.get());
            }
        }

        // Conversation-level idempotency fallback
        if (dto.getConversationId() != null) {
            Optional<RentalRequest> existingByConv = rentalRequestRepository.findByTenantIdAndConversationId(tenantId, dto.getConversationId());
            if (existingByConv.isPresent()) {
                return mapToDTO(existingByConv.get());
            }
        }

        RentalRequest request = new RentalRequest();
        request.setTenantId(tenantId);
        request.setRequestNumber(generateRequestNumber(tenantId));
        request.setIdempotencyKey(dto.getIdempotencyKey());
        request.setStatus(dto.getStatus() != null ? dto.getStatus() : RentalRequestStatus.SUBMITTED);
        request.setConversationId(dto.getConversationId());
        request.setLeadId(dto.getLeadId());
        request.setCustomerId(dto.getCustomerId());
        request.setQuoteId(dto.getQuoteId());

        request.setCustomerName(dto.getCustomerName() != null ? dto.getCustomerName().trim() : "Valued Prospect");
        request.setCustomerEmail(dto.getCustomerEmail() != null ? dto.getCustomerEmail().trim() : "inquiry@client.com");
        request.setCustomerPhone(dto.getCustomerPhone());

        request.setEventName(dto.getEventName());
        request.setEventType(dto.getEventType());
        request.setEventDate(dto.getEventDate());
        request.setRentalStartDate(dto.getRentalStartDate());
        request.setRentalEndDate(dto.getRentalEndDate());

        request.setDeliveryAddress(dto.getDeliveryAddress());
        request.setDeliveryCity(dto.getDeliveryCity());
        request.setDeliveryRequired(dto.isDeliveryRequired());

        request.setGuestCount(dto.getGuestCount());
        request.setEstimatedBudget(dto.getEstimatedBudget());
        request.setNotes(dto.getNotes());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (RentalRequestDTO.ItemDTO itemDto : dto.getItems()) {
                RentalRequestItem item = new RentalRequestItem();
                item.setProductId(itemDto.getProductId() != null ? itemDto.getProductId() : UUID.randomUUID());
                item.setProductName(itemDto.getProductName() != null ? itemDto.getProductName() : "Rental Item");
                item.setSku(itemDto.getSku());
                item.setQuantity(Math.max(1, itemDto.getQuantity()));
                item.setUnitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO);
                BigDecimal line = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setLineTotal(line);
                calculatedTotal = calculatedTotal.add(line);
                request.getItems().add(item);
            }
        }
        request.setEstimatedTotal(dto.getEstimatedTotal() != null && dto.getEstimatedTotal().compareTo(BigDecimal.ZERO) > 0
                ? dto.getEstimatedTotal() : calculatedTotal);

        RentalRequest saved = rentalRequestRepository.save(request);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<RentalRequestDTO> getRentalRequests(String tenantId) {
        return rentalRequestRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RentalRequestDTO> getRentalRequestById(String tenantId, UUID id) {
        return rentalRequestRepository.findByTenantIdAndId(tenantId, id).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<RentalRequestDTO> getRentalRequestByIdempotencyKey(String tenantId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return Optional.empty();
        }
        return rentalRequestRepository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey.trim()).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<RentalRequestDTO> getRentalRequestByConversationId(String tenantId, UUID conversationId) {
        if (conversationId == null) {
            return Optional.empty();
        }
        return rentalRequestRepository.findByTenantIdAndConversationId(tenantId, conversationId).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<RentalRequestDTO> getRentalRequestByConversation(String tenantId, UUID conversationId) {
        return rentalRequestRepository.findByTenantIdAndConversationId(tenantId, conversationId).map(this::mapToDTO);
    }

    public Optional<RentalRequestDTO> updateStatus(String tenantId, UUID id, RentalRequestStatus newStatus) {
        return rentalRequestRepository.findByTenantIdAndId(tenantId, id).map(req -> {
            req.setStatus(newStatus);
            return mapToDTO(rentalRequestRepository.save(req));
        });
    }

    public Optional<RentalRequestDTO> linkQuote(String tenantId, UUID id, UUID quoteId) {
        return rentalRequestRepository.findByTenantIdAndId(tenantId, id).map(req -> {
            req.setQuoteId(quoteId);
            req.setStatus(RentalRequestStatus.CONVERTED_TO_QUOTE);
            return mapToDTO(rentalRequestRepository.save(req));
        });
    }

    public Optional<RentalRequestDTO> linkLead(String tenantId, UUID id, UUID leadId) {
        return rentalRequestRepository.findByTenantIdAndId(tenantId, id).map(req -> {
            req.setLeadId(leadId);
            return mapToDTO(rentalRequestRepository.save(req));
        });
    }

    public RentalRequestDTO mapToDTO(RentalRequest entity) {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setId(entity.getId());
        dto.setTenantId(entity.getTenantId());
        dto.setRequestNumber(entity.getRequestNumber());
        dto.setIdempotencyKey(entity.getIdempotencyKey());
        dto.setStatus(entity.getStatus());
        dto.setConversationId(entity.getConversationId());
        dto.setLeadId(entity.getLeadId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setQuoteId(entity.getQuoteId());

        dto.setCustomerName(entity.getCustomerName());
        dto.setCustomerEmail(entity.getCustomerEmail());
        dto.setCustomerPhone(entity.getCustomerPhone());

        dto.setEventName(entity.getEventName());
        dto.setEventType(entity.getEventType());
        dto.setEventDate(entity.getEventDate());
        dto.setRentalStartDate(entity.getRentalStartDate());
        dto.setRentalEndDate(entity.getRentalEndDate());

        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setDeliveryCity(entity.getDeliveryCity());
        dto.setDeliveryRequired(entity.isDeliveryRequired());

        dto.setGuestCount(entity.getGuestCount());
        dto.setEstimatedBudget(entity.getEstimatedBudget());
        dto.setEstimatedTotal(entity.getEstimatedTotal());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(item ->
                new RentalRequestDTO.ItemDTO(
                    item.getProductId(),
                    item.getProductName(),
                    item.getSku(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
                )
            ).collect(Collectors.toList()));
        }

        return dto;
    }
}
