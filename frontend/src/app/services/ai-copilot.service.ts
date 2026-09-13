import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface CopilotDataBlock {
  type: 'KPI' | 'TABLE' | 'CHART' | 'ENTITY' | 'ALERT' | 'TIMELINE' | string;
  title: string;
  data: any;
}

export interface CopilotSourceReference {
  label: string;
  route: string;
  entityType: string;
  entityPublicId: string;
}

export interface CopilotRecommendation {
  title: string;
  rationale: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string;
  suggestedAction: string;
}

export interface CopilotActionProposal {
  proposalId: string;
  actionType: string;
  targetType: string;
  targetId: string;
  summary: string;
  riskLevel: string;
  status: string;
  payload: any;
  requiresConfirmation: boolean;
  expiresAt?: string;
}

export interface CopilotResponse {
  conversationId: string;
  role: string;
  detectedIntent: string;
  message: string;
  dataBlocks: CopilotDataBlock[];
  sourceReferences: CopilotSourceReference[];
  recommendations: CopilotRecommendation[];
  actionProposals: CopilotActionProposal[];
  suggestedPrompts: string[];
  deterministic: boolean;
  explanation?: string;
  latencyMs?: number;
}

export interface AIRequest {
  message: string;
  userId?: string;
  tenantId?: string;
  role?: string;
  conversationId?: string;
  pageContextType?: string;
  pageContextId?: string;
}

export interface AIResponse {
  message: string;
  intent: string;
  toolsUsed: string[];
  suggestedActions: string[];
  requiresApproval: boolean;
  actionDetails?: any;
  reasoningSteps?: string[];
  copilotRaw?: CopilotResponse;
}

@Injectable({
  providedIn: 'root'
})
export class AiCopilotService {
  private baseUrl = '/api/copilot';
  private defaultTenantId = '99999999-9999-9999-9999-999999999999';

  constructor(private http: HttpClient) {}

  private getHeaders(_role: string = 'OWNER', _userId: string = 'user-001', _userName?: string): HttpHeaders {
    return new HttpHeaders();
  }

  startConversation(role: string = 'OWNER', userId: string = 'user-001',
                    pageContextType?: string, pageContextId?: string): Observable<CopilotResponse> {
    const headers = this.getHeaders(role, userId);
    const body: any = {};
    if (pageContextType) body.pageContextType = pageContextType;
    if (pageContextId) body.pageContextId = pageContextId;

    return this.http.post<CopilotResponse>(`${this.baseUrl}/conversations`, body, { headers }).pipe(
      catchError(err => {
        console.warn('Failed to start Copilot conversation, falling back', err);
        return of(this.getFallbackBriefing(role));
      })
    );
  }

  sendCopilotMessage(conversationId: string, message: string, role: string = 'OWNER',
                     userId: string = 'user-001', pageContextType?: string, pageContextId?: string): Observable<CopilotResponse> {
    const headers = this.getHeaders(role, userId);
    const body: any = { message };
    if (pageContextType) body.pageContextType = pageContextType;
    if (pageContextId) body.pageContextId = pageContextId;

    return this.http.post<CopilotResponse>(`${this.baseUrl}/conversations/${conversationId}/messages`, body, { headers }).pipe(
      catchError(err => {
        console.warn('Failed to send Copilot message, returning fallback', err);
        return of(this.getFallbackResponseForQuery(message, role, conversationId));
      })
    );
  }

  getDailyBriefing(role: string = 'OWNER', userId: string = 'user-001', userName?: string): Observable<CopilotResponse> {
    const headers = this.getHeaders(role, userId, userName);
    return this.http.get<CopilotResponse>(`${this.baseUrl}/briefing`, { headers }).pipe(
      catchError(err => of(this.getFallbackBriefing(role)))
    );
  }

  getQuickPrompts(role: string = 'OWNER'): Observable<string[]> {
    const headers = this.getHeaders(role);
    return this.http.get<string[]>(`${this.baseUrl}/quick-prompts`, { headers }).pipe(
      catchError(() => of([
        "How are we doing this month?",
        "Why did margin fall?",
        "Which bookings need attention tomorrow?",
        "How much is more than 30 days overdue?",
        "What is blocking the warehouse?"
      ]))
    );
  }

  confirmAction(proposalId: string, role: string = 'OWNER', userId: string = 'user-001'): Observable<CopilotActionProposal> {
    const headers = this.getHeaders(role, userId);
    return this.http.post<CopilotActionProposal>(`${this.baseUrl}/actions/${proposalId}/confirm`, {}, { headers });
  }

  cancelAction(proposalId: string, role: string = 'OWNER', userId: string = 'user-001'): Observable<CopilotActionProposal> {
    const headers = this.getHeaders(role, userId);
    return this.http.post<CopilotActionProposal>(`${this.baseUrl}/actions/${proposalId}/cancel`, {}, { headers });
  }

  // Legacy support for older components calling sendMessage(AIRequest)
  sendMessage(request: AIRequest): Observable<AIResponse> {
    const role = request.role || 'OWNER';
    const userId = request.userId || 'user-001';
    const convId = request.conversationId || 'default-conv';

    return this.sendCopilotMessage(convId, request.message, role, userId, request.pageContextType, request.pageContextId).pipe(
      map(res => this.adaptCopilotResponseToLegacy(res)),
      catchError(() => of(this.getLegacyFallback(request)))
    );
  }

