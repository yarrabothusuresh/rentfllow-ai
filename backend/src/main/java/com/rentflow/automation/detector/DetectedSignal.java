package com.rentflow.automation.detector;

import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;

import java.util.Map;

public record DetectedSignal(
    BusinessSignalType signalType,
    BusinessSignalCategory category,
    String sourceEntityType,
    String sourceEntityId,
    String sourceEntityNumber,
    BusinessSignalSeverity severity,
    String dedupeKey,
    Map<String, Object> evidence,
    AutomationActionType suggestedActionType,
    Map<String, Object> suggestedActionPayload,
    String defaultTitle,
    String defaultSummary,
    String defaultWhyImportant
) {}
