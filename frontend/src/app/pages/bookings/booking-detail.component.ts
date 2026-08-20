import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { PaymentService } from '../../services/payment.service';
import { InvoiceService } from '../../services/invoice.service';
import { Invoice } from '../../models/invoice.models';
import { Booking, BookingStatus } from '../../models/booking.models';
import { Payment, PaymentMethod, PaymentStatus, BookingFinancialSummary } from '../../models/payment.models';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './booking-detail.component.html',
  styleUrls: ['./booking-detail.component.css']
})
export class BookingDetailComponent implements OnInit {
  bookingId: string | null = null;
  booking: Booking | null = null;
  payments: Payment[] = [];
  financialSummary: BookingFinancialSummary | null = null;
  associatedInvoice: Invoice | null = null;

  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  showCancelModal = false;
  isProcessingAction = false;

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

  // Void Payment Modal State
  showVoidModal = false;
  paymentToVoid: Payment | null = null;
  voidReason = '';
  isVoidingPayment = false;
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
    private bookingService: BookingService,
    private paymentService: PaymentService,
    private invoiceService: InvoiceService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.bookingId = params.get('id');
      if (this.bookingId) {
        this.loadBookingAndPayments(this.bookingId);
      }
    });
  }

  loadBookingAndPayments(id: string): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.bookingService.getBookingById(id).subscribe({
      next: (data) => {
        this.booking = data;
        this.loadPaymentsAndSummary(id);
        this.loadAssociatedInvoice(id);
      },
      error: (err) => {
        console.error('Failed to load booking', err);
        this.errorMessage = 'Booking not found or access denied.';
        this.isLoading = false;
      }
    });
  }

  loadAssociatedInvoice(bookingId: string): void {
    this.invoiceService.getBookingInvoice(bookingId).subscribe({
      next: (inv) => {
        this.associatedInvoice = inv;
      },
      error: () => {
        this.associatedInvoice = null;
      }
    });
  }

  handleInvoiceAction(): void {
    if (!this.bookingId) return;
    if (this.associatedInvoice) {
      this.router.navigate(['/invoices', this.associatedInvoice.id]);
    } else {
      this.invoiceService.createInvoiceFromBooking(this.bookingId).subscribe({
        next: (created) => {
          this.router.navigate(['/invoices', created.id]);
        },
        error: (err) => {
          this.errorMessage = err.error?.error || 'Failed to generate invoice.';
        }
      });
    }
  }

  loadPaymentsAndSummary(id: string): void {
    this.paymentService.getBookingPayments(id).subscribe({
      next: (payList) => {
        this.payments = payList;
      },
      error: (err) => {
        console.error('Failed to load payments', err);
      }
    });

    this.paymentService.getFinancialSummary(id).subscribe({
      next: (summary) => {
        this.financialSummary = summary;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load financial summary', err);
        this.isLoading = false;
      }
    });
  }

  confirmBooking(): void {
    if (!this.bookingId) return;
    this.isProcessingAction = true;
    this.bookingService.confirmBooking(this.bookingId).subscribe({
      next: (updated) => {
        this.booking = updated;
        this.isProcessingAction = false;
        this.successMessage = '✓ Booking has been successfully confirmed and inventory committed.';
        this.loadPaymentsAndSummary(this.bookingId!);
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to confirm booking', err);
        this.errorMessage = err.error?.message || err.error?.error || 'Failed to confirm booking.';
        this.isProcessingAction = false;
      }
    });
  }

  openCancelModal(): void {
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
  }

  confirmCancelBooking(): void {
    if (!this.bookingId) return;
    this.isProcessingAction = true;
    this.bookingService.cancelBooking(this.bookingId).subscribe({
      next: (updated) => {
        this.booking = updated;
        this.isProcessingAction = false;
        this.showCancelModal = false;
        this.successMessage = 'Booking cancelled and reserved inventory released.';
        this.loadPaymentsAndSummary(this.bookingId!);
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to cancel booking', err);
        this.errorMessage = err.error?.message || err.error?.error || 'Failed to cancel booking.';
        this.isProcessingAction = false;
        this.showCancelModal = false;
      }
    });
  }

  // --- RECORD PAYMENT DIALOG LOGIC ---
  openRecordPaymentModal(): void {
    this.recordPaymentError = null;
    const balance = this.financialSummary ? this.financialSummary.outstandingBalance : (this.booking?.balanceDue || 0);
    this.recordForm = {
      amount: balance > 0 ? balance : 0,
      paymentMethod: 'BANK_TRANSFER',
      paymentDate: new Date().toISOString().substring(0, 10),
      transactionReference: '',
      notes: ''
    };
    this.showRecordPaymentModal = true;
  }

  closeRecordPaymentModal(): void {
    this.showRecordPaymentModal = false;
    this.recordPaymentError = null;
  }

  submitRecordPayment(): void {
    if (!this.bookingId) return;
    this.recordPaymentError = null;

    if (!this.recordForm.amount || this.recordForm.amount <= 0) {
      this.recordPaymentError = 'Payment amount must be greater than zero.';
      return;
    }

    if (!this.recordForm.paymentMethod) {
      this.recordPaymentError = 'Payment method is required.';
      return;
    }

    if (!this.recordForm.paymentDate) {
      this.recordPaymentError = 'Payment date is required.';
      return;
    }

    const currentBalance = this.financialSummary ? this.financialSummary.outstandingBalance : (this.booking?.balanceDue || 0);
    if (this.recordForm.amount > currentBalance) {
      this.recordPaymentError = `Payment exceeds outstanding balance of $${currentBalance.toFixed(2)}.`;
      return;
    }

    this.isRecordingPayment = true;

    this.paymentService.recordPayment(this.bookingId, {
      amount: Number(this.recordForm.amount),
      paymentMethod: this.recordForm.paymentMethod,
      paymentDate: this.recordForm.paymentDate,
      transactionReference: this.recordForm.transactionReference,
      notes: this.recordForm.notes
    }).subscribe({
      next: (createdPay) => {
        this.isRecordingPayment = false;
        this.showRecordPaymentModal = false;
        this.successMessage = `Payment of $${createdPay.amount.toFixed(2)} recorded successfully.`;
        this.loadBookingAndPayments(this.bookingId!);
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to record payment', err);
        this.recordPaymentError = err.error?.error || err.error?.message || 'Failed to record payment.';
        this.isRecordingPayment = false;
      }
    });
  }

  // --- VOID PAYMENT LOGIC ---
  openVoidModal(payment: Payment): void {
    this.paymentToVoid = payment;
    this.voidReason = '';
    this.voidError = null;
    this.showVoidModal = true;
  }

  closeVoidModal(): void {
    this.showVoidModal = false;
    this.paymentToVoid = null;
    this.voidReason = '';
    this.voidError = null;
  }

  confirmVoidPayment(): void {
    if (!this.paymentToVoid) return;
    this.isVoidingPayment = true;
    this.voidError = null;

    this.paymentService.voidPayment(this.paymentToVoid.id, this.voidReason).subscribe({
      next: (voided) => {
        this.isVoidingPayment = false;
        this.showVoidModal = false;
        this.successMessage = `Payment of $${voided.amount.toFixed(2)} has been voided. Balance updated.`;
        this.paymentToVoid = null;
        this.loadBookingAndPayments(this.bookingId!);
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to void payment', err);
        this.voidError = err.error?.error || err.error?.message || 'Failed to void payment.';
        this.isVoidingPayment = false;
      }
    });
  }

  getStatusBadgeClass(status?: BookingStatus | string): string {
    switch (status) {
      case 'CONFIRMED': return 'badge-confirmed';
      case 'PENDING': return 'badge-pending';
      case 'DEPOSIT_PENDING': return 'badge-deposit-pending';
      case 'PARTIALLY_PAID': return 'badge-partially-paid';
      case 'PAID': return 'badge-paid';
      case 'CANCELLED': return 'badge-cancelled';
      case 'VOID': return 'badge-void';
      case 'COMPLETED': return 'badge-completed';
      default: return 'badge-secondary';
    }
  }

  getPaymentMethodLabel(method: PaymentMethod): string {
    switch (method) {
      case 'BANK_TRANSFER': return 'Bank Transfer';
      case 'CREDIT_CARD': return 'Credit Card';
      case 'DEBIT_CARD': return 'Debit Card';
      case 'CASH': return 'Cash';
      case 'CHECK': return 'Check';
      case 'OTHER': return 'Other';
      default: return method;
    }
  }
}
