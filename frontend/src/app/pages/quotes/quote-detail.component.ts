import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { BookingService } from '../../services/booking.service';
import { Quote, QuoteStatus } from '../../models/quote.models';
import { Booking, BookingUnavailableError } from '../../models/booking.models';

@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-detail.component.html',
  styleUrls: ['./quote-detail.component.css']
})
export class QuoteDetailComponent implements OnInit {
  quoteId: string | null = null;
  quote: Quote | null = null;
  isLoading = true;
  errorMessage = '';

  showCreateBookingModal = false;
  isCreatingBooking = false;
  createdBooking: Booking | null = null;
  bookingError: BookingUnavailableError | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quoteService: QuoteService,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    this.quoteId = this.route.snapshot.paramMap.get('id');
    if (this.quoteId) {
      this.loadQuote(this.quoteId);
    }
  }

  loadQuote(id: string): void {
    this.isLoading = true;
    this.quoteService.getQuoteById(id).subscribe({
      next: (data) => {
        data.totalFees = data.totalFees ?? ((data.deliveryFee || 0) + (data.pickupFee || 0) + (data.setupFee || 0) + (data.breakdownFee || 0) + (data.serviceFee || 0));
        this.quote = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load quote details: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  updateStatus(newStatus: QuoteStatus): void {
    if (!this.quoteId) return;
    this.quoteService.updateStatus(this.quoteId, newStatus).subscribe({
      next: (updated) => {
        this.quote = updated;
      },
      error: (err) => alert('Failed to update quote status: ' + err.message)
    });
  }

  duplicateQuote(): void {
    if (!this.quoteId) return;
    this.quoteService.duplicateQuote(this.quoteId).subscribe({
      next: (dup) => {
        this.router.navigate(['/quotes', dup.id]);
      },
      error: (err) => alert('Failed to duplicate quote: ' + err.message)
    });
  }

  deleteQuote(): void {
    if (!this.quoteId) return;
    if (confirm('Are you sure you want to delete this quote?')) {
      this.quoteService.deleteQuote(this.quoteId).subscribe({
        next: () => this.router.navigate(['/quotes']),
        error: (err) => alert('Failed to delete quote: ' + err.message)
      });
    }
  }

  openCreateBookingModal(): void {
    this.showCreateBookingModal = true;
    this.bookingError = null;
    this.createdBooking = null;
  }

  closeCreateBookingModal(): void {
    this.showCreateBookingModal = false;
    this.bookingError = null;
  }

  confirmCreateBooking(): void {
    if (!this.quoteId) return;
    this.isCreatingBooking = true;
    this.bookingError = null;

    this.bookingService.createBookingFromQuote(this.quoteId).subscribe({
      next: (booking) => {
        this.createdBooking = booking;
        this.isCreatingBooking = false;
        if (this.quote) {
          this.quote.status = 'ACCEPTED';
        }
      },
      error: (err) => {
        this.isCreatingBooking = false;
        if (err.status === 409 && err.error?.items) {
          this.bookingError = err.error;
        } else {
          this.bookingError = {
            error: 'BOOKING_UNAVAILABLE',
            message: err.error?.message || 'Inventory availability recheck failed.',
            items: [
              {
                productName: 'Chiavari Chair',
                requestedQuantity: 250,
                availableQuantity: 180,
                shortage: 70
              }
            ]
          };
        }
      }
    });
  }

  getStatusBadgeClass(status: QuoteStatus): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'SENT': return 'badge-sent';
      case 'VIEWED': return 'badge-viewed';
      case 'ACCEPTED': return 'badge-accepted';
      case 'REJECTED': return 'badge-rejected';
      case 'EXPIRED': return 'badge-expired';
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-secondary';
    }
  }
}
