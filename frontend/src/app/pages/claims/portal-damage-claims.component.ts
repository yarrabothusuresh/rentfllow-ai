import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DamageClaimsService, DamageClaim } from '../../services/damage-claims.service';

@Component({
  selector: 'app-portal-damage-claims',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="portal-claims-container">
      <div class="page-header mb-4">
        <div>
          <h2>Rental Return Claims</h2>
          <p class="text-muted mb-0">Review equipment return condition reports, estimates, and submit feedback or approvals</p>
        </div>
      </div>

      <div class="row g-4" *ngFor="let claim of claims">
        <div class="col-12">
          <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <div>
                <h5 class="mb-0 fw-bold text-primary">Claim {{ claim.claimNumber }}</h5>
                <small class="text-muted">Associated Booking: {{ claim.bookingNumber || 'BOOK-000123' }}</small>
              </div>
              <span class="badge status-badge" [ngClass]="'status-' + claim.status">
                {{ claim.status.replace('_', ' ') }}
              </span>
            </div>
            <div class="card-body">
              <p class="text-secondary">{{ claim.description }}</p>

              <div class="table-responsive mb-3">
                <table class="table table-sm align-middle">
                  <thead class="table-light">
                    <tr>
                      <th>Item Description</th>
                      <th class="text-center">Qty</th>
                      <th>Issue</th>
                      <th class="text-end">Estimated Cost</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let item of claim.items">
                      <td class="fw-bold">{{ item.productNameSnapshot }}</td>
                      <td class="text-center">{{ item.quantity }}</td>
                      <td><span class="badge bg-light text-dark">{{ item.claimType }}</span></td>
                      <td class="text-end fw-bold text-danger">$ {{ item.estimatedCost | number:'1.2-2' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                <div>
                  <span class="text-muted small">Total Estimated Cost: </span>
                  <strong class="fs-5 text-danger">$ {{ claim.estimatedTotalCost | number:'1.2-2' }}</strong>
                </div>

                <div class="btn-group" *ngIf="claim.status === 'CUSTOMER_REVIEW' || claim.status === 'ESTIMATE_CREATED'">
                  <button class="btn btn-success" (click)="approve(claim.id)">
                    <i class="bi bi-check-circle"></i> Approve & Accept Charge
                  </button>
                  <button class="btn btn-outline-danger" (click)="dispute(claim.id)">
                    <i class="bi bi-exclamation-triangle"></i> Dispute Claim
                  </button>
                </div>

                <div *ngIf="claim.status === 'APPROVED'" class="text-success fw-bold">
                  <i class="bi bi-check-circle-fill"></i> Approved by Customer
                </div>

                <div *ngIf="claim.status === 'DISPUTED'" class="text-danger fw-bold">
                  <i class="bi bi-exclamation-triangle-fill"></i> Under Dispute Review
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="claims.length === 0" class="card shadow-sm border-0 text-center py-5">
        <i class="bi bi-shield-check fs-1 text-success"></i>
        <h5 class="mt-3">No Pending Damage Claims</h5>
        <p class="text-muted">All equipment returns were completed without damages or missing items.</p>
      </div>
    </div>
  `,
  styles: [`
    .portal-claims-container {
      padding: 1.5rem;
    }
    .status-badge {
      padding: 0.35rem 0.65rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .status-ESTIMATE_CREATED { background-color: #ddd6fe; color: #5b21b6; }
    .status-CUSTOMER_REVIEW { background-color: #fef3c7; color: #b45309; }
    .status-APPROVED { background-color: #dcfce7; color: #15803d; }
    .status-DISPUTED { background-color: #fee2e2; color: #b91c1c; }
    .status-RESOLVED { background-color: #d1fae5; color: #065f46; }
  `]
})
export class PortalDamageClaimsComponent implements OnInit {
  claims: DamageClaim[] = [];

  constructor(private claimsService: DamageClaimsService) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.claimsService.getClaims().subscribe(data => {
      this.claims = data || [];
    });
  }

  approve(id: string): void {
    this.claimsService.customerApprove(id).subscribe(() => this.loadClaims());
  }

  dispute(id: string): void {
    const reason = prompt('Please describe why you are disputing this claim:');
    if (reason) {
      this.claimsService.customerDispute(id, reason).subscribe(() => this.loadClaims());
    }
  }
}
