package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.MarginStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component("aiSalesCheckInternalProfitabilityTool")
public class CheckInternalProfitabilityTool implements AiSalesTool {

    private final ProductRepository productRepository;

    public CheckInternalProfitabilityTool(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "checkInternalProfitability";
    }

    @Override
    public String getDescription() {
        return "INTERNAL ONLY: Calculate internal cost structure, depreciation/wear margin, and target margin health. Never visible to customer.";
    }

    @Override
    public boolean isCustomerVisible() {
        return false;
    }

    @Override
    public boolean isMutating() {
        return false;
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("SALES", "ADMIN", "OWNER");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        if ("CUSTOMER".equalsIgnoreCase(userRole)) {
            return ToolCallResultDTO.failure(getName(), "Permission denied: customer context cannot access internal profitability data", true);
        }

        Map<String, Object> args = request.getArguments();
        Object itemsObj = args.get("items");

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        if (itemsObj instanceof List<?> rawList) {
            for (Object itemObj : rawList) {
                if (itemObj instanceof Map<?, ?> itemMap) {
                    String pIdStr = (String) itemMap.get("productId");
                    int qty = itemMap.get("quantity") != null ? Integer.parseInt(itemMap.get("quantity").toString()) : 1;

                    if (pIdStr != null) {
                        try {
                            UUID pId = UUID.fromString(pIdStr);
                            Optional<Product> prodOpt = productRepository.findByTenantIdAndId(tenantId, pId);
                            if (prodOpt.isPresent()) {
                                Product p = prodOpt.get();
                                BigDecimal lineRev = p.getRentalPrice().multiply(BigDecimal.valueOf(qty));
                                totalRevenue = totalRevenue.add(lineRev);

                                // Estimate item cost: wear/depreciation (e.g. 15% of rental price) + turnaround handling ($2/item)
                                BigDecimal wearCost = lineRev.multiply(BigDecimal.valueOf(0.15));
                                BigDecimal handlingCost = BigDecimal.valueOf(2.00).multiply(BigDecimal.valueOf(qty));
                                totalCost = totalCost.add(wearCost).add(handlingCost);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        // Add standard dispatch labor delivery cost ($80.00)
        boolean delivery = args.get("deliveryRequired") == null || Boolean.parseBoolean(args.get("deliveryRequired").toString());
        if (delivery) {
            totalRevenue = totalRevenue.add(BigDecimal.valueOf(150.00));
            totalCost = totalCost.add(BigDecimal.valueOf(80.00));
        }

        BigDecimal profit = totalRevenue.subtract(totalCost);
        double marginPct = 0.0;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            marginPct = profit.divide(totalRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        double targetMargin = 30.0;
        MarginStatus status = MarginStatus.HEALTHY;
        List<String> warnings = new ArrayList<>();

        if (marginPct < 0) {
            status = MarginStatus.LOSS_MAKING;
            warnings.add("Quote is operating at a net financial loss! Review line item pricing and delivery distance.");
        } else if (marginPct < 20.0) {
            status = MarginStatus.LOW_MARGIN;
            warnings.add("Low margin warning: " + String.format("%.1f", marginPct) + "% is below the tenant's 30.0% target margin.");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estimatedRevenue", totalRevenue.setScale(2, RoundingMode.HALF_UP));
        result.put("estimatedCost", totalCost.setScale(2, RoundingMode.HALF_UP));
        result.put("estimatedProfit", profit.setScale(2, RoundingMode.HALF_UP));
        result.put("estimatedMarginPct", Math.round(marginPct * 10.0) / 10.0);
        result.put("targetMarginPct", targetMargin);
        result.put("marginStatus", status);
        result.put("warnings", warnings);

        return ToolCallResultDTO.success(getName(), result, true);
    }
}
