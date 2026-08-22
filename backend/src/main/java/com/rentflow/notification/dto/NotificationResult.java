package com.rentflow.notification.dto;

import com.rentflow.notification.model.NotificationStatus;

public class NotificationResult {
    private boolean success;
    private NotificationStatus status;
    private String failureReason;
    private String externalId;

    public NotificationResult() {}

    public NotificationResult(boolean success, NotificationStatus status, String failureReason, String externalId) {
        this.success = success;
        this.status = status;
        this.failureReason = failureReason;
        this.externalId = externalId;
    }

    public static NotificationResult success(NotificationStatus status, String externalId) {
        return new NotificationResult(true, status, null, externalId);
    }

    public static NotificationResult failure(String failureReason) {
        return new NotificationResult(false, NotificationStatus.FAILED, failureReason, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
}
