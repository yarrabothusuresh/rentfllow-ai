import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PackList } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-pack-lists',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="pack-lists-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>📦 Warehouse Pack & Kit Lists</h2>
          <p class="subtitle">Container packing, kit component validation, and staging prep.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/containers" class="btn btn-secondary">🧰 Containers Registry</a>
          <a routerLink="/warehouse/my-work" class="btn btn-accent">👷 My Work</a>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search pack list #, order #, event, customer..." (input)="filterLists()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'PENDING'" (click)="setStatusFilter('PENDING')">Pending</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'IN_PROGRESS'" (click)="setStatusFilter('IN_PROGRESS')">Packing</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'COMPLETED'" (click)="setStatusFilter('COMPLETED')">Completed</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'VERIFIED'" (click)="setStatusFilter('VERIFIED')">Verified</button>
        </div>
      </div>

      <!-- Table Card -->
      <div class="table-card">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading pack lists...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredPackLists.length === 0">
          <p>No pack lists found matching criteria.</p>
        </div>

        <table class="data-table" *ngIf="!loading && filteredPackLists.length > 0">
          <thead>
            <tr>
              <th>Pack List #</th>
              <th>Order / Booking</th>
              <th>Event</th>
              <th>Status</th>
              <th>Packed Progress</th>
              <th>Containers</th>
              <th>Assigned To</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let pkl of filteredPackLists">
              <td>
                <span class="code-badge-purple">{{ pkl.packListNumber }}</span>
              </td>
              <td>
                <div><strong>{{ pkl.warehouseOrderNumber }}</strong></div>
                <small class="text-muted">{{ pkl.bookingNumber }}</small>
              </td>
              <td>
                <div><strong>{{ pkl.eventName || 'Event Rental' }}</strong></div>
                <small class="text-muted">{{ pkl.customerName }}</small>
              </td>
              <td>
                <span class="status-badge" [ngClass]="'status-' + pkl.status.toLowerCase()">{{ pkl.status }}</span>
              </td>
              <td>
                <div class="progress-cell">
                  <div class="progress-text">{{ pkl.totalPackedItems }} / {{ pkl.totalRequiredItems }} ({{ pkl.progressPercentage | number:'1.0-0' }}%)</div>
                  <div class="progress-bar-mini">
                    <div class="progress-fill-purple" [style.width.%]="pkl.progressPercentage"></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="container-tags" *ngIf="pkl.containers && pkl.containers.length > 0">
                  <span class="cont-pill" *ngFor="let c of pkl.containers">{{ c }}</span>
                </div>
                <span class="text-muted" *ngIf="!pkl.containers || pkl.containers.length === 0">0 Containers</span>
              </td>
              <td>
                <span class="assigned-user">{{ pkl.assignedTo || 'Unassigned' }}</span>
              </td>
              <td>
                <a [routerLink]="['/warehouse/pack-lists', pkl.id]" class="btn-action">Pack & View ➔</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .pack-lists-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .header-actions { display: flex; gap: 0.75rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-secondary { background: #334155; color: #f8fafc; }
    .btn-accent { background: #7c3aed; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #a855f7; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #7c3aed; color: #fff; border-color: #7c3aed; font-weight: 600; }

    .table-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.9rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.9rem 1rem; border-bottom: 1px solid #334155; font-size: 0.9rem; }
    .data-table tr:hover { background: rgba(51, 65, 85, 0.4); }

    .code-badge-purple { font-family: monospace; background: rgba(168, 85, 247, 0.15); color: #c084fc; padding: 0.25rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .text-muted { color: #94a3b8; font-size: 0.8rem; }

    .status-badge { font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-in_progress { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-completed { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-verified { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .progress-cell { width: 140px; }
    .progress-text { font-size: 0.75rem; color: #cbd5e1; margin-bottom: 0.25rem; }
    .progress-bar-mini { height: 5px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill-purple { height: 100%; background: #a855f7; }

    .container-tags { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .cont-pill { font-family: monospace; font-size: 0.75rem; background: rgba(20, 184, 166, 0.2); color: #2dd4bf; padding: 0.15rem 0.4rem; border-radius: 3px; }

    .btn-action { background: #7c3aed; color: #fff; padding: 0.4rem 0.8rem; border-radius: 6px; font-size: 0.8rem; text-decoration: none; font-weight: 600; display: inline-block; }
    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #a855f7; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class WarehousePackListsComponent implements OnInit {
  packLists: PackList[] = [];
  filteredPackLists: PackList[] = [];
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
      this.loadPackLists();
    });
  }

  loadPackLists(): void {
    this.loading = true;
    this.fulfillmentService.getPackLists().subscribe({
      next: (lists) => {
        this.packLists = lists;
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
    this.filteredPackLists = this.packLists.filter(pkl => {
      const matchStatus = this.selectedStatus === 'ALL' || pkl.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        pkl.packListNumber.toLowerCase().includes(term) ||
        (pkl.warehouseOrderNumber && pkl.warehouseOrderNumber.toLowerCase().includes(term)) ||
        (pkl.bookingNumber && pkl.bookingNumber.toLowerCase().includes(term)) ||
        (pkl.customerName && pkl.customerName.toLowerCase().includes(term)) ||
        (pkl.eventName && pkl.eventName.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }
}
