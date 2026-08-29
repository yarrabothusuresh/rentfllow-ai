import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { AssetScanResult } from '../../models/inventory.models';

@Component({
  selector: 'app-scan-screen',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="max-w-3xl mx-auto space-y-6 text-slate-100">
      <div>
        <h1 class="text-2xl font-bold text-white">Barcode & QR Code Scanner</h1>
        <p class="text-xs text-slate-400">Scan physical barcode tags, QR tokens, or enter asset codes for instant verification</p>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 p-6 space-y-5 shadow-xl">
        <div class="space-y-2">
          <label class="block text-xs font-semibold text-slate-300">Scan Barcode / QR Code / Asset ID</label>
          <div class="flex gap-2">
            <input
              type="text"
              [(ngModel)]="scanInput"
              (keyup.enter)="onScan()"
              placeholder="e.g. RF-CHI000001000001 or CHI-000001"
              class="flex-1 px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono text-sm focus:outline-none focus:border-indigo-500"
            />
            <button (click)="onScan()" [disabled]="loading || !scanInput" class="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs rounded-xl shadow-md">
              Find Asset
            </button>
          </div>
        </div>

        <div *ngIf="loading" class="py-8 text-center text-xs text-slate-400">
          <div class="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
          Scanning barcode lookup registry...
        </div>

        <!-- SCAN RESULT DISPLAY -->
        <div *ngIf="result" class="p-5 rounded-2xl border bg-slate-950 space-y-4"
             [ngClass]="result.found ? 'border-emerald-500/40' : 'border-rose-500/40'">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="text-xl">{{ result.found ? '✅' : '❌' }}</span>
              <span class="font-bold text-white text-sm">{{ result.message }}</span>
            </div>
            <span *ngIf="result.found && result.status" class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              {{ result.status }}
            </span>
          </div>

          <div *ngIf="result.found" class="grid grid-cols-2 gap-4 text-xs pt-2 border-t border-slate-800">
            <div>
              <span class="text-slate-400">Product Name:</span>
              <div class="font-bold text-white mt-0.5">{{ result.productName }} ({{ result.productSku }})</div>
            </div>
            <div>
              <span class="text-slate-400">Asset Code:</span>
              <div class="font-mono font-bold text-indigo-300 mt-0.5">{{ result.assetCode }}</div>
            </div>
            <div>
              <span class="text-slate-400">Warehouse:</span>
              <div class="font-semibold text-slate-200 mt-0.5">{{ result.warehouseName || 'Main Warehouse' }}</div>
            </div>
            <div>
              <span class="text-slate-400">Condition:</span>
              <div class="font-semibold text-slate-200 mt-0.5">{{ result.condition }}</div>
            </div>
            <div *ngIf="result.currentBookingNumber">
              <span class="text-slate-400">Rental Booking:</span>
              <div class="font-bold text-sky-400 mt-0.5">{{ result.currentBookingNumber }} ({{ result.customerName }})</div>
            </div>
          </div>

          <div *ngIf="result.found && result.assetId" class="pt-3 flex justify-end">
            <a [routerLink]="['/dashboard/inventory/assets', result.assetId]" class="px-3 py-1.5 rounded-xl bg-indigo-600/20 text-indigo-300 border border-indigo-500/30 text-xs font-bold hover:bg-indigo-600/30">
              View Asset Details & History →
            </a>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ScanScreenComponent {
  scanInput = '';
  loading = false;
  result: AssetScanResult | null = null;

  constructor(private inventoryService: Inventory2Service) {}

  onScan(): void {
    if (!this.scanInput.trim()) return;
    this.loading = true;
    this.result = null;
    this.inventoryService.scanAsset(this.scanInput.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.result = res;
      },
      error: (err) => {
        this.loading = false;
        this.result = { found: false, message: 'Scan failed: ' + (err.error?.message || err.message) };
      }
    });
  }
}
