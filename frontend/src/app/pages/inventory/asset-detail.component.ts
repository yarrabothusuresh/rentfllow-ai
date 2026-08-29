import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { AssetDetail } from '../../models/inventory.models';

@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="max-w-5xl mx-auto space-y-6 text-slate-100" *ngIf="asset">
      <div class="flex items-center justify-between">
        <div>
          <div class="text-xs font-semibold text-indigo-400 uppercase tracking-wider">{{ asset.productName }} ({{ asset.productSku }})</div>
          <h1 class="text-3xl font-extrabold text-white font-mono mt-0.5">Asset {{ asset.assetCode }}</h1>
        </div>
        <a [routerLink]="['/dashboard/inventory/products', asset.productId, 'assets']" class="px-3 py-1.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700 border border-slate-700">
          ← Back to Product Assets
        </a>
      </div>

      <!-- CARD DETAILS & QR / BARCODE METRICS -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Asset Card Info -->
        <div class="md:col-span-2 bg-slate-900/90 rounded-2xl border border-slate-800 p-6 space-y-4 shadow-xl">
          <h2 class="text-base font-bold text-white border-b border-slate-800 pb-3">Physical Asset Information</h2>
          <div class="grid grid-cols-2 gap-4 text-xs">
            <div>
              <span class="text-slate-400">Serial Number:</span>
              <div class="font-mono font-bold text-slate-200 text-sm mt-0.5">{{ asset.serialNumber || 'N/A' }}</div>
            </div>
            <div>
              <span class="text-slate-400">Barcode Identifier:</span>
              <div class="font-mono font-bold text-slate-200 text-sm mt-0.5">{{ asset.barcode }}</div>
            </div>
            <div>
              <span class="text-slate-400">Current Warehouse:</span>
              <div class="font-bold text-white text-sm mt-0.5">{{ asset.warehouseName || 'Main Warehouse' }}</div>
            </div>
            <div>
              <span class="text-slate-400">Status & Condition:</span>
              <div class="flex items-center gap-2 mt-0.5">
                <span class="px-2 py-0.5 rounded-md text-xs font-bold border bg-indigo-500/15 text-indigo-300 border-indigo-500/30">
                  {{ asset.status }}
                </span>
                <span class="px-2 py-0.5 rounded-md text-xs font-bold border bg-slate-800 text-slate-300 border-slate-700">
                  {{ asset.condition }}
                </span>
              </div>
            </div>
            <div *ngIf="asset.currentBookingNumber">
              <span class="text-slate-400">Current Rental Booking:</span>
              <div class="font-bold text-sky-400 mt-0.5">{{ asset.currentBookingNumber }} ({{ asset.customerName }})</div>
            </div>
            <div>
              <span class="text-slate-400">Acquisition Cost:</span>
              <div class="font-bold text-emerald-400 mt-0.5">\${{ asset.purchaseCost | number:'1.2-2' }}</div>
            </div>
          </div>
        </div>

        <!-- QR & Barcode Card -->
        <div class="bg-slate-900/90 rounded-2xl border border-slate-800 p-6 flex flex-col items-center justify-center text-center shadow-xl">
          <div class="w-32 h-32 bg-white rounded-xl p-2 flex items-center justify-center border border-slate-300 shadow-inner">
            <!-- Simulated QR Render -->
            <div class="w-full h-full border-4 border-slate-900 p-1.5 flex flex-col justify-between items-center bg-white">
              <div class="text-[10px] font-mono font-bold text-slate-900 tracking-tighter">RF-QR-CODE</div>
              <div class="text-2xl font-bold text-slate-900">📱</div>
              <div class="text-[9px] font-mono text-slate-700 truncate w-full">{{ asset.qrCode }}</div>
            </div>
          </div>
          <div class="mt-3 text-xs font-mono font-bold text-slate-300">{{ asset.barcode }}</div>
          <p class="text-[11px] text-slate-400 mt-1">Scan QR or Barcode for instant warehouse checkin/checkout</p>
        </div>
      </div>

      <!-- ASSET MOVEMENT HISTORY TIMELINE -->
      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 p-6 space-y-4 shadow-xl">
        <h2 class="text-base font-bold text-white border-b border-slate-800 pb-3">Complete Asset Audit History</h2>
        
        <div *ngIf="!asset.history || asset.history.length === 0" class="text-xs text-slate-500 py-4 text-center">
          No previous movements recorded for this asset.
        </div>

        <div class="space-y-3">
          <div *ngFor="let h of asset.history" class="p-3.5 rounded-xl bg-slate-950 border border-slate-800/80 flex items-center justify-between gap-4 text-xs">
            <div>
              <div class="flex items-center gap-2">
                <span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  {{ h.movementType }}
                </span>
                <span class="font-semibold text-white">{{ h.reason || 'Standard operation' }}</span>
              </div>
              <div class="text-slate-400 text-[11px] mt-1">
                Performed by: <strong class="text-slate-300">{{ h.performedBy || 'System' }}</strong>
                <span *ngIf="h.fromWarehouseName && h.toWarehouseName"> | Transfer {{ h.fromWarehouseName }} → {{ h.toWarehouseName }}</span>
              </div>
            </div>
            <div class="text-right text-[11px] text-slate-400 shrink-0 font-mono">
              {{ h.timestamp | date:'short' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AssetDetailComponent implements OnInit {
  asset: AssetDetail | null = null;

  constructor(
    private route: ActivatedRoute,
    private inventoryService: Inventory2Service
  ) {}

  ngOnInit(): void {
    const assetId = this.route.snapshot.paramMap.get('assetId') || '';
    if (assetId) {
      this.inventoryService.getAssetDetail(assetId).subscribe({
        next: (d) => (this.asset = d),
        error: (err) => console.error('Failed to load asset detail', err)
      });
    }
  }
}
