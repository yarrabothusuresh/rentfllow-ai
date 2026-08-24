import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseService, WarehouseOrder, WarehouseOrderItem } from '../../services/warehouse.service';

export interface ShortageRow {
  orderId: string;
  orderNumber: string;
  eventName: string;
  eventDate?: string;
  priority: string;
  productName: string;
  location: string;
  quantityRequired: number;
  quantityPicked: number;
  quantityShort: number;
  notes?: string;
}

@Component({
  selector: 'app-warehouse-shortages',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="shortages-container">
      <div class="page-header">
        <div>
          <h2>⚠️ Warehouse Shortages & Exception Screen</h2>
          <p class="subtitle">Operational exception list of missing, damaged, or unfulfilled rental inventory.</p>
        </div>
        <a routerLink="/warehouse/dashboard" class="btn btn-outline">&larr; Back to Dashboard</a>
      </div>

      <div class="table-card">
        <table class="table" *ngIf="shortages && shortages.length > 0; else noShortages">
          <thead>
            <tr>
              <th>Warehouse Order</th>
              <th>Product</th>
              <th>Location</th>
              <th>Required</th>
              <th>Available / Picked</th>
              <th>Short Quantity</th>
              <th>Event Date</th>
              <th>Priority</th>
              <th>Notes</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of shortages">
              <td class="font-bold">{{ row.orderNumber }}</td>
              <td class="font-semibold">{{ row.productName }}</td>
              <td><span class="loc-tag">📍 {{ row.location }}</span></td>
              <td>{{ row.quantityRequired }}</td>
              <td>{{ row.quantityPicked }}</td>
              <td>
                <span class="badge-short">SHORT BY {{ row.quantityShort }}</span>
              </td>
              <td>{{ row.eventDate ? (row.eventDate | date:'mediumDate') : 'Aug 30, 2026' }}</td>
              <td>
                <span class="badge" [ngClass]="getPriorityClass(row.priority)">{{ row.priority }}</span>
              </td>
              <td class="text-danger font-semibold">{{ row.notes || 'Inventory unavailable in rack' }}</td>
              <td>
                <a [routerLink]="['/warehouse/orders', row.orderId]" class="btn btn-sm btn-primary">View Order &rarr;</a>
              </td>
            </tr>
          </tbody>
        </table>

        <ng-template #noShortages>
          <div class="empty-state">
            <p>🎉 No inventory shortages reported across active warehouse orders!</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .shortages-container { padding: 1.5rem; max-width: 1300px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h2 { margin: 0; font-size: 1.6rem; color: #991b1b; }
    .subtitle { color: #64748b; margin-top: 0.2rem; }

    .table-card {
      background: white;
      border-radius: 12px;
      border: 1px solid #fee2e2;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      overflow-x: auto;
    }
    .table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }
    .table th { background: #fef2f2; padding: 0.9rem 1rem; color: #991b1b; font-weight: 600; border-bottom: 1px solid #fee2e2; }
    .table td { padding: 0.9rem 1rem; border-bottom: 1px solid #fecaca; vertical-align: middle; }
    .font-bold { font-weight: 700; color: #0f172a; }
    .font-semibold { font-weight: 600; color: #1e293b; }
    .text-danger { color: #dc2626; }
    .loc-tag { font-size: 0.8rem; background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; }

    .badge-short { background: #fecaca; color: #991b1b; padding: 0.3rem 0.6rem; border-radius: 6px; font-weight: 700; font-size: 0.8rem; display: inline-block; }
    .badge { padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700; }
    .badge-urgent { background: #fecaca; color: #991b1b; }
    .badge-high { background: #fed7aa; color: #9a3412; }
    .badge-normal { background: #e2e8f0; color: #334155; }

    .btn { padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 0.85rem; display: inline-flex; align-items: center; }
    .btn-primary { background: #3b82f6; color: white; border: none; }
    .btn-outline { border: 1px solid #cbd5e1; color: #475569; background: white; }
    .empty-state { padding: 3rem; text-align: center; color: #64748b; }
  `]
})
export class WarehouseShortagesComponent implements OnInit {
  shortages: ShortageRow[] = [];

  constructor(private warehouseService: WarehouseService) {}

  ngOnInit(): void {
    this.warehouseService.getOrders().subscribe(orders => {
      const rows: ShortageRow[] = [];
      for (const order of orders) {
        for (const item of order.items) {
          if (item.status === 'SHORT' || item.quantityPicked < item.quantityRequired) {
            rows.push({
              orderId: order.id,
              orderNumber: order.orderNumber,
              eventName: order.eventName || 'Event',
              eventDate: order.eventDate,
              priority: order.priority,
              productName: item.productNameSnapshot,
              location: item.locationSnapshot,
              quantityRequired: item.quantityRequired,
              quantityPicked: item.quantityPicked,
              quantityShort: item.quantityRequired - item.quantityPicked,
              notes: item.notes
            });
          }
        }
      }
      this.shortages = rows;
    });
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'URGENT': return 'badge-urgent';
      case 'HIGH': return 'badge-high';
      default: return 'badge-normal';
    }
  }
}
