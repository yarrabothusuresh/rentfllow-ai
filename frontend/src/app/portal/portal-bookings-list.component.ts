import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalBooking } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-bookings-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>📅 My Bookings</h2>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && bookings.length === 0" class="alert alert-secondary">
        No active bookings found for your account.
      </div>

      <div class="row g-4" *ngIf="!isLoading">
        <div class="col-md-6 col-lg-4" *ngFor="let b of bookings">
          <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
            <div>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="font-monospace text-info">{{ b.bookingNumber }}</span>
                <span class="badge bg-success">{{ b.status }}</span>
              </div>
              <h4 class="text-light font-weight-bold">{{ b.eventName || 'Event Booking' }}</h4>
              <p class="text-muted mb-1">📅 Booking Date: {{ b.bookingDate }}</p>
              <p class="text-muted mb-2">📍 Venue: {{ b.venueName || 'Grand Ballroom' }}</p>
              
              <div class="d-flex justify-content-between small text-muted my-2 border-top border-secondary pt-2">
                <span>Total: \${{ b.totalAmount | number:'1.2-2' }}</span>
                <span>Paid: \${{ b.depositPaid | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between font-weight-bold text-warning mb-3">
                <span>Balance Due:</span>
                <span>\${{ b.balanceDue | number:'1.2-2' }}</span>
              </div>
            </div>
            <div>
              <a [routerLink]="['/portal/bookings', b.id]" class="btn btn-outline-info w-100">View Booking Details →</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalBookingsListComponent implements OnInit {
  bookings: CustomerPortalBooking[] = [];
  isLoading = true;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.portalService.getBookings().subscribe({
      next: (data) => { this.bookings = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }
}
