package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.warehouse.model.WarehouseException;
import com.rentflow.warehouse.model.WarehouseExceptionStatus;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseExceptionRepository;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetWarehouseBlockersTool implements AiSalesTool {

    private final WarehouseExceptionRepository exceptionRepository;
    private final WarehouseOrderRepository orderRepository;

    public GetWarehouseBlockersTool(WarehouseExceptionRepository exceptionRepository,
                                   WarehouseOrderRepository orderRepository) {
        this.exceptionRepository = exceptionRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public String getName() {
        return "getWarehouseBlockers";
    }

    @Override
    public String getDescription() {
        return "Inspects active warehouse fulfillment bottlenecks, including open shortages, damaged items, and unassigned pick orders.";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "OPERATIONS", "WAREHOUSE");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        try {
            List<WarehouseException> openExceptions = exceptionRepository.findByTenantIdAndStatus(tenantId, WarehouseExceptionStatus.OPEN);
            List<WarehouseOrder> orders = orderRepository.findByTenantId(tenantId);

            List<WarehouseOrder> unassignedOrders = orders.stream()
                    .filter(o -> o.getStatus() == WarehouseOrderStatus.READY_TO_PICK && (o.getAssignedTo() == null || o.getAssignedTo().isBlank()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> exceptionList = openExceptions.stream().map(ex -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("exceptionId", ex.getId().toString());
                map.put("type", ex.getType().name());
                map.put("severity", ex.getSeverity().name());
                map.put("description", ex.getDescription());
                map.put("productName", ex.getProductNameSnapshot());
                map.put("orderId", ex.getWarehouseOrderId() != null ? ex.getWarehouseOrderId().toString() : "N/A");
                return map;
            }).collect(Collectors.toList());

            List<Map<String, Object>> unassignedList = unassignedOrders.stream().map(o -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("orderId", o.getId().toString());
                map.put("orderNumber", o.getOrderNumber());
                map.put("priority", o.getPriority().name());
                map.put("bookingId", o.getBookingId() != null ? o.getBookingId().toString() : "N/A");
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalOpenExceptions", openExceptions.size());
            result.put("unassignedPickOrdersCount", unassignedOrders.size());
            result.put("exceptions", exceptionList);
            result.put("unassignedOrders", unassignedList);

            return ToolCallResultDTO.success(getName(), result, true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch warehouse blockers: " + e.getMessage(), true);
        }
    }
}
