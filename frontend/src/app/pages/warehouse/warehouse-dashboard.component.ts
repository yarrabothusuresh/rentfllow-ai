import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseService, WarehouseDashboard, WarehouseOrder } from '../../services/warehouse.service';

@Component({
  selector: 'app-warehouse-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="warehouse-dashboard-container" *ngIf="dashboard">
      <!-- Header -->
      <div class="page-header">
        <div>
          <h2>🏬 Warehouse Operations Dashboard</h2>
          <p class="subtitle">Real-time fulfillment metrics, picking queues, and staging status.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/pick" class="btn btn-primary">🚜 Pick List</a>
          <a routerLink="/warehouse/packing" class="btn btn-warning">📦 Packing Checklist</a>
          <a routerLink="/warehouse/shortages" class="btn btn-danger" *ngIf="dashboard.shortageOrdersCount > 0">
            ⚠️ Shortages ({{ dashboard.shortageOrdersCount }})
          </a>
        </div>
      </div>

      <!-- Metrics Row -->
      <div class="metrics-grid">
        <div class="metric-card">
          <span class="metric-icon">📋</span>
          <div class="metric-info">
            <span class="metric-value">{{ dashboard.totalActiveWorkOrders }}</span>
            <span class="metric-label">Active Work Orders</span>
          </div>
        </div>

        <div class="metric-card card-picking">
          <span class="metric-icon">🚜</span>
          <div class="metric-info">
            <span class="metric-value">{{ dashboard.ordersInPicking }}</span>
            <span class="metric-label">In Picking</span>
          </div>
        </div>

        <div class="metric-card card-packing">
          <span class="metric-icon">📦</span>
          <div class="metric-info">
            <span class="metric-value">{{ dashboard.ordersInPacking }}</span>
            <span class="metric-label">In Packing</span>
          </div>
        </div>

        <div class="metric-card card-ready">
          <span class="metric-icon">🚚</span>
          <div class="metric-info">
            <span class="metric-value">{{ dashboard.readyForDeliveryOrders }}</span>
            <span class="metric-label">Ready for Delivery</span>
          </div>
        </div>

        <div class="metric-card card-shortage" [class.alert]="dashboard.shortageOrdersCount > 0">
          <span class="metric-icon">⚠️</span>
          <div class="metric-info">
            <span class="metric-value">{{ dashboard.shortageOrdersCount }}</span>
            <span class="metric-label">Shortages Reported</span>
          </div>
        </div>
      </div>

      <!-- Main Section: Priority Work Orders & Audit Feed -->
      <div class="dashboard-main-grid">
        <!-- Urgent Work Orders Table -->
        <div class="card-section flex-2">
          <div class="section-header">
            <h3>Urgent & Upcoming Work Orders</h3>
            <a routerLink="/warehouse/orders" class="link-view-all">View All Work Orders &rarr;</a>
          </div>

          <div class="table-responsive">
            <table class="table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Event & Customer</th>
                  <th>Event Date</th>
                  <th>Priority</th>
                  <th>Progress</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let order of dashboard.urgentOrders">
                  <td class="font-bold">{{ order.orderNumber }}</td>
                  <td>
                    <div class="cell-stacked">
                      <span class="font-semibold">{{ order.eventName || 'Event Rental' }}</span>
                      <span class="sub-text">👤 {{ order.customerName }}</span>
                    </div>
                  </td>
                  <td>{{ order.eventDate ? (order.eventDate | date:'mediumDate') : 'Aug 30, 2026' }}</td>
                  <td>
                    <span class="badge" [ngClass]="getPriorityClass(order.priority)">{{ order.priority }}</span>
                  </td>
                  <td>
                    <div class="progress-container">
                      <span class="progress-text">{{ order.totalQuantityPicked }} / {{ order.totalQuantityRequired }}</span>
                      <div class="progress-bar">
                        <div class="progress-fill" [style.width.%]="order.pickingProgressPercentage"></div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span class="status-pill" [ngClass]="getStatusClass(order.status)">{{ order.status }}</span>
                  </td>
                  <td>
                    <a [routerLink]="['/warehouse/orders', order.id]" class="btn btn-sm btn-outline">Manage</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Recent Audit Log -->
        <div class="card-section flex-1">
          <div class="section-header">
            <h3>Recent Warehouse Activity</h3>
          </div>

          <div class="activity-feed">
            <div class="activity-item" *ngFor="let audit of dashboard.recentActivity">
              <div class="activity-bullet"></div>
              <div class="activity-content">
                <span class="activity-action">{{ audit.action }}</span>
                <p class="activity-details">{{ audit.details }}</p>
                <span class="activity-time">By {{ audit.performedBy }} • {{ audit.timestamp | date:'shortTime' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .warehouse-dashboard-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0; font-size: 1.6rem; color: #0f172a; }
    .subtitle { color: #64748b; margin-top: 0.2rem; }
    .header-actions { display: flex; gap: 0.75rem; }
    .btn {
      padding: 0.6rem 1.2rem;
      border-radius: 8px;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
      border: none;
      transition: all 0.2s ease;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-primary:hover { background: #2563eb; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-secondary:hover { background: #475569; }
    .btn-outline-danger { border: 1px solid #ef4444; color: #ef4444; background: transparent; }
    .btn-outline-danger:hover { background: #fee2e2; }
    .btn-outline { border: 1px solid #cbd5e1; color: #475569; background: white; }
    .btn-outline:hover { background: #f8fafc; }
    .btn-sm { padding: 0.4rem 0.8rem; font-size: 0.85rem; }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
      margin-bottom: 2.5rem;
    }
    .metric-card {
      background: white;
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      border: 1px solid #e2e8f0;
    }
    .metric-icon {
      font-size: 2rem;
    }
    .metric-content {
      display: flex;
      flex-direction: column;
    }
    .metric-value {
      font-size: 1.75rem;
      font-weight: 700;
      line-height: 1.2;
      color: #0f172a;
    }
    .metric-label {
      font-size: 0.85rem;
      color: #64748b;
      font-weight: 500;
    }

    .priorities-section {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      border: 1px solid #e2e8f0;
    }
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .section-header h3 {
      margin: 0;
      font-size: 1.2rem;
      color: #0f172a;
    }
    .view-all-link {
      color: #3b82f6;
      text-decoration: none;
      font-weight: 600;
    }
    .priorities-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.25rem;
    }
    .priority-card {
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      background: #f8fafc;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }
    .order-number {
      font-weight: 700;
      color: #1e293b;
    }
    .badge {
      padding: 0.25rem 0.6rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .badge-urgent { background: #fecaca; color: #991b1b; }
    .badge-high { background: #fed7aa; color: #9a3412; }
    .badge-normal { background: #e2e8f0; color: #334155; }
    .event-name {
      margin: 0 0 0.5rem 0;
      font-size: 1.1rem;
      color: #0f172a;
    }
    .customer-name, .event-date {
      margin: 0 0 0.4rem 0;
      font-size: 0.85rem;
      color: #475569;
    }
    .progress-bar-container {
      margin-top: 1rem;
    }
    .progress-label {
      display: flex;
      justify-content: space-between;
      font-size: 0.8rem;
      color: #64748b;
      margin-bottom: 0.3rem;
    }
    .progress-track {
      height: 8px;
      background: #e2e8f0;
      border-radius: 4px;
      overflow: hidden;
    }
    .progress-fill {
      height: 100%;
      background: #3b82f6;
      border-radius: 4px;
    }
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 1.25rem;
      padding-top: 0.75rem;
      border-top: 1px solid #e2e8f0;
    }
    .status-pill {
      font-size: 0.8rem;
      font-weight: 600;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
    }
    .status-ready { background: #dbeafe; color: #1e40af; }
    .status-picking { background: #fef3c7; color: #92400e; }
    .status-packed { background: #dcfce7; color: #166534; }
    .empty-state {
      text-align: center;
      padding: 2rem;
      color: #64748b;
    }
  `]
})
export class WarehouseDashboardComponent implements OnInit {
  dashboard: WarehouseDashboard | null = null;

  constructor(private warehouseService: WarehouseService) {}

  ngOnInit(): void {
    this.warehouseService.getDashboard().subscribe(data => {
      this.dashboard = data;
    });
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
