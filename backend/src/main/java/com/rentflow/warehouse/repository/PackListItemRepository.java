package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.PackListItem;
import com.rentflow.warehouse.model.PackListItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackListItemRepository extends JpaRepository<PackListItem, UUID> {
    List<PackListItem> findByPackListId(UUID packListId);
    List<PackListItem> findByTenantIdAndPackListId(String tenantId, UUID packListId);
    Optional<PackListItem> findByTenantIdAndId(String tenantId, UUID id);
    Optional<PackListItem> findByPackListIdAndId(UUID packListId, UUID id);
    List<PackListItem> findByTenantIdAndContainerId(String tenantId, UUID containerId);
    List<PackListItem> findByTenantIdAndStatus(String tenantId, PackListItemStatus status);
}
