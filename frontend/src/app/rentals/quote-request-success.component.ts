import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-quote-request-success',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="success-layout">
      <div class="success-card">
        <div class="mail-icon">📩</div>
        <h1>Quote Request Received</h1>
        <p class="req-number">{{ requestNumber }}</p>

        <p class="subtitle">"We'll review your request and contact you within 24 hours with a custom proposal."</p>

        <div class="details-box">
          <div class="detail-row">
            <span>Event:</span>
            <strong>Corporate Gala 2026</strong>
          </div>
          <div class="detail-row">
            <span>Rental Dates:</span>
            <strong>Sep 10 – Sep 12, 2026</strong>
          </div>
          <div class="detail-row">
            <span>Status:</span>
            <strong class="pending-text">UNDER SALES REVIEW</strong>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn-primary" routerLink="/portal/quotes">View My Quotes</button>
          <button class="btn-secondary" routerLink="/portal">Portal Dashboard</button>
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
    .mail-icon {
      width: 70px;
      height: 70px;
      background: #dbeafe;
      color: #2563eb;
      font-size: 2.5rem;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1.5rem auto;
    }
    h1 { margin: 0; font-size: 1.75rem; color: #0f172a; }
    .req-number { font-size: 1.25rem; font-weight: 700; color: #2563eb; margin: 0.25rem 0 1rem 0; }
    .subtitle { color: #475569; margin-bottom: 2rem; font-style: italic; }
    .details-box { background: #f8fafc; padding: 1.5rem; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 2rem; text-align: left; }
    .detail-row { display: flex; justify-content: space-between; margin-bottom: 0.75rem; font-size: 0.95rem; }
    .detail-row:last-child { margin-bottom: 0; }
    .detail-row span { color: #64748b; }
    .detail-row strong { color: #0f172a; }
    .pending-text { color: #d97706 !important; }
    .action-buttons { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .btn-primary { background: #2563eb; color: white; border: none; padding: 0.85rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
    .btn-secondary { background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; padding: 0.85rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
  `]
})
export class QuoteRequestSuccessComponent implements OnInit {
  requestNumber: string = 'REQ-000123';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['reqNo']) this.requestNumber = params['reqNo'];
    });
  }
}
