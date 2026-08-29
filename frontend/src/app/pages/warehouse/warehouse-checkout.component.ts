import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { AssetScanResult } from '../../models/inventory.models';

@Component({
  selector: 'app-warehouse-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="max-w-3xl mx-auto space-y-6 text-slate-100">
      <div>
        <h1 class="text-2xl font-bold text-white">Warehouse Dispatch Checkout Scan</h1>
        <p class="text-xs text-slate-400">Scan barcodes/QR codes of outgoing assets to mark them OUT_ON_RENT for customer bookings</p>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 p-6 space-y-5 shadow-xl">
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Booking Reference / ID *</label>
            <input type="text" [(ngModel)]="bookingId" placeholder="e.g. Booking UUID or BK-2026-1001" class="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Scan Asset Barcode / Code *</label>
            <div class="flex gap-2">
              <input type="text" [(ngModel)]="assetCode" (keyup.enter)="onCheckoutScan()" placeholder="Scan asset barcode..." class="flex-1 px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono text-sm" />
              <button (click)="onCheckoutScan()" [disabled]="loading || !bookingId || !assetCode" class="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-xl shadow-md">
                Confirm Checkout
              </button>
            </div>
          </div>
        </div>

        <div *ngIf="lastScanResult" class="p-4 rounded-xl border bg-slate-950 border-emerald-500/40 space-y-2 text-xs">
          <div class="flex items-center gap-2 font-bold text-emerald-400">
            <span>✅</span> {{ lastScanResult.message }}
          </div>
          <div class="grid grid-cols-2 gap-2 text-slate-300">
            <div>Asset: <strong class="font-mono text-indigo-300">{{ lastScanResult.assetCode }}</strong></div>
            <div>Status: <strong class="text-sky-400">{{ lastScanResult.status }}</strong></div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class WarehouseCheckoutComponent {
  bookingId = '';
  assetCode = '';
  loading = false;
  lastScanResult: AssetScanResult | null = null;

  constructor(private inventoryService: Inventory2Service) {}

  onCheckoutScan(): void {
    if (!this.bookingId || !this.assetCode) return;
    this.loading = true;
    this.inventoryService.checkoutScan(this.bookingId, this.assetCode).subscribe({
      next: (res) => {
        this.loading = false;
        this.lastScanResult = res;
        this.assetCode = '';
      },
      error: (err) => {
        this.loading = false;
        alert('Checkout scan failed: ' + (err.error?.message || err.message));
      }
    });
  }
}
