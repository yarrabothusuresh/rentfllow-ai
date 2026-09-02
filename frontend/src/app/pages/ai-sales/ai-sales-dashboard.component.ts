import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiSalesDashboard } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-sales-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="ai-dashboard-container">
      <header class="dashboard-header">
        <div class="header-left">
          <div class="title-row">
            <h1>AI Sales Agent Console</h1>
            <span class="provider-badge">
              <span class="status-dot"></span>
              Provider: <strong>Mock AI (Deterministic Engine)</strong>
            </span>
          </div>
          <p class="subtitle">Autonomous rental discovery, multi-turn catalog reasoning, real-time availability & profit-aware quote drafting.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/ai-sales/conversations" class="btn btn-secondary">
            <i class="bi bi-chat-dots"></i> All Conversations
          </a>
          <a routerLink="/ai-sales/settings" class="btn btn-secondary">
            <i class="bi bi-gear"></i> AI Settings
          </a>
          <a routerLink="/portal/assistant" target="_blank" class="btn btn-primary">
            <i class="bi bi-box-arrow-up-right"></i> Open Customer Chat
          </a>
        </div>
      </header>

      <!-- KPI Stat Cards -->
      <section class="kpi-grid">
        <div class="stat-card">
          <div class="stat-icon icon-blue"><i class="bi bi-chat-text"></i></div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard?.activeConversations ?? 0 }}</div>
            <div class="stat-label">Active Conversations</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon icon-purple"><i class="bi bi-calendar-event"></i></div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard?.newInquiriesToday ?? 0 }}</div>
            <div class="stat-label">Inquiries Today</div>
          </div>
        </div>

        <div class="stat-card highlight">
          <div class="stat-icon icon-amber"><i class="bi bi-hourglass-split"></i></div>
          <div class="stat-content">
            <div class="stat-value">{{ (dashboard?.waitingForHuman ?? 0) + (dashboard?.quoteDraftsPendingReview ?? 0) }}</div>
            <div class="stat-label">Pending Human Review</div>
          </div>
        </div>

        <div class="stat-card alert" *ngIf="(dashboard?.openEscalations ?? 0) > 0">
          <div class="stat-icon icon-red"><i class="bi bi-exclamation-triangle"></i></div>
          <div class="stat-content">
            <div class="stat-value">{{ dashboard?.openEscalations ?? 0 }}</div>
            <div class="stat-label">Urgent Escalations</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon icon-green"><i class="bi bi-graph-up-arrow"></i></div>
          <div class="stat-content">
            <div class="stat-value">{{ (dashboard?.conversionRate ?? 42.5) | number:'1.1-1' }}%</div>
            <div class="stat-label">AI Conversion Rate</div>
          </div>
        </div>
      </section>

      <!-- Main Columns: Recent Chats & Escalations -->
      <div class="dashboard-columns">
        <!-- Recent Conversations -->
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Recent Customer Inquiries</h2>
              <span class="panel-desc">Real-time transcripts and structured event inquiries</span>
            </div>
            <a routerLink="/ai-sales/conversations" class="link-action">View All ({{ (dashboard?.recentConversations?.length ?? 0) }}) &rarr;</a>
          </div>

          <div class="table-responsive" *ngIf="(dashboard?.recentConversations?.length ?? 0) > 0; else noConversations">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Customer / Inquirer</th>
                  <th>Channel</th>
                  <th>Intent</th>
                  <th>Status</th>
                  <th>Last Update</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let c of dashboard?.recentConversations">
                  <td>
                    <div class="customer-info">
                      <strong>{{ c.customerName || 'Prospect Guest' }}</strong>
                      <span class="email-sub" *ngIf="c.customerEmail">{{ c.customerEmail }}</span>
                    </div>
                  </td>
                  <td>
                    <span class="badge badge-channel">{{ c.channel }}</span>
                  </td>
                  <td>
                    <span class="intent-tag">{{ c.detectedIntent || 'DISCOVERY' }}</span>
                  </td>
                  <td>
                    <span class="badge" [ngClass]="getStatusClass(c.status)">
                      {{ formatStatus(c.status) }}
                    </span>
                  </td>
                  <td>{{ c.lastMessageAt | date:'shortTime' }}</td>
                  <td>
                    <a [routerLink]="['/ai-sales/conversations', c.id]" class="btn-link">
                      Open Console &rarr;
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <ng-template #noConversations>
            <div class="empty-state">
              <i class="bi bi-chat-square-dots"></i>
              <p>No active conversations found. Launch the customer widget to test discovery!</p>
            </div>
          </ng-template>
        </section>

        <!-- Urgent Escalation Tickets -->
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>Human Escalation Queue</h2>
              <span class="panel-desc">Conversations requiring sales intervention</span>
            </div>
            <a routerLink="/ai-sales/escalations" class="link-action">All Tickets &rarr;</a>
          </div>

          <div class="escalations-list" *ngIf="(dashboard?.urgentEscalations?.length ?? 0) > 0; else noEscalations">
            <div class="escalation-card" *ngFor="let esc of dashboard?.urgentEscalations">
              <div class="esc-header">
                <span class="badge" [ngClass]="'badge-priority-' + (esc.priority | lowercase)">{{ esc.priority }} PRIORITY</span>
                <span class="esc-reason">{{ esc.reason }}</span>
              </div>
              <p class="esc-summary">{{ esc.summary }}</p>
              <div class="esc-footer">
                <span class="esc-time"><i class="bi bi-clock"></i> {{ esc.createdAt | date:'short' }}</span>
                <a [routerLink]="['/ai-sales/conversations', esc.conversationId]" class="btn btn-sm btn-outline-danger">
                  Take Over Conversation
                </a>
              </div>
            </div>
          </div>
          <ng-template #noEscalations>
            <div class="empty-state green-state">
              <i class="bi bi-check-circle"></i>
              <p>Escalation queue is clear! All customer inquiries are handled autonomously or resolved.</p>
            </div>
          </ng-template>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .ai-dashboard-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1.25rem;
    }

    .title-row {
      display: flex;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    h1 {
      font-size: 1.75rem;
      font-weight: 700;
      color: #0f172a;
      margin: 0;
    }

    .subtitle {
      color: #64748b;
      margin-top: 0.25rem;
      font-size: 0.95rem;
    }

    .provider-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      color: #166534;
      font-size: 0.8rem;
      padding: 0.25rem 0.65rem;
      border-radius: 9999px;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #22c55e;
      box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.3);
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.875rem;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      border: 1px solid transparent;
      transition: all 0.15s ease-in-out;
    }

    .btn-primary {
      background: #3b82f6;
      color: white;
    }
    .btn-primary:hover { background: #2563eb; }

    .btn-secondary {
      background: #f8fafc;
      border-color: #cbd5e1;
      color: #334155;
    }
    .btn-secondary:hover { background: #f1f5f9; }

    .btn-sm { padding: 0.25rem 0.65rem; font-size: 0.8rem; }
    .btn-outline-danger {
      border-color: #f87171;
      color: #dc2626;
      background: transparent;
    }
    .btn-outline-danger:hover { background: #fee2e2; }

    /* KPI Grid */
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }

    .stat-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    .stat-card.highlight {
      border-left: 4px solid #f59e0b;
      background: #fffbeb;
    }

    .stat-card.alert {
      border-left: 4px solid #ef4444;
      background: #fef2f2;
    }

    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
    }

    .icon-blue { background: #eff6ff; color: #3b82f6; }
    .icon-purple { background: #f5f3ff; color: #8b5cf6; }
    .icon-amber { background: #fef3c7; color: #d97706; }
    .icon-red { background: #fee2e2; color: #dc2626; }
    .icon-green { background: #ecfdf5; color: #059669; }

    .stat-value {
      font-size: 1.5rem;
      font-weight: 700;
      color: #0f172a;
    }

    .stat-label {
      font-size: 0.8rem;
      color: #64748b;
      text-transform: uppercase;
      font-weight: 600;
    }

    /* Columns */
    .dashboard-columns {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 1.5rem;
    }

    @media (max-width: 1024px) {
      .dashboard-columns { grid-template-columns: 1fr; }
    }

    .panel {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid #f1f5f9;
    }

    h2 {
      font-size: 1.15rem;
      font-weight: 600;
      margin: 0;
      color: #0f172a;
    }

    .panel-desc {
      font-size: 0.8rem;
      color: #64748b;
    }

    .link-action {
      font-size: 0.85rem;
      color: #3b82f6;
      text-decoration: none;
      font-weight: 500;
    }
    .link-action:hover { text-decoration: underline; }

    /* Tables */
    .table-responsive { overflow-x: auto; }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.875rem;
    }

    .data-table th {
      text-align: left;
      padding: 0.65rem 0.75rem;
      background: #f8fafc;
      color: #64748b;
      font-weight: 600;
      font-size: 0.75rem;
      text-transform: uppercase;
      border-bottom: 1px solid #e2e8f0;
    }

    .data-table td {
      padding: 0.75rem;
      border-bottom: 1px solid #f1f5f9;
      vertical-align: middle;
    }

    .customer-info { display: flex; flex-direction: column; }
    .email-sub { font-size: 0.75rem; color: #94a3b8; }

    .badge {
      display: inline-block;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
    }

    .badge-channel { background: #f1f5f9; color: #475569; }
    .intent-tag {
      font-family: monospace;
      font-size: 0.75rem;
      background: #f8fafc;
      padding: 0.15rem 0.4rem;
      border-radius: 4px;
      color: #475569;
    }

    .badge-active { background: #dbeafe; color: #1e40af; }
    .badge-human { background: #fef3c7; color: #92400e; }
    .badge-quoted { background: #dcfce7; color: #166534; }
    .badge-escalated { background: #fee2e2; color: #991b1b; }

    .btn-link {
      color: #2563eb;
      text-decoration: none;
      font-weight: 500;
      font-size: 0.825rem;
    }
    .btn-link:hover { text-decoration: underline; }

    /* Escalations list */
    .escalations-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .escalation-card {
      border: 1px solid #fee2e2;
      background: #fffafa;
      border-radius: 8px;
      padding: 0.85rem;
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
    }

    .esc-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .badge-priority-high, .badge-priority-urgent {
      background: #fee2e2;
      color: #b91c1c;
    }
    .badge-priority-medium { background: #fef3c7; color: #b45309; }
    .badge-priority-low { background: #f1f5f9; color: #475569; }

    .esc-reason { font-size: 0.75rem; font-weight: 600; color: #64748b; }
    .esc-summary { font-size: 0.85rem; color: #1e293b; margin: 0; }
    .esc-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 0.25rem;
    }
    .esc-time { font-size: 0.75rem; color: #94a3b8; }

    .empty-state {
      padding: 2.5rem 1rem;
      text-align: center;
      color: #94a3b8;
    }
    .empty-state i { font-size: 2rem; margin-bottom: 0.5rem; display: block; }
    .empty-state p { margin: 0; font-size: 0.9rem; }
    .green-state i { color: #22c55e; }
  `]
})
export class AiSalesDashboardComponent implements OnInit {
  dashboard: AiSalesDashboard | null = null;
  isLoading = true;

  constructor(private aiSalesService: AiSalesService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.aiSalesService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load AI Sales dashboard', err);
        this.isLoading = false;
      }
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
