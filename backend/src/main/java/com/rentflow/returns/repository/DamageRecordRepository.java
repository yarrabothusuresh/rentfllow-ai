package com.rentflow.returns.repository;

import com.rentflow.returns.model.DamageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DamageRecordRepository extends JpaRepository<DamageRecord, UUID> {

    List<DamageRecord> findByTenantId(String tenantId);

    List<DamageRecord> findByTenantIdAndInspectionId(String tenantId, UUID inspectionId);

    List<DamageRecord> findByTenantIdAndReturnItemId(String tenantId, UUID returnItemId);
}
