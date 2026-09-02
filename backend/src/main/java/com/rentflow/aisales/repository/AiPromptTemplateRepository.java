package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiPromptTemplate;
import com.rentflow.aisales.model.AiPromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, UUID> {
    List<AiPromptTemplate> findByTypeAndActiveTrue(AiPromptType type);
    Optional<AiPromptTemplate> findFirstByTypeAndActiveTrueOrderByVersionDesc(AiPromptType type);
    List<AiPromptTemplate> findAllByOrderByCreatedAtDesc();
}
