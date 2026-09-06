package com.rentflow.phone.provider;

import com.rentflow.phone.model.CallStatus;

public class CallTurnResult {
    private String responseSpeech;
    private CallStatus callStatus;
    private boolean handoffRequested;
    private String handoffReason;
    private boolean inquiryUpdated;

    public CallTurnResult() {}

    public CallTurnResult(String responseSpeech, CallStatus callStatus, boolean handoffRequested, String handoffReason, boolean inquiryUpdated) {
        this.responseSpeech = responseSpeech;
        this.callStatus = callStatus;
        this.handoffRequested = handoffRequested;
        this.handoffReason = handoffReason;
        this.inquiryUpdated = inquiryUpdated;
    }

    public static CallTurnResult speech(String text) {
        return new CallTurnResult(text, CallStatus.AI_ACTIVE, false, null, false);
    }

    public static CallTurnResult handoff(String text, String reason) {
        return new CallTurnResult(text, CallStatus.WAITING_FOR_HUMAN, true, reason, false);
    }

    public String getResponseSpeech() { return responseSpeech; }
    public void setResponseSpeech(String responseSpeech) { this.responseSpeech = responseSpeech; }

    public CallStatus getCallStatus() { return callStatus; }
    public void setCallStatus(CallStatus callStatus) { this.callStatus = callStatus; }

    public boolean isHandoffRequested() { return handoffRequested; }
    public void setHandoffRequested(boolean handoffRequested) { this.handoffRequested = handoffRequested; }

    public String getHandoffReason() { return handoffReason; }
    public void setHandoffReason(String handoffReason) { this.handoffReason = handoffReason; }

    public boolean isInquiryUpdated() { return inquiryUpdated; }
    public void setInquiryUpdated(boolean inquiryUpdated) { this.inquiryUpdated = inquiryUpdated; }
}
