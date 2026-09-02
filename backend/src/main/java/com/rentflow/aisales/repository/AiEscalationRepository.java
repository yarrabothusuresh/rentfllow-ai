package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiEscalation;
import com.rentflow.aisales.model.AiEscalationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiEscalationRepository extends JpaRepository<AiEscalation, UUID> {
    List<AiEscalation> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<AiEscalation> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, AiEscalationStatus status);
    Optional<AiEscalation> findByTenantIdAndId(String tenantId, UUID id);
    Optional<AiEscalation> findByTenantIdAndConversationId(String tenantId, UUID conversationId);
    long countByTenantIdAndStatus(String tenantId, AiEscalationStatus status);
}
