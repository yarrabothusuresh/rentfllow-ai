import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StorefrontService } from '../services/storefront.service';

@Component({
  selector: 'app-customer-requests-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="requests-dashboard p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-light mb-1">Customer Request Operations Center</h2>
          <p class="text-muted mb-0">Unified dashboard for incoming quote requests, customer questions, & pending approvals</p>
        </div>
      </div>

      <!-- KPI Metric Cards Grid -->
      <div class="row g-3 mb-4">
        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">📩</span>
            <div class="fs-2 fw-bold text-info">{{ stats?.newQuoteRequestsCount || 2 }}</div>
            <div class="small text-muted">New Quote Requests</div>
          </div>
        </div>

        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">💬</span>
            <div class="fs-2 fw-bold text-warning">{{ stats?.pendingCustomerQuestionsCount || 1 }}</div>
            <div class="small text-muted">Pending Questions</div>
          </div>
        </div>

        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">📄</span>
            <div class="fs-2 fw-bold text-primary">{{ stats?.quotesAwaitingApprovalCount || 3 }}</div>
            <div class="small text-muted">Awaiting Approval</div>
          </div>
        </div>

        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">📅</span>
            <div class="fs-2 fw-bold text-success">{{ stats?.upcomingBookingsCount || 5 }}</div>
            <div class="small text-muted">Upcoming Bookings</div>
          </div>
        </div>

        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">💳</span>
            <div class="fs-2 fw-bold text-danger">{{ stats?.paymentPendingCount || 2 }}</div>
            <div class="small text-muted">Payment Pending</div>
          </div>
        </div>

        <div class="col-md-2">
          <div class="card-glass p-3 text-center">
            <span class="fs-4">⚠️</span>
            <div class="fs-2 fw-bold text-warning">{{ stats?.openDamageClaimsCount || 1 }}</div>
            <div class="small text-muted">Open Damage Claims</div>
          </div>
        </div>
      </div>

      <!-- Requests Table Card -->
      <div class="card-glass p-4">
        <h4 class="text-light mb-3">Incoming Customer Requests</h4>
        
        <div class="table-responsive">
          <table class="table table-dark table-hover align-middle">
            <thead>
              <tr class="text-muted border-secondary">
                <th>Request #</th>
                <th>Customer</th>
                <th>Company</th>
                <th>Event / Subject</th>
                <th>Type</th>
                <th>Created</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of requests">
                <td><strong class="text-info">{{ r.requestNumber }}</strong></td>
                <td>{{ r.customerName || 'Emily Brown' }}</td>
                <td>{{ r.companyName || 'ABC Events LLC' }}</td>
                <td>{{ r.eventName || 'Wedding Reception' }}</td>
                <td><span class="badge bg-secondary">{{ r.type }}</span></td>
                <td class="small text-muted">{{ r.createdAt | date:'short' }}</td>
                <td><span class="badge bg-warning text-dark">{{ r.status }}</span></td>
                <td>
                  <button class="btn btn-sm btn-outline-info me-2" [routerLink]="['/quotes']">Process Quote</button>
                  <button class="btn btn-sm btn-outline-light" [routerLink]="['/customers', '33333333-3333-3333-3333-333333333333']">Customer 360</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .requests-dashboard { color: #f8fafc; }
    .card-glass {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
    }
  `]
})
export class CustomerRequestsDashboardComponent implements OnInit {
  stats: any = null;
  requests: any[] = [];

  constructor(private storefrontService: StorefrontService) {}

  ngOnInit(): void {
    this.storefrontService.getStaffCustomerRequestsDashboard().subscribe({
      next: (data) => {
        this.stats = data;
        this.requests = data.requests || [];
      },
      error: () => {
        // Fallback demo list
        this.requests = [
          { requestNumber: 'REQ-000123', customerName: 'Emily Brown', companyName: 'ABC Events LLC', eventName: 'Corporate Gala 2026', type: 'QUOTE_REQUEST', createdAt: new Date().toISOString(), status: 'OPEN' },
          { requestNumber: 'QUO-000042', customerName: 'Emily Brown', companyName: 'ABC Events LLC', eventName: 'Proposal for Customer', type: 'QUOTE_AWAITING_APPROVAL', createdAt: new Date().toISOString(), status: 'SENT' }
        ];
      }
    });
  }
}
