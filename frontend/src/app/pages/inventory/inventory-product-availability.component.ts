import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { InventoryAvailabilityService, AvailabilityResult } from '../../services/inventory-availability.service';
import { CatalogService } from '../../services/catalog.service';

@Component({
  selector: 'app-inventory-product-availability',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="product-availability-container">
      <header class="page-header">
        <div>
          <h1>📊 Product Availability Timeline Grid</h1>
          <p class="subtitle">{{ product?.name }} (SKU: {{ product?.sku }})</p>
        </div>
        <a routerLink="/inventory/availability" class="btn btn-secondary">&larr; Back to Calendar</a>
      </header>

      <!-- Summary Banner -->
      <div class="summary-card">
        <div class="summary-item">
          <span class="label">Total Owned</span>
          <span class="val">{{ product?.quantityOwned || 0 }}</span>
        </div>
        <div class="summary-item">
          <span class="label">In Maintenance</span>
          <span class="val text-purple">{{ product?.quantityInMaintenance || 0 }}</span>
        </div>
        <div class="summary-item">
          <span class="label">Damaged / Lost</span>
          <span class="val text-red">{{ (product?.quantityDamaged || 0) + (product?.quantityLost || 0) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">Base Available</span>
          <span class="val text-green">{{ (product?.quantityOwned || 0) - (product?.quantityInMaintenance || 0) - (product?.quantityDamaged || 0) - (product?.quantityLost || 0) }}</span>
        </div>
      </div>

      <!-- 7-Day Availability Timeline Grid -->
      <div class="grid-card">
        <h2>7-Day Projected Availability</h2>
        <div class="timeline-grid">
          <div class="day-card" *ngFor="let item of timeline">
            <div class="day-header">{{ item.startDateTime | date:'EEE, MMM d' }}</div>
            <div class="day-metrics">
              <div class="metric-row">
                <span>Reserved:</span>
                <span class="font-bold text-amber">{{ item.quantityReserved }}</span>
              </div>
              <div class="metric-row">
                <span>Available:</span>
                <span class="font-bold text-green">{{ item.availableQuantity }}</span>
              </div>
            </div>
            <div class="day-status" [class.status-ok]="item.available" [class.status-short]="!item.available">
              {{ item.available ? 'OK (' + item.availableQuantity + ' Left)' : 'SHORTAGE' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-availability-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h1 { font-size: 1.8rem; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; }
    .btn { padding: 0.6rem 1.2rem; border-radius: 6px; font-weight: 600; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
    .btn-secondary { background: #64748b; color: white; }
    
    .summary-card { background: white; border-radius: 10px; padding: 1.25rem 2rem; display: flex; justify-content: space-between; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 1.5rem; }
    .summary-item { display: flex; flex-direction: column; align-items: center; }
    .summary-item .label { font-size: 0.85rem; color: #64748b; font-weight: 500; }
    .summary-item .val { font-size: 1.5rem; font-weight: 700; color: #0f172a; margin-top: 0.2rem; }
    .text-purple { color: #8b5cf6; }
    .text-red { color: #ef4444; }
    .text-green { color: #16a34a; }
    .text-amber { color: #d97706; }
    
    .grid-card { background: white; border-radius: 10px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
    .grid-card h2 { font-size: 1.2rem; margin-top: 0; margin-bottom: 1.25rem; color: #0f172a; }
    .timeline-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem; }
    
    .day-card { border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; background: #f8fafc; text-align: center; }
    .day-header { background: #e2e8f0; padding: 0.6rem; font-weight: 700; color: #334155; font-size: 0.9rem; }
    .day-metrics { padding: 0.8rem; display: flex; flex-direction: column; gap: 0.4rem; font-size: 0.85rem; }
    .metric-row { display: flex; justify-content: space-between; }
    .font-bold { font-weight: 700; }
    
    .day-status { padding: 0.4rem; font-size: 0.8rem; font-weight: 700; text-transform: uppercase; }
    .status-ok { background: #dcfce7; color: #15803d; }
    .status-short { background: #fee2e2; color: #b91c1c; }
  `]
})
export class InventoryProductAvailabilityComponent implements OnInit {
  productId: string = '';
  product: any = null;
  timeline: AvailabilityResult[] = [];

  constructor(
    private route: ActivatedRoute,
    private inventoryService: InventoryAvailabilityService,
    private catalogService: CatalogService
  ) {}

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('id') || '';
    if (this.productId) {
      this.catalogService.getProductById(this.productId).subscribe((p: any) => this.product = p);
      this.inventoryService.getProductAvailabilityTimeline(this.productId, 7).subscribe(t => this.timeline = t);
    }
  }
}
