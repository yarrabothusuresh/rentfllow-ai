import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AiCopilotService, AIResponse } from '../../services/ai-copilot.service';

export interface ChatMessage {
  id: string;
  role: 'user' | 'ai';
  content: string;
  intent?: string;
  toolsUsed?: string[];
  suggestedActions?: string[];
  requiresApproval?: boolean;
  actionDetails?: any;
  reasoningSteps?: string[];
  showReasoning?: boolean;
  approved?: boolean;
  timestamp: string;
}

@Component({
  selector: 'app-ai-copilot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-copilot.component.html',
  styleUrl: './ai-copilot.component.scss'
})
export class AiCopilotComponent implements OnInit {
  messages: ChatMessage[] = [];
  inputMessage: string = '';
  isLoading: boolean = false;
  
  activeRole: string = 'OWNER';
  roles: string[] = ['OWNER', 'SALES', 'WAREHOUSE', 'DRIVER', 'CUSTOMER'];

  tenantName: string = 'Evergreen Event Rentals';
  tenantId: string = '99999999-9999-9999-9999-999999999999';

  suggestedPrompts: string[] = [
    "Show me today's priorities",
    "Do I have enough chairs for Saturday?",
    "What is Emily Brown's booking status?",
    "Create a quote for a 250-person wedding",
    "Which event is most profitable?",
    "What should the warehouse prepare tomorrow?",
    "What deliveries are scheduled today?",
    "Send a payment reminder to Emily"
  ];

  constructor(
    private aiCopilotService: AiCopilotService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Check query params for prefilled question
    this.route.queryParams.subscribe(params => {
      if (params['prompt']) {
        this.askQuestion(params['prompt']);
      }
    });

    // Add initial welcome message
    if (this.messages.length === 0) {
      this.messages.push({
        id: 'msg-welcome',
        role: 'ai',
        content: `Good day! I am your <strong>RentFlow AI Business Assistant</strong>. I am connected to <strong>${this.tenantName}</strong> and ready to assist with leads, quotes, inventory, warehouse tasks, and delivery schedules through controlled business tools.`,
        toolsUsed: ['getUpcomingBookings', 'getWarehouseTasks', 'getDeliveries'],
        suggestedActions: ["View Rental Workflow", "Check Today's Deliveries"],
        reasoningSteps: [
          "✓ Initialized RentFlow AI Assistant context",
          "✓ Connected to Evergreen Event Rentals (Tenant ID: 99999999...)",
          "✓ Ready to process role-authenticated requests"
        ],
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      });
    }
  }

  setRole(role: string) {
    this.activeRole = role;
  }

  askQuestion(promptText: string) {
    if (!promptText || promptText.trim() === '' || this.isLoading) return;

    const userMsg: ChatMessage = {
      id: 'usr-' + Date.now(),
      role: 'user',
      content: promptText,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    this.messages.push(userMsg);
    this.inputMessage = '';
    this.isLoading = true;

    this.aiCopilotService.sendMessage({
      message: promptText,
      userId: 'user-001',
      tenantId: this.tenantId,
      role: this.activeRole,
      conversationId: 'conv-session'
    }).subscribe({
      next: (resp: AIResponse) => {
        this.isLoading = false;
        const aiMsg: ChatMessage = {
          id: 'ai-' + Date.now(),
          role: 'ai',
          content: resp.message,
          intent: resp.intent,
          toolsUsed: resp.toolsUsed,
          suggestedActions: resp.suggestedActions,
          requiresApproval: resp.requiresApproval,
          actionDetails: resp.actionDetails,
          reasoningSteps: resp.reasoningSteps,
          showReasoning: false,
          approved: false,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };
        this.messages.push(aiMsg);
      },
      error: (err) => {
        this.isLoading = false;
        this.messages.push({
          id: 'err-' + Date.now(),
          role: 'ai',
          content: "Sorry, an error occurred while connecting to the AI orchestrator.",
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
      }
    });
  }

  toggleReasoning(msg: ChatMessage) {
    msg.showReasoning = !msg.showReasoning;
  }

  approveAction(msg: ChatMessage) {
    msg.approved = true;
  }

  cancelAction(msg: ChatMessage) {
    msg.requiresApproval = false;
  }

  handleAction(actionName: string) {
    if (actionName === 'View Rental Workflow' || actionName === 'View Booking') {
      this.router.navigate(['/workflow-demo']);
    } else if (actionName === 'View ICP' || actionName === 'View Customer') {
      this.router.navigate(['/ideal-customer']);
    } else if (actionName === 'View Storefront') {
      this.router.navigate(['/landing-page']);
    } else {
      this.askQuestion(actionName);
    }
  }
}
