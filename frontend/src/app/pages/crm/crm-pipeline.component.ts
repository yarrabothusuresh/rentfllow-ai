import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CrmService } from '../../services/crm.service';
import { CrmPipeline, CrmPipelineColumn, LeadStage, LeadSummary } from '../../models/crm.models';

@Component({
  selector: 'app-crm-pipeline',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="crm-pipeline-container">
      <!-- Breadcrumb & Header -->
      <div class="breadcrumb">
        <a routerLink="/dashboard/crm/dashboard">CRM</a> / <span>Sales Pipeline</span>
      </div>

      <header class="page-header">
        <div>
          <h1>Interactive Sales Pipeline</h1>
          <p class="subtitle">Kanban visual workflow from initial inquiry to closed deals.</p>
        </div>
        <div class="header-summary" *ngIf="pipeline">
          <div class="summary-pill">
            <span class="label">Total Inquiries:</span>
            <strong>{{ pipeline.totalLeads }}</strong>
          </div>
          <div class="summary-pill highlight">
            <span class="label">Pipeline Value:</span>
            <strong>{{ pipeline.totalPipelineValue | currency }}</strong>
          </div>
          <a routerLink="/dashboard/crm/leads" class="btn btn-secondary btn-sm">
            <i class="bi bi-list-ul"></i> Table View
          </a>
        </div>
      </header>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading sales pipeline board...</p>
      </div>

      <!-- Kanban Pipeline Columns -->
      <div *ngIf="!loading && pipeline" class="kanban-board">
        <div *ngFor="let col of pipeline.columns" class="kanban-column" [ngClass]="'col-' + col.stage.toLowerCase()">
          <!-- Column Header -->
          <div class="col-header">
            <div class="col-title">
              <span class="stage-dot"></span>
              <h3>{{ col.stageName }}</h3>
            </div>
            <div class="col-stats">
              <span class="count-badge">{{ col.count }}</span>
              <span class="value-sum" *ngIf="col.totalValue > 0">{{ col.totalValue | currency:'USD':'symbol':'1.0-0' }}</span>
            </div>
          </div>

          <!-- Column Cards Stream -->
          <div class="col-cards">
            <div *ngIf="col.leads.length === 0" class="col-empty">
              <p>No leads in this stage</p>
            </div>

            <div *ngFor="let lead of col.leads" class="lead-card" [class.is-overdue]="lead.overdueFollowUp">
              <div class="card-top">
                <span class="card-lead-num">{{ lead.leadNumber }}</span>
                <span class="card-priority" [ngClass]="'prio-' + lead.priority.toLowerCase()">{{ lead.priority }}</span>
              </div>

              <a [routerLink]="['/dashboard/crm/leads', lead.id]" class="card-contact-name">
                {{ lead.contactName }}
              </a>

              <div *ngIf="lead.companyName" class="card-company">
                <i class="bi bi-building"></i> {{ lead.companyName }}
              </div>

              <div class="card-event-info">
                <span><i class="bi bi-calendar-event"></i> {{ lead.eventName || lead.eventType || 'Event' }}</span>
                <span *ngIf="lead.eventDate">&bull; {{ lead.eventDate | date:'shortDate' }}</span>
              </div>

              <div class="card-footer">
                <span class="card-value">{{ lead.estimatedValue ? (lead.estimatedValue | currency:'USD':'symbol':'1.0-0') : '—' }}</span>
                <span class="card-rep">{{ lead.assignedSalesUserName ? (lead.assignedSalesUserName | slice:0:15) : 'Unassigned' }}</span>
              </div>

              <div *ngIf="lead.nextFollowUpAt" class="card-followup" [class.overdue]="lead.overdueFollowUp">
                <i class="bi bi-clock"></i> {{ lead.nextFollowUpAt | date:'short' }}
              </div>

              <!-- Quick Move Transitions -->
              <div class="card-quick-actions">
                <button *ngIf="col.stage === 'NEW'" (click)="quickTransition(lead.id, 'CONTACTED')" class="btn-quick" title="Mark Contacted">
                  &rarr; Contact
                </button>
                <button *ngIf="col.stage === 'CONTACTED' || col.stage === 'NEEDS_DISCOVERY'" (click)="quickQualify(lead.id)" class="btn-quick" title="Qualify">
                  &rarr; Qualify
                </button>
                <button *ngIf="col.stage === 'QUALIFIED'" (click)="quickCreateQuote(lead.id)" class="btn-quick" title="Prepare Quote">
                  &rarr; Quote
                </button>
                <button *ngIf="col.stage === 'QUOTE_PREPARED'" (click)="quickTransition(lead.id, 'QUOTE_SENT')" class="btn-quick" title="Mark Quote Sent">
                  &rarr; Send
                </button>
                <button *ngIf="col.stage === 'QUOTE_SENT' || col.stage === 'FOLLOW_UP' || col.stage === 'NEGOTIATION'" (click)="quickTransition(lead.id, 'WON')" class="btn-quick btn-won" title="Mark Won">
                  &check; Won
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .crm-pipeline-container {
      padding: 1.5rem;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb { font-size: 0.85rem; color: #64748b; margin-bottom: 0.75rem; }
    .breadcrumb a { color: #3b82f6; text-decoration: none; }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1rem;
    }
    h1 { font-size: 1.6rem; font-weight: 700; color: #0f172a; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; font-size: 0.9rem; }
    .header-summary { display: flex; align-items: center; gap: 1rem; }
    .summary-pill {
      background: white;
      border: 1px solid #e2e8f0;
      padding: 0.4rem 0.75rem;
      border-radius: 8px;
      font-size: 0.85rem;
      display: flex;
      gap: 0.4rem;
      align-items: center;
    }
    .summary-pill .label { color: #64748b; }
    .summary-pill.highlight { border-color: #86efac; background: #f0fdf4; color: #15803d; }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 0.85rem;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      border: 1px solid #cbd5e1;
      background: white;
      color: #334155;
    }

    .kanban-board {
      display: flex;
      gap: 1rem;
      overflow-x: auto;
      padding-bottom: 1.5rem;
      min-height: calc(100vh - 220px);
    }
    .kanban-column {
      flex: 0 0 290px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      display: flex;
      flex-direction: column;
      max-height: 80vh;
    }
    .col-header {
      padding: 0.85rem 1rem;
      border-bottom: 2px solid #e2e8f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: white;
      border-top-left-radius: 10px;
      border-top-right-radius: 10px;
    }
    .col-title { display: flex; align-items: center; gap: 0.45rem; }
    .stage-dot { width: 8px; height: 8px; border-radius: 50%; background: #3b82f6; }
    .col-title h3 { font-size: 0.875rem; font-weight: 700; color: #1e293b; margin: 0; }
    .col-stats { display: flex; align-items: center; gap: 0.5rem; }
    .count-badge { background: #f1f5f9; color: #475569; font-size: 0.75rem; font-weight: 700; padding: 0.1rem 0.45rem; border-radius: 10px; }
    .value-sum { font-size: 0.75rem; font-weight: 700; color: #059669; }

    .col-cards {
      padding: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      overflow-y: auto;
      flex: 1;
    }
    .col-empty { text-align: center; padding: 2rem 0.5rem; color: #94a3b8; font-size: 0.8rem; }

    .lead-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 0.85rem;
      box-shadow: 0 1px 2px rgba(0,0,0,0.04);
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      transition: transform 0.15s ease, box-shadow 0.15s ease;
    }
    .lead-card:hover { transform: translateY(-2px); box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
    .lead-card.is-overdue { border-left: 3px solid #ef4444; }

    .card-top { display: flex; justify-content: space-between; align-items: center; }
    .card-lead-num { font-family: monospace; font-size: 0.75rem; color: #64748b; }
    .card-priority { font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.35rem; border-radius: 4px; text-transform: uppercase; }
    .prio-low { background: #f1f5f9; color: #64748b; }
    .prio-normal { background: #e0f2fe; color: #0369a1; }
    .prio-high { background: #fed7aa; color: #c2410c; }
    .prio-urgent { background: #fee2e2; color: #b91c1c; }

    .card-contact-name { font-size: 0.9rem; font-weight: 700; color: #0f172a; text-decoration: none; }
    .card-contact-name:hover { color: #3b82f6; }
    .card-company { font-size: 0.75rem; color: #64748b; display: flex; align-items: center; gap: 0.3rem; }
    .card-event-info { font-size: 0.75rem; color: #475569; display: flex; align-items: center; gap: 0.3rem; }
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid #f1f5f9;
      padding-top: 0.4rem;
      margin-top: 0.2rem;
      font-size: 0.75rem;
    }
    .card-value { font-weight: 700; color: #059669; }
    .card-rep { color: #64748b; }
    .card-followup { font-size: 0.7rem; color: #64748b; display: flex; align-items: center; gap: 0.25rem; }
    .card-followup.overdue { color: #dc2626; font-weight: 700; }

    .card-quick-actions { display: flex; justify-content: flex-end; gap: 0.35rem; margin-top: 0.3rem; }
    .btn-quick {
      background: #f1f5f9; border: 1px solid #cbd5e1; border-radius: 4px; padding: 0.15rem 0.45rem;
      font-size: 0.7rem; font-weight: 600; color: #334155; cursor: pointer;
    }
    .btn-quick:hover { background: #e2e8f0; }
    .btn-won { background: #dcfce7; border-color: #86efac; color: #15803d; }
    .btn-won:hover { background: #bbf7d0; }

    .loading-state { text-align: center; padding: 4rem 1rem; color: #64748b; }
    .spinner {
      width: 40px; height: 40px; border: 3px solid #e2e8f0; border-top-color: #3b82f6;
      border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem auto;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class CrmPipelineComponent implements OnInit {
  pipeline: CrmPipeline | null = null;
  loading = true;

  constructor(private crmService: CrmService) {}

  ngOnInit(): void {
    this.loadPipeline();
  }

  loadPipeline(): void {
    this.loading = true;
    this.crmService.getPipeline().subscribe({
      next: (data) => {
        this.pipeline = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load pipeline', err);
        this.loading = false;
      }
    });
  }

  quickTransition(leadId: string, stage: LeadStage): void {
    this.crmService.transitionStage(leadId, stage).subscribe({
      next: () => this.loadPipeline(),
      error: (err) => alert(err.error?.error || 'Transition failed')
    });
  }

  quickQualify(leadId: string): void {
    this.crmService.qualifyLead(leadId).subscribe({
      next: () => this.loadPipeline(),
      error: (err) => alert(err.error?.error || 'Qualification failed')
    });
  }

  quickCreateQuote(leadId: string): void {
    this.crmService.createQuoteDraft(leadId).subscribe({
      next: () => this.loadPipeline(),
      error: (err) => alert(err.error?.error || 'Failed to create quote')
    });
  }
}
