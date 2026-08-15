package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ProductDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetLowStockProductsTool implements AITool {

    private final ProductService productService;

    public GetLowStockProductsTool(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public String getName() {
        return "getLowStockProducts";
    }

    @Override
    public String getDescription() {
        return "Retrieve list of products with low availability or high utilization";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        String role = request.getUserRole();

        List<ProductDTO> products = productService.getProducts(tenantId, role);
        List<ProductDTO> lowStock = products.stream()
                .filter(p -> "CRITICAL".equalsIgnoreCase(p.getHealth()) || "WARNING".equalsIgnoreCase(p.getHealth()))
                .collect(Collectors.toList());

        if (lowStock.isEmpty()) {
            lowStock = products.stream().limit(3).collect(Collectors.toList());
        }

        List<Map<String, Object>> listData = lowStock.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("sku", p.getSku());
            m.put("quantityOwned", p.getQuantityOwned());
            m.put("availableQuantity", p.getAvailableQuantity());
            m.put("health", p.getHealth());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = Map.of("lowStockProducts", listData, "count", listData.size());
        return ToolResult.ok(data, "Found " + listData.size() + " products with low stock / warning health.");
    }
}
