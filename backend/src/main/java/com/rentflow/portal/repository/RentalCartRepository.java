package com.rentflow.portal.repository;

import com.rentflow.portal.model.RentalCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalCartRepository extends JpaRepository<RentalCart, UUID> {
    Optional<RentalCart> findByTenantIdAndCartToken(String tenantId, String cartToken);
    Optional<RentalCart> findByTenantIdAndCustomerId(String tenantId, UUID customerId);
}
