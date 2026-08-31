import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, RevenueAnalytics, DateRangePreset } from '../../services/analytics.service';

@Component({
  selector: 'app-revenue-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Revenue & Cashflow</span>
          </div>
          <h1 class="page-title">Revenue & Cashflow Streams</h1>
          <p class="subtitle">Authoritative multi-stream tracking: Booked, Invoiced, Collected, and Outstanding balances.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadRevenue()">
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
              <option value="LAST_YEAR">Last Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadRevenue()">
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
        <a routerLink="/dashboard/analytics/revenue" class="pill active">Revenue & Cashflow</a>
        <a routerLink="/dashboard/analytics/bookings" class="pill">Booking Profitability</a>
        <a routerLink="/dashboard/analytics/products" class="pill">Product Margins</a>
        <a routerLink="/dashboard/analytics/utilization" class="pill">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill">Customer LTV</a>
        <a routerLink="/dashboard/analytics/quotes" class="pill">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill">Operations & AR</a>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading revenue stream metrics...</p>
      </div>

      <div *ngIf="!loading && revenueData" class="revenue-content">
        <!-- 4 Key Revenue KPI Cards -->
        <div class="kpi-grid">
          <div class="kpi-card">
            <div class="kpi-label">Total Booked Revenue</div>
            <div class="kpi-val">\${{ revenueData.totalBookedRevenue | number:'1.0-0' }}</div>
            <div class="kpi-trend text-success">
              Growth: +{{ revenueData.revenueGrowthPercent | number:'1.1-1' }}%
            </div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Total Invoiced Revenue</div>
            <div class="kpi-val">\${{ revenueData.totalInvoicedRevenue | number:'1.0-0' }}</div>
            <div class="kpi-subtext">Issued billing statements</div>
          </div>

          <div class="kpi-card highlight-card">
            <div class="kpi-label">Collected Cash Revenue</div>
            <div class="kpi-val text-success">\${{ revenueData.totalCollectedRevenue | number:'1.0-0' }}</div>
            <div class="kpi-subtext">Completed payment receipts</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Outstanding Balance</div>
            <div class="kpi-val text-warning">\${{ revenueData.totalOutstandingRevenue | number:'1.0-0' }}</div>
            <div class="kpi-subtext">Accounts receivable</div>
          </div>
        </div>

        <!-- Breakdown Row -->
        <div class="charts-row">
          <!-- Revenue by Event Type -->
          <div class="card">
            <div class="card-header">
              <h3>Revenue by Event Type</h3>
            </div>
            <div class="breakdown-list">
              <div *ngFor="let item of revenueData.revenueByEventType" class="breakdown-item">
                <div class="item-header">
                  <span>{{ item.name }}</span>
                  <strong>\${{ item.value | number:'1.0-0' }} ({{ item.percentage | number:'1.1-1' }}%)</strong>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" [style.width.%]="item.percentage"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Revenue by Booking Status -->
          <div class="card">
            <div class="card-header">
              <h3>Revenue by Booking Status</h3>
            </div>
            <div class="breakdown-list">
              <div *ngFor="let item of revenueData.revenueByBookingStatus" class="breakdown-item">
                <div class="item-header">
                  <span>{{ item.name }}</span>
                  <strong>\${{ item.value | number:'1.0-0' }} ({{ item.percentage | number:'1.1-1' }}%)</strong>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill status-fill" [style.width.%]="item.percentage"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 6-Month Monthly Trend History Table -->
        <div class="card">
          <div class="card-header">
            <h3>Monthly Financial History (Authoritative)</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Period</th>
                  <th>Bookings Count</th>
                  <th>Booked Revenue</th>
                  <th>Invoiced Revenue</th>
                  <th>Collected Cash</th>
                  <th>Est. Gross Profit</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let m of revenueData.revenueTrend">
                  <td class="font-medium">{{ m.period }}</td>
                  <td>{{ m.bookingsCount }}</td>
                  <td class="font-medium">\${{ m.bookedRevenue | number:'1.0-0' }}</td>
                  <td>\${{ m.invoicedRevenue | number:'1.0-0' }}</td>
                  <td class="text-success font-medium">\${{ m.collectedRevenue | number:'1.0-0' }}</td>
                  <td class="text-primary font-medium">\${{ m.grossProfit | number:'1.0-0' }}</td>
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
    .kpi-card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 10px;
      padding: 1.25rem;
    }
    .kpi-card.highlight-card {
      background: linear-gradient(135deg, #1e293b 0%, #064e3b 100%);
      border: 1px solid #10b98144;
    }
    .kpi-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
    .kpi-val { font-size: 1.65rem; font-weight: 700; color: #ffffff; margin-bottom: 0.35rem; }
    .kpi-trend { font-size: 0.8rem; font-weight: 600; }
    .kpi-subtext { font-size: 0.8rem; color: #64748b; }
    .charts-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
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
      margin-bottom: 1rem;
    }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .breakdown-list { display: flex; flex-direction: column; gap: 0.85rem; }
    .breakdown-item { display: flex; flex-direction: column; gap: 0.35rem; }
    .item-header { display: flex; justify-content: space-between; font-size: 0.85rem; }
    .progress-bar { height: 6px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: #6366f1; border-radius: 3px; }
    .status-fill { background: #3b82f6; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.6rem 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-primary { color: #60a5fa; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class RevenueAnalyticsComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  revenueData: RevenueAnalytics | null = null;
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadRevenue();
  }

  loadRevenue(): void {
    this.loading = true;
    this.analyticsService.getRevenueAnalytics(this.selectedRange).subscribe({
      next: (data) => {
        this.revenueData = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  exportCsv(): void {
    this.analyticsService.exportReportCsv('revenue', this.selectedRange).subscribe((blob) => {
      this.analyticsService.downloadCsvBlob(blob, `revenue-report-${this.selectedRange.toLowerCase()}.csv`);
    });
  }
}
