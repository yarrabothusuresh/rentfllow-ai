package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final QuoteDiscountRepository quoteDiscountRepository;
    private final QuoteFeeRepository quoteFeeRepository;
    private final ProductRepository productRepository;
    private final AvailabilityService availabilityService;
    private final QuoteCalculationService calculationService;

    // Default max discount percentage for SALES role
    public static final BigDecimal SALES_MAX_DISCOUNT_PCT = new BigDecimal("20.00");

    public QuoteService(QuoteRepository quoteRepository,
                        QuoteItemRepository quoteItemRepository,
                        QuoteDiscountRepository quoteDiscountRepository,
                        QuoteFeeRepository quoteFeeRepository,
                        ProductRepository productRepository,
                        AvailabilityService availabilityService,
                        QuoteCalculationService calculationService) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.quoteDiscountRepository = quoteDiscountRepository;
        this.quoteFeeRepository = quoteFeeRepository;
        this.productRepository = productRepository;
        this.availabilityService = availabilityService;
        this.calculationService = calculationService;
    }

    public synchronized String generateQuoteNumber(String tenantId) {
        long count = quoteRepository.countByTenantId(tenantId) + 1;
        return String.format("QUO-%06d", count);
    }

    @Transactional
    public QuoteDTO createQuote(String tenantId, QuoteDTO dto, String userRole) {
        validateRolePricingPermission(userRole, dto);

        Quote q = new Quote();
        q.setTenantId(tenantId);
        q.setQuoteNumber(generateQuoteNumber(tenantId));
        q.setCustomerId(dto.getCustomerId());
        q.setEventId(dto.getEventId());
        q.setStatus(dto.getStatus() != null ? dto.getStatus() : QuoteStatus.DRAFT);
        q.setQuoteDate(dto.getQuoteDate() != null ? dto.getQuoteDate() : LocalDate.now());
        q.setValidUntil(dto.getValidUntil() != null ? dto.getValidUntil() : q.getQuoteDate().plusDays(7));
        q.setRentalStartDateTime(dto.getRentalStartDateTime() != null ? dto.getRentalStartDateTime() : LocalDateTime.now());
        q.setRentalEndDateTime(dto.getRentalEndDateTime() != null ? dto.getRentalEndDateTime() : q.getRentalStartDateTime().plusDays(2));
        q.setNotes(dto.getNotes());
        q.setInternalNotes(dto.getInternalNotes());
        q.setCreatedBy(userRole);

        // Copy financial values if passed
        q.setDeliveryFee(dto.getDeliveryFee() != null ? dto.getDeliveryFee() : BigDecimal.ZERO);
        q.setPickupFee(dto.getPickupFee() != null ? dto.getPickupFee() : BigDecimal.ZERO);
        q.setSetupFee(dto.getSetupFee() != null ? dto.getSetupFee() : BigDecimal.ZERO);
        q.setBreakdownFee(dto.getBreakdownFee() != null ? dto.getBreakdownFee() : BigDecimal.ZERO);
        q.setServiceFee(dto.getServiceFee() != null ? dto.getServiceFee() : BigDecimal.ZERO);
        q.setTaxRate(dto.getTaxRate() != null ? dto.getTaxRate() : new BigDecimal("8.25"));
        q.setDepositPercentage(dto.getDepositPercentage() != null ? dto.getDepositPercentage() : new BigDecimal("30.00"));

        Quote savedQuote = quoteRepository.save(q);

        // Save Quote Items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (QuoteItemDTO itemDto : dto.getItems()) {
                QuoteItem item = new QuoteItem();
                item.setQuoteId(savedQuote.getId());
                item.setProductId(itemDto.getProductId());
                item.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : "Rental Item");
                item.setQuantity(Math.max(1, itemDto.getQuantity()));
                item.setUnitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO);
                item.setStandardUnitPrice(itemDto.getStandardUnitPrice() != null ? itemDto.getStandardUnitPrice() : item.getUnitPrice());
                item.setPricingStrategy(itemDto.getPricingStrategy() != null ? itemDto.getPricingStrategy() : PricingStrategy.PER_EVENT);
                item.setRentalDays(Math.max(1, itemDto.getRentalDays()));
                item.setNotes(itemDto.getNotes());
                quoteItemRepository.save(item);
            }
        }

        // Recalculate Quote Totals
        recalculateAndSave(savedQuote);

        return getQuoteById(tenantId, savedQuote.getId(), userRole)
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve created quote"));
    }

    public List<QuoteDTO> getQuotes(String tenantId, String userRole) {
        return quoteRepository.findByTenantId(tenantId).stream()
                .map(q -> mapToDTO(q, userRole))
                .collect(Collectors.toList());
    }

    public Optional<QuoteDTO> getQuoteById(String tenantId, UUID id, String userRole) {
        return quoteRepository.findByTenantIdAndId(tenantId, id)
                .map(q -> mapToDTO(q, userRole));
    }

    @Transactional
    public Optional<QuoteDTO> updateQuote(String tenantId, UUID id, QuoteDTO dto, String userRole) {
        validateRolePricingPermission(userRole, dto);

        return quoteRepository.findByTenantIdAndId(tenantId, id).map(q -> {
            if (q.getStatus() == QuoteStatus.EXPIRED || q.getStatus() == QuoteStatus.CANCELLED) {
                throw new IllegalStateException("Quote has expired or been cancelled and cannot be modified.");
            }

            q.setCustomerId(dto.getCustomerId());
            q.setEventId(dto.getEventId());
            if (dto.getQuoteDate() != null) q.setQuoteDate(dto.getQuoteDate());
            if (dto.getValidUntil() != null) q.setValidUntil(dto.getValidUntil());
            if (dto.getRentalStartDateTime() != null) q.setRentalStartDateTime(dto.getRentalStartDateTime());
            if (dto.getRentalEndDateTime() != null) q.setRentalEndDateTime(dto.getRentalEndDateTime());
            q.setNotes(dto.getNotes());
            if (!"CUSTOMER".equalsIgnoreCase(userRole)) {
                q.setInternalNotes(dto.getInternalNotes());
            }

            q.setDeliveryFee(dto.getDeliveryFee() != null ? dto.getDeliveryFee() : q.getDeliveryFee());
            q.setPickupFee(dto.getPickupFee() != null ? dto.getPickupFee() : q.getPickupFee());
            q.setSetupFee(dto.getSetupFee() != null ? dto.getSetupFee() : q.getSetupFee());
            q.setBreakdownFee(dto.getBreakdownFee() != null ? dto.getBreakdownFee() : q.getBreakdownFee());
            q.setServiceFee(dto.getServiceFee() != null ? dto.getServiceFee() : q.getServiceFee());
            q.setTaxRate(dto.getTaxRate() != null ? dto.getTaxRate() : q.getTaxRate());
            q.setDepositPercentage(dto.getDepositPercentage() != null ? dto.getDepositPercentage() : q.getDepositPercentage());

            recalculateAndSave(q);
            return mapToDTO(q, userRole);
        });
    }

    @Transactional
    public boolean deleteQuote(String tenantId, UUID id) {
        return quoteRepository.findByTenantIdAndId(tenantId, id).map(q -> {
            quoteItemRepository.deleteByQuoteId(q.getId());
            quoteDiscountRepository.deleteByQuoteId(q.getId());
            quoteFeeRepository.deleteByQuoteId(q.getId());
            quoteRepository.delete(q);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<QuoteDTO> updateStatus(String tenantId, UUID id, QuoteStatus status, String userRole) {
        return quoteRepository.findByTenantIdAndId(tenantId, id).map(q -> {
            q.setStatus(status);
            Quote saved = quoteRepository.save(q);
            return mapToDTO(saved, userRole);
        });
    }

    @Transactional
    public Optional<QuoteDTO> duplicateQuote(String tenantId, UUID id, QuoteDuplicateRequest dupReq, String userRole) {
        return quoteRepository.findByTenantIdAndId(tenantId, id).map(original -> {
            Quote dup = new Quote();
            dup.setTenantId(tenantId);
            dup.setQuoteNumber(generateQuoteNumber(tenantId));
            dup.setCustomerId(original.getCustomerId());
            dup.setEventId(original.getEventId());
            dup.setStatus(QuoteStatus.DRAFT);

            dup.setQuoteDate(dupReq != null && dupReq.getNewQuoteDate() != null ? dupReq.getNewQuoteDate() : LocalDate.now());
            dup.setValidUntil(dupReq != null && dupReq.getNewValidUntil() != null ? dupReq.getNewValidUntil() : dup.getQuoteDate().plusDays(7));
            dup.setRentalStartDateTime(dupReq != null && dupReq.getNewRentalStartDateTime() != null ? dupReq.getNewRentalStartDateTime() : original.getRentalStartDateTime());
            dup.setRentalEndDateTime(dupReq != null && dupReq.getNewRentalEndDateTime() != null ? dupReq.getNewRentalEndDateTime() : original.getRentalEndDateTime());

            dup.setDeliveryFee(original.getDeliveryFee());
            dup.setPickupFee(original.getPickupFee());
            dup.setSetupFee(original.getSetupFee());
            dup.setBreakdownFee(original.getBreakdownFee());
            dup.setServiceFee(original.getServiceFee());
            dup.setTaxRate(original.getTaxRate());
            dup.setDepositPercentage(original.getDepositPercentage());
            dup.setNotes(original.getNotes());
            dup.setInternalNotes("Duplicated from " + original.getQuoteNumber());
            dup.setCreatedBy(userRole);

            Quote savedDup = quoteRepository.save(dup);

            // Duplicate Line Items
            List<QuoteItem> originalItems = quoteItemRepository.findByQuoteId(original.getId());
            for (QuoteItem item : originalItems) {
                QuoteItem newItem = new QuoteItem();
                newItem.setQuoteId(savedDup.getId());
                newItem.setProductId(item.getProductId());
                newItem.setDescription(item.getDescription());
                newItem.setQuantity(item.getQuantity());
                newItem.setUnitPrice(item.getUnitPrice());
                newItem.setStandardUnitPrice(item.getStandardUnitPrice());
                newItem.setPricingStrategy(item.getPricingStrategy());
                newItem.setRentalDays(item.getRentalDays());
                newItem.setNotes(item.getNotes());
                quoteItemRepository.save(newItem);
            }

            recalculateAndSave(savedDup);
            return mapToDTO(savedDup, userRole);
        });
    }

    // --- QUOTE ITEM MANAGEMENT ---

    @Transactional
    public QuoteDTO addQuoteItem(String tenantId, UUID quoteId, QuoteItemDTO itemDto, String userRole) {
        Quote q = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (List.of("WAREHOUSE", "DRIVER", "CUSTOMER").contains(userRole.toUpperCase())) {
            throw new IllegalArgumentException("You do not have permission to modify quote pricing.");
        }

        QuoteItem item = new QuoteItem();
        item.setQuoteId(q.getId());
        item.setProductId(itemDto.getProductId());

        // Fetch standard price from Product Catalog
        if (itemDto.getProductId() != null) {
            productRepository.findById(itemDto.getProductId()).ifPresent(p -> {
                if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
                    item.setDescription(p.getName());
                }
                item.setStandardUnitPrice(p.getRentalPrice());
            });
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : "Rental Equipment");
        }

        item.setQuantity(Math.max(1, itemDto.getQuantity()));
        item.setUnitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : (item.getStandardUnitPrice() != null ? item.getStandardUnitPrice() : BigDecimal.ZERO));
        if (item.getStandardUnitPrice() == null) item.setStandardUnitPrice(item.getUnitPrice());
        item.setPricingStrategy(itemDto.getPricingStrategy() != null ? itemDto.getPricingStrategy() : PricingStrategy.PER_EVENT);
        item.setRentalDays(Math.max(1, itemDto.getRentalDays()));
        item.setNotes(itemDto.getNotes());

        quoteItemRepository.save(item);
        recalculateAndSave(q);

        return mapToDTO(q, userRole);
    }

    @Transactional
    public QuoteDTO updateQuoteItem(String tenantId, UUID quoteId, UUID itemId, QuoteItemDTO itemDto, String userRole) {
        Quote q = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (List.of("WAREHOUSE", "DRIVER", "CUSTOMER").contains(userRole.toUpperCase())) {
            throw new IllegalArgumentException("You do not have permission to modify quote pricing.");
        }

        QuoteItem item = quoteItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Quote item not found"));

        if (itemDto.getQuantity() > 0) item.setQuantity(itemDto.getQuantity());
        if (itemDto.getUnitPrice() != null) item.setUnitPrice(itemDto.getUnitPrice());
        if (itemDto.getPricingStrategy() != null) item.setPricingStrategy(itemDto.getPricingStrategy());
        if (itemDto.getRentalDays() > 0) item.setRentalDays(itemDto.getRentalDays());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        item.setNotes(itemDto.getNotes());

        quoteItemRepository.save(item);
        recalculateAndSave(q);

        return mapToDTO(q, userRole);
    }

    @Transactional
    public QuoteDTO deleteQuoteItem(String tenantId, UUID quoteId, UUID itemId, String userRole) {
        Quote q = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        quoteItemRepository.deleteById(itemId);
        recalculateAndSave(q);

        return mapToDTO(q, userRole);
    }

    // --- RECALCULATION & HELPER METHODS ---

    @Transactional
    public void recalculateAndSave(Quote q) {
        List<QuoteItem> items = quoteItemRepository.findByQuoteId(q.getId());

        QuoteCalculationRequest req = new QuoteCalculationRequest();
        req.setDeliveryFee(q.getDeliveryFee());
        req.setPickupFee(q.getPickupFee());
        req.setSetupFee(q.getSetupFee());
        req.setBreakdownFee(q.getBreakdownFee());
        req.setServiceFee(q.getServiceFee());
        req.setTaxRate(q.getTaxRate());
        req.setDepositPercentage(q.getDepositPercentage());
        req.setDiscountValue(q.getDiscountAmount()); // Discount stored as fixed amount on Quote level

        for (QuoteItem item : items) {
            QuoteItemDTO idto = new QuoteItemDTO();
            idto.setId(item.getId());
            idto.setQuoteId(item.getQuoteId());
            idto.setProductId(item.getProductId());
            idto.setDescription(item.getDescription());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setStandardUnitPrice(item.getStandardUnitPrice());
            idto.setPricingStrategy(item.getPricingStrategy());
            idto.setRentalDays(item.getRentalDays());
            req.getItems().add(idto);
        }

        QuoteCalculationResponse resp = calculationService.calculate(req);

        // Update Line Items subtotal
        for (QuoteItemDTO calced : resp.getCalculatedItems()) {
            if (calced.getId() != null) {
                quoteItemRepository.findById(calced.getId()).ifPresent(entity -> {
                    entity.setLineSubtotal(calced.getLineSubtotal());
                    entity.setLineTotal(calced.getLineSubtotal());
                    quoteItemRepository.save(entity);
                });
            }
        }

        q.setSubtotal(resp.getSubtotal());
        q.setDiscountAmount(resp.getDiscountAmount());
        q.setDeliveryFee(resp.getDeliveryFee());
        q.setPickupFee(resp.getPickupFee());
        q.setSetupFee(resp.getSetupFee());
        q.setBreakdownFee(resp.getBreakdownFee());
        q.setServiceFee(resp.getServiceFee());
        q.setTaxAmount(resp.getTaxAmount());
        q.setTotalAmount(resp.getTotalAmount());
        q.setDepositAmount(resp.getDepositAmount());

        quoteRepository.save(q);
    }

    private void validateRolePricingPermission(String userRole, QuoteDTO dto) {
        String role = userRole != null ? userRole.toUpperCase() : "OWNER";

        if (List.of("WAREHOUSE", "DRIVER", "CUSTOMER").contains(role)) {
            if (dto.getDiscountAmount() != null && dto.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("You do not have permission to apply discounts.");
            }
        }

        if ("SALES".equals(role)) {
            if (dto.getDiscountAmount() != null && dto.getSubtotal() != null && dto.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountPct = dto.getDiscountAmount().multiply(new BigDecimal("100"))
                        .divide(dto.getSubtotal(), 2, QuoteCalculationService.ROUNDING);
                if (discountPct.compareTo(SALES_MAX_DISCOUNT_PCT) > 0) {
                    throw new IllegalArgumentException("Discount exceeds your permission limit.");
                }
            }
        }
    }

    public QuoteDTO mapToDTO(Quote q, String userRole) {
        QuoteDTO dto = new QuoteDTO();
        dto.setId(q.getId());
        dto.setTenantId(q.getTenantId());
        dto.setQuoteNumber(q.getQuoteNumber());
        dto.setCustomerId(q.getCustomerId());
        dto.setEventId(q.getEventId());
        dto.setStatus(q.getStatus());
        dto.setQuoteDate(q.getQuoteDate());
        dto.setValidUntil(q.getValidUntil());
        dto.setRentalStartDateTime(q.getRentalStartDateTime());
        dto.setRentalEndDateTime(q.getRentalEndDateTime());
        dto.setSubtotal(q.getSubtotal());
        dto.setDiscountAmount(q.getDiscountAmount());
        dto.setDeliveryFee(q.getDeliveryFee());
        dto.setPickupFee(q.getPickupFee());
        dto.setSetupFee(q.getSetupFee());
        dto.setBreakdownFee(q.getBreakdownFee());
        dto.setServiceFee(q.getServiceFee());
        dto.setTotalFees(q.getDeliveryFee().add(q.getPickupFee()).add(q.getSetupFee()).add(q.getBreakdownFee()).add(q.getServiceFee()));
        dto.setTaxRate(q.getTaxRate());
        dto.setTaxAmount(q.getTaxAmount());
        dto.setTotalAmount(q.getTotalAmount());
        dto.setDepositPercentage(q.getDepositPercentage());
        dto.setDepositAmount(q.getDepositAmount());
        dto.setRemainingBalance(q.getTotalAmount().subtract(q.getDepositAmount()).max(BigDecimal.ZERO));
        dto.setNotes(q.getNotes());
        dto.setCreatedBy(q.getCreatedBy());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());

        // Field Protection for CUSTOMER role
        if ("CUSTOMER".equalsIgnoreCase(userRole)) {
            dto.setInternalNotes(null);
        } else {
            dto.setInternalNotes(q.getInternalNotes());
        }

        // Map Line Items with Availability Verification
        List<QuoteItem> items = quoteItemRepository.findByQuoteId(q.getId());
        boolean hasShortage = false;
        List<String> shortageWarnings = new ArrayList<>();

        for (QuoteItem item : items) {
            QuoteItemDTO idto = new QuoteItemDTO();
            idto.setId(item.getId());
            idto.setQuoteId(item.getQuoteId());
            idto.setProductId(item.getProductId());
            idto.setDescription(item.getDescription());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setPricingStrategy(item.getPricingStrategy());
            idto.setRentalDays(item.getRentalDays());
            idto.setLineSubtotal(item.getLineSubtotal());
            idto.setLineTotal(item.getLineTotal());
            idto.setNotes(item.getNotes());
            idto.setCreatedAt(item.getCreatedAt());

            if (!"CUSTOMER".equalsIgnoreCase(userRole)) {
                idto.setStandardUnitPrice(item.getStandardUnitPrice());
                if (item.getStandardUnitPrice() != null && item.getUnitPrice() != null) {
                    idto.setPriceOverrideDifference(item.getUnitPrice().subtract(item.getStandardUnitPrice()));
                }
            } else {
                idto.setStandardUnitPrice(null);
                idto.setPriceOverrideDifference(null);
            }

            // Perform Availability Validation Check against AvailabilityService
            if (item.getProductId() != null && q.getRentalStartDateTime() != null && q.getRentalEndDateTime() != null) {
                AvailabilityResultDTO avail = availabilityService.checkAvailability(
                        q.getTenantId(), item.getProductId(), item.getQuantity(),
                        q.getRentalStartDateTime(), q.getRentalEndDateTime());

                idto.setAvailable(avail.isAvailable());
                idto.setAvailableQuantity(avail.getAvailableQuantity());
                int shortage = avail.getShortage();
                if (shortage > 0) {
                    hasShortage = true;
                    shortageWarnings.add("Inventory shortage for " + item.getDescription() +
                            ": Requested " + item.getQuantity() + ", Available " + avail.getAvailableQuantity() +
                            " (Shortage: " + shortage + ").");
                }
            }

            dto.getItems().add(idto);
        }

        dto.setHasAvailabilityShortage(hasShortage);
        dto.setShortageWarnings(shortageWarnings);

        return dto;
    }
}
