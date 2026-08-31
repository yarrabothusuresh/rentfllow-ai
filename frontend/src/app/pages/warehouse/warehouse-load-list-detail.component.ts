import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, LoadList, LoadListItem, WarehouseChecklist } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-load-list-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="load-detail-container animate-fade-in" *ngIf="loadList">
      <!-- Page Header -->
      <div class="page-header">
        <div>
          <a routerLink="/warehouse/load-lists" class="back-link">← Back to Load Lists</a>
          <div class="title-row">
            <h2>🚚 Load List {{ loadList.loadListNumber }}</h2>
            <span class="status-badge" [ngClass]="'status-' + loadList.status.toLowerCase()">{{ loadList.status }}</span>
          </div>
          <p class="subtitle">
            Order: <strong>{{ loadList.warehouseOrderNumber }}</strong> |
            Delivery: <strong>{{ loadList.deliveryNumber || 'Scheduled' }}</strong> |
            Vehicle: <strong>{{ loadList.vehicleCodeSnapshot || loadList.vehicleName || 'Unassigned' }}</strong> |
            Driver: <strong>{{ loadList.driverNameSnapshot || 'Unassigned' }}</strong>
          </p>
        </div>
        <div class="header-actions">
          <button
            class="btn btn-primary"
            *ngIf="loadList.status === 'PENDING'"
            (click)="startLoading()"
          >
            ▶ Start Loading
          </button>
          <button
            class="btn btn-purple"
            *ngIf="loadList.status === 'LOADED' && (roleService.currentRole() === 'ADMIN' || roleService.currentRole() === 'OPERATIONS_MANAGER' || roleService.currentRole() === 'WAREHOUSE_MANAGER')"
            (click)="verifyLoad()"
          >
            ✓ Verify Load (Manager)
          </button>
          <button
            class="btn btn-success btn-lg"
            *ngIf="loadList.status === 'VERIFIED'"
            (click)="openHandoffModal()"
          >
            🤝 Driver Handoff Sign-off
          </button>
        </div>
      </div>

      <!-- Overload Warning Banner -->
      <div class="alert-box alert-danger" *ngIf="loadList.capacityWarning">
        <span class="alert-icon">⚠️</span>
        <div class="alert-content">
          <h4>Vehicle Payload Overload Warning</h4>
          <p>{{ loadList.capacityWarningMessage || 'Estimated load weight exceeds vehicle capacity!' }}</p>
        </div>
      </div>

      <!-- Overview Cards -->
      <div class="info-card-grid">
        <div class="info-card">
          <span class="card-label">Vehicle & Dispatch</span>
          <h4>{{ loadList.vehicleCodeSnapshot || loadList.vehicleName || 'Standard Truck' }}</h4>
          <p>Driver: <strong>{{ loadList.driverNameSnapshot || 'Unassigned' }}</strong></p>
          <p *ngIf="loadList.deliveryNumber">Delivery #: {{ loadList.deliveryNumber }}</p>
        </div>

        <div class="info-card">
          <span class="card-label">Payload Capacity Check</span>
          <h4>{{ loadList.estimatedLoadWeightKg | number:'1.0-0' }} kg / {{ loadList.vehicleCapacityWeightKg | number:'1.0-0' }} kg</h4>
          <div class="progress-bar-lg">
            <div
              class="progress-fill-lg"
              [class.fill-danger]="loadList.capacityWarning"
              [style.width.%]="(loadList.estimatedLoadWeightKg * 100 / loadList.vehicleCapacityWeightKg)"
            ></div>
          </div>
          <small class="text-muted">Estimated weight based on standard rental gear density.</small>
        </div>

        <div class="info-card">
          <span class="card-label">Loading Status</span>
          <div class="summary-metric">
            <span class="summary-val">{{ loadList.loadedItems }} / {{ loadList.totalItems }}</span>
            <span class="summary-pct">({{ loadList.progressPercentage | number:'1.0-0' }}% Loaded)</span>
          </div>
          <div class="progress-bar-lg">
            <div class="progress-fill-green" [style.width.%]="loadList.progressPercentage"></div>
          </div>
        </div>
      </div>

      <!-- Loading Items & Containers Table -->
      <div class="content-card">
        <div class="card-header">
          <h3>📦 Items & Container Load Manifest</h3>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Item / Cargo</th>
              <th>Container / Pallet</th>
              <th>Required</th>
              <th>Loaded</th>
              <th>Remaining</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of loadList.items">
              <td>
                <div><strong>{{ item.productNameSnapshot }}</strong></div>
                <small class="text-muted">{{ item.skuSnapshot }}</small>
              </td>
              <td>
                <span class="cont-code-badge" *ngIf="item.containerCodeSnapshot">🧰 {{ item.containerCodeSnapshot }}</span>
                <span class="text-muted" *ngIf="!item.containerCodeSnapshot">Loose Cargo</span>
              </td>
              <td>{{ item.requiredQuantity }}</td>
              <td><strong class="text-green">{{ item.loadedQuantity }}</strong></td>
              <td><strong class="text-warning">{{ item.remainingQuantity }}</strong></td>
              <td>
                <span class="item-status-badge" [ngClass]="'status-' + item.status.toLowerCase()">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button
                  class="btn-sm btn-load-action"
                  *ngIf="item.remainingQuantity > 0 && (loadList.status === 'PENDING' || loadList.status === 'LOADING')"
                  (click)="loadItem(item)"
                >
                  Load into Vehicle ➔
                </button>
                <span class="text-success" *ngIf="item.status === 'LOADED'">✓ On Vehicle</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Load Operational Checklists -->
      <div class="content-card" *ngIf="checklists.length > 0">
        <div class="card-header">
          <h3>📋 Vehicle Staging & Tie-Down Safety Checklist</h3>
        </div>
        <div class="checklist-items">
          <div class="check-row" *ngFor="let cl of checklists">
            <input
              type="checkbox"
              [checked]="cl.completed"
              (change)="toggleChecklist(cl)"
            />
            <div class="check-info">
              <span class="check-desc" [class.checked-text]="cl.completed">{{ cl.taskDescription }}</span>
              <small class="check-meta" *ngIf="cl.completed">Completed by {{ cl.completedBy }} at {{ cl.completedAt | date:'short' }}</small>
            </div>
            <span class="badge badge-mandatory" *ngIf="cl.mandatory">MANDATORY</span>
          </div>
        </div>
      </div>

      <!-- Driver Handoff Sign-off Section (if completed) -->
      <div class="content-card handoff-complete-card" *ngIf="loadList.status === 'HANDED_OFF'">
        <div class="handoff-header">
          <span class="handoff-icon">🤝</span>
          <div>
            <h3>Driver Handoff Complete & Verified</h3>
            <p class="subtitle">Driver has received, inspected, and accepted custody of all loaded cargo.</p>
          </div>
        </div>
        <div class="handoff-details">
          <p><strong>Signed by Driver:</strong> {{ loadList.driverNameSnapshot || 'Driver' }}</p>
          <p><strong>Handoff Timestamp:</strong> {{ loadList.driverHandoffAt | date:'medium' }}</p>
          <p *ngIf="loadList.driverNotes"><strong>Driver Sign-off Notes:</strong> {{ loadList.driverNotes }}</p>
        </div>
      </div>

      <!-- Driver Handoff Modal -->
      <div class="modal-backdrop" *ngIf="showHandoffModal">
        <div class="modal-card">
          <h3>🤝 Driver Custody Sign-off & Handoff</h3>
          <p>Order: <strong>{{ loadList.warehouseOrderNumber }}</strong> | Total Items: <strong>{{ loadList.totalItems }}</strong></p>

          <label>Driver Full Name (Signature):</label>
          <input type="text" [(ngModel)]="driverName" class="form-input" placeholder="e.g. John Smith" />

          <label>Driver Receipt Notes & Inspection Confirmation:</label>
          <textarea [(ngModel)]="driverNotes" class="form-input" placeholder="All items accounted for and secured with ratchet straps. Ready for delivery route."></textarea>

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showHandoffModal = false">Cancel</button>
            <button class="btn-confirm-handoff" (click)="submitHandoff()">Confirm Driver Custody & Unblock Delivery</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .load-detail-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .title-row { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .title-row h2 { margin: 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .header-actions { display: flex; gap: 0.75rem; flex-wrap: wrap; }

    .btn { padding: 0.6rem 1.2rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-primary { background: #2563eb; color: #fff; }
    .btn-purple { background: #7c3aed; color: #fff; }
    .btn-success { background: #10b981; color: #fff; }
    .btn-lg { padding: 0.75rem 1.5rem; font-size: 0.95rem; }

    .alert-box { display: flex; align-items: center; gap: 1rem; padding: 1rem 1.25rem; border-radius: 10px; margin-bottom: 1.5rem; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.4); }
    .alert-icon { font-size: 1.8rem; }
    .alert-content h4 { margin: 0 0 0.2rem 0; font-size: 1rem; color: #fca5a5; }
    .alert-content p { margin: 0; font-size: 0.85rem; color: #cbd5e1; }

    .status-badge { font-size: 0.8rem; padding: 0.25rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-loading { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-loaded { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-verified { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-handed_off { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .info-card-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .info-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .card-label { font-size: 0.75rem; color: #94a3b8; text-transform: uppercase; font-weight: 600; display: block; margin-bottom: 0.35rem; }
    .info-card h4 { margin: 0 0 0.4rem 0; font-size: 1.1rem; color: #f8fafc; }
    .info-card p { margin: 0 0 0.2rem 0; font-size: 0.85rem; color: #cbd5e1; }

    .summary-metric { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.5rem; }
    .summary-val { font-size: 1.3rem; font-weight: 700; color: #4ade80; }
    .summary-pct { font-size: 0.85rem; color: #94a3b8; }
    .progress-bar-lg { height: 8px; background: #334155; border-radius: 4px; overflow: hidden; margin-top: 0.4rem; }
    .progress-fill-lg { height: 100%; background: #3b82f6; }
    .progress-fill-lg.fill-danger { background: #ef4444 !important; }
    .progress-fill-green { height: 100%; background: #10b981; }

    .content-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; margin-bottom: 1.5rem; overflow: hidden; }
    .card-header { padding: 1rem 1.25rem; border-bottom: 1px solid #334155; background: #0f172a; }
    .card-header h3 { margin: 0; font-size: 1.1rem; }

    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.8rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.8rem 1rem; border-bottom: 1px solid #334155; font-size: 0.85rem; }

    .cont-code-badge { font-family: monospace; font-weight: 700; background: rgba(20, 184, 166, 0.2); color: #2dd4bf; padding: 0.2rem 0.4rem; border-radius: 4px; }
    .item-status-badge { font-size: 0.75rem; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-partial { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-loaded { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .btn-load-action { background: #10b981; color: #fff; border: none; padding: 0.35rem 0.75rem; border-radius: 6px; font-weight: 600; cursor: pointer; }
    .checklist-items { padding: 1rem 1.25rem; display: flex; flex-direction: column; gap: 0.75rem; }
    .check-row { display: flex; align-items: center; gap: 0.75rem; background: #0f172a; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid #334155; }
    .check-row input { width: 18px; height: 18px; cursor: pointer; }
    .check-info { flex: 1; display: flex; flex-direction: column; }
    .check-desc { font-size: 0.9rem; color: #f8fafc; font-weight: 500; }
    .checked-text { text-decoration: line-through; color: #94a3b8; }
    .check-meta { font-size: 0.75rem; color: #64748b; }
    .badge-mandatory { background: rgba(239, 68, 68, 0.2); color: #f87171; font-size: 0.7rem; }

    .handoff-complete-card { background: rgba(34, 197, 94, 0.1); border: 1px solid rgba(34, 197, 94, 0.4); padding: 1.5rem; }
    .handoff-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1rem; }
    .handoff-icon { font-size: 2.2rem; }
    .handoff-details p { margin: 0 0 0.35rem 0; font-size: 0.9rem; color: #cbd5e1; }

    .text-green { color: #4ade80; }
    .text-warning { color: #f59e0b; }
    .text-success { color: #4ade80; font-weight: 600; }
    .text-muted { color: #94a3b8; }

    /* Modal */
    .modal-backdrop { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { background: #1e293b; border-radius: 12px; border: 1px solid #475569; width: 100%; max-width: 500px; padding: 1.5rem; }
    .modal-card h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; }
    .modal-card label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    .modal-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1.25rem; }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-handoff { background: #10b981; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 0.85rem; }
  `]
})
export class WarehouseLoadListDetailComponent implements OnInit {
  loadList: LoadList | null = null;
  checklists: WarehouseChecklist[] = [];
  loading = true;

  showHandoffModal = false;
  driverName = '';
  driverNotes = '';

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    private route: ActivatedRoute,
    private router: Router,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadDetails(id);
    }
  }

  loadDetails(id?: string): void {
    const ldId = id || (this.loadList ? this.loadList.id : this.route.snapshot.paramMap.get('id'));
    if (!ldId) return;

    this.loading = true;
    this.fulfillmentService.getLoadList(ldId).subscribe({
      next: (ld) => {
        this.loadList = ld;
        this.driverName = ld.driverNameSnapshot || '';
        this.loadChecklists(ld.warehouseOrderId);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadChecklists(orderId: string): void {
    this.fulfillmentService.getOrderChecklists(orderId).subscribe({
      next: (cls) => {
        this.checklists = cls.filter(c => c.stage === 'LOAD');
      }
    });
  }

  toggleChecklist(cl: WarehouseChecklist): void {
    this.fulfillmentService.toggleChecklistItem(cl.id, !cl.completed).subscribe({
      next: (updated) => {
        cl.completed = updated.completed;
        cl.completedBy = updated.completedBy;
        cl.completedAt = updated.completedAt;
      }
    });
  }

  startLoading(): void {
    if (!this.loadList) return;
    this.fulfillmentService.startLoading(this.loadList.id).subscribe({
      next: (ld) => {
        this.loadList = ld;
      }
    });
  }

  loadItem(item: LoadListItem): void {
    if (!this.loadList) return;
    this.fulfillmentService.loadItem(this.loadList.id, item.id, item.remainingQuantity).subscribe({
      next: (ld) => {
        this.loadList = ld;
      },
      error: (err) => {
        alert(err.error?.message || 'Error loading item');
      }
    });
  }

  verifyLoad(): void {
    if (!this.loadList) return;
    this.fulfillmentService.verifyLoad(this.loadList.id).subscribe({
      next: (ld) => {
        alert(`Load list ${ld.loadListNumber} verified! Ready for driver handoff.`);
        this.loadList = ld;
      },
      error: (err) => {
        alert(err.error?.message || 'Load verification failed.');
      }
    });
  }

  openHandoffModal(): void {
    this.showHandoffModal = true;
  }

  submitHandoff(): void {
    if (!this.loadList) return;
    this.fulfillmentService.driverHandoff(
      this.loadList.id,
      this.driverName || undefined,
      this.driverNotes || undefined
    ).subscribe({
      next: (res) => {
        this.showHandoffModal = false;
        alert(`Driver handoff complete! Delivery team is now authorized to begin route.`);
        this.loadList = res;
      },
      error: (err) => {
        alert(err.error?.message || 'Error completing driver handoff.');
      }
    });
  }
}
