import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, WarehouseException } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-exceptions',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="exceptions-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>⚠️ Warehouse Exception & Shortage Center</h2>
          <p class="subtitle">Resolve missing stock, bin location errors, and damaged assets.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/substitutions" class="btn btn-warning">🔄 Substitutions</a>
          <a routerLink="/warehouse/my-work" class="btn btn-accent">👷 My Work</a>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search product, order #, reason..." (input)="filterExceptions()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'OPEN'" (click)="setStatusFilter('OPEN')">🚨 Open (Blocking)</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'RESOLVED'" (click)="setStatusFilter('RESOLVED')">✓ Resolved</button>
        </div>
      </div>

      <!-- Table View -->
      <div class="table-card">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading warehouse exceptions...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredExceptions.length === 0">
          <p>No exceptions found matching criteria. Operations are running smoothly!</p>
        </div>

        <table class="data-table" *ngIf="!loading && filteredExceptions.length > 0">
          <thead>
            <tr>
              <th>Type / Severity</th>
              <th>Order / Booking</th>
              <th>Product / Details</th>
              <th>Affected Qty</th>
              <th>Reported By</th>
              <th>Status / Resolution</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let exc of filteredExceptions">
              <td>
                <div class="badge-stack">
                  <span class="badge" [class.badge-danger]="exc.severity === 'BLOCKING'" [class.badge-warning]="exc.severity === 'WARNING'">
                    {{ exc.severity }}
                  </span>
                  <span class="exc-type-text">{{ exc.exceptionType }}</span>
                </div>
              </td>
              <td>
                <div><strong>{{ exc.warehouseOrderNumber }}</strong></div>
                <small class="text-muted">{{ exc.bookingNumber }}</small>
              </td>
              <td>
                <div><strong>{{ exc.productName || 'Order Exception' }}</strong></div>
                <small class="text-muted">{{ exc.description || exc.reasonCode }}</small>
              </td>
              <td><strong class="text-danger">{{ exc.affectedQuantity }}</strong></td>
              <td>
                <div>{{ exc.reportedBy }}</div>
                <small class="text-muted">{{ exc.reportedAt | date:'short' }}</small>
              </td>
              <td>
                <span class="status-badge" [class.status-open]="exc.status === 'OPEN'" [class.status-resolved]="exc.status === 'RESOLVED'">
                  {{ exc.status }}
                </span>
                <div *ngIf="exc.resolutionType" class="res-type-pill">{{ exc.resolutionType }}</div>
              </td>
              <td>
                <button
                  class="btn-sm btn-resolve"
                  *ngIf="exc.status === 'OPEN'"
                  (click)="openResolveModal(exc)"
                >
                  Resolve ➔
                </button>
                <small class="text-muted" *ngIf="exc.status === 'RESOLVED'">
                  By {{ exc.resolvedBy }} ({{ exc.resolvedAt | date:'shortDate' }})
                </small>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Resolve Modal -->
      <div class="modal-backdrop" *ngIf="showResolveModal && activeExc">
        <div class="modal-card">
          <h3>✓ Resolve Warehouse Exception</h3>
          <p>Order: <strong>{{ activeExc.warehouseOrderNumber }}</strong> | Product: <strong>{{ activeExc.productName }}</strong></p>

          <label>Resolution Strategy:</label>
          <select [(ngModel)]="resolutionType" class="form-input">
            <option value="SUBSTITUTION_APPROVED">Controlled Substitution Approved</option>
            <option value="RESOLVED_FOUND">Missing Items Found in Alternate Bay</option>
            <option value="PARTIAL_SHIP">Customer Agreed to Partial Shipment</option>
            <option value="EMERGENCY_REPAIR">Emergency Same-day Maintenance Fix</option>
            <option value="TRANSFER_REQUESTED">Stock Transfer from Regional Hub</option>
            <option value="DISMISSED">Dismiss (False Alarm / Recounted)</option>
          </select>

          <label>Resolution Audit Notes:</label>
          <textarea [(ngModel)]="resolutionNotes" class="form-input" placeholder="Explain how this issue was reconciled before unblocking fulfillment..."></textarea>

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showResolveModal = false">Cancel</button>
            <button class="btn-confirm-resolve" (click)="submitResolve()">Resolve & Unblock</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .exceptions-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .header-actions { display: flex; gap: 0.75rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-warning { background: #d97706; color: #fff; }
    .btn-accent { background: #7c3aed; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #ef4444; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #dc2626; color: #fff; border-color: #dc2626; font-weight: 600; }

    .table-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.9rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.9rem 1rem; border-bottom: 1px solid #334155; font-size: 0.9rem; }
    .data-table tr:hover { background: rgba(51, 65, 85, 0.4); }

    .badge-stack { display: flex; flex-direction: column; gap: 0.25rem; align-items: flex-start; }
    .badge { font-size: 0.7rem; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 700; }
    .badge-danger { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .badge-warning { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .exc-type-text { font-size: 0.8rem; font-weight: 600; color: #f8fafc; }

    .status-badge { font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-open { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .status-resolved { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .res-type-pill { font-size: 0.7rem; color: #94a3b8; margin-top: 0.2rem; }

    .btn-resolve { background: #dc2626; color: #fff; border: none; padding: 0.4rem 0.8rem; border-radius: 6px; font-weight: 600; cursor: pointer; }
    .text-danger { color: #f87171; }
    .text-muted { color: #94a3b8; font-size: 0.8rem; }

    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #ef4444; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Modal */
    .modal-backdrop { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { background: #1e293b; border-radius: 12px; border: 1px solid #475569; width: 100%; max-width: 500px; padding: 1.5rem; }
    .modal-card h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; }
    .modal-card label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    .modal-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1.25rem; }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-resolve { background: #10b981; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class WarehouseExceptionsComponent implements OnInit {
  exceptions: WarehouseException[] = [];
  filteredExceptions: WarehouseException[] = [];
  selectedStatus: string = 'ALL';
  searchTerm: string = '';
  loading = true;

  showResolveModal = false;
  activeExc: WarehouseException | null = null;
  resolutionType = 'SUBSTITUTION_APPROVED';
  resolutionNotes = '';

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadExceptions();
  }

  loadExceptions(): void {
    this.loading = true;
    this.fulfillmentService.getExceptions().subscribe({
      next: (excs) => {
        this.exceptions = excs;
        this.filterExceptions();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.filterExceptions();
  }

  filterExceptions(): void {
    this.filteredExceptions = this.exceptions.filter(e => {
      const matchStatus = this.selectedStatus === 'ALL' || e.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        e.exceptionType.toLowerCase().includes(term) ||
        (e.warehouseOrderNumber && e.warehouseOrderNumber.toLowerCase().includes(term)) ||
        (e.productName && e.productName.toLowerCase().includes(term)) ||
        (e.description && e.description.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }

  openResolveModal(exc: WarehouseException): void {
    this.activeExc = exc;
    this.resolutionType = 'SUBSTITUTION_APPROVED';
    this.resolutionNotes = '';
    this.showResolveModal = true;
  }

  submitResolve(): void {
    if (!this.activeExc) return;
    this.fulfillmentService.resolveException(
      this.activeExc.id,
      this.resolutionNotes || undefined,
      this.resolutionType
    ).subscribe({
      next: () => {
        this.showResolveModal = false;
        alert('Exception resolved! Unblocking fulfillment pipeline.');
        this.loadExceptions();
      },
      error: (err) => {
        alert(err.error?.message || 'Error resolving exception');
      }
    });
  }
}
