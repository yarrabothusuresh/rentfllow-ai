package com.rentflow.portal.repository;

import com.rentflow.portal.model.CustomerActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerActivityRepository extends JpaRepository<CustomerActivity, UUID> {
    List<CustomerActivity> findByTenantIdAndCustomerIdOrderByCreatedAtDesc(String tenantId, UUID customerId);
}
