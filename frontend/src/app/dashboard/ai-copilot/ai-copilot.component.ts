import { Component, OnInit, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import {
  AiCopilotService,
  CopilotResponse,
  CopilotDataBlock,
  CopilotSourceReference,
  CopilotRecommendation,
  CopilotActionProposal
} from '../../services/ai-copilot.service';

export interface ChatMessage {
  id: string;
  role: 'user' | 'ai';
  content: string;
  intent?: string;
  deterministic?: boolean;
  dataBlocks?: CopilotDataBlock[];
  sourceReferences?: CopilotSourceReference[];
  recommendations?: CopilotRecommendation[];
  actionProposals?: CopilotActionProposal[];
  suggestedPrompts?: string[];
  latencyMs?: number;
  timestamp: string;
  feedback?: 'like' | 'dislike';
}

@Component({
  selector: 'app-ai-copilot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-copilot.component.html',
  styleUrl: './ai-copilot.component.scss'
})
export class AiCopilotComponent implements OnInit, AfterViewChecked {
  @ViewChild('chatFeed') private chatFeedContainer?: ElementRef;

  messages: ChatMessage[] = [];
  inputMessage: string = '';
  isLoading: boolean = false;
  conversationId: string = '';

  activeRole: string = 'OWNER';
  roles: string[] = ['OWNER', 'SALES', 'OPERATIONS', 'WAREHOUSE', 'FINANCE'];

  tenantName: string = 'Evergreen Event Rentals';
  suggestedPrompts: string[] = [];

  pageContextType?: string;
  pageContextId?: string;

  private shouldScroll: boolean = false;

  constructor(
    private aiCopilotService: AiCopilotService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Check route query params for context or prefilled prompt
    this.route.queryParams.subscribe(params => {
      if (params['contextType']) this.pageContextType = params['contextType'];
      if (params['contextId']) this.pageContextId = params['contextId'];
      if (params['role']) this.activeRole = params['role'].toUpperCase();

      this.initConversation(params['prompt']);
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.chatFeedContainer) {
        this.chatFeedContainer.nativeElement.scrollTop = this.chatFeedContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  initConversation(initialPrompt?: string) {
    this.isLoading = true;
    this.loadPrompts();

    this.aiCopilotService.startConversation(this.activeRole, 'user-001', this.pageContextType, this.pageContextId)
      .subscribe({
        next: (resp: CopilotResponse) => {
          this.isLoading = false;
          this.conversationId = resp.conversationId;

          this.messages = [{
            id: 'msg-welcome',
            role: 'ai',
            content: resp.message,
            intent: resp.detectedIntent,
            deterministic: resp.deterministic,
            dataBlocks: resp.dataBlocks || [],
            sourceReferences: resp.sourceReferences || [],
            recommendations: resp.recommendations || [],
            actionProposals: resp.actionProposals || [],
            suggestedPrompts: resp.suggestedPrompts || [],
            latencyMs: resp.latencyMs,
            timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          }];

          if (resp.suggestedPrompts && resp.suggestedPrompts.length > 0) {
            this.suggestedPrompts = resp.suggestedPrompts;
          }

          this.shouldScroll = true;

          if (initialPrompt) {
            this.askQuestion(initialPrompt);
          }
        },
        error: () => {
          this.isLoading = false;
          this.conversationId = 'session-' + Date.now();
        }
      });
  }

  setRole(role: string) {
    if (this.activeRole === role) return;
    this.activeRole = role;
    this.initConversation();
  }

  loadPrompts() {
    this.aiCopilotService.getQuickPrompts(this.activeRole).subscribe(prompts => {
      if (prompts && prompts.length > 0) {
        this.suggestedPrompts = prompts;
      }
    });
  }

  askQuestion(promptText: string) {
    const text = promptText ? promptText.trim() : this.inputMessage.trim();
    if (!text || this.isLoading) return;

    const userMsg: ChatMessage = {
      id: 'usr-' + Date.now(),
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    this.messages.push(userMsg);
    this.inputMessage = '';
    this.isLoading = true;
    this.shouldScroll = true;

    this.aiCopilotService.sendCopilotMessage(
      this.conversationId,
      text,
      this.activeRole,
      'user-001',
      this.pageContextType,
      this.pageContextId
    ).subscribe({
      next: (resp: CopilotResponse) => {
        this.isLoading = false;
        const aiMsg: ChatMessage = {
          id: 'ai-' + Date.now(),
          role: 'ai',
          content: resp.message,
          intent: resp.detectedIntent,
          deterministic: resp.deterministic,
          dataBlocks: resp.dataBlocks || [],
          sourceReferences: resp.sourceReferences || [],
          recommendations: resp.recommendations || [],
          actionProposals: resp.actionProposals || [],
          suggestedPrompts: resp.suggestedPrompts || [],
          latencyMs: resp.latencyMs,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };
        this.messages.push(aiMsg);

        if (resp.suggestedPrompts && resp.suggestedPrompts.length > 0) {
          this.suggestedPrompts = resp.suggestedPrompts;
        }

        this.shouldScroll = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.messages.push({
          id: 'err-' + Date.now(),
          role: 'ai',
          content: "⚠️ An error occurred while communicating with the Copilot orchestrator.",
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
        this.shouldScroll = true;
      }
    });
  }

  confirmProposal(proposal: CopilotActionProposal) {
    proposal.status = 'EXECUTING';
    this.aiCopilotService.confirmAction(proposal.proposalId, this.activeRole, 'user-001').subscribe({
      next: (updated) => {
        proposal.status = 'EXECUTED';
        proposal.requiresConfirmation = false;
        this.messages.push({
          id: 'act-' + Date.now(),
          role: 'ai',
          content: `✅ **Action Confirmed & Executed!**\n\n${proposal.summary} has been completed successfully and logged to the audit journal.`,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
        this.shouldScroll = true;
      },
      error: (err) => {
        proposal.status = 'FAILED';
        alert('Action execution failed: ' + (err.error?.message || err.message || 'Permission denied'));
      }
    });
  }

  cancelProposal(proposal: CopilotActionProposal) {
    this.aiCopilotService.cancelAction(proposal.proposalId, this.activeRole, 'user-001').subscribe({
      next: () => {
        proposal.status = 'CANCELLED';
        proposal.requiresConfirmation = false;
      }
    });
  }

  navigateToSource(route: string) {
    if (route) {
      this.router.navigateByUrl(route);
    }
  }

  setFeedback(msg: ChatMessage, rating: 'like' | 'dislike') {
    msg.feedback = rating;
  }

  // Helpers for table data block rendering
  getTableHeaders(data: any): string[] {
    if (Array.isArray(data) && data.length > 0) {
      return Object.keys(data[0]);
    }
    return [];
  }

  isObject(val: any): boolean {
    return val && typeof val === 'object' && !Array.isArray(val);
  }

  objectEntries(obj: any): [string, any][] {
    return obj ? Object.entries(obj) : [];
  }
}
