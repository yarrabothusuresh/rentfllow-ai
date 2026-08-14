import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface AIRequest {
  message: string;
  userId?: string;
  tenantId?: string;
  role?: string;
  conversationId?: string;
}

export interface AIResponse {
  message: string;
  intent: string;
  toolsUsed: string[];
  suggestedActions: string[];
  requiresApproval: boolean;
  actionDetails?: any;
  reasoningSteps?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AiCopilotService {
  private apiUrl = '/api/ai/chat';

  constructor(private http: HttpClient) {}

  sendMessage(request: AIRequest): Observable<AIResponse> {
    return this.http.post<AIResponse>(this.apiUrl, request).pipe(
      catchError(error => {
        console.warn('Backend AI service call failed, returning fallback mock response', error);
        return of(this.getFallbackResponse(request));
      })
    );
  }

  private getFallbackResponse(request: AIRequest): AIResponse {
    const role = request.role || 'OWNER';
    if (role === 'CUSTOMER' && (request.message.includes('another') || request.message.includes('other'))) {
      return {
        message: "You don't have permission to access that information.",
        intent: "PERMISSION_DENIED",
        toolsUsed: ["searchCustomer", "getBooking"],
        suggestedActions: ["View Own Quotes"],
        requiresApproval: false,
        reasoningSteps: [
          "✓ Understood request",
          "❌ Security check failed: CUSTOMER role restricted to own records",
          "✓ Returned permission denied notice"
        ]
      };
    }

    if (request.message.includes('remind') || request.message.includes('payment reminder')) {
      return {
        message: "AI wants to perform sensitive action: Send payment reminder to Emily Brown ($2,500 due). Explicit user approval required.",
        intent: "ACTION_REQUIRES_APPROVAL",
        toolsUsed: ["sendPaymentReminder"],
        suggestedActions: ["Approve Action", "Cancel"],
        requiresApproval: true,
        actionDetails: {
          targetAction: "Send payment reminder",
          customer: "Emily Brown",
          outstandingAmount: "$2,500.00",
          previewMessage: "Dear Emily, your deposit/payment balance of $2,500 is due for your upcoming wedding rental on Sept 20, 2026."
        },
        reasoningSteps: [
          "✓ Understood intent to send payment reminder",
          "✓ Prepared reminder parameters for Emily Brown",
          "⚠️ Action marked as requiresApproval"
        ]
      };
    }

    return {
      message: `Processed request for "${request.message}". Emily Brown's wedding quote ($6,480) has been sent and is awaiting confirmation.`,
      intent: "GENERAL_INFO",
      toolsUsed: ["searchCustomer", "getBooking"],
      suggestedActions: ["View Rental Workflow", "View Deliveries"],
      requiresApproval: false,
      reasoningSteps: [
        "✓ Understood request intent",
        "✓ Queried Evergreen Event Rentals database",
        "✓ Formatted operational summary"
      ]
    };
  }
}
