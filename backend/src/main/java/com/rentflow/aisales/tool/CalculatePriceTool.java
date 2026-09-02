package com.rentflow.aisales.tool;

import com.rentflow.ai.dto.QuoteCalculationRequest;
import com.rentflow.ai.dto.QuoteCalculationResponse;
import com.rentflow.ai.dto.QuoteItemDTO;
import com.rentflow.ai.model.PricingStrategy;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.QuoteCalculationService;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component("aiSalesCalculatePriceTool")
public class CalculatePriceTool implements AiSalesTool {

    private final QuoteCalculationService calculationService;
    private final ProductRepository productRepository;

    public CalculatePriceTool(QuoteCalculationService calculationService, ProductRepository productRepository) {
        this.calculationService = calculationService;
        this.productRepository = productRepository;
    }

    @Override
    public String getName() {
        return "calculatePrice";
    }

    @Override
    public String getDescription() {
        return "Calculate authoritative rental price, delivery fees, and estimated taxes using backend pricing rules.";
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

        Object itemsObj = args.get("items");
        if (!(itemsObj instanceof List<?> rawList) || rawList.isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Missing or invalid 'items' list", false);
        }

        QuoteCalculationRequest calcReq = new QuoteCalculationRequest();
        int rentalDays = args.get("rentalDays") != null ? Integer.parseInt(args.get("rentalDays").toString()) : 1;

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
                            QuoteItemDTO itemDTO = new QuoteItemDTO();
                            itemDTO.setProductId(p.getId());
                            itemDTO.setDescription(p.getName());
                            itemDTO.setQuantity(qty);
                            itemDTO.setUnitPrice(p.getRentalPrice());
                            itemDTO.setPricingStrategy(PricingStrategy.PER_EVENT);
                            itemDTO.setRentalDays(rentalDays);
                            calcReq.getItems().add(itemDTO);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        boolean deliveryRequired = args.get("deliveryRequired") == null || Boolean.parseBoolean(args.get("deliveryRequired").toString());
        BigDecimal deliveryFee = deliveryRequired ? BigDecimal.valueOf(150.00) : BigDecimal.ZERO;
        calcReq.setDeliveryFee(deliveryFee);
        calcReq.setTaxRate(new BigDecimal("8.25"));

        QuoteCalculationResponse resp = calculationService.calculate(calcReq);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subtotal", resp.getSubtotal());
        result.put("discountAmount", resp.getDiscountAmount());
        result.put("deliveryFee", deliveryFee);
        result.put("taxAmount", resp.getTaxAmount());
        result.put("totalAmount", resp.getTotalAmount());
        result.put("lineItemsCount", calcReq.getItems().size());

        return ToolCallResultDTO.success(getName(), result, false);
    }
}
