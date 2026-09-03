import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CrmService } from '../../services/crm.service';
import { CrmDashboard, LeadFollowUp, LeadSummary } from '../../models/crm.models';

@Component({
  selector: 'app-crm-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="crm-dashboard-container">
      <!-- Header -->
      <header class="page-header">
        <div>
          <h1>Rental CRM & Lead Management</h1>
          <p class="subtitle">Track inquiries, qualify opportunities, schedule follow-ups, and convert leads into rental quotes.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/dashboard/crm/pipeline" class="btn btn-secondary">
            <i class="bi bi-kanban"></i> Sales Pipeline
          </a>
          <a routerLink="/dashboard/crm/leads" class="btn btn-secondary">
            <i class="bi bi-list-ul"></i> All Leads
          </a>
          <button (click)="openCreateModal()" class="btn btn-primary">
            <i class="bi bi-plus-circle-fill"></i> Capture New Lead
          </button>
        </div>
      </header>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading CRM intelligence...</p>
      </div>

      <!-- Dashboard Content -->
      <div *ngIf="!loading && dashboard" class="dashboard-content">
        <!-- Critical Attention Alert -->
        <div *ngIf="dashboard.overdueFollowUps > 0 || dashboard.unassignedLeads > 0" class="alerts-banner">
          <div *ngIf="dashboard.overdueFollowUps > 0" class="alert-item alert-danger">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <span><strong>{{ dashboard.overdueFollowUps }} overdue follow-up{{ dashboard.overdueFollowUps > 1 ? 's' : '' }}</strong> require immediate attention.</span>
          </div>
          <div *ngIf="dashboard.unassignedLeads > 0" class="alert-item alert-warning">
            <i class="bi bi-person-exclamation"></i>
            <span><strong>{{ dashboard.unassignedLeads }} unassigned lead{{ dashboard.unassignedLeads > 1 ? 's' : '' }}</strong> waiting in the sales queue.</span>
          </div>
        </div>

        <!-- Metric Cards Grid -->
        <div class="metrics-grid">
          <div class="metric-card card-new">
            <div class="metric-title">New Inquiries</div>
            <div class="metric-value">{{ dashboard.newLeads }}</div>
            <div class="metric-sub">Awaiting initial contact</div>
          </div>

          <div class="metric-card card-unassigned">
            <div class="metric-title">Unassigned Leads</div>
            <div class="metric-value">{{ dashboard.unassignedLeads }}</div>
            <div class="metric-sub">Needs sales assignment</div>
          </div>

          <div class="metric-card card-active">
            <div class="metric-title">My Active Leads</div>
            <div class="metric-value">{{ dashboard.myActiveLeads }}</div>
            <div class="metric-sub">Assigned to you</div>
          </div>

          <div class="metric-card card-qualified">
            <div class="metric-title">Qualified Leads</div>
            <div class="metric-value">{{ dashboard.qualifiedLeads }}</div>
            <div class="metric-sub">Ready for quote builder</div>
          </div>

          <div class="metric-card card-quote-prep">
            <div class="metric-title">Quotes Prepared</div>
            <div class="metric-value">{{ dashboard.quotePreparedLeads }}</div>
            <div class="metric-sub">Draft proposals ready</div>
          </div>

          <div class="metric-card card-quote-sent">
            <div class="metric-title">Quotes Sent</div>
            <div class="metric-value">{{ dashboard.quoteSentLeads }}</div>
            <div class="metric-sub">Awaiting customer approval</div>
          </div>

          <div class="metric-card card-due-today">
            <div class="metric-title">Follow-Ups Due Today</div>
            <div class="metric-value">{{ dashboard.followUpsDueToday }}</div>
            <div class="metric-sub">Scheduled calls & emails</div>
          </div>

          <div class="metric-card card-won">
            <div class="metric-title">Closed Won</div>
            <div class="metric-value">{{ dashboard.wonLeads }}</div>
            <div class="metric-sub">Confirmed rental bookings</div>
          </div>
        </div>

        <!-- Main 2-Column Split: Unassigned Queue & Priority Follow-Ups -->
        <div class="split-row">
          <!-- Unassigned Queue -->
          <div class="panel-card">
            <div class="panel-header">
              <h3><i class="bi bi-person-plus"></i> Unassigned Leads Queue</h3>
              <a routerLink="/dashboard/crm/leads" [queryParams]="{ unassigned: true }" class="panel-link">View All</a>
            </div>

            <div *ngIf="dashboard.unassignedQueue.length === 0" class="empty-panel">
              <i class="bi bi-check2-circle"></i>
              <p>Great job! All incoming leads have been assigned.</p>
            </div>

            <div *ngIf="dashboard.unassignedQueue.length > 0" class="lead-list-table">
              <div *ngFor="let lead of dashboard.unassignedQueue" class="lead-row">
                <div class="lead-info">
                  <div class="lead-main">
                    <span class="lead-num">{{ lead.leadNumber }}</span>
                    <strong>{{ lead.contactName }}</strong>
                    <span *ngIf="lead.companyName" class="company-tag">{{ lead.companyName }}</span>
                  </div>
                  <div class="lead-sub">
                    <span class="source-badge">{{ lead.source }}</span>
                    <span><i class="bi bi-calendar-event"></i> {{ lead.eventDate || 'Date TBD' }}</span>
                    <span *ngIf="lead.estimatedValue" class="value-tag">{{ lead.estimatedValue | currency }}</span>
                  </div>
                </div>
                <div class="lead-actions">
                  <a [routerLink]="['/dashboard/crm/leads', lead.id]" class="btn btn-sm btn-outline-primary">
                    Open & Assign &rarr;
                  </a>
                </div>
              </div>
            </div>
          </div>

          <!-- Priority Follow-Ups -->
          <div class="panel-card">
            <div class="panel-header">
              <h3><i class="bi bi-clock-history"></i> Priority Follow-Up Tasks</h3>
            </div>

            <div *ngIf="dashboard.priorityFollowUps.length === 0" class="empty-panel">
              <i class="bi bi-calendar-check"></i>
              <p>No open follow-up tasks scheduled for today.</p>
            </div>

            <div *ngIf="dashboard.priorityFollowUps.length > 0" class="followup-list">
              <div *ngFor="let fu of dashboard.priorityFollowUps" class="followup-row" [class.is-overdue]="fu.overdue">
                <div class="fu-icon">
                  <i *ngIf="fu.type === 'CALL'" class="bi bi-telephone-fill"></i>
                  <i *ngIf="fu.type === 'EMAIL'" class="bi bi-envelope-fill"></i>
                  <i *ngIf="fu.type === 'MEETING'" class="bi bi-people-fill"></i>
                  <i *ngIf="fu.type === 'QUOTE'" class="bi bi-file-earmark-text-fill"></i>
                  <i *ngIf="fu.type === 'GENERAL' || fu.type === 'OTHER' || fu.type === 'SMS'" class="bi bi-check-square"></i>
                </div>
                <div class="fu-body">
                  <div class="fu-title">
                    <strong>{{ fu.title }}</strong>
                    <span *ngIf="fu.overdue" class="badge-overdue">OVERDUE</span>
                  </div>
                  <div class="fu-meta">
                    <a [routerLink]="['/dashboard/crm/leads', fu.leadId]">{{ fu.leadNumber }} &bull; {{ fu.contactName }}</a>
                    <span>&bull; Due: {{ fu.dueAt | date:'short' }}</span>
                  </div>
                  <p *ngIf="fu.notes" class="fu-notes">{{ fu.notes }}</p>
                </div>
                <div class="fu-action">
                  <button (click)="completeTask(fu.id)" class="btn btn-sm btn-success" title="Mark Task Completed">
                    <i class="bi bi-check2"></i> Complete
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Leads Table -->
        <div class="panel-card full-width-panel">
          <div class="panel-header">
            <h3><i class="bi bi-activity"></i> Recent Inquiries</h3>
            <a routerLink="/dashboard/crm/leads" class="panel-link">View All Leads &rarr;</a>
          </div>

          <div class="table-responsive">
            <table class="table">
              <thead>
                <tr>
                  <th>Lead #</th>
                  <th>Contact</th>
                  <th>Company</th>
                  <th>Event / Type</th>
                  <th>Event Date</th>
                  <th>Stage</th>
                  <th>Est. Value</th>
                  <th>Assigned Rep</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let l of dashboard.recentLeads">
                  <td><strong>{{ l.leadNumber }}</strong></td>
                  <td>{{ l.contactName }}</td>
                  <td>{{ l.companyName || '—' }}</td>
                  <td>{{ l.eventName || l.eventType || 'Event Inquiry' }}</td>
                  <td>{{ l.eventDate ? (l.eventDate | date:'mediumDate') : 'TBD' }}</td>
                  <td><span class="stage-pill" [ngClass]="'stage-' + l.stage.toLowerCase()">{{ l.stage }}</span></td>
                  <td><strong>{{ l.estimatedValue ? (l.estimatedValue | currency) : '—' }}</strong></td>
                  <td>{{ l.assignedSalesUserName || 'Unassigned' }}</td>
                  <td>
                    <a [routerLink]="['/dashboard/crm/leads', l.id]" class="btn btn-sm btn-primary">
                      Open Console
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Quick Create Lead Modal -->
      <div *ngIf="showModal" class="modal-backdrop">
        <div class="modal-content">
          <div class="modal-header">
            <h2>Capture New Sales Lead</h2>
            <button (click)="closeCreateModal()" class="btn-close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group col">
                <label>First Name *</label>
                <input type="text" [(ngModel)]="newLead.firstName" placeholder="First Name" />
              </div>
              <div class="form-group col">
                <label>Last Name</label>
                <input type="text" [(ngModel)]="newLead.lastName" placeholder="Last Name" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group col">
                <label>Email *</label>
                <input type="email" [(ngModel)]="newLead.email" placeholder="client@example.com" />
              </div>
              <div class="form-group col">
                <label>Phone</label>
                <input type="text" [(ngModel)]="newLead.phone" placeholder="(555) 000-0000" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group col">
                <label>Company Name</label>
                <input type="text" [(ngModel)]="newLead.companyName" placeholder="Company LLC (optional)" />
              </div>
              <div class="form-group col">
                <label>Lead Source</label>
                <select [(ngModel)]="newLead.source">
                  <option value="PHONE">Phone Inquiry</option>
                  <option value="EMAIL">Email Inquiry</option>
                  <option value="WALK_IN">Walk-In</option>
                  <option value="REFERRAL">Referral</option>
                  <option value="SOCIAL">Social Media</option>
                  <option value="MANUAL">Manual Entry</option>
                  <option value="STOREFRONT_REQUEST">Storefront Request</option>
                  <option value="WEBSITE_INQUIRY">Website Form</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group col">
                <label>Event Name / Occasion</label>
                <input type="text" [(ngModel)]="newLead.eventName" placeholder="e.g. Smith Wedding Reception" />
              </div>
              <div class="form-group col">
                <label>Event Date</label>
                <input type="date" [(ngModel)]="newLead.eventDate" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group col">
                <label>Guest Count</label>
                <input type="number" [(ngModel)]="newLead.guestCount" placeholder="100" />
              </div>
              <div class="form-group col">
                <label>Estimated Value ($)</label>
                <input type="number" [(ngModel)]="newLead.estimatedValue" placeholder="3500.00" />
              </div>
            </div>

            <div class="form-group">
              <label>Customer Inquired Needs / Equipment Notes</label>
              <textarea [(ngModel)]="newLead.customerNotes" rows="3" placeholder="Notes about tables, chairs, tents, or delivery details..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="closeCreateModal()" class="btn btn-secondary">Cancel</button>
            <button (click)="saveLead()" class="btn btn-primary" [disabled]="!newLead.firstName || (!newLead.email && !newLead.phone)">
              Save & Open Lead
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .crm-dashboard-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1rem;
    }
    h1 { font-size: 1.75rem; font-weight: 700; color: #0f172a; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.35rem; font-size: 0.95rem; }
    .header-actions { display: flex; gap: 0.75rem; }

    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      border: 1px solid transparent;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-primary:hover { background: #2563eb; }
    .btn-secondary { background: white; border: 1px solid #cbd5e1; color: #334155; }
    .btn-secondary:hover { background: #f8fafc; }
    .btn-success { background: #10b981; color: white; }
    .btn-success:hover { background: #059669; }
    .btn-sm { padding: 0.3rem 0.65rem; font-size: 0.8rem; }
    .btn-outline-primary { background: transparent; border: 1px solid #3b82f6; color: #3b82f6; }
    .btn-outline-primary:hover { background: #eff6ff; }

    .alerts-banner { display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1.5rem; }
    .alert-item {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      font-size: 0.9rem;
    }
    .alert-danger { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; }
    .alert-warning { background: #fef3c7; border: 1px solid #fde68a; color: #92400e; }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .metric-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem 1rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
      display: flex;
      flex-direction: column;
    }
    .metric-title { font-size: 0.8rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
    .metric-value { font-size: 1.85rem; font-weight: 700; color: #0f172a; margin: 0.35rem 0; }
    .metric-sub { font-size: 0.75rem; color: #94a3b8; }

    .card-new { border-left: 4px solid #3b82f6; }
    .card-unassigned { border-left: 4px solid #f59e0b; }
    .card-active { border-left: 4px solid #8b5cf6; }
    .card-qualified { border-left: 4px solid #06b6d4; }
    .card-quote-prep { border-left: 4px solid #6366f1; }
    .card-quote-sent { border-left: 4px solid #ec4899; }
    .card-due-today { border-left: 4px solid #e11d48; }
    .card-won { border-left: 4px solid #10b981; }

    .split-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
      margin-bottom: 1.5rem;
    }
    @media (max-width: 900px) {
      .split-row { grid-template-columns: 1fr; }
    }

    .panel-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    }
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      border-bottom: 1px solid #f1f5f9;
      padding-bottom: 0.65rem;
    }
    .panel-header h3 { font-size: 1.05rem; font-weight: 600; color: #0f172a; margin: 0; display: flex; align-items: center; gap: 0.5rem; }
    .panel-link { color: #3b82f6; font-size: 0.85rem; text-decoration: none; font-weight: 500; }

    .empty-panel { text-align: center; padding: 2rem 1rem; color: #94a3b8; }
    .empty-panel i { font-size: 2rem; color: #cbd5e1; margin-bottom: 0.5rem; display: block; }

    .lead-list-table { display: flex; flex-direction: column; gap: 0.75rem; }
    .lead-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
    }
    .lead-info { display: flex; flex-direction: column; gap: 0.25rem; }
    .lead-main { display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem; }
    .lead-num { color: #64748b; font-family: monospace; font-size: 0.8rem; }
    .company-tag { font-size: 0.8rem; color: #64748b; }
    .lead-sub { display: flex; align-items: center; gap: 0.75rem; font-size: 0.75rem; color: #64748b; }
    .source-badge { background: #e2e8f0; color: #334155; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600; }
    .value-tag { color: #059669; font-weight: 600; }

    .followup-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .followup-row {
      display: flex;
      gap: 0.75rem;
      align-items: flex-start;
      padding: 0.75rem;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
    }
    .followup-row.is-overdue { border-left: 4px solid #ef4444; background: #fff5f5; }
    .fu-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #e2e8f0;
      color: #3b82f6;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.9rem;
      flex-shrink: 0;
    }
    .fu-body { flex: 1; }
    .fu-title { font-size: 0.875rem; color: #1e293b; display: flex; align-items: center; gap: 0.5rem; }
    .badge-overdue { background: #fee2e2; color: #991b1b; font-size: 0.7rem; font-weight: 700; padding: 0.1rem 0.4rem; border-radius: 4px; }
    .fu-meta { font-size: 0.75rem; color: #64748b; margin-top: 0.2rem; }
    .fu-meta a { color: #3b82f6; text-decoration: none; font-weight: 500; }
    .fu-notes { font-size: 0.75rem; color: #475569; margin: 0.25rem 0 0 0; }

    .full-width-panel { margin-top: 1.5rem; }
    .table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .table th { text-align: left; padding: 0.65rem 0.75rem; background: #f8fafc; color: #64748b; font-weight: 600; font-size: 0.75rem; text-transform: uppercase; border-bottom: 1px solid #e2e8f0; }
    .table td { padding: 0.75rem; border-bottom: 1px solid #f1f5f9; color: #1e293b; vertical-align: middle; }

    .stage-pill {
      display: inline-block;
      padding: 0.2rem 0.55rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    .stage-new { background: #dbeafe; color: #1e40af; }
    .stage-contacted { background: #e0e7ff; color: #3730a3; }
    .stage-needs_discovery { background: #ede9fe; color: #5b21b6; }
    .stage-qualified { background: #cffafe; color: #155e75; }
    .stage-quote_prepared { background: #fef3c7; color: #92400e; }
    .stage-quote_sent { background: #fce7f3; color: #9d174d; }
    .stage-follow_up { background: #fed7aa; color: #9a3412; }
    .stage-negotiation { background: #fbcfe8; color: #831843; }
    .stage-won { background: #d1fae5; color: #065f46; }
    .stage-lost { background: #fee2e2; color: #991b1b; }
    .stage-disqualified { background: #f1f5f9; color: #475569; }

    /* Modal */
    .modal-backdrop {
      position: fixed;
      top: 0; left: 0; width: 100%; height: 100%;
      background: rgba(15, 23, 42, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }
    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 650px;
      box-shadow: 0 10px 25px rgba(0,0,0,0.15);
      overflow: hidden;
    }
    .modal-header {
      padding: 1.25rem 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .modal-header h2 { font-size: 1.25rem; font-weight: 700; color: #0f172a; margin: 0; }
    .btn-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #94a3b8; }
    .modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; max-height: 70vh; overflow-y: auto; }
    .modal-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid #e2e8f0;
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      background: #f8fafc;
    }

    .form-row { display: flex; gap: 1rem; }
    .form-group.col { flex: 1; }
    .form-group label { display: block; font-size: 0.8rem; font-weight: 600; color: #334155; margin-bottom: 0.35rem; }
    .form-group input, .form-group select, .form-group textarea {
      width: 100%;
      padding: 0.55rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
      box-sizing: border-box;
    }

    .loading-state { text-align: center; padding: 4rem 1rem; color: #64748b; }
    .spinner {
      width: 40px; height: 40px; border: 3px solid #e2e8f0; border-top-color: #3b82f6;
      border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem auto;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class CrmDashboardComponent implements OnInit {
  dashboard: CrmDashboard | null = null;
  loading = true;
  showModal = false;

  newLead: any = {
    firstName: '',
    lastName: '',
    companyName: '',
    email: '',
    phone: '',
    source: 'PHONE',
    priority: 'NORMAL',
    eventName: '',
    eventDate: '',
    guestCount: null,
    estimatedValue: null,
    customerNotes: ''
  };

  constructor(private crmService: CrmService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.crmService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load CRM dashboard', err);
        this.loading = false;
      }
    });
  }

  completeTask(followUpId: string): void {
    this.crmService.completeFollowUp(followUpId).subscribe({
      next: () => this.loadDashboard(),
      error: (err) => console.error('Failed to complete task', err)
    });
  }

  openCreateModal(): void {
    this.newLead = {
      firstName: '',
      lastName: '',
      companyName: '',
      email: '',
      phone: '',
      source: 'PHONE',
      priority: 'NORMAL',
      eventName: '',
      eventDate: '',
      guestCount: null,
      estimatedValue: null,
      customerNotes: ''
    };
    this.showModal = true;
  }

  closeCreateModal(): void {
    this.showModal = false;
  }

  saveLead(): void {
    this.crmService.createLead(this.newLead).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadDashboard();
      },
      error: (err) => console.error('Failed to create lead', err)
    });
  }
}
