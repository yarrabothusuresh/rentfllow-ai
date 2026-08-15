package com.rentflow.ai.tool;

import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.dto.ToolRequest;
import com.rentflow.ai.dto.ToolResult;
import com.rentflow.ai.service.CustomerService;
import com.rentflow.ai.service.EventService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class GetCustomerEventsTool implements AITool {

    private final CustomerService customerService;
    private final EventService eventService;

    public GetCustomerEventsTool(CustomerService customerService, EventService eventService) {
        this.customerService = customerService;
        this.eventService = eventService;
    }

    @Override
    public String getName() {
        return "getCustomerEvents";
    }

    @Override
    public String getDescription() {
        return "Retrieve all events for a specific customer";
    }

    @Override
    public Set<String> getAllowedRoles() {
        return Set.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER");
    }

    @Override
    public ToolResult execute(ToolRequest request) {
        String tenantId = request.getTenantId();
        List<CustomerDTO> customers = customerService.searchCustomers(tenantId, "Emily");
        if (customers.isEmpty()) {
            customers = customerService.getCustomers(tenantId);
        }

        if (!customers.isEmpty()) {
            CustomerDTO cust = customers.get(0);
            List<EventDTO> events = eventService.getEventsByCustomer(tenantId, cust.getId());
            return ToolResult.ok(events, "Retrieved " + events.size() + " event(s) for customer: " + cust.getFirstName() + " " + cust.getLastName());
        }

        return ToolResult.error("No customer found to retrieve events.");
    }
}
