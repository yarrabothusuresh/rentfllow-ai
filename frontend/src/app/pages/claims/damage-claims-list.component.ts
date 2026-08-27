import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DamageClaimsService, DamageClaim } from '../../services/damage-claims.service';

@Component({
  selector: 'app-damage-claims-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="claims-list-container">
      <div class="page-header mb-4">
        <div>
          <h2>Damage Claims Log</h2>
          <p class="text-muted mb-0">Search and filter equipment damage claims, costs, and resolution statuses</p>
        </div>
        <div class="header-actions">
          <a routerLink="/damage-claims/dashboard" class="btn btn-outline-primary me-2">
            <i class="bi bi-speedometer2"></i> Dashboard
          </a>
          <a routerLink="/maintenance" class="btn btn-outline-secondary">
            <i class="bi bi-tools"></i> Maintenance Center
          </a>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="card filter-card mb-4">
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-3">
              <label class="form-label">Search</label>
              <div class="input-group">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" class="form-control" placeholder="Claim #, Booking #, Customer..." [(ngModel)]="searchQuery" (ngModelChange)="loadClaims()">
              </div>
            </div>
            <div class="col-md-2">
              <label class="form-label">Status</label>
              <select class="form-select" [(ngModel)]="selectedStatus" (change)="loadClaims()">
                <option value="">All Statuses</option>
                <option value="OPEN">OPEN</option>
                <option value="UNDER_REVIEW">UNDER REVIEW</option>
                <option value="ESTIMATE_CREATED">ESTIMATE CREATED</option>
                <option value="CUSTOMER_REVIEW">CUSTOMER REVIEW</option>
                <option value="APPROVED">APPROVED</option>
                <option value="DISPUTED">DISPUTED</option>
                <option value="REPAIR_IN_PROGRESS">REPAIR IN PROGRESS</option>
                <option value="REPLACEMENT_REQUIRED">REPLACEMENT REQUIRED</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="WAIVED">WAIVED</option>
              </select>
            </div>
            <div class="col-md-2">
              <label class="form-label">Claim Type</label>
              <select class="form-select" [(ngModel)]="selectedType" (change)="loadClaims()">
                <option value="">All Types</option>
                <option value="DAMAGE">DAMAGE</option>
                <option value="MISSING">MISSING</option>
                <option value="LOST">LOST</option>
                <option value="REPAIR">REPAIR</option>
                <option value="REPLACEMENT">REPLACEMENT</option>
                <option value="MIXED">MIXED</option>
              </select>
            </div>
            <div class="col-md-2">
              <label class="form-label">Priority</label>
              <select class="form-select" [(ngModel)]="selectedPriority" (change)="loadClaims()">
                <option value="">All Priorities</option>
                <option value="LOW">LOW</option>
                <option value="NORMAL">NORMAL</option>
                <option value="HIGH">HIGH</option>
                <option value="URGENT">URGENT</option>
              </select>
            </div>
            <div class="col-md-3 d-flex align-items-end">
              <button class="btn btn-light w-100" (click)="resetFilters()">Reset Filters</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="card shadow-sm border-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Claim #</th>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Claim Type</th>
                <th>Items Count</th>
                <th>Est. Cost</th>
                <th>Approved / Final</th>
                <th>Status</th>
                <th>Reported At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let claim of claims" (click)="viewDetail(claim.id)" class="clickable-row">
                <td class="fw-bold text-primary">{{ claim.claimNumber }}</td>
                <td>{{ claim.bookingNumber || 'BOOK-000123' }}</td>
                <td>{{ claim.customerName || 'ABC Events LLC' }}</td>
                <td><span class="badge bg-light text-dark">{{ claim.claimType }}</span></td>
                <td><span class="badge bg-secondary">{{ claim.items ? claim.items.length : 0 }}</span></td>
                <td class="fw-bold text-danger">$ {{ claim.estimatedTotalCost | number:'1.2-2' }}</td>
                <td class="fw-bold text-success">
                  {{ (claim.finalTotalCost || claim.approvedTotalCost) ? ('$' + ((claim.finalTotalCost || claim.approvedTotalCost) | number:'1.2-2')) : '-' }}
                </td>
                <td>
                  <span class="badge status-badge" [ngClass]="'status-' + claim.status">
                    {{ claim.status.replace('_', ' ') }}
                  </span>
                </td>
                <td class="small text-muted">{{ claim.reportedAt ? (claim.reportedAt | date:'shortDate') : 'Today' }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-primary" (click)="$event.stopPropagation(); viewDetail(claim.id)">
                    Manage
                  </button>
                </td>
              </tr>
              <tr *ngIf="claims.length === 0">
                <td colspan="10" class="text-center py-5 text-muted">
                  <i class="bi bi-shield-check fs-1 text-success"></i>
                  <p class="mt-2">No claims matching criteria.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .claims-list-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .filter-card {
      border-radius: 12px;
      border: 1px solid #e2e8f0;
    }
    .clickable-row {
      cursor: pointer;
    }
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
export class DamageClaimsListComponent implements OnInit {
  claims: DamageClaim[] = [];
  searchQuery = '';
  selectedStatus = '';
  selectedType = '';
  selectedPriority = '';

  constructor(
    private claimsService: DamageClaimsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.claimsService.getClaims({
      search: this.searchQuery,
      status: this.selectedStatus,
      type: this.selectedType,
      priority: this.selectedPriority
    }).subscribe(data => {
      this.claims = data || [];
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedType = '';
    this.selectedPriority = '';
    this.loadClaims();
  }

  viewDetail(id: string): void {
    this.router.navigate(['/damage-claims', id]);
  }
}
