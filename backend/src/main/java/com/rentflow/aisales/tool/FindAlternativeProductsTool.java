package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.InventoryConflictDTO.AlternativeProductDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.InventoryAlternativeService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component("aiSalesFindAlternativeProductsTool")
public class FindAlternativeProductsTool implements AiSalesTool {

    private final InventoryAlternativeService alternativeService;
    private final ProductRepository productRepository;

    public FindAlternativeProductsTool(InventoryAlternativeService alternativeService, ProductRepository productRepository) {
        this.alternativeService = alternativeService;
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "findAlternativeProducts";
    }

    @Override
    public String getDescription() {
        return "Find real available alternative or complementary products from the catalog based on category, attributes, and date availability.";
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
        int requiredQuantity = args.get("requiredQuantity") != null ? Integer.parseInt(args.get("requiredQuantity").toString()) : 1;

        LocalDateTime start = parseDateTime(args.get("startDate"), true);
        LocalDateTime end = parseDateTime(args.get("endDate"), false);

        if (productIdStr != null && !productIdStr.trim().isEmpty()) {
            try {
                UUID pId = UUID.fromString(productIdStr.trim());
                List<AlternativeProductDTO> alts = alternativeService.findAlternatives(tenantId, pId, start, end, requiredQuantity);
                List<Map<String, Object>> safeAlts = new ArrayList<>();
                for (AlternativeProductDTO alt : alts) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("productId", alt.getProductId());
                    map.put("productName", alt.getProductName());
                    map.put("sku", alt.getSku());
                    map.put("availableQuantity", alt.getAvailableQuantity());
                    // Include customer-safe price if available
                    productRepository.findByTenantIdAndId(tenantId, alt.getProductId())
                        .ifPresent(p -> map.put("rentalPrice", p.getRentalPrice()));
                    safeAlts.add(map);
                }
                return ToolCallResultDTO.success(getName(), safeAlts, true);
            } catch (Exception ignored) {}
        }

        // Category/Query fallback if no specific product ID given
        String category = (String) args.get("category");
        List<Product> products = productRepository.findByTenantId(tenantId);
        List<Map<String, Object>> fallbackAlts = products.stream()
            .filter(p -> category == null || (p.getName() != null && p.getName().toLowerCase().contains(category.toLowerCase())))
            .limit(5)
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("productId", p.getId());
                m.put("productName", p.getName());
                m.put("sku", p.getSku());
                m.put("rentalPrice", p.getRentalPrice());
                return m;
            })
            .collect(Collectors.toList());

        return ToolCallResultDTO.success(getName(), fallbackAlts, true);
    }

    private LocalDateTime parseDateTime(Object val, boolean isStart) {
        if (val == null) {
            LocalDate base = isStart ? LocalDate.now().plusDays(7) : LocalDate.now().plusDays(9);
            return isStart ? base.atTime(9, 0) : base.atTime(18, 0);
        }
        String str = val.toString().trim();
        if (str.length() == 10) {
            LocalDate ld = LocalDate.parse(str);
            return isStart ? ld.atTime(9, 0) : ld.atTime(18, 0);
        }
        return LocalDateTime.parse(str);
    }
}
