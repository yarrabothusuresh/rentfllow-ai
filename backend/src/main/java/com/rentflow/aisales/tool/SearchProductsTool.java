package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("aiSearchProductsTool")
public class SearchProductsTool implements AiSalesTool {

    private final ProductRepository productRepository;

    public SearchProductsTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "searchProducts";
    }

    @Override
    public String getDescription() {
        return "Search available rental products by keyword. Returns customer-safe catalog items.";
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
        String query = args.get("query") != null ? args.get("query").toString().trim().toLowerCase() : "";
        int limit = args.get("limit") != null ? Integer.parseInt(args.get("limit").toString()) : 10;

        List<Product> products = productRepository.findByTenantId(tenantId);

        List<Map<String, Object>> sanitized = products.stream()
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .filter(p -> query.isEmpty() ||
                         (p.getName() != null && p.getName().toLowerCase().contains(query)) ||
                         (p.getSku() != null && p.getSku().toLowerCase().contains(query)) ||
                         (p.getDescription() != null && p.getDescription().toLowerCase().contains(query)))
            .limit(limit)
            .map(this::sanitizeProduct)
            .collect(Collectors.toList());

        return ToolCallResultDTO.success(getName(), sanitized, false);
    }

    private Map<String, Object> sanitizeProduct(Product p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("sku", p.getSku());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("rentalPrice", p.getRentalPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("totalQuantity", p.getQuantityOwned());

        // Dynamic guest seating capacity heuristic based on product name/description
        int capacity = 1;
        String lowerName = (p.getName() + " " + (p.getDescription() != null ? p.getDescription() : "")).toLowerCase();
        if (lowerName.contains("60") && lowerName.contains("round")) {
            capacity = 10;
        } else if (lowerName.contains("72") && lowerName.contains("round")) {
            capacity = 12;
        } else if (lowerName.contains("8ft") || lowerName.contains("8-foot") || lowerName.contains("banquet")) {
            capacity = 8;
        } else if (lowerName.contains("6ft") || lowerName.contains("6-foot")) {
            capacity = 6;
        }
        map.put("guestCapacity", capacity);

        return map;
    }
}
