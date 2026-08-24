import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InventoryAvailabilityService, InventoryConflict } from '../../services/inventory-availability.service';

@Component({
  selector: 'app-inventory-conflicts',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="conflicts-container">
      <header class="page-header">
        <div>
          <h1>⚠️ Inventory Conflicts & Shortages</h1>
          <p class="subtitle">Identify overbooked products and evaluate category alternative recommendations</p>
        </div>
        <a routerLink="/inventory/dashboard" class="btn btn-secondary">&larr; Back to Dashboard</a>
      </header>

      <!-- Conflicts Alert Summary -->
      <div class="summary-banner danger-banner" *ngIf="conflicts.length > 0">
        <span class="banner-icon">⚠️</span>
        <div>
          <strong>{{ conflicts.length }} Active Equipment Shortage Conflict(s)</strong>
          <p>Overbooking occurs when requested booking quantities exceed remaining unreserved warehouse inventory.</p>
        </div>
      </div>

      <div class="summary-banner success-banner" *ngIf="conflicts.length === 0">
        <span class="banner-icon">🟢</span>
        <div>
          <strong>No Active Inventory Conflicts</strong>
          <p>All confirmed booking dates have sufficient inventory reserved.</p>
        </div>
      </div>

      <!-- Conflicts Cards -->
      <div class="conflicts-list" *ngIf="conflicts.length > 0">
        <div class="conflict-card" *ngFor="let c of conflicts">
          <div class="conflict-header">
            <div class="title-group">
              <span class="priority-tag">{{ c.priority || 'HIGH' }} PRIORITY</span>
              <h2>{{ c.productName }}</h2>
              <span class="sku-tag">SKU: {{ c.sku }}</span>
            </div>
            <div class="shortage-badge">
              SHORT BY {{ c.shortageQuantity }} UNITS
            </div>
          </div>

          <div class="conflict-body">
            <div class="info-col">
              <span class="label">Available Balance</span>
              <span class="value text-red">{{ c.availableQuantity }} units</span>
            </div>
            <div class="info-col">
              <span class="label">Event Date</span>
              <span class="value">{{ c.eventDate | date:'mediumDate' }}</span>
            </div>
            <div class="info-col">
              <span class="label">Associated Event</span>
              <span class="value">{{ c.eventName || 'Wedding Reception' }}</span>
            </div>
          </div>

          <!-- Suggested Alternatives Section -->
          <div class="alternatives-section">
            <h3>💡 Suggested Equipment Alternatives</h3>
            <div class="alternatives-grid" *ngIf="c.suggestedAlternatives && c.suggestedAlternatives.length > 0">
              <div class="alt-card" *ngFor="let alt of c.suggestedAlternatives">
                <span class="alt-name">{{ alt.productName }}</span>
                <span class="alt-avail">{{ alt.availableQuantity }} Available</span>
              </div>
            </div>
            <p *ngIf="!c.suggestedAlternatives || c.suggestedAlternatives.length === 0" class="no-alt">
              No direct category alternatives found with available quantity.
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .conflicts-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h1 { font-size: 1.8rem; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; }
    .btn { padding: 0.6rem 1.2rem; border-radius: 6px; font-weight: 600; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
    .btn-secondary { background: #64748b; color: white; }
    
    .summary-banner { display: flex; align-items: center; gap: 1rem; padding: 1rem 1.25rem; border-radius: 8px; margin-bottom: 1.5rem; }
    .danger-banner { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
    .success-banner { background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; }
    .banner-icon { font-size: 1.8rem; }
    .summary-banner p { margin: 0.2rem 0 0 0; font-size: 0.85rem; }

    .conflicts-list { display: flex; flex-direction: column; gap: 1.25rem; }
    .conflict-card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-left: 5px solid #ef4444; }
    .conflict-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .title-group { display: flex; align-items: center; gap: 0.75rem; }
    .title-group h2 { font-size: 1.3rem; margin: 0; color: #0f172a; }
    .priority-tag { background: #fee2e2; color: #991b1b; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700; }
    .sku-tag { font-family: monospace; color: #64748b; font-size: 0.85rem; }
    .shortage-badge { background: #ef4444; color: white; padding: 0.4rem 0.8rem; border-radius: 20px; font-size: 0.85rem; font-weight: 700; }
    
    .conflict-body { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; padding: 1rem 0; border-top: 1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9; margin-bottom: 1rem; }
    .info-col { display: flex; flex-direction: column; }
    .info-col .label { font-size: 0.8rem; color: #64748b; font-weight: 500; }
    .info-col .value { font-weight: 700; font-size: 1.05rem; color: #0f172a; margin-top: 0.2rem; }
    .text-red { color: #ef4444; }

    .alternatives-section h3 { font-size: 0.95rem; margin: 0 0 0.75rem 0; color: #334155; }
    .alternatives-grid { display: flex; gap: 0.75rem; flex-wrap: wrap; }
    .alt-card { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0.5rem 0.8rem; display: flex; gap: 0.6rem; align-items: center; }
    .alt-name { font-weight: 600; font-size: 0.85rem; }
    .alt-avail { background: #dcfce7; color: #15803d; padding: 0.15rem 0.4rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700; }
    .no-alt { font-size: 0.85rem; color: #94a3b8; margin: 0; }
  `]
})
export class InventoryConflictsComponent implements OnInit {
  conflicts: InventoryConflict[] = [];

  constructor(private inventoryService: InventoryAvailabilityService) {}

  ngOnInit(): void {
    this.loadConflicts();
  }

  loadConflicts(): void {
    this.inventoryService.getConflicts().subscribe(c => {
      this.conflicts = c;
      this.conflicts.forEach(conf => {
        this.inventoryService.getProductAlternatives(conf.productId, conf.shortageQuantity).subscribe(alts => {
          conf.suggestedAlternatives = alts;
        });
      });
    });
  }
}
