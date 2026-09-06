package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.WarehouseExceptionRepository;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class WarehouseSignalDetector implements BusinessSignalDetector {

    @Autowired
    private WarehouseOrderRepository warehouseOrderRepository;

    @Autowired
    private WarehouseExceptionRepository warehouseExceptionRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.WAREHOUSE_SHORTAGE_DETECTED,
            BusinessSignalType.WAREHOUSE_ORDER_UNASSIGNED,
            BusinessSignalType.WAREHOUSE_STAGING_BLOCKED
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. WAREHOUSE_SHORTAGE_DETECTED
        List<WarehouseException> exceptions = warehouseExceptionRepository.findByTenantId(tenantId);
        for (WarehouseException exc : exceptions) {
            if (exc.getStatus() == WarehouseExceptionStatus.OPEN) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("exceptionId", exc.getId().toString());
                evidence.put("warehouseOrderId", exc.getWarehouseOrderId() != null ? exc.getWarehouseOrderId().toString() : "");
                evidence.put("productName", exc.getProductNameSnapshot());
                evidence.put("quantity", exc.getQuantity());
                evidence.put("severity", exc.getSeverity() != null ? exc.getSeverity().name() : "");
                evidence.put("type", exc.getType() != null ? exc.getType().name() : "");
                evidence.put("description", exc.getDescription());

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("taskTitle", "Resolve Warehouse " + exc.getType() + ": " + exc.getProductNameSnapshot() + " (qty: " + exc.getQuantity() + ")");
                payload.put("entityType", "WAREHOUSE_EXCEPTION");
                payload.put("entityId", exc.getId().toString());
                payload.put("priority", exc.getSeverity() == WarehouseExceptionSeverity.BLOCKING ? "CRITICAL" : "HIGH");
                payload.put("assignedRole", "ROLE_WAREHOUSE");

                signals.add(new DetectedSignal(
                    BusinessSignalType.WAREHOUSE_SHORTAGE_DETECTED,
                    BusinessSignalCategory.WAREHOUSE,
                    "WAREHOUSE_EXCEPTION",
                    exc.getId().toString(),
                    exc.getProductNameSnapshot(),
                    exc.getSeverity() == WarehouseExceptionSeverity.BLOCKING ? BusinessSignalSeverity.CRITICAL : BusinessSignalSeverity.HIGH,
                    tenantId + ":WAREHOUSE_SHORTAGE_DETECTED:" + exc.getId(),
                    evidence,
                    AutomationActionType.CREATE_INTERNAL_TASK,
                    payload,
                    "Warehouse shortage: " + exc.getProductNameSnapshot() + " (qty " + exc.getQuantity() + ")",
                    "A blocking warehouse shortage was flagged on order " + exc.getWarehouseOrderId() + " for product " + exc.getProductNameSnapshot() + ".",
                    "Unresolved shortages cause order fulfillment failure, last-minute cancellations, and customer dissatisfaction."
                ));
            }
        }

        // 2. WAREHOUSE_ORDER_UNASSIGNED
        List<WarehouseOrder> orders = warehouseOrderRepository.findByTenantId(tenantId);
        for (WarehouseOrder wo : orders) {
            if (wo.getStatus() == WarehouseOrderStatus.HANDED_TO_DRIVER || wo.getStatus() == WarehouseOrderStatus.CANCELLED) {
                continue;
            }

            boolean isUnassigned = (wo.getAssignedTo() == null || wo.getAssignedTo().isBlank());
            boolean isUpcoming = wo.getScheduledDate() != null && wo.getScheduledDate().isBefore(now.plusDays(3));

            if (isUnassigned && isUpcoming) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("orderNumber", wo.getOrderNumber());
                evidence.put("scheduledDate", wo.getScheduledDate() != null ? wo.getScheduledDate().toString() : "");
                evidence.put("priority", wo.getPriority().name());
                evidence.put("status", wo.getStatus().name());

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("warehouseOrderId", wo.getId().toString());
                payload.put("orderNumber", wo.getOrderNumber());
                payload.put("operatorName", "Lead Warehouse Specialist");

                signals.add(new DetectedSignal(
                    BusinessSignalType.WAREHOUSE_ORDER_UNASSIGNED,
                    BusinessSignalCategory.WAREHOUSE,
                    "WAREHOUSE_ORDER",
                    wo.getId().toString(),
                    wo.getOrderNumber(),
                    BusinessSignalSeverity.MEDIUM,
                    tenantId + ":WAREHOUSE_ORDER_UNASSIGNED:" + wo.getId(),
                    evidence,
                    AutomationActionType.ASSIGN_WAREHOUSE_OPERATOR,
                    payload,
                    "Warehouse Order " + wo.getOrderNumber() + " is unassigned",
                    "Warehouse order " + wo.getOrderNumber() + " scheduled for " + wo.getScheduledDate() + " has no assigned warehouse fulfillment technician.",
                    "Unassigned warehouse orders risk missing fulfillment pick/pack deadlines before transit."
                ));
            }
        }

        return signals;
    }
}
