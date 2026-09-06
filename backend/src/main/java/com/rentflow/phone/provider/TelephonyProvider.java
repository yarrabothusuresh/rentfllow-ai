package com.rentflow.phone.provider;

import com.rentflow.phone.model.CallHandoffReason;
import com.rentflow.phone.model.PhoneCallSession;

import java.util.UUID;

/**
 * Provider-neutral telephony adapter interface.
 * Abstracts telephony providers like Twilio, Vonage, Telnyx, or Mock.
 */
public interface TelephonyProvider {

    String getProviderId();

    boolean validateWebhookSignature(String signature, String timestamp, String payload);

    PhoneCallSession initializeInboundCall(String tenantId, String providerCallId, String callerNumber);

    CallTurnResult processCallerUtterance(PhoneCallSession session, String utterance);

    CallHandoffResult transferCall(PhoneCallSession session, String queueOrNumber, CallHandoffReason reason);

    void endCall(PhoneCallSession session);
}
