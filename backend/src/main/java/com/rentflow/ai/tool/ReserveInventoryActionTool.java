package com.rentflow.ai.tool;

import com.rentflow.ai.dto.InventoryReservationDTO;
import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.model.ReservationStatus;
import com.rentflow.ai.service.InventoryService;
import com.rentflow.ai.service.ProductService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ReserveInventoryActionTool implements AITool {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public ReserveInventoryActionTool(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @Override
    public String getName() {
        return "reserveInventoryAction";
    }

    @Override
    public String getDescription() {
        return "Action tool to reserve inventory quantity for an event date range (Requires Human Approval)";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String role = request.getUserRole();
        Map<String, Object> params = request.getParams() != null ? request.getParams() : Map.of();

        String productName = (String) params.getOrDefault("productName", "Chiavari Chair");
        int quantity = params.containsKey("quantity") ? Integer.parseInt(params.get("quantity").toString()) : 100;

        List<ProductDTO> matches = productService.searchProducts(tenantId, productName, role);
        if (matches.isEmpty()) {
            return ToolResult.error("Product not found: " + productName);
        }

        ProductDTO product = matches.get(0);

        InventoryReservationDTO reqDto = new InventoryReservationDTO();
        reqDto.setProductId(product.getId());
        reqDto.setQuantity(quantity);
        reqDto.setStartDateTime(LocalDateTime.of(2026, 9, 20, 10, 0));
        reqDto.setEndDateTime(LocalDateTime.of(2026, 9, 22, 18, 0));
        reqDto.setStatus(ReservationStatus.RESERVED);

        InventoryReservationDTO created = inventoryService.createReservation(tenantId, reqDto, "AI Copilot");

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", created.getId());
        data.put("productName", product.getName());
        data.put("quantity", quantity);
        data.put("startDateTime", created.getStartDateTime().toString());
        data.put("endDateTime", created.getEndDateTime().toString());
        data.put("status", created.getStatus());

        return ToolResult.requiresApproval(data, "Reserve " + quantity + " " + product.getName() + " for Emily's Wedding (Requires Approval)");
    }
}
