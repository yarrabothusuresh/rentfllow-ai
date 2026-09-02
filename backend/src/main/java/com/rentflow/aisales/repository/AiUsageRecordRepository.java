package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AiUsageRecordRepository extends JpaRepository<AiUsageRecord, UUID> {
    List<AiUsageRecord> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<AiUsageRecord> findByTenantIdAndCreatedAtAfter(String tenantId, LocalDateTime after);
    long countByTenantIdAndCreatedAtAfter(String tenantId, LocalDateTime after);

    @Query("SELECT COALESCE(SUM(u.inputTokens + u.outputTokens), 0) FROM AiUsageRecord u WHERE u.tenantId = :tenantId AND u.createdAt >= :after")
    long sumTokensByTenantIdAndCreatedAtAfter(@Param("tenantId") String tenantId, @Param("after") LocalDateTime after);
}
