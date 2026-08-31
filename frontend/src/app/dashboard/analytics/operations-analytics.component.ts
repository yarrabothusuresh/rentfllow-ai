import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  AnalyticsService,
  WarehouseAnalytics,
  DeliveryAnalytics,
  ReturnAndDamageAnalytics,
  PaymentAndArAnalytics,
  DateRangePreset
} from '../../services/analytics.service';

@Component({
  selector: 'app-operations-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> / <span>Operations & AR</span>
          </div>
          <h1 class="page-title">Operations & Accounts Receivable</h1>
          <p class="subtitle">Authoritative operational throughput, on-time delivery metrics, damage/repair expenses, and AR aging buckets.</p>
        </div>

        <div class="actions-bar">
          <div class="range-selector">
            <label>Period:</label>
            <select [(ngModel)]="selectedRange" (change)="loadAllOperations()">
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="THIS_QUARTER">This Quarter</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button class="btn btn-outline" (click)="loadAllOperations()">
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
        <a routerLink="/dashboard/analytics/quotes" class="pill">Sales Funnel</a>
        <a routerLink="/dashboard/analytics/operations" class="pill active">Operations & AR</a>
      </div>

      <!-- Operation Tabs -->
      <div class="tab-header">
        <button class="tab-btn" [class.active]="activeTab === 'WAREHOUSE'" (click)="activeTab = 'WAREHOUSE'">
          📦 Warehouse Operations
        </button>
        <button class="tab-btn" [class.active]="activeTab === 'DELIVERY'" (click)="activeTab = 'DELIVERY'">
          🚚 Delivery & Logistics
        </button>
        <button class="tab-btn" [class.active]="activeTab === 'DAMAGE'" (click)="activeTab = 'DAMAGE'">
          🛡️ Returns, Damage & Repairs
        </button>
        <button class="tab-btn" [class.active]="activeTab === 'AR'" (click)="activeTab = 'AR'">
          💳 Payment & AR Aging
        </button>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Calculating operational throughput and aging metrics...</p>
      </div>

      <div *ngIf="!loading">
        <!-- TAB 1: WAREHOUSE -->
        <div *ngIf="activeTab === 'WAREHOUSE' && warehouseData" class="tab-pane">
          <div class="kpi-grid">
            <div class="kpi-card highlight-card">
              <div class="kpi-label">On-Time Staging Readiness</div>
              <div class="kpi-val text-success">{{ warehouseData.onTimeReadinessRate | number:'1.1-1' }}%</div>
              <div class="kpi-subtext">{{ warehouseData.ordersReadyOnTime }} of {{ warehouseData.totalOrdersProcessed }} orders ready on time</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Avg Fulfillment Time</div>
              <div class="kpi-val text-primary">{{ warehouseData.averageTotalFulfillmentMinutes | number:'1.1-1' }} min</div>
              <div class="kpi-subtext">Pick: {{ warehouseData.averagePickDurationMinutes }}m • Pack: {{ warehouseData.averagePackDurationMinutes }}m • Load: {{ warehouseData.averageLoadDurationMinutes }}m</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Exceptions Reported</div>
              <div class="kpi-val text-warning">{{ warehouseData.totalExceptionsReported }}</div>
              <div class="kpi-subtext">{{ warehouseData.resolvedExceptionsCount }} resolved • {{ warehouseData.totalShortPicksCount }} shortages</div>
            </div>
          </div>

          <!-- Exception and Order Status Breakdown -->
          <div class="charts-row">
            <div class="card">
              <div class="card-header"><h3>Exceptions by Type</h3></div>
              <div class="breakdown-list">
                <div *ngFor="let item of warehouseData.exceptionsByType" class="breakdown-item">
                  <div class="item-header">
                    <span>{{ item.name }}</span>
                    <strong>{{ item.count }} exceptions ({{ item.percentage | number:'1.1-1' }}%)</strong>
                  </div>
                  <div class="progress-bar"><div class="progress-fill" [style.width.%]="item.percentage"></div></div>
                </div>
              </div>
            </div>

            <div class="card">
              <div class="card-header"><h3>Warehouse Orders by Status</h3></div>
              <div class="breakdown-list">
                <div *ngFor="let item of warehouseData.ordersByStatus" class="breakdown-item">
                  <div class="item-header">
                    <span>{{ item.name }}</span>
                    <strong>{{ item.count }} orders</strong>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- TAB 2: DELIVERY -->
        <div *ngIf="activeTab === 'DELIVERY' && deliveryData" class="tab-pane">
          <div class="kpi-grid">
            <div class="kpi-card highlight-card">
              <div class="kpi-label">On-Time Delivery Rate</div>
              <div class="kpi-val text-success">{{ deliveryData.onTimeDeliveryRate | number:'1.1-1' }}%</div>
              <div class="kpi-subtext">{{ deliveryData.onTimeDeliveries }} on-time of {{ deliveryData.totalDeliveries }} total</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Avg Transit & Delivery Time</div>
              <div class="kpi-val">{{ deliveryData.averageDeliveryDurationMinutes | number:'1.1-1' }} min</div>
              <div class="kpi-subtext">Route completion speed</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Fleet Vehicle Utilization</div>
              <div class="kpi-val text-primary">{{ deliveryData.fleetVehicleUtilizationPercent | number:'1.1-1' }}%</div>
              <div class="kpi-subtext">Active vehicle schedule rate</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Delivery Exceptions</div>
              <div class="kpi-val" [class.text-danger]="deliveryData.lateDeliveries + deliveryData.failedDeliveries > 0">
                {{ deliveryData.lateDeliveries + deliveryData.failedDeliveries }}
              </div>
              <div class="kpi-subtext">{{ deliveryData.lateDeliveries }} late • {{ deliveryData.failedDeliveries }} failed</div>
            </div>
          </div>
        </div>

        <!-- TAB 3: DAMAGE & REPAIRS -->
        <div *ngIf="activeTab === 'DAMAGE' && damageData" class="tab-pane">
          <div class="kpi-grid">
            <div class="kpi-card">
              <div class="kpi-label">Overall Damage Rate</div>
              <div class="kpi-val text-warning">{{ damageData.damageRatePercent | number:'1.2-2' }}%</div>
              <div class="kpi-subtext">{{ damageData.damagedUnitsCount }} damaged of {{ damageData.totalReturnedUnits }} returned</div>
            </div>

            <div class="kpi-card highlight-card">
              <div class="kpi-label">Damage Cost (Approved)</div>
              <div class="kpi-val text-danger">\${{ damageData.approvedDamageCost | number:'1.2-2' }}</div>
              <div class="kpi-subtext">\${{ damageData.estimatedDamageCost | number:'1.2-2' }} estimated initial</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Actual Maintenance / Repairs</div>
              <div class="kpi-val">\${{ damageData.actualRepairCost | number:'1.2-2' }}</div>
              <div class="kpi-subtext">Closed repair work orders</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Claims Resolution</div>
              <div class="kpi-val text-success">{{ damageData.customerApprovedClaimsCount }} approved</div>
              <div class="kpi-subtext">{{ damageData.disputedClaimsCount }} disputed • {{ damageData.waivedClaimsCount }} waived</div>
            </div>
          </div>
        </div>

        <!-- TAB 4: AR AGING -->
        <div *ngIf="activeTab === 'AR' && arData" class="tab-pane">
          <div class="kpi-grid">
            <div class="kpi-card highlight-card">
              <div class="kpi-label">Collection Rate</div>
              <div class="kpi-val text-success">{{ arData.collectionRatePercent | number:'1.1-1' }}%</div>
              <div class="kpi-subtext">Avg {{ arData.averageDaysToPay | number:'1.1-1' }} days to payment</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Total Cash Collected</div>
              <div class="kpi-val text-success">\${{ arData.totalCollectedAmount | number:'1.2-2' }}</div>
              <div class="kpi-subtext">{{ arData.completedPaymentsCount }} completed payments</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Outstanding Receivables</div>
              <div class="kpi-val">\${{ arData.totalOutstandingAmount | number:'1.2-2' }}</div>
              <div class="kpi-subtext">All unpaid invoice balances</div>
            </div>

            <div class="kpi-card">
              <div class="kpi-label">Overdue Exposure</div>
              <div class="kpi-val text-danger">\${{ arData.totalOverdueAmount | number:'1.2-2' }}</div>
              <div class="kpi-subtext">Invoices past due date</div>
            </div>
          </div>

          <!-- AR Aging Buckets Table -->
          <div class="card">
            <div class="card-header">
              <h3>Accounts Receivable (AR) Aging Buckets</h3>
              <span class="badge">Authoritative Invoicing Aging</span>
            </div>
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Aging Bucket</th>
                    <th>Invoices Count</th>
                    <th>Outstanding Amount</th>
                    <th>% of Total Receivables</th>
                    <th>Visual Weight</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let b of arData.arAgingBuckets">
                    <td class="font-medium">{{ b.bucketName }}</td>
                    <td>{{ b.invoiceCount }}</td>
                    <td class="font-bold">\${{ b.amount | number:'1.2-2' }}</td>
                    <td>{{ b.percentageOfTotal | number:'1.1-1' }}%</td>
                    <td style="width: 250px;">
                      <div class="progress-bar">
                        <div class="progress-fill" [style.width.%]="b.percentageOfTotal"></div>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
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
    .tab-header {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #334155;
      padding-bottom: 0.5rem;
      flex-wrap: wrap;
    }
    .tab-btn {
      background: #1e293b;
      border: 1px solid #334155;
      color: #94a3b8;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
    }
    .tab-btn:hover { background: #334155; color: white; }
    .tab-btn.active { background: #3b82f6; border-color: #3b82f6; color: white; font-weight: 600; }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .kpi-card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .kpi-card.highlight-card { background: linear-gradient(135deg, #1e293b 0%, #1e1b4b 100%); border: 1px solid #6366f144; }
    .kpi-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
    .kpi-val { font-size: 1.65rem; font-weight: 700; color: #ffffff; margin-bottom: 0.35rem; }
    .kpi-subtext { font-size: 0.8rem; color: #64748b; }
    .charts-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; margin-bottom: 1.5rem; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .breakdown-list { display: flex; flex-direction: column; gap: 0.85rem; }
    .breakdown-item { display: flex; flex-direction: column; gap: 0.35rem; }
    .item-header { display: flex; justify-content: space-between; font-size: 0.85rem; }
    .progress-bar { height: 6px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: #6366f1; border-radius: 3px; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .font-bold { font-weight: 700; }
    .text-primary { color: #60a5fa; }
    .text-success { color: #10b981; }
    .text-warning { color: #f59e0b; }
    .text-danger { color: #ef4444; }
    .badge { background: #334155; color: #94a3b8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; }
    .loading-state { text-align: center; padding: 4rem; color: #94a3b8; }
    .spinner { width: 40px; height: 40px; border: 3px solid #334155; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class OperationsAnalyticsComponent implements OnInit {
  selectedRange: DateRangePreset = 'THIS_MONTH';
  activeTab: 'WAREHOUSE' | 'DELIVERY' | 'DAMAGE' | 'AR' = 'WAREHOUSE';

  warehouseData: WarehouseAnalytics | null = null;
  deliveryData: DeliveryAnalytics | null = null;
  damageData: ReturnAndDamageAnalytics | null = null;
  arData: PaymentAndArAnalytics | null = null;
  loading = false;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadAllOperations();
  }

  loadAllOperations(): void {
    this.loading = true;
    this.analyticsService.getWarehouseAnalytics(this.selectedRange).subscribe({
      next: (d) => { this.warehouseData = d; }
    });
    this.analyticsService.getDeliveryAnalytics(this.selectedRange).subscribe({
      next: (d) => { this.deliveryData = d; }
    });
    this.analyticsService.getReturnAndDamageAnalytics(this.selectedRange).subscribe({
      next: (d) => { this.damageData = d; }
    });
    this.analyticsService.getPaymentAndArAnalytics(this.selectedRange).subscribe({
      next: (d) => {
        this.arData = d;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
