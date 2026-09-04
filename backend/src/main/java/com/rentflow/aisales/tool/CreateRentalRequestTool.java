package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.AiSalesConversation;
import com.rentflow.aisales.repository.AiSalesConversationRepository;
import com.rentflow.crm.model.ActivityDirection;
import com.rentflow.crm.model.ActivityType;
import com.rentflow.crm.service.LeadActivityService;
import com.rentflow.rentalrequest.dto.RentalRequestDTO;
import com.rentflow.rentalrequest.model.RentalRequestStatus;
import com.rentflow.rentalrequest.service.RentalRequestService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component("aiSalesCreateRentalRequestTool")
public class CreateRentalRequestTool implements AiSalesTool {

    private final RentalRequestService rentalRequestService;
    private final AiSalesConversationRepository conversationRepository;
    private final LeadActivityService activityService;

    public CreateRentalRequestTool(RentalRequestService rentalRequestService,
                                   AiSalesConversationRepository conversationRepository,
                                   LeadActivityService activityService) {
        this.rentalRequestService = rentalRequestService;
        this.conversationRepository = conversationRepository;
        this.activityService = activityService;
    }

    @Override
    public String getName() {
        return "createRentalRequest";
    }

    @Override
    public String getDescription() {
        return "Create a formal, non-binding Rental Request for sales team review. Requires explicit customer confirmation. Never confirms bookings or reserves inventory.";
    }

    @Override
    public boolean isCustomerVisible() {
        return true;
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

        // Idempotency: check if rental request already exists for this conversation
        if (conversation != null && conversation.getRentalRequestId() != null) {
            Optional<RentalRequestDTO> existing = rentalRequestService.getRentalRequestById(tenantId, conversation.getRentalRequestId());
            if (existing.isPresent()) {
                RentalRequestDTO req = existing.get();
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("rentalRequestId", req.getId());
                res.put("requestNumber", req.getRequestNumber());
                res.put("status", req.getStatus().name());
                res.put("estimatedTotal", req.getEstimatedTotal());
                res.put("message", "Your rental request (" + req.getRequestNumber() + ") has already been submitted for review. Our sales team will follow up shortly.");
                res.put("action", "EXISTING");
                return ToolCallResultDTO.success(getName(), res, false);
            }
        }

        String idempotencyKey = args.get("idempotencyKey") != null
            ? args.get("idempotencyKey").toString().trim()
            : (convId != null ? "req_" + convId : null);

        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            Optional<RentalRequestDTO> existingByKey = rentalRequestService.getRentalRequestByIdempotencyKey(tenantId, idempotencyKey);
            if (existingByKey.isPresent()) {
                RentalRequestDTO req = existingByKey.get();
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("rentalRequestId", req.getId());
                res.put("requestNumber", req.getRequestNumber());
                res.put("status", req.getStatus().name());
                res.put("estimatedTotal", req.getEstimatedTotal());
                res.put("message", "Your rental request (" + req.getRequestNumber() + ") has already been submitted.");
                res.put("action", "EXISTING");
                return ToolCallResultDTO.success(getName(), res, false);
            }
        }

        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setTenantId(tenantId);
        dto.setIdempotencyKey(idempotencyKey);
        dto.setConversationId(convId);
        dto.setStatus(RentalRequestStatus.SUBMITTED);

        if (conversation != null) {
            dto.setLeadId(conversation.getLeadId());
            dto.setCustomerId(conversation.getCustomerId());
        }

        dto.setCustomerName(args.get("customerName") != null ? args.get("customerName").toString().trim() : "Valued Prospect");
        dto.setCustomerEmail(args.get("customerEmail") != null ? args.get("customerEmail").toString().trim() : "inquiry@client.com");
        dto.setCustomerPhone(args.get("customerPhone") != null ? args.get("customerPhone").toString().trim() : null);
        dto.setEventName(args.get("eventName") != null ? args.get("eventName").toString() : "Rental Event");
        dto.setEventType(args.get("eventType") != null ? args.get("eventType").toString() : "WEDDING");

