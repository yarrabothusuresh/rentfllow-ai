import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, InventoryUtilization, DateRangePreset } from '../../services/analytics.service';

@Component({
  selector: 'app-utilization-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Fleet Utilization</span>
          </div>
          <h1 class="page-title">Inventory & Fleet Utilization</h1>
          <p class="subtitle">Asset deployment efficiency, capacity constraints, underutilized equipment capital, and expansion opportunities.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadUtilization()">
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadUtilization()">
            <span class="icon">🔄</span> Refresh
          </button>

          <button class="btn btn-primary" (click)="exportCsv()">
            <span class="icon">📥</span> Export CSV
          </button>
        </div>
      </div>

      <!-- Sub-Nav Pills -->
      <div class="sub-nav-pills">
        <a routerLink="/dashboard/analytics/dashboard" class="pill">Executive BI</a>
        <a routerLink="/dashboard/analytics/revenue" class="pill">Revenue & Cashflow</a>
        <a routerLink="/dashboard/analytics/bookings" class="pill">Booking Profitability</a>
        <a routerLink="/dashboard/analytics/products" class="pill">Product Margins</a>
        <a routerLink="/dashboard/analytics/utilization" class="pill active">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill">Customer LTV</a>
        <a routerLink="/dashboard/analytics/quotes" class="pill">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill">Operations & AR</a>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Analyzing serialized and bulk inventory utilization...</p>
      </div>

      <div *ngIf="!loading && utilData" class="util-content">
        <!-- Top Stats Row -->
        <div class="kpi-grid">
          <div class="kpi-card highlight-card">
            <div class="kpi-label">Overall Fleet Utilization</div>
            <div class="kpi-val text-primary">{{ utilData.overallUtilizationPercent | number:'1.1-1' }}%</div>
            <div class="kpi-subtext">Target: {{ utilData.targetUtilizationPercent }}%</div>
            <div class="kpi-progress-bar">
              <div class="progress-fill" [style.width.%]="utilData.overallUtilizationPercent"></div>
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Total Fleet Units</div>
            <div class="kpi-val">{{ utilData.totalFleetUnits }}</div>
            <div class="kpi-subtext">Across {{ utilData.totalTrackedProducts }} catalog items</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Units on Rent</div>
            <div class="kpi-val text-success">{{ utilData.unitsOnRent }}</div>
            <div class="kpi-subtext">Active bookings in period</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Unavailable (Maint / Dmg)</div>
            <div class="kpi-val text-warning">{{ utilData.unitsInMaintenance + utilData.unitsDamaged }}</div>
            <div class="kpi-subtext">{{ utilData.unitsInMaintenance }} in maint • {{ utilData.unitsDamaged }} damaged</div>
          </div>
        </div>

        <!-- High Demand / Capacity Constraints Section -->
        <div class="card opportunity-card" *ngIf="utilData.highDemandConflictProducts?.length">
          <div class="card-header">
            <h3>🚨 High-Demand Fleet Constraints & Expansion Opportunities</h3>
            <span class="badge badge-success">Revenue Opportunity</span>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Product / SKU</th>
                  <th>Category</th>
                  <th>Current Utilization</th>
                  <th>Availability Conflicts</th>
                  <th>Unmet Quantity</th>
                  <th>Potential Revenue Opportunity</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let c of utilData.highDemandConflictProducts">
                  <td>
                    <a [routerLink]="['/dashboard/analytics/products', c.productId]" class="prod-link">
                      <div class="font-medium">{{ c.name }}</div>
                      <div class="text-xs text-muted">{{ c.sku }}</div>
                    </a>
                  </td>
                  <td><span class="badge">{{ c.categoryName }}</span></td>
                  <td class="font-bold text-danger">{{ c.currentUtilizationPercent | number:'1.1-1' }}%</td>
                  <td>{{ c.conflictIncidentsCount }} incidents</td>
                  <td>{{ c.unmetQuantityRequested }} units</td>
                  <td class="font-bold text-success">+\${{ c.potentialRevenueOpportunity | number:'1.2-2' }}</td>
                  <td><span class="badge badge-warning">{{ c.statusTag }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Underutilized Fleet Assets Section -->
        <div class="card underutilized-card">
          <div class="card-header">
            <h3>⚠️ Underutilized Inventory Assets (Utilization &lt; 25%)</h3>
            <span class="badge badge-warning">Capital Optimization</span>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Product / SKU</th>
                  <th>Category</th>
                  <th>Owned Units</th>
                  <th>Inventory Capital Tied Up</th>
                  <th>Utilization</th>
                  <th>Rental Revenue</th>
                  <th>Suggested Optimization Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let u of utilData.underutilizedProducts">
                  <td>
                    <a [routerLink]="['/dashboard/analytics/products', u.productId]" class="prod-link">
                      <div class="font-medium">{{ u.name }}</div>
                      <div class="text-xs text-muted">{{ u.sku }}</div>
                    </a>
                  </td>
                  <td><span class="badge">{{ u.categoryName }}</span></td>
                  <td>{{ u.quantityOwned }}</td>
                  <td class="font-medium text-warning">\${{ u.inventoryAssetValue | number:'1.2-2' }}</td>
                  <td class="text-danger font-bold">{{ u.utilizationPercent | number:'1.1-1' }}%</td>
                  <td>\${{ u.rentalRevenue | number:'1.2-2' }}</td>
                  <td><span class="suggested-action">{{ u.suggestedAction }}</span></td>
                </tr>
                <tr *ngIf="utilData.underutilizedProducts.length === 0">
                  <td colspan="7" class="empty-state">No underutilized products detected for this period.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Top Utilized Assets -->
        <div class="card">
          <div class="card-header">
            <h3>Top Utilized Fleet Assets</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Category</th>
                  <th>Owned</th>
                  <th>Rented Qty</th>
                  <th>Utilization</th>
                  <th>Rental Revenue</th>
                  <th>Margin %</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let top of utilData.topUtilizedProducts">
                  <td>
                    <a [routerLink]="['/dashboard/analytics/products', top.productId]" class="prod-link">
                      <div class="font-medium">{{ top.name }}</div>
                      <div class="text-xs text-muted">{{ top.sku }}</div>
                    </a>
                  </td>
                  <td><span class="badge">{{ top.categoryName }}</span></td>
                  <td>{{ top.quantityOwned }}</td>
                  <td>{{ top.totalQuantityRented }}</td>
                  <td class="font-bold text-success">{{ top.utilizationPercent | number:'1.1-1' }}%</td>
                  <td>\${{ top.rentalRevenue | number:'1.2-2' }}</td>
                  <td>
                    <span class="margin-badge" [ngClass]="top.marginFlag.toLowerCase()">
                      {{ top.marginPercent | number:'1.1-1' }}%
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .analytics-container {
      padding: 1.5rem 2rem;
      color: #f1f5f9;
      background-color: #0f172a;
      min-height: 100vh;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    }
    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .breadcrumbs { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.25rem; }
    .breadcrumbs a { color: #60a5fa; text-decoration: none; }
    .page-title { font-size: 1.75rem; font-weight: 700; color: #ffffff; margin: 0 0 0.25rem 0; }
    .subtitle { font-size: 0.9rem; color: #94a3b8; margin: 0; }
    .actions-bar { display: flex; align-items: center; gap: 0.75rem; }
    .range-selector {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: #1e293b;
      padding: 0.4rem 0.75rem;
      border-radius: 8px;
      border: 1px solid #334155;
    }
    .range-selector label { font-size: 0.85rem; color: #94a3b8; }
    .range-selector select {
      background: #0f172a;
      color: #f8fafc;
      border: 1px solid #475569;
      padding: 0.35rem 0.6rem;
      border-radius: 6px;
      font-size: 0.85rem;
    }
    .btn {
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      border: none;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-outline { background: #1e293b; color: #e2e8f0; border: 1px solid #334155; }
    .sub-nav-pills {
      display: flex;
      gap: 0.5rem;
      overflow-x: auto;
      padding-bottom: 0.75rem;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #1e293b;
    }
    .pill {
      padding: 0.45rem 0.9rem;
      border-radius: 20px;
      font-size: 0.85rem;
      background: #1e293b;
      color: #94a3b8;
      text-decoration: none;
      white-space: nowrap;
    }
    .pill.active { background: #2563eb; color: white; font-weight: 600; }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .kpi-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .kpi-card.highlight-card {
      background: linear-gradient(135deg, #1e293b 0%, #1e1b4b 100%);
      border: 1px solid #6366f144;
    }
    .kpi-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
    .kpi-val { font-size: 1.65rem; font-weight: 700; color: #ffffff; margin-bottom: 0.35rem; }
    .kpi-subtext { font-size: 0.8rem; color: #64748b; }
    .kpi-progress-bar { height: 6px; background: #334155; border-radius: 3px; overflow: hidden; margin-top: 0.5rem; }
    .progress-fill { height: 100%; background: #3b82f6; border-radius: 3px; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem; }
    .opportunity-card { border: 1px solid #10b98144; background: linear-gradient(135deg, #1e293b 0%, #064e3b22 100%); }
    .underutilized-card { border: 1px solid #f59e0b44; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .prod-link { text-decoration: none; color: inherit; }
    .prod-link:hover .font-medium { color: #60a5fa; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .font-bold { font-weight: 700; }
    .text-xs { font-size: 0.75rem; }
    .text-muted { color: #64748b; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-danger { color: #ef4444; }
    .text-primary { color: #60a5fa; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .badge-success { background: #10b98122; color: #10b981; }
    .badge-warning { background: #f59e0b22; color: #f59e0b; }
    .suggested-action { font-size: 0.8rem; color: #60a5fa; background: #1e3a8a33; padding: 0.2rem 0.5rem; border-radius: 4px; }
    .margin-badge { font-weight: 600; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .margin-badge.high_margin { background: #10b98122; color: #10b981; }
    .margin-badge.healthy_margin { background: #3b82f622; color: #3b82f6; }
    .margin-badge.low_margin { background: #f59e0b22; color: #f59e0b; }
    .margin-badge.negative_margin { background: #ef444422; color: #ef4444; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class UtilizationAnalyticsComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  utilData: InventoryUtilization | null = null;
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadUtilization();
  }

  loadUtilization(): void {
    this.loading = true;
    this.analyticsService.getInventoryUtilization(this.selectedRange).subscribe({
      next: (data) => {
        this.utilData = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  exportCsv(): void {
    this.analyticsService.exportReportCsv('utilization', this.selectedRange).subscribe((blob) => {
      this.analyticsService.downloadCsvBlob(blob, `fleet-utilization-${this.selectedRange.toLowerCase()}.csv`);
    });
  }
}
