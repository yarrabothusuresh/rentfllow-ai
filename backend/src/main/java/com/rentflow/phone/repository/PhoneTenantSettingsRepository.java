package com.rentflow.phone.repository;

import com.rentflow.phone.model.PhoneTenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhoneTenantSettingsRepository extends JpaRepository<PhoneTenantSettings, UUID> {

    Optional<PhoneTenantSettings> findByTenantId(String tenantId);
}
