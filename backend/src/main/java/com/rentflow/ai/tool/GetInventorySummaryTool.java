package com.rentflow.ai.tool;

import com.rentflow.ai.dto.InventorySummaryDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.InventoryService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetInventorySummaryTool implements AITool {

    private final InventoryService inventoryService;

    public GetInventorySummaryTool(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public String getName() {
        return "getInventorySummary";
    }

    @Override
    public String getDescription() {
        return "Retrieve company-wide inventory summary metrics (total units, reserved, maintenance, damaged, lost)";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        InventorySummaryDTO summary = inventoryService.getSummary(tenantId);

        Map<String, Object> data = new HashMap<>();
        data.put("totalProducts", summary.getTotalProducts());
        data.put("totalUnits", summary.getTotalUnits());
        data.put("availableUnits", summary.getAvailableUnits());
        data.put("reservedUnits", summary.getReservedUnits());
        data.put("maintenanceUnits", summary.getMaintenanceUnits());
        data.put("damagedUnits", summary.getDamagedUnits());
        data.put("lostUnits", summary.getLostUnits());
        data.put("lowStockProducts", summary.getLowStockProducts());

        return ToolResult.ok(data, "Inventory Summary: " + summary.getTotalUnits() + " total units across " + summary.getTotalProducts() + " products.");
    }
}
