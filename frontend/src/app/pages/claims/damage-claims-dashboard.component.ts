import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DamageClaimsService, ClaimDashboard } from '../../services/damage-claims.service';

@Component({
  selector: 'app-damage-claims-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="claims-dashboard-container" *ngIf="dashboard">
      <!-- Header -->
      <div class="page-header mb-4">
        <div>
          <h2>Damage Claims & Loss Control</h2>
          <p class="text-muted mb-0">Equipment damage assessments, financial claims, customer reviews, and repair/replacement tracking</p>
        </div>
        <div class="header-actions">
          <a routerLink="/damage-claims" class="btn btn-primary me-2">
            <i class="bi bi-list-task"></i> All Claims
          </a>
          <a routerLink="/maintenance" class="btn btn-outline-secondary me-2">
            <i class="bi bi-tools"></i> Maintenance Center
          </a>
          <a routerLink="/replacements" class="btn btn-outline-secondary">
            <i class="bi bi-box-seam"></i> Replacements
          </a>
        </div>
      </div>

      <!-- Financial Metric Header Card -->
      <div class="card exposure-card text-white mb-4">
        <div class="card-body py-4">
          <div class="row align-items-center">
            <div class="col-md-4 border-end border-light">
              <span class="text-uppercase tracking-wider opacity-75 small">Total Estimated Financial Exposure</span>
              <h2 class="display-6 fw-bold my-1">$ {{ dashboard.estimatedExposure | number:'1.2-2' }}</h2>
              <small class="opacity-75">Configured Tenant Currency: {{ dashboard.currency }}</small>
            </div>
            <div class="col-md-4 border-end border-light">
              <span class="text-uppercase tracking-wider opacity-75 small">Approved Claims Value</span>
              <h3 class="fw-bold my-1">$ {{ dashboard.approvedTotal | number:'1.2-2' }}</h3>
              <small class="opacity-75">Customer Approved / Billing Ready</small>
            </div>
            <div class="col-md-4">
              <span class="text-uppercase tracking-wider opacity-75 small">Resolved Claims Value</span>
              <h3 class="fw-bold my-1">$ {{ dashboard.resolvedTotal | number:'1.2-2' }}</h3>
              <small class="opacity-75">Fully Restored / Written Off</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 8 Metric Cards Grid -->
      <div class="row g-3 mb-4">
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-primary-subtle text-primary-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Open Claims</span>
              <h3 class="fw-bold my-1">{{ dashboard.openClaims }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-info-subtle text-info-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Under Review</span>
              <h3 class="fw-bold my-1">{{ dashboard.underReview }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-warning-subtle text-warning-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Customer Review</span>
              <h3 class="fw-bold my-1">{{ dashboard.customerReview }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-success-subtle text-success-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Approved</span>
              <h3 class="fw-bold my-1">{{ dashboard.approved }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-danger-subtle text-danger-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Disputed</span>
              <h3 class="fw-bold my-1">{{ dashboard.disputed }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-purple-subtle text-purple-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Repair In Progress</span>
              <h3 class="fw-bold my-1">{{ dashboard.repairInProgress }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-orange-subtle text-orange-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Replacement Required</span>
              <h3 class="fw-bold my-1">{{ dashboard.replacementRequired }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card metric-card bg-dark-subtle text-dark-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Resolved</span>
              <h3 class="fw-bold my-1">{{ dashboard.resolved }}</h3>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Claims Queue -->
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-clock-history"></i> Recent Claims Queue</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Claim #</th>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Type</th>
                <th>Est. Cost</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of dashboard.recentClaims">
                <td class="fw-bold text-primary">{{ item.claimNumber }}</td>
                <td>{{ item.bookingNumber || 'BOOK-000123' }}</td>
                <td>{{ item.customerName || 'ABC Events LLC' }}</td>
                <td><span class="badge bg-light text-dark">{{ item.claimType }}</span></td>
                <td class="fw-bold text-danger">$ {{ item.estimatedTotalCost | number:'1.2-2' }}</td>
                <td>
                  <span class="badge status-badge" [ngClass]="'status-' + item.status">
                    {{ item.status.replace('_', ' ') }}
                  </span>
                </td>
                <td>
                  <a [routerLink]="['/damage-claims', item.id]" class="btn btn-sm btn-outline-primary">
                    Manage
                  </a>
                </td>
              </tr>
              <tr *ngIf="dashboard.recentClaims.length === 0">
                <td colspan="7" class="text-center py-4 text-muted">No damage claims recorded.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .claims-dashboard-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .exposure-card {
      background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
      border-radius: 16px;
      border: none;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
    .metric-card {
      border-radius: 12px;
      border: none;
    }
    .bg-primary-subtle { background-color: #e0f2fe; color: #0369a1; }
    .bg-info-subtle { background-color: #e0e7ff; color: #3730a3; }
    .bg-warning-subtle { background-color: #fef3c7; color: #b45309; }
    .bg-success-subtle { background-color: #dcfce7; color: #15803d; }
    .bg-danger-subtle { background-color: #fee2e2; color: #b91c1c; }
    .bg-purple-subtle { background-color: #f3e8ff; color: #6b21a8; }
    .bg-orange-subtle { background-color: #ffedd5; color: #c2410c; }
    .bg-dark-subtle { background-color: #f1f5f9; color: #334155; }

    .status-badge {
      padding: 0.35rem 0.65rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .status-OPEN { background-color: #e0f2fe; color: #0369a1; }
    .status-UNDER_REVIEW { background-color: #e0e7ff; color: #3730a3; }
    .status-ESTIMATE_CREATED { background-color: #ddd6fe; color: #5b21b6; }
    .status-CUSTOMER_REVIEW { background-color: #fef3c7; color: #b45309; }
    .status-APPROVED { background-color: #dcfce7; color: #15803d; }
    .status-DISPUTED { background-color: #fee2e2; color: #b91c1c; }
    .status-REPAIR_IN_PROGRESS { background-color: #f3e8ff; color: #6b21a8; }
    .status-REPLACEMENT_REQUIRED { background-color: #ffedd5; color: #c2410c; }
    .status-RESOLVED { background-color: #d1fae5; color: #065f46; }
    .status-WAIVED { background-color: #f1f5f9; color: #475569; }
  `]
})
export class DamageClaimsDashboardComponent implements OnInit {
  dashboard: ClaimDashboard | null = null;

  constructor(private claimsService: DamageClaimsService) {}

  ngOnInit(): void {
    this.claimsService.getDashboard().subscribe(data => {
      this.dashboard = data;
    });
  }
}
