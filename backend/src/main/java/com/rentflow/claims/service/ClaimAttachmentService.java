package com.rentflow.claims.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public interface ClaimAttachmentService {
    default List<String> getClaimPhotos(String tenantId, UUID claimId) {
        return Collections.emptyList();
    }

    default List<String> getRepairPhotos(String tenantId, UUID repairId) {
        return Collections.emptyList();
    }
}
