import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PickList, WarehouseChecklist } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-pick-list-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="pick-detail-container animate-fade-in" *ngIf="pickList">
      <!-- Breadcrumbs & Header -->
      <div class="page-header">
        <div>
          <a routerLink="/warehouse/pick-lists" class="back-link">← Back to Pick Lists</a>
          <div class="title-row">
            <h2>🚜 Pick List {{ pickList.pickListNumber }}</h2>
            <span class="status-badge" [ngClass]="'status-' + pickList.status.toLowerCase()">{{ pickList.status }}</span>
            <span class="badge" [ngClass]="'badge-' + pickList.priority.toLowerCase()">{{ pickList.priority }}</span>
          </div>
          <p class="subtitle">
            Order: <strong>{{ pickList.warehouseOrderNumber }}</strong> |
            Booking: <strong>{{ pickList.bookingNumber }}</strong> |
            Assigned: <strong>{{ pickList.assignedTo || 'Unassigned' }}</strong>
          </p>
        </div>
        <div class="header-actions">
          <a [routerLink]="['/warehouse/pick-lists', pickList.id, 'mobile']" class="btn btn-primary">
            📱 Open Mobile Scanner
          </a>
          <button
            class="btn btn-success"
            *ngIf="pickList.status === 'COMPLETED' && (roleService.currentRole() === 'ADMIN' || roleService.currentRole() === 'OPERATIONS_MANAGER' || roleService.currentRole() === 'WAREHOUSE_MANAGER')"
            (click)="verifyPickList()"
          >
            ✓ Verify Pick List (Manager)
          </button>
          <button
            class="btn btn-purple"
            *ngIf="pickList.status === 'VERIFIED'"
            (click)="generatePackList()"
          >
            📦 Proceed to Packing Stage
          </button>
        </div>
      </div>

      <!-- Event & Customer Info Card -->
      <div class="info-card-grid">
        <div class="info-card">
          <span class="card-label">Event Details</span>
          <h4>{{ pickList.eventName || 'Standard Rental Order' }}</h4>
          <p>Venue: {{ pickList.venueName || 'Main Logistics Warehouse' }}</p>
          <p *ngIf="pickList.eventDate">Date: {{ pickList.eventDate }}</p>
        </div>

        <div class="info-card">
          <span class="card-label">Customer Info</span>
          <h4>{{ pickList.customerName || 'Direct Rental Client' }}</h4>
          <p>Warehouse ID: {{ pickList.warehouseId }}</p>
        </div>

        <div class="info-card">
          <span class="card-label">Fulfillment Summary</span>
          <div class="summary-metric">
            <span class="summary-val">{{ pickList.totalPickedItems }} / {{ pickList.totalRequiredItems }}</span>
            <span class="summary-pct">({{ pickList.progressPercentage | number:'1.0-0' }}% Picked)</span>
          </div>
          <div class="progress-bar-lg">
            <div class="progress-fill-lg" [style.width.%]="pickList.progressPercentage"></div>
          </div>
        </div>
      </div>

      <!-- Pick List Items Table in Location Traversal Sequence -->
      <div class="content-card">
        <div class="card-header">
          <h3>📍 Traversal Route Sequence ({{ pickList.items.length }} Stops)</h3>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Seq #</th>
              <th>Bin Location</th>
              <th>Product / SKU</th>
              <th>Tracking</th>
              <th>Required</th>
              <th>Picked</th>
              <th>Remaining</th>
              <th>Status</th>
              <th>Scanned Serials</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of pickList.items">
              <td><strong>#{{ item.sequenceNumber }}</strong></td>
              <td>
                <span class="loc-code-badge">{{ item.locationCodeSnapshot || 'MAIN' }}</span>
              </td>
              <td>
                <div><strong>{{ item.productNameSnapshot }}</strong></div>
                <small class="text-muted">{{ item.skuSnapshot }}</small>
              </td>
              <td>
                <span class="tracking-pill" [class.serial]="item.trackingType === 'SERIALIZED'">
                  {{ item.trackingType }}
                </span>
              </td>
              <td>{{ item.requiredQuantity }}</td>
              <td><strong class="text-info">{{ item.pickedQuantity }}</strong></td>
              <td><strong class="text-warning">{{ item.remainingQuantity }}</strong></td>
              <td>
                <span class="item-status-badge" [ngClass]="'status-' + item.status.toLowerCase()">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <div class="serial-tags" *ngIf="item.scannedAssetCodes && item.scannedAssetCodes.length > 0">
                  <span class="serial-pill" *ngFor="let code of item.scannedAssetCodes">{{ code }}</span>
                </div>
                <span class="text-muted" *ngIf="!item.scannedAssetCodes || item.scannedAssetCodes.length === 0">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pick Operational Checklists -->
      <div class="content-card" *ngIf="checklists.length > 0">
        <div class="card-header">
          <h3>📋 Pick Operational Safety & Quality Checklists</h3>
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
    </div>
  `,
  styles: [`
    .pick-detail-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; color: #f8fafc; }
    .back-link { display: inline-block; color: #38bdf8; font-size: 0.85rem; text-decoration: none; margin-bottom: 0.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem; }
    .title-row { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.25rem; }
    .title-row h2 { margin: 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .header-actions { display: flex; gap: 0.75rem; flex-wrap: wrap; }

    .btn { padding: 0.6rem 1.2rem; border-radius: 8px; font-weight: 600; font-size: 0.85rem; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 0.4rem; border: none; }
    .btn-primary { background: #2563eb; color: #fff; }
    .btn-success { background: #10b981; color: #fff; }
    .btn-purple { background: #7c3aed; color: #fff; }

    .badge { font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; }
    .badge-urgent, .badge-high { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .badge-normal, .badge-low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }
    .badge-mandatory { background: rgba(239, 68, 68, 0.2); color: #f87171; font-size: 0.7rem; }

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
    .summary-metric { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.5rem; }
    .summary-val { font-size: 1.3rem; font-weight: 700; color: #38bdf8; }
    .summary-pct { font-size: 0.85rem; color: #94a3b8; }
    .progress-bar-lg { height: 8px; background: #334155; border-radius: 4px; overflow: hidden; }
    .progress-fill-lg { height: 100%; background: #3b82f6; }

    .content-card { background: #1e293b; border: 1px solid #334155; border-radius: 12px; margin-bottom: 1.5rem; overflow: hidden; }
    .card-header { padding: 1rem 1.25rem; border-bottom: 1px solid #334155; background: #0f172a; }
    .card-header h3 { margin: 0; font-size: 1.1rem; }

    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { background: #0f172a; color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; font-weight: 600; padding: 0.8rem 1rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.8rem 1rem; border-bottom: 1px solid #334155; font-size: 0.85rem; }
    .loc-code-badge { font-family: monospace; font-weight: 700; background: rgba(56, 189, 248, 0.15); color: #38bdf8; padding: 0.25rem 0.5rem; border-radius: 4px; }
    .tracking-pill { font-size: 0.7rem; font-weight: 600; padding: 0.15rem 0.4rem; border-radius: 4px; background: #334155; color: #cbd5e1; }
    .tracking-pill.serial { background: rgba(168, 85, 247, 0.2); color: #c084fc; }

    .item-status-badge { font-size: 0.75rem; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600; }
    .status-pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-partial { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .status-picked { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-short { background: rgba(239, 68, 68, 0.2); color: #f87171; }

    .serial-tags { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .serial-pill { font-family: monospace; font-size: 0.7rem; background: rgba(59, 130, 246, 0.15); color: #60a5fa; padding: 0.15rem 0.35rem; border-radius: 3px; }

    .checklist-items { padding: 1rem 1.25rem; display: flex; flex-direction: column; gap: 0.75rem; }
    .check-row { display: flex; align-items: center; gap: 0.75rem; background: #0f172a; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid #334155; }
    .check-row input { width: 18px; height: 18px; cursor: pointer; }
    .check-info { flex: 1; display: flex; flex-direction: column; }
    .check-desc { font-size: 0.9rem; color: #f8fafc; font-weight: 500; }
    .checked-text { text-decoration: line-through; color: #94a3b8; }
    .check-meta { font-size: 0.75rem; color: #64748b; }
    .text-info { color: #38bdf8; }
    .text-warning { color: #f59e0b; }
    .text-muted { color: #94a3b8; }
  `]
})
export class WarehousePickListDetailComponent implements OnInit {
  pickList: PickList | null = null;
  checklists: WarehouseChecklist[] = [];
  loading = true;

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    private route: ActivatedRoute,
    private router: Router,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPickList(id);
    }
  }

  loadPickList(id?: string): void {
    const pickId = id || (this.pickList ? this.pickList.id : this.route.snapshot.paramMap.get('id'));
    if (!pickId) return;

    this.loading = true;
    this.fulfillmentService.getPickList(pickId).subscribe({
      next: (pl) => {
        this.pickList = pl;
        this.loadChecklists(pl.warehouseOrderId);
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
        this.checklists = cls.filter(c => c.stage === 'PICK');
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

  verifyPickList(): void {
    if (!this.pickList) return;
    this.fulfillmentService.verifyPickList(this.pickList.id).subscribe({
      next: (res) => {
        alert(`Pick List ${res.pickListNumber} verified! Warehouse order advanced to PACKING.`);
        this.pickList = res;
      },
      error: (err) => {
        alert(err.error?.message || 'Verification failed. Ensure all items are picked and blocking exceptions are resolved.');
      }
    });
  }

  generatePackList(): void {
    if (!this.pickList) return;
    this.fulfillmentService.generatePackList(this.pickList.warehouseOrderId).subscribe({
      next: (pkl) => {
        this.router.navigate(['/warehouse/pack-lists', pkl.id]);
      },
      error: (err) => {
        alert(err.error?.message || 'Error generating pack list');
      }
    });
  }
}
