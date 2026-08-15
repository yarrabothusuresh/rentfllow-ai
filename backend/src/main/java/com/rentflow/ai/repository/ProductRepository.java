package com.rentflow.ai.repository;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByTenantId(String tenantId);
    Optional<Product> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Product> findByTenantIdAndSkuIgnoreCase(String tenantId, String sku);
    List<Product> findByTenantIdAndStatus(String tenantId, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("tenantId") String tenantId, @Param("query") String query);
}
