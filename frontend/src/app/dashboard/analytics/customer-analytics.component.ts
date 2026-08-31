import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AnalyticsService, CustomerAnalytics, DateRangePreset } from '../../services/analytics.service';

@Component({
  selector: 'app-customer-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Customer LTV</span>
          </div>
          <h1 class="page-title">Customer Lifetime Value & Profitability</h1>
          <p class="subtitle">Authoritative customer account analytics: Lifetime revenue, cash collected, outstanding receivables, and quote conversion rates.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadCustomers()">
              <option value="THIS_MONTH">This Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadCustomers()">
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
        <a routerLink="/dashboard/analytics/utilization" class="pill">Fleet Utilization</a>
        <a routerLink="/dashboard/analytics/customers" class="pill active">Customer LTV</a>
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
            placeholder="Search by customer name, company, or customer #..."
          />
        </div>

        <div class="status-filter">
          <label>Customer Type:</label>
          <select [(ngModel)]="selectedTypeFilter">
            <option value="ALL">All Types</option>
            <option value="CORPORATE">Corporate</option>
            <option value="EVENT_PLANNER">Event Planner</option>
            <option value="INDIVIDUAL">Individual / Standard</option>
          </select>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading customer lifetime profitability & transaction histories...</p>
      </div>

      <!-- Table View -->
      <div *ngIf="!loading" class="card">
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Customer / Account</th>
                <th>Type</th>
                <th>Total Bookings</th>
                <th>Lifetime Revenue</th>
                <th>Cash Collected</th>
                <th>Outstanding</th>
                <th>Avg Booking Value</th>
                <th>Quote Conversion</th>
                <th>Claims</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of filteredCustomers">
                <td>
                  <a [routerLink]="['/dashboard/analytics/customers', c.customerId]" class="cust-link">
                    <div class="font-medium">{{ c.companyName || c.customerName }}</div>
                    <div class="text-xs text-muted">{{ c.customerNumber }} {{ c.companyName ? '(' + c.customerName + ')' : '' }}</div>
                  </a>
                </td>
                <td><span class="badge">{{ c.customerType }}</span></td>
                <td>{{ c.totalBookingsCount }}</td>
                <td class="font-medium">\${{ c.lifetimeRevenue | number:'1.2-2' }}</td>
                <td class="text-success font-medium">\${{ c.lifetimeCollected | number:'1.2-2' }}</td>
                <td [class.text-warning]="c.outstandingBalance > 0">
                  \${{ c.outstandingBalance | number:'1.2-2' }}
                </td>
                <td>\${{ c.averageBookingValue | number:'1.2-2' }}</td>
                <td>
                  <span class="badge badge-success">{{ c.quoteConversionRate | number:'1.1-1' }}%</span>
                </td>
                <td>
                  <span *ngIf="c.damageClaimsCount > 0" class="badge badge-warning">{{ c.damageClaimsCount }} claims</span>
                  <span *ngIf="c.damageClaimsCount === 0" class="text-muted">0</span>
                </td>
                <td>
                  <a [routerLink]="['/dashboard/analytics/customers', c.customerId]" class="btn-sm btn-outline">
                    LTV Profile →
                  </a>
                </td>
              </tr>
              <tr *ngIf="filteredCustomers.length === 0">
                <td colspan="10" class="empty-state">No customers found matching the search criteria.</td>
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
    .cust-link { text-decoration: none; color: inherit; }
    .cust-link:hover .font-medium { color: #60a5fa; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .text-xs { font-size: 0.75rem; }
    .text-muted { color: #64748b; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .badge-success { background: #10b98122; color: #10b981; }
    .badge-warning { background: #f59e0b22; color: #f59e0b; }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 6px; font-size: 0.75rem; text-decoration: none; }
    .empty-state { text-align: center; padding: 2rem; color: #64748b; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class CustomerAnalyticsComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  customers: CustomerAnalytics[] = [];
  searchQuery = '';
  selectedTypeFilter = 'ALL';
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.analyticsService.getCustomerAnalytics(this.selectedRange).subscribe({
      next: (data) => {
        this.customers = data || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredCustomers(): CustomerAnalytics[] {
    return this.customers.filter((c) => {
      const matchesSearch =
        !this.searchQuery ||
        c.customerName?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        c.companyName?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        c.customerNumber?.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesType =
        this.selectedTypeFilter === 'ALL' || c.customerType === this.selectedTypeFilter;

      return matchesSearch && matchesType;
    });
  }

  exportCsv(): void {
    this.analyticsService.exportReportCsv('customers', this.selectedRange).subscribe((blob) => {
      this.analyticsService.downloadCsvBlob(blob, `customer-ltv-${this.selectedRange.toLowerCase()}.csv`);
    });
  }
}
