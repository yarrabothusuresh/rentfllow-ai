import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { Inventory2Service } from '../../services/inventory-2.service';
import { WarehouseService, WarehouseLocation } from '../../services/warehouse.service';
import { Product } from '../../models/catalog.models';

@Component({
  selector: 'app-stock-receive',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="max-w-3xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Stock Receiving (PO Intake)</h1>
          <p class="text-xs text-slate-400">Receive new product inventory, generate barcodes & assign serial numbers</p>
        </div>
        <a routerLink="/dashboard/inventory" class="px-3 py-1.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700 border border-slate-700">
          Cancel
        </a>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 p-6 space-y-5 shadow-xl">
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Select Product *</label>
            <select [(ngModel)]="selectedProductId" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option value="">-- Choose Product --</option>
              <option *ngFor="let p of products" [value]="p.id">{{ p.name }} ({{ p.sku }}) - {{ p.trackingType || 'QUANTITY' }}</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Destination Warehouse *</label>
            <select [(ngModel)]="selectedWarehouseId" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option value="">-- Choose Warehouse --</option>
              <option *ngFor="let w of warehouses" [value]="w.id">{{ w.name }} ({{ w.code }})</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Quantity Received *</label>
              <input type="number" [(ngModel)]="quantity" min="1" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
            </div>

            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Stock Condition</label>
              <select [(ngModel)]="condition" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
                <option value="NEW">NEW</option>
                <option value="EXCELLENT">EXCELLENT</option>
                <option value="GOOD">GOOD</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">PO / Supplier Reference</label>
            <input type="text" [(ngModel)]="reference" placeholder="e.g. PO-2026-9901" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Notes / Inspection Summary</label>
            <textarea [(ngModel)]="notes" rows="3" placeholder="Additional notes or supplier batch info..." class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"></textarea>
          </div>
        </div>

        <button (click)="onSubmit()" [disabled]="loading || !selectedProductId || !selectedWarehouseId" class="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-xs rounded-xl shadow-lg transition">
          Confirm Stock Receipt & Save Ledger
        </button>
      </div>
    </div>
  `
})
export class StockReceiveComponent implements OnInit {
  products: Product[] = [];
  warehouses: WarehouseLocation[] = [];

  selectedProductId = '';
  selectedWarehouseId = '';
  quantity = 1;
  condition = 'NEW';
  reference = '';
  notes = '';
  loading = false;

  constructor(
    private catalogService: CatalogService,
    private warehouseService: WarehouseService,
    private inventoryService: Inventory2Service,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.catalogService.getProducts().subscribe({ next: (p) => (this.products = p) });
    this.warehouseService.getWarehouses().subscribe({ next: (w: WarehouseLocation[]) => (this.warehouses = w) });
  }

  onSubmit(): void {
    if (!this.selectedProductId || !this.selectedWarehouseId) return;
    this.loading = true;
    const request = {
      productId: this.selectedProductId,
      warehouseId: this.selectedWarehouseId,
      quantity: this.quantity,
      condition: this.condition,
      reference: this.reference,
      notes: this.notes
    };

    this.inventoryService.receiveStock(request).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard/inventory']);
      },
      error: (err) => {
        this.loading = false;
        alert('Failed to receive stock: ' + (err.error?.message || err.message));
      }
    });
  }
}
