package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component("aiSalesCheckBulkAvailabilityTool")
public class CheckBulkAvailabilityTool implements AiSalesTool {

    private final AvailabilityService availabilityService;

    public CheckBulkAvailabilityTool(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Override
    public String getName() {
        return "checkBulkAvailability";
    }

    @Override
    public String getDescription() {
        return "Check availability simultaneously for multiple rental products and quantities.";
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

        Object itemsObj = args.get("items");
        if (!(itemsObj instanceof List<?> rawList) || rawList.isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Missing or invalid 'items' list", false);
        }

        LocalDateTime startDt = parseDateTime(args.get("startDate"), true);
        LocalDateTime endDt = parseDateTime(args.get("endDate"), false);

        boolean allAvailable = true;
        List<Map<String, Object>> itemResults = new ArrayList<>();

        for (Object itemObj : rawList) {
            if (itemObj instanceof Map<?, ?> itemMap) {
                String pIdStr = (String) itemMap.get("productId");
                int qty = itemMap.get("quantity") != null ? Integer.parseInt(itemMap.get("quantity").toString()) : 1;

                if (pIdStr != null) {
                    try {
                        UUID pId = UUID.fromString(pIdStr);
                        AvailabilityResultDTO res = availabilityService.checkAvailability(tenantId, pId, qty, startDt, endDt);

                        if (!res.isAvailable()) {
                            allAvailable = false;
                        }

                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("productId", pId);
                        r.put("productName", res.getProductName());
                        r.put("requested", qty);
                        r.put("availableQuantity", res.getAvailableQuantity());
                        r.put("available", res.isAvailable());
                        itemResults.add(r);
                    } catch (Exception ignored) {
                        allAvailable = false;
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("allAvailable", allAvailable);
        result.put("items", itemResults);
        result.put("startDate", startDt.toLocalDate());
        result.put("endDate", endDt.toLocalDate());

        return ToolCallResultDTO.success(getName(), result, false);
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
