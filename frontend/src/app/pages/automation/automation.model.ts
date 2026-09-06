export type BusinessSignalType = 
  | 'QUOTE_EXPIRING_SOON'
  | 'QUOTE_EXPIRED_PENDING_ACTION'
  | 'QUOTE_MARGIN_LOW'
  | 'QUOTE_AWAITING_CUSTOMER_RESPONSE'
  | 'BOOKING_NEGATIVE_MARGIN'
  | 'BOOKING_MARGIN_LOW'
  | 'INVENTORY_CAPACITY_PRESSURE'
  | 'AVAILABILITY_CONFLICT_REPEATED'
  | 'WAREHOUSE_SHORTAGE_DETECTED'
  | 'WAREHOUSE_ORDER_UNASSIGNED'
  | 'WAREHOUSE_STAGING_BLOCKED'
  | 'DELIVERY_MISSING_DRIVER'
  | 'DELIVERY_AT_RISK_TOMORROW'
  | 'DEPOSIT_UNPAID'
  | 'CONTRACT_UNSIGNED'
  | 'INVOICE_OVERDUE'
  | 'PAYMENT_FAILED'
  | 'RETURN_INSPECTION_PENDING'
  | 'RETURN_ITEM_DAMAGED'
  | 'DAMAGE_CLAIM_PENDING_ESTIMATE'
  | 'DAMAGE_CLAIM_OPEN_OVER_THRESHOLD'
  | 'MAINTENANCE_TICKET_OVERDUE'
  | 'INTEGRATION_DEAD_LETTER_EVENT'
  | 'INTEGRATION_WEBHOOK_FAILED_MULTIPLE'
  | 'CRM_LEAD_OVERDUE_FOLLOW_UP'
  | 'CRM_HIGH_VALUE_LEAD_UNTOUCHED';

export type BusinessSignalSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type BusinessSignalStatus = 'ACTIVE' | 'RESOLVED' | 'IGNORED' | 'EXPIRED';
export type BusinessSignalCategory =
  | 'SALES'
  | 'PRICING'
  | 'PROFITABILITY'
  | 'INVENTORY'
  | 'WAREHOUSE'
  | 'DELIVERY'
  | 'LOGISTICS'
  | 'FINANCE'
  | 'CONTRACT'
  | 'RETURNS'
  | 'DAMAGE'
  | 'MAINTENANCE'
  | 'INTEGRATION'
  | 'CRM';

export type RecommendationStatus = 
  | 'NEW'
  | 'REVIEWED'
  | 'APPROVED'
  | 'REJECTED'
  | 'DISMISSED'
  | 'EXECUTING'
  | 'EXECUTED'
  | 'FAILED'
  | 'RESOLVED'
  | 'EXPIRED';

export type RecommendationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type RecommendationGeneratedBy = 'RULE' | 'RULE_PLUS_AI';
export type EvidenceStrength = 'LOW' | 'MEDIUM' | 'HIGH';
export type AutomationMode = 'RECOMMEND_ONLY' | 'APPROVAL_REQUIRED' | 'AUTO_EXECUTE_LOW_RISK';
export type AutomationActionType =
  | 'CREATE_INTERNAL_TASK'
  | 'CREATE_LEAD_FOLLOW_UP'
  | 'SEND_QUOTE_REMINDER'
  | 'SEND_CONTRACT_REMINDER'
  | 'SEND_DEPOSIT_REMINDER'
  | 'SEND_INVOICE_REMINDER'
  | 'ASSIGN_DRIVER'
  | 'ASSIGN_WAREHOUSE_OPERATOR'
  | 'RETRY_INTEGRATION_EVENT'
  | 'RETRY_WEBHOOK_DELIVERY'
  | 'CREATE_MAINTENANCE_REVIEW_TASK'
  | 'CREATE_DAMAGE_REVIEW_TASK';

export type AutomationExecutionStatus = 
  | 'PROPOSED'
  | 'WAITING_APPROVAL'
  | 'APPROVED'
  | 'EXECUTING'
  | 'EXECUTED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'FAILED'
  | 'EXPIRED'
  | 'SKIPPED'
  | 'STALE';

export type AutomationApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXPIRED';

export interface BusinessSignal {
  id: string;
  tenantId: string;
  signalType: BusinessSignalType;
  category: BusinessSignalCategory;
  sourceEntityType: string;
  sourceEntityId: string;
  sourceEntityNumber?: string;
  severity: BusinessSignalSeverity;
  status: BusinessSignalStatus;
  dedupeKey: string;
  evidenceJson?: string;
  detectedAt: string;
  lastDetectedAt: string;
  resolvedAt?: string;
}

export interface AiRecommendation {
  id: string;
  tenantId: string;
  recommendationNumber: string;
  signalId?: string;
  signalType: BusinessSignalType;
  category: BusinessSignalCategory;
  title: string;
  summary: string;
  detailedExplanation?: string;
  explanation?: string;
  whyImportant?: string;
  whyItMatters?: string;
  sourceEntityType: string;
  sourceEntityId: string;
  sourceEntityNumber?: string;
  entityType?: string;
  entityReference?: string;
  priority: RecommendationPriority;
  status: RecommendationStatus;
  generatedBy: RecommendationGeneratedBy;
  evidenceJson?: string;
  evidence?: any;
  evidenceStrength: EvidenceStrength;
  suggestedActionType?: AutomationActionType;
  suggestedActionPayloadJson?: string;
  actionPayloadJson?: string;
  ruleId?: string;
  reviewedBy?: string;
  reviewedAt?: string;
  dismissReason?: string;
  createdAt: string;
  updatedAt: string;
  expiresAt?: string;
  resolvedAt?: string;
}

export interface AutomationRule {
  id: string;
  tenantId: string;
  ruleCode: string;
  name: string;
  description?: string;
  signalType: BusinessSignalType;
  triggerType: string;
  mode: AutomationMode;
  actionType?: AutomationActionType;
  enabled: boolean;
  conditionsJson?: string;
  cooldownMinutes: number;
  maxExecutionsPerDay: number;
  allowedRoles?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AutomationApproval {
  id: string;
  tenantId: string;
  recommendationId: string;
  actionType: AutomationActionType;
  actionPayloadJson?: string;
  status: AutomationApprovalStatus;
  requestedBy: string;
  proposedBy?: string;
  approvedBy?: string;
  approvalReason?: string;
  rejectionReason?: string;
  decisionReason?: string;
  requiredRole?: string;
  createdAt: string;
  proposedAt?: string;
  decidedAt?: string;
  expiresAt?: string;
}

export interface AutomationExecution {
  id: string;
  tenantId: string;
  recommendationId?: string;
  approvalId?: string;
  ruleId?: string;
  actionType: AutomationActionType;
  idempotencyKey: string;
  executionStatus: AutomationExecutionStatus;
  actionPayloadJson?: string;
  resultSummary?: string;
  errorDetails?: string;
  attemptCount: number;
  executedBy: string;
  createdAt: string;
  executedAt?: string;
  triggerType?: string;
  startedAt?: string;
  completedAt?: string;
}

export interface AutomationDashboardDTO {
  totalActiveRecommendations: number;
  criticalRecommendations: number;
  highRecommendations: number;
  mediumRecommendations: number;
  lowRecommendations: number;
  pendingApprovals: number;
  totalSignalsActive: number;
  totalExecutions: number;
  recentRecommendations: AiRecommendation[];
  pendingApprovalsList: AutomationApproval[];
  recentExecutions: AutomationExecution[];
}
