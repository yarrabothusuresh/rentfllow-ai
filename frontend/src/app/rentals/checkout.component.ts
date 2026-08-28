import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StorefrontService, Cart } from '../services/storefront.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="checkout-layout">
      <header class="checkout-header">
        <h1>Online Checkout</h1>
        <p>Complete your booking details & confirm your equipment rental</p>
      </header>

      <div class="checkout-container">
        <div class="checkout-grid">
          <!-- Multi-Step Form -->
          <div class="steps-card">
            <!-- Step Navigation Pills -->
            <div class="step-pills">
              <span [class.active]="currentStep === 1">1. Dates & Address</span>
              <span [class.active]="currentStep === 2">2. Event Info</span>
              <span [class.active]="currentStep === 3">3. Review & Pay</span>
            </div>

            <!-- Step 1: Dates & Addresses -->
            <div *ngIf="currentStep === 1" class="step-content">
              <h2>1. Rental Dates & Delivery Address</h2>
              
              <div class="form-row">
                <div class="form-group">
                  <label>Start Date</label>
                  <input type="date" [(ngModel)]="startDate" class="form-control" />
                </div>
                <div class="form-group">
                  <label>End Date</label>
                  <input type="date" [(ngModel)]="endDate" class="form-control" />
                </div>
              </div>

              <div class="form-group">
                <label>Delivery Address</label>
                <input type="text" [(ngModel)]="deliveryAddress" placeholder="Street, City, State, ZIP" class="form-control" />
              </div>

              <div class="form-group">
                <label>Delivery Instructions / Venue Details</label>
                <input type="text" [(ngModel)]="instructions" placeholder="e.g. Loading dock at rear of main hall" class="form-control" />
              </div>

              <button class="btn-next" (click)="currentStep = 2">Next: Event Details →</button>
            </div>

            <!-- Step 2: Event Details -->
            <div *ngIf="currentStep === 2" class="step-content">
              <h2>2. Event Information</h2>

              <div class="form-row">
                <div class="form-group">
                  <label>Event Name</label>
                  <input type="text" [(ngModel)]="eventName" class="form-control" />
                </div>
                <div class="form-group">
                  <label>Event Type</label>
                  <select [(ngModel)]="eventType" class="form-control">
                    <option value="WEDDING">WEDDING</option>
                    <option value="CORPORATE">CORPORATE</option>
                    <option value="PARTY">PARTY</option>
                    <option value="CONFERENCE">CONFERENCE</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Contact Name</label>
                  <input type="text" [(ngModel)]="customerName" class="form-control" />
                </div>
                <div class="form-group">
                  <label>Email</label>
                  <input type="email" [(ngModel)]="customerEmail" class="form-control" />
                </div>
              </div>

              <div class="step-buttons">
                <button class="btn-back" (click)="currentStep = 1">← Back</button>
                <button class="btn-next" (click)="currentStep = 3">Next: Review & Payment →</button>
              </div>
            </div>

            <!-- Step 3: Review & Payment -->
            <div *ngIf="currentStep === 3" class="step-content">
              <h2>3. Payment & Confirmation</h2>

              <div class="payment-options">
                <label class="radio-card" [class.selected]="paymentMethod === 'PAYMENT_PENDING'">
                  <input type="radio" [(ngModel)]="paymentMethod" value="PAYMENT_PENDING" />
                  <div>
                    <strong>Pay Later / Invoice</strong>
                    <p>Receive an invoice with 30-day payment terms</p>
                  </div>
                </label>

                <label class="radio-card" [class.selected]="paymentMethod === 'PAYMENT_PAID'">
                  <input type="radio" [(ngModel)]="paymentMethod" value="PAYMENT_PAID" />
                  <div>
                    <strong>Instant Card / Demo Payment</strong>
                    <p>Process instant rental deposit & confirm booking immediately</p>
                  </div>
                </label>
              </div>

              <div class="step-buttons">
                <button class="btn-back" (click)="currentStep = 2">← Back</button>
                <button class="btn-confirm" (click)="confirmBooking()" [disabled]="processing">
                  {{ processing ? 'Processing...' : 'Confirm & Complete Booking' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Summary Card -->
          <div class="summary-card">
            <h2>Order Summary</h2>

            <div class="items-preview" *ngIf="cart && cart.items">
              <div class="preview-item" *ngFor="let item of cart.items">
                <span>{{ item.quantity }}x {{ item.productName }}</span>
                <span>\${{ item.lineSubtotal | number:'1.2-2' }}</span>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row">
              <span>Rental Subtotal</span>
              <span>\${{ cart?.subtotal || 0 | number:'1.2-2' }}</span>
            </div>
            <div class="summary-row">
              <span>Est. Tax (8.25%)</span>
              <span>\${{ cart?.estimatedTax || 0 | number:'1.2-2' }}</span>
            </div>
            <div class="summary-row total-row">
              <span>Total Due</span>
              <span class="total-amount">\${{ cart?.estimatedTotal || 0 | number:'1.2-2' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .checkout-layout {
      min-height: 100vh;
      background: #f8fafc;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .checkout-header {
      background: #1e293b;
      color: white;
      padding: 2.5rem 3rem;
      text-align: center;
    }
    .checkout-header h1 { margin: 0; font-size: 2rem; font-weight: 800; }
    .checkout-header p { color: #94a3b8; margin: 0.5rem 0 0 0; }
    .checkout-container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 2rem;
    }
    .checkout-grid {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 2rem;
    }
    .steps-card, .summary-card {
      background: white;
      border-radius: 16px;
      padding: 2rem;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
      border: 1px solid #cbd5e1;
    }
    .step-pills {
      display: flex;
      gap: 1rem;
      margin-bottom: 2rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1rem;
    }
    .step-pills span {
      font-size: 0.85rem;
      font-weight: 600;
      color: #94a3b8;
      padding: 0.4rem 0.75rem;
      border-radius: 20px;
    }
    .step-pills span.active {
      background: #eff6ff;
      color: #2563eb;
    }
    .step-content h2 {
      margin-top: 0;
      font-size: 1.25rem;
      color: #0f172a;
      margin-bottom: 1.5rem;
    }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { margin-bottom: 1.25rem; }
    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 0.35rem; }
    .form-control { width: 100%; padding: 0.65rem; border: 1px solid #cbd5e1; border-radius: 8px; box-sizing: border-box; }
    .step-buttons { display: flex; justify-content: space-between; margin-top: 2rem; }
    .btn-next, .btn-confirm { background: #2563eb; color: white; border: none; padding: 0.85rem 1.5rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
    .btn-back { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; padding: 0.85rem 1.5rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .payment-options { display: flex; flex-direction: column; gap: 1rem; margin-bottom: 2rem; }
    .radio-card { display: flex; gap: 1rem; padding: 1.25rem; border: 2px solid #e2e8f0; border-radius: 12px; cursor: pointer; }
    .radio-card.selected { border-color: #2563eb; background: #eff6ff; }
    .summary-card h2 { margin-top: 0; font-size: 1.2rem; border-bottom: 1px solid #e2e8f0; padding-bottom: 0.75rem; }
    .items-preview { display: flex; flex-direction: column; gap: 0.6rem; font-size: 0.9rem; }
    .preview-item { display: flex; justify-content: space-between; }
    .summary-divider { height: 1px; background: #e2e8f0; margin: 1.25rem 0; }
    .summary-row { display: flex; justify-content: space-between; margin-bottom: 0.75rem; color: #475569; font-size: 0.9rem; }
    .total-row { font-weight: 800; color: #0f172a; font-size: 1.1rem; }
    .total-amount { color: #2563eb; font-size: 1.35rem; }

    @media (max-width: 900px) {
      .checkout-grid { grid-template-columns: 1fr; }
      .form-row { grid-template-columns: 1fr; }
    }
  `]
})
export class CheckoutComponent implements OnInit {
  currentStep: number = 1;
  cart: Cart | null = null;
  processing: boolean = false;

  startDate: string = new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0];
  endDate: string = new Date(Date.now() + 86400000 * 5).toISOString().split('T')[0];
  deliveryAddress: string = '742 Evergreen Terrace, Springfield, IL';
  instructions: string = 'Deliver to rear loading dock';

  eventName: string = 'Wedding Reception';
  eventType: string = 'WEDDING';
  customerName: string = 'Emily Brown';
  customerEmail: string = 'emily@abcevents.demo';

  paymentMethod: string = 'PAYMENT_PENDING';

  constructor(private storefrontService: StorefrontService, private router: Router) {}

  ngOnInit(): void {
    this.storefrontService.getCart().subscribe({
      next: (c) => this.cart = c
    });
  }

  confirmBooking(): void {
    this.processing = true;
    setTimeout(() => {
      this.processing = false;
      this.router.navigate(['/checkout/success'], {
        queryParams: {
          bkgNo: 'BOOK-000123',
          event: this.eventName,
          total: this.cart?.estimatedTotal || 2450
        }
      });
    }, 800);
  }
}
