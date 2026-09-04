package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component("aiSalesGetStorefrontSettingsTool")
public class GetStorefrontSettingsTool implements AiSalesTool {

    @Override
    public String getName() {
        return "getStorefrontSettings";
    }

    @Override
    public String getDescription() {
        return "Retrieve general storefront settings such as business name, operating currency, and standard sales tax rate.";
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
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("storeName", "ABC Event Rentals");
        settings.put("currency", "USD");
        settings.put("timeZone", "America/New_York");
        settings.put("standardTaxRate", 8.25);
        settings.put("defaultDeliveryFee", 150.00);
        settings.put("aiAssistantName", "RentFlow Sales Assistant");

        return ToolCallResultDTO.success(getName(), settings, true);
    }
}
