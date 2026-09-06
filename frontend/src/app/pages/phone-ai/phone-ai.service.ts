import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CallHandoffReason,
  CallStatus,
  CallTurnResponse,
  PhoneCallSession,
  PhoneDashboardMetrics,
  PhoneTenantSettings,
  PhoneTranscriptSegment
} from './phone-ai.model';

@Injectable({
  providedIn: 'root'
})
export class PhoneAiService {

  private baseUrl = '/api/phone';
  private defaultTenantId = '99999999-9999-9999-9999-999999999999';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'ADMIN', userId: string = 'user-001'): HttpHeaders {
    return new HttpHeaders()
      .set('X-Tenant-Id', this.defaultTenantId)
      .set('X-User-Role', role)
      .set('X-User-Id', userId);
  }

  getDashboard(role: string = 'ADMIN'): Observable<PhoneDashboardMetrics> {
    return this.http.get<PhoneDashboardMetrics>(`${this.baseUrl}/dashboard`, { headers: this.getHeaders(role) });
  }

  getCalls(
    status?: CallStatus,
    page: number = 0,
    size: number = 20,
    role: string = 'ADMIN'
  ): Observable<{ content: PhoneCallSession[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) params = params.set('status', status);

    return this.http.get<{ content: PhoneCallSession[]; totalElements: number; totalPages: number }>(
      `${this.baseUrl}/calls`, { headers: this.getHeaders(role), params }
    );
  }

  getCallDetail(id: string, role: string = 'ADMIN'): Observable<{ call: PhoneCallSession; transcripts: PhoneTranscriptSegment[] }> {
    return this.http.get<{ call: PhoneCallSession; transcripts: PhoneTranscriptSegment[] }>(
      `${this.baseUrl}/calls/${id}`, { headers: this.getHeaders(role) }
    );
  }

  simulateInboundCall(callerNumber?: string, role: string = 'ADMIN'): Observable<PhoneCallSession> {
    const body = callerNumber ? { callerNumber } : {};
    return this.http.post<PhoneCallSession>(`${this.baseUrl}/calls/simulate-inbound`, body, { headers: this.getHeaders(role) });
  }

  sendUtterance(callId: string, utterance: string, role: string = 'ADMIN'): Observable<CallTurnResponse> {
    return this.http.post<CallTurnResponse>(
      `${this.baseUrl}/calls/${callId}/utterance`,
      { utterance },
      { headers: this.getHeaders(role) }
    );
  }

  requestHandoff(callId: string, reason: CallHandoffReason = 'CUSTOMER_REQUEST', role: string = 'ADMIN'): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/calls/${callId}/handoff`,
      { reason },
      { headers: this.getHeaders(role) }
    );
  }

  endCall(callId: string, role: string = 'ADMIN'): Observable<any> {
    return this.http.post(`${this.baseUrl}/calls/${callId}/end`, {}, { headers: this.getHeaders(role) });
  }

  getSettings(role: string = 'ADMIN'): Observable<PhoneTenantSettings> {
    return this.http.get<PhoneTenantSettings>(`${this.baseUrl}/settings`, { headers: this.getHeaders(role) });
  }

  updateSettings(settings: PhoneTenantSettings, role: string = 'ADMIN'): Observable<PhoneTenantSettings> {
    return this.http.put<PhoneTenantSettings>(`${this.baseUrl}/settings`, settings, { headers: this.getHeaders(role) });
  }
}
