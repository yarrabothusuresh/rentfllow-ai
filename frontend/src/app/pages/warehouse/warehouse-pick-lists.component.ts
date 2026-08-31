import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PickList } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-pick-lists',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="pick-lists-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>🚜 Warehouse Pick Lists</h2>
          <p class="subtitle">Location-sequenced pick lists for event order fulfillment.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/my-work" class="btn btn-accent">👷 My Pick Work</a>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search pick list #, order #, customer, venue..." (input)="filterLists()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'PENDING'" (click)="setStatusFilter('PENDING')">Ready (Pending)</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'IN_PROGRESS'" (click)="setStatusFilter('IN_PROGRESS')">In Progress</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'COMPLETED'" (click)="setStatusFilter('COMPLETED')">Completed</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'VERIFIED'" (click)="setStatusFilter('VERIFIED')">Verified</button>
        </div>
      </div>

      <!-- Table View -->
      <div class="table-card">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading pick lists...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredPickLists.length === 0">
          <p>No pick lists found matching the filter criteria.</p>
        </div>

        <table class="data-table" *ngIf="!loading && filteredPickLists.length > 0">
          <thead>
            <tr>
              <th>Pick List #</th>
              <th>Order / Booking</th>
              <th>Event & Venue</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Progress</th>
              <th>Assigned To</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let pl of filteredPickLists">
              <td>
                <span class="code-badge">{{ pl.pickListNumber }}</span>
              </td>
              <td>
                <div><strong>{{ pl.warehouseOrderNumber }}</strong></div>
                <small class="text-muted">{{ pl.bookingNumber }}</small>
              </td>
              <td>
                <div><strong>{{ pl.eventName || 'Standard Fulfillment' }}</strong></div>
                <small class="text-muted">{{ pl.venueName || 'Main Facility' }}</small>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + pl.priority.toLowerCase()">{{ pl.priority }}</span>
              </td>
              <td>
                <span class="status-badge" [ngClass]="'status-' + pl.status.toLowerCase()">{{ pl.status }}</span>
              </td>
              <td>
                <div class="progress-cell">
                  <div class="progress-text">{{ pl.totalPickedItems }} / {{ pl.totalRequiredItems }} ({{ pl.progressPercentage | number:'1.0-0' }}%)</div>
                  <div class="progress-bar-mini">
                    <div class="progress-fill-mini" [style.width.%]="pl.progressPercentage"></div>
                  </div>
                </div>
              </td>
              <td>
                <span class="assigned-user">{{ pl.assignedTo || 'Unassigned' }}</span>
              </td>
              <td>
                <div class="action-buttons">
                  <a [routerLink]="['/warehouse/pick-lists', pl.id, 'mobile']" class="btn-action btn-scan" title="Mobile Scanner">📱 Scan</a>
                  <a [routerLink]="['/warehouse/pick-lists', pl.id]" class="btn-action btn-view" title="Desktop Details">View</a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .pick-lists-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-accent { background: #7c3aed; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #38bdf8; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #2563eb; color: #fff; border-color: #2563eb; font-weight: 600; }

    .table-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.9rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.9rem 1rem; border-bottom: 1px solid #334155; font-size: 0.9rem; }
    .data-table tr:hover { background: rgba(51, 65, 85, 0.4); }

    .code-badge { font-family: monospace; background: rgba(56, 189, 248, 0.15); color: #38bdf8; padding: 0.25rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .text-muted { color: #94a3b8; font-size: 0.8rem; }
    .badge { font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .badge-urgent, .badge-high { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .badge-normal, .badge-low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }

    .status-badge { font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-in_progress { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-completed { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-verified { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .progress-cell { width: 140px; }
    .progress-text { font-size: 0.75rem; color: #cbd5e1; margin-bottom: 0.25rem; }
    .progress-bar-mini { height: 5px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill-mini { height: 100%; background: #3b82f6; }

    .action-buttons { display: flex; gap: 0.4rem; }
    .btn-action { padding: 0.35rem 0.7rem; border-radius: 6px; font-size: 0.8rem; text-decoration: none; font-weight: 600; }
    .btn-scan { background: #2563eb; color: #fff; }
    .btn-view { background: #334155; color: #f8fafc; }

    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #38bdf8; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class WarehousePickListsComponent implements OnInit {
  pickLists: PickList[] = [];
  filteredPickLists: PickList[] = [];
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
      this.loadPickLists();
    });
  }

  loadPickLists(): void {
    this.loading = true;
    this.fulfillmentService.getPickLists().subscribe({
      next: (lists) => {
        this.pickLists = lists;
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
    this.filteredPickLists = this.pickLists.filter(pl => {
      const matchStatus = this.selectedStatus === 'ALL' || pl.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        pl.pickListNumber.toLowerCase().includes(term) ||
        (pl.warehouseOrderNumber && pl.warehouseOrderNumber.toLowerCase().includes(term)) ||
        (pl.bookingNumber && pl.bookingNumber.toLowerCase().includes(term)) ||
        (pl.customerName && pl.customerName.toLowerCase().includes(term)) ||
        (pl.eventName && pl.eventName.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }
}
