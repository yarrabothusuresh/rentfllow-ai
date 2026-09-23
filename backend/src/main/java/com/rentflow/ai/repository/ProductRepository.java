package com.rentflow.ai.repository;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.model.ProductStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByTenantId(String tenantId);
    Page<Product> findByTenantId(String tenantId, Pageable pageable);
    Optional<Product> findByTenantIdAndId(String tenantId, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.id = :id")
    Optional<Product> findWithLockByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") UUID id);

    Optional<Product> findByTenantIdAndSkuIgnoreCase(String tenantId, String sku);
    List<Product> findByTenantIdAndStatus(String tenantId, ProductStatus status);
    Page<Product> findByTenantIdAndStatus(String tenantId, ProductStatus status, Pageable pageable);
    List<Product> findByTenantIdAndCategoryId(String tenantId, UUID categoryId);
    Page<Product> findByTenantIdAndCategoryId(String tenantId, UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("tenantId") String tenantId, @Param("query") String query);

    @Query(value = "SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))",
           countQuery = "SELECT count(p) FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("tenantId") String tenantId, @Param("query") String query, Pageable pageable);
}
