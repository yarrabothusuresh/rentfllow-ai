import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReturnsService, ReturnOrder } from '../../services/returns.service';

@Component({
  selector: 'app-returns-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="returns-list-container">
      <div class="page-header">
        <div>
          <h2>Return Orders</h2>
          <p class="subtitle">Manage rental equipment returns, pickup logistics, and condition inspections</p>
        </div>
        <div class="header-actions">
          <a routerLink="/returns/dashboard" class="btn btn-outline-primary">
            <i class="bi bi-speedometer2"></i> Dashboard
          </a>
          <a routerLink="/returns/inspection" class="btn btn-outline-secondary">
            <i class="bi bi-search"></i> Inspections
          </a>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="filter-card">
        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label">Search</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-search"></i></span>
              <input type="text" class="form-control" placeholder="Return #, Booking #, Customer..." [(ngModel)]="searchQuery" (ngModelChange)="loadReturns()">
            </div>
          </div>
          <div class="col-md-2">
            <label class="form-label">Status</label>
            <select class="form-select" [(ngModel)]="selectedStatus" (change)="loadReturns()">
              <option value="">All Statuses</option>
              <option value="PENDING">PENDING</option>
              <option value="SCHEDULED">SCHEDULED</option>
              <option value="ASSIGNED">ASSIGNED</option>
              <option value="READY_FOR_PICKUP">READY FOR PICKUP</option>
              <option value="OUT_FOR_PICKUP">OUT FOR PICKUP</option>
              <option value="ARRIVED">ARRIVED</option>
              <option value="PICKED_UP">PICKED UP</option>
              <option value="CHECK_IN">CHECK IN</option>
              <option value="INSPECTION">INSPECTION</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label">Pickup Date</label>
            <input type="date" class="form-control" [(ngModel)]="selectedDate" (change)="loadReturns()">
          </div>
          <div class="col-md-2">
            <label class="form-label">Priority</label>
            <select class="form-select" [(ngModel)]="selectedPriority" (change)="loadReturns()">
              <option value="">All Priorities</option>
              <option value="LOW">LOW</option>
              <option value="NORMAL">NORMAL</option>
              <option value="HIGH">HIGH</option>
              <option value="URGENT">URGENT</option>
            </select>
          </div>
          <div class="col-md-3 d-flex align-items-end">
            <button class="btn btn-light w-100 me-2" (click)="resetFilters()">Reset Filters</button>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="table-card">
        <div class="table-responsive">
          <table class="table hover-table align-middle">
            <thead class="table-light">
              <tr>
                <th>Return #</th>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Event</th>
                <th>Pickup Date</th>
                <th>Driver</th>
                <th>Vehicle</th>
                <th>Status</th>
                <th>Missing</th>
                <th>Damaged</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of returns" (click)="viewDetail(item.id)" class="clickable-row">
                <td class="fw-bold text-primary">{{ item.returnNumber }}</td>
                <td>{{ item.bookingNumber || 'BOOK-000123' }}</td>
                <td>{{ item.customerName || 'ABC Events LLC' }}</td>
                <td>{{ item.eventName || 'Wedding Reception' }}</td>
                <td>
                  <div>{{ item.scheduledDate || 'Today' }}</div>
                  <small class="text-muted" *ngIf="item.scheduledStartTime">{{ item.scheduledStartTime }} - {{ item.scheduledEndTime }}</small>
                </td>
                <td>
                  <span *ngIf="item.driverName" class="badge bg-light text-dark"><i class="bi bi-person"></i> {{ item.driverName }}</span>
                  <span *ngIf="!item.driverName" class="text-muted fst-italic">Unassigned</span>
                </td>
                <td>
                  <span *ngIf="item.vehicleNumber" class="badge bg-light text-dark"><i class="bi bi-truck"></i> {{ item.vehicleNumber }}</span>
                  <span *ngIf="!item.vehicleNumber" class="text-muted fst-italic">Unassigned</span>
                </td>
                <td>
                  <span class="badge status-badge" [ngClass]="'status-' + item.status">
                    {{ item.status ? item.status.replace('_', ' ') : '' }}
                  </span>
                </td>
                <td>
                  <span class="badge" [ngClass]="(item.totalMissingItems || 0) > 0 ? 'bg-danger' : 'bg-light text-muted'">
                    {{ item.totalMissingItems || 0 }}
                  </span>
                </td>
                <td>
                  <span class="badge" [ngClass]="(item.totalDamagedItems || 0) > 0 ? 'bg-warning text-dark' : 'bg-light text-muted'">
                    {{ item.totalDamagedItems || 0 }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary" (click)="$event.stopPropagation(); viewDetail(item.id)">
                    Manage
                  </button>
                </td>
              </tr>
              <tr *ngIf="returns.length === 0">
                <td colspan="11" class="text-center py-5 text-muted">
                  <i class="bi bi-search fs-2"></i>
                  <p class="mt-2">No return orders found matching criteria.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .returns-list-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .subtitle {
      color: #6c757d;
    }
    .filter-card {
      background: #ffffff;
      padding: 1.25rem;
      border-radius: 12px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
      margin-bottom: 1.5rem;
    }
    .table-card {
      background: #ffffff;
      border-radius: 12px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
      overflow: hidden;
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
  `]
})
export class ReturnsListComponent implements OnInit {
  returns: ReturnOrder[] = [];
  searchQuery: string = '';
  selectedStatus: string = '';
  selectedDate: string = '';
  selectedPriority: string = '';

  constructor(
    private returnsService: ReturnsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReturns();
  }

  loadReturns(): void {
    this.returnsService.getReturns({
      search: this.searchQuery,
      status: this.selectedStatus,
      date: this.selectedDate,
      priority: this.selectedPriority
    }).subscribe(data => {
      this.returns = data || [];
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedDate = '';
    this.selectedPriority = '';
    this.loadReturns();
  }

  viewDetail(id: string): void {
    this.router.navigate(['/returns', id]);
  }
}
