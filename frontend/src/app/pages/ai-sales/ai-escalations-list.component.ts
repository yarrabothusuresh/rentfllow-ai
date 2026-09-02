import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiEscalation } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-escalations-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="escalations-container">
      <header class="page-header">
        <div>
          <div class="breadcrumb">
            <a routerLink="/ai-sales">&larr; AI Sales Console</a>
          </div>
          <h1>Human Escalation Queue</h1>
          <p class="subtitle">Conversations that triggered sales representative handoff or commercial exceptions.</p>
        </div>
      </header>

      <div class="cards-grid" *ngIf="escalations.length > 0; else noEscalations">
        <div class="escalation-card" *ngFor="let esc of escalations">
          <div class="card-top">
            <span class="badge" [ngClass]="'badge-priority-' + (esc.priority | lowercase)">
              {{ esc.priority }} PRIORITY
            </span>
            <span class="badge badge-status">{{ esc.status }}</span>
          </div>

          <div class="reason-row">
            <span class="reason-label"><i class="bi bi-tag"></i> Reason:</span>
            <strong>{{ esc.reason }}</strong>
          </div>

          <p class="summary-text">{{ esc.summary }}</p>

          <div class="meta-footer">
            <div class="timestamp">
              <i class="bi bi-clock"></i> {{ esc.createdAt | date:'medium' }}
            </div>
            <a [routerLink]="['/ai-sales/conversations', esc.conversationId]" class="btn btn-primary btn-sm">
              <i class="bi bi-chat-text"></i> Open Dialogue &rarr;
            </a>
          </div>
        </div>
      </div>

      <ng-template #noEscalations>
        <div class="empty-state">
          <i class="bi bi-shield-check"></i>
          <h3>Escalation queue is empty!</h3>
          <p>No active customer escalations requiring human intervention.</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .escalations-container {
      padding: 1.5rem;
      max-width: 1300px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb a { color: #3b82f6; text-decoration: none; font-size: 0.85rem; font-weight: 500; }
    .page-header { margin-bottom: 1.5rem; }
    h1 { font-size: 1.75rem; font-weight: 700; color: #0f172a; margin: 0.25rem 0 0 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; font-size: 0.95rem; }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
      gap: 1.25rem;
    }

    .escalation-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
      border-left: 4px solid #ef4444;
    }

    .card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }

    .badge {
      display: inline-block;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .badge-priority-high, .badge-priority-urgent { background: #fee2e2; color: #991b1b; }
    .badge-priority-medium { background: #fef3c7; color: #b45309; }
    .badge-priority-low { background: #f1f5f9; color: #475569; }
    .badge-status { background: #f1f5f9; color: #334155; }

    .reason-row { font-size: 0.85rem; color: #334155; margin-bottom: 0.5rem; }
    .reason-label { color: #64748b; margin-right: 0.35rem; }

    .summary-text {
      font-size: 0.9rem;
      color: #1e293b;
      line-height: 1.45;
      flex: 1;
      margin: 0 0 1rem 0;
    }

    .meta-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid #f1f5f9;
      padding-top: 0.75rem;
    }

    .timestamp { font-size: 0.8rem; color: #94a3b8; }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.4rem 0.85rem;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      border: 1px solid transparent;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-primary:hover { background: #2563eb; }

    .empty-state {
      text-align: center;
      padding: 3rem 1rem;
      background: white;
      border-radius: 10px;
      border: 1px solid #e2e8f0;
      color: #64748b;
    }
    .empty-state i { font-size: 3rem; color: #10b981; margin-bottom: 0.75rem; display: block; }
    .empty-state h3 { color: #1e293b; margin-bottom: 0.25rem; }
  `]
})
export class AiEscalationsListComponent implements OnInit {
  escalations: AiEscalation[] = [];

  constructor(private aiSalesService: AiSalesService) {}

  ngOnInit(): void {
    this.aiSalesService.getEscalations().subscribe({
      next: (data) => this.escalations = data,
      error: (err) => console.error('Failed to load escalations', err)
    });
  }
}
