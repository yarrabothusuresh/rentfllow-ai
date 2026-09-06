package com.rentflow.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentflow.automation.detector.DetectedSignal;
import com.rentflow.automation.model.BusinessSignal;
import com.rentflow.automation.model.BusinessSignalStatus;
import com.rentflow.automation.repository.BusinessSignalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BusinessSignalService {

    private static final Logger log = LoggerFactory.getLogger(BusinessSignalService.class);

    @Autowired
    private BusinessSignalRepository businessSignalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public BusinessSignal recordOrUpdateSignal(String tenantId, DetectedSignal detected) {
        String dedupeKey = detected.dedupeKey();
        Optional<BusinessSignal> existingOpt = businessSignalRepository.findByTenantIdAndDedupeKey(tenantId, dedupeKey);

        String evidenceJson = null;
        try {
            evidenceJson = objectMapper.writeValueAsString(detected.evidence());
        } catch (Exception e) {
            log.warn("Failed to serialize evidence for signal {}: {}", dedupeKey, e.getMessage());
            evidenceJson = "{}";
        }

        if (existingOpt.isPresent()) {
            BusinessSignal existing = existingOpt.get();
            if (existing.getStatus() == BusinessSignalStatus.ACTIVE) {
                existing.setLastDetectedAt(LocalDateTime.now());
                existing.setEvidenceJson(evidenceJson);
                existing.setSeverity(detected.severity());
                return businessSignalRepository.save(existing);
            }
        }

        BusinessSignal signal = new BusinessSignal();
        signal.setTenantId(tenantId);
        signal.setSignalType(detected.signalType());
        signal.setCategory(detected.category());
        signal.setSourceEntityType(detected.sourceEntityType());
        signal.setSourceEntityId(detected.sourceEntityId());
        signal.setSourceEntityNumber(detected.sourceEntityNumber());
        signal.setSeverity(detected.severity());
        signal.setStatus(BusinessSignalStatus.ACTIVE);
        signal.setDedupeKey(dedupeKey);
        signal.setEvidenceJson(evidenceJson);
        signal.setDetectedAt(LocalDateTime.now());
        signal.setLastDetectedAt(LocalDateTime.now());

        return businessSignalRepository.save(signal);
    }

    @Transactional
    public void resolveSignal(BusinessSignal signal) {
        if (signal != null && signal.getStatus() == BusinessSignalStatus.ACTIVE) {
            signal.setStatus(BusinessSignalStatus.RESOLVED);
            signal.setResolvedAt(LocalDateTime.now());
            businessSignalRepository.save(signal);
        }
    }
}
