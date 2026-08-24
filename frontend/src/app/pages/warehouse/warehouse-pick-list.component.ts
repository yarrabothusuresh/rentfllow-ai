import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseService, WarehouseOrder } from '../../services/warehouse.service';

@Component({
  selector: 'app-warehouse-pick-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="pick-list-container">
      <div class="page-header">
        <div>
          <h2>📋 Warehouse Pick List</h2>
          <p class="subtitle">Work orders awaiting picking or active picking in progress.</p>
        </div>
        <a routerLink="/warehouse/dashboard" class="btn btn-outline">&larr; Back to Dashboard</a>
      </div>

      <div class="table-card">
        <table class="table" *ngIf="pickOrders && pickOrders.length > 0; else noData">
          <thead>
            <tr>
              <th>Warehouse Order</th>
              <th>Booking</th>
              <th>Event</th>
              <th>Customer</th>
              <th>Event Date</th>
              <th>Priority</th>
              <th>Required Items</th>
              <th>Status</th>
              <th>Assigned To</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let order of pickOrders">
              <td class="font-bold">{{ order.orderNumber }}</td>
              <td><span class="booking-badge">{{ order.bookingNumber || 'BKG-001' }}</span></td>
              <td class="font-semibold">{{ order.eventName || 'Wedding Reception' }}</td>
              <td>{{ order.customerName || 'Customer' }}</td>
              <td>{{ order.eventDate ? (order.eventDate | date:'mediumDate') : 'Aug 30, 2026' }}</td>
              <td>
                <span class="badge" [ngClass]="getPriorityClass(order.priority)">{{ order.priority }}</span>
              </td>
              <td>{{ order.items.length }} Products ({{ order.totalQuantityRequired }} Units)</td>
              <td>
                <span class="status-pill" [ngClass]="getStatusClass(order.status)">{{ order.status }}</span>
              </td>
              <td>{{ order.assignedTo || 'Unassigned' }}</td>
              <td>
                <a [routerLink]="['/warehouse/orders', order.id]" class="btn btn-sm btn-primary">
                  {{ order.status === 'READY_TO_PICK' ? 'Start Picking' : 'Continue Pick' }} &rarr;
                </a>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #noData>
          <div class="empty-state">
            <p>🎉 All active picking orders have been processed!</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .pick-list-container { padding: 1.5rem; max-width: 1250px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h2 { margin: 0; font-size: 1.6rem; color: #0f172a; }
    .subtitle { color: #64748b; margin-top: 0.2rem; }

    .table-card {
      background: white;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      overflow-x: auto;
    }
    .table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }
    .table th { background: #f8fafc; padding: 0.9rem 1rem; color: #475569; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
    .table td { padding: 0.9rem 1rem; border-bottom: 1px solid #e2e8f0; vertical-align: middle; }
    .font-bold { font-weight: 700; color: #0f172a; }
    .font-semibold { font-weight: 600; color: #1e293b; }
    .booking-badge { background: #f1f5f9; color: #334155; padding: 0.2rem 0.5rem; border-radius: 4px; font-family: monospace; }

    .badge { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700; }
    .badge-urgent { background: #fecaca; color: #991b1b; }
    .badge-high { background: #fed7aa; color: #9a3412; }
    .badge-normal { background: #e2e8f0; color: #334155; }

    .status-pill { font-size: 0.8rem; font-weight: 600; padding: 0.2rem 0.5rem; border-radius: 4px; }
    .status-ready { background: #dbeafe; color: #1e40af; }
    .status-picking { background: #fef3c7; color: #92400e; }

    .btn { padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 0.85rem; display: inline-flex; align-items: center; cursor: pointer; }
    .btn-primary { background: #3b82f6; color: white; border: none; }
    .btn-primary:hover { background: #2563eb; }
    .btn-outline { border: 1px solid #cbd5e1; color: #475569; background: white; }
    .empty-state { padding: 3rem; text-align: center; color: #64748b; }
  `]
})
export class WarehousePickListComponent implements OnInit {
  pickOrders: WarehouseOrder[] = [];

  constructor(private warehouseService: WarehouseService) {}

  ngOnInit(): void {
    this.warehouseService.getOrders().subscribe(data => {
      this.pickOrders = data.filter(o => o.status === 'READY_TO_PICK' || o.status === 'PICKING');
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
      default: return 'status-ready';
    }
  }
}
