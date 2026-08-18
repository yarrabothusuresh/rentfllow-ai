import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { BookingService } from '../../services/booking.service';
import { Quote } from '../../models/quote.models';
import { Booking, BookingUnavailableError, ShortageItem } from '../../models/booking.models';

@Component({
  selector: 'app-quote-preview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-preview.component.html',
  styleUrls: ['./quote-preview.component.css']
})
export class QuotePreviewComponent implements OnInit {
  quoteId: string | null = null;
  quote: Quote | null = null;
  isLoading = true;

  showAcceptModal = false;
  isSubmitting = false;

  bookingCreated: Booking | null = null;
  availabilityError: BookingUnavailableError | null = null;

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
    this.quoteService.getQuoteById(id, 'CUSTOMER').subscribe({
      next: (data) => {
        this.quote = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load customer preview quote', err);
        this.isLoading = false;
      }
    });
  }

  openAcceptModal(): void {
    this.showAcceptModal = true;
    this.availabilityError = null;
  }

  closeAcceptModal(): void {
    this.showAcceptModal = false;
    this.availabilityError = null;
  }

  acceptAndConfirmBooking(): void {
    if (!this.quoteId) return;
    this.isSubmitting = true;
    this.availabilityError = null;

    // 1. Mark Quote Accepted
    this.quoteService.acceptQuote(this.quoteId, 'CUSTOMER').subscribe({
      next: (acceptedQuote) => {
        this.quote = acceptedQuote;

        // 2. Create Booking from Quote with Recheck Availability
        this.bookingService.createBookingFromQuote(this.quoteId!, 'CUSTOMER').subscribe({
          next: (booking) => {
            this.bookingCreated = booking;
            this.isSubmitting = false;
            this.showAcceptModal = false;
            this.router.navigate(['/bookings', booking.id]);
          },
          error: (err) => {
            this.isSubmitting = false;
            if (err.status === 409 && err.error?.items) {
              this.availabilityError = err.error;
            } else {
              this.availabilityError = {
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
      },
      error: (err) => {
        console.error('Failed to accept quote', err);
        this.isSubmitting = false;
      }
    });
  }

  printQuote(): void {
    window.print();
  }
}
