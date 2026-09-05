package com.rentflow.aisales.tool;

import com.rentflow.aisales.dto.ToolCallRequestDTO;
import com.rentflow.aisales.dto.ToolCallResultDTO;
import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadFollowUp;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import com.rentflow.crm.repository.LeadRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GetSalesFollowUpsDueTool implements AiSalesTool {

    private final LeadFollowUpRepository followUpRepository;
    private final LeadRepository leadRepository;

    public GetSalesFollowUpsDueTool(LeadFollowUpRepository followUpRepository, LeadRepository leadRepository) {
        this.followUpRepository = followUpRepository;
        this.leadRepository = leadRepository;
    }

    @Override
    public String getName() {
        return "getSalesFollowUpsDue";
    }

    @Override
    public String getDescription() {
        return "Lists sales follow-up tasks that are overdue or scheduled for today, with lead details.";
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
        return Set.of("OWNER", "ADMIN", "SALES");
    }

    @Override
    public ToolCallResultDTO execute(String tenantId, String userRole, ToolCallRequestDTO request) {
        try {
            List<LeadFollowUp> openFollowUps = followUpRepository.findByTenantIdAndStatus(tenantId, FollowUpStatus.OPEN);
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();

            List<Map<String, Object>> overdueList = new ArrayList<>();
            List<Map<String, Object>> dueTodayList = new ArrayList<>();

            for (LeadFollowUp f : openFollowUps) {
                if (f.getDueAt() == null) continue;

                Lead lead = leadRepository.findByTenantIdAndId(tenantId, f.getLeadId()).orElse(null);
                String leadContact = lead != null ? lead.getContactName() : "Unknown";
                String company = lead != null ? lead.getCompanyName() : "";
                String leadNumber = lead != null ? lead.getLeadNumber() : "";

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("followUpId", f.getId().toString());
                map.put("leadId", f.getLeadId().toString());
                map.put("leadNumber", leadNumber);
                map.put("contactName", leadContact);
                map.put("companyName", company);
                map.put("title", f.getTitle());
                map.put("type", f.getType().name());
                map.put("dueAt", f.getDueAt().toString());
                map.put("assignedTo", f.getAssignedToName() != null ? f.getAssignedToName() : f.getAssignedTo());

                if (f.getDueAt().isBefore(now)) {
                    overdueList.add(map);
                } else if (f.getDueAt().toLocalDate().isEqual(today)) {
                    dueTodayList.add(map);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalOverdueCount", overdueList.size());
            result.put("dueTodayCount", dueTodayList.size());
            result.put("overdueFollowUps", overdueList);
            result.put("dueTodayFollowUps", dueTodayList);

            return ToolCallResultDTO.success(getName(), result, true);
        } catch (Exception e) {
            return ToolCallResultDTO.failure(getName(), "Failed to fetch sales follow-ups: " + e.getMessage(), true);
        }
    }
}
