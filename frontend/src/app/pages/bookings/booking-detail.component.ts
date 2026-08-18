import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { Booking, BookingStatus } from '../../models/booking.models';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './booking-detail.component.html',
  styleUrls: ['./booking-detail.component.css']
})
export class BookingDetailComponent implements OnInit {
  bookingId: string | null = null;
  booking: Booking | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  showCancelModal = false;
  isProcessingAction = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.bookingId = params.get('id');
      if (this.bookingId) {
        this.loadBooking(this.bookingId);
      }
    });
  }

  loadBooking(id: string): void {
    this.isLoading = true;
    this.bookingService.getBookingById(id).subscribe({
      next: (data) => {
        this.booking = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load booking', err);
        this.errorMessage = 'Booking not found or access denied.';
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
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to confirm booking', err);
        this.errorMessage = err.error?.message || 'Failed to confirm booking.';
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
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        console.error('Failed to cancel booking', err);
        this.errorMessage = err.error?.message || 'Failed to cancel booking.';
        this.isProcessingAction = false;
        this.showCancelModal = false;
      }
    });
  }

  getStatusBadgeClass(status?: BookingStatus): string {
    switch (status) {
      case 'CONFIRMED': return 'badge-confirmed';
      case 'PENDING': return 'badge-pending';
      case 'DEPOSIT_PENDING': return 'badge-deposit-pending';
      case 'PAID': return 'badge-paid';
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-secondary';
    }
  }
}
