package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetDeliveryRisksTool implements AiSalesTool {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;

    public GetDeliveryRisksTool(DeliveryRepository deliveryRepository,
                               WarehouseOrderRepository warehouseOrderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
    }

    @Override
    public String getName() {
        return "getDeliveryRisks";
    }

    @Override
    public String getDescription() {
        return "Analyzes scheduled dispatches and flags delivery risks (missing drivers, unassigned fleet vehicles, incomplete warehouse prep).";
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
        return Set.of("OWNER", "ADMIN", "OPERATIONS");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        int daysAhead = 1;
        if (request.getParameters() != null && request.getParameters().containsKey("daysAhead")) {
            try {
                daysAhead = Integer.parseInt(request.getParameters().get("daysAhead").toString());
            } catch (Exception ignored) {}
        }

        LocalDate targetDate = LocalDate.now().plusDays(daysAhead);

        try {
            List<Delivery> scheduled = deliveryRepository.findByTenantIdAndScheduledDate(tenantId, targetDate);
            List<Map<String, Object>> riskDeliveries = new ArrayList<>();

            for (Delivery d : scheduled) {
                if (d.getStatus() == DeliveryStatus.CANCELLED || d.getStatus() == DeliveryStatus.DELIVERED) continue;

                List<String> riskReasons = new ArrayList<>();
                if (d.getDriverId() == null) {
                    riskReasons.add("Driver unassigned");
                }
                if (d.getVehicleId() == null) {
                    riskReasons.add("Vehicle unassigned");
                }

                if (d.getWarehouseOrderId() != null) {
                    WarehouseOrder wo = warehouseOrderRepository.findByTenantIdAndId(tenantId, d.getWarehouseOrderId()).orElse(null);
                    if (wo != null && wo.getStatus() != WarehouseOrderStatus.PACKED &&
                            wo.getStatus() != WarehouseOrderStatus.READY_FOR_DELIVERY &&
                            wo.getStatus() != WarehouseOrderStatus.HANDED_TO_DRIVER) {
                        riskReasons.add("Warehouse staging incomplete (Status: " + wo.getStatus() + ")");
                    }
                }

                if (!riskReasons.isEmpty()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("deliveryId", d.getId().toString());
                    map.put("deliveryNumber", d.getDeliveryNumber());
                    map.put("scheduledDate", d.getScheduledDate().toString());
                    map.put("status", d.getStatus().name());
                    map.put("risks", riskReasons);
                    riskDeliveries.add(map);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("targetDate", targetDate.toString());
            result.put("totalScheduledDeliveries", scheduled.size());
            result.put("atRiskDeliveriesCount", riskDeliveries.size());
            result.put("atRiskDeliveries", riskDeliveries);

            return ToolCallResultDTO.success(getName(), result, true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to analyze delivery risks: " + e.getMessage(), true);
        }
    }
}
