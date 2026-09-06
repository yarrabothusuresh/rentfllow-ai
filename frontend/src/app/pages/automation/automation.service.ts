import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  AiRecommendation, 
  AutomationApproval, 
  AutomationDashboardDTO, 
  AutomationExecution, 
  AutomationMode, 
  AutomationRule, 
  BusinessSignal, 
  BusinessSignalCategory, 
  BusinessSignalStatus, 
  RecommendationPriority, 
  RecommendationStatus 
} from './automation.model';

@Injectable({
  providedIn: 'root'
})
export class AutomationService {

  private autoBaseUrl = '/api/automation';
  private recBaseUrl = '/api/recommendations';
  private defaultTenantId = '99999999-9999-9999-9999-999999999999';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'ADMIN', userId: string = 'user-001'): HttpHeaders {
    return new HttpHeaders()
      .set('X-Tenant-Id', this.defaultTenantId)
      .set('X-User-Role', role)
      .set('X-User-Id', userId);
  }

  getDashboard(role: string = 'ADMIN'): Observable<AutomationDashboardDTO> {
    return this.http.get<AutomationDashboardDTO>(`${this.autoBaseUrl}/dashboard`, { headers: this.getHeaders(role) });
  }

  getRecommendations(
    status?: RecommendationStatus,
    priority?: RecommendationPriority,
    category?: BusinessSignalCategory,
    page: number = 0,
    size: number = 20,
    role: string = 'ADMIN'
  ): Observable<{ content: AiRecommendation[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) params = params.set('status', status);
    if (priority) params = params.set('priority', priority);
    if (category) params = params.set('category', category);

    return this.http.get<{ content: AiRecommendation[]; totalElements: number; totalPages: number }>(
      this.recBaseUrl, { headers: this.getHeaders(role), params }
    );
  }

  getRecommendation(id: string, role: string = 'ADMIN'): Observable<AiRecommendation> {
    return this.http.get<AiRecommendation>(`${this.recBaseUrl}/${id}`, { headers: this.getHeaders(role) });
  }

  markReviewed(id: string, role: string = 'ADMIN'): Observable<AiRecommendation> {
    return this.http.post<AiRecommendation>(`${this.recBaseUrl}/${id}/review`, {}, { headers: this.getHeaders(role) });
  }

  dismissRecommendation(id: string, reason: string, role: string = 'ADMIN'): Observable<AiRecommendation> {
    return this.http.post<AiRecommendation>(`${this.recBaseUrl}/${id}/dismiss`, { reason }, { headers: this.getHeaders(role) });
  }

  proposeAction(id: string, role: string = 'ADMIN'): Observable<AutomationApproval> {
    return this.http.post<AutomationApproval>(`${this.recBaseUrl}/${id}/propose-action`, {}, { headers: this.getHeaders(role) });
  }

  triggerDetection(role: string = 'ADMIN'): Observable<AiRecommendation[]> {
    return this.http.post<AiRecommendation[]>(`${this.recBaseUrl}/detect`, {}, { headers: this.getHeaders(role) });
  }

  getSignals(status?: BusinessSignalStatus, role: string = 'ADMIN'): Observable<BusinessSignal[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<BusinessSignal[]>(`${this.autoBaseUrl}/signals`, { headers: this.getHeaders(role), params });
  }

  getApprovals(status?: string, page: number = 0, size: number = 20, role: string = 'ADMIN'): Observable<{ content: AutomationApproval[]; totalElements: number }> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) params = params.set('status', status);
    return this.http.get<{ content: AutomationApproval[]; totalElements: number }>(
      `${this.autoBaseUrl}/approvals`, { headers: this.getHeaders(role), params }
    );
  }

  approve(id: string, reason: string = 'Approved by user', role: string = 'ADMIN'): Observable<AutomationExecution> {
    return this.http.post<AutomationExecution>(`${this.autoBaseUrl}/approvals/${id}/approve`, { reason }, { headers: this.getHeaders(role) });
  }

  reject(id: string, reason: string, role: string = 'ADMIN'): Observable<AutomationApproval> {
    return this.http.post<AutomationApproval>(`${this.autoBaseUrl}/approvals/${id}/reject`, { reason }, { headers: this.getHeaders(role) });
  }

  getRules(role: string = 'ADMIN'): Observable<AutomationRule[]> {
    return this.http.get<AutomationRule[]>(`${this.autoBaseUrl}/rules`, { headers: this.getHeaders(role) });
  }

  updateRule(id: string, mode: AutomationMode, enabled: boolean, role: string = 'ADMIN'): Observable<AutomationRule> {
    return this.http.put<AutomationRule>(`${this.autoBaseUrl}/rules/${id}`, { mode, enabled }, { headers: this.getHeaders(role) });
  }

  toggleRule(id: string, enabled: boolean, role: string = 'ADMIN'): Observable<AutomationRule> {
    return this.http.post<AutomationRule>(`${this.autoBaseUrl}/rules/${id}/toggle`, {}, { 
      headers: this.getHeaders(role),
      params: new HttpParams().set('enabled', enabled.toString())
    });
  }

  updateRuleMode(id: string, mode: AutomationMode, role: string = 'ADMIN'): Observable<AutomationRule> {
    return this.http.put<AutomationRule>(`${this.autoBaseUrl}/rules/${id}/mode`, {}, { 
      headers: this.getHeaders(role),
      params: new HttpParams().set('mode', mode)
    });
  }

  getExecutions(page: number = 0, size: number = 20, role: string = 'ADMIN'): Observable<{ content: AutomationExecution[]; totalElements: number }> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<{ content: AutomationExecution[]; totalElements: number }>(
      `${this.autoBaseUrl}/executions`, { headers: this.getHeaders(role), params }
    );
  }
}
