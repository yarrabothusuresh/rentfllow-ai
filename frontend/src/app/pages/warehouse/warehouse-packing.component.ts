import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseService, WarehouseOrder } from '../../services/warehouse.service';

@Component({
  selector: 'app-warehouse-packing',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="packing-container">
      <div class="page-header">
        <div>
          <h2>📦 Warehouse Packing & Staging Checklist</h2>
          <p class="subtitle">Pack picked rental items into protective crates/bins and stage for truck load.</p>
        </div>
        <a routerLink="/warehouse/dashboard" class="btn btn-outline">&larr; Back to Dashboard</a>
      </div>

      <div class="orders-packing-list" *ngIf="packingOrders && packingOrders.length > 0; else noData">
        <div class="packing-card" *ngFor="let order of packingOrders">
          <div class="card-header">
            <div>
              <h3>PACKING — {{ order.orderNumber }}</h3>
              <p class="meta-sub">Event: {{ order.eventName }} ({{ order.eventDate | date:'mediumDate' }}) | Customer: {{ order.customerName }}</p>
            </div>
            <a [routerLink]="['/warehouse/orders', order.id]" class="btn btn-sm btn-primary">Open Packing Checklist &rarr;</a>
          </div>

          <div class="packing-items">
            <div class="item-check" *ngFor="let item of order.items">
              <span class="check-icon" [class.checked]="item.quantityPacked === item.quantityRequired">
                {{ item.quantityPacked === item.quantityRequired ? '✓' : '◯' }}
              </span>
              <span class="item-text">{{ item.quantityPacked }} / {{ item.quantityRequired }} packed — {{ item.productNameSnapshot }}</span>
              <span class="loc-code">📍 {{ item.locationSnapshot }}</span>
            </div>
          </div>
        </div>
      </div>

      <ng-template #noData>
        <div class="empty-card">
          <p>🎉 All picked work orders have been packed and staged for delivery!</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .packing-container { padding: 1.5rem; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h2 { margin: 0; font-size: 1.6rem; color: #0f172a; }
    .subtitle { color: #64748b; margin-top: 0.2rem; }

    .orders-packing-list { display: flex; flex-direction: column; gap: 1.25rem; }
    .packing-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 0.75rem; }
    .card-header h3 { margin: 0; font-size: 1.2rem; color: #0f172a; }
    .meta-sub { margin: 0.2rem 0 0 0; font-size: 0.85rem; color: #64748b; }

    .packing-items { display: flex; flex-direction: column; gap: 0.6rem; }
    .item-check { display: flex; align-items: center; gap: 0.75rem; font-size: 0.95rem; }
    .check-icon { font-weight: 700; color: #94a3b8; font-size: 1.1rem; }
    .check-icon.checked { color: #10b981; }
    .item-text { font-weight: 600; color: #1e293b; }
    .loc-code { font-size: 0.8rem; color: #64748b; background: #f1f5f9; padding: 0.1rem 0.4rem; border-radius: 4px; }

    .btn { padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 0.85rem; display: inline-flex; align-items: center; }
    .btn-primary { background: #3b82f6; color: white; border: none; }
    .btn-outline { border: 1px solid #cbd5e1; color: #475569; background: white; }
    .empty-card { background: white; padding: 3rem; text-align: center; border-radius: 12px; border: 1px solid #e2e8f0; color: #64748b; }
  `]
})
export class WarehousePackingComponent implements OnInit {
  packingOrders: WarehouseOrder[] = [];

  constructor(private warehouseService: WarehouseService) {}

  ngOnInit(): void {
    this.warehouseService.getOrders().subscribe(data => {
      this.packingOrders = data.filter(o => o.status === 'PICKED' || o.status === 'PACKING');
    });
  }
}
