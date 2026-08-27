import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { DamageClaimsService, RepairDashboard, RepairOrder } from '../../services/damage-claims.service';

@Component({
  selector: 'app-repairs-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="repairs-dashboard-container" *ngIf="dashboard">
      <div class="page-header mb-4">
        <div>
          <h2>Maintenance & Repair Operations</h2>
          <p class="text-muted mb-0">Equipment repair tracking, technician assignments, repair cost tracking, and inventory restoration</p>
        </div>
        <a routerLink="/damage-claims" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i> Damage Claims
        </a>
      </div>

      <!-- KPI Cards Grid (Step 14) -->
      <div class="row g-3 mb-4">
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-warning-subtle text-warning-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Pending Repairs</span>
              <h2 class="fw-bold my-1">{{ dashboard.pendingRepairs }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-purple-subtle text-purple-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">In Progress</span>
              <h2 class="fw-bold my-1">{{ dashboard.inProgress }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-success-subtle text-success-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Completed</span>
              <h2 class="fw-bold my-1">{{ dashboard.completed }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-danger-subtle text-danger-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Failed</span>
              <h2 class="fw-bold my-1">{{ dashboard.failed }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-info-subtle text-info-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Est. Repair Cost</span>
              <h3 class="fw-bold my-1">$ {{ dashboard.estimatedCost | number:'1.2-2' }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card metric-card bg-dark-subtle text-dark-emphasis">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Actual Cost</span>
              <h3 class="fw-bold my-1">$ {{ dashboard.actualCost | number:'1.2-2' }}</h3>
            </div>
          </div>
        </div>
      </div>

      <!-- Repair Orders Queue -->
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-tools"></i> Active Maintenance & Repair Orders</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Repair #</th>
                <th>Claim #</th>
                <th>Product</th>
                <th class="text-center">Quantity</th>
                <th>Status</th>
                <th>Assigned To</th>
                <th class="text-end">Est Cost</th>
                <th class="text-end">Actual Cost</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of dashboard.recentRepairs">
                <td class="fw-bold text-primary">{{ item.repairNumber }}</td>
                <td>{{ item.claimNumber || 'CLM-000123' }}</td>
                <td>
                  <div class="fw-bold">{{ item.productName || 'Chiavari Chair' }}</div>
                  <small class="text-muted">{{ item.productSku || 'SKU-CHAIR-01' }}</small>
                </td>
                <td class="text-center"><span class="badge bg-secondary fs-6">{{ item.quantity }}</span></td>
                <td>
                  <span class="badge status-badge" [ngClass]="'status-' + item.status">
                    {{ item.status.replace('_', ' ') }}
                  </span>
                </td>
                <td><span class="badge bg-light text-dark"><i class="bi bi-person"></i> {{ item.assignedTo || 'Warehouse Tech' }}</span></td>
                <td class="text-end fw-semibold">$ {{ item.estimatedCost | number:'1.2-2' }}</td>
                <td class="text-end fw-bold text-success">$ {{ item.actualCost | number:'1.2-2' }}</td>
                <td>
                  <a [routerLink]="['/maintenance', item.id]" class="btn btn-sm btn-outline-primary">
                    Manage Repair
                  </a>
                </td>
              </tr>
              <tr *ngIf="dashboard.recentRepairs.length === 0">
                <td colspan="9" class="text-center py-5 text-muted">No maintenance orders found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .repairs-dashboard-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .metric-card {
      border-radius: 12px;
      border: none;
    }
    .bg-warning-subtle { background-color: #fef3c7; color: #b45309; }
    .bg-purple-subtle { background-color: #f3e8ff; color: #6b21a8; }
    .bg-success-subtle { background-color: #dcfce7; color: #15803d; }
    .bg-danger-subtle { background-color: #fee2e2; color: #b91c1c; }
    .bg-info-subtle { background-color: #e0f2fe; color: #0369a1; }
    .bg-dark-subtle { background-color: #f1f5f9; color: #334155; }

    .status-badge {
      padding: 0.35rem 0.65rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .status-PENDING { background-color: #fef3c7; color: #b45309; }
    .status-ASSIGNED { background-color: #dbeafe; color: #1e40af; }
    .status-IN_PROGRESS { background-color: #f3e8ff; color: #6b21a8; }
    .status-COMPLETED { background-color: #dcfce7; color: #15803d; }
    .status-FAILED { background-color: #fee2e2; color: #b91c1c; }
  `]
})
export class RepairsDashboardComponent implements OnInit {
  dashboard: RepairDashboard | null = null;

  constructor(private claimsService: DamageClaimsService) {}

  ngOnInit(): void {
    this.claimsService.getRepairDashboard().subscribe(data => {
      this.dashboard = data;
    });
  }
}
