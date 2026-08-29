import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { Inventory2Service } from '../../services/inventory-2.service';
import { WarehouseService, WarehouseLocation } from '../../services/warehouse.service';
import { Product } from '../../models/catalog.models';

@Component({
  selector: 'app-stock-adjust',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="max-w-3xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Stock Adjustment & Write-offs</h1>
          <p class="text-xs text-slate-400">Record cycle count corrections, damages, losses, or retirement (Reason log mandatory)</p>
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
              <option *ngFor="let p of products" [value]="p.id">{{ p.name }} ({{ p.sku }})</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Warehouse *</label>
            <select [(ngModel)]="selectedWarehouseId" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option value="">-- Choose Warehouse --</option>
              <option *ngFor="let w of warehouses" [value]="w.id">{{ w.name }}</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Adjustment Type *</label>
              <select [(ngModel)]="type" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
                <option value="ADJUSTMENT_IN">ADJUSTMENT_IN (+ Quantity Found)</option>
                <option value="ADJUSTMENT_OUT">ADJUSTMENT_OUT (- Missing/Count error)</option>
                <option value="DAMAGE">DAMAGE (Mark Damaged)</option>
                <option value="LOSS">LOSS (Mark Lost)</option>
                <option value="RETIREMENT">RETIREMENT (End of Life)</option>
              </select>
            </div>

            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Quantity *</label>
              <input type="number" [(ngModel)]="quantity" min="1" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Adjustment Reason (Mandatory) *</label>
            <input type="text" [(ngModel)]="reason" placeholder="e.g. Annual physical count correction, damaged during transport" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Notes / Manager Approval Reference</label>
            <textarea [(ngModel)]="notes" rows="2" placeholder="Approval reference or detailed notes..." class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white"></textarea>
          </div>
        </div>

        <button (click)="onSubmit()" [disabled]="loading || !selectedProductId || !selectedWarehouseId || !reason" class="w-full py-3 bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white font-bold text-xs rounded-xl shadow-lg transition">
          Submit Stock Adjustment & Post Movement
        </button>
      </div>
    </div>
  `
})
export class StockAdjustComponent implements OnInit {
  products: Product[] = [];
  warehouses: WarehouseLocation[] = [];

  selectedProductId = '';
  selectedWarehouseId = '';
  type = 'ADJUSTMENT_IN';
  quantity = 1;
  reason = '';
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
    if (!this.selectedProductId || !this.selectedWarehouseId || !this.reason) return;
    this.loading = true;
    const request = {
      productId: this.selectedProductId,
      warehouseId: this.selectedWarehouseId,
      type: this.type,
      quantity: this.quantity,
      reason: this.reason,
      notes: this.notes
    };

    this.inventoryService.adjustStock(request).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard/inventory']);
      },
      error: (err) => {
        this.loading = false;
        alert('Failed to adjust stock: ' + (err.error?.message || err.message));
      }
    });
  }
}
