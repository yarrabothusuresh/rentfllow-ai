package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.LoadListItem;
import com.rentflow.warehouse.model.LoadListItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoadListItemRepository extends JpaRepository<LoadListItem, UUID> {
    List<LoadListItem> findByLoadListId(UUID loadListId);
    List<LoadListItem> findByTenantIdAndLoadListId(String tenantId, UUID loadListId);
    Optional<LoadListItem> findByTenantIdAndId(String tenantId, UUID id);
    Optional<LoadListItem> findByLoadListIdAndId(UUID loadListId, UUID id);
    List<LoadListItem> findByTenantIdAndContainerId(String tenantId, UUID containerId);
    List<LoadListItem> findByTenantIdAndStatus(String tenantId, LoadListItemStatus status);
}
