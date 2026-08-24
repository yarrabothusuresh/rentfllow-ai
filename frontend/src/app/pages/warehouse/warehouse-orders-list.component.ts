import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseService, WarehouseOrder } from '../../services/warehouse.service';

@Component({
  selector: 'app-warehouse-orders-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="warehouse-orders-container">
      <div class="page-header">
        <div>
          <h2>📦 Warehouse Work Orders</h2>
          <p class="subtitle">Manage staging, picking, packing, and dispatch readiness across bookings.</p>
        </div>
        <a routerLink="/warehouse/dashboard" class="btn btn-outline">&larr; Back to Dashboard</a>
      </div>

      <!-- Filters & Search Toolbar -->
      <div class="toolbar">
        <div class="search-box">
          <input
            type="text"
            placeholder="Search Order #, Booking #, Event, Customer..."
            [(ngModel)]="searchQuery"
            (ngModelChange)="onFilterChange()"
            class="form-control"
          />
        </div>

        <div class="filter-group">
          <select [(ngModel)]="selectedStatus" (change)="onFilterChange()" class="form-select">
            <option value="">All Statuses</option>
            <option value="READY_TO_PICK">Ready to Pick</option>
            <option value="PICKING">Picking</option>
            <option value="PICKED">Picked</option>
            <option value="PACKING">Packing</option>
            <option value="PACKED">Packed</option>
            <option value="READY_FOR_DELIVERY">Ready for Delivery</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <select [(ngModel)]="selectedPriority" (change)="onFilterChange()" class="form-select">
            <option value="">All Priorities</option>
            <option value="URGENT">URGENT</option>
            <option value="HIGH">HIGH</option>
            <option value="NORMAL">NORMAL</option>
            <option value="LOW">LOW</option>
          </select>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="table-card">
        <table class="table" *ngIf="orders && orders.length > 0; else noData">
          <thead>
            <tr>
              <th>Work Order</th>
              <th>Booking</th>
              <th>Event & Venue</th>
              <th>Customer</th>
              <th>Event Date</th>
              <th>Priority</th>
              <th>Items Progress</th>
              <th>Status</th>
              <th>Assigned To</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let order of orders">
              <td class="font-bold">{{ order.orderNumber }}</td>
              <td>
                <span class="booking-badge">{{ order.bookingNumber || 'BKG-001' }}</span>
              </td>
              <td>
                <div class="event-cell">
                  <span class="event-title">{{ order.eventName || 'Event Rental' }}</span>
                  <span class="venue-subtitle" *ngIf="order.venueName">📍 {{ order.venueName }}</span>
                </div>
              </td>
              <td>{{ order.customerName || 'Customer' }}</td>
              <td>{{ order.eventDate ? (order.eventDate | date:'mediumDate') : 'Scheduled' }}</td>
              <td>
                <span class="badge" [ngClass]="getPriorityClass(order.priority)">{{ order.priority }}</span>
              </td>
              <td>
                <div class="progress-info">
                  <span>{{ order.totalQuantityPicked }} / {{ order.totalQuantityRequired }} picked</span>
                  <div class="mini-progress">
                    <div class="mini-progress-fill" [style.width.%]="order.pickingProgressPercentage"></div>
                  </div>
                </div>
              </td>
              <td>
                <span class="status-pill" [ngClass]="getStatusClass(order.status)">{{ order.status }}</span>
              </td>
              <td>{{ order.assignedTo || 'Unassigned' }}</td>
              <td>
                <a [routerLink]="['/warehouse/orders', order.id]" class="btn btn-sm btn-primary">Manage &rarr;</a>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #noData>
          <div class="empty-state">
            <p>No warehouse work orders match your selected filters.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .warehouse-orders-container {
      padding: 1.5rem;
      max-width: 1300px;
      margin: 0 auto;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .page-header h2 { margin: 0; font-size: 1.6rem; color: #0f172a; }
    .subtitle { color: #64748b; margin-top: 0.2rem; }

    .toolbar {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
    }
    .search-box { flex: 1; min-width: 250px; }
    .filter-group { display: flex; gap: 0.75rem; }
    .form-control, .form-select {
      width: 100%;
      padding: 0.6rem 0.9rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.9rem;
      background: white;
    }

    .table-card {
      background: white;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      overflow-x: auto;
    }
    .table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.9rem;
    }
    .table th {
      background: #f8fafc;
      padding: 0.9rem 1rem;
      color: #475569;
      font-weight: 600;
      border-bottom: 1px solid #e2e8f0;
    }
    .table td {
      padding: 0.9rem 1rem;
      border-bottom: 1px solid #e2e8f0;
      vertical-align: middle;
    }
    .font-bold { font-weight: 700; color: #0f172a; }
    .booking-badge {
      background: #f1f5f9;
      color: #334155;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-family: monospace;
      font-size: 0.85rem;
    }
    .event-cell { display: flex; flex-direction: column; }
    .event-title { font-weight: 600; color: #1e293b; }
    .venue-subtitle { font-size: 0.78rem; color: #64748b; }

    .badge {
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .badge-urgent { background: #fecaca; color: #991b1b; }
    .badge-high { background: #fed7aa; color: #9a3412; }
    .badge-normal { background: #e2e8f0; color: #334155; }

    .progress-info { font-size: 0.8rem; color: #475569; }
    .mini-progress {
      height: 6px;
      background: #e2e8f0;
      border-radius: 3px;
      margin-top: 0.25rem;
      overflow: hidden;
    }
    .mini-progress-fill { height: 100%; background: #3b82f6; }

    .status-pill {
      font-size: 0.8rem;
      font-weight: 600;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
    }
    .status-ready { background: #dbeafe; color: #1e40af; }
    .status-picking { background: #fef3c7; color: #92400e; }
    .status-packed { background: #dcfce7; color: #166534; }

    .btn {
      padding: 0.5rem 1rem;
      border-radius: 6px;
      text-decoration: none;
      font-weight: 600;
      font-size: 0.85rem;
      display: inline-flex;
      align-items: center;
      cursor: pointer;
    }
    .btn-primary { background: #3b82f6; color: white; border: none; }
    .btn-primary:hover { background: #2563eb; }
    .btn-outline { border: 1px solid #cbd5e1; color: #475569; background: white; }
    .empty-state { padding: 3rem; text-align: center; color: #64748b; }
  `]
})
export class WarehouseOrdersListComponent implements OnInit {
  orders: WarehouseOrder[] = [];
  searchQuery: string = '';
  selectedStatus: string = '';
  selectedPriority: string = '';

  constructor(private warehouseService: WarehouseService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.warehouseService.getOrders(this.selectedStatus, this.selectedPriority, this.searchQuery)
      .subscribe(data => this.orders = data);
  }

  onFilterChange(): void {
    this.loadOrders();
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'URGENT': return 'badge-urgent';
      case 'HIGH': return 'badge-high';
      default: return 'badge-normal';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'READY_TO_PICK': return 'status-ready';
      case 'PICKING': return 'status-picking';
      case 'PICKED': case 'PACKED': case 'READY_FOR_DELIVERY': return 'status-packed';
      default: return 'status-ready';
    }
  }
}
