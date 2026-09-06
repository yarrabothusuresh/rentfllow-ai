package com.rentflow.automation.detector;

import com.rentflow.automation.model.BusinessSignalType;
import java.util.List;

public interface BusinessSignalDetector {
    List<BusinessSignalType> getSupportedTypes();
    List<DetectedSignal> detect(String tenantId);
}
