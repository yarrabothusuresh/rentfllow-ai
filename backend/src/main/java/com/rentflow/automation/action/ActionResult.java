package com.rentflow.automation.action;

import java.util.Collections;
import java.util.Map;

public record ActionResult(
    boolean success,
    String summary,
    String errorDetails,
    Map<String, Object> outputData
) {
    public static ActionResult success(String summary) {
        return new ActionResult(true, summary, null, Collections.emptyMap());
    }

    public static ActionResult success(String summary, Map<String, Object> outputData) {
        return new ActionResult(true, summary, null, outputData != null ? outputData : Collections.emptyMap());
    }

    public static ActionResult failure(String summary, String errorDetails) {
        return new ActionResult(false, summary, errorDetails, Collections.emptyMap());
    }
}
