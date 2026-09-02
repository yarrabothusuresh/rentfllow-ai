package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiSalesMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiSalesMessageRepository extends JpaRepository<AiSalesMessage, UUID> {
    List<AiSalesMessage> findByTenantIdAndConversationIdOrderByCreatedAtAsc(String tenantId, UUID conversationId);
    long countByTenantIdAndConversationId(String tenantId, UUID conversationId);
}
