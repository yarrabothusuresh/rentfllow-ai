package com.rentflow.portal.repository;

import com.rentflow.portal.model.TenantStorefrontConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantStorefrontConfigRepository extends JpaRepository<TenantStorefrontConfig, UUID> {
    Optional<TenantStorefrontConfig> findByTenantId(String tenantId);
    Optional<TenantStorefrontConfig> findByTenantSlug(String tenantSlug);
}
