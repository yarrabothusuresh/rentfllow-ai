import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseService, WarehouseOrder, WarehouseOrderItem } from '../../services/warehouse.service';

@Component({
  selector: 'app-warehouse-order-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="order-detail-container" *ngIf="order">
      <div class="top-nav">
        <a routerLink="/warehouse/orders" class="btn btn-outline">&larr; Back to Work Orders</a>
        <div class="header-badges">
          <span class="badge" [ngClass]="getPriorityClass(order.priority)">Priority: {{ order.priority }}</span>
          <span class="status-pill" [ngClass]="getStatusClass(order.status)">Status: {{ order.status }}</span>
        </div>
      </div>

      <!-- Order Header Card -->
      <div class="order-header-card">
        <div class="header-main">
          <h2>WAREHOUSE WORK ORDER {{ order.orderNumber }}</h2>
          <h3 class="event-title">{{ order.eventName || 'Equipment Staging' }}</h3>
        </div>

        <div class="header-meta-grid">
          <div class="meta-item">
            <span class="meta-label">Customer</span>
            <span class="meta-value">👤 {{ order.customerName || 'Direct Rental' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Booking Reference</span>
            <span class="meta-value">📋 {{ order.bookingNumber || 'BKG-000123' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Event Date</span>
            <span class="meta-value">📅 {{ order.eventDate ? (order.eventDate | date:'mediumDate') : 'Scheduled' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Venue</span>
            <span class="meta-value">📍 {{ order.venueName || 'Main Warehouse' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Assigned To</span>
            <span class="meta-value">👷 {{ order.assignedTo || 'Unassigned' }}</span>
          </div>
        </div>

        <!-- Progress bar -->
        <div class="progress-section">
          <div class="progress-header">
            <span>Overall Picking Progress</span>
            <span class="progress-count">{{ order.totalQuantityPicked }} / {{ order.totalQuantityRequired }} units picked ({{ order.pickingProgressPercentage | number:'1.1-1' }}%)</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill" [style.width.%]="order.pickingProgressPercentage"></div>
          </div>
        </div>

        <!-- Workflow Action Toolbar -->
        <div class="action-toolbar">
          <button *ngIf="order.status === 'READY_TO_PICK'" (click)="startPicking()" class="btn btn-lg btn-success">
            🚜 Start Picking
          </button>
          <button *ngIf="order.status === 'PICKING'" (click)="completePicking()" class="btn btn-lg btn-primary">
            ✓ Complete Picking
          </button>
          <button *ngIf="order.status === 'PICKED'" (click)="startPacking()" class="btn btn-lg btn-warning">
            📦 Start Packing
          </button>
          <button *ngIf="order.status === 'PACKING'" (click)="completePacking()" class="btn btn-lg btn-primary">
            🚚 Complete Packing & Stage for Delivery
          </button>
          <span *ngIf="order.status === 'READY_FOR_DELIVERY'" class="ready-notice">
            🎉 Work order is fully packed & ready for delivery!
          </span>
        </div>
      </div>

      <!-- Items Section with Tablet Touch Controls -->
      <div class="items-card">
        <h3>EQUIPMENT PICK & PACK CHECKLIST</h3>

        <div class="items-list">
          <div class="item-row" *ngFor="let item of order.items" [ngClass]="{'item-short': item.status === 'SHORT', 'item-done': item.status === 'PICKED' || item.status === 'PACKED'}">
            <div class="item-info">
              <span class="location-badge">📍 Location: {{ item.locationSnapshot || 'A-01-01' }}</span>
              <h4 class="product-name">{{ item.productNameSnapshot }}</h4>
              <span class="sku-tag">SKU: {{ item.skuSnapshot }}</span>
              <p class="item-notes" *ngIf="item.notes">⚠️ Note: {{ item.notes }}</p>
            </div>

            <div class="item-counts">
              <div class="count-box">
                <span class="count-num">{{ item.quantityRequired }}</span>
                <span class="count-lbl">Required</span>
              </div>
              <div class="count-box text-primary">
                <span class="count-num">{{ item.quantityPicked }}</span>
                <span class="count-lbl">Picked</span>
              </div>
              <div class="count-box text-info">
                <span class="count-num">{{ item.quantityPacked }}</span>
                <span class="count-lbl">Packed</span>
              </div>
            </div>

            <!-- Tablet Touch Controls for Operators -->
            <div class="touch-controls" *ngIf="order.status === 'PICKING'">
              <button (click)="adjustPick(item, -5)" class="btn-touch btn-touch-sub">-5</button>
              <button (click)="adjustPick(item, -1)" class="btn-touch btn-touch-sub">-1</button>
              <button (click)="adjustPick(item, 1)" class="btn-touch btn-touch-add">+1</button>
              <button (click)="adjustPick(item, 5)" class="btn-touch btn-touch-add">+5</button>
              <button (click)="markAllPicked(item)" class="btn-touch btn-touch-all">Mark Picked</button>
              <button (click)="reportShortage(item)" class="btn-touch btn-touch-danger">Shortage</button>
            </div>

            <div class="touch-controls" *ngIf="order.status === 'PACKING'">
              <button (click)="adjustPack(item, 1)" class="btn-touch btn-touch-add">+1 Pack</button>
              <button (click)="markAllPacked(item)" class="btn-touch btn-touch-all">Mark Packed</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Shortage Confirmation Modal -->
      <div class="modal-backdrop" *ngIf="showShortageModal">
        <div class="modal-card">
          <h3>⚠️ Shortage Confirmation Required</h3>
          <p>Some items are short or not fully picked. Are you sure you want to complete picking and proceed with shortage override?</p>
          <div class="modal-actions">
            <button (click)="showShortageModal = false" class="btn btn-outline">Cancel</button>
            <button (click)="confirmShortageAndComplete()" class="btn btn-danger">Confirm Shortage Override</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .order-detail-container { padding: 1.5rem; max-width: 1200px; margin: 0 auto; }
    .top-nav { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .header-badges { display: flex; gap: 0.75rem; }
    .badge { padding: 0.3rem 0.7rem; border-radius: 6px; font-weight: 700; font-size: 0.8rem; }
    .badge-urgent { background: #fecaca; color: #991b1b; }
    .badge-high { background: #fed7aa; color: #9a3412; }
    .badge-normal { background: #e2e8f0; color: #334155; }
    .status-pill { padding: 0.3rem 0.7rem; border-radius: 6px; font-weight: 700; font-size: 0.8rem; }
    .status-ready { background: #dbeafe; color: #1e40af; }
    .status-picking { background: #fef3c7; color: #92400e; }
    .status-packed { background: #dcfce7; color: #166534; }

    .order-header-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
      margin-bottom: 1.5rem;
    }
    .header-main h2 { margin: 0; font-size: 1.1rem; color: #64748b; letter-spacing: 0.05em; }
    .event-title { margin: 0.25rem 0 1rem 0; font-size: 1.6rem; color: #0f172a; }

    .header-meta-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      padding: 1rem 0;
      border-top: 1px solid #f1f5f9;
      border-bottom: 1px solid #f1f5f9;
    }
    .meta-label { font-size: 0.78rem; color: #64748b; font-weight: 600; text-transform: uppercase; display: block; }
    .meta-value { font-size: 0.95rem; font-weight: 600; color: #1e293b; margin-top: 0.2rem; display: block; }

    .progress-section { margin-top: 1.25rem; }
    .progress-header { display: flex; justify-content: space-between; font-size: 0.85rem; color: #475569; font-weight: 600; margin-bottom: 0.4rem; }
    .progress-track { height: 10px; background: #e2e8f0; border-radius: 5px; overflow: hidden; }
    .progress-fill { height: 100%; background: #3b82f6; transition: width 0.3s ease; }

    .action-toolbar { margin-top: 1.5rem; display: flex; gap: 1rem; }
    .btn-lg { padding: 0.8rem 1.6rem; font-size: 1rem; font-weight: 700; border-radius: 8px; border: none; cursor: pointer; }
    .btn-success { background: #10b981; color: white; }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-warning { background: #f59e0b; color: white; }
    .btn-danger { background: #ef4444; color: white; }
    .btn-outline { border: 1px solid #cbd5e1; background: white; color: #475569; padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; font-weight: 600; }
    .ready-notice { background: #dcfce7; color: #166534; padding: 0.8rem 1.5rem; border-radius: 8px; font-weight: 700; font-size: 1rem; }

    .items-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }
    .items-card h3 { margin: 0 0 1.25rem 0; font-size: 1.1rem; color: #0f172a; }

    .item-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.25rem;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      margin-bottom: 1rem;
      background: #f8fafc;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .item-short { border-color: #fca5a5; background: #fff5f5; }
    .item-done { border-color: #86efac; background: #f0fdf4; }

    .item-info { flex: 2; min-width: 240px; }
    .location-badge { background: #e2e8f0; color: #334155; font-size: 0.78rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px; }
    .product-name { margin: 0.4rem 0 0.2rem 0; font-size: 1.1rem; color: #0f172a; }
    .sku-tag { font-family: monospace; font-size: 0.8rem; color: #64748b; }
    .item-notes { font-size: 0.85rem; color: #dc2626; margin-top: 0.4rem; font-weight: 600; }

    .item-counts { display: flex; gap: 1.25rem; }
    .count-box { text-align: center; }
    .count-num { font-size: 1.4rem; font-weight: 700; display: block; }
    .count-lbl { font-size: 0.75rem; color: #64748b; font-weight: 600; }

    /* Touch controls for tablets */
    .touch-controls { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .btn-touch {
      padding: 0.6rem 0.9rem;
      font-size: 0.95rem;
      font-weight: 700;
      border-radius: 8px;
      border: none;
      cursor: pointer;
    }
    .btn-touch-sub { background: #e2e8f0; color: #334155; }
    .btn-touch-add { background: #3b82f6; color: white; }
    .btn-touch-all { background: #10b981; color: white; }
    .btn-touch-danger { background: #ef4444; color: white; }

    /* Modal */
    .modal-backdrop {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex; align-items: center; justify-content: center;
      z-index: 1000;
    }
    .modal-card {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      max-width: 480px;
      width: 90%;
    }
    .modal-card h3 { margin-top: 0; color: #991b1b; }
    .modal-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
  `]
})
export class WarehouseOrderDetailComponent implements OnInit {
  order: WarehouseOrder | null = null;
  showShortageModal: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private warehouseService: WarehouseService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(id);
    }
  }

  loadOrder(id: string): void {
    this.warehouseService.getOrderById(id).subscribe(data => this.order = data);
  }

  startPicking(): void {
    if (!this.order) return;
    this.warehouseService.startPicking(this.order.id).subscribe(data => this.order = data);
  }

  adjustPick(item: WarehouseOrderItem, delta: number): void {
    if (!this.order) return;
    const newQty = Math.max(0, Math.min(item.quantityRequired - item.quantityPicked, delta));
    if (newQty <= 0 && delta > 0) return;
    this.warehouseService.pickItem(this.order.id, item.id, delta > 0 ? newQty : delta).subscribe(data => this.order = data);
  }

  markAllPicked(item: WarehouseOrderItem): void {
    if (!this.order) return;
    const qtyNeeded = item.quantityRequired - item.quantityPicked;
    if (qtyNeeded <= 0) return;
    this.warehouseService.pickItem(this.order.id, item.id, qtyNeeded).subscribe(data => this.order = data);
  }

  reportShortage(item: WarehouseOrderItem): void {
    if (!this.order) return;
    const note = prompt('Enter shortage description (e.g. 5 chairs damaged/missing):');
    if (note) {
      this.warehouseService.pickItem(this.order.id, item.id, 0, note, true).subscribe(data => this.order = data);
    }
  }

  completePicking(): void {
    if (!this.order) return;
    this.warehouseService.completePicking(this.order.id, false).subscribe({
      next: data => this.order = data,
      error: () => this.showShortageModal = true
    });
  }

  confirmShortageAndComplete(): void {
    if (!this.order) return;
    this.showShortageModal = false;
    this.warehouseService.completePicking(this.order.id, true, 'Shortage confirmed by manager').subscribe(data => this.order = data);
  }

  startPacking(): void {
    if (!this.order) return;
    this.warehouseService.startPacking(this.order.id).subscribe(data => this.order = data);
  }

  adjustPack(item: WarehouseOrderItem, delta: number): void {
    if (!this.order) return;
    this.warehouseService.packItem(this.order.id, item.id, delta).subscribe(data => this.order = data);
  }

  markAllPacked(item: WarehouseOrderItem): void {
    if (!this.order) return;
    const qtyToPack = item.quantityPicked - item.quantityPacked;
    if (qtyToPack <= 0) return;
    this.warehouseService.packItem(this.order.id, item.id, qtyToPack).subscribe(data => this.order = data);
  }

  completePacking(): void {
    if (!this.order) return;
    this.warehouseService.completePacking(this.order.id, true).subscribe(data => this.order = data);
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'URGENT': return 'badge-urgent';
      case 'HIGH': return 'badge-high';
      default: return 'badge-normal';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'READY_TO_PICK': return 'status-ready';
      case 'PICKING': return 'status-picking';
      case 'PICKED': case 'PACKED': case 'READY_FOR_DELIVERY': return 'status-packed';
      default: return 'status-ready';
    }
  }
}
