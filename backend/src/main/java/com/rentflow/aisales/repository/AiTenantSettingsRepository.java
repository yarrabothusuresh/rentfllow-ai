package com.rentflow.aisales.repository;

import com.rentflow.aisales.model.AiTenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiTenantSettingsRepository extends JpaRepository<AiTenantSettings, UUID> {
    Optional<AiTenantSettings> findByTenantId(String tenantId);
}
