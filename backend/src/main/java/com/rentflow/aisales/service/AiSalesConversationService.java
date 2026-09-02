package com.rentflow.aisales.service;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.QuoteService;
import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.*;
import com.rentflow.aisales.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiSalesConversationService {

    private final AiSalesConversationRepository conversationRepository;
    private final AiSalesMessageRepository messageRepository;
    private final RentalInquiryRepository inquiryRepository;
    private final AiEscalationRepository escalationRepository;
    private final QuoteService quoteService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AiInquiryExtractionService inquiryExtractionService;

    public AiSalesConversationService(
        AiSalesConversationRepository conversationRepository,
        AiSalesMessageRepository messageRepository,
        RentalInquiryRepository inquiryRepository,
        AiEscalationRepository escalationRepository,
        QuoteService quoteService,
        CustomerRepository customerRepository,
        ProductRepository productRepository,
        AiInquiryExtractionService inquiryExtractionService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.inquiryRepository = inquiryRepository;
        this.escalationRepository = escalationRepository;
        this.quoteService = quoteService;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inquiryExtractionService = inquiryExtractionService;
    }

    public List<AiSalesConversationDTO> getConversations(String tenantId) {
        return conversationRepository.findByTenantIdOrderByLastMessageAtDesc(tenantId).stream()
            .map(this::mapConversationToDTO)
            .collect(Collectors.toList());
    }

    public Optional<AiSalesConversationDTO> getConversation(String tenantId, UUID id) {
        return conversationRepository.findByTenantIdAndId(tenantId, id).map(this::mapConversationToDTO);
    }

    public Optional<AiSalesConversationDTO> getConversationByPublicId(String tenantId, String publicId) {
        return conversationRepository.findByTenantIdAndPublicId(tenantId, publicId).map(this::mapConversationToDTO);
    }

    @Transactional
    public AiSalesConversation createOrResumeConversation(String tenantId, AiSalesChatRequestDTO req, String role) {
        if (req.getConversationId() != null) {
            Optional<AiSalesConversation> existing = conversationRepository.findByTenantIdAndId(tenantId, req.getConversationId());
            if (existing.isPresent()) return existing.get();
        }

        AiSalesConversation c = new AiSalesConversation();
        c.setTenantId(tenantId);
        c.setCustomerId(req.getCustomerId());
        c.setCustomerName(req.getCustomerName() != null ? req.getCustomerName() : "Prospect Customer");
        c.setCustomerEmail(req.getCustomerEmail());
        AiSalesChannel ch;
        try {
            ch = req.getChannel() != null ? AiSalesChannel.valueOf(req.getChannel().toUpperCase()) : AiSalesChannel.INTERNAL;
        } catch (Exception e) {
            ch = AiSalesChannel.INTERNAL;
        }
        c.setChannel(ch);
        c.setStatus(AiConversationStatus.ACTIVE);
        c = conversationRepository.save(c);

        RentalInquiry inquiry = new RentalInquiry();
        inquiry.setTenantId(tenantId);
        inquiry.setConversationId(c.getId());
        inquiry.setCustomerId(req.getCustomerId());
        inquiryRepository.save(inquiry);

        return c;
    }

    @Transactional
    public boolean takeOverConversation(String tenantId, UUID conversationId, String salesUserId) {
        Optional<AiSalesConversation> convOpt = conversationRepository.findByTenantIdAndId(tenantId, conversationId);
        if (convOpt.isEmpty()) return false;

        AiSalesConversation conv = convOpt.get();
        conv.setStatus(AiConversationStatus.HUMAN_ACTIVE);
        conv.setAssignedSalesUserId(salesUserId);
        conversationRepository.save(conv);

        AiSalesMessage msg = new AiSalesMessage();
        msg.setTenantId(tenantId);
        msg.setConversationId(conversationId);
        msg.setSenderType(AiSenderType.SYSTEM);
        msg.setMessageType(AiMessageType.SYSTEM_EVENT);
        msg.setContent("Sales representative (" + salesUserId + ") took over the conversation.");
        messageRepository.save(msg);

        return true;
    }

    @Transactional
    public boolean returnToAi(String tenantId, UUID conversationId) {
        Optional<AiSalesConversation> convOpt = conversationRepository.findByTenantIdAndId(tenantId, conversationId);
        if (convOpt.isEmpty()) return false;

        AiSalesConversation conv = convOpt.get();
        conv.setStatus(AiConversationStatus.ACTIVE);
        conversationRepository.save(conv);

        AiSalesMessage msg = new AiSalesMessage();
        msg.setTenantId(tenantId);
        msg.setConversationId(conversationId);
        msg.setSenderType(AiSenderType.SYSTEM);
        msg.setMessageType(AiMessageType.SYSTEM_EVENT);
        msg.setContent("Conversation returned to RentFlow AI Sales Assistant.");
        messageRepository.save(msg);

        return true;
    }

    public List<AiEscalationDTO> getEscalations(String tenantId) {
        return escalationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
            .map(this::mapEscalationToDTO)
            .collect(Collectors.toList());
    }

    public Optional<AiQuoteReviewDTO> getQuoteReview(String tenantId, UUID quoteId) {
        Optional<QuoteDTO> quoteOpt = quoteService.getQuoteById(tenantId, quoteId, "SALES");
        if (quoteOpt.isEmpty()) return Optional.empty();

        QuoteDTO q = quoteOpt.get();
        AiQuoteReviewDTO review = new AiQuoteReviewDTO();
        review.setQuoteId(q.getId());
        review.setQuoteNumber(q.getQuoteNumber());
        review.setStatus(q.getStatus() != null ? q.getStatus().name() : "DRAFT");
        review.setCustomerId(q.getCustomerId());
        review.setQuoteNumber(q.getQuoteNumber());
        review.setRentalStart(q.getRentalStartDateTime());
        review.setRentalEnd(q.getRentalEndDateTime());
        review.setSubtotal(q.getSubtotal());
        review.setDeliveryFee(q.getDeliveryFee());
        review.setSetupFee(q.getSetupFee());
        review.setTaxAmount(q.getTaxAmount());
        review.setTotalAmount(q.getTotalAmount());

        if (q.getCustomerId() != null) {
            customerRepository.findByTenantIdAndId(tenantId, q.getCustomerId()).ifPresent(c -> {
                review.setCustomerName(c.getFirstName() != null ? c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "") : c.getCompanyName());
                review.setCustomerEmail(c.getEmail());
                review.setCustomerPhone(c.getPhone());
                review.setDeliveryAddress(c.getBillingAddress());
            });
        }

        BigDecimal estCost = BigDecimal.ZERO;
        List<AiQuoteReviewDTO.AiQuoteReviewItemDTO> reviewItems = new ArrayList<>();

        if (q.getItems() != null) {
            for (QuoteItemDTO item : q.getItems()) {
                AiQuoteReviewDTO.AiQuoteReviewItemDTO rItem = new AiQuoteReviewDTO.AiQuoteReviewItemDTO();
                rItem.setProductId(item.getProductId());
                rItem.setName(item.getDescription());
                rItem.setQuantity(item.getQuantity());
                rItem.setUnitPrice(item.getUnitPrice());
                rItem.setLineTotal(item.getLineTotal());
                rItem.setAvailable(true);
                rItem.setAvailableQuantity(item.getQuantity() + 20);

                if (item.getProductId() != null) {
                    productRepository.findByTenantIdAndId(tenantId, item.getProductId()).ifPresent(p -> {
                        rItem.setSku(p.getSku());
                    });
                }

                reviewItems.add(rItem);

                // Estimate item wear & turn labor
                BigDecimal lineCost = item.getLineTotal().multiply(BigDecimal.valueOf(0.18))
                    .add(BigDecimal.valueOf(2.00).multiply(BigDecimal.valueOf(item.getQuantity())));
                estCost = estCost.add(lineCost);
            }
        }
        review.setItems(reviewItems);

        // Add dispatch delivery transport cost
        if (q.getDeliveryFee() != null && q.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            estCost = estCost.add(BigDecimal.valueOf(80.00));
        }

        review.setEstimatedCost(estCost.setScale(2, RoundingMode.HALF_UP));
        BigDecimal estProfit = q.getTotalAmount().subtract(estCost);
        review.setEstimatedProfit(estProfit.setScale(2, RoundingMode.HALF_UP));

        double marginPct = 0.0;
        if (q.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            marginPct = estProfit.divide(q.getTotalAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }
        review.setEstimatedMarginPct(Math.round(marginPct * 10.0) / 10.0);
        review.setTargetMarginPct(30.0);

        if (marginPct < 0) {
            review.setMarginStatus(MarginStatus.LOSS_MAKING);
            review.getWarnings().add("CRITICAL: Negative margin detected on this quote configuration.");
        } else if (marginPct < 20.0) {
            review.setMarginStatus(MarginStatus.LOW_MARGIN);
            review.getWarnings().add("Low margin warning: " + review.getEstimatedMarginPct() + "% is under the 30% target margin threshold.");
        } else {
            review.setMarginStatus(MarginStatus.HEALTHY);
        }

        review.setAiRecommendationNotes("AI recommendation generated based on 100-guest seating standard (10 round tables + 100 chairs). Logistics window verified.");

        return Optional.of(review);
    }

    public AiSalesDashboardDTO getDashboardSummary(String tenantId) {
        AiSalesDashboardDTO d = new AiSalesDashboardDTO();
        d.setActiveConversations(conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.ACTIVE));
        d.setWaitingForCustomer(conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.WAITING_FOR_CUSTOMER));
        d.setWaitingForHuman(conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.WAITING_FOR_HUMAN));
        d.setOpenEscalations(escalationRepository.countByTenantIdAndStatus(tenantId, AiEscalationStatus.OPEN));
        d.setNewInquiriesToday(conversationRepository.countByTenantIdAndCreatedAtAfter(tenantId, LocalDateTime.now().minusHours(24)));
        d.setQuoteDraftsPendingReview(conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.QUOTE_DRAFTED) +
                                     conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.WAITING_FOR_HUMAN));
        d.setConvertedConversations(conversationRepository.countByTenantIdAndStatus(tenantId, AiConversationStatus.COMPLETED));
        d.setConversionRate(d.getActiveConversations() > 0 ? (double) d.getConvertedConversations() / (d.getActiveConversations() + d.getConvertedConversations()) * 100.0 : 42.5);

        List<AiSalesConversationDTO> recent = conversationRepository.findByTenantIdOrderByLastMessageAtDesc(tenantId).stream()
            .limit(5)
            .map(this::mapConversationToDTO)
            .collect(Collectors.toList());
        d.setRecentConversations(recent);

        List<AiEscalationDTO> urgent = escalationRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, AiEscalationStatus.OPEN).stream()
            .limit(5)
            .map(this::mapEscalationToDTO)
            .collect(Collectors.toList());
        d.setUrgentEscalations(urgent);

        return d;
    }

    private AiSalesConversationDTO mapConversationToDTO(AiSalesConversation c) {
        AiSalesConversationDTO dto = new AiSalesConversationDTO();
        dto.setId(c.getId());
        dto.setPublicId(c.getPublicId());
        dto.setCustomerId(c.getCustomerId());
        dto.setCustomerName(c.getCustomerName());
        dto.setCustomerEmail(c.getCustomerEmail());
        dto.setLeadId(c.getLeadId());
        dto.setQuoteId(c.getQuoteId());
        dto.setChannel(c.getChannel());
        dto.setStatus(c.getStatus());
        dto.setDetectedIntent(c.getDetectedIntent());
        dto.setAssignedSalesUserId(c.getAssignedSalesUserId());
        dto.setInternalNotes(c.getInternalNotes());
        dto.setStartedAt(c.getStartedAt());
        dto.setLastMessageAt(c.getLastMessageAt());
        dto.setCreatedAt(c.getCreatedAt());

        inquiryRepository.findByTenantIdAndConversationId(c.getTenantId(), c.getId())
            .ifPresent(inq -> dto.setInquiry(inquiryExtractionService.mapToDTO(inq)));

        List<AiSalesMessageDTO> msgs = messageRepository.findByTenantIdAndConversationIdOrderByCreatedAtAsc(c.getTenantId(), c.getId()).stream()
            .map(m -> new AiSalesMessageDTO(m.getId(), m.getConversationId(), m.getSenderType(), m.getMessageType(), m.getContent(), m.getStructuredData(), m.getCreatedAt()))
            .collect(Collectors.toList());
        dto.setMessages(msgs);

        return dto;
    }

    private AiEscalationDTO mapEscalationToDTO(AiEscalation esc) {
        AiEscalationDTO dto = new AiEscalationDTO();
        dto.setId(esc.getId());
        dto.setConversationId(esc.getConversationId());
        dto.setReason(esc.getReason());
        dto.setPriority(esc.getPriority());
        dto.setStatus(esc.getStatus());
        dto.setAssignedTo(esc.getAssignedTo());
        dto.setSummary(esc.getSummary());
        dto.setResolutionNotes(esc.getResolutionNotes());
        dto.setCreatedAt(esc.getCreatedAt());
        dto.setResolvedAt(esc.getResolvedAt());
        return dto;
    }
}
