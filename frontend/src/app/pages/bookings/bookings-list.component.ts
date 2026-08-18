import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { Booking, BookingStatus } from '../../models/booking.models';

@Component({
  selector: 'app-bookings-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './bookings-list.component.html',
  styleUrls: ['./bookings-list.component.css']
})
export class BookingsListComponent implements OnInit {
  bookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  isLoading = true;
  searchTerm = '';
  selectedStatus = 'ALL';

  // Metrics
  pendingCount = 0;
  confirmedCount = 0;
  depositPendingCount = 0;
  upcomingCount = 0;
  inProgressCount = 0;
  completedCount = 0;
  cancelledCount = 0;
  totalBookingValue = 0;

  constructor(private bookingService: BookingService, private router: Router) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.isLoading = true;
    this.bookingService.getBookings().subscribe({
      next: (data) => {
        this.bookings = data;
        this.calculateMetrics();
        this.filterBookings();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load bookings', err);
        this.isLoading = false;
      }
    });
  }

  calculateMetrics(): void {
    this.pendingCount = this.bookings.filter(b => b.status === 'PENDING').length;
    this.confirmedCount = this.bookings.filter(b => b.status === 'CONFIRMED').length;
    this.depositPendingCount = this.bookings.filter(b => b.status === 'DEPOSIT_PENDING' || (b.status === 'CONFIRMED' && (b.depositPaid || 0) < (b.depositRequired || 0))).length;
    this.upcomingCount = this.bookings.filter(b => b.status === 'CONFIRMED' || b.status === 'PENDING').length;
    this.inProgressCount = this.bookings.filter(b => b.status === 'IN_PROGRESS' || b.status === 'READY_FOR_FULFILLMENT').length;
    this.completedCount = this.bookings.filter(b => b.status === 'COMPLETED').length;
    this.cancelledCount = this.bookings.filter(b => b.status === 'CANCELLED').length;
    this.totalBookingValue = this.bookings
      .filter(b => b.status !== 'CANCELLED')
      .reduce((sum, b) => sum + (b.totalAmount || 0), 0);
  }

  filterBookings(): void {
    this.filteredBookings = this.bookings.filter(b => {
      const matchesSearch = !this.searchTerm ||
        (b.bookingNumber && b.bookingNumber.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (b.customerName && b.customerName.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (b.eventName && b.eventName.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesStatus = this.selectedStatus === 'ALL' || b.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }

  onSearch(): void {
    this.filterBookings();
  }

  onStatusChange(): void {
    this.filterBookings();
  }

  getStatusBadgeClass(status: BookingStatus): string {
    switch (status) {
      case 'CONFIRMED': return 'badge-confirmed';
      case 'PENDING': return 'badge-pending';
      case 'DEPOSIT_PENDING': return 'badge-deposit-pending';
      case 'PAID': return 'badge-paid';
      case 'IN_PROGRESS': return 'badge-in-progress';
      case 'COMPLETED': return 'badge-completed';
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-secondary';
    }
  }

  viewBooking(id?: string): void {
    if (id) {
      this.router.navigate(['/bookings', id]);
    }
  }
}
