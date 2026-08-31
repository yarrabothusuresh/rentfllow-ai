import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, WarehouseSubstitution } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-substitutions',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="substitutions-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Operations Center</a>
          <h2>🔄 Controlled Warehouse Substitutions</h2>
          <p class="subtitle">Manager approval queue for inventory replacements, stock swaps, and price adjustments.</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-primary" (click)="openProposeModal()">+ Propose Substitution</button>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search order #, booking #, product..." (input)="filterSubs()" />
        </div>
        <div class="status-filters">
          <button class="filter-btn" [class.active]="selectedStatus === 'ALL'" (click)="setStatusFilter('ALL')">All</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'PROPOSED'" (click)="setStatusFilter('PROPOSED')">⏳ Awaiting Approval</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'APPROVED'" (click)="setStatusFilter('APPROVED')">✓ Approved</button>
          <button class="filter-btn" [class.active]="selectedStatus === 'REJECTED'" (click)="setStatusFilter('REJECTED')">✕ Rejected</button>
        </div>
      </div>

      <!-- Substitution Cards -->
      <div class="subs-grid">
        <div class="loading-state" *ngIf="loading">
          <div class="spinner"></div>
          <p>Loading substitutions...</p>
        </div>

        <div class="empty-state" *ngIf="!loading && filteredSubs.length === 0">
          <p>No substitutions found matching criteria.</p>
        </div>

        <div class="sub-card" *ngFor="let sub of filteredSubs">
          <div class="sub-card-header">
            <div class="order-tags">
              <span class="code-badge">{{ sub.warehouseOrderNumber }}</span>
              <span class="booking-tag">{{ sub.bookingNumber }}</span>
            </div>
            <span class="status-badge" [ngClass]="'status-' + sub.status.toLowerCase()">{{ sub.status }}</span>
          </div>

          <div class="comparison-grid">
            <!-- Original Item -->
            <div class="compare-col original">
              <span class="col-label">ORIGINAL (SHORT)</span>
              <h4 class="prod-title">{{ sub.originalProductName }}</h4>
              <p class="sku-text">SKU: {{ sub.originalProductSku }}</p>
              <div class="qty-pill text-danger">Qty: {{ sub.originalQuantity }}</div>
              <div class="price-text" *ngIf="sub.originalPrice">\${{ sub.originalPrice | number:'1.2-2' }}/day</div>
            </div>

            <div class="compare-arrow">➔</div>

            <!-- Replacement Item -->
            <div class="compare-col replacement">
              <span class="col-label">REPLACEMENT</span>
              <h4 class="prod-title">{{ sub.replacementProductName }}</h4>
              <p class="sku-text">SKU: {{ sub.replacementProductSku }}</p>
              <div class="qty-pill text-success">Qty: {{ sub.replacementQuantity }}</div>
              <div class="stock-check">
                <span class="stock-tag">✓ {{ sub.replacementAvailableStock }} In Stock</span>
              </div>
              <div class="price-text" *ngIf="sub.replacementPrice">\${{ sub.replacementPrice | number:'1.2-2' }}/day</div>
            </div>
          </div>

          <div class="sub-reason" *ngIf="sub.reason">
            <strong>Reason:</strong> {{ sub.reason }}
          </div>

          <div class="price-diff-bar" *ngIf="sub.priceDifference !== undefined">
            <span>Price Delta per unit:</span>
            <strong [class.text-warning]="sub.priceDifference > 0" [class.text-green]="sub.priceDifference <= 0">
              {{ sub.priceDifference >= 0 ? '+' : '' }}\${{ sub.priceDifference | number:'1.2-2' }}
            </strong>
          </div>

          <div class="sub-meta">
            <small class="text-muted">Proposed by {{ sub.proposedBy }} on {{ sub.proposedAt | date:'short' }}</small>
            <small class="text-muted" *ngIf="sub.approvedBy">Approved by {{ sub.approvedBy }} on {{ sub.approvedAt | date:'short' }}</small>
          </div>

          <!-- Actions -->
          <div class="sub-card-actions" *ngIf="sub.status === 'PROPOSED'">
            <button
              class="btn-sm btn-reject"
              (click)="openRejectModal(sub)"
              *ngIf="roleService.currentRole() === 'ADMIN' || roleService.currentRole() === 'OPERATIONS_MANAGER' || roleService.currentRole() === 'WAREHOUSE_MANAGER'"
            >
              ✕ Reject
            </button>
            <button
              class="btn-sm btn-approve"
              (click)="approveSub(sub)"
              *ngIf="roleService.currentRole() === 'ADMIN' || roleService.currentRole() === 'OPERATIONS_MANAGER' || roleService.currentRole() === 'WAREHOUSE_MANAGER'"
            >
              ✓ Approve Substitution
            </button>
          </div>
        </div>
      </div>

      <!-- Reject Modal -->
      <div class="modal-backdrop" *ngIf="showRejectModal && activeSub">
        <div class="modal-card">
          <h3>✕ Reject Substitution</h3>
          <p>Order: <strong>{{ activeSub.warehouseOrderNumber }}</strong></p>

          <label>Rejection Reason:</label>
          <textarea [(ngModel)]="rejectReason" class="form-input" placeholder="Explain why substitution cannot be accepted..."></textarea>

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showRejectModal = false">Cancel</button>
            <button class="btn-confirm-reject" (click)="submitReject()">Confirm Rejection</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .substitutions-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .btn { padding: 0.55rem 1.1rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-primary { background: #2563eb; color: #fff; }

    .filter-bar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
    .search-box input { background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.6rem 1rem; color: #f8fafc; font-size: 0.9rem; width: 320px; }
    .search-box input:focus { outline: none; border-color: #f59e0b; }
    .status-filters { display: flex; gap: 0.4rem; flex-wrap: wrap; }
    .filter-btn { background: #1e293b; border: 1px solid #334155; color: #94a3b8; padding: 0.45rem 0.9rem; border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 500; }
    .filter-btn.active { background: #d97706; color: #fff; border-color: #d97706; font-weight: 600; }

    .subs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 1.25rem; }
    .sub-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; padding: 1.25rem; display: flex; flex-direction: column; }
    .sub-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .order-tags { display: flex; gap: 0.5rem; align-items: center; }
    .code-badge { font-family: monospace; background: rgba(56, 189, 248, 0.15); color: #38bdf8; padding: 0.2rem 0.4rem; border-radius: 4px; font-weight: 600; font-size: 0.8rem; }
    .booking-tag { font-size: 0.8rem; color: #94a3b8; }

    .status-badge { font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-proposed { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-approved { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-rejected { background: rgba(239, 68, 68, 0.2); color: #f87171; }

    .comparison-grid { display: flex; align-items: center; gap: 0.75rem; background: #0f172a; border-radius: 8px; padding: 1rem; border: 1px solid #334155; margin-bottom: 0.75rem; }
    .compare-col { flex: 1; }
    .col-label { font-size: 0.65rem; font-weight: 700; color: #94a3b8; letter-spacing: 0.05em; display: block; margin-bottom: 0.25rem; }
    .prod-title { margin: 0 0 0.2rem 0; font-size: 0.95rem; color: #f8fafc; font-weight: 600; }
    .sku-text { margin: 0 0 0.4rem 0; font-size: 0.75rem; color: #94a3b8; }
    .compare-arrow { font-size: 1.2rem; color: #38bdf8; font-weight: 700; }

    .qty-pill { font-size: 0.8rem; font-weight: 700; display: inline-block; padding: 0.15rem 0.4rem; border-radius: 4px; background: rgba(255,255,255,0.05); }
    .stock-tag { font-size: 0.75rem; color: #4ade80; font-weight: 600; }
    .price-text { font-size: 0.75rem; color: #cbd5e1; margin-top: 0.25rem; }

    .sub-reason { background: rgba(255,255,255,0.03); border-radius: 6px; padding: 0.5rem; font-size: 0.8rem; color: #cbd5e1; margin-bottom: 0.5rem; }
    .price-diff-bar { display: flex; justify-content: space-between; font-size: 0.8rem; padding: 0.4rem 0.5rem; border-top: 1px solid #334155; border-bottom: 1px solid #334155; margin-bottom: 0.5rem; }
    .sub-meta { display: flex; flex-direction: column; gap: 0.2rem; margin-top: auto; padding-top: 0.5rem; font-size: 0.75rem; color: #94a3b8; }

    .sub-card-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-top: 0.75rem; }
    .btn-sm { padding: 0.45rem; border-radius: 6px; font-size: 0.8rem; font-weight: 600; border: none; cursor: pointer; }
    .btn-reject { background: #334155; color: #f87171; }
    .btn-approve { background: #10b981; color: #fff; }

    .text-danger { color: #f87171; }
    .text-success { color: #4ade80; }
    .text-warning { color: #fbbf24; }
    .text-green { color: #4ade80; }
    .text-muted { color: #94a3b8; }

    .loading-state, .empty-state { padding: 3rem; text-align: center; color: #94a3b8; grid-column: 1 / -1; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #f59e0b; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 0.5rem; }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* Modal */
    .modal-backdrop { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { background: #1e293b; border-radius: 12px; border: 1px solid #475569; width: 100%; max-width: 480px; padding: 1.5rem; }
    .modal-card h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; }
    .modal-card label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    .modal-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1.25rem; }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-reject { background: #dc2626; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class WarehouseSubstitutionsComponent implements OnInit {
  substitutions: WarehouseSubstitution[] = [];
  filteredSubs: WarehouseSubstitution[] = [];
  selectedStatus: string = 'ALL';
  searchTerm: string = '';
  loading = true;

  showRejectModal = false;
  activeSub: WarehouseSubstitution | null = null;
  rejectReason = '';

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadSubstitutions();
  }

  loadSubstitutions(): void {
    this.loading = true;
    this.fulfillmentService.getSubstitutions().subscribe({
      next: (subs) => {
        this.substitutions = subs;
        this.filterSubs();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  setStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.filterSubs();
  }

  filterSubs(): void {
    this.filteredSubs = this.substitutions.filter(s => {
      const matchStatus = this.selectedStatus === 'ALL' || s.status === this.selectedStatus;
      const term = this.searchTerm.toLowerCase().trim();
      const matchSearch = !term ||
        (s.warehouseOrderNumber && s.warehouseOrderNumber.toLowerCase().includes(term)) ||
        (s.bookingNumber && s.bookingNumber.toLowerCase().includes(term)) ||
        (s.originalProductName && s.originalProductName.toLowerCase().includes(term)) ||
        (s.replacementProductName && s.replacementProductName.toLowerCase().includes(term));

      return matchStatus && matchSearch;
    });
  }

  approveSub(sub: WarehouseSubstitution): void {
    this.fulfillmentService.approveSubstitution(sub.id).subscribe({
      next: () => {
        alert(`Substitution for order ${sub.warehouseOrderNumber} approved!`);
        this.loadSubstitutions();
      },
      error: (err) => {
        alert(err.error?.message || 'Error approving substitution');
      }
    });
  }

  openRejectModal(sub: WarehouseSubstitution): void {
    this.activeSub = sub;
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  submitReject(): void {
    if (!this.activeSub || !this.rejectReason) return;
    this.fulfillmentService.rejectSubstitution(this.activeSub.id, this.rejectReason).subscribe({
      next: () => {
        this.showRejectModal = false;
        alert('Substitution rejected.');
        this.loadSubstitutions();
      },
      error: (err) => {
        alert(err.error?.message || 'Error rejecting substitution');
      }
    });
  }

  openProposeModal(): void {
    alert('Substitutions are automatically proposed directly during picking shortages in the Pick List / Mobile view.');
  }
}
