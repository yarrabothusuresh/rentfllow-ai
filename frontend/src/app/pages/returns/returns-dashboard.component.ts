import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReturnsService, ReturnsDashboard, ReturnOrder } from '../../services/returns.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-returns-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="returns-dashboard-container">
      <div class="header-section">
        <div>
          <h2>Returns & Pickup Operations</h2>
          <p class="subtitle">Real-time scheduling, driver pickups, equipment check-in & inspection tracking</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-primary" (click)="openQuickReturnModal()">
            <i class="bi bi-plus-circle"></i> Create Return Order
          </button>
          <a routerLink="/returns" class="btn btn-secondary">
            <i class="bi bi-list-task"></i> View All Returns
          </a>
        </div>
      </div>

      <!-- KPI Dashboard Cards -->
      <div class="metrics-grid" *ngIf="metrics">
        <div class="metric-card bg-primary-subtle" (click)="filterQueue('ALL')">
          <div class="card-icon text-primary"><i class="bi bi-truck-flatbed"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.todaysPickups }}</span>
            <span class="metric-label">Today's Pickups</span>
          </div>
        </div>

        <div class="metric-card bg-warning-subtle" (click)="filterQueue('UNASSIGNED')">
          <div class="card-icon text-warning"><i class="bi bi-exclamation-triangle"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.unassigned }}</span>
            <span class="metric-label">Unassigned</span>
          </div>
        </div>

        <div class="metric-card bg-info-subtle" (click)="filterQueue('SCHEDULED')">
          <div class="card-icon text-info"><i class="bi bi-calendar-event"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.scheduled }}</span>
            <span class="metric-label">Scheduled</span>
          </div>
        </div>

        <div class="metric-card bg-purple-subtle" (click)="filterQueue('OUT_FOR_PICKUP')">
          <div class="card-icon text-purple"><i class="bi bi-geo-alt"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.outForPickup }}</span>
            <span class="metric-label">Out for Pickup</span>
          </div>
        </div>

        <div class="metric-card bg-teal-subtle" (click)="filterQueue('PICKED_UP')">
          <div class="card-icon text-teal"><i class="bi bi-box-arrow-in-down"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.pickedUp }}</span>
            <span class="metric-label">Picked Up</span>
          </div>
        </div>

        <div class="metric-card bg-orange-subtle" routerLink="/returns/inspection">
          <div class="card-icon text-orange"><i class="bi bi-search"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.pendingInspection }}</span>
            <span class="metric-label">Pending Inspection</span>
          </div>
        </div>

        <div class="metric-card bg-danger-subtle" routerLink="/returns/damage">
          <div class="card-icon text-danger"><i class="bi bi-question-diamond"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.missingItems }}</span>
            <span class="metric-label">Missing Items</span>
          </div>
        </div>

        <div class="metric-card bg-rose-subtle" routerLink="/inventory/damage">
          <div class="card-icon text-rose"><i class="bi bi-tools"></i></div>
          <div class="metric-body">
            <span class="metric-value">{{ metrics.damagedItems }}</span>
            <span class="metric-label">Damaged Items</span>
          </div>
        </div>
      </div>

      <!-- Today's Queue Section -->
      <div class="queue-section">
        <div class="queue-header">
          <h3>Today's Pickup Queue</h3>
          <div class="search-box">
            <i class="bi bi-search"></i>
            <input type="text" placeholder="Search return #, booking, customer..." [(ngModel)]="searchTerm" (ngModelChange)="applyFilter()">
          </div>
        </div>

        <div class="table-responsive" *ngIf="filteredQueue.length > 0">
          <table class="table hover-table">
            <thead>
              <tr>
                <th>Return #</th>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Event</th>
                <th>Pickup Window</th>
                <th>Driver</th>
                <th>Vehicle</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of filteredQueue" (click)="viewReturn(item.id)" class="clickable-row">
                <td class="fw-bold text-primary">{{ item.returnNumber }}</td>
                <td>{{ item.bookingNumber || 'BOOK-000123' }}</td>
                <td>{{ item.customerName || 'ABC Events LLC' }}</td>
                <td>{{ item.eventName || 'Wedding Reception' }}</td>
                <td>
                  <span class="badge bg-light text-dark">
                    <i class="bi bi-clock"></i> {{ item.scheduledStartTime || '10:00' }} - {{ item.scheduledEndTime || '12:00' }}
                  </span>
                </td>
                <td>
                  <span *ngIf="item.driverName" class="driver-pill"><i class="bi bi-person"></i> {{ item.driverName }}</span>
                  <span *ngIf="!item.driverName" class="badge bg-danger-subtle text-danger">Unassigned</span>
                </td>
                <td>
                  <span *ngIf="item.vehicleNumber" class="vehicle-pill"><i class="bi bi-truck"></i> {{ item.vehicleNumber }}</span>
                  <span *ngIf="!item.vehicleNumber" class="badge bg-secondary-subtle text-secondary">Unassigned</span>
                </td>
                <td>
                  <span class="badge status-badge" [ngClass]="getStatusClass(item.status)">
                    {{ formatStatus(item.status) }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary" (click)="$event.stopPropagation(); viewReturn(item.id)">
                    Open <i class="bi bi-arrow-right-short"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="empty-state" *ngIf="filteredQueue.length === 0">
          <i class="bi bi-inbox text-muted fs-1"></i>
          <p>No pickups scheduled for today match your filter criteria.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .returns-dashboard-container {
      padding: 1.5rem;
    }
    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .subtitle {
      color: var(--color-text-muted, #6c757d);
      margin-top: 0.25rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }
    .metric-card {
      display: flex;
      align-items: center;
      padding: 1.25rem;
      border-radius: 12px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .metric-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    }
    .card-icon {
      font-size: 2rem;
      margin-right: 1rem;
    }
    .metric-body {
      display: flex;
      flex-direction: column;
    }
    .metric-value {
      font-size: 1.75rem;
      font-weight: 700;
      line-height: 1.2;
    }
    .metric-label {
      font-size: 0.85rem;
      color: #495057;
      font-weight: 500;
    }
    .bg-primary-subtle { background-color: #e7f1ff; }
    .bg-warning-subtle { background-color: #fff3cd; }
    .bg-info-subtle { background-color: #cff4fc; }
    .bg-purple-subtle { background-color: #f3e8ff; }
    .bg-teal-subtle { background-color: #e6fffa; }
    .bg-orange-subtle { background-color: #ffedd5; }
    .bg-danger-subtle { background-color: #f8d7da; }
    .bg-rose-subtle { background-color: #ffe4e6; }
    .text-purple { color: #8b5cf6; }
    .text-teal { color: #0d9488; }
    .text-orange { color: #ea580c; }
    .text-rose { color: #e11d48; }

    .queue-section {
      background: #ffffff;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }
    .queue-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    .search-box {
      position: relative;
      width: 300px;
    }
    .search-box i {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      color: #6c757d;
    }
    .search-box input {
      width: 100%;
      padding: 0.5rem 0.75rem 0.5rem 2.25rem;
      border-radius: 8px;
      border: 1px solid #ced4da;
    }
    .driver-pill, .vehicle-pill {
      font-size: 0.85rem;
      background: #f1f5f9;
      padding: 0.25rem 0.5rem;
      border-radius: 6px;
    }
    .clickable-row {
      cursor: pointer;
    }
    .status-badge {
      padding: 0.35rem 0.65rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .status-PENDING { background-color: #f1f5f9; color: #475569; }
    .status-SCHEDULED { background-color: #e0f2fe; color: #0369a1; }
    .status-ASSIGNED { background-color: #dbeafe; color: #1e40af; }
    .status-READY_FOR_PICKUP { background-color: #fef3c7; color: #b45309; }
    .status-OUT_FOR_PICKUP { background-color: #f3e8ff; color: #6b21a8; }
    .status-ARRIVED { background-color: #fae8ff; color: #86198f; }
    .status-PICKED_UP { background-color: #ccfbf1; color: #0f766e; }
    .status-CHECK_IN { background-color: #ffedd5; color: #c2410c; }
    .status-INSPECTION { background-color: #fed7aa; color: #9a3412; }
    .status-COMPLETED { background-color: #dcfce7; color: #15803d; }
    .empty-state {
      text-align: center;
      padding: 3rem 1rem;
    }
  `]
})
export class ReturnsDashboardComponent implements OnInit {
  metrics: ReturnsDashboard | null = null;
  filteredQueue: ReturnOrder[] = [];
  searchTerm: string = '';

  constructor(
    private returnsService: ReturnsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.returnsService.getReturnsDashboard().subscribe(data => {
      this.metrics = data;
      this.filteredQueue = data.todaysQueue || [];
    });
  }

  filterQueue(statusFilter: string): void {
    if (!this.metrics || !this.metrics.todaysQueue) return;
    if (statusFilter === 'ALL') {
      this.filteredQueue = this.metrics.todaysQueue;
    } else if (statusFilter === 'UNASSIGNED') {
      this.filteredQueue = this.metrics.todaysQueue.filter(r => !r.driverId);
    } else {
      this.filteredQueue = this.metrics.todaysQueue.filter(r => r.status === statusFilter);
    }
  }

  applyFilter(): void {
    if (!this.metrics || !this.metrics.todaysQueue) return;
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.filteredQueue = this.metrics.todaysQueue;
    } else {
      this.filteredQueue = this.metrics.todaysQueue.filter(r =>
        r.returnNumber.toLowerCase().includes(term) ||
        (r.bookingNumber && r.bookingNumber.toLowerCase().includes(term)) ||
        (r.customerName && r.customerName.toLowerCase().includes(term)) ||
        (r.eventName && r.eventName.toLowerCase().includes(term))
      );
    }
  }

  viewReturn(id: string): void {
    this.router.navigate(['/returns', id]);
  }

  openQuickReturnModal(): void {
    this.router.navigate(['/returns']);
  }

  formatStatus(status: string): string {
    return status ? status.replace(/_/g, ' ') : '';
  }

  getStatusClass(status: string): string {
    return 'status-' + status;
  }
}
