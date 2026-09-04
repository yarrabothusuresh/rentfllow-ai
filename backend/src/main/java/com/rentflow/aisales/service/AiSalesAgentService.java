package com.rentflow.aisales.service;

import com.rentflow.aisales.dto.*;
import com.rentflow.aisales.model.*;
import com.rentflow.aisales.provider.AiSalesProvider;
import com.rentflow.aisales.provider.MockAiSalesProvider;
import com.rentflow.aisales.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AiSalesAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiSalesAgentService.class);

    private final AiSalesConversationRepository conversationRepository;
    private final AiSalesMessageRepository messageRepository;
    private final RentalInquiryRepository inquiryRepository;
    private final AiUsageRecordRepository usageRepository;
    private final AiFeedbackRepository feedbackRepository;
    private final AiSalesSettingsService settingsService;
    private final AiResponseValidatorService validatorService;
    private final AiSalesConversationService conversationService;
    private final MockAiSalesProvider mockProvider;
    private final List<AiSalesProvider> providers;

    public AiSalesAgentService(
        AiSalesConversationRepository conversationRepository,
        AiSalesMessageRepository messageRepository,
        RentalInquiryRepository inquiryRepository,
        AiUsageRecordRepository usageRepository,
        AiFeedbackRepository feedbackRepository,
        AiSalesSettingsService settingsService,
        AiResponseValidatorService validatorService,
        AiSalesConversationService conversationService,
        MockAiSalesProvider mockProvider,
        List<AiSalesProvider> providers
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.inquiryRepository = inquiryRepository;
        this.usageRepository = usageRepository;
        this.feedbackRepository = feedbackRepository;
        this.settingsService = settingsService;
        this.validatorService = validatorService;
        this.conversationService = conversationService;
        this.mockProvider = mockProvider;
        this.providers = providers;
    }

    @Transactional
    public AiSalesChatResponseDTO handleMessage(String tenantId, String userRole, AiSalesChatRequestDTO request) {
        long startTime = System.currentTimeMillis();

        // 1. Settings & feature checks
        AiTenantSettings settings = settingsService.getSettings(tenantId);
        if (!settings.isAiEnabled()) {
            AiSalesChatResponseDTO disabledResp = new AiSalesChatResponseDTO();
            disabledResp.setReplyText("AI assistance is currently unavailable. You can continue browsing our catalog or submit a rental request.");
            disabledResp.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
            return disabledResp;
        }

        // 2. Resolve or create conversation
        AiSalesConversation conv = conversationService.createOrResumeConversation(tenantId, request, userRole);

        // If human takeover is active, don't auto-reply as AI
        if (conv.getStatus() == AiConversationStatus.HUMAN_ACTIVE) {
            AiSalesMessage custMsg = new AiSalesMessage();
            custMsg.setTenantId(tenantId);
            custMsg.setConversationId(conv.getId());
            custMsg.setSenderType("CUSTOMER".equalsIgnoreCase(userRole) ? AiSenderType.CUSTOMER : AiSenderType.SALES_USER);
            custMsg.setContent(request.getMessage());
            messageRepository.save(custMsg);

            AiSalesChatResponseDTO takeoverResp = new AiSalesChatResponseDTO();
            takeoverResp.setConversationId(conv.getId());
            takeoverResp.setPublicId(conv.getPublicId());
            takeoverResp.setStatus(AiConversationStatus.HUMAN_ACTIVE);
            takeoverResp.setReplyText("Your message has been received by our sales team. An agent is currently attending to this conversation.");
            return takeoverResp;
        }

        // 3. Save incoming customer message
        AiSalesMessage incoming = new AiSalesMessage();
        incoming.setTenantId(tenantId);
        incoming.setConversationId(conv.getId());
        incoming.setSenderType("CUSTOMER".equalsIgnoreCase(userRole) ? AiSenderType.CUSTOMER : AiSenderType.SALES_USER);
        incoming.setContent(request.getMessage());
        messageRepository.save(incoming);

        // 4. Fetch history and structured inquiry
        List<AiSalesMessage> history = messageRepository.findByTenantIdAndConversationIdOrderByCreatedAtAsc(tenantId, conv.getId());
        RentalInquiry inquiry = inquiryRepository.findByTenantIdAndConversationId(tenantId, conv.getId())
            .orElseGet(() -> {
                RentalInquiry inq = new RentalInquiry();
                inq.setTenantId(tenantId);
                inq.setConversationId(conv.getId());
                return inquiryRepository.save(inq);
            });

        // 5. Select provider (defaults to Mock)
        AiSalesProvider activeProvider = resolveProvider(settings.getAiProvider());

        // 6. Process message
        AiSalesChatResponseDTO responseDTO;
        try {
            responseDTO = activeProvider.processMessage(
                tenantId,
                userRole,
                conv,
                history,
                request.getMessage(),
                inquiry
            );
        } catch (Exception e) {
            log.error("[AiSalesAgentService] Provider execution failed: {}", e.getMessage(), e);
            responseDTO = new AiSalesChatResponseDTO();
            responseDTO.setConversationId(conv.getId());
            responseDTO.setPublicId(conv.getPublicId());
            responseDTO.setReplyText("I'm having trouble completing that request right now. I have passed your inquiry to our sales team for prompt assistance.");
            responseDTO.setStatus(AiConversationStatus.WAITING_FOR_HUMAN);
        }

        // 7. Validate output guardrails
        validatorService.validateCustomerResponse(responseDTO);

        // 8. Update conversation & inquiry state
        conv.setStatus(responseDTO.getStatus());
        conv.setLastMessageAt(LocalDateTime.now());
        if (responseDTO.getDetectedIntent() != null) {
            conv.setDetectedIntent(responseDTO.getDetectedIntent());
        }
        if (responseDTO.getQuoteDraftId() != null) {
            conv.setQuoteId(responseDTO.getQuoteDraftId());
        }
        if (inquiry.getLeadId() != null && conv.getLeadId() == null) {
            conv.setLeadId(inquiry.getLeadId());
        }
        if (inquiry.getRentalRequestId() != null && conv.getRentalRequestId() == null) {
            conv.setRentalRequestId(inquiry.getRentalRequestId());
        }
        conversationRepository.save(conv);
        inquiryRepository.save(inquiry);

        // 9. Persist AI reply message
        AiSalesMessage aiReply = new AiSalesMessage();
        aiReply.setTenantId(tenantId);
        aiReply.setConversationId(conv.getId());
        aiReply.setSenderType(AiSenderType.AI);
        aiReply.setContent(responseDTO.getReplyText());
        messageRepository.save(aiReply);

        // 10. Record usage metrics
        long duration = System.currentTimeMillis() - startTime;
        AiUsageRecord usage = new AiUsageRecord();
        usage.setTenantId(tenantId);
        usage.setConversationId(conv.getId());
        usage.setProvider(activeProvider.getProviderName());
        usage.setModel(settings.getAiModel());
        usage.setOperation("SALES_CHAT");
        usage.setInputTokens(request.getMessage().length() / 4 + 50);
        usage.setOutputTokens(responseDTO.getReplyText().length() / 4);
        usage.setLatencyMs(duration);
        usage.setSuccess(true);
        usageRepository.save(usage);

        return responseDTO;
    }

    @Transactional
    public void recordFeedback(String tenantId, String submittedBy, AiFeedbackRequestDTO req) {
        AiFeedback fb = new AiFeedback();
        fb.setTenantId(tenantId);
        fb.setConversationId(req.getConversationId());
        fb.setMessageId(req.getMessageId());
        fb.setHelpful(req.isHelpful());
        fb.setReason(req.getReason());
        fb.setComments(req.getComments());
        fb.setSubmittedBy(submittedBy);
        feedbackRepository.save(fb);
    }

    private AiSalesProvider resolveProvider(String configuredName) {
        if (configuredName != null) {
            for (AiSalesProvider p : providers) {
                if (p.getProviderName().equalsIgnoreCase(configuredName)) {
                    return p;
                }
            }
        }
        return mockProvider;
    }
}
