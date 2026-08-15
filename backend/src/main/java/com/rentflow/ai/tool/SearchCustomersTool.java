package com.rentflow.ai.tool;

import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.CustomerService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class SearchCustomersTool implements AITool {

    private final CustomerService customerService;

    public SearchCustomersTool(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public String getName() {
        return "searchCustomers";
    }

    @Override
    public String getDescription() {
        return "Search customers by name, email, phone, company, or customer number";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String query = request.getParams() != null ? (String) request.getParams().get("query") : "";
        List<CustomerDTO> customers = customerService.searchCustomers(request.getTenantId(), query);

        if (!customers.isEmpty()) {
            return ToolResult.ok(customers.get(0), "Found customer matching: " + customers.get(0).getFirstName() + " " + customers.get(0).getLastName());
        }
        return ToolResult.error("No customer found matching: " + query);
    }
}
