import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, QuoteAnalytics, DateRangePreset } from '../../services/analytics.service';

@Component({
  selector: 'app-quote-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Sales Funnel</span>
          </div>
          <h1 class="page-title">Quote Analytics & Sales Funnel</h1>
          <p class="subtitle">Proposal conversion metrics, stage-by-stage pipeline velocity, and lost revenue recovery opportunities.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadQuotes()">
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadQuotes()">
            <span class="icon">🔄</span> Refresh
          </button>
        </div>
      </div>

      <!-- Sub-Nav Pills -->
      <div class="sub-nav-pills">
        <a routerLink="/dashboard/analytics/dashboard" class="pill">Executive BI</a>
        <a routerLink="/dashboard/analytics/revenue" class="pill">Revenue & Cashflow</a>
        <a routerLink="/dashboard/analytics/bookings" class="pill">Booking Profitability</a>
        <a routerLink="/dashboard/analytics/products" class="pill">Product Margins</a>
        <a routerLink="/dashboard/analytics/utilization" class="pill">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill">Customer LTV</a>
        <a routerLink="/dashboard/analytics/quotes" class="pill active">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill">Operations & AR</a>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Calculating sales funnel conversion rates...</p>
      </div>

      <div *ngIf="!loading && quoteData" class="quote-content">
        <!-- Top Stats Row -->
        <div class="kpi-grid">
          <div class="kpi-card highlight-card">
            <div class="kpi-label">Overall Conversion Rate</div>
            <div class="kpi-val text-success">{{ quoteData.overallConversionRate | number:'1.1-1' }}%</div>
            <div class="kpi-subtext">Approved / Total Sent proposals</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Total Quoted Value</div>
            <div class="kpi-val">\${{ quoteData.totalQuotedValue | number:'1.2-2' }}</div>
            <div class="kpi-subtext">{{ quoteData.totalQuotesCreated }} proposals issued</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Approved Pipeline Value</div>
            <div class="kpi-val text-primary">\${{ quoteData.totalApprovedValue | number:'1.2-2' }}</div>
            <div class="kpi-subtext">{{ quoteData.quotesApproved }} approved quotes</div>
          </div>

          <div class="kpi-card">
            <div class="kpi-label">Lost Pipeline Revenue</div>
            <div class="kpi-val text-danger">\${{ quoteData.potentialLostRevenue | number:'1.2-2' }}</div>
            <div class="kpi-subtext">{{ quoteData.quotesDeclined }} declined • {{ quoteData.quotesExpired }} expired</div>
          </div>
        </div>

        <!-- Sales Pipeline Funnel Card -->
        <div class="card">
          <div class="card-header">
            <h3>Rental Sales Funnel & Stage Drop-Off</h3>
            <span class="badge">Pipeline Velocity: Avg {{ quoteData.averageApprovalHours | number:'1.1-1' }} hrs to approval</span>
          </div>

          <div class="funnel-container">
            <div *ngFor="let stage of quoteData.salesFunnel; let i = index" class="funnel-stage">
              <div class="stage-top">
                <div class="stage-name">{{ stage.stageName }}</div>
                <div class="stage-counts">
                  <strong>{{ stage.count }}</strong>
                  <span class="text-muted">(\${{ stage.value | number:'1.0-0' }})</span>
                </div>
              </div>

              <!-- Visual Funnel Bar -->
              <div class="funnel-bar-wrapper">
                <div
                  class="funnel-bar"
                  [style.width.%]="stage.overallConversion > 0 ? stage.overallConversion : 100"
                  [ngClass]="'stage-color-' + i"
                >
                  <span class="bar-label">{{ stage.overallConversion | number:'1.1-1' }}% of Top</span>
                </div>
              </div>

              <div class="stage-footer" *ngIf="i > 0">
                <span>Stage Conversion from previous: <strong>{{ stage.conversionFromPrevious | number:'1.1-1' }}%</strong></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Quote Status Breakdown -->
        <div class="card">
          <div class="card-header">
            <h3>Proposal Distribution by Status</h3>
          </div>
          <div class="status-grid">
            <div *ngFor="let item of quoteData.quotesByStatus" class="status-box">
              <div class="status-title">{{ item.name }}</div>
              <div class="status-count">{{ item.count }} quotes</div>
              <div class="status-pct">{{ item.percentage | number:'1.1-1' }}%</div>
            </div>
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
      background: linear-gradient(135deg, #1e293b 0%, #064e3b 100%);
      border: 1px solid #10b98144;
    }
    .kpi-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
    .kpi-val { font-size: 1.65rem; font-weight: 700; color: #ffffff; margin-bottom: 0.35rem; }
    .kpi-subtext { font-size: 0.8rem; color: #64748b; }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .funnel-container { display: flex; flex-direction: column; gap: 1.25rem; }
    .funnel-stage { background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 1rem; }
    .stage-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .stage-name { font-weight: 600; font-size: 0.95rem; color: #f8fafc; }
    .stage-counts { font-size: 0.9rem; }
    .funnel-bar-wrapper { height: 26px; background: #1e293b; border-radius: 6px; overflow: hidden; margin-bottom: 0.5rem; }
    .funnel-bar { height: 100%; display: flex; align-items: center; padding-left: 0.75rem; border-radius: 6px; transition: width 0.4s ease; min-width: 60px; }
    .bar-label { font-size: 0.75rem; font-weight: 600; color: white; }
    .stage-color-0 { background: #3b82f6; }
    .stage-color-1 { background: #6366f1; }
    .stage-color-2 { background: #8b5cf6; }
    .stage-color-3 { background: #06b6d4; }
    .stage-color-4 { background: #10b981; }
    .stage-footer { font-size: 0.8rem; color: #94a3b8; }
    .status-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 1rem; }
    .status-box { background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 1rem; text-align: center; }
    .status-title { font-size: 0.8rem; color: #94a3b8; margin-bottom: 0.35rem; }
    .status-count { font-size: 1.25rem; font-weight: 700; color: #f8fafc; }
    .status-pct { font-size: 0.75rem; color: #60a5fa; margin-top: 0.2rem; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .text-muted { color: #64748b; }
    .text-primary { color: #60a5fa; }
    .text-success { color: #10b981; }
    .text-danger { color: #ef4444; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class QuoteAnalyticsComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  quoteData: QuoteAnalytics | null = null;
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadQuotes();
  }

  loadQuotes(): void {
    this.loading = true;
    this.analyticsService.getQuoteAnalytics(this.selectedRange).subscribe({
      next: (data) => {
        this.quoteData = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
