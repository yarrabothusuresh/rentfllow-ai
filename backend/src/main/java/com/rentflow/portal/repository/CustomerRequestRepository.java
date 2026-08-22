package com.rentflow.portal.repository;

import com.rentflow.portal.model.CustomerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, UUID> {
    List<CustomerRequest> findByTenantIdAndCustomerIdOrderByCreatedAtDesc(String tenantId, UUID customerId);
}
