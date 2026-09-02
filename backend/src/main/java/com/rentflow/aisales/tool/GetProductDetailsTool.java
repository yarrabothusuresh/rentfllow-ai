package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component("aiGetProductDetailsTool")
public class GetProductDetailsTool implements AiSalesTool {

    private final ProductRepository productRepository;

    public GetProductDetailsTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "getProductDetails";
    }

    @Override
    public String getDescription() {
        return "Retrieve customer-safe details, specifications, and rental pricing for a specific product ID.";
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
    public java.util.Set<String> getAllowedRoles() {
        return java.util.Set.of("CUSTOMER", "SALES", "ADMIN", "OWNER", "STAFF");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        Map<String, Object> args = request.getArguments();
        String productIdStr = args.get("productId") != null ? args.get("productId").toString() : null;

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Missing required argument 'productId'", false);
        }

        UUID productId;
        try {
            productId = UUID.fromString(productIdStr);
        } catch (IllegalArgumentException e) {
            return ToolCallResultDTO.failure(getName(), "Invalid UUID format for productId: " + productIdStr, false);
        }

        Optional<Product> prodOpt = productRepository.findByTenantIdAndId(tenantId, productId);
        if (prodOpt.isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Product not found with ID: " + productId, false);
        }

        Product p = prodOpt.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("sku", p.getSku());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("rentalPrice", p.getRentalPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("totalQuantity", p.getQuantityOwned());

        return ToolCallResultDTO.success(getName(), map, false);
    }
}
