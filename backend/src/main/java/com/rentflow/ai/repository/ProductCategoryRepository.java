package com.rentflow.ai.repository;

import com.rentflow.ai.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByTenantId(String tenantId);
    Optional<ProductCategory> findByTenantIdAndId(String tenantId, UUID id);
    List<ProductCategory> findByTenantIdAndParentCategoryId(String tenantId, UUID parentCategoryId);
}
