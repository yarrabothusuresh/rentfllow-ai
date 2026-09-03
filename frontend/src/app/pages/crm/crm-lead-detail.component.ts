import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CrmService } from '../../services/crm.service';
import {
  CustomerMatch,
  FollowUpType,
  LeadActivity,
  LeadDetail,
  LeadFollowUp,
  LeadLostReason,
  LeadStage
} from '../../models/crm.models';

@Component({
  selector: 'app-crm-lead-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="crm-detail-container" *ngIf="lead">
      <!-- Top Navigation & Breadcrumb -->
      <div class="detail-header-nav">
        <div class="breadcrumb">
          <a routerLink="/dashboard/crm/dashboard">CRM</a> /
          <a routerLink="/dashboard/crm/leads">Leads</a> /
          <span>{{ lead.leadNumber }}</span>
        </div>
        <div class="nav-actions">
          <a routerLink="/dashboard/crm/pipeline" class="btn btn-secondary btn-sm">
            <i class="bi bi-kanban"></i> Pipeline
          </a>
          <a routerLink="/dashboard/crm/leads" class="btn btn-secondary btn-sm">
            &larr; Back to Directory
          </a>
        </div>
      </div>

      <!-- Lead Headline & Stage Progression Bar -->
      <div class="lead-hero-card">
        <div class="lead-hero-main">
          <div class="lead-title-row">
            <span class="lead-number-tag">{{ lead.leadNumber }}</span>
            <h1>{{ lead.contactName }}</h1>
            <span *ngIf="lead.companyName" class="company-badge">{{ lead.companyName }}</span>
            <span class="stage-pill" [ngClass]="'stage-' + (lead.stage ? lead.stage.toLowerCase() : 'new')">{{ lead.stage }}</span>
          </div>
          <div class="lead-meta-row">
            <span><i class="bi bi-tag"></i> <strong>Source:</strong> {{ lead.source }}</span>
            <span><i class="bi bi-calendar-event"></i> <strong>Event:</strong> {{ lead.eventName || lead.eventType || 'Event Inquiry' }}</span>
            <span><i class="bi bi-calendar-date"></i> <strong>Date:</strong> {{ lead.eventDate ? (lead.eventDate | date:'mediumDate') : 'TBD' }}</span>
            <span *ngIf="lead.estimatedValue" class="value-highlight">
              <i class="bi bi-currency-dollar"></i> {{ lead.estimatedValue | currency }}
            </span>
          </div>
        </div>

        <!-- Action Quick-Bar -->
        <div class="lead-hero-actions">
          <!-- Primary Lifecycle Action Buttons -->
          <button *ngIf="lead.stage === 'NEW'" (click)="transitionTo('CONTACTED')" class="btn btn-outline-primary">
            <i class="bi bi-chat-dots"></i> Mark Contacted
          </button>

          <button *ngIf="lead.stage !== 'QUALIFIED' && lead.stage !== 'WON' && lead.stage !== 'LOST' && lead.stage !== 'DISQUALIFIED'"
                  (click)="qualifyLead()" class="btn btn-primary">
            <i class="bi bi-patch-check-fill"></i> Qualify Opportunity
          </button>

          <button *ngIf="!lead.customerId" (click)="openConvertCustomerModal()" class="btn btn-success">
            <i class="bi bi-person-check-fill"></i> Convert to Customer
          </button>

          <button *ngIf="lead.customerId && !lead.quoteId" (click)="createQuoteDraft()" class="btn btn-primary">
            <i class="bi bi-file-earmark-plus-fill"></i> Create Quote Draft
          </button>

          <button *ngIf="lead.stage !== 'WON' && lead.stage !== 'LOST' && lead.stage !== 'DISQUALIFIED'"
                  (click)="openMarkWonModal()" class="btn btn-success">
            <i class="bi bi-trophy-fill"></i> Mark Won
          </button>

          <button *ngIf="lead.stage !== 'LOST' && lead.stage !== 'DISQUALIFIED'"
                  (click)="openMarkLostModal()" class="btn btn-outline-danger">
            <i class="bi bi-x-circle"></i> Mark Lost
          </button>

          <button *ngIf="lead.stage === 'LOST' || lead.stage === 'DISQUALIFIED'"
                  (click)="openReopenModal()" class="btn btn-warning">
            <i class="bi bi-arrow-counterclockwise"></i> Reopen Lead
          </button>
        </div>
      </div>

      <!-- Customer Match Detection Warning -->
      <div *ngIf="!lead.customerId && customerMatches.length > 0" class="customer-match-banner">
        <div class="match-icon"><i class="bi bi-people-fill"></i></div>
        <div class="match-content">
          <h4>Existing Customer Match Detected</h4>
          <p>We found {{ customerMatches.length }} existing customer account(s) matching this lead's contact info. Link this lead to retain account history:</p>
          <div class="match-items">
            <div *ngFor="let m of customerMatches" class="match-item">
              <div class="match-info">
                <strong>{{ m.name }}</strong> ({{ m.customerNumber }}) &bull; {{ m.email }}
                <span *ngIf="m.companyName">&bull; {{ m.companyName }}</span>
              </div>
              <button (click)="linkExistingCustomer(m.customerId)" class="btn btn-sm btn-primary">
                Link to Customer
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main 2-Column Layout -->
      <div class="detail-split">
        <!-- LEFT COLUMN: Lead Information & Cross-Domain Links -->
        <div class="left-col">
          <!-- Cross-Domain Relationship Cards -->
          <div class="panel-card links-card" *ngIf="lead.customerId || lead.quoteId || lead.rentalRequestId || lead.bookingId">
            <div class="panel-header">
              <h3><i class="bi bi-link-45deg"></i> Linked Records</h3>
            </div>
            <div class="links-grid">
              <div *ngIf="lead.customerId" class="link-chip">
                <i class="bi bi-person-circle"></i>
                <div>
                  <span class="chip-label">Customer Account</span>
                  <a [routerLink]="['/dashboard/customers', lead.customerId]">
                    {{ lead.customerName || 'View Customer' }}
                  </a>
                </div>
              </div>

              <div *ngIf="lead.quoteId" class="link-chip">
                <i class="bi bi-file-earmark-text"></i>
                <div>
                  <span class="chip-label">Rental Quote</span>
                  <a [routerLink]="['/dashboard/quotes', lead.quoteId]">
                    {{ lead.quoteNumber || 'View Quote Draft' }}
                  </a>
                </div>
              </div>

              <div *ngIf="lead.rentalRequestId" class="link-chip">
                <i class="bi bi-cart-check"></i>
                <div>
                  <span class="chip-label">Storefront Request</span>
                  <span>#{{ lead.rentalRequestId | slice:0:8 }}</span>
                </div>
              </div>

              <div *ngIf="lead.bookingId" class="link-chip">
                <i class="bi bi-calendar-check"></i>
                <div>
                  <span class="chip-label">Confirmed Booking</span>
                  <a [routerLink]="['/dashboard/bookings', lead.bookingId]">
                    {{ lead.bookingNumber || 'View Booking' }}
                  </a>
                </div>
              </div>
            </div>
          </div>

          <!-- Contact Information & Edit Form -->
          <div class="panel-card">
            <div class="panel-header">
              <h3><i class="bi bi-person-lines-fill"></i> Contact & Event Details</h3>
              <button (click)="toggleEdit()" class="btn btn-sm btn-secondary">
                {{ isEditing ? 'Cancel' : 'Edit Details' }}
              </button>
            </div>

            <div *ngIf="!isEditing" class="info-grid">
              <div class="info-group">
                <span class="info-label">Full Name</span>
                <span class="info-val">{{ lead.contactName }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Email</span>
                <span class="info-val"><a [href]="'mailto:' + lead.email">{{ lead.email }}</a></span>
              </div>
              <div class="info-group">
                <span class="info-label">Phone</span>
                <span class="info-val">{{ lead.phone || '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Company</span>
                <span class="info-val">{{ lead.companyName || '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Event Type</span>
                <span class="info-val">{{ lead.eventType || '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Event Date</span>
                <span class="info-val">{{ lead.eventDate ? (lead.eventDate | date:'mediumDate') : '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Rental Window</span>
                <span class="info-val">
                  {{ lead.rentalStartDate ? (lead.rentalStartDate | date:'mediumDate') : '—' }}
                  to {{ lead.rentalEndDate ? (lead.rentalEndDate | date:'mediumDate') : '—' }}
                </span>
              </div>
              <div class="info-group">
                <span class="info-label">Venue</span>
                <span class="info-val">{{ lead.venueName || '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Guest Count</span>
                <span class="info-val">{{ lead.guestCount || '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Budget</span>
                <span class="info-val">{{ lead.estimatedBudget ? (lead.estimatedBudget | currency) : '—' }}</span>
              </div>
              <div class="info-group">
                <span class="info-label">Assigned Sales Rep</span>
                <span class="info-val">{{ lead.assignedSalesUserName || 'Unassigned' }}</span>
              </div>
              <div class="info-group full-width">
                <span class="info-label">Customer Inquired Notes</span>
                <p class="notes-content">{{ lead.customerNotes || 'No notes provided by customer.' }}</p>
              </div>
              <div class="info-group full-width" *ngIf="lead.internalNotes">
                <span class="info-label">Internal Sales Notes</span>
                <p class="notes-content internal-notes">{{ lead.internalNotes }}</p>
              </div>
              <div class="info-group full-width" *ngIf="lead.lostReason">
                <span class="info-label">Closed Lost Reason</span>
                <p class="notes-content lost-notes">
                  <strong>{{ lead.lostReason }}</strong> &bull; {{ lead.lostReasonNotes || 'No notes' }}
                </p>
              </div>
            </div>

            <!-- Edit Form Mode -->
            <div *ngIf="isEditing" class="edit-form">
              <div class="form-row">
                <div class="form-group col">
                  <label>First Name</label>
                  <input type="text" [(ngModel)]="editForm.firstName" />
                </div>
                <div class="form-group col">
                  <label>Last Name</label>
                  <input type="text" [(ngModel)]="editForm.lastName" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col">
                  <label>Email</label>
                  <input type="email" [(ngModel)]="editForm.email" />
                </div>
                <div class="form-group col">
                  <label>Phone</label>
                  <input type="text" [(ngModel)]="editForm.phone" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col">
                  <label>Company Name</label>
                  <input type="text" [(ngModel)]="editForm.companyName" />
                </div>
                <div class="form-group col">
                  <label>Priority</label>
                  <select [(ngModel)]="editForm.priority">
                    <option value="LOW">Low</option>
                    <option value="NORMAL">Normal</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                  </select>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col">
                  <label>Event Name</label>
                  <input type="text" [(ngModel)]="editForm.eventName" />
                </div>
                <div class="form-group col">
                  <label>Event Date</label>
                  <input type="date" [(ngModel)]="editForm.eventDate" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col">
                  <label>Rental Start</label>
                  <input type="date" [(ngModel)]="editForm.rentalStartDate" />
                </div>
                <div class="form-group col">
                  <label>Rental End</label>
                  <input type="date" [(ngModel)]="editForm.rentalEndDate" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group col">
                  <label>Estimated Value ($)</label>
                  <input type="number" [(ngModel)]="editForm.estimatedValue" />
                </div>
                <div class="form-group col">
                  <label>Venue Name</label>
                  <input type="text" [(ngModel)]="editForm.venueName" />
                </div>
              </div>
              <div class="form-group">
                <label>Internal Sales Notes</label>
                <textarea [(ngModel)]="editForm.internalNotes" rows="3"></textarea>
              </div>
              <div class="form-actions">
                <button (click)="toggleEdit()" class="btn btn-secondary">Cancel</button>
                <button (click)="saveEdit()" class="btn btn-primary">Save Changes</button>
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT COLUMN: Activity Timeline & Scheduled Follow-Ups -->
        <div class="right-col">
          <!-- Follow-Up Tasks Panel -->
          <div class="panel-card">
            <div class="panel-header">
              <h3><i class="bi bi-check2-circle"></i> Follow-Up Tasks</h3>
              <button (click)="openScheduleModal()" class="btn btn-sm btn-outline-primary">
                <i class="bi bi-plus"></i> Schedule Task
              </button>
            </div>

            <div *ngIf="followUps.length === 0" class="empty-sub-panel">
              <p>No follow-ups scheduled. Set a task to ensure timely customer contact.</p>
            </div>

            <div *ngIf="followUps.length > 0" class="tasks-list">
              <div *ngFor="let fu of followUps" class="task-item" [class.is-done]="fu.status === 'COMPLETED'">
                <div class="task-checkbox">
                  <button *ngIf="fu.status === 'OPEN'" (click)="completeTask(fu.id)" class="btn-check-task" title="Complete task">
                    <i class="bi bi-circle"></i>
                  </button>
                  <i *ngIf="fu.status === 'COMPLETED'" class="bi bi-check-circle-fill done-icon"></i>
                </div>
                <div class="task-content">
                  <div class="task-title" [class.line-through]="fu.status === 'COMPLETED'">
                    <strong>{{ fu.title }}</strong>
                    <span *ngIf="fu.overdue && fu.status === 'OPEN'" class="badge-overdue">OVERDUE</span>
                  </div>
                  <div class="task-meta">
                    <span>Due: {{ fu.dueAt | date:'short' }}</span>
                    <span *ngIf="fu.assignedToName">&bull; {{ fu.assignedToName }}</span>
                  </div>
                  <p *ngIf="fu.notes" class="task-notes">{{ fu.notes }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Sales Activity Timeline -->
          <div class="panel-card">
            <div class="panel-header">
              <h3><i class="bi bi-clock-history"></i> Activity Timeline</h3>
              <div class="timeline-quick-actions">
                <button (click)="openLogActivityModal('CALL')" class="btn btn-sm btn-secondary" title="Log Phone Call">
                  <i class="bi bi-telephone"></i> Call
                </button>
                <button (click)="openLogActivityModal('EMAIL')" class="btn btn-sm btn-secondary" title="Log Email">
                  <i class="bi bi-envelope"></i> Email
                </button>
                <button (click)="openLogActivityModal('NOTE')" class="btn btn-sm btn-secondary" title="Add Note">
                  <i class="bi bi-journal-text"></i> Note
                </button>
              </div>
            </div>

            <div class="timeline-stream">
              <div *ngFor="let act of activities" class="timeline-entry">
                <div class="timeline-dot" [ngClass]="'dot-' + act.type.toLowerCase()">
                  <i *ngIf="act.type === 'CALL'" class="bi bi-telephone-fill"></i>
                  <i *ngIf="act.type === 'EMAIL'" class="bi bi-envelope-fill"></i>
                  <i *ngIf="act.type === 'NOTE'" class="bi bi-journal-text"></i>
                  <i *ngIf="act.type === 'STATUS_CHANGE'" class="bi bi-arrow-right-circle-fill"></i>
                  <i *ngIf="act.type === 'QUOTE_CREATED'" class="bi bi-file-earmark-check-fill"></i>
                  <i *ngIf="act.type === 'WON'" class="bi bi-trophy-fill"></i>
                  <i *ngIf="act.type === 'LOST'" class="bi bi-x-circle-fill"></i>
                  <i *ngIf="act.type !== 'CALL' && act.type !== 'EMAIL' && act.type !== 'NOTE' && act.type !== 'STATUS_CHANGE' && act.type !== 'QUOTE_CREATED' && act.type !== 'WON' && act.type !== 'LOST'" class="bi bi-dot"></i>
                </div>
                <div class="timeline-body">
                  <div class="timeline-heading">
                    <strong>{{ act.subject }}</strong>
                    <span class="timeline-time">{{ act.occurredAt | date:'short' }}</span>
                  </div>
                  <p *ngIf="act.summary" class="timeline-summary">{{ act.summary }}</p>
                  <p *ngIf="act.notes" class="timeline-notes">{{ act.notes }}</p>
                  <span *ngIf="act.createdBy" class="timeline-author">by {{ act.createdBy }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modals for Actions -->
      <!-- 1. Schedule Follow-Up Modal -->
      <div *ngIf="showScheduleModal" class="modal-backdrop">
        <div class="modal-content">
          <div class="modal-header">
            <h2>Schedule Follow-Up Task</h2>
            <button (click)="showScheduleModal = false" class="btn-close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Task Type</label>
              <select [(ngModel)]="newFollowUp.type">
                <option value="CALL">Phone Call</option>
                <option value="EMAIL">Follow-Up Email</option>
                <option value="MEETING">Client Meeting</option>
                <option value="QUOTE">Send / Review Quote</option>
                <option value="GENERAL">General Task</option>
              </select>
            </div>
            <div class="form-group">
              <label>Task Title *</label>
              <input type="text" [(ngModel)]="newFollowUp.title" placeholder="e.g. Call client regarding linen color selection" />
            </div>
            <div class="form-group">
              <label>Due Date & Time *</label>
              <input type="datetime-local" [(ngModel)]="newFollowUp.dueAt" />
            </div>
            <div class="form-group">
              <label>Task Notes</label>
              <textarea [(ngModel)]="newFollowUp.notes" rows="3" placeholder="Context or preparation notes for the sales rep..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="showScheduleModal = false" class="btn btn-secondary">Cancel</button>
            <button (click)="saveFollowUp()" class="btn btn-primary" [disabled]="!newFollowUp.title || !newFollowUp.dueAt">
              Schedule Task
            </button>
          </div>
        </div>
      </div>

      <!-- 2. Log Activity Modal -->
      <div *ngIf="showLogActivityModal" class="modal-backdrop">
        <div class="modal-content">
          <div class="modal-header">
            <h2>Log {{ newActivity.type }}</h2>
            <button (click)="showLogActivityModal = false" class="btn-close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group" *ngIf="newActivity.type === 'CALL'">
              <label>Call Outcome</label>
              <select [(ngModel)]="newActivity.callOutcome">
                <option value="CONNECTED">Connected & Spoke</option>
                <option value="VOICEMAIL">Left Voicemail</option>
                <option value="NO_ANSWER">No Answer</option>
                <option value="FOLLOW_UP_REQUIRED">Follow-Up Required</option>
              </select>
            </div>
            <div class="form-group">
              <label>Subject / Headline *</label>
              <input type="text" [(ngModel)]="newActivity.subject" placeholder="Summary headline" />
            </div>
            <div class="form-group">
              <label>Detailed Notes</label>
              <textarea [(ngModel)]="newActivity.notes" rows="4" placeholder="Log details of conversation, preferences, or agreed next steps..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="showLogActivityModal = false" class="btn btn-secondary">Cancel</button>
            <button (click)="saveActivity()" class="btn btn-primary" [disabled]="!newActivity.subject">
              Log Activity
            </button>
          </div>
        </div>
      </div>

      <!-- 3. Mark Lost Modal -->
      <div *ngIf="showLostModal" class="modal-backdrop">
        <div class="modal-content">
          <div class="modal-header">
            <h2>Mark Lead Closed Lost</h2>
            <button (click)="showLostModal = false" class="btn-close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Lost Reason *</label>
              <select [(ngModel)]="lostData.reason">
                <option value="PRICE">Price / Budget Too High</option>
                <option value="AVAILABILITY">Equipment Unavailable</option>
                <option value="COMPETITOR">Went with Competitor</option>
                <option value="EVENT_CANCELLED">Event Cancelled</option>
                <option value="NO_RESPONSE">Customer Unresponsive</option>
                <option value="NOT_A_FIT">Not a Good Fit</option>
                <option value="DUPLICATE">Duplicate Inquiry</option>
                <option value="OTHER">Other Reason</option>
              </select>
            </div>
            <div class="form-group">
              <label>Explanation Notes</label>
              <textarea [(ngModel)]="lostData.notes" rows="3" placeholder="Explain context so sales analytics can track lost deal trends..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="showLostModal = false" class="btn btn-secondary">Cancel</button>
            <button (click)="saveMarkLost()" class="btn btn-danger" [disabled]="!lostData.reason">
              Confirm Closed Lost
            </button>
          </div>
        </div>
      </div>

      <!-- 4. Reopen Modal -->
      <div *ngIf="showReopenModal" class="modal-backdrop">
        <div class="modal-content">
          <div class="modal-header">
            <h2>Reopen Sales Lead</h2>
            <button (click)="showReopenModal = false" class="btn-close">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Reopen Reason *</label>
              <textarea [(ngModel)]="reopenReason" rows="3" placeholder="Why is this opportunity being reopened? (e.g. Customer budget increased, date changed)..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="showReopenModal = false" class="btn btn-secondary">Cancel</button>
            <button (click)="saveReopen()" class="btn btn-warning" [disabled]="!reopenReason">
              Reopen Opportunity
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .crm-detail-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .detail-header-nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    .breadcrumb { font-size: 0.85rem; color: #64748b; }
    .breadcrumb a { color: #3b82f6; text-decoration: none; }
    .nav-actions { display: flex; gap: 0.5rem; }

    .lead-hero-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .lead-hero-main { display: flex; flex-direction: column; gap: 0.5rem; }
    .lead-title-row { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
    .lead-number-tag { font-family: monospace; background: #f1f5f9; color: #475569; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.85rem; }
    h1 { font-size: 1.6rem; font-weight: 700; color: #0f172a; margin: 0; }
    .company-badge { font-size: 1rem; color: #64748b; }
    .lead-meta-row { display: flex; align-items: center; gap: 1.25rem; font-size: 0.85rem; color: #64748b; flex-wrap: wrap; }
    .value-highlight { color: #059669; font-weight: 700; font-size: 0.95rem; }
    .lead-hero-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }

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
    .btn-warning { background: #f59e0b; color: white; }
    .btn-danger { background: #ef4444; color: white; }
    .btn-sm { padding: 0.3rem 0.65rem; font-size: 0.8rem; }
    .btn-outline-primary { background: transparent; border: 1px solid #3b82f6; color: #3b82f6; }
    .btn-outline-danger { background: transparent; border: 1px solid #ef4444; color: #ef4444; }

    .customer-match-banner {
      background: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }
    .match-icon { font-size: 1.5rem; color: #2563eb; }
    .match-content { flex: 1; }
    .match-content h4 { margin: 0 0 0.35rem 0; color: #1e3a8a; font-size: 1rem; }
    .match-content p { margin: 0 0 0.75rem 0; font-size: 0.85rem; color: #1e40af; }
    .match-items { display: flex; flex-direction: column; gap: 0.5rem; }
    .match-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: white;
      padding: 0.5rem 0.75rem;
      border-radius: 6px;
      font-size: 0.85rem;
      border: 1px solid #dbeafe;
    }

    .detail-split {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.5rem;
    }
    @media (max-width: 950px) {
      .detail-split { grid-template-columns: 1fr; }
    }

    .panel-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
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

    .links-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 0.75rem; }
    .link-chip {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      padding: 0.75rem;
      border-radius: 8px;
    }
    .link-chip i { font-size: 1.25rem; color: #3b82f6; }
    .chip-label { display: block; font-size: 0.7rem; color: #64748b; text-transform: uppercase; font-weight: 600; }
    .link-chip a { font-size: 0.85rem; font-weight: 600; color: #2563eb; text-decoration: none; }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    .info-group { display: flex; flex-direction: column; gap: 0.2rem; }
    .info-group.full-width { grid-column: 1 / -1; }
    .info-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; }
    .info-val { font-size: 0.9rem; color: #0f172a; }
    .info-val a { color: #3b82f6; text-decoration: none; }
    .notes-content { margin: 0.35rem 0 0 0; font-size: 0.875rem; color: #334155; line-height: 1.4; background: #f8fafc; padding: 0.75rem; border-radius: 6px; border: 1px solid #e2e8f0; }
    .internal-notes { border-left: 3px solid #8b5cf6; }
    .lost-notes { border-left: 3px solid #ef4444; background: #fff5f5; }

    .tasks-list { display: flex; flex-direction: column; gap: 0.65rem; }
    .task-item {
      display: flex;
      gap: 0.75rem;
      align-items: flex-start;
      padding: 0.65rem;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
    }
    .task-item.is-done { opacity: 0.6; }
    .btn-check-task { background: none; border: none; font-size: 1.1rem; color: #94a3b8; cursor: pointer; padding: 0; }
    .btn-check-task:hover { color: #10b981; }
    .done-icon { font-size: 1.1rem; color: #10b981; }
    .task-content { flex: 1; }
    .task-title { font-size: 0.85rem; color: #1e293b; display: flex; align-items: center; gap: 0.5rem; }
    .line-through { text-decoration: line-through; }
    .badge-overdue { background: #fee2e2; color: #991b1b; font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.35rem; border-radius: 4px; }
    .task-meta { font-size: 0.75rem; color: #64748b; margin-top: 0.15rem; }
    .task-notes { font-size: 0.75rem; color: #475569; margin: 0.2rem 0 0 0; }

    .timeline-quick-actions { display: flex; gap: 0.35rem; }
    .timeline-stream { display: flex; flex-direction: column; gap: 1rem; position: relative; padding-left: 0.5rem; }
    .timeline-entry { display: flex; gap: 0.75rem; align-items: flex-start; }
    .timeline-dot {
      width: 28px; height: 28px; border-radius: 50%; background: #e2e8f0; color: #3b82f6;
      display: flex; align-items: center; justify-content: center; font-size: 0.8rem; flex-shrink: 0;
    }
    .dot-call { background: #dbeafe; color: #1e40af; }
    .dot-email { background: #e0e7ff; color: #3730a3; }
    .dot-note { background: #fef3c7; color: #92400e; }
    .dot-won { background: #d1fae5; color: #065f46; }
    .dot-lost { background: #fee2e2; color: #991b1b; }
    .timeline-body { flex: 1; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0.65rem 0.85rem; }
    .timeline-heading { display: flex; justify-content: space-between; font-size: 0.85rem; color: #1e293b; }
    .timeline-time { font-size: 0.75rem; color: #94a3b8; }
    .timeline-summary { font-size: 0.8rem; color: #475569; margin: 0.25rem 0 0 0; }
    .timeline-notes { font-size: 0.8rem; color: #334155; margin: 0.25rem 0 0 0; white-space: pre-wrap; }
    .timeline-author { display: block; font-size: 0.7rem; color: #94a3b8; margin-top: 0.35rem; }

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

    .empty-sub-panel { color: #94a3b8; font-size: 0.85rem; padding: 1rem 0; }

    /* Modals */
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
      max-width: 550px;
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
    .modal-header h2 { font-size: 1.2rem; font-weight: 700; color: #0f172a; margin: 0; }
    .btn-close { background: none; border: none; font-size: 1.5rem; cursor: pointer; color: #94a3b8; }
    .modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
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
    .form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; }
  `]
})
export class CrmLeadDetailComponent implements OnInit {
  leadId!: string;
  lead: LeadDetail | null = null;
  activities: LeadActivity[] = [];
  followUps: LeadFollowUp[] = [];
  customerMatches: CustomerMatch[] = [];

  isEditing = false;
  editForm: any = {};

  showScheduleModal = false;
  newFollowUp: any = { type: 'CALL', title: '', dueAt: '', notes: '' };

  showLogActivityModal = false;
  newActivity: any = { type: 'CALL', callOutcome: 'CONNECTED', subject: '', notes: '' };

  showLostModal = false;
  lostData: { reason: LeadLostReason; notes: string } = { reason: 'PRICE', notes: '' };

  showReopenModal = false;
  reopenReason = '';

  constructor(
    private crmService: CrmService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.leadId = params['id'];
      if (this.leadId) {
        this.loadLead();
      }
    });
  }

  loadLead(): void {
    this.crmService.getLeadById(this.leadId).subscribe({
      next: (data) => {
        this.lead = data;
        this.loadActivities();
        this.loadFollowUps();
        if (!this.lead.customerId) {
          this.loadCustomerMatches();
        }
      },
      error: (err) => console.error('Failed to load lead', err)
    });
  }

  loadActivities(): void {
    this.crmService.getActivities(this.leadId).subscribe({
      next: (data) => this.activities = data,
      error: (err) => console.error('Failed to load activities', err)
    });
  }

  loadFollowUps(): void {
    this.crmService.getFollowUps(this.leadId).subscribe({
      next: (data) => this.followUps = data,
      error: (err) => console.error('Failed to load follow-ups', err)
    });
  }

  loadCustomerMatches(): void {
    this.crmService.getCustomerMatches(this.leadId).subscribe({
      next: (data) => this.customerMatches = data,
      error: (err) => console.error('Failed to load matches', err)
    });
  }

  transitionTo(stage: LeadStage): void {
    this.crmService.transitionStage(this.leadId, stage).subscribe({
      next: () => this.loadLead(),
      error: (err) => alert(err.error?.error || 'Transition failed')
    });
  }

  qualifyLead(): void {
    this.crmService.qualifyLead(this.leadId).subscribe({
      next: () => this.loadLead(),
      error: (err) => alert(err.error?.error || 'Qualification failed')
    });
  }

  openConvertCustomerModal(): void {
    if (confirm('Convert this lead into a permanent Customer account?')) {
      this.crmService.convertCustomer(this.leadId, false).subscribe({
        next: () => this.loadLead(),
        error: (err) => alert(err.error?.error || 'Conversion failed')
      });
    }
  }

  linkExistingCustomer(customerId: string): void {
    this.crmService.linkCustomer(this.leadId, customerId).subscribe({
      next: () => this.loadLead(),
      error: (err) => alert(err.error?.error || 'Link failed')
    });
  }

  createQuoteDraft(): void {
    this.crmService.createQuoteDraft(this.leadId).subscribe({
      next: (updatedLead) => {
        this.lead = updatedLead;
        if (updatedLead.quoteId) {
          this.router.navigate(['/dashboard/quotes', updatedLead.quoteId]);
        } else {
          this.loadLead();
        }
      },
      error: (err) => alert(err.error?.error || 'Failed to create quote draft')
    });
  }

  openMarkWonModal(): void {
    if (confirm('Mark this opportunity as WON?')) {
      this.crmService.markWon(this.leadId, this.lead?.quoteId, this.lead?.bookingId).subscribe({
        next: () => this.loadLead(),
        error: (err) => alert(err.error?.error || 'Failed to mark won')
      });
    }
  }

  openMarkLostModal(): void {
    this.lostData = { reason: 'PRICE', notes: '' };
    this.showLostModal = true;
  }

  saveMarkLost(): void {
    this.crmService.markLost(this.leadId, this.lostData.reason, this.lostData.notes).subscribe({
      next: () => {
        this.showLostModal = false;
        this.loadLead();
      },
      error: (err) => alert(err.error?.error || 'Failed to mark lost')
    });
  }

  openReopenModal(): void {
    this.reopenReason = '';
    this.showReopenModal = true;
  }

  saveReopen(): void {
    this.crmService.reopenLead(this.leadId, this.reopenReason).subscribe({
      next: () => {
        this.showReopenModal = false;
        this.loadLead();
      },
      error: (err) => alert(err.error?.error || 'Failed to reopen lead')
    });
  }

  toggleEdit(): void {
    if (!this.isEditing && this.lead) {
      this.editForm = { ...this.lead };
    }
    this.isEditing = !this.isEditing;
  }

  saveEdit(): void {
    this.crmService.updateLead(this.leadId, this.editForm).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadLead();
      },
      error: (err) => alert(err.error?.error || 'Failed to update lead')
    });
  }

  openScheduleModal(): void {
    this.newFollowUp = {
      type: 'CALL',
      title: '',
      dueAt: new Date(Date.now() + 86400000).toISOString().slice(0, 16),
      notes: ''
    };
    this.showScheduleModal = true;
  }

  saveFollowUp(): void {
    this.crmService.scheduleFollowUp(this.leadId, this.newFollowUp).subscribe({
      next: () => {
        this.showScheduleModal = false;
        this.loadLead();
      },
      error: (err) => console.error('Failed to schedule follow-up', err)
    });
  }

  completeTask(followUpId: string): void {
    this.crmService.completeFollowUp(followUpId).subscribe({
      next: () => this.loadFollowUps(),
      error: (err) => console.error('Failed to complete task', err)
    });
  }

  openLogActivityModal(type: 'CALL' | 'EMAIL' | 'NOTE'): void {
    this.newActivity = {
      type,
      callOutcome: 'CONNECTED',
      subject: type === 'CALL' ? 'Phone call with client' : (type === 'EMAIL' ? 'Email sent to client' : 'Note added'),
      notes: ''
    };
    this.showLogActivityModal = true;
  }

  saveActivity(): void {
    this.crmService.logActivity(this.leadId, this.newActivity).subscribe({
      next: () => {
        this.showLogActivityModal = false;
        this.loadActivities();
      },
      error: (err) => console.error('Failed to log activity', err)
    });
  }
}
