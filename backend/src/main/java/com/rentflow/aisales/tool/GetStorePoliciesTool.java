package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component("aiSalesGetStorePoliciesTool")
public class GetStorePoliciesTool implements AiSalesTool {

    @Override
    public String getName() {
        return "getStorePolicies";
    }

    @Override
    public String getDescription() {
        return "Retrieve customer-facing rental policies, turnaround buffers, deposit guidelines, and delivery options.";
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
        Map<String, Object> policies = new LinkedHashMap<>();
        policies.put("standardDeliveryHours", "8:00 AM - 6:00 PM Monday through Saturday");
        policies.put("minimumRentalDurationDays", 1);
        policies.put("securityDepositRate", "25% refundable security deposit required upon formal contract signing");
        policies.put("cancellationGracePeriod", "Full refund if cancelled at least 14 days before event");
        policies.put("damageWaiverOptional", "Optional 8% equipment protection waiver available");
        policies.put("serviceArea", "Greater Metropolitan & Suburbs (up to 50 miles radius)");
        policies.put("humanReviewNotice", "All AI-prepared requests and draft quotes require human sales specialist verification.");

        return ToolCallResultDTO.success(getName(), policies, true);
    }
}
