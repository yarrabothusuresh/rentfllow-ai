package com.rentflow.ai.tool;

import com.rentflow.ai.dto.QuoteCalculationRequest;
import com.rentflow.ai.dto.QuoteCalculationResponse;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.QuoteCalculationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
public class CalculateQuoteTool implements AITool {

    private final QuoteCalculationService calculationService;

    public CalculateQuoteTool() {
        this.calculationService = new QuoteCalculationService();
    }

    public CalculateQuoteTool(QuoteCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Override
    public String getName() {
        return "calculateQuote";
    }

    @Override
    public String getDescription() {
        return "Calculates subtotal, discounts, fees, tax, total, and deposit for rental quote line items.";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        Map<String, Object> parameters = request.getParams() != null ? request.getParams() : Map.of();

        try {
            QuoteCalculationRequest req = new QuoteCalculationRequest();
            if (parameters.containsKey("taxRate")) {
                req.setTaxRate(new BigDecimal(parameters.get("taxRate").toString()));
            }
            if (parameters.containsKey("depositPercentage")) {
                req.setDepositPercentage(new BigDecimal(parameters.get("depositPercentage").toString()));
            }

            QuoteCalculationResponse resp = calculationService.calculate(req);
            return ToolResult.ok(resp, "Calculated quote total: $" + resp.getTotalAmount() + " (Deposit: $" + resp.getDepositAmount() + ")");
        } catch (Exception e) {
            return ToolResult.error("Failed to calculate quote: " + e.getMessage());
        }
    }
}
