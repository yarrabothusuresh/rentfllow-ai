import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { WarehouseService, WarehouseLocation } from '../../services/warehouse.service';
import { InventoryCycleCount } from '../../models/inventory.models';

@Component({
  selector: 'app-cycle-count',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Physical Inventory Cycle Counts</h1>
          <p class="text-xs text-slate-400">Perform physical count audits, record variances and process manager stock adjustments</p>
        </div>
        <button (click)="showCreateModal = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold rounded-xl shadow-md">
          + Plan New Cycle Count
        </button>
      </div>

      <!-- COUNTS TABLE -->
      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <table class="w-full text-left text-xs">
          <thead>
            <tr class="bg-slate-950 border-b border-slate-800 text-slate-400 font-bold uppercase">
              <th class="py-3.5 px-4">Count #</th>
              <th class="py-3.5 px-3">Warehouse</th>
              <th class="py-3.5 px-3">Count Date</th>
              <th class="py-3.5 px-3 text-center">Status</th>
              <th class="py-3.5 px-3">Assigned To</th>
              <th class="py-3.5 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let cc of counts" class="hover:bg-slate-900/50">
              <td class="py-3 px-4 font-mono font-bold text-indigo-300">{{ cc.countNumber }}</td>
              <td class="py-3 px-3 font-semibold text-slate-200">{{ getWarehouseName(cc.warehouseId) }}</td>
              <td class="py-3 px-3 text-slate-300 font-mono">{{ cc.countDate | date:'shortDate' }}</td>
              <td class="py-3 px-3 text-center">
                <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold border"
                      [ngClass]="{
                        'bg-sky-500/15 text-sky-300 border-sky-500/30': cc.status === 'PLANNED',
                        'bg-amber-500/15 text-amber-300 border-amber-500/30': cc.status === 'IN_PROGRESS',
                        'bg-purple-500/15 text-purple-300 border-purple-500/30': cc.status === 'COMPLETED',
                        'bg-emerald-500/15 text-emerald-300 border-emerald-500/30': cc.status === 'APPROVED'
                      }">
                  {{ cc.status }}
                </span>
              </td>
              <td class="py-3 px-3 text-slate-400">{{ cc.assignedTo || 'Unassigned' }}</td>
              <td class="py-3 px-4 text-center">
                <div class="flex items-center justify-center gap-2">
                  <button *ngIf="cc.status === 'IN_PROGRESS' || cc.status === 'PLANNED'" (click)="complete(cc.id)" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-purple-600/20 text-purple-300 border border-purple-500/30">
                    Mark Complete
                  </button>
                  <button *ngIf="cc.status === 'COMPLETED'" (click)="approve(cc.id)" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-600/20 text-emerald-300 border border-emerald-500/30">
                    Approve Adjustments
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
          <h3 class="text-lg font-bold text-white">Plan Physical Cycle Count</h3>
          <div>
            <label class="block text-xs text-slate-400 mb-1">Target Warehouse *</label>
            <select [(ngModel)]="warehouseId" class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white">
              <option *ngFor="let w of warehouses" [value]="w.id">{{ w.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-xs text-slate-400 mb-1">Assigned Counter / Staff</label>
            <input type="text" [(ngModel)]="assignedTo" placeholder="e.g. warehouse.operator" class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>
          <div>
            <label class="block text-xs text-slate-400 mb-1">Notes</label>
            <input type="text" [(ngModel)]="notes" placeholder="e.g. Monthly seating section audit" class="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button (click)="showCreateModal = false" class="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold">Cancel</button>
            <button (click)="createCount()" [disabled]="!warehouseId" class="px-4 py-2 bg-indigo-600 text-white rounded-xl text-xs font-bold">Start Count Task</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class CycleCountComponent implements OnInit {
  counts: InventoryCycleCount[] = [];
  warehouses: WarehouseLocation[] = [];

  showCreateModal = false;
  warehouseId = '';
  assignedTo = '';
  notes = '';

  constructor(
    private inventoryService: Inventory2Service,
    private warehouseService: WarehouseService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.inventoryService.getCycleCounts().subscribe({ next: (c) => (this.counts = c) });
    this.warehouseService.getWarehouses().subscribe({ next: (w: WarehouseLocation[]) => (this.warehouses = w) });
  }

  getWarehouseName(id: string): string {
    const w = this.warehouses.find((x) => x.id === id);
    return w ? w.name : 'Warehouse';
  }

  createCount(): void {
    if (!this.warehouseId) return;
    this.inventoryService.createCycleCount({ warehouseId: this.warehouseId, assignedTo: this.assignedTo, notes: this.notes }).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.loadData();
      },
      error: (err) => alert('Failed to create count: ' + (err.error?.message || err.message))
    });
  }

  complete(id: string): void {
    this.inventoryService.completeCycleCount(id).subscribe({ next: () => this.loadData() });
  }

  approve(id: string): void {
    this.inventoryService.approveCycleCount(id).subscribe({ next: () => this.loadData() });
  }
}
