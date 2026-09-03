package com.rentflow.crm.service;

import com.rentflow.crm.dto.*;
import com.rentflow.crm.model.FollowUpStatus;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadFollowUp;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.repository.LeadFollowUpRepository;
import com.rentflow.crm.repository.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CrmDashboardService {

    private final LeadRepository leadRepository;
    private final LeadFollowUpRepository followUpRepository;
    private final LeadService leadService;

    public CrmDashboardService(LeadRepository leadRepository,
                               LeadFollowUpRepository followUpRepository,
                               LeadService leadService) {
        this.leadRepository = leadRepository;
        this.followUpRepository = followUpRepository;
        this.leadService = leadService;
    }

    public CrmDashboardDTO getDashboard(String tenantId, String currentUserId) {
        CrmDashboardDTO dto = new CrmDashboardDTO();

        dto.setNewLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.NEW));
        dto.setUnassignedLeads(leadRepository.countByTenantIdAndAssignedSalesUserIdIsNull(tenantId));
        dto.setMyActiveLeads(currentUserId != null ?
                leadRepository.countByTenantIdAndAssignedSalesUserIdAndStageNotIn(tenantId, currentUserId, List.of(LeadStage.WON, LeadStage.LOST, LeadStage.DISQUALIFIED)) : 0);
        dto.setContactedLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.CONTACTED));
        dto.setQualifiedLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.QUALIFIED));
        dto.setQuotePreparedLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.QUOTE_PREPARED));
        dto.setQuoteSentLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.QUOTE_SENT));
        dto.setFollowUpLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.FOLLOW_UP) +
                leadRepository.countByTenantIdAndStage(tenantId, LeadStage.NEGOTIATION));
        dto.setWonLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.WON));
        dto.setLostLeads(leadRepository.countByTenantIdAndStage(tenantId, LeadStage.LOST));

        long activeTotal = leadRepository.findByTenantId(tenantId).stream()
                .filter(l -> l.getStage() != LeadStage.WON && l.getStage() != LeadStage.LOST && l.getStage() != LeadStage.DISQUALIFIED)
                .count();
        dto.setTotalActiveLeads(activeTotal);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        dto.setOverdueFollowUps(followUpRepository.countByTenantIdAndStatusAndDueAtBefore(tenantId, FollowUpStatus.OPEN, now));
        dto.setFollowUpsDueToday(followUpRepository.countByTenantIdAndStatusAndDueAtBetween(tenantId, FollowUpStatus.OPEN, todayStart, todayEnd));

        // Unassigned queue
        List<LeadSummaryDTO> unassigned = leadRepository.findByTenantId(tenantId).stream()
                .filter(l -> l.getAssignedSalesUserId() == null && l.getStage() != LeadStage.LOST && l.getStage() != LeadStage.DISQUALIFIED)
                .limit(10)
                .map(leadService::toSummaryDTO)
                .collect(Collectors.toList());
        dto.setUnassignedQueue(unassigned);

        // Priority follow-ups
        List<LeadFollowUp> openFollowUps = followUpRepository.findByTenantIdAndStatus(tenantId, FollowUpStatus.OPEN);
        Map<UUID, Lead> leadMap = leadRepository.findByTenantId(tenantId).stream()
                .collect(Collectors.toMap(Lead::getId, l -> l, (a, b) -> a));

        List<LeadFollowUpDTO> priorityFollowUps = openFollowUps.stream()
                .sorted(Comparator.comparing(LeadFollowUp::getDueAt))
                .limit(10)
                .map(f -> {
                    Lead l = leadMap.get(f.getLeadId());
                    LeadFollowUpDTO fuDto = new LeadFollowUpDTO();
                    fuDto.setId(f.getId());
                    fuDto.setLeadId(f.getLeadId());
                    fuDto.setLeadNumber(l != null ? l.getLeadNumber() : null);
                    fuDto.setContactName(l != null ? l.getContactName() : null);
                    fuDto.setAssignedTo(f.getAssignedTo());
                    fuDto.setAssignedToName(f.getAssignedToName());
                    fuDto.setType(f.getType());
                    fuDto.setTitle(f.getTitle());
                    fuDto.setNotes(f.getNotes());
                    fuDto.setDueAt(f.getDueAt());
                    fuDto.setStatus(f.getStatus());
                    fuDto.setOverdue(f.isOverdue());
                    fuDto.setCreatedAt(f.getCreatedAt());
                    return fuDto;
                })
                .collect(Collectors.toList());
        dto.setPriorityFollowUps(priorityFollowUps);

        // Recent leads
        List<LeadSummaryDTO> recent = leadRepository.findByTenantId(tenantId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(leadService::toSummaryDTO)
                .collect(Collectors.toList());
        dto.setRecentLeads(recent);

        return dto;
    }

    public CrmPipelineDTO getPipeline(String tenantId) {
        List<Lead> allLeads = leadRepository.findByTenantId(tenantId);

        Map<LeadStage, List<Lead>> grouped = allLeads.stream()
                .collect(Collectors.groupingBy(Lead::getStage));

        List<LeadStage> pipelineStages = List.of(
                LeadStage.NEW,
                LeadStage.CONTACTED,
                LeadStage.NEEDS_DISCOVERY,
                LeadStage.QUALIFIED,
                LeadStage.QUOTE_PREPARED,
                LeadStage.QUOTE_SENT,
                LeadStage.FOLLOW_UP,
                LeadStage.NEGOTIATION,
                LeadStage.WON,
                LeadStage.LOST
        );

        List<CrmPipelineDTO.CrmPipelineColumnDTO> columns = new ArrayList<>();
        BigDecimal totalPipelineVal = BigDecimal.ZERO;
        long totalCount = allLeads.size();

        for (LeadStage st : pipelineStages) {
            List<Lead> stageLeads = grouped.getOrDefault(st, Collections.emptyList());
            BigDecimal colVal = stageLeads.stream()
                    .map(l -> l.getEstimatedValue() != null ? l.getEstimatedValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (st != LeadStage.LOST && st != LeadStage.DISQUALIFIED) {
                totalPipelineVal = totalPipelineVal.add(colVal);
            }

            List<LeadSummaryDTO> dtos = stageLeads.stream()
                    .map(leadService::toSummaryDTO)
                    .collect(Collectors.toList());

            columns.add(new CrmPipelineDTO.CrmPipelineColumnDTO(
                    st,
                    formatStageName(st),
                    stageLeads.size(),
                    colVal,
                    dtos
            ));
        }

        return new CrmPipelineDTO(columns, totalCount, totalPipelineVal);
    }

    private String formatStageName(LeadStage stage) {
        switch (stage) {
            case NEW: return "New Inquiry";
            case CONTACTED: return "Contacted";
            case NEEDS_DISCOVERY: return "Discovery";
            case QUALIFIED: return "Qualified";
            case QUOTE_PREPARED: return "Quote Prepared";
            case QUOTE_SENT: return "Quote Sent";
            case FOLLOW_UP: return "Follow-Up";
            case NEGOTIATION: return "Negotiation";
            case WON: return "Won / Booked";
            case LOST: return "Closed Lost";
            case DISQUALIFIED: return "Disqualified";
            default: return stage.name();
        }
    }
}
