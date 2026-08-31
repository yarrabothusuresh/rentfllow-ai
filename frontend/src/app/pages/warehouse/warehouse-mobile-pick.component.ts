import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WarehouseFulfillmentService, PickList, PickListItem } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-mobile-pick',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="mobile-pick-view animate-fade-in" *ngIf="pickList">
      <!-- Top Mobile Navigation -->
      <div class="mobile-nav">
        <a [routerLink]="['/warehouse/pick-lists', pickList.id]" class="btn-nav">← Back</a>
        <div class="nav-title">
          <h3>{{ pickList.pickListNumber }}</h3>
          <span class="nav-sub">{{ pickList.totalPickedItems }}/{{ pickList.totalRequiredItems }} picked ({{ pickList.progressPercentage | number:'1.0-0' }}%)</span>
        </div>
        <button class="btn-refresh" (click)="loadPickList()">🔄</button>
      </div>

      <!-- Mobile Progress Bar -->
      <div class="mobile-progress-track">
        <div class="mobile-progress-fill" [style.width.%]="pickList.progressPercentage"></div>
      </div>

      <!-- Scan Input Bar -->
      <div class="scanner-bar">
        <div class="scanner-input-wrap">
          <span class="scanner-icon">📷</span>
          <input
            #barcodeInput
            type="text"
            [(ngModel)]="scannedBarcode"
            placeholder="Scan Barcode / Serial #"
            (keyup.enter)="handleScan()"
            autofocus
          />
          <button class="btn-scan-submit" (click)="handleScan()" [disabled]="!scannedBarcode">Scan</button>
        </div>
        <div class="scan-feedback success" *ngIf="scanSuccessMsg">{{ scanSuccessMsg }}</div>
        <div class="scan-feedback error" *ngIf="scanErrorMsg">{{ scanErrorMsg }}</div>
      </div>

      <!-- Current Sequence Item (Focus Card) -->
      <div class="current-item-card" *ngIf="activeItem">
        <div class="location-banner">
          <span class="loc-tag">📍 LOCATION</span>
          <span class="loc-code">{{ activeItem.locationCodeSnapshot || 'MAIN BAY' }}</span>
          <span class="seq-tag">Stop #{{ activeItem.sequenceNumber }}</span>
        </div>

        <div class="item-details">
          <div class="tracking-badge" [ngClass]="activeItem.trackingType === 'SERIALIZED' ? 'badge-serial' : 'badge-bulk'">
            {{ activeItem.trackingType === 'SERIALIZED' ? '🔍 SERIALIZED ASSET' : '📦 BULK QUANTITY' }}
          </div>
          <h2 class="item-name">{{ activeItem.productNameSnapshot }}</h2>
          <div class="item-sku">SKU: <strong>{{ activeItem.skuSnapshot }}</strong></div>

          <div class="item-counts">
            <div class="count-box">
              <span class="count-val">{{ activeItem.requiredQuantity }}</span>
              <span class="count-lbl">Required</span>
            </div>
            <div class="count-box picked">
              <span class="count-val">{{ activeItem.pickedQuantity }}</span>
              <span class="count-lbl">Picked</span>
            </div>
            <div class="count-box remaining">
              <span class="count-val">{{ activeItem.remainingQuantity }}</span>
              <span class="count-lbl">Remaining</span>
            </div>
          </div>

          <!-- Big Touch Buttons for Picking -->
          <div class="touch-buttons" *ngIf="activeItem.remainingQuantity > 0">
            <button class="btn-touch btn-add-1" (click)="pickQty(activeItem, 1)">+1 Pick</button>
            <button class="btn-touch btn-add-5" *ngIf="activeItem.remainingQuantity >= 5" (click)="pickQty(activeItem, 5)">+5 Pick</button>
            <button class="btn-touch btn-add-all" (click)="pickQty(activeItem, activeItem.remainingQuantity)">
              +{{ activeItem.remainingQuantity }} (All)
            </button>
          </div>

          <div class="custom-qty-row" *ngIf="activeItem.remainingQuantity > 0">
            <input type="number" [(ngModel)]="customQuantity" min="1" [max]="activeItem.remainingQuantity" placeholder="Qty" />
            <button class="btn-custom-pick" (click)="pickCustomQty(activeItem)">Submit Qty</button>
          </div>

          <!-- Scanned Assets List (if serialized) -->
          <div class="scanned-tags" *ngIf="activeItem.scannedAssetCodes && activeItem.scannedAssetCodes.length > 0">
            <span class="tag-title">Scanned Serial Codes:</span>
            <div class="tags-row">
              <span class="code-pill" *ngFor="let c of activeItem.scannedAssetCodes">✓ {{ c }}</span>
            </div>
          </div>

          <!-- Exception Buttons -->
          <div class="exception-actions" *ngIf="activeItem.remainingQuantity > 0">
            <button class="btn-exc btn-shortage" (click)="openShortageModal(activeItem)">⚠️ Report Shortage</button>
            <button class="btn-exc btn-damage" (click)="openDamageModal(activeItem)">💥 Report Damage</button>
          </div>
        </div>
      </div>

      <!-- Item Sequence List -->
      <div class="sequence-list-section">
        <h4 class="section-title">Sequence Traversal ({{ pickList.items.length }} Items)</h4>
        <div class="seq-list">
          <div
            class="seq-item-row"
            *ngFor="let item of pickList.items"
            [class.active-row]="activeItem?.id === item.id"
            [class.completed-row]="item.status === 'PICKED'"
            (click)="selectItem(item)"
          >
            <div class="seq-num">{{ item.sequenceNumber }}</div>
            <div class="seq-info">
              <span class="seq-loc">{{ item.locationCodeSnapshot }}</span>
              <span class="seq-prod">{{ item.productNameSnapshot }}</span>
            </div>
            <div class="seq-status">
              <span class="seq-progress">{{ item.pickedQuantity }}/{{ item.requiredQuantity }}</span>
              <span class="seq-check" *ngIf="item.status === 'PICKED'">✓</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Action -->
      <div class="mobile-footer">
        <button
          class="btn-complete-pick"
          [disabled]="pickList.totalPickedItems === 0"
          (click)="completePicking()"
        >
          ✓ Complete Pick Phase
        </button>
      </div>

      <!-- Shortage Modal -->
      <div class="modal-backdrop" *ngIf="showShortageModal">
        <div class="modal-sheet">
          <h3>⚠️ Report Pick Shortage</h3>
          <p>Item: <strong>{{ activeItem?.productNameSnapshot }}</strong></p>

          <label>Short Quantity:</label>
          <input type="number" [(ngModel)]="shortQuantity" class="form-input" min="1" />

          <label>Reason Code:</label>
          <select [(ngModel)]="shortReason" class="form-input">
            <option value="NOT_FOUND">Item Not Found in Bay</option>
            <option value="INSUFFICIENT_STOCK">Insufficient Physical Stock</option>
            <option value="DAMAGED_IN_BAY">Damaged Goods in Bay</option>
            <option value="WRONG_LOCATION">Bin Empty / Misplaced</option>
          </select>

          <label>Notes & Location Details:</label>
          <textarea [(ngModel)]="shortNotes" class="form-input" placeholder="Explain missing count or bay status..."></textarea>

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showShortageModal = false">Cancel</button>
            <button class="btn-confirm-shortage" (click)="submitShortage()">Log Shortage (Blocking)</button>
          </div>
        </div>
      </div>

      <!-- Damage Modal -->
      <div class="modal-backdrop" *ngIf="showDamageModal">
        <div class="modal-sheet">
          <h3>💥 Report Damaged Asset</h3>
          <p>Item: <strong>{{ activeItem?.productNameSnapshot }}</strong></p>

          <label>Damaged Quantity:</label>
          <input type="number" [(ngModel)]="damagedQuantity" class="form-input" min="1" />

          <label>Asset Serial / Barcode (if applicable):</label>
          <input type="text" [(ngModel)]="damagedAssetCode" class="form-input" placeholder="e.g. CHR-000123" />

          <label>Damage Description:</label>
          <textarea [(ngModel)]="damageNotes" class="form-input" placeholder="Broken leg, torn cushion, bent frame..."></textarea>

          <div class="modal-actions">
            <button class="btn-cancel" (click)="showDamageModal = false">Cancel</button>
            <button class="btn-confirm-damage" (click)="submitDamage()">Log Damage & Quarantine</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .mobile-pick-view {
      max-width: 600px;
      margin: 0 auto;
      background: #0f172a;
      min-height: 100vh;
      color: #f8fafc;
      padding-bottom: 5rem;
    }
    .mobile-nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.8rem 1rem;
      background: #1e293b;
      border-bottom: 1px solid #334155;
    }
    .btn-nav { color: #38bdf8; text-decoration: none; font-weight: 600; font-size: 0.9rem; }
    .nav-title { text-align: center; }
    .nav-title h3 { margin: 0; font-size: 1.1rem; font-weight: 700; color: #f8fafc; }
    .nav-sub { font-size: 0.75rem; color: #94a3b8; }
    .btn-refresh { background: transparent; border: none; font-size: 1.2rem; cursor: pointer; color: #94a3b8; }

    .mobile-progress-track { height: 6px; background: #334155; }
    .mobile-progress-fill { height: 100%; background: #3b82f6; transition: width 0.3s ease; }

    /* Scanner Bar */
    .scanner-bar {
      padding: 0.8rem 1rem;
      background: #1e293b;
      border-bottom: 1px solid #334155;
    }
    .scanner-input-wrap {
      display: flex;
      align-items: center;
      background: #0f172a;
      border: 2px solid #3b82f6;
      border-radius: 8px;
      padding: 0.4rem 0.6rem;
      gap: 0.5rem;
    }
    .scanner-icon { font-size: 1.2rem; }
    .scanner-input-wrap input {
      flex: 1;
      background: transparent;
      border: none;
      color: #fff;
      font-size: 1rem;
      outline: none;
    }
    .btn-scan-submit {
      background: #3b82f6;
      border: none;
      color: #fff;
      padding: 0.4rem 0.8rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
    }
    .btn-scan-submit:disabled { background: #475569; }
    .scan-feedback { font-size: 0.85rem; padding: 0.4rem 0.6rem; border-radius: 6px; margin-top: 0.4rem; font-weight: 600; }
    .scan-feedback.success { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .scan-feedback.error { background: rgba(239, 68, 68, 0.2); color: #f87171; }

    /* Current Item Card */
    .current-item-card {
      margin: 1rem;
      background: #1e293b;
      border-radius: 12px;
      border: 1px solid #334155;
      overflow: hidden;
      box-shadow: 0 8px 16px rgba(0,0,0,0.3);
    }
    .location-banner {
      background: #2563eb;
      padding: 0.75rem 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #fff;
    }
    .loc-tag { font-size: 0.75rem; font-weight: 700; opacity: 0.9; }
    .loc-code { font-size: 1.4rem; font-weight: 800; font-family: monospace; letter-spacing: 0.05em; }
    .seq-tag { font-size: 0.8rem; background: rgba(255,255,255,0.2); padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600; }

    .item-details { padding: 1.25rem; }
    .tracking-badge { display: inline-block; font-size: 0.75rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px; margin-bottom: 0.5rem; }
    .badge-serial { background: rgba(168, 85, 247, 0.2); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.4); }
    .badge-bulk { background: rgba(59, 130, 246, 0.2); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.4); }

    .item-name { margin: 0 0 0.25rem 0; font-size: 1.3rem; font-weight: 700; color: #f8fafc; }
    .item-sku { font-size: 0.85rem; color: #94a3b8; margin-bottom: 1rem; }

    .item-counts {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
    }
    .count-box {
      background: #0f172a;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 0.6rem;
      text-align: center;
    }
    .count-val { display: block; font-size: 1.4rem; font-weight: 700; color: #f8fafc; }
    .count-lbl { font-size: 0.75rem; color: #94a3b8; }
    .count-box.picked .count-val { color: #38bdf8; }
    .count-box.remaining .count-val { color: #f59e0b; }

    /* Touch Buttons */
    .touch-buttons {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.5rem;
      margin-bottom: 0.75rem;
    }
    .btn-touch {
      padding: 0.9rem 0.5rem;
      border-radius: 8px;
      border: none;
      font-size: 1.1rem;
      font-weight: 700;
      color: #fff;
      cursor: pointer;
      box-shadow: 0 4px 6px rgba(0,0,0,0.2);
    }
    .btn-add-1 { background: #2563eb; }
    .btn-add-5 { background: #4f46e5; }
    .btn-add-all { background: #16a34a; }

    .custom-qty-row {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }
    .custom-qty-row input {
      width: 80px;
      background: #0f172a;
      border: 1px solid #334155;
      border-radius: 6px;
      color: #fff;
      text-align: center;
      font-size: 1rem;
    }
    .btn-custom-pick {
      flex: 1;
      background: #334155;
      border: 1px solid #475569;
      color: #fff;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
    }

    .scanned-tags { margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid #334155; }
    .tag-title { font-size: 0.75rem; color: #94a3b8; display: block; margin-bottom: 0.35rem; }
    .tags-row { display: flex; flex-wrap: wrap; gap: 0.35rem; }
    .code-pill { font-family: monospace; font-size: 0.75rem; background: rgba(56, 189, 248, 0.15); color: #38bdf8; padding: 0.2rem 0.4rem; border-radius: 4px; }

    .exception-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.5rem;
      margin-top: 1rem;
      padding-top: 0.75rem;
      border-top: 1px solid #334155;
    }
    .btn-exc {
      padding: 0.6rem;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      border: none;
      cursor: pointer;
    }
    .btn-shortage { background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.4); }
    .btn-damage { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); }

    /* Sequence List */
    .sequence-list-section { padding: 1rem; }
    .section-title { margin: 0 0 0.75rem 0; font-size: 0.95rem; color: #94a3b8; }
    .seq-list { display: flex; flex-direction: column; gap: 0.4rem; }
    .seq-item-row {
      display: flex;
      align-items: center;
      background: #1e293b;
      padding: 0.6rem 0.8rem;
      border-radius: 8px;
      border: 1px solid #334155;
      gap: 0.75rem;
      cursor: pointer;
    }
    .seq-item-row.active-row { border-color: #3b82f6; background: #1e3a8a; }
    .seq-item-row.completed-row { opacity: 0.6; }
    .seq-num { font-size: 0.9rem; font-weight: 700; color: #94a3b8; width: 24px; }
    .seq-info { flex: 1; display: flex; flex-direction: column; }
    .seq-loc { font-size: 0.75rem; font-weight: 700; color: #38bdf8; font-family: monospace; }
    .seq-prod { font-size: 0.85rem; color: #f8fafc; }
    .seq-status { display: flex; align-items: center; gap: 0.4rem; }
    .seq-progress { font-size: 0.8rem; color: #cbd5e1; font-weight: 600; }
    .seq-check { color: #4ade80; font-weight: 800; }

    /* Mobile Footer */
    .mobile-footer {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      max-width: 600px;
      margin: 0 auto;
      background: #1e293b;
      padding: 0.8rem 1rem;
      border-top: 1px solid #334155;
      box-shadow: 0 -4px 10px rgba(0,0,0,0.3);
    }
    .btn-complete-pick {
      width: 100%;
      padding: 0.9rem;
      background: #10b981;
      border: none;
      border-radius: 8px;
      color: #fff;
      font-size: 1.1rem;
      font-weight: 700;
      cursor: pointer;
    }
    .btn-complete-pick:disabled { background: #475569; }

    /* Modals */
    .modal-backdrop {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      align-items: flex-end;
      justify-content: center;
      z-index: 1000;
    }
    .modal-sheet {
      background: #1e293b;
      width: 100%;
      max-width: 600px;
      padding: 1.5rem;
      border-radius: 16px 16px 0 0;
      border-top: 1px solid #475569;
    }
    .modal-sheet h3 { margin: 0 0 0.5rem 0; font-size: 1.2rem; color: #f8fafc; }
    .modal-sheet label { display: block; font-size: 0.85rem; color: #94a3b8; margin: 0.75rem 0 0.25rem 0; font-weight: 600; }
    .form-input {
      width: 100%;
      background: #0f172a;
      border: 1px solid #334155;
      border-radius: 6px;
      padding: 0.6rem;
      color: #fff;
      font-size: 0.9rem;
      box-sizing: border-box;
    }
    .modal-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }
    .btn-cancel { background: #334155; border: none; color: #fff; padding: 0.75rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-shortage { background: #d97706; border: none; color: #fff; padding: 0.75rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-confirm-damage { background: #dc2626; border: none; color: #fff; padding: 0.75rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class WarehouseMobilePickComponent implements OnInit {
  @ViewChild('barcodeInput') barcodeInputRef!: ElementRef;

  pickList: PickList | null = null;
  activeItem: PickListItem | null = null;
  scannedBarcode: string = '';
  customQuantity: number = 1;
  scanSuccessMsg: string = '';
  scanErrorMsg: string = '';

  showShortageModal = false;
  shortQuantity = 1;
  shortReason = 'NOT_FOUND';
  shortNotes = '';

  showDamageModal = false;
  damagedQuantity = 1;
  damagedAssetCode = '';
  damageNotes = '';

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

    this.fulfillmentService.getPickList(pickId).subscribe({
      next: (pl) => {
        this.pickList = pl;
        if (!this.activeItem || this.activeItem.status === 'PICKED') {
          this.activeItem = pl.items.find(i => i.status !== 'PICKED') || pl.items[0];
        } else {
          this.activeItem = pl.items.find(i => i.id === this.activeItem!.id) || pl.items[0];
        }
      }
    });
  }

  selectItem(item: PickListItem): void {
    this.activeItem = item;
    this.customQuantity = item.remainingQuantity > 0 ? item.remainingQuantity : 1;
  }

  handleScan(): void {
    if (!this.scannedBarcode || !this.pickList) return;
    const barcode = this.scannedBarcode.trim();
    this.scannedBarcode = '';
    this.scanSuccessMsg = '';
    this.scanErrorMsg = '';

    this.fulfillmentService.scanPickItem(this.pickList.id, barcode).subscribe({
      next: (res) => {
        this.scanSuccessMsg = `✓ Scanned ${res.assetCode || barcode} (${res.productName || ''}) successfully!`;
        this.loadPickList();
      },
      error: (err) => {
        this.scanErrorMsg = err.error?.message || err.error?.error || 'Invalid scan: item not matched or already scanned.';
      }
    });
  }

  pickQty(item: PickListItem, qty: number): void {
    if (!this.pickList) return;
    this.fulfillmentService.pickQuantity(this.pickList.id, item.id, qty).subscribe({
      next: () => {
        this.loadPickList();
      },
      error: (err) => {
        alert(err.error?.message || 'Error picking quantity');
      }
    });
  }

  pickCustomQty(item: PickListItem): void {
    if (this.customQuantity > 0) {
      this.pickQty(item, this.customQuantity);
    }
  }

  openShortageModal(item: PickListItem): void {
    this.activeItem = item;
    this.shortQuantity = item.remainingQuantity;
    this.shortReason = 'NOT_FOUND';
    this.shortNotes = '';
    this.showShortageModal = true;
  }

  submitShortage(): void {
    if (!this.pickList || !this.activeItem) return;
    this.fulfillmentService.reportShortage(
      this.pickList.id,
      this.activeItem.id,
      this.shortQuantity,
      this.shortReason,
      this.shortNotes
    ).subscribe({
      next: () => {
        this.showShortageModal = false;
        alert('Shortage reported. Blocking exception logged on order.');
        this.loadPickList();
      },
      error: (err) => {
        alert(err.error?.message || 'Error logging shortage');
      }
    });
  }

  openDamageModal(item: PickListItem): void {
    this.activeItem = item;
    this.damagedQuantity = 1;
    this.damagedAssetCode = '';
    this.damageNotes = '';
    this.showDamageModal = true;
  }

  submitDamage(): void {
    if (!this.pickList || !this.activeItem) return;
    this.fulfillmentService.reportDamage(
      this.pickList.id,
      this.activeItem.id,
      this.damagedQuantity,
      this.damageNotes,
      this.damagedAssetCode
    ).subscribe({
      next: () => {
        this.showDamageModal = false;
        alert('Damaged asset reported and quarantined.');
        this.loadPickList();
      },
      error: (err) => {
        alert(err.error?.message || 'Error logging damage');
      }
    });
  }

  completePicking(): void {
    if (!this.pickList) return;
    this.fulfillmentService.completePickList(this.pickList.id).subscribe({
      next: (res) => {
        alert(`Pick List ${res.pickListNumber} completed! Transitioned to verification.`);
        this.router.navigate(['/warehouse/pick-lists', res.id]);
      },
      error: (err) => {
        alert(err.error?.message || 'Cannot complete pick list. Ensure all blocking exceptions are resolved.');
      }
    });
  }
}
