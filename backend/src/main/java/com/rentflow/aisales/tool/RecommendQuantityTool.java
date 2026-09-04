package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.service.RentalQuantityRecommendationService;
import com.rentflow.aisales.service.RentalQuantityRecommendationService.RecommendationResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("aiSalesRecommendQuantityTool")
public class RecommendQuantityTool implements AiSalesTool {

    private final RentalQuantityRecommendationService recommendationService;
    private final ProductRepository productRepository;

    public RecommendQuantityTool(RentalQuantityRecommendationService recommendationService, ProductRepository productRepository) {
        this.recommendationService = recommendationService;
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "recommendQuantity";
    }

    @Override
    public String getDescription() {
        return "Calculate authoritative, deterministic recommended quantities for event rentals based on guest count and product capacity rules.";
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
        String productIdStr = (String) args.get("productId");
        int guestCount = args.get("guestCount") != null ? Integer.parseInt(args.get("guestCount").toString()) : 100;
        String eventType = args.get("eventType") != null ? args.get("eventType").toString() : "EVENT";

        Product product = null;
        if (productIdStr != null && !productIdStr.trim().isEmpty()) {
            try {
                UUID pId = UUID.fromString(productIdStr.trim());
                product = productRepository.findByTenantIdAndId(tenantId, pId).orElse(null);
            } catch (Exception ignored) {}
        }

        if (product == null && args.get("productName") != null) {
            String name = args.get("productName").toString();
            product = new Product();
            product.setName(name);
        }

        RecommendationResult rec = recommendationService.recommendQuantity(product, guestCount, eventType);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("recommendedQuantity", rec.getRecommendedQuantity());
        res.put("explanation", rec.getExplanation());
        res.put("configuredUnitCapacity", rec.getConfiguredUnitCapacity());
        res.put("guestCount", guestCount);
        res.put("eventType", eventType);

        return ToolCallResultDTO.success(getName(), res, true);
    }
}
