import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AnalyticsService, BookingProfitDetail } from '../../services/analytics.service';

@Component({
  selector: 'app-booking-profit-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <div class="breadcrumbs">
            <a routerLink="/dashboard/analytics/dashboard">Analytics</a> /
            <a routerLink="/dashboard/analytics/bookings">Booking Profitability</a> /
            <span>{{ detail?.bookingNumber || 'Detail' }}</span>
          </div>
          <h1 class="page-title">Booking Financial & Cost Breakdown</h1>
          <p class="subtitle">{{ detail?.bookingNumber }} — {{ detail?.customerName }} ({{ detail?.eventName }})</p>
        </div>

        <div class="actions-bar">
          <a routerLink="/dashboard/analytics/bookings" class="btn btn-outline">
            ← Back to Bookings
          </a>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Loading authoritative cost source calculation...</p>
      </div>

      <div *ngIf="!loading && detail" class="detail-content">
        <!-- Overview Banner -->
        <div class="overview-banner">
          <div class="banner-stat">
            <span class="label">Total Revenue</span>
            <span class="val text-primary">\${{ detail.totalRevenue | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Total Direct Cost</span>
            <span class="val text-muted">\${{ detail.totalDirectCost | number:'1.2-2' }}</span>
          </div>
          <div class="banner-stat">
            <span class="label">Gross Profit</span>
            <span class="val font-bold" [ngClass]="detail.grossProfit >= 0 ? 'text-success' : 'text-danger'">
              \${{ detail.grossProfit | number:'1.2-2' }}
            </span>
          </div>
          <div class="banner-stat">
            <span class="label">Gross Margin</span>
            <span class="margin-badge" [ngClass]="detail.marginFlag.toLowerCase()">
              {{ detail.grossMarginPercent | number:'1.1-1' }}% ({{ detail.marginFlag }})
            </span>
          </div>
        </div>

        <!-- Revenue & Cost Grid -->
        <div class="breakdown-grid">
          <!-- Revenue Streams Itemization -->
          <div class="card">
            <div class="card-header">
              <h3>Revenue Streams Itemization</h3>
            </div>
            <div class="item-list">
              <div class="item-row">
                <span>Rental Subtotal</span>
                <strong>\${{ detail.rentalRevenue | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.deliveryRevenue > 0">
                <span>Delivery Transit Fee</span>
                <strong>\${{ detail.deliveryRevenue | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.setupRevenue > 0">
                <span>On-Site Setup & Staging Fee</span>
                <strong>\${{ detail.setupRevenue | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.breakdownRevenue > 0">
                <span>Post-Event Breakdown Fee</span>
                <strong>\${{ detail.breakdownRevenue | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.serviceRevenue > 0">
                <span>Specialized Service Fee</span>
                <strong>\${{ detail.serviceRevenue | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row text-danger" *ngIf="detail.discountAmount > 0">
                <span>Promotional Discount</span>
                <strong>-\${{ detail.discountAmount | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.taxAmount > 0">
                <span>Collected Sales Tax</span>
                <strong>\${{ detail.taxAmount | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row total-row">
                <span>Total Booked Invoiced</span>
                <strong>\${{ detail.totalRevenue | number:'1.2-2' }}</strong>
              </div>
            </div>
          </div>

          <!-- Direct Cost Allocation Breakdown -->
          <div class="card">
            <div class="card-header">
              <h3>Direct Cost Allocations</h3>
            </div>
            <div class="item-list">
              <div class="item-row">
                <span>Inventory Asset Depreciation / Amortization</span>
                <strong>\${{ detail.inventoryAllocationCost | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.deliveryVehicleCost > 0">
                <span>Delivery Fleet Vehicle & Transit Fuel</span>
                <strong>\${{ detail.deliveryVehicleCost | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.pickupVehicleCost > 0">
                <span>Return Retrieval Vehicle Cost</span>
                <strong>\${{ detail.pickupVehicleCost | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row" *ngIf="detail.laborCost > 0">
                <span>Field Operator Setup & Strike Labor</span>
                <strong>\${{ detail.laborCost | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row text-danger" *ngIf="detail.repairAndDamageCost > 0">
                <span>Attributable Damage & Repair Cost</span>
                <strong>\${{ detail.repairAndDamageCost | number:'1.2-2' }}</strong>
              </div>
              <div class="item-row total-row">
                <span>Total Direct Operational Costs</span>
                <strong>\${{ detail.totalDirectCost | number:'1.2-2' }}</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- Cost Sources Transparency Table -->
        <div class="card">
          <div class="card-header">
            <h3>Authoritative Cost Source Explanations</h3>
          </div>
          <div class="table-responsive">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Cost Category</th>
                  <th>Allocated Amount</th>
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
    .breakdown-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .card { background: #1e293b; border: 1px solid #334155; border-radius: 10px; padding: 1.25rem; }
    .card-header { margin-bottom: 1rem; }
    .card-header h3 { font-size: 1.1rem; font-weight: 600; margin: 0; color: #f8fafc; }
    .item-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .item-row { display: flex; justify-content: space-between; font-size: 0.85rem; padding-bottom: 0.5rem; border-bottom: 1px solid #33415544; }
    .total-row { border-top: 1px solid #475569; border-bottom: none; padding-top: 0.75rem; font-size: 1rem; font-weight: 700; color: #ffffff; }
    .table-responsive { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left; }
    .data-table th { color: #94a3b8; font-weight: 600; padding: 0.75rem; border-bottom: 1px solid #334155; }
    .data-table td { padding: 0.75rem; border-bottom: 1px solid #1e293b; color: #cbd5e1; }
    .font-medium { font-weight: 500; color: #f8fafc; }
    .font-bold { font-weight: 700; }
    .text-muted { color: #94a3b8; }
    .text-primary { color: #60a5fa; }
    .text-success { color: #10b981; }
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
export class BookingProfitDetailComponent implements OnInit {
  bookingId = '';
  detail: BookingProfitDetail | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private analyticsService: AnalyticsService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.bookingId = params['id'];
      if (this.bookingId) {
        this.loadDetail();
      }
    });
  }

  loadDetail(): void {
    this.loading = true;
    this.analyticsService.getBookingProfitDetail(this.bookingId).subscribe({
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
