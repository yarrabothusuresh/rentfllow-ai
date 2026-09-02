package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("aiSalesGetRentalPackagesTool")
public class GetRentalPackagesTool implements AiSalesTool {

    private final ProductRepository productRepository;

    public GetRentalPackagesTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "getRentalPackages";
    }

    @Override
    public String getDescription() {
        return "Retrieve standard curated rental packages for specific guest counts (e.g., 50, 100, 150 guests).";
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
        int guestCount = request.getArguments().get("guestCount") != null ?
            Integer.parseInt(request.getArguments().get("guestCount").toString()) : 100;

        List<Product> products = productRepository.findByTenantId(tenantId);
        Product tableProd = products.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains("round")).findFirst().orElse(null);
        Product chairProd = products.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains("chair")).findFirst().orElse(null);

        int tableQty = (int) Math.ceil((double) guestCount / 10.0);
        int chairQty = guestCount;

        List<Map<String, Object>> items = new ArrayList<>();
        if (tableProd != null) {
            items.add(Map.of("productId", tableProd.getId(), "name", tableProd.getName(), "quantity", tableQty, "unitPrice", tableProd.getRentalPrice()));
        }
        if (chairProd != null) {
            items.add(Map.of("productId", chairProd.getId(), "name", chairProd.getName(), "quantity", chairQty, "unitPrice", chairProd.getRentalPrice()));
        }

        Map<String, Object> pkg = new LinkedHashMap<>();
        pkg.put("packageName", guestCount + " Guest Essential Reception Package");
        pkg.put("guestCount", guestCount);
        pkg.put("description", "Complete seating and table arrangement for " + guestCount + " guests with 60-inch round tables and premium chairs.");
        pkg.put("items", items);

        return ToolCallResultDTO.success(getName(), List.of(pkg), false);
    }
}
