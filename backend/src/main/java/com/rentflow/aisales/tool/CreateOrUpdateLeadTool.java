package com.rentflow.aisales.tool;

import com.rentflow.ai.model.EventType;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.repository.AiSalesConversationRepository;
import com.rentflow.crm.dto.LeadCreateRequest;
import com.rentflow.crm.dto.LeadDetailDTO;
import com.rentflow.crm.dto.LeadUpdateRequest;
import com.rentflow.crm.model.*;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.crm.service.LeadService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component("aiSalesCreateOrUpdateLeadTool")
public class CreateOrUpdateLeadTool implements AiSalesTool {

    private final LeadService leadService;
    private final LeadRepository leadRepository;
    private final LeadActivityService activityService;
    private final AiSalesConversationRepository conversationRepository;

    public CreateOrUpdateLeadTool(LeadService leadService,
                                  LeadRepository leadRepository,
                                  LeadActivityService activityService,
                                  AiSalesConversationRepository conversationRepository) {
        this.leadService = leadService;
        this.leadRepository = leadRepository;
        this.activityService = activityService;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public String getName() {
        return "createOrUpdateLead";
    }

    @Override
    public String getDescription() {
        return "Idempotently create or update a structured CRM Lead from the conversational rental requirements.";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return true;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "SALES", "ADMIN", "OWNER", "STAFF");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        Map<String, Object> args = request.getArguments();

        String convIdStr = (String) args.get("conversationId");
        UUID convId = null;
        AiSalesConversation conversation = null;
        if (convIdStr != null && !convIdStr.trim().isEmpty()) {
            try {
                convId = UUID.fromString(convIdStr.trim());
                conversation = conversationRepository.findByTenantIdAndId(tenantId, convId).orElse(null);
            } catch (Exception ignored) {}
        }

        // Idempotency: Check if conversation already has an existing Lead
        UUID existingLeadId = conversation != null ? conversation.getLeadId() : null;
        if (existingLeadId == null && args.get("leadId") != null) {
            try { existingLeadId = UUID.fromString(args.get("leadId").toString().trim()); } catch (Exception ignored) {}
        }

        String email = args.get("email") != null ? args.get("email").toString().trim() : null;
        String phone = args.get("phone") != null ? args.get("phone").toString().trim() : null;
        String customerName = args.get("customerName") != null ? args.get("customerName").toString().trim() : "Prospective Client";
        String eventTypeStr = args.get("eventType") != null ? args.get("eventType").toString() : null;
        String eventName = args.get("eventName") != null ? args.get("eventName").toString() : (customerName + "'s Event");
        Integer guestCount = args.get("guestCount") != null ? Integer.parseInt(args.get("guestCount").toString()) : null;
        BigDecimal budget = args.get("budget") != null ? new BigDecimal(args.get("budget").toString()) : null;
        String venue = args.get("venue") != null ? args.get("venue").toString() : null;
        String address = args.get("address") != null ? args.get("address").toString() : null;

        LocalDate eventDate = null;
        if (args.get("eventDate") != null) {
            try { eventDate = LocalDate.parse(args.get("eventDate").toString().trim().substring(0, 10)); } catch (Exception ignored) {}
        }

        EventType eventType = null;
        if (eventTypeStr != null) {
            try { eventType = EventType.valueOf(eventTypeStr.toUpperCase()); } catch (Exception ignored) {}
        }

        if (existingLeadId != null) {
            // Update existing lead idempotently
            Optional<Lead> leadOpt = leadRepository.findByTenantIdAndId(tenantId, existingLeadId);
            if (leadOpt.isPresent()) {
                Lead lead = leadOpt.get();
                LeadUpdateRequest updateReq = new LeadUpdateRequest();
                String[] nameParts = customerName.split("\\s+", 2);
                updateReq.setFirstName(nameParts[0]);
                if (nameParts.length > 1) updateReq.setLastName(nameParts[1]);
                if (email != null && !email.isBlank()) updateReq.setEmail(email);
                if (phone != null && !phone.isBlank()) updateReq.setPhone(phone);
                if (eventDate != null) updateReq.setEventDate(eventDate);
                if (eventType != null) updateReq.setEventType(eventType);
                if (guestCount != null) updateReq.setGuestCount(guestCount);
                if (budget != null) updateReq.setEstimatedBudget(budget);
                if (venue != null) updateReq.setVenueName(venue);
                if (address != null) updateReq.setVenueAddressSnapshot(address);

                LeadDetailDTO updated = leadService.updateLead(tenantId, lead.getId(), updateReq, "AI_SALES_AGENT");
                activityService.logActivity(tenantId, lead.getId(), ActivityType.AI_REQUIREMENTS_CAPTURED,
                        ActivityDirection.INTERNAL, "AI Requirements Updated",
                        "Updated event specifications from conversational sales inquiry.",
                        null, null, "AI_CONVERSATION", convId != null ? convId.toString() : null, "AI_SALES_AGENT");

                Map<String, Object> res = new LinkedHashMap<>();
                res.put("leadId", updated.getId());
                res.put("leadNumber", updated.getLeadNumber());
                res.put("contactName", updated.getContactName());
                res.put("stage", updated.getStage().name());
                res.put("action", "UPDATED");
                return ToolCallResultDTO.success(getName(), res, false);
            }
        }

        // Create new lead
        LeadCreateRequest createReq = new LeadCreateRequest();
        createReq.setSource(LeadSource.AI_STOREFRONT);
        createReq.setPriority(LeadPriority.NORMAL);

        String[] parts = customerName.split("\\s+", 2);
        createReq.setFirstName(parts[0]);
        createReq.setLastName(parts.length > 1 ? parts[1] : "");
        createReq.setEmail(email != null && !email.isBlank() ? email : "inquiry@client.com");
        createReq.setPhone(phone);
        createReq.setEventName(eventName);
        createReq.setEventType(eventType);
        createReq.setEventDate(eventDate);
        createReq.setGuestCount(guestCount);
        createReq.setEstimatedBudget(budget);
        createReq.setVenueName(venue);
        createReq.setVenueAddressSnapshot(address);
        createReq.setCustomerNotes(args.get("notes") != null ? args.get("notes").toString() : "Captured via AI Sales Agent");

        LeadDetailDTO created = leadService.createLead(tenantId, createReq, "AI_SALES_AGENT");

        // Link to conversation
        if (conversation != null) {
            conversation.setLeadId(created.getId());
            conversationRepository.save(conversation);
        }

        activityService.logActivity(tenantId, created.getId(), ActivityType.AI_CONVERSATION_STARTED,
                ActivityDirection.INTERNAL, "AI Conversation Started",
                "New prospective client engaged AI Sales Agent.",
                null, null, "AI_CONVERSATION", convId != null ? convId.toString() : null, "AI_SALES_AGENT");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("leadId", created.getId());
        res.put("leadNumber", created.getLeadNumber());
        res.put("contactName", created.getContactName());
        res.put("stage", created.getStage().name());
        res.put("action", "CREATED");

        return ToolCallResultDTO.success(getName(), res, false);
    }
}
