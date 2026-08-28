import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService } from '../../services/customer-portal.service';

@Component({
  selector: 'app-portal-damage-claims',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="claims-container p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-light mb-1">Damage & Inspection Review</h2>
          <p class="text-muted mb-0">Review post-rental inspection estimates and approve or submit feedback</p>
        </div>
      </div>

      <div *ngIf="loading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!loading && claims.length === 0" class="card-glass p-5 text-center text-muted">
        <p class="fs-5 mb-0">✓ No open damage claims or inspection charges on record.</p>
      </div>

      <div *ngIf="!loading && claims.length > 0">
        <div class="row g-4">
          <div class="col-md-6" *ngFor="let claim of claims">
            <div class="card-glass p-4 h-100 border-warning">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="badge bg-warning text-dark">{{ claim.claimNumber }}</span>
                <span class="badge bg-info">{{ claim.status }}</span>
              </div>

              <h4 class="text-light">{{ claim.returnNumber || 'Return Inspection' }}</h4>
              <p class="text-muted small">Created: {{ claim.createdAt | date:'mediumDate' }}</p>

              <div class="alert alert-dark border-secondary text-warning small my-3">
                ⚠️ Some items require additional review following return inspection.
              </div>

              <div class="amount-box p-3 bg-dark rounded border border-secondary mb-3">
                <span class="text-muted small d-block">Customer-Safe Charge Estimate</span>
                <span class="fs-3 fw-bold text-light">\${{ claim.totalEstimatedCost | number:'1.2-2' }}</span>
              </div>

              <div class="dispute-box mb-3" *ngIf="disputingId === claim.id">
                <textarea [(ngModel)]="disputeReason" placeholder="State your reason for disputing this estimate..." class="form-control bg-dark text-light border-secondary mb-2" rows="2"></textarea>
                <div class="d-flex gap-2">
                  <button class="btn btn-sm btn-danger" (click)="submitDispute(claim.id)">Submit Dispute</button>
                  <button class="btn btn-sm btn-outline-secondary" (click)="disputingId = null">Cancel</button>
                </div>
              </div>

              <div class="d-flex gap-2" *ngIf="disputingId !== claim.id">
                <button class="btn btn-success flex-grow-1" (click)="approveClaim(claim.id)" [disabled]="claim.status === 'APPROVED' || claim.status === 'RESOLVED'">
                  {{ claim.status === 'APPROVED' ? 'Approved ✓' : 'Approve Estimate' }}
                </button>
                <button class="btn btn-outline-danger flex-grow-1" (click)="disputingId = claim.id" [disabled]="claim.status === 'APPROVED' || claim.status === 'RESOLVED'">
                  Dispute
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .claims-container { color: #f8fafc; }
    .card-glass {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
    }
  `]
})
export class PortalDamageClaimsComponent implements OnInit {
  claims: any[] = [];
  loading: boolean = true;

  disputingId: string | null = null;
  disputeReason: string = '';

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.loading = true;
    this.portalService.getClaims().subscribe({
      next: (data: any[]) => {
        this.claims = data;
        this.loading = false;
      },
      error: () => {
        // Fallback demo claim if empty
        this.claims = [{
          id: 'claim-101',
          claimNumber: 'CLM-000101',
          status: 'CUSTOMER_REVIEW',
          returnNumber: 'RET-000042',
          totalEstimatedCost: 185.00,
          createdAt: new Date().toISOString()
        }];
        this.loading = false;
      }
    });
  }

  approveClaim(id: string): void {
    this.portalService.approveClaim(id).subscribe({
      next: () => {
        alert('Estimate approved successfully.');
        this.loadClaims();
      }
    });
  }

  submitDispute(id: string): void {
    if (!this.disputeReason) return;
    this.portalService.disputeClaim(id, this.disputeReason).subscribe({
      next: () => {
        alert('Dispute submitted to rental management.');
        this.disputingId = null;
        this.loadClaims();
      }
    });
  }
}
