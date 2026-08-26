package com.rentflow.returns.repository;

import com.rentflow.returns.model.ReturnOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReturnOrderItemRepository extends JpaRepository<ReturnOrderItem, UUID> {

    List<ReturnOrderItem> findByTenantIdAndReturnOrderId(String tenantId, UUID returnOrderId);

    Optional<ReturnOrderItem> findByTenantIdAndId(String tenantId, UUID id);
}
