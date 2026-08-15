package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetProductTool implements AITool {

    private final ProductService productService;

    public GetProductTool(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public String getName() {
        return "getProduct";
    }

    @Override
    public String getDescription() {
        return "Retrieve specific product specifications and inventory metrics by ID or SKU";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String role = request.getUserRole();
        Map<String, Object> params = request.getParams() != null ? request.getParams() : Map.of();

        String query = (String) params.getOrDefault("query", "Chiavari");
        List<ProductDTO> products = productService.searchProducts(tenantId, query, role);

        if (!products.isEmpty()) {
            ProductDTO p = products.get(0);
            Map<String, Object> data = new HashMap<>();
            data.put("id", p.getId());
            data.put("name", p.getName());
            data.put("sku", p.getSku());
            data.put("rentalPrice", p.getRentalPrice());
            data.put("replacementCost", p.getReplacementCost());
            data.put("quantityOwned", p.getQuantityOwned());
            data.put("quantityInMaintenance", p.getQuantityInMaintenance());
            data.put("quantityDamaged", p.getQuantityDamaged());
            data.put("quantityLost", p.getQuantityLost());
            data.put("availableQuantity", p.getAvailableQuantity());
            data.put("health", p.getHealth());

            return ToolResult.ok(data, "Product details for " + p.getName());
        }

        return ToolResult.error("Product not found: " + query);
    }
}
