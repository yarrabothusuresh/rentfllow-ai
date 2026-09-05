package com.rentflow.aisales.tool;

import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SearchCustomersTool implements AiSalesTool {

    private final CustomerRepository customerRepository;

    public SearchCustomersTool(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public String getName() {
        return "searchCustomers";
    }

    @Override
    public String getDescription() {
        return "Searches customers by name, company name, email, or phone. Returns matching candidates with contact details.";
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
        return Set.of("OWNER", "ADMIN", "SALES", "OPERATIONS", "FINANCE");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        String query = request.getStringParam("query", "").trim();
        if (query.isEmpty()) {
            return ToolCallResultDTO.failure(getName(), "Search query cannot be empty", true);
        }

        try {
            List<Customer> list = customerRepository.searchCustomers(tenantId, query);
            List<Map<String, Object>> mapped = list.stream().map(c -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("customerId", c.getId().toString());
                map.put("customerNumber", c.getCustomerNumber());
                map.put("name", (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim());
                map.put("companyName", c.getCompanyName());
                map.put("email", c.getEmail());
                map.put("phone", c.getPhone());
                map.put("type", c.getCustomerType() != null ? c.getCustomerType().name() : "INDIVIDUAL");
                return map;
            }).collect(Collectors.toList());

            return ToolCallResultDTO.success(getName(), Map.of(
                    "query", query,
                    "matchCount", mapped.size(),
                    "requiresDisambiguation", mapped.size() > 1,
                    "customers", mapped
            ), true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Customer search failed: " + e.getMessage(), true);
        }
    }
}
