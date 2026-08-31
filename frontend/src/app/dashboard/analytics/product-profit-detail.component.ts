import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AnalyticsService, ProductProfitDetail } from '../../services/analytics.service';

@Component({
  selector: 'app-product-profit-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> /
            <a routerLink="/dashboard/analytics/products">Product Margins</a> /
            <span>{{ detail?.sku || 'Detail' }}</span>
          </div>
          <h1 class="page-title">{{ detail?.name }}</h1>
          <p class="subtitle">SKU: {{ detail?.sku }} • Category: {{ detail?.categoryName }} • Tracking: {{ detail?.trackingType }}</p>
        </div>

        <div class="actions-bar">
          <a routerLink="/dashboard/analytics/products" class="btn btn-outline">
            ← Back to Products
          </a>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Calculating product fleet ROI & depreciation model...</p>
      </div>

      <div *ngIf="!loading && detail" class="detail-content">
        <!-- Overview Banner -->
        <div class="overview-banner">
          <div class="banner-stat">
            <span class="label">Rental Revenue</span>
            <span class="val text-primary">\${{ detail.rentalRevenue | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Total Costs (Direct + Repairs)</span>
            <span class="val text-muted">\${{ detail.totalCost | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Gross Profit</span>
            <span class="val font-bold" [ngClass]="detail.profit >= 0 ? 'text-success' : 'text-danger'">
              \${{ detail.profit | number:'1.2-2' }}
            </span>
          </div>
          <div class="banner-stat">
            <span class="label">Margin %</span>
            <span class="margin-badge" [ngClass]="detail.marginFlag.toLowerCase()">
              {{ detail.marginPercent | number:'1.1-1' }}%
            </span>
          </div>
        </div>

        <!-- Fleet Lifecycle Status Grid -->
        <div class="kpi-grid">
          <div class="kpi-card">
            <div class="kpi-label">Quantity Owned</div>
            <div class="kpi-val">{{ detail.quantityOwned }}</div>
            <div class="kpi-subtext">Total catalog fleet</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Units on Rent</div>
            <div class="kpi-val text-success">{{ detail.totalQuantityRented }}</div>
            <div class="kpi-subtext">Total units rented in period</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Fleet Utilization</div>
            <div class="kpi-val">{{ detail.utilizationPercent | number:'1.1-1' }}%</div>
            <div class="kpi-progress-bar">
              <div class="progress-fill" [style.width.%]="detail.utilizationPercent"></div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">In Maintenance / Damaged</div>
            <div class="kpi-val text-warning">{{ detail.quantityInMaintenance + detail.quantityDamaged }}</div>
            <div class="kpi-subtext">{{ detail.quantityInMaintenance }} maint / {{ detail.quantityDamaged }} dmg</div>
          </div>
        </div>

        <!-- 6-Month Monthly Trend Table -->
        <div class="card" *ngIf="detail.monthlyTrend?.length">
          <div class="card-header">
            <h3>Monthly Historical Rental Performance</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Period</th>
                  <th>Rentals Count</th>
                  <th>Rental Revenue</th>
                  <th>Estimated Gross Profit</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let m of detail.monthlyTrend">
                  <td class="font-medium">{{ m.period }}</td>
                  <td>{{ m.bookingsCount }}</td>
                  <td class="font-medium">\${{ m.bookedRevenue | number:'1.2-2' }}</td>
                  <td class="text-success font-medium">\${{ m.grossProfit | number:'1.2-2' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Cost Sources Explanation Table -->
        <div class="card">
          <div class="card-header">
            <h3>Authoritative Cost Source Explanations</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Cost Category</th>
                  <th>Amount</th>
                  <th>Source & Ledger Reference</th>
                  <th>Calculation Type</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let src of detail.costSources">
                  <td class="font-medium">{{ src.costCategory }}</td>
                  <td class="font-bold">\${{ src.amount | number:'1.2-2' }}</td>
                  <td>{{ src.source }}</td>
                  <td>
                    <span class="badge" [class.badge-success]="src.calculationType === 'ACTUAL'">
                      {{ src.calculationType }}
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
      text-decoration: none;
    }
    .btn-outline { background: #1e293b; color: #e2e8f0; border: 1px solid #334155; }
    .overview-banner {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.25rem;
      background: linear-gradient(135deg, #1e293b 0%, #111827 100%);
      border: 1px solid #334155;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }
    .banner-stat { display: flex; flex-direction: column; gap: 0.4rem; }
    .banner-stat .label { font-size: 0.8rem; color: #94a3b8; }
    .banner-stat .val { font-size: 1.6rem; font-weight: 700; }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .kpi-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .kpi-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
    .kpi-val { font-size: 1.5rem; font-weight: 700; color: #ffffff; margin-bottom: 0.35rem; }
    .kpi-subtext { font-size: 0.8rem; color: #64748b; }
    .kpi-progress-bar { height: 6px; background: #334155; border-radius: 3px; overflow: hidden; margin-top: 0.5rem; }
    .progress-fill { height: 100%; background: #3b82f6; border-radius: 3px; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem; }
    .card-header { margin-bottom: 1rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .font-bold { font-weight: 700; }
    .text-muted { color: #94a3b8; }
    .text-primary { color: #60a5fa; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-danger { color: #ef4444; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .badge-success { background: #10b98122; color: #10b981; }
    .margin-badge { font-weight: 600; padding: 0.3rem 0.6rem; border-radius: 4px; font-size: 0.85rem; display: inline-block; width: fit-content; }
    .margin-badge.high_margin { background: #10b98122; color: #10b981; }
    .margin-badge.healthy_margin { background: #3b82f622; color: #3b82f6; }
    .margin-badge.low_margin { background: #f59e0b22; color: #f59e0b; }
    .margin-badge.negative_margin { background: #ef444422; color: #ef4444; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class ProductProfitDetailComponent implements OnInit {
  productId = '';
  detail: ProductProfitDetail | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.productId = params['id'];
      if (this.productId) {
        this.loadDetail();
      }
    });
  }

  loadDetail(): void {
    this.loading = true;
    this.analyticsService.getProductProfitDetail(this.productId).subscribe({
      next: (data) => {
        this.detail = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
