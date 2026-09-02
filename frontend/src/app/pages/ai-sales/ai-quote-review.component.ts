import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiQuoteReview, MarginStatus } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-quote-review',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="quote-review-container" *ngIf="review">
      <!-- Top Navigation & Status -->
      <header class="review-header">
        <div>
          <div class="breadcrumb">
            <a routerLink="/ai-sales">&larr; AI Sales Console</a>
            <span class="sep">/</span>
            <span class="curr">Quote Review</span>
          </div>
          <div class="title-row">
            <h1>Review AI-Drafted Quote: <code>{{ review.quoteNumber }}</code></h1>
            <span class="badge" [ngClass]="review.status === 'SENT' ? 'badge-sent' : 'badge-draft'">
              {{ review.status }}
            </span>
          </div>
          <p class="subtitle">Human authorization gateway before commercial quote issuance to client.</p>
        </div>

        <div class="approval-actions" *ngIf="review.status === 'DRAFT'">
          <button (click)="rejectQuote()" class="btn btn-outline-danger" [disabled]="isActionLoading">
            <i class="bi bi-x-circle"></i> Reject Draft
          </button>
          <a [routerLink]="['/quotes', review.quoteId, 'builder']" class="btn btn-secondary">
            <i class="bi bi-pencil"></i> Edit in Builder
          </a>
          <button (click)="approveAndSend()" class="btn btn-success btn-lg" [disabled]="isActionLoading">
            <i class="bi bi-check-circle-fill"></i> Approve & Send to Customer
          </button>
        </div>

        <div class="approved-banner" *ngIf="review.status === 'SENT'">
          <i class="bi bi-check-all"></i> This quote has been approved and issued to the customer.
        </div>
      </header>

      <!-- Alert if Low Margin -->
      <div class="alert-banner" *ngIf="review.warnings?.length">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <div class="alert-content">
          <strong>Review Notice:</strong>
          <ul>
            <li *ngFor="let w of review.warnings">{{ w }}</li>
          </ul>
        </div>
      </div>

      <!-- Main Layout: 2 Columns -->
      <div class="review-grid">
        <!-- Left: Line Items & Customer Specs -->
        <div class="main-content">
          <!-- Customer & Event Context -->
          <div class="card">
            <h3><i class="bi bi-calendar-event"></i> Event & Delivery Logistics</h3>
            <div class="spec-grid">
              <div class="spec-item">
                <span class="label">Customer</span>
                <strong>{{ review.customerName || 'Prospect Guest' }}</strong>
                <span class="sub" *ngIf="review.customerEmail">{{ review.customerEmail }}</span>
              </div>
              <div class="spec-item">
                <span class="label">Rental Window</span>
                <strong>{{ review.rentalStart | date:'mediumDate' }} - {{ review.rentalEnd | date:'mediumDate' }}</strong>
                <span class="sub">Standard Event Duration</span>
              </div>
              <div class="spec-item">
                <span class="label">Delivery Location</span>
                <strong>{{ review.deliveryAddress || 'Client Pickup / TBD' }}</strong>
              </div>
            </div>
          </div>

          <!-- Proposed Equipment Items -->
          <div class="card">
            <h3><i class="bi bi-box-seam"></i> Proposed Equipment & Availability Verification</h3>
            <table class="items-table">
              <thead>
                <tr>
                  <th>Product Description</th>
                  <th>Quantity</th>
                  <th>Unit Rate</th>
                  <th>Line Total</th>
                  <th>Stock Verification</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of review.items">
                  <td>
                    <strong>{{ item.name }}</strong>
                    <span class="sku" *ngIf="item.sku">SKU: {{ item.sku }}</span>
                  </td>
                  <td>{{ item.quantity }}</td>
                  <td>{{ item.unitPrice | currency }}</td>
                  <td><strong>{{ item.lineTotal | currency }}</strong></td>
                  <td>
                    <span class="stock-badge stock-ok" *ngIf="item.available">
                      <i class="bi bi-check-circle"></i> In Stock ({{ item.availableQuantity }} avail)
                    </span>
                    <span class="stock-badge stock-short" *ngIf="!item.available">
                      <i class="bi bi-exclamation-circle"></i> Shortage
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- AI Recommendation Note -->
          <div class="card ai-note-card" *ngIf="review.aiRecommendationNotes">
            <h3><i class="bi bi-robot"></i> AI Sales Agent Rationale</h3>
            <p>{{ review.aiRecommendationNotes }}</p>
          </div>
        </div>

        <!-- Right: Commercial Summary & Internal Margin Analysis -->
        <div class="side-content">
          <!-- Financial Summary -->
          <div class="card">
            <h3><i class="bi bi-cash-stack"></i> Commercial Pricing</h3>
            <div class="pricing-rows">
              <div class="p-row">
                <span>Equipment Subtotal</span>
                <span>{{ review.subtotal | currency }}</span>
              </div>
              <div class="p-row" *ngIf="review.deliveryFee > 0">
                <span>Delivery & Logistics</span>
                <span>{{ review.deliveryFee | currency }}</span>
              </div>
              <div class="p-row" *ngIf="review.setupFee > 0">
                <span>Setup & Teardown</span>
                <span>{{ review.setupFee | currency }}</span>
              </div>
              <div class="p-row" *ngIf="review.discountAmount > 0">
                <span>Discounts Applied</span>
                <span class="discount-val">-{{ review.discountAmount | currency }}</span>
              </div>
              <div class="p-row">
                <span>Sales Tax</span>
                <span>{{ review.taxAmount | currency }}</span>
              </div>
              <div class="p-row total-row">
                <span>Grand Total</span>
                <span>{{ review.totalAmount | currency }}</span>
              </div>
            </div>
          </div>

          <!-- Internal Profitability (Staff Only) -->
          <div class="card profit-card">
            <div class="card-header-inner">
              <h3><i class="bi bi-shield-lock"></i> Internal Profitability Analysis</h3>
              <span class="badge badge-staff">STAFF CONFIDENTIAL</span>
            </div>
            <p class="desc-text">Calculated depreciation, wear & dispatch turnaround labor costs.</p>

            <div class="margin-gauge">
              <div class="gauge-header">
                <span class="label">Gross Margin</span>
                <strong class="gauge-pct" [ngClass]="getMarginClass(review.marginStatus)">
                  {{ review.estimatedMarginPct | number:'1.1-1' }}%
                </strong>
              </div>
              <div class="progress-bar-bg">
                <div
                  class="progress-fill"
                  [ngClass]="getMarginClass(review.marginStatus)"
                  [style.width.%]="review.estimatedMarginPct > 100 ? 100 : (review.estimatedMarginPct < 0 ? 0 : review.estimatedMarginPct)"
                ></div>
              </div>
              <span class="target-sub">Target: {{ review.targetMarginPct }}% &bull; Status: <strong>{{ review.marginStatus }}</strong></span>
            </div>

            <div class="profit-breakdown">
              <div class="p-row">
                <span>Estimated Revenue:</span>
                <strong>{{ review.totalAmount | currency }}</strong>
              </div>
              <div class="p-row">
                <span>Internal Turn Cost:</span>
                <span>{{ review.estimatedCost | currency }}</span>
              </div>
              <div class="p-row total-profit">
                <span>Estimated Profit:</span>
                <strong>{{ review.estimatedProfit | currency }}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .quote-review-container {
      padding: 1.5rem;
      max-width: 1350px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb a { color: #3b82f6; text-decoration: none; font-size: 0.85rem; }
    .breadcrumb .sep { margin: 0 0.4rem; color: #94a3b8; }
    .breadcrumb .curr { color: #64748b; font-size: 0.85rem; }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1.25rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .title-row { display: flex; align-items: center; gap: 0.75rem; margin-top: 0.25rem; }
    h1 { font-size: 1.65rem; font-weight: 700; margin: 0; color: #0f172a; }
    .subtitle { color: #64748b; margin-top: 0.25rem; font-size: 0.95rem; }

    .approval-actions { display: flex; gap: 0.75rem; align-items: center; }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      border: 1px solid transparent;
    }
    .btn-lg { padding: 0.65rem 1.25rem; font-size: 0.95rem; }
    .btn-success { background: #10b981; color: white; }
    .btn-success:hover { background: #059669; }
    .btn-secondary { background: #f8fafc; border-color: #cbd5e1; color: #334155; }
    .btn-outline-danger { background: transparent; border-color: #f87171; color: #dc2626; }

    .approved-banner {
      background: #ecfdf5;
      color: #065f46;
      border: 1px solid #a7f3d0;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .alert-banner {
      display: flex;
      gap: 0.75rem;
      background: #fffbeb;
      border: 1px solid #fde68a;
      border-radius: 8px;
      padding: 0.85rem 1.25rem;
      color: #92400e;
      margin-bottom: 1.5rem;
    }
    .alert-banner i { font-size: 1.25rem; color: #d97706; }
    .alert-content ul { margin: 0.25rem 0 0 1rem; padding: 0; }

    .review-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 1.5rem;
    }
    @media (max-width: 1024px) {
      .review-grid { grid-template-columns: 1fr; }
    }

    .card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      margin-bottom: 1.25rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    }
    .card h3 {
      font-size: 1rem;
      font-weight: 600;
      color: #0f172a;
      margin: 0 0 1rem 0;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .spec-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
    .spec-item { display: flex; flex-direction: column; }
    .spec-item .label { font-size: 0.75rem; text-transform: uppercase; color: #64748b; }
    .spec-item strong { font-size: 0.95rem; color: #1e293b; margin-top: 0.15rem; }
    .spec-item .sub { font-size: 0.75rem; color: #94a3b8; }

    .items-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .items-table th {
      text-align: left;
      padding: 0.65rem;
      background: #f8fafc;
      color: #64748b;
      font-weight: 600;
      font-size: 0.75rem;
      text-transform: uppercase;
      border-bottom: 1px solid #e2e8f0;
    }
    .items-table td { padding: 0.75rem 0.65rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
    .sku { font-size: 0.75rem; color: #94a3b8; display: block; font-family: monospace; }

    .stock-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .stock-ok { background: #f0fdf4; color: #166534; }
    .stock-short { background: #fef2f2; color: #991b1b; }

    .ai-note-card { border-left: 4px solid #3b82f6; background: #f8faff; }
    .ai-note-card p { margin: 0; font-size: 0.9rem; color: #334155; line-height: 1.5; }

    .p-row {
      display: flex;
      justify-content: space-between;
      padding: 0.4rem 0;
      font-size: 0.875rem;
      color: #475569;
    }
    .discount-val { color: #dc2626; font-weight: 600; }
    .total-row {
      border-top: 2px solid #e2e8f0;
      margin-top: 0.5rem;
      padding-top: 0.6rem;
      font-size: 1.1rem;
      font-weight: 700;
      color: #0f172a;
    }

    /* Profit card */
    .profit-card { border-left: 4px solid #8b5cf6; }
    .card-header-inner { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
    .card-header-inner h3 { margin: 0; }
    .badge-staff { background: #f3e8ff; color: #6b21a8; font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.4rem; border-radius: 4px; }
    .desc-text { font-size: 0.8rem; color: #64748b; margin: 0 0 1rem 0; }

    .margin-gauge { margin-bottom: 1rem; }
    .gauge-header { display: flex; justify-content: space-between; margin-bottom: 0.35rem; }
    .gauge-pct { font-size: 1.25rem; font-weight: 700; }
    .progress-bar-bg { height: 10px; background: #e2e8f0; border-radius: 9999px; overflow: hidden; }
    .progress-fill { height: 100%; transition: width 0.3s ease; }

    .margin-healthy { color: #16a34a; background: #22c55e; }
    .margin-acceptable { color: #2563eb; background: #3b82f6; }
    .margin-low { color: #d97706; background: #f59e0b; }
    .margin-loss { color: #dc2626; background: #ef4444; }

    .target-sub { font-size: 0.75rem; color: #64748b; margin-top: 0.35rem; display: block; }
    .profit-breakdown { border-top: 1px solid #f1f5f9; padding-top: 0.75rem; }
    .total-profit { border-top: 1px dashed #cbd5e1; margin-top: 0.4rem; padding-top: 0.4rem; font-size: 0.95rem; }

    .badge-draft { background: #fef3c7; color: #92400e; padding: 0.25rem 0.6rem; border-radius: 4px; font-weight: 600; font-size: 0.8rem; }
    .badge-sent { background: #dcfce7; color: #166534; padding: 0.25rem 0.6rem; border-radius: 4px; font-weight: 600; font-size: 0.8rem; }
  `]
})
export class AiQuoteReviewComponent implements OnInit {
  quoteId!: string;
  review: AiQuoteReview | null = null;
  isActionLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private aiSalesService: AiSalesService
  ) {}

  ngOnInit(): void {
    this.quoteId = this.route.snapshot.paramMap.get('id')!;
    this.loadReview();
  }

  loadReview(): void {
    this.aiSalesService.getQuoteReview(this.quoteId).subscribe({
      next: (r) => this.review = r,
      error: (err) => console.error('Failed to load quote review', err)
    });
  }

  approveAndSend(): void {
    if (!this.review) return;
    this.isActionLoading = true;
    this.aiSalesService.approveQuote(this.review.quoteId).subscribe({
      next: (res) => {
        this.isActionLoading = false;
        if (this.review) this.review.status = 'SENT';
      },
      error: (err) => {
        console.error('Failed to approve quote', err);
        this.isActionLoading = false;
      }
    });
  }

  rejectQuote(): void {
    if (!this.review) return;
    this.isActionLoading = true;
    this.aiSalesService.rejectQuote(this.review.quoteId).subscribe({
      next: () => {
        this.isActionLoading = false;
        if (this.review) this.review.status = 'CANCELLED';
      },
      error: () => this.isActionLoading = false
    });
  }

  getMarginClass(status: MarginStatus): string {
    switch (status) {
      case 'HEALTHY': return 'margin-healthy';
      case 'ACCEPTABLE': return 'margin-acceptable';
      case 'LOW_MARGIN': return 'margin-low';
      case 'LOSS_MAKING': return 'margin-loss';
      default: return 'margin-healthy';
    }
  }
}
