import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AnalyticsService, CustomerAnalyticsDetail } from '../../services/analytics.service';

@Component({
  selector: 'app-customer-analytics-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> /
            <a routerLink="/dashboard/analytics/customers">Customer LTV</a> /
            <span>{{ detail?.customerNumber || 'Profile' }}</span>
          </div>
          <h1 class="page-title">{{ detail?.companyName || detail?.customerName }}</h1>
          <p class="subtitle">{{ detail?.customerNumber }} • Contact: {{ detail?.customerName }} • {{ detail?.email }} • Type: {{ detail?.customerType }}</p>
        </div>

        <div class="actions-bar">
          <a routerLink="/dashboard/analytics/customers" class="btn btn-outline">
            ← Back to Customers
          </a>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading customer lifetime financial ledger...</p>
      </div>

      <div *ngIf="!loading && detail" class="detail-content">
        <!-- Overview Banner -->
        <div class="overview-banner">
          <div class="banner-stat">
            <span class="label">Lifetime Revenue</span>
            <span class="val text-primary">\${{ detail.lifetimeRevenue | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Cash Collected</span>
            <span class="val text-success">\${{ detail.lifetimeCollected | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Outstanding Balance</span>
            <span class="val font-bold" [ngClass]="detail.outstandingBalance > 0 ? 'text-warning' : 'text-muted'">
              \${{ detail.outstandingBalance | number:'1.2-2' }}
            </span>
          </div>
          <div class="banner-stat">
            <span class="label">LTV Gross Margin</span>
            <span class="margin-badge high_margin">
              {{ detail.lifetimeMarginPercent | number:'1.1-1' }}% (+\${{ detail.lifetimeProfit | number:'1.0-0' }})
            </span>
          </div>
        </div>

        <!-- Account Key Metrics Grid -->
        <div class="kpi-grid">
          <div class="kpi-card">
            <div class="kpi-label">Total Bookings</div>
            <div class="kpi-val">{{ detail.totalBookingsCount }}</div>
            <div class="kpi-subtext">Last booking: {{ detail.lastBookingDate || 'N/A' }}</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Average Booking Value</div>
            <div class="kpi-val">\${{ detail.averageBookingValue | number:'1.2-2' }}</div>
            <div class="kpi-subtext">Per reservation ticket</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Quote Conversion Rate</div>
            <div class="kpi-val text-success">{{ detail.quoteConversionRate | number:'1.1-1' }}%</div>
            <div class="kpi-subtext">{{ detail.approvedQuotesCount }} approved of {{ detail.totalQuotesCount }} quotes</div>
          </div>
          <div class="kpi-card">
            <div class="kpi-label">Damage History</div>
            <div class="kpi-val" [class.text-danger]="detail.damageClaimsCount > 0">{{ detail.damageClaimsCount }} claims</div>
            <div class="kpi-subtext">\${{ detail.totalDamageCost | number:'1.2-2' }} claim damage value</div>
          </div>
        </div>

        <!-- Top Rented Products & Revenue Trend Row -->
        <div class="charts-row">
          <!-- Top Rented Catalog Products -->
          <div class="card">
            <div class="card-header">
              <h3>Top Rented Products by this Customer</h3>
            </div>
            <div class="breakdown-list" *ngIf="detail.topRentedProducts?.length">
              <div *ngFor="let item of detail.topRentedProducts" class="breakdown-item">
                <div class="item-header">
                  <span>{{ item.name }}</span>
                  <strong>\${{ item.value | number:'1.2-2' }} ({{ item.count }} units)</strong>
                </div>
              </div>
            </div>
            <div *ngIf="!detail.topRentedProducts?.length" class="empty-state">
              No product items rented yet.
            </div>
          </div>

          <!-- Account Revenue Trend -->
          <div class="card" *ngIf="detail.revenueTrend?.length">
            <div class="card-header">
              <h3>6-Month Account Revenue History</h3>
            </div>
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Period</th>
                    <th>Bookings</th>
                    <th>Revenue</th>
                    <th>Gross Profit</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let m of detail.revenueTrend">
                    <td class="font-medium">{{ m.period }}</td>
                    <td>{{ m.bookingsCount }}</td>
                    <td class="font-medium">\${{ m.bookedRevenue | number:'1.2-2' }}</td>
                    <td class="text-success font-medium">\${{ m.grossProfit | number:'1.2-2' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Recent Bookings Table -->
        <div class="card" *ngIf="detail.recentBookings?.length">
          <div class="card-header">
            <h3>Recent Bookings & Margins</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Booking #</th>
                  <th>Booking Date</th>
                  <th>Status</th>
                  <th>Total Revenue</th>
                  <th>Direct Cost</th>
                  <th>Gross Profit</th>
                  <th>Margin %</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let b of detail.recentBookings">
                  <td>
                    <a [routerLink]="['/dashboard/analytics/bookings', b.bookingId]" class="booking-link">
                      <strong>{{ b.bookingNumber }}</strong>
                    </a>
                  </td>
                  <td>{{ b.bookingDate }}</td>
                  <td><span class="badge">{{ b.status }}</span></td>
                  <td class="font-medium">\${{ b.totalRevenue | number:'1.2-2' }}</td>
                  <td class="text-muted">\${{ b.directCost | number:'1.2-2' }}</td>
                  <td class="font-bold" [ngClass]="b.grossProfit >= 0 ? 'text-success' : 'text-danger'">
                    \${{ b.grossProfit | number:'1.2-2' }}
                  </td>
                  <td>
                    <span class="margin-badge" [ngClass]="b.marginFlag.toLowerCase()">
                      {{ b.grossMarginPercent | number:'1.1-1' }}%
                    </span>
                  </td>
                  <td>
                    <a [routerLink]="['/dashboard/analytics/bookings', b.bookingId]" class="btn-sm btn-outline">
                      Cost Breakdown →
                    </a>
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
    .charts-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem; }
    .card-header { margin-bottom: 1rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .breakdown-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .breakdown-item { display: flex; flex-direction: column; gap: 0.25rem; padding-bottom: 0.5rem; border-bottom: 1px solid #33415544; }
    .item-header { display: flex; justify-content: space-between; font-size: 0.85rem; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .booking-link { color: #60a5fa; text-decoration: none; }
    .booking-link:hover { text-decoration: underline; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .font-bold { font-weight: 700; }
    .text-muted { color: #94a3b8; }
    .text-primary { color: #60a5fa; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-danger { color: #ef4444; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .margin-badge { font-weight: 600; padding: 0.3rem 0.6rem; border-radius: 4px; font-size: 0.85rem; display: inline-block; width: fit-content; }
    .margin-badge.high_margin { background: #10b98122; color: #10b981; }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 6px; font-size: 0.75rem; text-decoration: none; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class CustomerAnalyticsDetailComponent implements OnInit {
  customerId = '';
  detail: CustomerAnalyticsDetail | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.customerId = params['id'];
      if (this.customerId) {
        this.loadDetail();
      }
    });
  }

  loadDetail(): void {
    this.loading = true;
    this.analyticsService.getCustomerAnalyticsDetail(this.customerId).subscribe({
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
