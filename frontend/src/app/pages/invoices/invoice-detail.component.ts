import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../services/invoice.service';
import { PaymentService } from '../../services/payment.service';
import { RoleStateService } from '../../services/role-state.service';
import { Invoice, InvoiceStatus } from '../../models/invoice.models';
import { Payment, PaymentMethod } from '../../models/payment.models';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css']
})
export class InvoiceDetailComponent implements OnInit {
  invoiceId: string | null = null;
  invoice: Invoice | null = null;
  payments: Payment[] = [];

  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Record Payment Modal State
  showRecordPaymentModal = false;
  isRecordingPayment = false;
  recordPaymentError: string | null = null;

  recordForm = {
    amount: 0,
    paymentMethod: 'BANK_TRANSFER' as PaymentMethod,
    paymentDate: new Date().toISOString().substring(0, 10),
    transactionReference: '',
    notes: ''
  };

  // Void Invoice Modal State
  showVoidModal = false;
  voidReason = '';
  isVoidingInvoice = false;
  voidError: string | null = null;

  paymentMethods: PaymentMethod[] = [
    'CASH',
    'BANK_TRANSFER',
    'CREDIT_CARD',
    'DEBIT_CARD',
    'CHECK',
    'OTHER'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private invoiceService: InvoiceService,
    private paymentService: PaymentService,
    public roleStateService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.invoiceId = params.get('id');
      if (this.invoiceId) {
        this.loadInvoice(this.invoiceId);
      }
    });
  }

  loadInvoice(id: string): void {
    this.isLoading = true;
    this.errorMessage = null;
    const role = this.roleStateService.getCurrentRole();

    this.invoiceService.getInvoiceById(id, role).subscribe({
      next: (data) => {
        this.invoice = data;
        this.payments = data.payments || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Invoice not found or access denied.';
        this.isLoading = false;
      }
    });
  }

  markAsSent(): void {
    if (!this.invoice) return;
    const role = this.roleStateService.getCurrentRole();

    this.invoiceService.updateInvoiceStatus(this.invoice.id, 'SENT', role).subscribe({
      next: (updated) => {
        this.invoice = updated;
        this.showSuccess('Invoice marked as SENT.');
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Failed to update invoice status.';
      }
    });
  }

  // Record Payment Modal Methods
  openRecordPaymentModal(): void {
    if (!this.invoice) return;
    this.recordForm = {
      amount: this.invoice.balanceDue > 0 ? this.invoice.balanceDue : 0,
      paymentMethod: 'BANK_TRANSFER',
      paymentDate: new Date().toISOString().substring(0, 10),
      transactionReference: '',
      notes: ''
    };
    this.recordPaymentError = null;
    this.showRecordPaymentModal = true;
  }

  closeRecordPaymentModal(): void {
    this.showRecordPaymentModal = false;
    this.recordPaymentError = null;
  }

  submitRecordPayment(): void {
    if (!this.invoice) return;
    if (this.recordForm.amount <= 0) {
      this.recordPaymentError = 'Payment amount must be greater than zero.';
      return;
    }
    if (this.recordForm.amount > this.invoice.balanceDue) {
      this.recordPaymentError = `Payment cannot exceed outstanding balance of $${this.invoice.balanceDue.toFixed(2)}.`;
      return;
    }

    this.isRecordingPayment = true;
    this.recordPaymentError = null;
    const role = this.roleStateService.getCurrentRole();

    this.paymentService.recordPayment(this.invoice.bookingId, {
      amount: this.recordForm.amount,
      paymentMethod: this.recordForm.paymentMethod,
      paymentDate: this.recordForm.paymentDate,
      transactionReference: this.recordForm.transactionReference,
      notes: this.recordForm.notes
    }, role).subscribe({
      next: () => {
        this.isRecordingPayment = false;
        this.showRecordPaymentModal = false;
        this.showSuccess('Payment recorded successfully!');
        if (this.invoiceId) {
          this.loadInvoice(this.invoiceId);
        }
      },
      error: (err) => {
        this.isRecordingPayment = false;
        this.recordPaymentError = err.error?.error || 'Failed to record payment.';
      }
    });
  }

  // Void Invoice Modal Methods
  openVoidModal(): void {
    this.voidReason = '';
    this.voidError = null;
    this.showVoidModal = true;
  }

  closeVoidModal(): void {
    this.showVoidModal = false;
    this.voidError = null;
  }

  submitVoidInvoice(): void {
    if (!this.invoice) return;
    this.isVoidingInvoice = true;
    this.voidError = null;
    const role = this.roleStateService.getCurrentRole();

    this.invoiceService.voidInvoice(this.invoice.id, this.voidReason, role).subscribe({
      next: (voided) => {
        this.invoice = voided;
        this.isVoidingInvoice = false;
        this.showVoidModal = false;
        this.showSuccess('Invoice voided successfully.');
      },
      error: (err) => {
        this.isVoidingInvoice = false;
        this.voidError = err.error?.error || 'Failed to void invoice.';
      }
    });
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

  showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => {
      this.successMessage = null;
    }, 4000);
  }
}
