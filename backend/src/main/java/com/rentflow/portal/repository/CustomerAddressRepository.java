package com.rentflow.portal.repository;

import com.rentflow.portal.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
    Optional<CustomerAddress> findByTenantIdAndCustomerIdAndId(String tenantId, UUID customerId, UUID id);
    Optional<CustomerAddress> findByTenantIdAndCustomerIdAndIsDefaultTrue(String tenantId, UUID customerId);
}
