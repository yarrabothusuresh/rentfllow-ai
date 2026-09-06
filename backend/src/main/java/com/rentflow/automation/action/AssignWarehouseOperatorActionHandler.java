package com.rentflow.automation.action;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AssignWarehouseOperatorActionHandler implements AutomationActionHandler {

    @Autowired
    private WarehouseOrderRepository warehouseOrderRepository;

    @Override
    public AutomationActionType getActionType() {
        return AutomationActionType.ASSIGN_WAREHOUSE_OPERATOR;
    }

    @Override
    public RevalidationResult revalidate(String tenantId, Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("warehouseOrderId")) {
            return RevalidationResult.stale("Missing warehouseOrderId in action payload");
        }
        UUID orderId;
        try {
            orderId = UUID.fromString(payload.get("warehouseOrderId").toString());
        } catch (Exception e) {
            return RevalidationResult.stale("Invalid warehouseOrderId: " + payload.get("warehouseOrderId"));
        }

        WarehouseOrder order = warehouseOrderRepository.findById(orderId).orElse(null);
        if (order == null || !tenantId.equals(order.getTenantId())) {
            return RevalidationResult.stale("Warehouse order not found or belongs to another tenant");
        }

        if (order.getStatus() == WarehouseOrderStatus.HANDED_TO_DRIVER || order.getStatus() == WarehouseOrderStatus.CANCELLED) {
            return RevalidationResult.stale("Warehouse order " + order.getOrderNumber() + " is already " + order.getStatus());
        }

        if (order.getAssignedTo() != null && !order.getAssignedTo().isBlank()) {
            return RevalidationResult.alreadyCompleted("Warehouse operator " + order.getAssignedTo() + " is already assigned to " + order.getOrderNumber());
        }

        return RevalidationResult.valid();
    }

    @Override
    public ActionResult execute(String tenantId, Map<String, Object> payload, String executedBy) {
        UUID orderId = UUID.fromString(payload.get("warehouseOrderId").toString());
        WarehouseOrder order = warehouseOrderRepository.findById(orderId).orElseThrow();
        String operator = (String) payload.getOrDefault("operatorName", "Warehouse Specialist");

        order.setAssignedTo(operator);
        if (order.getStatus() == WarehouseOrderStatus.PENDING) {
            order.setStatus(WarehouseOrderStatus.READY_TO_PICK);
        }
        warehouseOrderRepository.save(order);

        return ActionResult.success(
            "Assigned operator '" + operator + "' to Warehouse Order " + order.getOrderNumber(),
            Map.of("orderNumber", order.getOrderNumber(), "assignedTo", operator)
        );
    }
}