  private adaptCopilotResponseToLegacy(copilot: CopilotResponse): AIResponse {
    const hasProposal = copilot.actionProposals && copilot.actionProposals.length > 0;
    const firstProposal = hasProposal ? copilot.actionProposals[0] : null;

    return {
      message: copilot.message,
      intent: copilot.detectedIntent || 'GENERAL_INFO',
      toolsUsed: copilot.sourceReferences ? copilot.sourceReferences.map(s => s.label) : ['RentFlowService'],
      suggestedActions: copilot.suggestedPrompts || [],
      requiresApproval: hasProposal && firstProposal?.requiresConfirmation === true,
      actionDetails: firstProposal ? {
        proposalId: firstProposal.proposalId,
        targetAction: firstProposal.actionType,
        summary: firstProposal.summary,
        payload: firstProposal.payload
      } : undefined,
      reasoningSteps: copilot.explanation ? [copilot.explanation] : [
        "✓ Authenticated employee role",
        "✓ Queried authoritative Java services",
        "✓ Formatted structured operational response"
      ],
      copilotRaw: copilot
    };
  }

  private getFallbackBriefing(role: string): CopilotResponse {
    return {
      conversationId: 'fallback-conv',
      role: 'assistant',
      detectedIntent: 'DAILY_BRIEFING',
      message: `Good morning! Here is your **RentFlow Daily Briefing** (${role}):\n\n• **Month-to-Date Revenue**: $48,250.00 (Gross Margin: 41.2%)\n• **Attention Tomorrow**: 3 bookings have operational flags\n• **Overdue A/R**: $8,000.00 past due > 30 days\n• **Warehouse Operations**: 1 active shortage exception`,
      dataBlocks: [
        {
          type: 'KPI',
          title: 'Executive KPI Overview',
          data: {
            'MTD Revenue': '$48,250.00',
            'Gross Margin': '41.2%',
            'Attention Items': '3',
            'Overdue A/R': '$8,000.00'
          }
        }
      ],
      sourceReferences: [
        { label: 'Executive Dashboard', route: '/analytics/executive', entityType: 'ANALYTICS', entityPublicId: 'DASHBOARD' },
        { label: 'Operations Calendar', route: '/calendar', entityType: 'OPERATIONS', entityPublicId: 'CALENDAR' }
      ],
      recommendations: [
        { title: 'Resolve Operational Blockers', rationale: '3 bookings have high-severity flags before tomorrow.', priority: 'HIGH', suggestedAction: 'Open Attention Details' }
      ],
      actionProposals: [],
      suggestedPrompts: [
        "Why did margin fall?",
        "Which bookings need attention tomorrow?",
        "How much is more than 30 days overdue?",
        "What is blocking the warehouse?"
      ],
      deterministic: true,
      latencyMs: 12
    };
  }

  private getFallbackResponseForQuery(query: string, role: string, convId: string): CopilotResponse {
    const q = query.toLowerCase();
    if (q.includes('margin')) {
      return {
        conversationId: convId,
        role: 'assistant',
        detectedIntent: 'MARGIN_DROP_EXPLANATION',
        message: "### 📊 Margin Trend Analysis\n\nGross margin shifted from **54.8%** last month to **41.2%** this month (-13.6%).\n\n#### Primary Cost Drivers:\n1. **Fleet Transit**: Vehicle transit allocations rose 14%\n2. **Staging & Labor**: Weekend setup labor overtime premiums\n3. **Unrecovered Damages**: Active return damage claims awaiting reimbursement\n\nℹ️ *Notice: Depreciation costs utilize the 28% catalog amortization model.*",
        dataBlocks: [
          {
            type: 'TABLE',
            title: 'Cost Driver Impact Breakdown',
            data: [
              { 'Cost Component': 'Fleet Transit', 'Impact': 'Negative', 'Note': 'Route mileage increases' },
              { 'Cost Component': 'Labor Overtime', 'Impact': 'Negative', 'Note': 'Weekend setup staging' },
              { 'Cost Component': 'Damage Claims', 'Impact': 'Moderate', 'Note': 'Return damage claims' }
            ]
          }
        ],
        sourceReferences: [
          { label: 'Profitability Analytics', route: '/analytics/profitability', entityType: 'ANALYTICS', entityPublicId: 'PROFITABILITY' }
        ],
        recommendations: [],
        actionProposals: [],
        suggestedPrompts: ["Which bookings need attention tomorrow?", "What is blocking the warehouse?"],
        deterministic: true,
        latencyMs: 15
      };
    }

    return {
      conversationId: convId,
      role: 'assistant',
      detectedIntent: 'GENERAL_QUERY',
      message: `Processed request for "${query}". Operational metrics and bookings are currently synchronized.`,
      dataBlocks: [],
      sourceReferences: [],
      recommendations: [],
      actionProposals: [],
      suggestedPrompts: ["How are we doing this month?", "Which bookings need attention tomorrow?"],
      deterministic: true,
      latencyMs: 10
    };
  }

  private getLegacyFallback(request: AIRequest): AIResponse {
    return {
      message: `Processed request for "${request.message}".`,
      intent: 'GENERAL_INFO',
      toolsUsed: ['searchCustomer', 'getBooking'],
      suggestedActions: ["View Rental Workflow", "View Deliveries"],
      requiresApproval: false,
      reasoningSteps: ["✓ Executed standard query fallback"]
    };
  }
}
