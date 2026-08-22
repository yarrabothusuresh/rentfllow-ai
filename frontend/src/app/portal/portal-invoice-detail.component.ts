import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalInvoice } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-invoice-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <a routerLink="/portal/invoices" class="text-info text-decoration-none">← Back to Invoices</a>
        <button class="btn btn-outline-light btn-sm" (click)="printInvoice()">🖨️ Print / Save PDF</button>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && invoice" class="card-glass p-4" id="invoice-printable-area">
        <!-- Invoice Business Header -->
        <div class="row border-bottom border-secondary pb-4 mb-4">
          <div class="col-md-6">
            <h2 class="text-info font-weight-bold mb-1">Evergreen Event Rentals</h2>
            <p class="text-muted mb-0">Premium Party & Event Rental SaaS</p>
            <p class="text-muted mb-0">1234 Industrial Pkwy, Suite 100 • Dallas, TX 75201</p>
            <p class="text-muted mb-0">billing&#64;evergreenrentals.demo • (555) 019-2831</p>
          </div>
          <div class="col-md-6 text-md-end">
            <h1 class="text-light font-monospace mb-1">{{ invoice.invoiceNumber }}</h1>
            <span class="badge fs-6 mb-2" [ngClass]="getStatusBadgeClass(invoice.status)">{{ invoice.status }}</span>
            <p class="mb-1 text-muted">Issue Date: <strong>{{ invoice.issueDate }}</strong></p>
            <p class="mb-0 text-muted">Due Date: <strong>{{ invoice.dueDate }}</strong></p>
          </div>
        </div>

        <!-- Customer Billing Snapshot -->
        <div class="row mb-4">
          <div class="col-md-6">
            <h5 class="text-info mb-2">Billed To:</h5>
            <p class="mb-1 font-weight-bold text-light">{{ invoice.companyName || invoice.customerName }}</p>
            <p class="mb-1 text-muted">{{ invoice.customerName }}</p>
            <p class="mb-1 text-muted" *ngIf="invoice.billingAddress">{{ invoice.billingAddress }}</p>
            <p class="mb-1 text-muted" *ngIf="invoice.city">{{ invoice.city }}, {{ invoice.state }} {{ invoice.zipCode }}</p>
            <p class="mb-0 text-muted" *ngIf="invoice.email">{{ invoice.email }} • {{ invoice.phone }}</p>
          </div>
        </div>

        <!-- Line Items -->
        <h4 class="text-light mb-3">Line Items</h4>
        <div class="table-responsive mb-4">
          <table class="table table-dark table-striped align-middle">
            <thead>
              <tr>
                <th>Description</th>
                <th class="text-center">Qty</th>
                <th class="text-end">Unit Price</th>
                <th class="text-end">Line Total</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of invoice.items">
                <td>{{ item.description }}</td>
                <td class="text-center">{{ item.quantity }}</td>
                <td class="text-end">\${{ item.unitPrice | number:'1.2-2' }}</td>
                <td class="text-end font-weight-bold">\${{ item.lineTotal | number:'1.2-2' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Financial Totals -->
        <div class="row mb-5">
          <div class="col-md-6">
            <div *ngIf="invoice.notes" class="p-3 card-glass bg-dark">
              <small class="text-muted d-block">Invoice Notes:</small>
              <p class="mb-0 text-light">{{ invoice.notes }}</p>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card-glass p-3 ms-auto" style="max-width: 400px;">
              <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Subtotal:</span>
                <span>\${{ invoice.subtotal | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2 text-success" *ngIf="invoice.discount">
                <span>Discount:</span>
                <span>-\${{ invoice.discount | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2" *ngIf="invoice.fees">
                <span>Fees:</span>
                <span>\${{ invoice.fees | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2" *ngIf="invoice.tax">
                <span>Sales Tax (8.25%):</span>
                <span>\${{ invoice.tax | number:'1.2-2' }}</span>
              </div>
              <hr class="border-secondary my-2" />
              <div class="d-flex justify-content-between fs-5 font-weight-bold">
                <span class="text-light">Total Amount:</span>
                <span>\${{ invoice.totalAmount | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between text-success my-1">
                <span>Amount Paid:</span>
                <span>-\${{ invoice.amountPaid | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between fs-5 font-weight-bold text-warning border-top border-secondary pt-2">
                <span>BALANCE DUE:</span>
                <span>\${{ invoice.balanceDue | number:'1.2-2' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Payment History Table -->
        <h4 class="text-light mb-3">Payment History</h4>
        <div *ngIf="payments.length === 0" class="text-muted mb-4">No payments recorded against this invoice yet.</div>
        <div class="table-responsive" *ngIf="payments.length > 0">
          <table class="table table-dark table-bordered align-middle">
            <thead>
              <tr>
                <th>Payment Date</th>
                <th>Method</th>
                <th class="text-end">Amount</th>
                <th>Reference #</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let pay of payments">
                <td>{{ pay.paymentDate }}</td>
                <td>{{ pay.paymentMethod }}</td>
                <td class="text-end text-success font-weight-bold">\${{ pay.amount | number:'1.2-2' }}</td>
                <td class="font-monospace small">{{ pay.transactionReference || 'N/A' }}</td>
                <td><span class="badge bg-success">{{ pay.paymentStatus }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalInvoiceDetailComponent implements OnInit {
  invoice: CustomerPortalInvoice | null = null;
  payments: any[] = [];
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private portalService: CustomerPortalService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.portalService.getInvoiceDetail(id).subscribe({
        next: (data) => {
          this.invoice = data;
          this.isLoading = false;
          this.loadPayments(id);
        },
        error: () => { this.isLoading = false; }
      });
    }
  }

  loadPayments(invoiceId: string): void {
    this.portalService.getInvoicePayments(invoiceId).subscribe({
      next: (payList) => { this.payments = payList; },
      error: () => { this.payments = []; }
    });
  }

  printInvoice(): void {
    window.print();
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PAID': return 'bg-success';
      case 'PARTIALLY_PAID': return 'bg-warning text-dark';
      case 'OVERDUE': return 'bg-danger';
      case 'SENT': return 'bg-primary';
      default: return 'bg-secondary';
    }
  }
}
