package com.rentflow.ai.repository;

import com.rentflow.ai.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByTenantId(String tenantId);
    Optional<Customer> findByTenantIdAndId(String tenantId, UUID id);
    Optional<Customer> findFirstByTenantIdAndEmailIgnoreCase(String tenantId, String email);
    Optional<Customer> findByTenantIdAndCustomerNumberIgnoreCase(String tenantId, String customerNumber);
    long countByTenantId(String tenantId);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Customer> searchCustomers(@Param("tenantId") String tenantId, @Param("query") String query);
}
