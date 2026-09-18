package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.aisales.model.MarginStatus;
import com.rentflow.common.financial.FinancialMath;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component("aiSalesCheckInternalProfitabilityTool")
public class CheckInternalProfitabilityTool implements AiSalesTool {

    private static final BigDecimal WEAR_RATE = new BigDecimal("0.15");
    private static final BigDecimal HANDLING_PER_ITEM = new BigDecimal("2.00");
    private static final BigDecimal DISPATCH_REVENUE = new BigDecimal("150.00");
    private static final BigDecimal DISPATCH_COST = new BigDecimal("80.00");
    private static final BigDecimal TARGET_MARGIN = new BigDecimal("30.00");
    private static final BigDecimal LOW_MARGIN_THRESHOLD = new BigDecimal("20.00");

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
        return Set.of("SALES", "ADMIN", "OWNER", "FINANCE");
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
                                BigDecimal unitPrice = FinancialMath.scaleCurrency(p.getRentalPrice());
                                BigDecimal lineRev = FinancialMath.multiply(unitPrice, qty);
                                totalRevenue = totalRevenue.add(lineRev);

                                // Estimate item cost: wear/depreciation (15% of rental price) + turnaround handling ($2/item)
                                BigDecimal wearCost = lineRev.multiply(WEAR_RATE).setScale(FinancialMath.CURRENCY_SCALE, RoundingMode.HALF_UP);
                                BigDecimal handlingCost = HANDLING_PER_ITEM.multiply(BigDecimal.valueOf(qty)).setScale(FinancialMath.CURRENCY_SCALE, RoundingMode.HALF_UP);
                                totalCost = totalCost.add(wearCost).add(handlingCost);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        // Add standard dispatch labor delivery cost ($80.00) and delivery revenue ($150.00)
        boolean delivery = args.get("deliveryRequired") == null || Boolean.parseBoolean(args.get("deliveryRequired").toString());
        if (delivery) {
            totalRevenue = totalRevenue.add(DISPATCH_REVENUE);
            totalCost = totalCost.add(DISPATCH_COST);
        }

        totalRevenue = FinancialMath.scaleCurrency(totalRevenue);
        totalCost = FinancialMath.scaleCurrency(totalCost);
        BigDecimal profit = FinancialMath.calculateProfit(totalRevenue, totalCost);
        BigDecimal marginPct = FinancialMath.calculateMargin(totalRevenue, totalCost);

        MarginStatus status = MarginStatus.HEALTHY;
        List<String> warnings = new ArrayList<>();

        if (marginPct.compareTo(BigDecimal.ZERO) < 0 || profit.compareTo(BigDecimal.ZERO) < 0) {
            status = MarginStatus.LOSS_MAKING;
            warnings.add("Quote is operating at a net financial loss! Review line item pricing and delivery distance.");
        } else if (marginPct.compareTo(LOW_MARGIN_THRESHOLD) < 0) {
            status = MarginStatus.LOW_MARGIN;
            warnings.add("Low margin warning: " + marginPct + "% is below the tenant's " + TARGET_MARGIN + "% target margin.");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estimatedRevenue", totalRevenue);
        result.put("estimatedCost", totalCost);
        result.put("estimatedProfit", profit);
        result.put("estimatedMarginPct", marginPct);
        result.put("targetMarginPct", TARGET_MARGIN);
        result.put("marginStatus", status);
        result.put("warnings", warnings);

        return ToolCallResultDTO.success(getName(), result, true);
    }
}
