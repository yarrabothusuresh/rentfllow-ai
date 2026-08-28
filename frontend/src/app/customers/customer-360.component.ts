import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { StorefrontService } from '../services/storefront.service';

@Component({
  selector: 'app-customer-360',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="c360-container p-4">
      <!-- Header -->
      <div class="card-glass p-4 mb-4">
        <div class="row align-items-center">
          <div class="col-md-8">
            <div class="d-flex align-items-center gap-3">
              <div class="avatar-box bg-info text-dark fw-bold fs-3 rounded-circle p-3">
                {{ (data?.customerInfo?.firstName || 'E')[0] }}{{ (data?.customerInfo?.lastName || 'B')[0] }}
              </div>
              <div>
                <h2 class="text-light mb-1">{{ data?.customerInfo?.firstName }} {{ data?.customerInfo?.lastName }}</h2>
                <p class="text-info mb-0 fw-semibold">{{ data?.customerInfo?.companyName || 'ABC Events LLC' }} • CUS-000123</p>
                <small class="text-muted">✉️ {{ data?.customerInfo?.email }} • 📞 {{ data?.customerInfo?.phone }}</small>
              </div>
            </div>
          </div>
          <div class="col-md-4 text-md-end mt-3 mt-md-0">
            <div class="d-flex gap-3 justify-content-md-end">
              <div class="p-2 bg-dark rounded border border-secondary text-center">
                <small class="text-muted d-block">Lifetime Value</small>
                <span class="fs-4 fw-bold text-success">\${{ data?.totalLifetimeValue || 14850 | number:'1.2-2' }}</span>
              </div>
              <div class="p-2 bg-dark rounded border border-secondary text-center">
                <small class="text-muted d-block">Balance Due</small>
                <span class="fs-4 fw-bold text-danger">\${{ data?.currentOutstandingBalance || 1250 | number:'1.2-2' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation Tabs -->
      <div class="row g-4">
        <!-- Main Column: Rentals, Quotes, Invoices -->
        <div class="col-md-8">
          <!-- Active Rentals & Bookings -->
          <div class="card-glass p-4 mb-4">
            <h4 class="text-light mb-3">📅 Active & Upcoming Rentals</h4>
            <div class="list-group list-group-flush bg-transparent">
              <div class="list-group-item bg-transparent text-light border-secondary p-3 mb-2 rounded border">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h5 class="mb-1 text-info">BOOK-000123 • Wedding Reception</h5>
                    <small class="text-muted">Aug 31, 2026 – Sep 2, 2026 • 100 Chairs, 10 Tables</small>
                  </div>
                  <span class="badge bg-success">OUT ON RENT</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Quotes History -->
          <div class="card-glass p-4 mb-4">
            <h4 class="text-light mb-3">📄 Proposals & Quotes</h4>
            <div class="table-responsive">
              <table class="table table-dark table-hover">
                <thead>
                  <tr class="text-muted"><th>Quote #</th><th>Total</th><th>Status</th></tr>
                </thead>
                <tbody>
                  <tr>
                    <td class="text-info">QUO-000042</td>
                    <td>$2,450.00</td>
                    <td><span class="badge bg-success">ACCEPTED</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Invoices & Damage Claims -->
          <div class="card-glass p-4">
            <h4 class="text-light mb-3">⚠️ Damage Claims & Inspections</h4>
            <div class="alert alert-dark border-secondary text-warning">
              CLM-000101 • Customer review requested • Estimate: $185.00
            </div>
          </div>
        </div>

        <!-- Right Column: Addresses, Activity, Messages -->
        <div class="col-md-4">
          <!-- Addresses -->
          <div class="card-glass p-4 mb-4">
            <h5 class="text-light mb-3">📍 Saved Addresses</h5>
            <div class="small text-muted p-2 bg-dark rounded border border-secondary mb-2">
              <strong class="text-light">VENUE (Default):</strong><br/>
              742 Evergreen Terrace, Springfield, IL
            </div>
          </div>

          <!-- Activity Timeline -->
          <div class="card-glass p-4">
            <h5 class="text-light mb-3">📜 Customer Activity Timeline</h5>
            <div class="timeline border-start border-secondary ps-3">
              <div class="timeline-item mb-3">
                <small class="text-info d-block">Today 10:15 AM</small>
                <span class="text-light small">Approved Quote QUO-000042 online</span>
              </div>
              <div class="timeline-item">
                <small class="text-muted d-block">Yesterday 4:30 PM</small>
                <span class="text-light small">Submitted Quote Request REQ-000123</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .c360-container { color: #f8fafc; }
    .card-glass {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
    }
  `]
})
export class Customer360Component implements OnInit {
  customerId: string = '';
  data: any = null;

  constructor(private route: ActivatedRoute, private storefrontService: StorefrontService) {}

  ngOnInit(): void {
    this.customerId = this.route.snapshot.paramMap.get('id') || '';
    if (this.customerId) {
      this.storefrontService.getCustomer360(this.customerId).subscribe({
        next: (res) => this.data = res,
        error: () => {}
      });
    }
  }
}
