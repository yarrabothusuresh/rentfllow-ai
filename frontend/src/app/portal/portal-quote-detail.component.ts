import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalQuote } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-quote-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="mb-3">
        <a routerLink="/portal/quotes" class="text-info text-decoration-none">← Back to Proposals</a>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        🎉 {{ successMessage }}
      </div>
      <div *ngIf="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <div *ngIf="!isLoading && quote" class="card-glass p-4">
        <!-- Proposal Header -->
        <div class="d-flex justify-content-between align-items-start border-bottom border-secondary pb-3 mb-4">
          <div>
            <span class="badge bg-info text-dark font-monospace mb-2">PROPOSAL {{ quote.quoteNumber }}</span>
            <h2 class="text-light font-weight-bold mb-1">{{ quote.eventName || 'Rental Proposal' }}</h2>
            <p class="text-muted mb-0">📅 Valid Until: <strong>{{ quote.validUntil }}</strong></p>
          </div>
          <div class="text-end">
            <span class="badge fs-6 mb-2" [ngClass]="getStatusBadgeClass(quote.status)">{{ quote.status }}</span>
            <h2 class="text-info font-weight-bold mb-0">\${{ quote.totalAmount | number:'1.2-2' }}</h2>
          </div>
        </div>

        <!-- Rental Items Table -->
        <h4 class="text-light mb-3">Itemized Rental Breakdown</h4>
        <div class="table-responsive mb-4">
          <table class="table table-dark table-striped align-middle">
            <thead>
              <tr>
                <th>Item Description</th>
                <th class="text-center">Qty</th>
                <th class="text-end">Unit Price</th>
                <th class="text-end">Line Total</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of quote.items">
                <td>{{ item.description }}</td>
                <td class="text-center">{{ item.quantity }}</td>
                <td class="text-end">\${{ item.unitPrice | number:'1.2-2' }}</td>
                <td class="text-end font-weight-bold">\${{ item.lineSubtotal | number:'1.2-2' }}</td>
              </tr>
              <tr *ngIf="!quote.items || quote.items.length === 0">
                <td colspan="4" class="text-center text-muted">No line items detailed.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Financial Summary Breakdown -->
        <div class="row mb-4">
          <div class="col-md-6">
            <div *ngIf="quote.notes" class="p-3 card-glass bg-dark">
              <small class="text-muted d-block">Proposal Notes:</small>
              <p class="mb-0 text-light">{{ quote.notes }}</p>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card-glass p-3 ms-auto" style="max-width: 400px;">
              <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Subtotal:</span>
                <span>\${{ quote.subtotal | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2" *ngIf="quote.discountAmount">
                <span class="text-muted">Discount:</span>
                <span class="text-success">-\${{ quote.discountAmount | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2" *ngIf="quote.fees">
                <span class="text-muted">Delivery & Service Fees:</span>
                <span>\${{ quote.fees | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2" *ngIf="quote.taxAmount">
                <span class="text-muted">Estimated Sales Tax (8.25%):</span>
                <span>\${{ quote.taxAmount | number:'1.2-2' }}</span>
              </div>
              <hr class="border-secondary my-2" />
              <div class="d-flex justify-content-between fs-5 font-weight-bold">
                <span class="text-light">TOTAL PROPOSAL:</span>
                <span class="text-info">\${{ quote.totalAmount | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between small text-muted mt-1" *ngIf="quote.depositRequired">
                <span>Deposit Required to Confirm:</span>
                <span>\${{ quote.depositRequired | number:'1.2-2' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Customer Action Buttons -->
        <div class="d-flex gap-3 justify-content-end border-top border-secondary pt-4" *ngIf="quote.status === 'SENT' || quote.status === 'VIEWED' || quote.status === 'DRAFT'">
          <button class="btn btn-outline-warning px-4" (click)="showRequestChangesModal = true">
            💬 Request Changes
          </button>
          <button class="btn btn-success btn-lg px-5 font-weight-bold" (click)="showAcceptModal = true">
            ✅ Accept Proposal (\${{ quote.totalAmount | number:'1.2-2' }})
          </button>
        </div>

        <div *ngIf="quote.status === 'ACCEPTED'" class="alert alert-success text-center my-3">
          ✔️ This proposal has been accepted. We are preparing your booking order!
        </div>
        <div *ngIf="quote.status === 'CHANGE_REQUESTED'" class="alert alert-warning text-center my-3">
          ⏳ Your change request has been submitted. Our sales team is reviewing your updates.
        </div>
      </div>
    </div>

    <!-- Accept Quote Confirmation Modal -->
    <div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.7);" *ngIf="showAcceptModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-light border-secondary">
          <div class="modal-header border-secondary">
            <h5 class="modal-title">Confirm Proposal Acceptance</h5>
            <button type="button" class="btn-close btn-close-white" (click)="showAcceptModal = false"></button>
          </div>
          <div class="modal-body text-center">
            <h4>Accept this proposal?</h4>
            <h2 class="text-info my-3">\${{ quote?.totalAmount | number:'1.2-2' }}</h2>
            <p class="text-muted">By accepting, you confirm that you agree to proceed with this rental proposal for {{ quote?.eventName }}.</p>
          </div>
          <div class="modal-footer border-secondary">
            <button type="button" class="btn btn-secondary" (click)="showAcceptModal = false">Cancel</button>
            <button type="button" class="btn btn-success font-weight-bold px-4" (click)="acceptQuote()" [disabled]="isSubmitting">
              {{ isSubmitting ? 'Accepting...' : 'Yes, Accept Proposal' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Request Changes Modal -->
    <div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.7);" *ngIf="showRequestChangesModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-light border-secondary">
          <div class="modal-header border-secondary">
            <h5 class="modal-title">Request Changes to Proposal</h5>
            <button type="button" class="btn-close btn-close-white" (click)="showRequestChangesModal = false"></button>
          </div>
          <div class="modal-body">
            <label class="form-label text-muted">Tell us what you would like to change or adjust:</label>
            <textarea 
              class="form-control bg-black text-light border-secondary" 
              rows="4" 
              [(ngModel)]="changeMessage" 
              placeholder="e.g. Can we add 20 extra chiavari chairs and adjust delivery time to 9 AM?"></textarea>
          </div>
          <div class="modal-footer border-secondary">
            <button type="button" class="btn btn-secondary" (click)="showRequestChangesModal = false">Cancel</button>
            <button type="button" class="btn btn-warning px-4" (click)="submitRequestChanges()" [disabled]="isSubmitting || !changeMessage">
              {{ isSubmitting ? 'Sending...' : 'Send Request' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalQuoteDetailComponent implements OnInit {
  quote: CustomerPortalQuote | null = null;
  isLoading = true;
  isSubmitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  showAcceptModal = false;
  showRequestChangesModal = false;
  changeMessage = '';

  constructor(
    private route: ActivatedRoute,
    private portalService: CustomerPortalService
  ) {}

  ngOnInit(): void {
    this.loadQuote();
  }

  loadQuote(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isLoading = true;
      this.portalService.getQuoteDetail(id).subscribe({
        next: (data) => { this.quote = data; this.isLoading = false; },
        error: (err) => { this.errorMessage = err.error?.error || 'Failed to load proposal.'; this.isLoading = false; }
      });
    }
  }

  acceptQuote(): void {
    if (!this.quote) return;
    this.isSubmitting = true;
    this.portalService.acceptQuote(this.quote.id).subscribe({
      next: (res) => {
        this.quote = res;
        this.isSubmitting = false;
        this.showAcceptModal = false;
        this.successMessage = 'Quote accepted successfully! Your rental proposal is confirmed.';
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.error || 'Failed to accept quote.';
      }
    });
  }

  submitRequestChanges(): void {
    if (!this.quote || !this.changeMessage) return;
    this.isSubmitting = true;
    this.portalService.requestQuoteChanges(this.quote.id, this.changeMessage).subscribe({
      next: (res) => {
        this.quote = res;
        this.isSubmitting = false;
        this.showRequestChangesModal = false;
        this.successMessage = 'Your change request has been sent to our sales team.';
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.error || 'Failed to send change request.';
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACCEPTED': return 'bg-success';
      case 'SENT': return 'bg-primary';
      case 'CHANGE_REQUESTED': return 'bg-warning text-dark';
      case 'EXPIRED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
