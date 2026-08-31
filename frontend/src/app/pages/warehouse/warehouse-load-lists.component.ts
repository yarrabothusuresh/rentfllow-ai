import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, LoadList } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-load-lists',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="load-lists-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>🚚 Vehicle Load & Driver Handoff Lists</h2>
          <p class="subtitle">Stage vehicle loading, capacity checks, and driver sign-offs.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/my-work" class="btn btn-accent">👷 My Work</a>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search load list #, vehicle, driver, order..." (input)="filterLists()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'PENDING'" (click)="setStatusFilter('PENDING')">Pending</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'LOADING'" (click)="setStatusFilter('LOADING')">Loading</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'LOADED'" (click)="setStatusFilter('LOADED')">Loaded</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'VERIFIED'" (click)="setStatusFilter('VERIFIED')">Verified</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'HANDED_OFF'" (click)="setStatusFilter('HANDED_OFF')">Handed to Driver</button>
        </div>
      </div>

      <!-- Table View -->
      <div class="table-card">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading vehicle load lists...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredLoadLists.length === 0">
          <p>No load lists found matching criteria.</p>
        </div>

        <table class="data-table" *ngIf="!loading && filteredLoadLists.length > 0">
          <thead>
            <tr>
              <th>Load List #</th>
              <th>Order / Delivery</th>
              <th>Vehicle & Driver</th>
              <th>Capacity Check</th>
              <th>Status</th>
              <th>Loaded Progress</th>
              <th>Containers</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let ld of filteredLoadLists">
              <td>
                <span class="code-badge-green">{{ ld.loadListNumber }}</span>
              </td>
              <td>
                <div><strong>{{ ld.warehouseOrderNumber }}</strong></div>
                <small class="text-muted">{{ ld.deliveryNumber || ld.bookingNumber }}</small>
              </td>
              <td>
                <div><strong>{{ ld.vehicleCodeSnapshot || ld.vehicleName || 'Unassigned Vehicle' }}</strong></div>
                <small class="text-muted">{{ ld.driverNameSnapshot || 'Unassigned Driver' }}</small>
              </td>
              <td>
                <span class="cap-badge ok" *ngIf="!ld.capacityWarning">
                  ✓ {{ ld.estimatedLoadWeightKg | number:'1.0-0' }} / {{ ld.vehicleCapacityWeightKg | number:'1.0-0' }} kg
                </span>
                <span class="cap-badge warning" *ngIf="ld.capacityWarning">
                  ⚠️ {{ ld.estimatedLoadWeightKg | number:'1.0-0' }} kg (Overloaded)
                </span>
              </td>
              <td>
                <span class="status-badge" [ngClass]="'status-' + ld.status.toLowerCase()">{{ ld.status }}</span>
              </td>
              <td>
                <div class="progress-cell">
                  <div class="progress-text">{{ ld.loadedItems }} / {{ ld.totalItems }} ({{ ld.progressPercentage | number:'1.0-0' }}%)</div>
                  <div class="progress-bar-mini">
                    <div class="progress-fill-green" [style.width.%]="ld.progressPercentage"></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="container-tags" *ngIf="ld.containers && ld.containers.length > 0">
                  <span class="cont-pill" *ngFor="let c of ld.containers">{{ c }}</span>
                </div>
                <span class="text-muted" *ngIf="!ld.containers || ld.containers.length === 0">0 Containers</span>
              </td>
              <td>
                <a [routerLink]="['/warehouse/load-lists', ld.id]" class="btn-action">Load & Sign ➔</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .load-lists-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .header-actions { display: flex; gap: 0.75rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-accent { background: #7c3aed; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #10b981; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #10b981; color: #fff; border-color: #10b981; font-weight: 600; }

    .table-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.9rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.9rem 1rem; border-bottom: 1px solid #334155; font-size: 0.9rem; }
    .data-table tr:hover { background: rgba(51, 65, 85, 0.4); }

    .code-badge-green { font-family: monospace; background: rgba(34, 197, 94, 0.15); color: #4ade80; padding: 0.25rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .text-muted { color: #94a3b8; font-size: 0.8rem; }

    .cap-badge { font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; display: inline-block; }
    .cap-badge.ok { background: rgba(34, 197, 94, 0.15); color: #4ade80; }
    .cap-badge.warning { background: rgba(239, 68, 68, 0.2); color: #f87171; }

    .status-badge { font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-loading { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-loaded { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-verified { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-handed_off { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .progress-cell { width: 140px; }
    .progress-text { font-size: 0.75rem; color: #cbd5e1; margin-bottom: 0.25rem; }
    .progress-bar-mini { height: 5px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill-green { height: 100%; background: #10b981; }

    .container-tags { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .cont-pill { font-family: monospace; font-size: 0.75rem; background: rgba(20, 184, 166, 0.2); color: #2dd4bf; padding: 0.15rem 0.4rem; border-radius: 3px; }

    .btn-action { background: #10b981; color: #fff; padding: 0.4rem 0.8rem; border-radius: 6px; font-size: 0.8rem; text-decoration: none; font-weight: 600; display: inline-block; }
    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #10b981; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class WarehouseLoadListsComponent implements OnInit {
  loadLists: LoadList[] = [];
  filteredLoadLists: LoadList[] = [];
  selectedStatus: string = 'ALL';
  searchTerm: string = '';
  loading = true;

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['status']) {
        this.selectedStatus = params['status'];
      }
      this.loadLoadLists();
    });
  }

  loadLoadLists(): void {
    this.loading = true;
    this.fulfillmentService.getLoadLists().subscribe({
      next: (lists) => {
        this.loadLists = lists;
        this.filterLists();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.filterLists();
  }

  filterLists(): void {
    this.filteredLoadLists = this.loadLists.filter(ld => {
      const matchStatus = this.selectedStatus === 'ALL' || ld.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        ld.loadListNumber.toLowerCase().includes(term) ||
        (ld.warehouseOrderNumber && ld.warehouseOrderNumber.toLowerCase().includes(term)) ||
        (ld.bookingNumber && ld.bookingNumber.toLowerCase().includes(term)) ||
        (ld.vehicleCodeSnapshot && ld.vehicleCodeSnapshot.toLowerCase().includes(term)) ||
        (ld.driverNameSnapshot && ld.driverNameSnapshot.toLowerCase().includes(term)) ||
        (ld.eventName && ld.eventName.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }
}
