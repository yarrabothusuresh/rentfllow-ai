package com.rentflow.automation.repository;

import com.rentflow.automation.model.AiRecommendation;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.RecommendationPriority;
import com.rentflow.automation.model.RecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {
    Optional<AiRecommendation> findByTenantIdAndRecommendationNumber(String tenantId, String recommendationNumber);
    Optional<AiRecommendation> findByIdAndTenantId(UUID id, String tenantId);
    List<AiRecommendation> findByTenantIdAndStatus(String tenantId, RecommendationStatus status);
    List<AiRecommendation> findByTenantIdAndSignalId(String tenantId, UUID signalId);
    Optional<AiRecommendation> findFirstByTenantIdAndSignalIdAndStatusNotIn(String tenantId, UUID signalId, List<RecommendationStatus> statuses);

    @Query("SELECT r FROM AiRecommendation r WHERE r.tenantId = :tenantId " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:priority IS NULL OR r.priority = :priority) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "ORDER BY r.createdAt DESC")
    Page<AiRecommendation> findFiltered(
        @Param("tenantId") String tenantId,
        @Param("status") RecommendationStatus status,
        @Param("priority") RecommendationPriority priority,
        @Param("category") BusinessSignalCategory category,
        Pageable pageable
    );

    long countByTenantIdAndStatus(String tenantId, RecommendationStatus status);
    long countByTenantIdAndPriorityAndStatus(String tenantId, RecommendationPriority priority, RecommendationStatus status);
    long countByTenantId(String tenantId);
}
