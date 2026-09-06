package com.rentflow.automation.action;

public record RevalidationResult(
    boolean isValid,
    String reason,
    boolean alreadyCompleted
) {
    public static RevalidationResult valid() {
        return new RevalidationResult(true, null, false);
    }

    public static RevalidationResult stale(String reason) {
        return new RevalidationResult(false, reason, false);
    }

    public static RevalidationResult alreadyCompleted(String reason) {
        return new RevalidationResult(false, reason, true);
    }
}
