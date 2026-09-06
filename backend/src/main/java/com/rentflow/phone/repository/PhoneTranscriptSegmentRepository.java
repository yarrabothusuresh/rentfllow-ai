package com.rentflow.phone.repository;

import com.rentflow.phone.model.PhoneTranscriptSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PhoneTranscriptSegmentRepository extends JpaRepository<PhoneTranscriptSegment, UUID> {

    List<PhoneTranscriptSegment> findByTenantIdAndCallSessionIdOrderByTimestampAsc(String tenantId, UUID callSessionId);

    void deleteByTenantIdAndCreatedAtBefore(String tenantId, Instant cutoff);
}
