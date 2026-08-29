import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { WarehouseService, WarehouseLocation } from '../../services/warehouse.service';
import { InventoryTransfer } from '../../models/inventory.models';

@Component({
  selector: 'app-stock-transfers',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Multi-Warehouse Transfers</h1>
          <p class="text-xs text-slate-400">Inter-warehouse stock transfer requests, shipment dispatch & partial receiving</p>
        </div>
        <button (click)="showCreateModal = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold rounded-xl shadow-md">
          + New Transfer Request
        </button>
      </div>

      <!-- TRANSFERS TABLE -->
      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <table class="w-full text-left text-xs">
          <thead>
            <tr class="bg-slate-950 border-b border-slate-800 text-slate-400 font-bold uppercase">
              <th class="py-3.5 px-4">Transfer #</th>
              <th class="py-3.5 px-3">From Warehouse</th>
              <th class="py-3.5 px-3">To Warehouse</th>
              <th class="py-3.5 px-3 text-center">Status</th>
              <th class="py-3.5 px-3">Requested By</th>
              <th class="py-3.5 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let t of transfers" class="hover:bg-slate-900/50">
              <td class="py-3 px-4 font-mono font-bold text-indigo-300">{{ t.transferNumber }}</td>
              <td class="py-3 px-3 font-semibold text-slate-200">{{ t.fromWarehouseName }}</td>
              <td class="py-3 px-3 font-semibold text-slate-200">{{ t.toWarehouseName }}</td>
              <td class="py-3 px-3 text-center">
                <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold border"
                      [ngClass]="{
                        'bg-sky-500/15 text-sky-300 border-sky-500/30': t.status === 'REQUESTED',
                        'bg-indigo-500/15 text-indigo-300 border-indigo-500/30': t.status === 'APPROVED',
                        'bg-amber-500/15 text-amber-300 border-amber-500/30': t.status === 'IN_TRANSIT',
                        'bg-emerald-500/15 text-emerald-300 border-emerald-500/30': t.status === 'RECEIVED'
                      }">
                  {{ t.status }}
                </span>
              </td>
              <td class="py-3 px-3 text-slate-400">{{ t.requestedBy || 'User' }}</td>
              <td class="py-3 px-4 text-center">
                <div class="flex items-center justify-center gap-2">
                  <button *ngIf="t.status === 'REQUESTED'" (click)="approve(t.id)" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-indigo-600/20 text-indigo-300 border border-indigo-500/30">
                    Approve
                  </button>
                  <button *ngIf="t.status === 'APPROVED'" (click)="ship(t.id)" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-amber-600/20 text-amber-300 border border-amber-500/30">
                    Ship Dispatch
                  </button>
                  <button *ngIf="t.status === 'IN_TRANSIT' || t.status === 'PARTIALLY_RECEIVED'" (click)="receive(t.id)" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-600/20 text-emerald-300 border border-emerald-500/30">
                    Receive Stock
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- CREATE MODAL -->
      <div *ngIf="showCreateModal" class="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
        <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-lg w-full space-y-4 shadow-2xl">
          <h3 class="text-lg font-bold text-white">Create Inter-Warehouse Transfer</h3>
          <div>
            <label class="block text-xs text-slate-400 mb-1">From Warehouse *</label>
            <select [(ngModel)]="fromWhId" class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option *ngFor="let w of warehouses" [value]="w.id">{{ w.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-xs text-slate-400 mb-1">To Warehouse *</label>
            <select [(ngModel)]="toWhId" class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option *ngFor="let w of warehouses" [value]="w.id">{{ w.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-xs text-slate-400 mb-1">Transfer Notes</label>
            <input type="text" [(ngModel)]="notes" placeholder="Stock rebalancing note..." class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button (click)="showCreateModal = false" class="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold">Cancel</button>
            <button (click)="createTransfer()" [disabled]="!fromWhId || !toWhId" class="px-4 py-2 bg-indigo-600 text-white rounded-xl text-xs font-bold">Submit Request</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class StockTransfersComponent implements OnInit {
  transfers: InventoryTransfer[] = [];
  warehouses: WarehouseLocation[] = [];

  showCreateModal = false;
  fromWhId = '';
  toWhId = '';
  notes = '';

  constructor(
    private inventoryService: Inventory2Service,
    private warehouseService: WarehouseService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.inventoryService.getTransfers().subscribe({ next: (t) => (this.transfers = t) });
    this.warehouseService.getWarehouses().subscribe({ next: (w: WarehouseLocation[]) => (this.warehouses = w) });
  }

  createTransfer(): void {
    if (!this.fromWhId || !this.toWhId) return;
    this.inventoryService.createTransfer({ fromWarehouseId: this.fromWhId, toWarehouseId: this.toWhId, notes: this.notes }).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.loadData();
      },
      error: (err) => alert('Failed to create transfer: ' + (err.error?.message || err.message))
    });
  }

  approve(id: string): void {
    this.inventoryService.approveTransfer(id).subscribe({ next: () => this.loadData() });
  }

  ship(id: string): void {
    this.inventoryService.shipTransfer(id).subscribe({ next: () => this.loadData() });
  }

  receive(id: string): void {
    this.inventoryService.receiveTransfer(id).subscribe({ next: () => this.loadData() });
  }
}