        if (args.get("eventDate") != null) {
            try { dto.setEventDate(LocalDate.parse(args.get("eventDate").toString().trim().substring(0, 10))); } catch (Exception ignored) {}
        }

        dto.setRentalStartDate(parseDateTime(args.get("startDate"), true));
        dto.setRentalEndDate(parseDateTime(args.get("endDate"), false));
        dto.setDeliveryAddress(args.get("deliveryAddress") != null ? args.get("deliveryAddress").toString() : "Event Venue");
        dto.setDeliveryCity(args.get("deliveryCity") != null ? args.get("deliveryCity").toString() : "Boston");
        dto.setDeliveryRequired(args.get("deliveryRequired") == null || Boolean.parseBoolean(args.get("deliveryRequired").toString()));
        dto.setGuestCount(args.get("guestCount") != null ? Integer.parseInt(args.get("guestCount").toString()) : 100);

        if (args.get("estimatedTotal") != null) {
            dto.setEstimatedTotal(new BigDecimal(args.get("estimatedTotal").toString()));
        }

        dto.setNotes(args.get("notes") != null ? args.get("notes").toString() : "Submitted via AI Sales Agent");

        // Parse items
        Object itemsObj = args.get("items");
        if (itemsObj instanceof List<?> rawList) {
            for (Object itemObj : rawList) {
                if (itemObj instanceof Map<?, ?> itemMap) {
                    String pIdStr = (String) itemMap.get("productId");
                    String pName = (String) itemMap.get("productName");
                    String sku = (String) itemMap.get("sku");
                    int qty = itemMap.get("quantity") != null ? Integer.parseInt(itemMap.get("quantity").toString()) : 1;
                    BigDecimal price = itemMap.get("unitPrice") != null ? new BigDecimal(itemMap.get("unitPrice").toString()) : BigDecimal.ZERO;
                    UUID pId = null;
                    try { if (pIdStr != null) pId = UUID.fromString(pIdStr); } catch (Exception ignored) {}

                    dto.getItems().add(new RentalRequestDTO.ItemDTO(pId, pName != null ? pName : "Rental Item", sku, qty, price, price.multiply(BigDecimal.valueOf(qty))));
                }
            }
        }

        RentalRequestDTO created = rentalRequestService.createRentalRequest(tenantId, dto);

        // Update conversation linkage
        if (conversation != null) {
            conversation.setRentalRequestId(created.getId());
            conversationRepository.save(conversation);
        }

        // Log CRM activity if lead is linked
        if (dto.getLeadId() != null) {
            activityService.logActivity(tenantId, dto.getLeadId(), ActivityType.AI_RENTAL_REQUEST_CREATED,
                    ActivityDirection.INTERNAL, "Rental Request Submitted (" + created.getRequestNumber() + ")",
                    "Customer confirmed rental request submission through AI Sales Agent. Awaiting sales review.",
                    null, null, "RENTAL_REQUEST", created.getId().toString(), "AI_SALES_AGENT");
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("rentalRequestId", created.getId());
        res.put("requestNumber", created.getRequestNumber());
        res.put("status", created.getStatus().name());
        res.put("estimatedTotal", created.getEstimatedTotal());
        res.put("message", "Your rental request (" + created.getRequestNumber() + ") has been submitted for review. Our sales team will verify logistics and follow up with a proposal.");
        res.put("action", "CREATED");

        return ToolCallResultDTO.success(getName(), res, false);
    }

    private LocalDateTime parseDateTime(Object val, boolean isStart) {
        if (val == null) {
            LocalDate base = isStart ? LocalDate.now().plusDays(7) : LocalDate.now().plusDays(9);
            return isStart ? base.atTime(9, 0) : base.atTime(18, 0);
        }
        String str = val.toString().trim();
        if (str.length() == 10) {
            LocalDate ld = LocalDate.parse(str);
            return isStart ? ld.atTime(9, 0) : ld.atTime(18, 0);
        }
        return LocalDateTime.parse(str);
    }
}
