import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiSalesConversation, AiConversationStatus } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-conversations-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="conv-container">
      <header class="page-header">
        <div>
          <div class="breadcrumb">
            <a routerLink="/ai-sales">&larr; AI Sales Console</a>
          </div>
          <h1>Sales Agent Conversations</h1>
          <p class="subtitle">Monitor real-time rental discovery dialogues, structured inquiries, and quote drafting pipelines.</p>
        </div>
        <div class="actions">
          <a routerLink="/portal/assistant" target="_blank" class="btn btn-primary">
            <i class="bi bi-chat-dots"></i> Launch Test Chat
          </a>
        </div>
      </header>

      <!-- Filter Controls -->
      <section class="filters-bar">
        <div class="search-box">
          <i class="bi bi-search search-icon"></i>
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (ngModelChange)="applyFilters()"
            placeholder="Search by customer name, email, or session ID..."
          />
        </div>

        <div class="filter-group">
          <label>Status:</label>
          <select [(ngModel)]="statusFilter" (change)="applyFilters()">
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="WAITING_FOR_CUSTOMER">Waiting for Customer</option>
            <option value="WAITING_FOR_HUMAN">Waiting for Human</option>
            <option value="HUMAN_ACTIVE">Human Active</option>
            <option value="QUOTE_DRAFTED">Quote Drafted</option>
            <option value="ESCALATED">Escalated</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Channel:</label>
          <select [(ngModel)]="channelFilter" (change)="applyFilters()">
            <option value="ALL">All Channels</option>
            <option value="CUSTOMER_PORTAL">Customer Portal</option>
            <option value="STOREFRONT">Storefront</option>
            <option value="WEB_CHAT">Web Chat</option>
            <option value="INTERNAL">Internal</option>
          </select>
        </div>
      </section>

      <!-- Conversations Table -->
      <div class="table-card">
        <div class="table-responsive" *ngIf="filteredConversations.length > 0; else noData">
          <table class="data-table">
            <thead>
              <tr>
                <th>Customer / Contact</th>
                <th>Channel</th>
                <th>Detected Intent</th>
                <th>Event & Guest Count</th>
                <th>Quote Draft</th>
                <th>Status</th>
                <th>Last Active</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of filteredConversations">
                <td>
                  <div class="cust-cell">
                    <strong>{{ c.customerName || 'Prospect Guest' }}</strong>
                    <span class="sub-text">{{ c.customerEmail || c.publicId }}</span>
                  </div>
                </td>
                <td>
                  <span class="badge badge-channel">{{ c.channel }}</span>
                </td>
                <td>
                  <span class="intent-pill">{{ c.detectedIntent || 'DISCOVERY' }}</span>
                </td>
                <td>
                  <div *ngIf="c.inquiry; else noInquiry">
                    <strong>{{ c.inquiry.eventType || 'Event' }}</strong>
                    <span class="sub-text" *ngIf="c.inquiry.guestCount">{{ c.inquiry.guestCount }} Guests &bull; {{ c.inquiry.deliveryCity || 'TBD' }}</span>
                  </div>
                  <ng-template #noInquiry><span class="sub-text">Inquiry in progress</span></ng-template>
                </td>
                <td>
                  <span *ngIf="c.quoteId" class="quote-link">
                    <a [routerLink]="['/ai-sales/quotes', c.quoteId, 'review']">
                      <i class="bi bi-file-earmark-text"></i> Review Draft
                    </a>
                  </span>
                  <span *ngIf="!c.quoteId" class="text-muted">-</span>
                </td>
                <td>
                  <span class="badge" [ngClass]="getStatusClass(c.status)">
                    {{ formatStatus(c.status) }}
                  </span>
                </td>
                <td>
                  <div class="time-cell">
                    <span>{{ c.lastMessageAt | date:'mediumDate' }}</span>
                    <span class="sub-text">{{ c.lastMessageAt | date:'shortTime' }}</span>
                  </div>
                </td>
                <td>
                  <a [routerLink]="['/ai-sales/conversations', c.id]" class="btn-open">
                    Open Console &rarr;
                  </a>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noData>
          <div class="empty-state">
            <i class="bi bi-inbox"></i>
            <h3>No conversations match your filter criteria</h3>
            <p>Try resetting filters or launch a new conversation from the customer portal.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .conv-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb a { color: #3b82f6; text-decoration: none; font-size: 0.85rem; font-weight: 500; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 700; color: #0f172a; margin: 0.25rem 0 0 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; font-size: 0.95rem; }

    .filters-bar {
      display: flex;
      gap: 1rem;
      align-items: center;
      background: white;
      padding: 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      margin-bottom: 1.25rem;
      flex-wrap: wrap;
    }

    .search-box {
      flex: 1;
      min-width: 260px;
      position: relative;
    }
    .search-icon {
      position: absolute;
      left: 10px;
      top: 50%;
      transform: translateY(-50%);
      color: #94a3b8;
    }
    .search-box input {
      width: 100%;
      padding: 0.5rem 0.75rem 0.5rem 2rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
      box-sizing: border-box;
    }

    .filter-group { display: flex; align-items: center; gap: 0.5rem; }
    .filter-group label { font-size: 0.85rem; color: #475569; font-weight: 500; }
    .filter-group select {
      padding: 0.5rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
      background: white;
    }

    .btn-primary {
      background: #3b82f6;
      color: white;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      text-decoration: none;
      font-size: 0.875rem;
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
    }
    .btn-primary:hover { background: #2563eb; }

    .table-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }

    .data-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .data-table th {
      text-align: left;
      padding: 0.75rem 1rem;
      background: #f8fafc;
      color: #64748b;
      font-weight: 600;
      font-size: 0.75rem;
      text-transform: uppercase;
      border-bottom: 1px solid #e2e8f0;
    }
    .data-table td { padding: 0.85rem 1rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }

    .cust-cell { display: flex; flex-direction: column; }
    .sub-text { font-size: 0.75rem; color: #94a3b8; }
    .time-cell { display: flex; flex-direction: column; font-size: 0.85rem; }

    .badge { display: inline-block; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }
    .badge-channel { background: #f1f5f9; color: #475569; }
    .intent-pill {
      font-family: monospace;
      font-size: 0.75rem;
      background: #f1f5f9;
      color: #334155;
      padding: 0.2rem 0.4rem;
      border-radius: 4px;
    }

    .badge-active { background: #dbeafe; color: #1e40af; }
    .badge-human { background: #fef3c7; color: #92400e; }
    .badge-quoted { background: #dcfce7; color: #166534; }
    .badge-escalated { background: #fee2e2; color: #991b1b; }

    .quote-link a { color: #059669; text-decoration: none; font-weight: 600; font-size: 0.825rem; }
    .quote-link a:hover { text-decoration: underline; }

    .btn-open { color: #2563eb; text-decoration: none; font-weight: 500; font-size: 0.85rem; }
    .btn-open:hover { text-decoration: underline; }

    .empty-state { text-align: center; padding: 3rem 1rem; color: #94a3b8; }
    .empty-state i { font-size: 2.5rem; margin-bottom: 0.5rem; display: block; }
    .empty-state h3 { color: #475569; margin-bottom: 0.25rem; }
  `]
})
export class AiConversationsListComponent implements OnInit {
  conversations: AiSalesConversation[] = [];
  filteredConversations: AiSalesConversation[] = [];
  searchQuery = '';
  statusFilter = 'ALL';
  channelFilter = 'ALL';
  isLoading = true;

  constructor(private aiSalesService: AiSalesService) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.isLoading = true;
    this.aiSalesService.getConversations().subscribe({
      next: (data) => {
        this.conversations = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load conversations', err);
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredConversations = this.conversations.filter(c => {
      const matchQuery = !q ||
        (c.customerName && c.customerName.toLowerCase().includes(q)) ||
        (c.customerEmail && c.customerEmail.toLowerCase().includes(q)) ||
        (c.publicId && c.publicId.toLowerCase().includes(q));

      const matchStatus = this.statusFilter === 'ALL' || c.status === this.statusFilter;
      const matchChannel = this.channelFilter === 'ALL' || c.channel === this.channelFilter;

      return matchQuery && matchStatus && matchChannel;
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
}
