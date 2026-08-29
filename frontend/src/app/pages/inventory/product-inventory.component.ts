import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { Product } from '../../models/catalog.models';

@Component({
  selector: 'app-product-inventory',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Product Inventory & Tracking</h1>
          <p class="text-xs text-slate-400">View tracking type, quantities owned, available, damaged, and physical asset details</p>
        </div>
        <div class="flex gap-2">
          <a routerLink="/dashboard/inventory/receive" class="px-3 py-2 bg-emerald-600/20 text-emerald-300 border border-emerald-500/30 rounded-xl text-xs font-bold hover:bg-emerald-600/30">
            + Receive Stock
          </a>
          <a routerLink="/dashboard/inventory/adjust" class="px-3 py-2 bg-slate-800 text-slate-300 border border-slate-700 rounded-xl text-xs font-bold hover:bg-slate-700">
            Adjust Stock
          </a>
        </div>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <table class="w-full text-left text-xs">
          <thead>
            <tr class="bg-slate-950 border-b border-slate-800 text-slate-400 font-bold uppercase">
              <th class="py-3.5 px-4">Product / SKU</th>
              <th class="py-3.5 px-3">Tracking Type</th>
              <th class="py-3.5 px-3 text-center">Total Owned</th>
              <th class="py-3.5 px-3 text-center">Available</th>
              <th class="py-3.5 px-3 text-center">Maint / Damaged</th>
              <th class="py-3.5 px-3 text-right">Daily Rate</th>
              <th class="py-3.5 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let p of products" class="hover:bg-slate-900/50">
              <td class="py-3 px-4 font-bold text-white">
                <div>{{ p.name }}</div>
                <div class="text-[11px] font-mono text-slate-400">{{ p.sku }}</div>
              </td>
              <td class="py-3 px-3">
                <span [ngClass]="p.trackingType === 'SERIALIZED' ? 'bg-purple-500/15 text-purple-300 border-purple-500/30' : 'bg-sky-500/15 text-sky-300 border-sky-500/30'" class="px-2 py-0.5 rounded-md text-[10px] font-bold border">
                  {{ p.trackingType || 'QUANTITY' }}
                </span>
              </td>
              <td class="py-3 px-3 text-center font-bold text-slate-200">{{ p.quantityOwned }}</td>
              <td class="py-3 px-3 text-center font-bold text-emerald-400">{{ p.availableQuantity }}</td>
              <td class="py-3 px-3 text-center text-slate-400">
                <span [class.text-amber-400]="p.quantityInMaintenance > 0">{{ p.quantityInMaintenance }}</span> /
                <span [class.text-rose-400]="p.quantityDamaged > 0">{{ p.quantityDamaged }}</span>
              </td>
              <td class="py-3 px-3 text-right font-bold text-emerald-400">\${{ p.rentalPrice | number:'1.2-2' }}</td>
              <td class="py-3 px-4 text-center">
                <div class="flex items-center justify-center gap-2">
                  <a *ngIf="p.trackingType === 'SERIALIZED'" [routerLink]="['/dashboard/inventory/products', p.id, 'assets']" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-indigo-600/20 text-indigo-300 border border-indigo-500/30 hover:bg-indigo-600/30">
                    Serialized Assets ({{ p.quantityOwned }})
                  </a>
                  <a [routerLink]="['/dashboard/products', p.id]" class="px-2 py-1 rounded-lg text-xs font-medium bg-slate-800 text-slate-300 border border-slate-700">
                    Product Detail
                  </a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class ProductInventoryComponent implements OnInit {
  products: Product[] = [];

  constructor(private catalogService: CatalogService) {}

  ngOnInit(): void {
    this.catalogService.getProducts().subscribe({
      next: (prods) => (this.products = prods),
      error: (err) => console.error('Failed to load products', err)
    });
  }
}
