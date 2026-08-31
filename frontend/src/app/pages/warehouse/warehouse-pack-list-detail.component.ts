import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PackList, PackListItem, PackingContainer, WarehouseChecklist } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-pack-list-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="pack-detail-container animate-fade-in" *ngIf="packList">
      <!-- Page Header -->
      <div class="page-header">
        <div>
          <a routerLink="/warehouse/pack-lists" class="back-link">← Back to Pack Lists</a>
          <div class="title-row">
            <h2>📦 Pack & Kit List {{ packList.packListNumber }}</h2>
            <span class="status-badge" [ngClass]="'status-' + packList.status.toLowerCase()">{{ packList.status }}</span>
          </div>
          <p class="subtitle">
            Order: <strong>{{ packList.warehouseOrderNumber }}</strong> |
            Booking: <strong>{{ packList.bookingNumber }}</strong> |
            Assigned: <strong>{{ packList.assignedTo || 'Unassigned' }}</strong>
          </p>
        </div>
        <div class="header-actions">
          <button
            class="btn btn-primary"
            *ngIf="packList.status === 'PENDING'"
            (click)="startPacking()"
          >
            ▶ Start Packing
          </button>
          <button
            class="btn btn-purple"
            *ngIf="packList.status === 'IN_PROGRESS'"
            [disabled]="packList.totalPackedItems === 0"
            (click)="completePacking()"
          >
            ✓ Complete Packing
          </button>
          <button
            class="btn btn-success"
            *ngIf="packList.status === 'COMPLETED' && (roleService.currentRole() === 'ADMIN' || roleService.currentRole() === 'OPERATIONS_MANAGER' || roleService.currentRole() === 'WAREHOUSE_MANAGER')"
            (click)="verifyPacking()"
          >
            ✓ Verify Packing (Manager)
          </button>
          <button
            class="btn btn-green"
            *ngIf="packList.status === 'VERIFIED'"
            (click)="generateLoadList()"
          >
            🚚 Proceed to Load Stage
          </button>
        </div>
      </div>

      <!-- Overview Cards -->
      <div class="info-card-grid">
        <div class="info-card">
          <span class="card-label">Event & Customer</span>
          <h4>{{ packList.eventName || 'Event Rental Package' }}</h4>
          <p>Customer: {{ packList.customerName || 'Direct Client' }}</p>
        </div>

        <div class="info-card">
          <span class="card-label">Containers Utilized</span>
          <h4>{{ packList.containerCount }} Container(s)</h4>
          <div class="container-tags" *ngIf="packList.containers && packList.containers.length > 0">
            <span class="cont-pill" *ngFor="let c of packList.containers">{{ c }}</span>
          </div>
          <p *ngIf="!packList.containers || packList.containers.length === 0" class="text-muted">No containers assigned yet.</p>
        </div>

        <div class="info-card">
          <span class="card-label">Packing Progress</span>
          <div class="summary-metric">
            <span class="summary-val">{{ packList.totalPackedItems }} / {{ packList.totalRequiredItems }}</span>
            <span class="summary-pct">({{ packList.progressPercentage | number:'1.0-0' }}% Packed)</span>
          </div>
          <div class="progress-bar-lg">
            <div class="progress-fill-purple" [style.width.%]="packList.progressPercentage"></div>
          </div>
        </div>
      </div>

      <!-- Packing Items Table with Container Assignment -->
      <div class="content-card">
        <div class="card-header">
          <h3>📦 Items & Container Packing</h3>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Item / Package</th>
              <th>Kit Type</th>
              <th>Required</th>
              <th>Packed</th>
              <th>Remaining</th>
              <th>Container Code</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of packList.items">
              <td>
                <div><strong>{{ item.productNameSnapshot }}</strong></div>
                <small class="text-muted">{{ item.skuSnapshot }}</small>
              </td>
              <td>
                <span class="badge" [class.badge-kit]="item.isKitPackage" [class.badge-standard]="!item.isKitPackage">
                  {{ item.isKitPackage ? 'COMPOSITE KIT' : 'STANDARD' }}
                </span>
              </td>
              <td>{{ item.requiredQuantity }}</td>
              <td><strong class="text-purple">{{ item.packedQuantity }}</strong></td>
              <td><strong class="text-warning">{{ item.remainingQuantity }}</strong></td>
              <td>
                <span class="cont-code-badge" *ngIf="item.containerCode">🧰 {{ item.containerCode }}</span>
                <span class="text-muted" *ngIf="!item.containerCode">Unassigned</span>
              </td>
              <td>
                <span class="item-status-badge" [ngClass]="'status-' + item.status.toLowerCase()">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button
                  class="btn-sm btn-pack-action"
                  *ngIf="item.remainingQuantity > 0 && packList.status === 'IN_PROGRESS'"
                  (click)="openPackItemModal(item)"
                >
                  Pack ➔
                </button>
                <span class="text-success" *ngIf="item.status === 'PACKED'">✓ Done</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pack Operational Checklists -->
      <div class="content-card" *ngIf="checklists.length > 0">
        <div class="card-header">
          <h3>📋 Packing Quality & Kit Verification Checklist</h3>
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

      <!-- Pack Item Modal -->
      <div class="modal-backdrop" *ngIf="showPackModal && activeItem">
        <div class="modal-card">
          <h3>📦 Pack Item into Container</h3>
          <p>Product: <strong>{{ activeItem.productNameSnapshot }}</strong></p>

          <label>Quantity to Pack:</label>
          <input type="number" [(ngModel)]="packQuantity" class="form-input" min="1" [max]="activeItem.remainingQuantity" />

          <label>Select Transport Container / Pallet:</label>
          <select [(ngModel)]="selectedContainerCode" class="form-input">
            <option value="">No Container / Loose Staging</option>
            <option *ngFor="let c of availableContainers" [value]="c.containerCode">
              {{ c.containerCode }} ({{ c.containerType }}) - {{ c.notes || 'Available' }}
            </option>
          </select>

          <label>Packing Notes / Seal Tags:</label>
          <input type="text" [(ngModel)]="packNotes" class="form-input" placeholder="e.g. Foam padded, seal #4820" />

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showPackModal = false">Cancel</button>
            <button class="btn-confirm-pack" (click)="submitPackItem()">Confirm Pack</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .pack-detail-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
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
    .btn-green { background: #16a34a; color: #fff; }

    .status-badge { font-size: 0.8rem; padding: 0.25rem 0.6rem; border-radius: 9999px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-in_progress { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-completed { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-verified { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .info-card-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .info-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .card-label { font-size: 0.75rem; color: #94a3b8; text-transform: uppercase; font-weight: 600; display: block; margin-bottom: 0.35rem; }
    .info-card h4 { margin: 0 0 0.4rem 0; font-size: 1.1rem; color: #f8fafc; }
    .info-card p { margin: 0 0 0.2rem 0; font-size: 0.85rem; color: #cbd5e1; }

    .container-tags { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-top: 0.35rem; }
    .cont-pill { font-family: monospace; font-size: 0.75rem; background: rgba(20, 184, 166, 0.2); color: #2dd4bf; padding: 0.2rem 0.5rem; border-radius: 4px; }

    .summary-metric { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.5rem; }
    .summary-val { font-size: 1.3rem; font-weight: 700; color: #c084fc; }
    .summary-pct { font-size: 0.85rem; color: #94a3b8; }
    .progress-bar-lg { height: 8px; background: #334155; border-radius: 4px; overflow: hidden; }
    .progress-fill-purple { height: 100%; background: #a855f7; }

    .content-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; margin-bottom: 1.5rem; overflow: hidden; }
    .card-header { padding: 1rem 1.25rem; border-bottom: 1px solid #334155; background: #0f172a; }
    .card-header h3 { margin: 0; font-size: 1.1rem; }

    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.8rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.8rem 1rem; border-bottom: 1px solid #334155; font-size: 0.85rem; }

    .badge { font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .badge-kit { background: rgba(168, 85, 247, 0.2); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.4); }
    .badge-standard { background: #334155; color: #cbd5e1; }
    .badge-mandatory { background: rgba(239, 68, 68, 0.2); color: #f87171; font-size: 0.7rem; }

    .cont-code-badge { font-family: monospace; font-weight: 700; background: rgba(20, 184, 166, 0.2); color: #2dd4bf; padding: 0.2rem 0.4rem; border-radius: 4px; }
    .item-status-badge { font-size: 0.75rem; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-partial { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-packed { background: rgba(34, 197, 94, 0.2); color: #4ade80; }

    .btn-pack-action { background: #7c3aed; color: #fff; border: none; padding: 0.35rem 0.75rem; border-radius: 6px; font-weight: 600; cursor: pointer; }
    .checklist-items { padding: 1rem 1.25rem; display: flex; flex-direction: column; gap: 0.75rem; }
    .check-row { display: flex; align-items: center; gap: 0.75rem; background: #0f172a; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid #334155; }
    .check-row input { width: 18px; height: 18px; cursor: pointer; }
    .check-info { flex: 1; display: flex; flex-direction: column; }
    .check-desc { font-size: 0.9rem; color: #f8fafc; font-weight: 500; }
    .checked-text { text-decoration: line-through; color: #94a3b8; }
    .check-meta { font-size: 0.75rem; color: #64748b; }
    .text-purple { color: #c084fc; }
    .text-warning { color: #f59e0b; }
    .text-success { color: #4ade80; font-weight: 600; }
    .text-muted { color: #94a3b8; }

    /* Modal */
    .modal-backdrop { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { background: #1e293b; border-radius: 12px; border: 1px solid #475569; width: 100%; max-width: 480px; padding: 1.5rem; }
    .modal-card h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; }
    .modal-card label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    .modal-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1.25rem; }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-pack { background: #7c3aed; border: none; color: #fff; padding: 0.7rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class WarehousePackListDetailComponent implements OnInit {
  packList: PackList | null = null;
  checklists: WarehouseChecklist[] = [];
  availableContainers: PackingContainer[] = [];
  loading = true;

  showPackModal = false;
  activeItem: PackListItem | null = null;
  packQuantity = 1;
  selectedContainerCode = '';
  packNotes = '';

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    private route: ActivatedRoute,
    private router: Router,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPackList(id);
      this.loadContainers();
    }
  }

  loadPackList(id?: string): void {
    const pklId = id || (this.packList ? this.packList.id : this.route.snapshot.paramMap.get('id'));
    if (!pklId) return;

    this.loading = true;
    this.fulfillmentService.getPackList(pklId).subscribe({
      next: (pkl) => {
        this.packList = pkl;
        this.loadChecklists(pkl.warehouseOrderId);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadContainers(): void {
    this.fulfillmentService.getContainers().subscribe({
      next: (conts) => {
        this.availableContainers = conts;
      }
    });
  }

  loadChecklists(orderId: string): void {
    this.fulfillmentService.getOrderChecklists(orderId).subscribe({
      next: (cls) => {
        this.checklists = cls.filter(c => c.stage === 'PACK');
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

  startPacking(): void {
    if (!this.packList) return;
    this.fulfillmentService.startPacking(this.packList.id).subscribe({
      next: (pkl) => {
        this.packList = pkl;
      }
    });
  }

  openPackItemModal(item: PackListItem): void {
    this.activeItem = item;
    this.packQuantity = item.remainingQuantity;
    this.selectedContainerCode = '';
    this.packNotes = '';
    this.showPackModal = true;
  }

  submitPackItem(): void {
    if (!this.packList || !this.activeItem) return;
    this.fulfillmentService.packItem(
      this.packList.id,
      this.activeItem.id,
      this.packQuantity,
      this.selectedContainerCode || undefined,
      this.packNotes || undefined
    ).subscribe({
      next: (res) => {
        this.showPackModal = false;
        this.packList = res;
      },
      error: (err) => {
        alert(err.error?.message || 'Error packing item');
      }
    });
  }

  completePacking(): void {
    if (!this.packList) return;
    this.fulfillmentService.completePackList(this.packList.id).subscribe({
      next: (res) => {
        alert(`Pack List ${res.packListNumber} completed! Ready for manager verification.`);
        this.packList = res;
      },
      error: (err) => {
        alert(err.error?.message || 'Cannot complete pack list.');
      }
    });
  }

  verifyPacking(): void {
    if (!this.packList) return;
    this.fulfillmentService.verifyPackList(this.packList.id).subscribe({
      next: (res) => {
        alert(`Pack List ${res.packListNumber} verified! Ready for vehicle loading.`);
        this.packList = res;
      },
      error: (err) => {
        alert(err.error?.message || 'Pack verification failed. Check kit component completeness.');
      }
    });
  }

  generateLoadList(): void {
    if (!this.packList) return;
    this.fulfillmentService.generateLoadList(this.packList.warehouseOrderId).subscribe({
      next: (ld) => {
        this.router.navigate(['/warehouse/load-lists', ld.id]);
      },
      error: (err) => {
        alert(err.error?.message || 'Error generating load list');
      }
    });
  }
}
