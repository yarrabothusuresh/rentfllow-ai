import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { InventorySummary2 } from '../../models/inventory.models';

@Component({
  selector: 'app-inventory-dashboard-2',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <!-- HEADER -->
      <div class="relative overflow-hidden bg-gradient-to-r from-slate-900 via-indigo-950/70 to-slate-900 rounded-3xl border border-indigo-500/20 p-6 md:p-8 shadow-2xl backdrop-blur-xl">
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div class="flex items-center gap-3">
            <div class="p-3 rounded-2xl bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
              <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m-8-10l-8-4m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <div>
              <h1 class="text-3xl font-extrabold text-white tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-100 to-indigo-200">
                Inventory 2.0 Control Dashboard
              </h1>
              <p class="text-slate-400 text-sm mt-0.5">Serialized asset tracking, barcode scanning, stock movements & multi-warehouse status</p>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3">
            <a routerLink="/dashboard/inventory/receive" class="px-4 py-2.5 rounded-xl text-xs font-bold text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 transition flex items-center gap-2">
              📥 Receive Stock
            </a>
            <a routerLink="/dashboard/inventory/transfers" class="px-4 py-2.5 rounded-xl text-xs font-bold text-sky-300 bg-sky-500/10 hover:bg-sky-500/20 border border-sky-500/30 transition flex items-center gap-2">
              🚚 Transfers
            </a>
            <a routerLink="/dashboard/inventory/scan" class="px-4 py-2.5 rounded-xl text-xs font-bold text-white bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 border border-indigo-400/30 transition flex items-center gap-2 shadow-lg">
              🔍 Scan Barcode/QR
            </a>
          </div>
        </div>

        <!-- STAT CARDS -->
        <div *ngIf="summary" class="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-6 gap-4 mt-6 pt-6 border-t border-slate-800/80">
          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Total Fleet Assets</div>
            <div class="text-2xl font-bold text-white mt-1">{{ summary.totalAssets | number }}</div>
            <div class="text-[10px] text-indigo-400 mt-0.5">Across All Warehouses</div>
          </div>

          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Available Now</div>
            <div class="text-2xl font-bold text-emerald-400 mt-1">{{ summary.availableAssets | number }}</div>
            <div class="text-[10px] text-emerald-300 mt-0.5">Ready for Rental</div>
          </div>

          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Out On Rent</div>
            <div class="text-2xl font-bold text-sky-400 mt-1">{{ summary.outOnRentAssets | number }}</div>
            <div class="text-[10px] text-sky-300 mt-0.5">Active Customer Rentals</div>
          </div>

          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Under Maintenance</div>
            <div class="text-2xl font-bold text-amber-400 mt-1">{{ summary.maintenanceAssets | number }}</div>
            <div class="text-[10px] text-amber-300 mt-0.5">In Repair Center</div>
          </div>

          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Damaged / Lost</div>
            <div class="text-2xl font-bold text-rose-400 mt-1">{{ summary.damagedAssets + summary.lostAssets }}</div>
            <div class="text-[10px] text-rose-300 mt-0.5">Under Inspection/Claim</div>
          </div>

          <div class="bg-slate-950/60 rounded-2xl p-4 border border-slate-800">
            <div class="text-[11px] font-semibold text-slate-400 uppercase">Asset Valuation</div>
            <div class="text-2xl font-bold text-slate-100 mt-1">\${{ summary.totalAssetValue | number:'1.0-0' }}</div>
            <div class="text-[10px] text-slate-400 mt-0.5">Capital Investment</div>
          </div>
        </div>
      </div>

      <!-- QUICK NAVIGATION GRID -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <a routerLink="/dashboard/inventory" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">📦</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Product Inventory</h3>
          <p class="text-xs text-slate-400 mt-1">View tracking types, total stock, available units, and serialized asset lists.</p>
        </a>

        <a routerLink="/dashboard/inventory/receive" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">📥</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Receive Stock</h3>
          <p class="text-xs text-slate-400 mt-1">Receive new stock purchase orders, enter serial numbers & auto-generate barcodes.</p>
        </a>

        <a routerLink="/dashboard/inventory/adjust" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">⚖️</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Stock Adjustments</h3>
          <p class="text-xs text-slate-400 mt-1">Record cycle count corrections, damages, write-offs with mandatory reason logs.</p>
        </a>

        <a routerLink="/dashboard/inventory/transfers" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">🚚</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Multi-Warehouse Transfers</h3>
          <p class="text-xs text-slate-400 mt-1">Transfer stock between warehouses with ship, receive, and partial delivery tracking.</p>
        </a>

        <a routerLink="/dashboard/inventory/count" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">📋</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Cycle Counts</h3>
          <p class="text-xs text-slate-400 mt-1">Perform physical warehouse counts, identify variances and submit manager approvals.</p>
        </a>

        <a routerLink="/dashboard/inventory/history" class="bg-slate-900/90 p-6 rounded-2xl border border-slate-800 hover:border-indigo-500/40 transition group">
          <div class="text-2xl mb-2">📜</div>
          <h3 class="font-bold text-white group-hover:text-indigo-300 transition text-lg">Stock Movement Ledger</h3>
          <p class="text-xs text-slate-400 mt-1">Immutable audit trail of all receipts, checkouts, returns, damages, and transfers.</p>
        </a>
      </div>
    </div>
  `
})
export class InventoryDashboard2Component implements OnInit {
  summary: InventorySummary2 | null = null;

  constructor(private inventoryService: Inventory2Service) {}

  ngOnInit(): void {
    this.inventoryService.getSummary().subscribe({
      next: (s) => (this.summary = s),
      error: (err) => console.error('Failed to load inventory summary', err)
    });
  }
}
