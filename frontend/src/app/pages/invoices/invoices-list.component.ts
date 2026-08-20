import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../services/invoice.service';
import { RoleStateService } from '../../services/role-state.service';
import { Invoice, InvoiceStatus } from '../../models/invoice.models';

@Component({
  selector: 'app-invoices-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './invoices-list.component.html',
  styleUrls: ['./invoices-list.component.css']
})
export class InvoicesListComponent implements OnInit {
  invoices: Invoice[] = [];
  filteredInvoices: Invoice[] = [];
  isLoading = true;
  errorMessage = '';

  // Filter & Search state
  selectedStatus: string = 'ALL';
  searchTerm: string = '';

  // KPI Metrics
  totalInvoiced: number = 0;
  totalPaid: number = 0;
  totalOutstanding: number = 0;
  overdueCount: number = 0;

  constructor(
    private invoiceService: InvoiceService,
    public roleStateService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading = true;
    const role = this.roleStateService.getCurrentRole();
    const statusParam = this.selectedStatus !== 'ALL' ? (this.selectedStatus as InvoiceStatus) : undefined;

    this.invoiceService.getInvoices({ status: statusParam, search: this.searchTerm }, role).subscribe({
      next: (data) => {
        this.invoices = data;
        this.filteredInvoices = data;
        this.calculateMetrics();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Failed to load invoices.';
        this.isLoading = false;
      }
    });
  }

  calculateMetrics(): void {
    this.totalInvoiced = this.invoices.reduce((sum, inv) => sum + (inv.status !== 'VOID' ? inv.totalAmount : 0), 0);
    this.totalPaid = this.invoices.reduce((sum, inv) => sum + (inv.status !== 'VOID' ? inv.amountPaid : 0), 0);
    this.totalOutstanding = this.invoices.reduce((sum, inv) => sum + (inv.status !== 'VOID' ? inv.balanceDue : 0), 0);
    this.overdueCount = this.invoices.filter(inv => inv.status === 'OVERDUE').length;
  }

  onFilterChange(): void {
    this.loadInvoices();
  }

  getStatusBadgeClass(status: InvoiceStatus): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'SENT': return 'badge-sent';
      case 'PARTIALLY_PAID': return 'badge-partially-paid';
      case 'PAID': return 'badge-paid';
      case 'OVERDUE': return 'badge-overdue';
      case 'VOID': return 'badge-void';
      default: return 'badge-secondary';
    }
  }

  formatStatusText(status: InvoiceStatus): string {
    switch (status) {
      case 'PARTIALLY_PAID': return 'PARTIALLY PAID';
      default: return status;
    }
  }
}
