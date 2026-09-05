package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.CopilotActionProposal;
import com.rentflow.aisales.model.CopilotActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CopilotActionProposalRepository extends JpaRepository<CopilotActionProposal, UUID> {

    Optional<CopilotActionProposal> findByTenantIdAndId(String tenantId, UUID id);

    List<CopilotActionProposal> findByTenantIdAndConversationIdOrderByCreatedAtDesc(String tenantId, UUID conversationId);

    List<CopilotActionProposal> findByTenantIdAndStatus(String tenantId, CopilotActionStatus status);

    List<CopilotActionProposal> findByTenantIdAndStatusAndExpiresAtBefore(String tenantId, CopilotActionStatus status, LocalDateTime now);
}
