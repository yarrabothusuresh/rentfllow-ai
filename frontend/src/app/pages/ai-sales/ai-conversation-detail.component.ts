import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiSalesConversation, AiSalesMessage } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-conversation-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="conv-detail-container" *ngIf="conversation">
      <!-- Breadcrumb & Top Bar -->
      <header class="detail-header">
        <div class="header-left">
          <div class="breadcrumb">
            <a routerLink="/ai-sales/conversations">&larr; All Conversations</a>
          </div>
          <div class="title-with-badge">
            <h1>{{ conversation.customerName || 'Inquiry Session' }}</h1>
            <span class="badge" [ngClass]="getStatusClass(conversation.status)">
              {{ formatStatus(conversation.status) }}
            </span>
            <span class="channel-tag">{{ conversation.channel }}</span>
          </div>
          <span class="session-id">ID: <code>{{ conversation.publicId }}</code></span>
        </div>

        <div class="header-controls">
          <button
            *ngIf="conversation.status !== 'HUMAN_ACTIVE'"
            (click)="takeOver()"
            class="btn btn-warning"
            [disabled]="isActionLoading"
          >
            <i class="bi bi-person-fill-gear"></i> Take Over (Human)
          </button>

          <button
            *ngIf="conversation.status === 'HUMAN_ACTIVE'"
            (click)="returnToAi()"
            class="btn btn-success"
            [disabled]="isActionLoading"
          >
            <i class="bi bi-robot"></i> Return to AI
          </button>

          <button
            (click)="escalate()"
            class="btn btn-danger-outline"
            [disabled]="isActionLoading || conversation.status === 'ESCALATED'"
          >
            <i class="bi bi-flag"></i> Escalate
          </button>

          <a
            *ngIf="conversation.quoteId"
            [routerLink]="['/ai-sales/quotes', conversation.quoteId, 'review']"
            class="btn btn-primary"
          >
            <i class="bi bi-receipt"></i> Review Quote Draft &rarr;
          </a>
        </div>
      </header>

      <!-- 2-Column Split Console -->
      <div class="console-grid">
        <!-- Left: Transcript & Message Input -->
        <div class="transcript-pane">
          <div class="messages-container" #scrollContainer>
            <div
              *ngFor="let msg of conversation.messages"
              class="message-wrapper"
              [ngClass]="'sender-' + (msg.senderType | lowercase)"
            >
              <div class="avatar" [ngClass]="'avatar-' + (msg.senderType | lowercase)">
                <i [ngClass]="getSenderIcon(msg.senderType)"></i>
              </div>
              <div class="message-bubble">
                <div class="message-meta">
                  <strong>{{ getSenderLabel(msg.senderType) }}</strong>
                  <span>{{ msg.createdAt | date:'shortTime' }}</span>
                </div>
                <div class="message-text">{{ msg.content }}</div>
              </div>
            </div>

            <div *ngIf="isAiThinking" class="message-wrapper sender-ai">
              <div class="avatar avatar-ai"><i class="bi bi-robot"></i></div>
              <div class="message-bubble thinking-bubble">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
                <em>AI Sales Agent checking catalog and availability...</em>
              </div>
            </div>
          </div>

          <!-- Message Composer -->
          <div class="composer-bar">
            <input
              type="text"
              [(ngModel)]="newMessageText"
              (keyup.enter)="sendMessage()"
              placeholder="Type message or reply as sales representative..."
              [disabled]="isAiThinking"
            />
            <button
              (click)="sendMessage()"
              class="btn btn-primary btn-send"
              [disabled]="!newMessageText.trim() || isAiThinking"
            >
              <i class="bi bi-send"></i> Send
            </button>
          </div>
        </div>

        <!-- Right: Structured Inquiry & Decision Context -->
        <div class="context-pane">
          <!-- Customer Profile -->
          <div class="context-card">
            <h3><i class="bi bi-person-badge"></i> Contact Information</h3>
            <div class="info-row">
              <span class="label">Name:</span>
              <strong>{{ conversation.customerName || 'Prospect Guest' }}</strong>
            </div>
            <div class="info-row" *ngIf="conversation.customerEmail">
              <span class="label">Email:</span>
              <span>{{ conversation.customerEmail }}</span>
            </div>
            <div class="info-row">
              <span class="label">Started:</span>
              <span>{{ conversation.startedAt | date:'medium' }}</span>
            </div>
          </div>

          <!-- Structured Inquiry State -->
          <div class="context-card">
            <div class="card-title-row">
              <h3><i class="bi bi-clipboard-check"></i> Structured Inquiry State</h3>
              <span class="badge" [ngClass]="conversation.inquiry?.complete ? 'badge-quoted' : 'badge-human'">
                {{ conversation.inquiry?.complete ? 'Complete' : 'Collecting Details' }}
              </span>
            </div>

            <div class="inquiry-grid" *ngIf="conversation.inquiry">
              <div class="inquiry-item">
                <span class="label">Event Type</span>
                <span class="value">{{ conversation.inquiry.eventType || 'Not specified' }}</span>
              </div>
              <div class="inquiry-item">
                <span class="label">Guest Count</span>
                <span class="value">{{ conversation.inquiry.guestCount ? conversation.inquiry.guestCount + ' guests' : 'TBD' }}</span>
              </div>
              <div class="inquiry-item">
                <span class="label">Event Date</span>
                <span class="value">{{ conversation.inquiry.eventDate ? (conversation.inquiry.eventDate | date:'mediumDate') : 'TBD' }}</span>
              </div>
              <div class="inquiry-item">
                <span class="label">Delivery City</span>
                <span class="value">{{ conversation.inquiry.deliveryCity || 'TBD' }}</span>
              </div>
              <div class="inquiry-item">
                <span class="label">Table Preference</span>
                <span class="value">{{ conversation.inquiry.tablePreference || 'TBD' }}</span>
              </div>
              <div class="inquiry-item">
                <span class="label">Delivery Time</span>
                <span class="value">{{ conversation.inquiry.deliveryTime || 'TBD' }}</span>
              </div>
            </div>

            <div class="missing-fields" *ngIf="conversation.inquiry?.missingFields?.length">
              <span class="missing-label"><i class="bi bi-info-circle"></i> Missing Required Fields:</span>
              <div class="chips">
                <span class="chip" *ngFor="let field of conversation.inquiry?.missingFields">{{ field }}</span>
              </div>
            </div>
          </div>

          <!-- Commercial Quote Draft -->
          <div class="context-card quote-card" *ngIf="conversation.quoteId">
            <div class="card-title-row">
              <h3><i class="bi bi-file-earmark-spreadsheet"></i> Commercial Quote Draft</h3>
              <span class="badge badge-quoted">DRAFT READY</span>
            </div>
            <p class="quote-desc">A formal draft quote has been generated with availability verified and margin sanity-checked.</p>
            <a [routerLink]="['/ai-sales/quotes', conversation.quoteId, 'review']" class="btn btn-primary btn-block">
              Review & Approve Quote &rarr;
            </a>
          </div>

          <!-- Internal Sales Notes -->
          <div class="context-card">
            <h3><i class="bi bi-journal-text"></i> Internal Sales Notes</h3>
            <p class="notes-text" *ngIf="conversation.internalNotes; else noNotes">
              {{ conversation.internalNotes }}
            </p>
            <ng-template #noNotes>
              <span class="text-muted">No special notes recorded.</span>
            </ng-template>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .conv-detail-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb a { color: #3b82f6; text-decoration: none; font-size: 0.85rem; font-weight: 500; }
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1rem;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .title-with-badge { display: flex; align-items: center; gap: 0.75rem; margin-top: 0.25rem; }
    h1 { font-size: 1.65rem; font-weight: 700; margin: 0; color: #0f172a; }
    .session-id { font-size: 0.8rem; color: #64748b; margin-top: 0.25rem; display: block; }
    .channel-tag {
      font-size: 0.75rem;
      background: #f1f5f9;
      color: #475569;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-weight: 600;
    }

    .header-controls { display: flex; gap: 0.6rem; align-items: center; }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.45rem 0.85rem;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      border: 1px solid transparent;
      text-decoration: none;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-primary:hover { background: #2563eb; }
    .btn-warning { background: #f59e0b; color: white; }
    .btn-warning:hover { background: #d97706; }
    .btn-success { background: #10b981; color: white; }
    .btn-success:hover { background: #059669; }
    .btn-danger-outline { background: transparent; border-color: #f87171; color: #dc2626; }
    .btn-danger-outline:hover { background: #fee2e2; }
    .btn-block { width: 100%; justify-content: center; margin-top: 0.5rem; }

    /* Console grid */
    .console-grid {
      display: grid;
      grid-template-columns: 3fr 2fr;
      gap: 1.5rem;
    }

    @media (max-width: 1024px) {
      .console-grid { grid-template-columns: 1fr; }
    }

    /* Left Transcript */
    .transcript-pane {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      display: flex;
      flex-direction: column;
      height: 680px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      background: #fafbfc;
    }

    .message-wrapper { display: flex; gap: 0.75rem; align-items: flex-start; max-width: 85%; }
    .sender-customer { align-self: flex-start; }
    .sender-ai, .sender-sales_user { align-self: flex-end; flex-direction: row-reverse; }
    .sender-system { align-self: center; max-width: 90%; }

    .avatar {
      width: 34px;
      height: 34px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.95rem;
      flex-shrink: 0;
    }
    .avatar-customer { background: #e0e7ff; color: #4338ca; }
    .avatar-ai { background: #ecfdf5; color: #047857; }
    .avatar-sales_user { background: #fef3c7; color: #b45309; }
    .avatar-system { background: #f1f5f9; color: #64748b; }

    .message-bubble {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 0.75rem 1rem;
      box-shadow: 0 1px 2px rgba(0,0,0,0.03);
    }
    .sender-customer .message-bubble { background: white; border-color: #e2e8f0; }
    .sender-ai .message-bubble { background: #f0fdf4; border-color: #bbf7d0; }
    .sender-sales_user .message-bubble { background: #eff6ff; border-color: #bfdbfe; }
    .sender-system .message-bubble { background: #f8fafc; border-color: #e2e8f0; font-size: 0.8rem; text-align: center; }

    .message-meta {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      font-size: 0.75rem;
      color: #64748b;
      margin-bottom: 0.25rem;
    }
    .message-text { font-size: 0.875rem; color: #1e293b; line-height: 1.45; white-space: pre-line; }

    .thinking-bubble { display: flex; align-items: center; gap: 0.5rem; color: #059669; font-size: 0.85rem; }
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #059669;
      animation: blink 1.2s infinite ease-in-out both;
    }
    @keyframes blink { 0%, 80%, 100% { opacity: 0.2; } 40% { opacity: 1; } }

    .composer-bar {
      display: flex;
      gap: 0.5rem;
      padding: 0.85rem 1rem;
      background: white;
      border-top: 1px solid #e2e8f0;
    }
    .composer-bar input {
      flex: 1;
      padding: 0.6rem 0.85rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
    }

    /* Right Context */
    .context-pane { display: flex; flex-direction: column; gap: 1rem; }
    .context-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.2rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .context-card h3 {
      font-size: 0.95rem;
      font-weight: 600;
      color: #0f172a;
      margin: 0 0 0.75rem 0;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .card-title-row h3 { margin: 0; }

    .info-row { display: flex; justify-content: space-between; font-size: 0.85rem; padding: 0.35rem 0; border-bottom: 1px solid #f8fafc; }
    .label { color: #64748b; font-weight: 500; }

    .inquiry-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
      margin-top: 0.5rem;
    }
    .inquiry-item {
      background: #f8fafc;
      padding: 0.5rem 0.75rem;
      border-radius: 6px;
      display: flex;
      flex-direction: column;
    }
    .inquiry-item .label { font-size: 0.7rem; text-transform: uppercase; color: #64748b; }
    .inquiry-item .value { font-size: 0.875rem; font-weight: 600; color: #1e293b; margin-top: 0.15rem; }

    .missing-fields { margin-top: 0.75rem; font-size: 0.8rem; color: #b45309; background: #fffbeb; padding: 0.5rem; border-radius: 6px; }
    .chips { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.35rem; }
    .chip { background: #fde68a; color: #78350f; padding: 0.15rem 0.45rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }

    .quote-card { border-left: 4px solid #10b981; }
    .quote-desc { font-size: 0.85rem; color: #475569; margin: 0 0 0.5rem 0; }
    .notes-text { font-size: 0.85rem; color: #334155; margin: 0; }

    .badge { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }
    .badge-active { background: #dbeafe; color: #1e40af; }
    .badge-human { background: #fef3c7; color: #92400e; }
    .badge-quoted { background: #dcfce7; color: #166534; }
    .badge-escalated { background: #fee2e2; color: #991b1b; }
  `]
})
export class AiConversationDetailComponent implements OnInit {
  conversationId!: string;
  conversation: AiSalesConversation | null = null;
  newMessageText = '';
  isAiThinking = false;
  isActionLoading = false;

  constructor(
    private route: ActivatedRoute,
    private aiSalesService: AiSalesService
  ) {}

  ngOnInit(): void {
    this.conversationId = this.route.snapshot.paramMap.get('id')!;
    this.loadConversation();
  }

  loadConversation(): void {
    this.aiSalesService.getConversation(this.conversationId).subscribe({
      next: (c) => this.conversation = c,
      error: (err) => console.error('Failed to load conversation', err)
    });
  }

  sendMessage(): void {
    if (!this.newMessageText.trim() || !this.conversation) return;
    const msg = this.newMessageText.trim();
    this.newMessageText = '';
    this.isAiThinking = true;

    this.aiSalesService.sendMessage(this.conversation.id, { message: msg }).subscribe({
      next: () => {
        this.isAiThinking = false;
        this.loadConversation();
      },
      error: (err) => {
        console.error('Failed to send message', err);
        this.isAiThinking = false;
      }
    });
  }

  takeOver(): void {
    if (!this.conversation) return;
    this.isActionLoading = true;
    this.aiSalesService.takeOverConversation(this.conversation.id).subscribe({
      next: () => {
        this.isActionLoading = false;
        this.loadConversation();
      },
      error: () => this.isActionLoading = false
    });
  }

  returnToAi(): void {
    if (!this.conversation) return;
    this.isActionLoading = true;
    this.aiSalesService.returnToAi(this.conversation.id).subscribe({
      next: () => {
        this.isActionLoading = false;
        this.loadConversation();
      },
      error: () => this.isActionLoading = false
    });
  }

  escalate(): void {
    if (!this.conversation) return;
    this.isActionLoading = true;
    this.aiSalesService.escalateConversation(this.conversation.id, 'CUSTOMER_REQUEST', 'Staff requested immediate escalation')
      .subscribe({
        next: () => {
          this.isActionLoading = false;
          this.loadConversation();
        },
        error: () => this.isActionLoading = false
      });
  }

  formatStatus(status: string): string {
    return status ? status.replace(/_/g, ' ') : '';
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'badge-active';
      case 'WAITING_FOR_HUMAN':
      case 'HUMAN_ACTIVE': return 'badge-human';
      case 'QUOTE_DRAFTED':
      case 'COMPLETED': return 'badge-quoted';
      case 'ESCALATED': return 'badge-escalated';
      default: return 'badge-channel';
    }
  }

  getSenderIcon(sender: string): string {
    switch (sender) {
      case 'CUSTOMER': return 'bi-person';
      case 'AI': return 'bi-robot';
      case 'SALES_USER': return 'bi-person-badge';
      default: return 'bi-gear';
    }
  }

  getSenderLabel(sender: string): string {
    switch (sender) {
      case 'CUSTOMER': return 'Customer';
      case 'AI': return 'RentFlow AI';
      case 'SALES_USER': return 'Sales Specialist';
      default: return 'System';
    }
  }
}
