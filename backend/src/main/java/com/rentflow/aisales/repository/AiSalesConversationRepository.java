package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiConversationStatus;
import com.rentflow.aisales.model.AiSalesChannel;
import com.rentflow.aisales.model.AiSalesConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiSalesConversationRepository extends JpaRepository<AiSalesConversation, UUID> {
    List<AiSalesConversation> findByTenantIdOrderByLastMessageAtDesc(String tenantId);
    Optional<AiSalesConversation> findByTenantIdAndId(String tenantId, UUID id);
    Optional<AiSalesConversation> findByTenantIdAndPublicId(String tenantId, String publicId);
    List<AiSalesConversation> findByTenantIdAndStatusOrderByLastMessageAtDesc(String tenantId, AiConversationStatus status);
    List<AiSalesConversation> findByTenantIdAndChannelOrderByLastMessageAtDesc(String tenantId, AiSalesChannel channel);
    List<AiSalesConversation> findByTenantIdAndCustomerIdOrderByLastMessageAtDesc(String tenantId, UUID customerId);
    long countByTenantId(String tenantId);
    long countByTenantIdAndStatus(String tenantId, AiConversationStatus status);
    long countByTenantIdAndCreatedAtAfter(String tenantId, LocalDateTime after);
}
