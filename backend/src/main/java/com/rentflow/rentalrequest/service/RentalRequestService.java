package com.rentflow.rentalrequest.service;

import com.rentflow.rentalrequest.dto.RentalRequestDTO;
import com.rentflow.rentalrequest.model.RentalRequest;
import com.rentflow.rentalrequest.model.RentalRequestItem;
import com.rentflow.rentalrequest.model.RentalRequestStatus;
import com.rentflow.rentalrequest.repository.RentalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalRequestService {

    private final RentalRequestRepository rentalRequestRepository;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    public RentalRequestService(RentalRequestRepository rentalRequestRepository,
                                org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.rentalRequestRepository = rentalRequestRepository;
        this.transactionTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
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

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public RentalRequestDTO createRentalRequest(String tenantId, RentalRequestDTO dto) {
        // 1. Calculate canonical payload fingerprint
        List<String> canonicalItems = new ArrayList<>();
        if (dto.getItems() != null) {
            for (RentalRequestDTO.ItemDTO item : dto.getItems()) {
                canonicalItems.add((item.getProductId() != null ? item.getProductId().toString() : "") + ":" + item.getQuantity());
            }
        }
        String currentHash = com.rentflow.common.idempotency.IdempotencyUtils.computeRequestFingerprint(
                tenantId,
                dto.getCustomerEmail(),
                dto.getRentalStartDate() != null ? dto.getRentalStartDate().toString() : "",
                dto.getRentalEndDate() != null ? dto.getRentalEndDate().toString() : "",
                canonicalItems,
                dto.getEstimatedTotal() != null ? dto.getEstimatedTotal().toPlainString() : "0"
        );

        // 2. Fast-path idempotency check: if idempotencyKey is supplied, check if request exists
        if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().trim().isEmpty()) {
            String lockKey = (tenantId + ":" + dto.getIdempotencyKey().trim()).intern();
            synchronized (lockKey) {
                return transactionTemplate.execute(status -> {
                    Optional<RentalRequest> existing = rentalRequestRepository.findByTenantIdAndIdempotencyKey(tenantId, dto.getIdempotencyKey().trim());
                    if (existing.isPresent()) {
                        RentalRequest req = existing.get();
                        if (req.getRequestHash() != null && !req.getRequestHash().equals(currentHash)) {
                            throw new com.rentflow.payment.exception.IdempotencyConflictException(
                                    "This request has changed since it was first submitted. Please start a new checkout.");
                        }
                        RentalRequestDTO result = mapToDTO(req);
                        result.setIdempotentReplay(true);
                        return result;
                    }
                    return doCreateRentalRequest(tenantId, dto, currentHash);
                });
            }
        }

        return transactionTemplate.execute(status -> doCreateRentalRequest(tenantId, dto, currentHash));
    }

    private RentalRequestDTO doCreateRentalRequest(String tenantId, RentalRequestDTO dto, String currentHash) {
        // Conversation-level idempotency fallback
        if (dto.getConversationId() != null) {
            Optional<RentalRequest> existingByConv = rentalRequestRepository.findByTenantIdAndConversationId(tenantId, dto.getConversationId());
            if (existingByConv.isPresent()) {
                RentalRequestDTO result = mapToDTO(existingByConv.get());
                result.setIdempotentReplay(true);
                return result;
            }
        }

        RentalRequest request = new RentalRequest();
        request.setTenantId(tenantId);
        request.setRequestNumber(generateRequestNumber(tenantId));
        request.setIdempotencyKey(dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().trim().isEmpty() ? dto.getIdempotencyKey().trim() : null);
        request.setRequestHash(currentHash);
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
                request.addItem(item);
            }
        }
        request.setEstimatedTotal(dto.getEstimatedTotal() != null && dto.getEstimatedTotal().compareTo(BigDecimal.ZERO) > 0
                ? dto.getEstimatedTotal() : calculatedTotal);

        try {
            RentalRequest saved = rentalRequestRepository.saveAndFlush(request);
            return mapToDTO(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Concurrent race condition fallback: reload existing record committed by competing process/cluster
            if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().trim().isEmpty()) {
                Optional<RentalRequest> concurrentRecord = rentalRequestRepository.findByTenantIdAndIdempotencyKey(tenantId, dto.getIdempotencyKey().trim());
                if (concurrentRecord.isPresent()) {
                    RentalRequest existingReq = concurrentRecord.get();
                    if (existingReq.getRequestHash() != null && !existingReq.getRequestHash().equals(currentHash)) {
                        throw new com.rentflow.payment.exception.IdempotencyConflictException(
                                "This request has changed since it was first submitted. Please start a new checkout.");
                    }
                    RentalRequestDTO result = mapToDTO(existingReq);
                    result.setIdempotentReplay(true);
                    return result;
                }
            }
            throw e;
        }
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
        dto.setRequestHash(entity.getRequestHash());
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
