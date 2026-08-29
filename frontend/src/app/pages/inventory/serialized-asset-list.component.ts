import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { InventoryItem } from '../../models/inventory.models';

@Component({
  selector: 'app-serialized-asset-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Serialized Asset Registry</h1>
          <p class="text-xs text-slate-400">Individual physical items, serial numbers, barcodes & real-time asset statuses</p>
        </div>
        <a routerLink="/dashboard/inventory" class="px-3 py-1.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700 border border-slate-700">
          ← Back to Inventory
        </a>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <table class="w-full text-left text-xs">
          <thead>
            <tr class="bg-slate-950 border-b border-slate-800 text-slate-400 font-bold uppercase">
              <th class="py-3.5 px-4">Asset Code</th>
              <th class="py-3.5 px-3">Serial Number</th>
              <th class="py-3.5 px-3">Barcode</th>
              <th class="py-3.5 px-3 text-center">Status</th>
              <th class="py-3.5 px-3 text-center">Condition</th>
              <th class="py-3.5 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let asset of assets" class="hover:bg-slate-900/50">
              <td class="py-3 px-4 font-mono font-bold text-indigo-300">{{ asset.assetCode }}</td>
              <td class="py-3 px-3 font-mono text-slate-300">{{ asset.serialNumber || 'N/A' }}</td>
              <td class="py-3 px-3 font-mono text-slate-400">{{ asset.barcode || 'N/A' }}</td>
              <td class="py-3 px-3 text-center">
                <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold border"
                      [ngClass]="{
                        'bg-emerald-500/15 text-emerald-300 border-emerald-500/30': asset.status === 'AVAILABLE',
                        'bg-sky-500/15 text-sky-300 border-sky-500/30': asset.status === 'OUT_ON_RENT',
                        'bg-amber-500/15 text-amber-300 border-amber-500/30': asset.status === 'MAINTENANCE',
                        'bg-rose-500/15 text-rose-300 border-rose-500/30': asset.status === 'DAMAGED' || asset.status === 'LOST'
                      }">
                  {{ asset.status }}
                </span>
              </td>
              <td class="py-3 px-3 text-center font-semibold text-slate-300">{{ asset.condition }}</td>
              <td class="py-3 px-4 text-center">
                <a [routerLink]="['/dashboard/inventory/assets', asset.id]" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700">
                  View Detail & Timeline
                </a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class SerializedAssetListComponent implements OnInit {
  productId = '';
  assets: InventoryItem[] = [];

  constructor(
    private route: ActivatedRoute,
    private inventoryService: Inventory2Service
  ) {}

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('productId') || '';
    if (this.productId) {
      this.inventoryService.getProductAssets(this.productId).subscribe({
        next: (page) => (this.assets = page.content || []),
        error: (err) => console.error('Failed to load assets', err)
      });
    }
  }
}
