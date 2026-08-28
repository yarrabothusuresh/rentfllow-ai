import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-checkout-success',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="success-layout">
      <div class="success-card">
        <div class="check-icon">✓</div>
        <h1>BOOKING CONFIRMED</h1>
        <p class="bkg-number">{{ bookingNumber }}</p>

        <p class="thank-you">Thank you! Your rental has been confirmed & inventory is reserved.</p>

        <div class="details-box">
          <div class="detail-row">
            <span>Event:</span>
            <strong>{{ eventName }}</strong>
          </div>
          <div class="detail-row">
            <span>Date:</span>
            <strong>August 31, 2026</strong>
          </div>
          <div class="detail-row">
            <span>Total:</span>
            <strong>\${{ totalAmount | number:'1.2-2' }}</strong>
          </div>
          <div class="detail-row">
            <span>Payment Status:</span>
            <strong class="paid-text">PAID</strong>
          </div>
          <div class="detail-row">
            <span>Scheduled Delivery:</span>
            <strong>Aug 31, 10 AM – 12 PM</strong>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn-primary" routerLink="/portal/bookings">View Booking</button>
          <button class="btn-secondary" routerLink="/portal/invoices">View Invoice</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .success-layout {
      min-height: 100vh;
      background: #f8fafc;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 2rem;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .success-card {
      background: white;
      border-radius: 20px;
      padding: 3rem;
      max-width: 540px;
      width: 100%;
      text-align: center;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08);
      border: 1px solid #cbd5e1;
    }
    .check-icon {
      width: 70px;
      height: 70px;
      background: #dcfce7;
      color: #16a34a;
      font-size: 2.5rem;
      font-weight: 800;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1.5rem auto;
    }
    h1 {
      margin: 0;
      font-size: 1.75rem;
      color: #0f172a;
    }
    .bkg-number {
      font-size: 1.25rem;
      font-weight: 700;
      color: #2563eb;
      margin: 0.25rem 0 1rem 0;
    }
    .thank-you {
      color: #475569;
      margin-bottom: 2rem;
    }
    .details-box {
      background: #f8fafc;
      padding: 1.5rem;
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      margin-bottom: 2rem;
      text-align: left;
    }
    .detail-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.75rem;
      font-size: 0.95rem;
    }
    .detail-row:last-child { margin-bottom: 0; }
    .detail-row span { color: #64748b; }
    .detail-row strong { color: #0f172a; }
    .paid-text { color: #16a34a !important; }
    .action-buttons {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    .btn-primary {
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.85rem;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
    }
    .btn-secondary {
      background: #f1f5f9;
      color: #334155;
      border: 1px solid #cbd5e1;
      padding: 0.85rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
    }
  `]
})
export class CheckoutSuccessComponent implements OnInit {
  bookingNumber: string = 'BOOK-000123';
  eventName: string = 'Wedding Reception';
  totalAmount: number = 2450;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['bkgNo']) this.bookingNumber = params['bkgNo'];
      if (params['event']) this.eventName = params['event'];
      if (params['total']) this.totalAmount = parseFloat(params['total']);
    });
  }
}
