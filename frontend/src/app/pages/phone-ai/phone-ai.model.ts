export type CallDirection = 'INBOUND' | 'OUTBOUND';

export type CallStatus =
  | 'RINGING'
  | 'ANSWERED'
  | 'AI_ACTIVE'
  | 'WAITING_FOR_HUMAN'
  | 'HUMAN_ACTIVE'
  | 'COMPLETED'
  | 'FAILED'
  | 'ABANDONED';

export type CallConsentStatus = 'NOT_REQUESTED' | 'PENDING' | 'GRANTED' | 'DECLINED';

export type CallRecordingStatus = 'DISABLED' | 'RECORDING' | 'COMPLETED';

export type CallSpeaker = 'CUSTOMER' | 'AI' | 'HUMAN_AGENT' | 'SYSTEM';

export type CallHandoffReason =
  | 'CUSTOMER_REQUEST'
  | 'AI_UNCERTAIN'
  | 'COMPLEX_EVENT'
  | 'CUSTOM_PRODUCT'
  | 'PRICING_EXCEPTION'
  | 'AVAILABILITY_CONFLICT'
  | 'COMPLAINT'
  | 'PAYMENT_QUESTION'
  | 'CONTRACT_QUESTION'
  | 'OTHER';

export interface PhoneCallSession {
  id: string;
  tenantId: string;
  publicId: string;
  provider: string;
  providerCallId?: string;
  direction: CallDirection;
  status: CallStatus;
  callerNumberMasked: string;
  customerId?: string;
  leadId?: string;
  aiConversationId?: string;
  assignedUserId?: string;
  consentStatus: CallConsentStatus;
  recordingStatus: CallRecordingStatus;
  startedAt: string;
  answeredAt?: string;
  endedAt?: string;
  handoffAt?: string;
  handoffReason?: CallHandoffReason;
  summary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PhoneTranscriptSegment {
  id: string;
  tenantId: string;
  callSessionId: string;
  speaker: CallSpeaker;
  text: string;
  timestamp: string;
  confidence?: number;
  createdAt: string;
}

export interface PhoneTenantSettings {
  id?: string;
  tenantId?: string;
  phoneAiEnabled: boolean;
  provider: string;
  inboundEnabled: boolean;
  recordingEnabled: boolean;
  transcriptionEnabled: boolean;
  transcriptRetentionDays: number;
  humanHandoffNumber: string;
  greeting: string;
  disclosureText: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PhoneDashboardMetrics {
  callsToday: number;
  activeCalls: number;
  waitingForHuman: number;
  completedCalls: number;
  failedCalls: number;
  leadsCreated: number;
}

export interface CallTurnResponse {
  callId: string;
  publicId: string;
  status: CallStatus;
  aiResponseSpeech: string;
  handoffRequested: boolean;
  leadId?: string;
}
