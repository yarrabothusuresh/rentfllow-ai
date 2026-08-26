import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReturnsService, InspectionDashboard, ReturnOrder } from '../../services/returns.service';

@Component({
  selector: 'app-returns-inspection',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="inspection-dashboard-container">
      <div class="header-section mb-4">
        <div>
          <h2>Warehouse Inspection Center</h2>
          <p class="text-muted">Equipment check-in, condition grading, damage assessments, and maintenance routing</p>
        </div>
        <a routerLink="/returns" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i> All Returns
        </a>
      </div>

      <!-- KPI Cards -->
      <div class="row g-3 mb-4" *ngIf="dashboard">
        <div class="col-md-2 col-6">
          <div class="card card-metric bg-warning-subtle text-warning-emphasis">
            <div class="card-body">
              <span class="fs-6 text-muted fw-semibold">Pending Inspection</span>
              <h2 class="fw-bold my-1">{{ dashboard.pendingInspection }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card card-metric bg-success-subtle text-success-emphasis">
            <div class="card-body">
              <span class="fs-6 text-muted fw-semibold">Good Quantity</span>
              <h2 class="fw-bold my-1">{{ dashboard.goodItems }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-2 col-6">
          <div class="card card-metric bg-danger-subtle text-danger-emphasis">
            <div class="card-body">
              <span class="fs-6 text-muted fw-semibold">Damaged Quantity</span>
              <h2 class="fw-bold my-1">{{ dashboard.damagedItems }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-6">
          <div class="card card-metric bg-purple-subtle text-purple-emphasis">
            <div class="card-body">
              <span class="fs-6 text-muted fw-semibold">Missing Quantity</span>
              <h2 class="fw-bold my-1">{{ dashboard.missingItems }}</h2>
            </div>
          </div>
        </div>
        <div class="col-md-3 col-12">
          <div class="card card-metric bg-orange-subtle text-orange-emphasis">
            <div class="card-body">
              <span class="fs-6 text-muted fw-semibold">Maintenance Required</span>
              <h2 class="fw-bold my-1">{{ dashboard.maintenanceRequired }}</h2>
            </div>
          </div>
        </div>
      </div>

      <!-- Pending Inspections Queue -->
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-clock-history"></i> Pending Inspection Orders</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0" *ngIf="dashboard && dashboard.pendingInspectionList.length > 0">
            <thead class="table-light">
              <tr>
                <th>Return #</th>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Scheduled Date</th>
                <th>Items Expected</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of dashboard.pendingInspectionList">
                <td class="fw-bold text-primary">{{ item.returnNumber }}</td>
                <td>{{ item.bookingNumber || 'BOOK-000123' }}</td>
                <td>{{ item.customerName || 'ABC Events LLC' }}</td>
                <td>{{ item.scheduledDate || 'Today' }}</td>
                <td><span class="badge bg-light text-dark fs-6">{{ item.totalExpectedItems || 0 }}</span></td>
                <td>
                  <span class="badge bg-warning text-dark">{{ item.status }}</span>
                </td>
                <td>
                  <button class="btn btn-sm btn-primary me-2" (click)="openInspect(item.id)">
                    <i class="bi bi-clipboard-check"></i> Inspect
                  </button>
                  <button class="btn btn-sm btn-outline-secondary" (click)="viewReturn(item.id)">
                    View Return
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="p-5 text-center text-muted" *ngIf="!dashboard || dashboard.pendingInspectionList.length === 0">
            <i class="bi bi-check-circle-fill text-success fs-1"></i>
            <p class="mt-2">All received equipment has been inspected!</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inspection-dashboard-container {
      padding: 1.5rem;
    }
    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .card-metric {
      border-radius: 12px;
      border: none;
    }
    .bg-warning-subtle { background-color: #fff3cd; }
    .bg-success-subtle { background-color: #d1e7dd; }
    .bg-danger-subtle { background-color: #f8d7da; }
    .bg-purple-subtle { background-color: #f3e8ff; }
    .bg-orange-subtle { background-color: #ffedd5; }
  `]
})
export class ReturnsInspectionComponent implements OnInit {
  dashboard: InspectionDashboard | null = null;

  constructor(
    private returnsService: ReturnsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.returnsService.getInspectionDashboard().subscribe(data => {
      this.dashboard = data;
    });
  }

  openInspect(id: string): void {
    this.router.navigate(['/returns', id]);
  }

  viewReturn(id: string): void {
    this.router.navigate(['/returns', id]);
  }
}
