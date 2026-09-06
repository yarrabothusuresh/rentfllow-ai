package com.rentflow.automation.service;

import com.rentflow.automation.dto.AutomationDashboardDTO;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationDashboardService {

    @Autowired
    private AiRecommendationRepository recommendationRepository;

    @Autowired
    private AutomationApprovalRepository approvalRepository;

    @Autowired
    private BusinessSignalRepository signalRepository;

    @Autowired
    private AutomationExecutionRepository executionRepository;

    public AutomationDashboardDTO getDashboard(String tenantId) {
        AutomationDashboardDTO dto = new AutomationDashboardDTO();

        long critical = recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.CRITICAL, RecommendationStatus.NEW)
                      + recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.CRITICAL, RecommendationStatus.REVIEWED);
        long high = recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.HIGH, RecommendationStatus.NEW)
                  + recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.HIGH, RecommendationStatus.REVIEWED);
        long med = recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.MEDIUM, RecommendationStatus.NEW)
                 + recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.MEDIUM, RecommendationStatus.REVIEWED);
        long low = recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.LOW, RecommendationStatus.NEW)
                 + recommendationRepository.countByTenantIdAndPriorityAndStatus(tenantId, RecommendationPriority.LOW, RecommendationStatus.REVIEWED);

        dto.setCriticalRecommendations(critical);
        dto.setHighRecommendations(high);
        dto.setMediumRecommendations(med);
        dto.setLowRecommendations(low);
        dto.setTotalActiveRecommendations(critical + high + med + low);

        dto.setPendingApprovals(approvalRepository.countByTenantIdAndStatus(tenantId, AutomationApprovalStatus.PENDING));
        dto.setTotalSignalsActive(signalRepository.countByTenantIdAndStatus(tenantId, BusinessSignalStatus.ACTIVE));
        dto.setTotalExecutions(executionRepository.countByTenantIdAndExecutionStatus(tenantId, AutomationExecutionStatus.EXECUTED));

        // Recent items
        List<AiRecommendation> recList = recommendationRepository.findFiltered(tenantId, null, null, null, PageRequest.of(0, 10)).getContent();
        dto.setRecentRecommendations(recList);

        List<AutomationApproval> pendingApprovals = approvalRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, AutomationApprovalStatus.PENDING, PageRequest.of(0, 10)).getContent();
        dto.setPendingApprovalsList(pendingApprovals);

        List<AutomationExecution> recentExecutions = executionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, 10)).getContent();
        dto.setRecentExecutions(recentExecutions);

        return dto;
    }
}
