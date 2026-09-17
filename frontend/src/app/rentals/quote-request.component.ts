import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StorefrontService, Cart } from '../services/storefront.service';

@Component({
  selector: 'app-quote-request',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="quote-req-layout">
      <header class="req-header">
        <h1>Request a Rental Quote</h1>
        <p>Fill in event details and our team will prepare a formal proposal</p>
      </header>

      <div class="req-container">
        <div class="req-grid">
          <!-- Form Section -->
          <div class="form-card">
            <h2>Event & Delivery Information</h2>

            <div class="form-row">
              <div class="form-group">
                <label>Event Name *</label>
                <input type="text" [(ngModel)]="eventName" placeholder="e.g. Corporate Gala 2026" class="form-control" />
              </div>
              <div class="form-group">
                <label>Event Type *</label>
                <select [(ngModel)]="eventType" class="form-control">
                  <option value="WEDDING">WEDDING</option>
                  <option value="CORPORATE">CORPORATE</option>
                  <option value="PARTY">PARTY</option>
                  <option value="CONFERENCE">CONFERENCE</option>
                  <option value="TRADE_SHOW">TRADE_SHOW</option>
                  <option value="PHOTO_SHOOT">PHOTO_SHOOT</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Rental Start Date *</label>
                <input type="date" [(ngModel)]="startDate" class="form-control" />
              </div>
              <div class="form-group">
                <label>Rental End Date *</label>
                <input type="date" [(ngModel)]="endDate" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label>Delivery Address *</label>
              <input type="text" [(ngModel)]="deliveryAddress" placeholder="Street Address, City, State, ZIP" class="form-control" />
            </div>

            <h3>Contact Details</h3>

            <div class="form-row">
              <div class="form-group">
                <label>Full Name / Contact Person *</label>
                <input type="text" [(ngModel)]="guestName" placeholder="Emily Brown" class="form-control" />
              </div>
              <div class="form-group">
                <label>Company / Organization</label>
                <input type="text" [(ngModel)]="guestCompany" placeholder="ABC Events LLC" class="form-control" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Email Address *</label>
                <input type="email" [(ngModel)]="guestEmail" placeholder="emily@abcevents.com" class="form-control" />
              </div>
              <div class="form-group">
                <label>Phone Number *</label>
                <input type="tel" [(ngModel)]="guestPhone" placeholder="(555) 019-2831" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label>Special Instructions / Notes</label>
              <textarea [(ngModel)]="notes" rows="3" placeholder="Please deliver before 10 AM, venue has loading dock behind main hall..." class="form-control"></textarea>
            </div>
          </div>

          <!-- Summary & Submit -->
          <div class="summary-card">
            <h2>Requested Items</h2>

            <div class="items-list" *ngIf="cart && cart.items">
              <div class="item-row" *ngFor="let item of cart.items">
                <div>
                  <strong>{{ item.quantity }}x {{ item.productName }}</strong>
                  <span class="sku">SKU: {{ item.sku }}</span>
                </div>
                <span>\${{ item.lineSubtotal | number:'1.2-2' }}</span>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="est-row">
              <span>Estimated Total</span>
              <span class="est-amount">\${{ cart?.estimatedTotal || 0 | number:'1.2-2' }}</span>
            </div>

            <button class="btn-submit" (click)="submitRequest()" [disabled]="submitting">
              {{ submitting ? 'Submitting Request...' : 'Submit Quote Request' }}
            </button>

            <p class="terms-note">
              No payment is charged today. Our sales team will review availability and send a final quote to your email.
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .quote-req-layout {
      min-height: 100vh;
      background: #f8fafc;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .req-header {
      background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
      color: white;
      padding: 2.5rem 3rem;
      text-align: center;
    }
    .req-header h1 {
      margin: 0;
      font-size: 2rem;
      font-weight: 800;
    }
    .req-header p {
      color: #94a3b8;
      margin: 0.5rem 0 0 0;
    }
    .req-container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 2rem;
    }
    .req-grid {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 2rem;
    }
    .form-card, .summary-card {
      background: white;
      border-radius: 16px;
      padding: 2rem;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
      border: 1px solid #cbd5e1;
    }
    .form-card h2, .summary-card h2 {
      margin-top: 0;
      font-size: 1.25rem;
      color: #0f172a;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 0.75rem;
      margin-bottom: 1.5rem;
    }
    .form-card h3 {
      font-size: 1.05rem;
      color: #334155;
      margin: 1.5rem 0 1rem 0;
    }
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    .form-group {
      margin-bottom: 1.25rem;
    }
    .form-group label {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: #475569;
      margin-bottom: 0.35rem;
    }
    .form-control {
      width: 100%;
      padding: 0.65rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      box-sizing: border-box;
      font-size: 0.9rem;
    }
    .items-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .item-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.9rem;
    }
    .sku {
      display: block;
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .summary-divider {
      height: 1px;
      background: #e2e8f0;
      margin: 1.25rem 0;
    }
    .est-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      font-weight: 700;
      color: #0f172a;
    }
    .est-amount {
      font-size: 1.4rem;
      color: #2563eb;
      font-weight: 800;
    }
    .btn-submit {
      width: 100%;
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.9rem;
      border-radius: 8px;
      font-weight: 700;
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .btn-submit:hover { background: #1d4ed8; }
    .btn-submit:disabled { background: #94a3b8; cursor: not-allowed; }
    .terms-note {
      font-size: 0.8rem;
      color: #64748b;
      margin-top: 1rem;
      text-align: center;
      line-height: 1.4;
    }

    @media (max-width: 900px) {
      .req-grid { grid-template-columns: 1fr; }
      .form-row { grid-template-columns: 1fr; }
    }
  `]
})
export class QuoteRequestComponent implements OnInit {
  cart: Cart | null = null;
  submitting: boolean = false;

  eventName: string = 'Wedding Reception';
  eventType: string = 'WEDDING';
  startDate: string = new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0];
  endDate: string = new Date(Date.now() + 86400000 * 5).toISOString().split('T')[0];
  deliveryAddress: string = '742 Evergreen Terrace, Springfield, IL';
  guestName: string = 'Emily Brown';
  guestCompany: string = 'ABC Events LLC';
  guestEmail: string = 'emily@abcevents.demo';
  guestPhone: string = '(555) 019-2831';
  notes: string = 'Please deliver before 10 AM on event day.';

  idempotencyKey: string = '';
  errorMessage: string | null = null;

  constructor(private storefrontService: StorefrontService, private router: Router) {}

  ngOnInit(): void {
    if (!this.idempotencyKey) {
      this.idempotencyKey = (typeof crypto !== 'undefined' && crypto.randomUUID) ? crypto.randomUUID() : 'quo-' + Date.now();
    }
    this.storefrontService.getCart().subscribe({
      next: (c) => this.cart = c
    });
  }

  submitRequest(): void {
    if (!this.eventName || !this.guestEmail) {
      alert('Please complete all required fields.');
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    const requestPayload = {
      idempotencyKey: this.idempotencyKey,
      startDate: this.startDate,
      endDate: this.endDate,
      eventName: this.eventName,
      eventType: this.eventType,
      deliveryAddressText: this.deliveryAddress,
      guestName: this.guestName,
      guestEmail: this.guestEmail,
      guestPhone: this.guestPhone,
      guestCompany: this.guestCompany,
      notes: this.notes,
      items: this.cart?.items ? this.cart.items.map(i => ({ productId: i.productId, quantity: i.quantity })) : []
    };

    this.storefrontService.submitQuoteRequest(requestPayload, this.idempotencyKey).subscribe({
      next: (res) => {
        this.submitting = false;
        this.router.navigate(['/quote-request/success'], { queryParams: { reqNo: res.quoteNumber || 'REQ-000123' } });
      },
      error: (err) => {
        this.submitting = false;
        if (err.status === 409) {
          alert('This request has changed since it was first submitted. Please start a new checkout.');
        } else {
          alert('Failed to submit quote request: ' + (err.error?.message || err.message));
        }
      }
    });
  }
}
