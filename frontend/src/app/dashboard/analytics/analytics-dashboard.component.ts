import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, ExecutiveDashboard, DateRangePreset, BusinessInsight } from '../../services/analytics.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <!-- Top Navigation & Header -->
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard">Dashboard</a> / <span>Executive Analytics & BI</span>
          </div>
          <h1 class="page-title">Executive Business Intelligence</h1>
          <p class="subtitle">Authoritative financial metrics, fleet utilization, sales conversion, and gross margin intelligence.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="onRangeChange()">
              <option value="TODAY">Today</option>
              <option value="YESTERDAY">Yesterday</option>
              <option value="THIS_WEEK">This Week</option>
              <option value="LAST_WEEK">Last Week</option>
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
              <option value="CUSTOM">Custom Range</option>
            </select>
          </div>

          <div *ngIf="selectedRange === 'CUSTOM'" class="custom-dates">
            <input type="date" [(ngModel)]="customStartDate" (change)="loadDashboard()" />
            <span>to</span>
            <input type="date" [(ngModel)]="customEndDate" (change)="loadDashboard()" />
          </div>

          <button class="btn btn-outline" (click)="loadDashboard()">
            <span class="icon">🔄</span> Refresh
          </button>

          <button class="btn btn-primary" (click)="exportExecutiveCsv()">
            <span class="icon">📥</span> Export CSV
          </button>
        </div>
      </div>

      <!-- Quick Sub-Navigation Pills -->
      <div class="sub-nav-pills">
        <a routerLink="/dashboard/analytics/dashboard" class="pill active">Executive BI</a>
        <a routerLink="/dashboard/analytics/revenue" class="pill">Revenue & Cashflow</a>
        <a routerLink="/dashboard/analytics/bookings" class="pill">Booking Profitability</a>
        <a routerLink="/dashboard/analytics/products" class="pill">Product Margins</a>
        <a routerLink="/dashboard/analytics/utilization" class="pill">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill">Customer LTV</a>
        <a routerLink="/dashboard/analytics/quotes" class="pill">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill">Operations & AR</a>
      </div>

      <!-- Loading / Error States -->
      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Calculating authoritative analytics from operational transactions...</p>
      </div>

      <div *ngIf="error" class="alert alert-danger">
        <span class="alert-icon">⚠️</span>
        <div>{{ error }}</div>
      </div>

      <div *ngIf="!loading && dashboard" class="dashboard-content">
        <!-- Business Insights & AI Alerts -->
        <div *ngIf="insights.length > 0" class="insights-banner">
          <div class="insight-header">
            <span class="sparkle-icon">✨</span>
            <h3>Operational & Margin Optimization Alerts</h3>
          </div>
          <div class="insight-grid">
            <div *ngFor="let ins of insights" class="insight-card" [ngClass]="ins.severity.toLowerCase()">
              <div class="insight-card-top">
                <span class="insight-badge" [ngClass]="ins.severity.toLowerCase()">{{ ins.severity }}</span>
                <span class="insight-category">{{ ins.category }}</span>
              </div>
              <h4>{{ ins.title }}</h4>
              <p>{{ ins.summary }}</p>
              <div class="insight-impact" *ngIf="ins.financialImpact > 0">
                Financial Impact: <strong>\${{ ins.financialImpact | number:'1.0-0' }}</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- KPI Primary Grid -->
        <div class="kpi-grid">
          <!-- Booked Revenue -->
          <div class="kpi-card">
            <div class="kpi-label">Booked Revenue</div>
            <div class="kpi-val">{{ dashboard.bookedRevenue?.formattedValue || '$0' }}</div>
            <div class="kpi-trend" [ngClass]="dashboard.bookedRevenue?.trend?.toLowerCase()">
              <span *ngIf="dashboard.bookedRevenue?.trend === 'UP'">↑ +{{ dashboard.bookedRevenue?.changePercentage }}%</span>
              <span *ngIf="dashboard.bookedRevenue?.trend === 'DOWN'">↓ {{ dashboard.bookedRevenue?.changePercentage }}%</span>
              <span *ngIf="dashboard.bookedRevenue?.trend === 'FLAT'">→ 0.0%</span>
              <span class="kpi-period-label">vs previous</span>
            </div>
          </div>

          <!-- Collected Revenue -->
          <div class="kpi-card">
            <div class="kpi-label">Collected Cash</div>
            <div class="kpi-val text-success">{{ dashboard.collectedRevenue?.formattedValue || '$0' }}</div>
            <div class="kpi-trend" [ngClass]="dashboard.collectedRevenue?.trend?.toLowerCase()">
              <span *ngIf="dashboard.collectedRevenue?.trend === 'UP'">↑ +{{ dashboard.collectedRevenue?.changePercentage }}%</span>
              <span *ngIf="dashboard.collectedRevenue?.trend === 'DOWN'">↓ {{ dashboard.collectedRevenue?.changePercentage }}%</span>
              <span class="kpi-period-label">payments received</span>
            </div>
          </div>

          <!-- Outstanding Balance -->
          <div class="kpi-card">
            <div class="kpi-label">Outstanding Receivables</div>
            <div class="kpi-val text-warning">{{ dashboard.outstandingRevenue?.formattedValue || '$0' }}</div>
            <div class="kpi-subtext">Unpaid invoice balances</div>
          </div>

          <!-- Gross Profit -->
          <div class="kpi-card highlight-card">
            <div class="kpi-label">Gross Profit</div>
            <div class="kpi-val text-primary">{{ dashboard.grossProfit?.formattedValue || '$0' }}</div>
            <div class="kpi-trend" [ngClass]="dashboard.grossProfit?.trend?.toLowerCase()">
              <span>Margin: <strong>{{ dashboard.grossMargin?.formattedValue || '0.0%' }}</strong></span>
            </div>
          </div>

          <!-- Average Booking Value -->
          <div class="kpi-card">
            <div class="kpi-label">Avg Booking Value</div>
            <div class="kpi-val">{{ dashboard.averageBookingValue?.formattedValue || '$0' }}</div>
            <div class="kpi-subtext">{{ dashboard.bookingsCount?.value || 0 }} total bookings</div>
          </div>

          <!-- Fleet Utilization -->
          <div class="kpi-card">
            <div class="kpi-label">Fleet Utilization</div>
            <div class="kpi-val">{{ dashboard.inventoryUtilization?.formattedValue || '0.0%' }}</div>
            <div class="kpi-progress-bar">
              <div class="progress-fill" [style.width.%]="dashboard.inventoryUtilization?.value || 0"></div>
            </div>
          </div>

          <!-- Quote Conversion -->
          <div class="kpi-card">
            <div class="kpi-label">Quote Conversion Rate</div>
            <div class="kpi-val">{{ dashboard.quoteConversionRate?.formattedValue || '0.0%' }}</div>
            <div class="kpi-subtext">Sent to Confirmed %</div>
          </div>

          <!-- Open Damage Exposure -->
          <div class="kpi-card">
            <div class="kpi-label">Open Damage Exposure</div>
            <div class="kpi-val text-danger">{{ dashboard.openDamageExposure?.formattedValue || '$0' }}</div>
            <div class="kpi-subtext">Pending claim investigations</div>
          </div>
        </div>

        <!-- Trend & Performance Charts Section -->
        <div class="charts-row">
          <!-- 6-Month Monthly Revenue & Profit Trend Chart -->
          <div class="card chart-card">
            <div class="card-header">
              <h3>6-Month Revenue & Gross Profit Trend</h3>
              <span class="badge">Booked vs Profit</span>
            </div>
            <div class="trend-chart-container">
              <div class="svg-container">
                <svg viewBox="0 0 600 220" class="trend-svg">
                  <!-- Grid lines -->
                  <line x1="50" y1="20" x2="570" y2="20" stroke="#2d3748" stroke-dasharray="3" />
                  <line x1="50" y1="70" x2="570" y2="70" stroke="#2d3748" stroke-dasharray="3" />
                  <line x1="50" y1="120" x2="570" y2="120" stroke="#2d3748" stroke-dasharray="3" />
                  <line x1="50" y1="170" x2="570" y2="170" stroke="#2d3748" />

                  <!-- Revenue bars -->
                  <g *ngFor="let item of dashboard.revenueTrend; let i = index">
                    <!-- Revenue Bar -->
                    <rect
                      [attr.x]="80 + i * 85"
                      [attr.y]="170 - getBarHeight(item.bookedRevenue)"
                      width="30"
                      [attr.height]="getBarHeight(item.bookedRevenue)"
                      fill="#6366f1"
                      rx="4"
                      class="bar-rect"
                    />
                    <!-- Profit Bar -->
                    <rect
                      [attr.x]="115 + i * 85"
                      [attr.y]="170 - getBarHeight(item.grossProfit)"
                      width="18"
                      [attr.height]="getBarHeight(item.grossProfit)"
                      fill="#10b981"
                      rx="3"
                      class="bar-rect profit-bar"
                    />
                    <!-- Month Label -->
                    <text
                      [attr.x]="105 + i * 85"
                      y="195"
                      text-anchor="middle"
                      fill="#94a3b8"
                      font-size="12"
                    >{{ item.period }}</text>
                  </g>
                </svg>
              </div>

              <!-- Legend -->
              <div class="chart-legend">
                <div class="legend-item"><span class="color-dot revenue-dot"></span> Booked Revenue</div>
                <div class="legend-item"><span class="color-dot profit-dot"></span> Gross Profit</div>
              </div>
            </div>
          </div>

          <!-- Revenue by Category Breakdown -->
          <div class="card category-card">
            <div class="card-header">
              <h3>Revenue & Margin by Category</h3>
              <a routerLink="/dashboard/analytics/utilization" class="btn-link">View All</a>
            </div>
            <div class="category-list">
              <div *ngFor="let cat of dashboard.revenueByCategory" class="category-item">
                <div class="cat-info">
                  <span class="cat-name">{{ cat.categoryName }}</span>
                  <span class="cat-rev">\${{ cat.revenue | number:'1.0-0' }}</span>
                </div>
                <div class="cat-progress">
                  <div class="cat-bar" [style.width.%]="cat.utilizationPercent"></div>
                </div>
                <div class="cat-meta">
                  <span>Margin: <strong>{{ cat.marginPercent | number:'1.1-1' }}%</strong></span>
                  <span>Util: <strong>{{ cat.utilizationPercent | number:'1.1-1' }}%</strong></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Top Products & Top Customers Row -->
        <div class="tables-row">
          <!-- Top Profitable Products -->
          <div class="card">
            <div class="card-header">
              <h3>Top Profitable Products</h3>
              <a routerLink="/dashboard/analytics/products" class="btn-link">All Products</a>
            </div>
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Revenue</th>
                    <th>Gross Profit</th>
                    <th>Margin</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let prod of dashboard.topProfitableProducts">
                    <td>
                      <a [routerLink]="['/dashboard/analytics/products', prod.productId]" class="item-title-link">
                        <div class="font-medium">{{ prod.name }}</div>
                        <div class="text-xs text-muted">{{ prod.sku }}</div>
                      </a>
                    </td>
                    <td><span class="badge">{{ prod.categoryName }}</span></td>
                    <td>\${{ prod.rentalRevenue | number:'1.0-0' }}</td>
                    <td class="text-success font-medium">\${{ prod.profit | number:'1.0-0' }}</td>
                    <td>
                      <span class="margin-badge" [ngClass]="prod.marginFlag.toLowerCase()">
                        {{ prod.marginPercent | number:'1.1-1' }}%
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Top Customers by Lifetime Value -->
          <div class="card">
            <div class="card-header">
              <h3>Top Customers by Lifetime Value</h3>
              <a routerLink="/dashboard/analytics/customers" class="btn-link">All Customers</a>
            </div>
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Customer</th>
                    <th>Bookings</th>
                    <th>Lifetime Revenue</th>
                    <th>Collected</th>
                    <th>LTV Margin</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let cust of dashboard.topCustomers">
                    <td>
                      <a [routerLink]="['/dashboard/analytics/customers', cust.customerId]" class="item-title-link">
                        <div class="font-medium">{{ cust.companyName || cust.customerName }}</div>
                        <div class="text-xs text-muted">{{ cust.customerNumber }}</div>
                      </a>
                    </td>
                    <td>{{ cust.totalBookingsCount }}</td>
                    <td class="font-medium">\${{ cust.lifetimeRevenue | number:'1.0-0' }}</td>
                    <td class="text-success">\${{ cust.lifetimeCollected | number:'1.0-0' }}</td>
                    <td>
                      <span class="margin-badge high_margin">
                        {{ cust.lifetimeMarginPercent | number:'1.1-1' }}%
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Data Transparency Disclaimer -->
        <div class="transparency-note" *ngIf="dashboard.dataQualityWarnings?.length">
          <span class="note-icon">ℹ️</span>
          <div>
            <strong>Operational Intelligence Transparency:</strong>
            <span *ngFor="let warn of dashboard.dataQualityWarnings; let last = last">
              {{ warn }} {{ last ? '' : '• ' }}
            </span>
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
    .breadcrumbs {
      font-size: 0.85rem;
      color: #94a3b8;
      margin-bottom: 0.25rem;
    }
    .breadcrumbs a {
      color: #60a5fa;
      text-decoration: none;
    }
    .page-title {
      font-size: 1.75rem;
      font-weight: 700;
      color: #ffffff;
      margin: 0 0 0.25rem 0;
    }
    .subtitle {
      font-size: 0.9rem;
      color: #94a3b8;
      margin: 0;
    }
    .actions-bar {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .range-selector {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: #1e293b;
      padding: 0.4rem 0.75rem;
      border-radius: 8px;
      border: 1px solid #334155;
    }
    .range-selector label {
      font-size: 0.85rem;
      color: #94a3b8;
    }
    .range-selector select, .custom-dates input {
      background: #0f172a;
      color: #f8fafc;
      border: 1px solid #475569;
      padding: 0.35rem 0.6rem;
      border-radius: 6px;
      font-size: 0.85rem;
      outline: none;
    }
    .custom-dates {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.85rem;
      color: #94a3b8;
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
      transition: all 0.2s;
    }
    .btn-primary {
      background: #3b82f6;
      color: white;
    }
    .btn-primary:hover {
      background: #2563eb;
    }
    .btn-outline {
      background: #1e293b;
      color: #e2e8f0;
      border: 1px solid #334155;
    }
    .btn-outline:hover {
      background: #334155;
    }
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
      transition: all 0.2s;
    }
    .pill:hover {
      color: #f8fafc;
      background: #334155;
    }
    .pill.active {
      background: #2563eb;
      color: white;
      font-weight: 600;
    }
    .insights-banner {
      background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(15, 23, 42, 0.9) 100%);
      border: 1px solid #3b82f633;
      border-radius: 12px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .insight-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }
    .insight-header h3 {
      font-size: 1.05rem;
      margin: 0;
      color: #60a5fa;
    }
    .insight-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
    }
    .insight-card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 1rem;
      position: relative;
    }
    .insight-card.critical { border-left: 4px solid #ef4444; }
    .insight-card.warning { border-left: 4px solid #f59e0b; }
    .insight-card.opportunity { border-left: 4px solid #10b981; }
    .insight-card.info { border-left: 4px solid #3b82f6; }
    .insight-card-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }
    .insight-badge {
      font-size: 0.7rem;
      font-weight: 700;
      padding: 0.15rem 0.4rem;
      border-radius: 4px;
      text-transform: uppercase;
    }
    .insight-badge.critical { background: #ef444422; color: #ef4444; }
    .insight-badge.warning { background: #f59e0b22; color: #f59e0b; }
    .insight-badge.opportunity { background: #10b98122; color: #10b981; }
    .insight-category {
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .insight-card h4 {
      margin: 0 0 0.35rem 0;
      font-size: 0.95rem;
      color: #f8fafc;
    }
    .insight-card p {
      margin: 0 0 0.5rem 0;
      font-size: 0.85rem;
      color: #cbd5e1;
      line-height: 1.4;
    }
    .insight-impact {
      font-size: 0.8rem;
      color: #94a3b8;
    }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .kpi-card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .kpi-card.highlight-card {
      background: linear-gradient(135deg, #1e293b 0%, #1e1b4b 100%);
      border: 1px solid #6366f144;
    }
    .kpi-label {
      font-size: 0.85rem;
      color: #94a3b8;
      margin-bottom: 0.5rem;
    }
    .kpi-val {
      font-size: 1.65rem;
      font-weight: 700;
      color: #ffffff;
      margin-bottom: 0.35rem;
    }
    .kpi-trend {
      font-size: 0.8rem;
      font-weight: 600;
    }
    .kpi-trend.up { color: #10b981; }
    .kpi-trend.down { color: #ef4444; }
    .kpi-trend.flat { color: #94a3b8; }
    .kpi-period-label {
      font-weight: 400;
      color: #64748b;
      margin-left: 0.3rem;
    }
    .kpi-subtext {
      font-size: 0.8rem;
      color: #64748b;
    }
    .kpi-progress-bar {
      height: 6px;
      background: #334155;
      border-radius: 3px;
      overflow: hidden;
      margin-top: 0.5rem;
    }
    .progress-fill {
      height: 100%;
      background: #3b82f6;
      border-radius: 3px;
    }
    .charts-row, .tables-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 10px;
      padding: 1.25rem;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    .card-header h3 {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0;
      color: #f8fafc;
    }
    .btn-link {
      color: #60a5fa;
      text-decoration: none;
      font-size: 0.85rem;
    }
    .btn-link:hover {
      text-decoration: underline;
    }
    .trend-chart-container {
      position: relative;
    }
    .svg-container {
      width: 100%;
      overflow-x: auto;
    }
    .trend-svg {
      width: 100%;
      height: auto;
    }
    .bar-rect {
      transition: opacity 0.2s;
      cursor: pointer;
    }
    .bar-rect:hover {
      opacity: 0.8;
    }
    .chart-legend {
      display: flex;
      justify-content: center;
      gap: 1.5rem;
      margin-top: 0.75rem;
    }
    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.8rem;
      color: #94a3b8;
    }
    .color-dot {
      width: 10px;
      height: 10px;
      border-radius: 2px;
    }
    .revenue-dot { background: #6366f1; }
    .profit-dot { background: #10b981; }
    .category-list {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
    }
    .category-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .cat-info {
      display: flex;
      justify-content: space-between;
      font-size: 0.85rem;
      font-weight: 500;
    }
    .cat-progress {
      height: 6px;
      background: #334155;
      border-radius: 3px;
      overflow: hidden;
    }
    .cat-bar {
      height: 100%;
      background: #3b82f6;
      border-radius: 3px;
    }
    .cat-meta {
      display: flex;
      justify-content: space-between;
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .table-responsive {
      overflow-x: auto;
    }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
      text-align: left;
    }
    .data-table th {
      color: #94a3b8;
      font-weight: 600;
      padding: 0.6rem 0.75rem;
      border-bottom: 1px solid #334155;
    }
    .data-table td {
      padding: 0.75rem;
      border-bottom: 1px solid #1e293b;
      color: #cbd5e1;
    }
    .item-title-link {
      text-decoration: none;
      color: inherit;
    }
    .item-title-link:hover .font-medium {
      color: #60a5fa;
    }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .text-xs { font-size: 0.75rem; }
    .text-muted { color: #64748b; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-danger { color: #ef4444; }
    .text-primary { color: #60a5fa; }
    .badge {
      background: #334155;
      color: #94a3b8;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
    }
    .margin-badge {
      font-weight: 600;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
    }
    .margin-badge.high_margin { background: #10b98122; color: #10b981; }
    .margin-badge.healthy_margin { background: #3b82f622; color: #3b82f6; }
    .margin-badge.low_margin { background: #f59e0b22; color: #f59e0b; }
    .margin-badge.negative_margin { background: #ef444422; color: #ef4444; }
    .transparency-note {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 0.75rem 1rem;
      font-size: 0.8rem;
      color: #94a3b8;
    }
    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 1rem;
      color: #94a3b8;
    }
    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #334155;
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class AnalyticsDashboardComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  customStartDate = '';
  customEndDate = '';
  dashboard: ExecutiveDashboard | null = null;
  insights: BusinessInsight[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private analyticsService: AnalyticsService,
    public roleState: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.loadInsights();
  }

  onRangeChange(): void {
    if (this.selectedRange !== 'CUSTOM') {
      this.loadDashboard();
    }
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;
    this.analyticsService.getDashboard(
      this.selectedRange,
      this.selectedRange === 'CUSTOM' ? this.customStartDate : undefined,
      this.selectedRange === 'CUSTOM' ? this.customEndDate : undefined
    ).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load executive dashboard analytics.';
        this.loading = false;
      }
    });
  }

  loadInsights(): void {
    this.analyticsService.getBusinessInsights().subscribe({
      next: (data) => { this.insights = data || []; },
      error: () => {}
    });
  }

  getBarHeight(val: number): number {
    if (!val || val <= 0) return 4;
    const maxVal = 50000;
    return Math.min(140, Math.max(6, (val / maxVal) * 140));
  }

  exportExecutiveCsv(): void {
    this.analyticsService.exportReportCsv(
      'revenue',
      this.selectedRange,
      this.selectedRange === 'CUSTOM' ? this.customStartDate : undefined,
      this.selectedRange === 'CUSTOM' ? this.customEndDate : undefined
    ).subscribe((blob) => {
      this.analyticsService.downloadCsvBlob(blob, `executive-summary-${this.selectedRange.toLowerCase()}.csv`);
    });
  }
}
