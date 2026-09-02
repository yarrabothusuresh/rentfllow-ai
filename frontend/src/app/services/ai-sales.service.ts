import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AiSalesConversation,
  AiSalesDashboard,
  AiEscalation,
  AiQuoteReview,
  AiSettings,
  AiChatRequest,
  AiChatResponse
} from '../models/ai-sales.model';

@Injectable({
  providedIn: 'root'
})
export class AiSalesService {
  private internalBaseUrl = '/api/ai-sales';
  private portalBaseUrl = '/api/portal/ai-sales';

  constructor(private http: HttpClient) {}

  // Internal Sales API
  getDashboard(): Observable<AiSalesDashboard> {
    return this.http.get<AiSalesDashboard>(`${this.internalBaseUrl}/dashboard`);
  }

  getConversations(): Observable<AiSalesConversation[]> {
    return this.http.get<AiSalesConversation[]>(`${this.internalBaseUrl}/conversations`);
  }

  getConversation(id: string): Observable<AiSalesConversation> {
    return this.http.get<AiSalesConversation>(`${this.internalBaseUrl}/conversations/${id}`);
  }

  sendMessage(conversationId: string, request: AiChatRequest): Observable<AiChatResponse> {
    return this.http.post<AiChatResponse>(`${this.internalBaseUrl}/conversations/${conversationId}/messages`, request);
  }

  takeOverConversation(id: string): Observable<{ success: boolean; status: string }> {
    return this.http.post<{ success: boolean; status: string }>(`${this.internalBaseUrl}/conversations/${id}/takeover`, {});
  }

  returnToAi(id: string): Observable<{ success: boolean; status: string }> {
    return this.http.post<{ success: boolean; status: string }>(`${this.internalBaseUrl}/conversations/${id}/return-to-ai`, {});
  }

  escalateConversation(id: string, reason: string, summary: string): Observable<any> {
    return this.http.post(`${this.internalBaseUrl}/conversations/${id}/escalate`, { reason, summary });
  }

  getEscalations(): Observable<AiEscalation[]> {
    return this.http.get<AiEscalation[]>(`${this.internalBaseUrl}/escalations`);
  }

  getQuoteReview(quoteId: string): Observable<AiQuoteReview> {
    return this.http.get<AiQuoteReview>(`${this.internalBaseUrl}/quotes/${quoteId}/review`);
  }

  approveQuote(quoteId: string): Observable<{ success: boolean; status: string; message: string }> {
    return this.http.post<{ success: boolean; status: string; message: string }>(`${this.internalBaseUrl}/quotes/${quoteId}/approve`, {});
  }

  rejectQuote(quoteId: string, reason?: string): Observable<{ success: boolean; status: string; message: string }> {
    return this.http.post<{ success: boolean; status: string; message: string }>(`${this.internalBaseUrl}/quotes/${quoteId}/reject`, { reason });
  }

  getSettings(): Observable<AiSettings> {
    return this.http.get<AiSettings>(`${this.internalBaseUrl}/settings`);
  }

  updateSettings(settings: AiSettings): Observable<AiSettings> {
    return this.http.put<AiSettings>(`${this.internalBaseUrl}/settings`, settings);
  }

  submitFeedback(feedback: { conversationId: string; messageId?: string; helpful: boolean; reason?: string; comments?: string }): Observable<any> {
    return this.http.post(`${this.internalBaseUrl}/feedback`, feedback);
  }

  // Customer Portal / Storefront Public API
  startCustomerConversation(request: AiChatRequest): Observable<AiChatResponse> {
    return this.http.post<AiChatResponse>(`${this.portalBaseUrl}/conversations`, request);
  }

  sendCustomerMessage(conversationId: string, request: AiChatRequest): Observable<AiChatResponse> {
    return this.http.post<AiChatResponse>(`${this.portalBaseUrl}/conversations/${conversationId}/messages`, request);
  }

  getCustomerConversation(conversationId: string): Observable<AiSalesConversation> {
    return this.http.get<AiSalesConversation>(`${this.portalBaseUrl}/conversations/${conversationId}`);
  }
}
