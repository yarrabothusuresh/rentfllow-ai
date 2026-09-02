package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiFeedbackRepository extends JpaRepository<AiFeedback, UUID> {
    List<AiFeedback> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<AiFeedback> findByTenantIdAndConversationId(String tenantId, UUID conversationId);
}
