package com.rentflow.warehouse.repository;

import com.rentflow.warehouse.model.PickListItem;
import com.rentflow.warehouse.model.PickListItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PickListItemRepository extends JpaRepository<PickListItem, UUID> {
    List<PickListItem> findByPickListId(UUID pickListId);
    List<PickListItem> findByPickListIdOrderBySequenceNumberAsc(UUID pickListId);
    List<PickListItem> findByTenantIdAndPickListId(String tenantId, UUID pickListId);
    Optional<PickListItem> findByTenantIdAndId(String tenantId, UUID id);
    Optional<PickListItem> findByPickListIdAndId(UUID pickListId, UUID id);
    List<PickListItem> findByTenantIdAndProductId(String tenantId, UUID productId);
    List<PickListItem> findByTenantIdAndStatus(String tenantId, PickListItemStatus status);
}
