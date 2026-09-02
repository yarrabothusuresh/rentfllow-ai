package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component("aiSalesCheckAvailabilityTool")
public class CheckAvailabilityTool implements AiSalesTool {

    private final AvailabilityService availabilityService;

    public CheckAvailabilityTool(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Override
    public String getName() {
        return "checkAvailability";
    }

    @Override
    public String getDescription() {
        return "Check authoritative real-time inventory availability for a product quantity and date interval.";
    }

    @Override
    public boolean isCustomerVisible() {
        return true;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("CUSTOMER", "SALES", "ADMIN", "OWNER", "STAFF");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        Map<String, Object> args = request.getArguments();

        String productIdStr = args.get("productId") != null ? args.get("productId").toString() : null;
        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Missing required argument 'productId'", false);
        }

        UUID productId;
        try {
            productId = UUID.fromString(productIdStr);
        } catch (IllegalArgumentException e) {
            return ToolCallResultDTO.failure(getName(), "Invalid UUID format for productId", false);
        }

        int requestedQuantity = args.get("quantity") != null ? Integer.parseInt(args.get("quantity").toString()) : 1;

        LocalDateTime startDt = parseDateTime(args.get("startDate"), true);
        LocalDateTime endDt = parseDateTime(args.get("endDate"), false);

        try {
            AvailabilityResultDTO res = availabilityService.checkAvailability(tenantId, productId, requestedQuantity, startDt, endDt);

            String status = "UNAVAILABLE";
            if (res.isAvailable()) {
                status = "AVAILABLE";
            } else if (res.getAvailableQuantity() > 0) {
                status = "LIMITED";
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("productId", productId);
            result.put("productName", res.getProductName());
            result.put("requested", requestedQuantity);
            result.put("availableQuantity", res.getAvailableQuantity());
            result.put("status", status);
            result.put("startDate", startDt.toLocalDate());
            result.put("endDate", endDt.toLocalDate());

            return ToolCallResultDTO.success(getName(), result, false);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Error checking availability: " + e.getMessage(), false);
        }
    }

    private LocalDateTime parseDateTime(Object val, boolean isStart) {
        if (val == null) {
            LocalDate base = isStart ? LocalDate.now().plusDays(7) : LocalDate.now().plusDays(9);
            return isStart ? base.atTime(9, 0) : base.atTime(18, 0);
        }
        String str = val.toString().trim();
        if (str.length() == 10) { // YYYY-MM-DD
            LocalDate ld = LocalDate.parse(str);
            return isStart ? ld.atTime(9, 0) : ld.atTime(18, 0);
        }
        return LocalDateTime.parse(str);
    }
}
