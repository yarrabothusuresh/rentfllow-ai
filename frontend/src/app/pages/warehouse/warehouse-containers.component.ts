import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PackingContainer } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-containers',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="containers-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>🧰 Reusable Transport Containers & Gear</h2>
          <p class="subtitle">Manage transport bags, flight cases, rolling carts, crates, and staging pallets.</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-primary" (click)="showNewModal = true">+ Register Container</button>
        </div>
      </div>

      <!-- Filters -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search container code, type, notes..." (input)="filterContainers()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'AVAILABLE'" (click)="setStatusFilter('AVAILABLE')">✓ Available</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'IN_USE'" (click)="setStatusFilter('IN_USE')">📦 In Use</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'MAINTENANCE'" (click)="setStatusFilter('MAINTENANCE')">🔧 Maintenance</button>
        </div>
      </div>

      <!-- Container Cards Grid -->
      <div class="containers-grid">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading containers...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredContainers.length === 0">
          <p>No containers found.</p>
        </div>

        <div class="cont-card" *ngFor="let c of filteredContainers">
          <div class="cont-card-header">
            <div class="cont-icon-wrap" [ngClass]="'type-' + c.containerType.toLowerCase()">
              {{ getIcon(c.containerType) }}
            </div>
            <div class="cont-main">
              <span class="cont-code">{{ c.containerCode }}</span>
              <span class="cont-type">{{ c.containerType }}</span>
            </div>
            <span class="status-pill" [ngClass]="'status-' + c.status.toLowerCase()">{{ c.status }}</span>
          </div>

          <div class="cont-body">
            <p class="cont-notes">{{ c.notes || 'Standard transport container unit.' }}</p>
            <p *ngIf="c.currentWarehouseOrderId" class="cont-order">
              Currently Allocated to Order: <strong>{{ c.currentWarehouseOrderId }}</strong>
            </p>
          </div>
        </div>
      </div>

      <!-- Register Container Modal -->
      <div class="modal-backdrop" *ngIf="showNewModal">
        <div class="modal-card">
          <h3>+ Register New Transport Container</h3>

          <label>Container Code (Barcode/QR):</label>
          <input type="text" [(ngModel)]="newCode" class="form-input" placeholder="e.g. CASE-005, CART-003, PALLET-010" />

          <label>Container Type:</label>
          <select [(ngModel)]="newType" class="form-input">
            <option value="BAG">Linen Transport Bag</option>
            <option value="CASE">Flight / Padded Case</option>
            <option value="CART">Rolling Cart / Dolly</option>
            <option value="PALLET">Wood / Plastic Pallet</option>
            <option value="CRATE">Heavy Duty Crate</option>
            <option value="BOX">Corrugated Staging Box</option>
          </select>

          <label>Description / Notes:</label>
          <input type="text" [(ngModel)]="newNotes" class="form-input" placeholder="e.g. Foam insert for stage uplights" />

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showNewModal = false">Cancel</button>
            <button class="btn-confirm-save" (click)="createContainer()">Save Container</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .containers-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-primary { background: #2563eb; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #14b8a6; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #0d9488; color: #fff; border-color: #0d9488; font-weight: 600; }

    .containers-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.25rem; }
    .cont-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.25rem; display: flex; flex-direction: column; }
    .cont-card-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.75rem; }
    .cont-icon-wrap { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
    .type-bag { background: rgba(59, 130, 246, 0.2); }
    .type-case { background: rgba(168, 85, 247, 0.2); }
    .type-cart { background: rgba(34, 197, 94, 0.2); }
    .type-pallet { background: rgba(245, 158, 11, 0.2); }
    .type-crate { background: rgba(20, 184, 166, 0.2); }
    .type-box { background: rgba(148, 163, 184, 0.2); }

    .cont-main { flex: 1; display: flex; flex-direction: column; }
    .cont-code { font-family: monospace; font-size: 1.1rem; font-weight: 700; color: #f8fafc; }
    .cont-type { font-size: 0.75rem; color: #94a3b8; }

    .status-pill { font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 9999px; font-weight: 600; }
    .status-available { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-in_use { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-maintenance { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }

    .cont-body { font-size: 0.85rem; color: #cbd5e1; }
    .cont-notes { margin: 0 0 0.35rem 0; }
    .cont-order { margin: 0; font-size: 0.75rem; color: #38bdf8; }

    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; grid-column: 1 / -1; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #14b8a6; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Modal */
    .modal-backdrop { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { background: #1e293b; border-radius: 12px; border: 1px solid #475569; width: 100%; max-width: 480px; padding: 1.5rem; }
    .modal-card h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; }
    .modal-card label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    .modal-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1.25rem; }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-save { background: #0d9488; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class WarehouseContainersComponent implements OnInit {
  containers: PackingContainer[] = [];
  filteredContainers: PackingContainer[] = [];
  selectedStatus: string = 'ALL';
  searchTerm: string = '';
  loading = true;

  showNewModal = false;
  newCode = '';
  newType: any = 'CASE';
  newNotes = '';

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadContainers();
  }

  loadContainers(): void {
    this.loading = true;
    this.fulfillmentService.getContainers().subscribe({
      next: (conts) => {
        this.containers = conts;
        this.filterContainers();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.filterContainers();
  }

  filterContainers(): void {
    this.filteredContainers = this.containers.filter(c => {
      const matchStatus = this.selectedStatus === 'ALL' || c.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        c.containerCode.toLowerCase().includes(term) ||
        c.containerType.toLowerCase().includes(term) ||
        (c.notes && c.notes.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'BAG': return '🛍️';
      case 'CASE': return '💼';
      case 'CART': return '🛒';
      case 'PALLET': return '🪵';
      case 'CRATE': return '📦';
      default: return '🧰';
    }
  }

  createContainer(): void {
    if (!this.newCode) return;
    this.fulfillmentService.createContainer({
      containerCode: this.newCode,
      containerType: this.newType,
      notes: this.newNotes
    }).subscribe({
      next: () => {
        this.showNewModal = false;
        this.newCode = '';
        this.newNotes = '';
        this.loadContainers();
      },
      error: (err) => {
        alert(err.error?.message || 'Error registering container');
      }
    });
  }
}
