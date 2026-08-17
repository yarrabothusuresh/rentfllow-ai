package com.rentflow.ai.tool;

import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SuggestQuoteItemsTool implements AITool {

    private final ProductRepository productRepository;

    public SuggestQuoteItemsTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "suggestQuoteItems";
    }

    @Override
    public String getDescription() {
        return "Suggests relevant rental products based on event type or guest count.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();

        try {
            List<Product> products = productRepository.findByTenantId(tenantId);
            return ToolResult.ok(Map.of(
                    "suggestedProducts", products,
                    "recommendation", "Recommended package: Chiavari Chairs, 60\" Round Tables, Linens, and Wireless LED Uplights."
            ), "Suggested package items for event.");
        } catch (Exception e) {
            return ToolResult.error("Failed to suggest quote items: " + e.getMessage());
        }
    }
}
