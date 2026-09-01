import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  IntegrationConnection,
  ConnectIntegrationRequest,
  IntegrationSyncJob,
  WebhookEndpoint,
  CreateWebhookRequest,
  WebhookDelivery,
  ExternalApiKey,
  CreateApiKeyRequest,
  GeneratedApiKeyResponse,
  IntegrationEvent,
  ExternalEntityMapping,
  IntegrationDashboardSummary
} from '../models/integration.model';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Dashboard & Connections
  getDashboardSummary(): Observable<IntegrationDashboardSummary> {
    return this.http.get<IntegrationDashboardSummary>(`${this.baseUrl}/integrations/dashboard-summary`);
  }

  getConnections(): Observable<IntegrationConnection[]> {
    return this.http.get<IntegrationConnection[]>(`${this.baseUrl}/integrations`);
  }

  getConnection(id: string): Observable<IntegrationConnection> {
    return this.http.get<IntegrationConnection>(`${this.baseUrl}/integrations/${id}`);
  }

  connect(req: ConnectIntegrationRequest): Observable<IntegrationConnection> {
    return this.http.post<IntegrationConnection>(`${this.baseUrl}/integrations`, req);
  }

  testConnection(id: string): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(`${this.baseUrl}/integrations/${id}/test`, {});
  }

  triggerSync(id: string): Observable<IntegrationSyncJob> {
    return this.http.post<IntegrationSyncJob>(`${this.baseUrl}/integrations/${id}/sync`, {});
  }

  disconnect(id: string): Observable<IntegrationConnection> {
    return this.http.post<IntegrationConnection>(`${this.baseUrl}/integrations/${id}/disconnect`, {});
  }

  getSyncJobs(id: string): Observable<IntegrationSyncJob[]> {
    return this.http.get<IntegrationSyncJob[]>(`${this.baseUrl}/integrations/${id}/sync-jobs`);
  }

  getEvents(limit = 50): Observable<IntegrationEvent[]> {
    return this.http.get<IntegrationEvent[]>(`${this.baseUrl}/integrations/events?limit=${limit}`);
  }

  getDeadLetterDeliveries(): Observable<WebhookDelivery[]> {
    return this.http.get<WebhookDelivery[]>(`${this.baseUrl}/integrations/dead-letter`);
  }

  getEntityMappings(provider?: string): Observable<ExternalEntityMapping[]> {
    const url = provider ? `${this.baseUrl}/integrations/mappings?provider=${provider}` : `${this.baseUrl}/integrations/mappings`;
    return this.http.get<ExternalEntityMapping[]>(url);
  }

  // Webhooks
  getWebhooks(): Observable<WebhookEndpoint[]> {
    return this.http.get<WebhookEndpoint[]>(`${this.baseUrl}/integrations/webhooks`);
  }

  getWebhook(id: string): Observable<WebhookEndpoint> {
    return this.http.get<WebhookEndpoint>(`${this.baseUrl}/integrations/webhooks/${id}`);
  }

  createWebhook(req: CreateWebhookRequest): Observable<{ endpoint: WebhookEndpoint; signingSecret: string }> {
    return this.http.post<{ endpoint: WebhookEndpoint; signingSecret: string }>(`${this.baseUrl}/integrations/webhooks`, req);
  }

  updateWebhook(id: string, req: CreateWebhookRequest): Observable<WebhookEndpoint> {
    return this.http.patch<WebhookEndpoint>(`${this.baseUrl}/integrations/webhooks/${id}`, req);
  }

  deleteWebhook(id: string): Observable<{ success: boolean }> {
    return this.http.delete<{ success: boolean }>(`${this.baseUrl}/integrations/webhooks/${id}`);
  }

  testWebhook(id: string): Observable<WebhookDelivery> {
    return this.http.post<WebhookDelivery>(`${this.baseUrl}/integrations/webhooks/${id}/test`, {});
  }

  rotateWebhookSecret(id: string): Observable<{ endpoint: WebhookEndpoint; newSigningSecret: string }> {
    return this.http.post<{ endpoint: WebhookEndpoint; newSigningSecret: string }>(`${this.baseUrl}/integrations/webhooks/${id}/rotate-secret`, {});
  }

  getWebhookDeliveries(id: string, limit = 50): Observable<WebhookDelivery[]> {
    return this.http.get<WebhookDelivery[]>(`${this.baseUrl}/integrations/webhooks/${id}/deliveries?limit=${limit}`);
  }

  retryDelivery(id: string): Observable<WebhookDelivery> {
    return this.http.post<WebhookDelivery>(`${this.baseUrl}/integrations/webhook-deliveries/${id}/retry`, {});
  }

  // API Keys
  getApiKeys(): Observable<ExternalApiKey[]> {
    return this.http.get<ExternalApiKey[]>(`${this.baseUrl}/api-keys`);
  }

  generateApiKey(req: CreateApiKeyRequest): Observable<GeneratedApiKeyResponse> {
    return this.http.post<GeneratedApiKeyResponse>(`${this.baseUrl}/api-keys`, req);
  }

  revokeApiKey(id: string): Observable<ExternalApiKey> {
    return this.http.post<ExternalApiKey>(`${this.baseUrl}/api-keys/${id}/revoke`, {});
  }
}
