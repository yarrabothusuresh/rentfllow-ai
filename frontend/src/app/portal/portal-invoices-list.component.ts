import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalInvoice } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-invoices-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>💳 My Invoices & Payment History</h2>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && invoices.length === 0" class="alert alert-secondary">
        No invoices issued for your account yet.
      </div>

      <div class="table-responsive" *ngIf="!isLoading && invoices.length > 0">
        <table class="table table-dark table-hover align-middle card-glass">
          <thead>
            <tr>
              <th>Invoice #</th>
              <th>Issue Date</th>
              <th>Due Date</th>
              <th class="text-end">Total Amount</th>
              <th class="text-end">Amount Paid</th>
              <th class="text-end">Balance Due</th>
              <th class="text-center">Status</th>
              <th class="text-end">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let inv of invoices">
              <td class="font-monospace text-info font-weight-bold">{{ inv.invoiceNumber }}</td>
              <td>{{ inv.issueDate }}</td>
              <td>{{ inv.dueDate }}</td>
              <td class="text-end">\${{ inv.totalAmount | number:'1.2-2' }}</td>
              <td class="text-end text-success">\${{ inv.amountPaid | number:'1.2-2' }}</td>
              <td class="text-end font-weight-bold text-warning">\${{ inv.balanceDue | number:'1.2-2' }}</td>
              <td class="text-center">
                <span class="badge" [ngClass]="getStatusBadgeClass(inv.status)">{{ inv.status }}</span>
              </td>
              <td class="text-end">
                <a [routerLink]="['/portal/invoices', inv.id]" class="btn btn-sm btn-outline-info">View Invoice →</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalInvoicesListComponent implements OnInit {
  invoices: CustomerPortalInvoice[] = [];
  isLoading = true;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.portalService.getInvoices().subscribe({
      next: (data) => { this.invoices = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
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
