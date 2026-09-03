import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CrmService, PageResponse } from '../../services/crm.service';
import { LeadStage, LeadSummary } from '../../models/crm.models';

@Component({
  selector: 'app-crm-leads-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="crm-page-container">
      <!-- Breadcrumb & Header -->
      <div class="breadcrumb">
        <a routerLink="/dashboard/crm/dashboard">CRM</a> / <span>All Leads</span>
      </div>

      <header class="page-header">
        <div>
          <h1>Sales Leads Directory</h1>
          <p class="subtitle">Search, filter, and track customer inquiries across all sales stages.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/dashboard/crm/pipeline" class="btn btn-secondary">
            <i class="bi bi-kanban"></i> Pipeline Board
          </a>
          <button (click)="openCreateModal()" class="btn btn-primary">
            <i class="bi bi-plus-circle-fill"></i> New Lead
          </button>
        </div>
      </header>

      <!-- Filter Bar -->
      <div class="filter-card">
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (ngModelChange)="onSearchChange()"
            placeholder="Search by lead #, contact, company, email, or event..."
          />
          <button *ngIf="searchQuery" (click)="clearSearch()" class="btn-clear">&times;</button>
        </div>

        <div class="filter-group">
          <select [(ngModel)]="selectedStage" (change)="loadLeads()">
            <option [ngValue]="null">All Stages</option>
            <option value="NEW">New</option>
            <option value="CONTACTED">Contacted</option>
            <option value="NEEDS_DISCOVERY">Needs Discovery</option>
            <option value="QUALIFIED">Qualified</option>
            <option value="QUOTE_PREPARED">Quote Prepared</option>
            <option value="QUOTE_SENT">Quote Sent</option>
            <option value="FOLLOW_UP">Follow Up</option>
            <option value="NEGOTIATION">Negotiation</option>
            <option value="WON">Closed Won</option>
            <option value="LOST">Closed Lost</option>
            <option value="DISQUALIFIED">Disqualified</option>
          </select>

          <label class="toggle-checkbox">
            <input type="checkbox" [(ngModel)]="unassignedOnly" (change)="loadLeads()" />
            <span>Unassigned Only</span>
          </label>
        </div>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading leads...</p>
      </div>

      <!-- Leads Table -->
      <div *ngIf="!loading" class="panel-card">
        <div *ngIf="leadsPage && leadsPage.content.length === 0" class="empty-state">
          <i class="bi bi-inbox"></i>
          <h3>No leads found</h3>
          <p>Try clearing your filters or create a new lead to get started.</p>
        </div>

        <div *ngIf="leadsPage && leadsPage.content.length > 0" class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Lead #</th>
                <th>Contact</th>
                <th>Company</th>
                <th>Event / Occasion</th>
                <th>Event Date</th>
                <th>Stage</th>
                <th>Source</th>
                <th>Est. Value</th>
                <th>Assigned Sales Rep</th>
                <th>Next Follow-Up</th>
                <th class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let lead of leadsPage.content">
                <td>
                  <a [routerLink]="['/dashboard/crm/leads', lead.id]" class="lead-link">
                    <strong>{{ lead.leadNumber }}</strong>
                  </a>
                </td>
                <td>
                  <div class="contact-col">
                    <strong>{{ lead.contactName }}</strong>
                    <span class="contact-sub">{{ lead.email }}</span>
                  </div>
                </td>
                <td>{{ lead.companyName || '—' }}</td>
                <td>{{ lead.eventName || lead.eventType || 'Event Inquiry' }}</td>
                <td>{{ lead.eventDate ? (lead.eventDate | date:'mediumDate') : 'TBD' }}</td>
                <td>
                  <span class="stage-pill" [ngClass]="'stage-' + lead.stage.toLowerCase()">
                    {{ lead.stage }}
                  </span>
                </td>
                <td><span class="source-tag">{{ lead.source }}</span></td>
                <td><strong>{{ lead.estimatedValue ? (lead.estimatedValue | currency) : '—' }}</strong></td>
                <td>{{ lead.assignedSalesUserName || 'Unassigned' }}</td>
                <td>
                  <div *ngIf="lead.nextFollowUpAt" class="followup-cell" [class.overdue]="lead.overdueFollowUp">
                    <i class="bi bi-clock"></i>
                    <span>{{ lead.nextFollowUpAt | date:'short' }}</span>
                  </div>
                  <span *ngIf="!lead.nextFollowUpAt" class="text-muted">None</span>
                </td>
                <td class="text-right">
                  <a [routerLink]="['/dashboard/crm/leads', lead.id]" class="btn btn-sm btn-outline-primary">
                    View & Action &rarr;
                  </a>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div class="pagination-bar" *ngIf="leadsPage.totalPages > 1">
            <button
              class="btn btn-sm btn-secondary"
              [disabled]="page === 0"
              (click)="changePage(page - 1)">
              &larr; Previous
            </button>
            <span class="page-indicator">Page {{ page + 1 }} of {{ leadsPage.totalPages }} ({{ leadsPage.totalElements }} leads)</span>
            <button
              class="btn btn-sm btn-secondary"
              [disabled]="page + 1 >= leadsPage.totalPages"
              (click)="changePage(page + 1)">
              Next &rarr;
            </button>
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
    .crm-page-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb { font-size: 0.85rem; color: #64748b; margin-bottom: 0.75rem; }
    .breadcrumb a { color: #3b82f6; text-decoration: none; }
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
    .btn-sm { padding: 0.3rem 0.65rem; font-size: 0.8rem; }
    .btn-outline-primary { background: transparent; border: 1px solid #3b82f6; color: #3b82f6; }
    .btn-outline-primary:hover { background: #eff6ff; }

    .filter-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1rem;
      margin-bottom: 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    }
    @media (max-width: 800px) {
      .filter-card { flex-direction: column; align-items: stretch; }
    }
    .search-box {
      position: relative;
      flex: 1;
      display: flex;
      align-items: center;
    }
    .search-box i { position: absolute; left: 0.75rem; color: #94a3b8; font-size: 0.9rem; }
    .search-box input {
      width: 100%;
      padding: 0.55rem 0.75rem 0.55rem 2.2rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
    }
    .btn-clear {
      position: absolute; right: 0.75rem; background: none; border: none; font-size: 1.1rem; color: #94a3b8; cursor: pointer;
    }
    .filter-group { display: flex; align-items: center; gap: 1rem; }
    .filter-group select {
      padding: 0.55rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
      background: white;
    }
    .toggle-checkbox { display: flex; align-items: center; gap: 0.4rem; font-size: 0.85rem; color: #334155; cursor: pointer; }

    .panel-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    }
    .table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .table th { text-align: left; padding: 0.75rem; background: #f8fafc; color: #64748b; font-weight: 600; font-size: 0.75rem; text-transform: uppercase; border-bottom: 1px solid #e2e8f0; }
    .table td { padding: 0.85rem 0.75rem; border-bottom: 1px solid #f1f5f9; color: #1e293b; vertical-align: middle; }
    .text-right { text-align: right; }
    .lead-link { color: #3b82f6; text-decoration: none; font-family: monospace; }
    .contact-col { display: flex; flex-direction: column; }
    .contact-sub { font-size: 0.75rem; color: #64748b; }
    .source-tag { background: #f1f5f9; color: #475569; font-size: 0.75rem; font-weight: 600; padding: 0.15rem 0.45rem; border-radius: 4px; }
    .followup-cell { display: flex; align-items: center; gap: 0.35rem; font-size: 0.75rem; color: #475569; }
    .followup-cell.overdue { color: #dc2626; font-weight: 600; }

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

    .pagination-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
      background: #f8fafc;
      border-top: 1px solid #e2e8f0;
    }
    .page-indicator { font-size: 0.85rem; color: #64748b; }

    .empty-state { text-align: center; padding: 4rem 1rem; color: #94a3b8; }
    .empty-state i { font-size: 3rem; color: #cbd5e1; margin-bottom: 0.5rem; display: block; }

    .loading-state { text-align: center; padding: 4rem 1rem; color: #64748b; }
    .spinner {
      width: 40px; height: 40px; border: 3px solid #e2e8f0; border-top-color: #3b82f6;
      border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem auto;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

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
  `]
})
export class CrmLeadsListComponent implements OnInit {
  leadsPage: PageResponse<LeadSummary> | null = null;
  loading = true;

  searchQuery = '';
  selectedStage: LeadStage | null = null;
  unassignedOnly = false;
  page = 0;
  size = 20;

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

  private searchTimeout: any;

  constructor(
    private crmService: CrmService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['unassigned'] === 'true') {
        this.unassignedOnly = true;
      }
      this.loadLeads();
    });
  }

  loadLeads(): void {
    this.loading = true;
    this.crmService.getLeads(
      this.searchQuery,
      this.selectedStage || undefined,
      undefined,
      this.unassignedOnly,
      this.page,
      this.size
    ).subscribe({
      next: (data) => {
        this.leadsPage = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load leads', err);
        this.loading = false;
      }
    });
  }

  onSearchChange(): void {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    this.searchTimeout = setTimeout(() => {
      this.page = 0;
      this.loadLeads();
    }, 300);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.page = 0;
    this.loadLeads();
  }

  changePage(newPage: number): void {
    this.page = newPage;
    this.loadLeads();
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
        this.loadLeads();
      },
      error: (err) => console.error('Failed to create lead', err)
    });
  }
}
