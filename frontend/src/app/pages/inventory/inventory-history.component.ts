import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Inventory2Service } from '../../services/inventory-2.service';
import { StockMovement } from '../../models/inventory.models';

@Component({
  selector: 'app-inventory-history',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="max-w-7xl mx-auto space-y-6 text-slate-100">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-white">Immutable Stock Movement Audit Ledger</h1>
          <p class="text-xs text-slate-400">Complete audit trail of all receipts, checkouts, returns, transfers & adjustments</p>
        </div>
        <a routerLink="/dashboard/inventory" class="px-3 py-1.5 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700 border border-slate-700">
          ← Back to Inventory
        </a>
      </div>

      <div class="bg-slate-900/90 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <table class="w-full text-left text-xs">
          <thead>
            <tr class="bg-slate-950 border-b border-slate-800 text-slate-400 font-bold uppercase">
              <th class="py-3.5 px-4">Timestamp</th>
              <th class="py-3.5 px-3">Movement Type</th>
              <th class="py-3.5 px-3 text-center">Qty</th>
              <th class="py-3.5 px-3">Reference / Reason</th>
              <th class="py-3.5 px-3">Operator</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let m of movements" class="hover:bg-slate-900/50">
              <td class="py-3 px-4 font-mono text-slate-300 text-[11px]">{{ m.createdAt | date:'medium' }}</td>
              <td class="py-3 px-3">
                <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold border"
                      [ngClass]="{
                        'bg-emerald-500/15 text-emerald-300 border-emerald-500/30': m.movementType === 'RECEIPT' || m.movementType === 'RETURN',
                        'bg-sky-500/15 text-sky-300 border-sky-500/30': m.movementType === 'CHECKOUT',
                        'bg-amber-500/15 text-amber-300 border-amber-500/30': m.movementType === 'TRANSFER_OUT' || m.movementType === 'TRANSFER_IN',
                        'bg-rose-500/15 text-rose-300 border-rose-500/30': m.movementType === 'DAMAGE' || m.movementType === 'LOSS'
                      }">
                  {{ m.movementType }}
                </span>
              </td>
              <td class="py-3 px-3 text-center font-bold" [class.text-emerald-400]="m.quantity > 0" [class.text-rose-400]="m.quantity < 0">
                {{ m.quantity }}
              </td>
              <td class="py-3 px-3 text-slate-200">
                <div>{{ m.reason || 'Standard operation' }}</div>
                <div *ngIf="m.referenceType" class="text-[10px] text-slate-400">Ref: {{ m.referenceType }}</div>
              </td>
              <td class="py-3 px-3 text-slate-400">{{ m.performedBy || 'System' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class InventoryHistoryComponent implements OnInit {
  movements: StockMovement[] = [];

  constructor(private inventoryService: Inventory2Service) {}

  ngOnInit(): void {
    this.inventoryService.getMovements().subscribe({
      next: (page) => (this.movements = page.content || []),
      error: (err) => console.error('Failed to load movements', err)
    });
  }
}
