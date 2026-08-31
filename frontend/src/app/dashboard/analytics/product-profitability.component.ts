import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, ProductProfitability, DateRangePreset } from '../../services/analytics.service';

@Component({
  selector: 'app-product-profitability',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Product Margins</span>
          </div>
          <h1 class="page-title">Product Profitability & Fleet Margins</h1>
          <p class="subtitle">Authoritative profitability, direct depreciation costs, maintenance expenses, and rental utilization per SKU.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadProducts()">
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadProducts()">
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
        <a routerLink="/dashboard/analytics/products" class="pill active">Product Margins</a>
        <a routerLink="/dashboard/analytics/utilization" class="pill">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill">Customer LTV</a>
        <a routerLink="/dashboard/analytics/quotes" class="pill">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill">Operations & AR</a>
      </div>

      <!-- Filter Controls -->
      <div class="filter-controls">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input
            type="text"
            [(ngModel)]="searchQuery"
            placeholder="Search by SKU or product name..."
          />
        </div>

        <div class="status-filter">
          <label>Category:</label>
          <select [(ngModel)]="selectedCategory">
            <option value="ALL">All Categories</option>
            <option *ngFor="let cat of categories" [value]="cat">{{ cat }}</option>
          </select>
        </div>

        <div class="status-filter">
          <label>Margin Tier:</label>
          <select [(ngModel)]="selectedMarginFilter">
            <option value="ALL">All Tiers</option>
            <option value="HIGH_MARGIN">High Margin (>= 50%)</option>
            <option value="HEALTHY_MARGIN">Healthy Margin (30-49%)</option>
            <option value="LOW_MARGIN">Low Margin (0-29%)</option>
            <option value="NEGATIVE_MARGIN">Negative Margin</option>
          </select>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading SKU-level margins & equipment maintenance costs...</p>
      </div>

      <!-- Table View -->
      <div *ngIf="!loading" class="card">
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Product / SKU</th>
                <th>Category</th>
                <th>Owned Units</th>
                <th>Rented Qty</th>
                <th>Utilization</th>
                <th>Rental Revenue</th>
                <th>Direct Cost</th>
                <th>Repair Cost</th>
                <th>Gross Profit</th>
                <th>Margin %</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let p of filteredProducts">
                <td>
                  <a [routerLink]="['/dashboard/analytics/products', p.productId]" class="prod-link">
                    <div class="font-medium">{{ p.name }}</div>
                    <div class="text-xs text-muted">{{ p.sku }}</div>
                  </a>
                </td>
                <td><span class="badge">{{ p.categoryName }}</span></td>
                <td>{{ p.quantityOwned }}</td>
                <td>{{ p.totalQuantityRented }}</td>
                <td>
                  <div class="util-cell">
                    <span>{{ p.utilizationPercent | number:'1.1-1' }}%</span>
                    <div class="mini-bar">
                      <div class="mini-fill" [style.width.%]="p.utilizationPercent"></div>
                    </div>
                  </div>
                </td>
                <td class="font-medium">\${{ p.rentalRevenue | number:'1.2-2' }}</td>
                <td class="text-muted">\${{ p.directCost | number:'1.2-2' }}</td>
                <td class="text-warning">\${{ p.repairCost | number:'1.2-2' }}</td>
                <td class="font-bold text-success">\${{ p.profit | number:'1.2-2' }}</td>
                <td>
                  <span class="margin-badge" [ngClass]="p.marginFlag.toLowerCase()">
                    {{ p.marginPercent | number:'1.1-1' }}%
                  </span>
                </td>
                <td>
                  <a [routerLink]="['/dashboard/analytics/products', p.productId]" class="btn-sm btn-outline">
                    ROI Detail →
                  </a>
                </td>
              </tr>
              <tr *ngIf="filteredProducts.length === 0">
                <td colspan="11" class="empty-state">No products found matching the selected filters.</td>
              </tr>
            </tbody>
          </table>
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
    .filter-controls { display: flex; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap; align-items: center; }
    .search-box {
      display: flex;
      align-items: center;
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 0.4rem 0.8rem;
      flex: 1;
      min-width: 250px;
    }
    .search-icon { margin-right: 0.5rem; }
    .search-box input { background: transparent; border: none; color: white; outline: none; width: 100%; font-size: 0.85rem; }
    .status-filter { display: flex; align-items: center; gap: 0.5rem; background: #1e293b; border: 1px solid #334155; border-radius: 8px; padding: 0.4rem 0.8rem; font-size: 0.85rem; color: #94a3b8; }
    .status-filter select { background: #0f172a; color: white; border: 1px solid #475569; padding: 0.3rem 0.6rem; border-radius: 6px; font-size: 0.85rem; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
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
    .util-cell { display: flex; flex-direction: column; gap: 0.2rem; min-width: 80px; }
    .mini-bar { height: 4px; background: #334155; border-radius: 2px; overflow: hidden; }
    .mini-fill { height: 100%; background: #3b82f6; border-radius: 2px; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .margin-badge { font-weight: 600; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .margin-badge.high_margin { background: #10b98122; color: #10b981; }
    .margin-badge.healthy_margin { background: #3b82f622; color: #3b82f6; }
    .margin-badge.low_margin { background: #f59e0b22; color: #f59e0b; }
    .margin-badge.negative_margin { background: #ef444422; color: #ef4444; }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 6px; font-size: 0.75rem; text-decoration: none; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class ProductProfitabilityComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  products: ProductProfitability[] = [];
  categories: string[] = [];
  searchQuery = '';
  selectedCategory = 'ALL';
  selectedMarginFilter = 'ALL';
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.analyticsService.getProductProfitability(this.selectedRange).subscribe({
      next: (data) => {
        this.products = data || [];
        const catSet = new Set<string>();
        this.products.forEach((p) => {
          if (p.categoryName) catSet.add(p.categoryName);
        });
        this.categories = Array.from(catSet);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredProducts(): ProductProfitability[] {
    return this.products.filter((p) => {
      const matchesSearch =
        !this.searchQuery ||
        p.name?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        p.sku?.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesCat =
        this.selectedCategory === 'ALL' || p.categoryName === this.selectedCategory;

      const matchesMargin =
        this.selectedMarginFilter === 'ALL' || p.marginFlag === this.selectedMarginFilter;

      return matchesSearch && matchesCat && matchesMargin;
    });
  }

  exportCsv(): void {
    this.analyticsService.exportReportCsv('products', this.selectedRange).subscribe((blob) => {
      this.analyticsService.downloadCsvBlob(blob, `product-margins-${this.selectedRange.toLowerCase()}.csv`);
    });
  }
}
