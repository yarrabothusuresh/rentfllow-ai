package com.rentflow.phone.provider;

public class CallHandoffResult {
    private boolean successful;
    private String targetDestination;
    private String message;

    public CallHandoffResult() {}

    public CallHandoffResult(boolean successful, String targetDestination, String message) {
        this.successful = successful;
        this.targetDestination = targetDestination;
        this.message = message;
    }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public String getTargetDestination() { return targetDestination; }
    public void setTargetDestination(String targetDestination) { this.targetDestination = targetDestination; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
