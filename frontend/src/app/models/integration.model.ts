export type IntegrationProvider =
  | 'QUICKBOOKS'
  | 'XERO'
  | 'SHOPIFY'
  | 'HUBSPOT'
  | 'SALESFORCE'
  | 'STRIPE'
  | 'ZAPIER'
  | 'MAKE'
  | 'N8N'
  | 'CUSTOM_WEBHOOK'
  | 'CUSTOM_API'
  | 'MOCK_ACCOUNTING'
  | 'MOCK_CRM'
  | 'MOCK_COMMERCE';

export type ConnectionStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR' | 'DISABLED';
export type ConnectorCapability = 'CUSTOMERS' | 'PRODUCTS' | 'QUOTES' | 'BOOKINGS' | 'INVOICES' | 'PAYMENTS' | 'REFUNDS' | 'INVENTORY' | 'ANALYTICS' | 'WEBHOOKS';
export type WebhookStatus = 'ACTIVE' | 'DISABLED' | 'ERROR';
export type DeliveryStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'RETRYING' | 'DEAD_LETTER';
export type ApiKeyStatus = 'ACTIVE' | 'REVOKED' | 'EXPIRED';

export interface IntegrationConnection {
  id: string;
  tenantId: string;
  provider: IntegrationProvider;
  name: string;
  status: ConnectionStatus;
  configuration?: string;
  maskedCredential?: string;
  capabilities: ConnectorCapability[];
  lastConnectedAt?: string;
  lastSyncAt?: string;
  lastError?: string;
  createdBy?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ConnectIntegrationRequest {
  provider: IntegrationProvider;
  name?: string;
  configuration?: string;
  apiKeyOrSecret?: string;
}

export interface IntegrationSyncJob {
  id: string;
  tenantId: string;
  connectionId: string;
  provider: IntegrationProvider;
  syncType: string;
  status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'PARTIAL' | 'FAILED';
  startedAt: string;
  completedAt?: string;
  recordsProcessed: number;
  recordsFailed: number;
  errorSummary?: string;
  createdAt: string;
}

export interface WebhookEndpoint {
  id: string;
  tenantId: string;
  name: string;
  endpointUrl: string;
  maskedSecret: string;
  status: WebhookStatus;
  subscribedEvents: string[];
  description?: string;
  totalDeliveries: number;
  successfulDeliveries: number;
  successRate: number;
  lastDeliveryAt?: string;
  createdBy?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateWebhookRequest {
  name: string;
  endpointUrl: string;
  subscribedEvents: string[];
  description?: string;
}

export interface WebhookDelivery {
  id: string;
  tenantId: string;
  webhookEndpointId: string;
  webhookName?: string;
  endpointUrl?: string;
  integrationEventId: string;
  eventType: string;
  attemptNumber: number;
  status: DeliveryStatus;
  httpStatus?: number;
  responseSummary?: string;
  requestHeaders?: string;
  requestPayload?: string;
  durationMs?: number;
  nextRetryAt?: string;
  startedAt: string;
  completedAt?: string;
  createdAt: string;
}

export interface ExternalApiKey {
  id: string;
  tenantId: string;
  name: string;
  keyPrefix: string;
  status: ApiKeyStatus;
  scopes: string[];
  rateLimitPerMinute: number;
  lastUsedAt?: string;
  expiresAt?: string;
  createdBy?: string;
  createdAt: string;
  revokedAt?: string;
}

export interface CreateApiKeyRequest {
  name: string;
  scopes: string[];
  rateLimitPerMinute?: number;
  expiresAt?: string;
}

export interface GeneratedApiKeyResponse {
  apiKey: ExternalApiKey;
  rawApiKey: string;
}

export interface IntegrationEvent {
  id: string;
  tenantId: string;
  eventId: string;
  eventType: string;
  aggregateType?: string;
  aggregateId?: string;
  payload: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'PARTIAL' | 'FAILED' | 'DEAD_LETTER';
  occurredAt: string;
  createdAt: string;
}

export interface ExternalEntityMapping {
  id: string;
  tenantId: string;
  provider: IntegrationProvider;
  entityType: string;
  internalId: string;
  externalId: string;
  syncStatus: string;
  lastSyncedAt: string;
  lastError?: string;
}

export interface IntegrationDashboardSummary {
  connectedIntegrations: number;
  webhookEndpoints: number;
  eventsToday: number;
  successfulDeliveriesToday: number;
  failedDeliveriesToday: number;
  pendingRetries: number;
  deadLetterCount: number;
  successRate: number;
}
