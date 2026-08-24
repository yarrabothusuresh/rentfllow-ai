import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InventoryAvailabilityService, AvailabilityResult } from '../../services/inventory-availability.service';
import { CatalogService } from '../../services/catalog.service';

@Component({
  selector: 'app-inventory-availability-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="availability-calendar-container">
      <header class="page-header">
        <div>
          <h1>📅 Real-Time Availability Calendar</h1>
          <p class="subtitle">Check product availability balances across any rental window</p>
        </div>
        <a routerLink="/inventory/dashboard" class="btn btn-secondary">&larr; Back to Dashboard</a>
      </header>

      <!-- Date & Search Filter Toolbar -->
      <div class="filter-card">
        <div class="filter-row">
          <div class="field-group">
            <label>Rental Start Date/Time</label>
            <input type="datetime-local" [(ngModel)]="startDateTime" (change)="checkAll()" class="form-control" />
          </div>

          <div class="field-group">
            <label>Rental End Date/Time</label>
            <input type="datetime-local" [(ngModel)]="endDateTime" (change)="checkAll()" class="form-control" />
          </div>

          <div class="field-group">
            <label>Requested Quantity</label>
            <input type="number" min="1" [(ngModel)]="requestedQty" (change)="checkAll()" class="form-control" />
          </div>

          <div class="field-group">
            <label>&nbsp;</label>
            <button (click)="checkAll()" class="btn btn-primary">🔍 Re-calculate</button>
          </div>
        </div>
      </div>

      <!-- Results Table -->
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>Product SKU</th>
              <th>Product Name</th>
              <th>Total Owned</th>
              <th>Reserved</th>
              <th>Available</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let res of availabilityList">
              <td><code>{{ res.sku }}</code></td>
              <td class="font-semibold">{{ res.productName }}</td>
              <td>{{ res.quantityOwned }}</td>
              <td class="text-amber">{{ res.quantityReserved }}</td>
              <td class="text-emerald font-bold">{{ res.availableQuantity }}</td>
              <td>
                <span class="status-badge" [class.badge-avail]="res.available" [class.badge-unavail]="!res.available">
                  {{ res.available ? 'AVAILABLE (' + res.availableQuantity + ')' : 'SHORTAGE (' + res.shortage + ' SHORT)' }}
                </span>
              </td>
              <td>
                <a [routerLink]="['/inventory/products', res.productId, 'availability']" class="btn btn-xs btn-outline">
                  Timeline Grid &rarr;
                </a>
              </td>
            </tr>
            <tr *ngIf="availabilityList.length === 0">
              <td colspan="7" class="text-center py-4">No inventory products found for selected filters.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .availability-calendar-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h1 { font-size: 1.8rem; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; }
    .btn { padding: 0.6rem 1.2rem; border-radius: 6px; font-weight: 600; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-outline { border-color: #cbd5e1; color: #3b82f6; background: white; }
    .btn-xs { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
    
    .filter-card { background: white; padding: 1.25rem; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 1.5rem; }
    .filter-row { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
    .field-group { display: flex; flex-direction: column; gap: 0.4rem; }
    .field-group label { font-size: 0.85rem; font-weight: 600; color: #475569; }
    .form-control { padding: 0.55rem 0.8rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.9rem; }
    
    .table-card { background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th, .data-table td { padding: 1rem; border-bottom: 1px solid #f1f5f9; }
    .data-table th { background: #f8fafc; font-size: 0.85rem; color: #475569; text-transform: uppercase; }
    .text-amber { color: #d97706; font-weight: 600; }
    .text-emerald { color: #16a34a; }
    .font-semibold { font-weight: 600; }
    .font-bold { font-weight: 700; }
    
    .status-badge { padding: 0.3rem 0.75rem; border-radius: 20px; font-size: 0.8rem; font-weight: 700; }
    .badge-avail { background: #dcfce7; color: #15803d; }
    .badge-unavail { background: #fee2e2; color: #b91c1c; }
  `]
})
export class InventoryAvailabilityCalendarComponent implements OnInit {
  startDateTime: string = '2026-08-30T08:00';
  endDateTime: string = '2026-08-30T22:00';
  requestedQty: number = 100;
  availabilityList: AvailabilityResult[] = [];

  constructor(
    private inventoryService: InventoryAvailabilityService,
    private catalogService: CatalogService
  ) {}

  ngOnInit(): void {
    this.checkAll();
  }

  checkAll(): void {
    this.catalogService.getProducts().subscribe((products: any[]) => {
      this.availabilityList = [];
      products.forEach((p: any) => {
        this.inventoryService.getAvailability(p.id, this.requestedQty, this.startDateTime, this.endDateTime)
          .subscribe(res => this.availabilityList.push(res));
      });
    });
  }
}
