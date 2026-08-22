import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalBooking } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-booking-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="mb-3">
        <a routerLink="/portal/bookings" class="text-info text-decoration-none">← Back to Bookings</a>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && booking" class="card-glass p-4">
        <!-- Booking Header -->
        <div class="d-flex justify-content-between align-items-start border-bottom border-secondary pb-3 mb-4">
          <div>
            <span class="badge bg-success font-monospace mb-2">BOOKING {{ booking.bookingNumber }}</span>
            <h2 class="text-light font-weight-bold mb-1">{{ booking.eventName || 'Event Booking' }}</h2>
            <p class="text-muted mb-0">📅 Order Date: {{ booking.bookingDate }}</p>
          </div>
          <div class="text-end">
            <span class="badge bg-success fs-6 mb-2">{{ booking.status }}</span>
            <h2 class="text-info font-weight-bold mb-0">\${{ booking.totalAmount | number:'1.2-2' }}</h2>
          </div>
        </div>

        <!-- Logistics & Delivery Info -->
        <div class="row mb-4">
          <div class="col-md-6">
            <div class="card-glass p-3 bg-dark h-100">
              <h5 class="text-info mb-2">🚚 Delivery & Pickup Schedule</h5>
              <p class="mb-1"><strong>Rental Start (Delivery):</strong> {{ booking.rentalStartDateTime | date:'medium' }}</p>
              <p class="mb-1"><strong>Rental End (Pickup):</strong> {{ booking.rentalEndDateTime | date:'medium' }}</p>
              <p class="mb-0 text-muted"><small>Our warehouse dispatch team will coordinate delivery setup prior to event start time.</small></p>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card-glass p-3 bg-dark h-100">
              <h5 class="text-info mb-2">📍 Event Venue</h5>
              <p class="mb-1"><strong>Venue:</strong> {{ booking.venueName || 'Grand Ballroom' }}</p>
              <p class="mb-0"><strong>Address:</strong> {{ booking.venueAddress || '200 Garden Way, Dallas, TX 75201' }}</p>
            </div>
          </div>
        </div>

        <!-- Booked Items Table -->
        <h4 class="text-light mb-3">Booked Equipment & Items</h4>
        <div class="table-responsive mb-4">
          <table class="table table-dark table-striped align-middle">
            <thead>
              <tr>
                <th>Item Description</th>
                <th class="text-center">Qty</th>
                <th class="text-end">Unit Price</th>
                <th class="text-end">Line Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of booking.items">
                <td>{{ item.description }}</td>
                <td class="text-center">{{ item.quantity }}</td>
                <td class="text-end">\${{ item.unitPrice | number:'1.2-2' }}</td>
                <td class="text-end font-weight-bold">\${{ item.lineSubtotal | number:'1.2-2' }}</td>
              </tr>
              <tr *ngIf="!booking.items || booking.items.length === 0">
                <td colspan="4" class="text-center text-muted">No line items detailed.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Financial Summary -->
        <div class="row">
          <div class="col-md-6">
            <div *ngIf="booking.notes" class="p-3 card-glass bg-dark">
              <small class="text-muted d-block">Special Event Notes:</small>
              <p class="mb-0 text-light">{{ booking.notes }}</p>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card-glass p-3 ms-auto" style="max-width: 400px;">
              <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Total Order Amount:</span>
                <span>\${{ booking.totalAmount | number:'1.2-2' }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2 text-success">
                <span>Amount Paid / Deposit:</span>
                <span>-\${{ booking.depositPaid | number:'1.2-2' }}</span>
              </div>
              <hr class="border-secondary my-2" />
              <div class="d-flex justify-content-between fs-5 font-weight-bold">
                <span class="text-light">OUTSTANDING BALANCE:</span>
                <span class="text-warning">\${{ booking.balanceDue | number:'1.2-2' }}</span>
              </div>
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
export class PortalBookingDetailComponent implements OnInit {
  booking: CustomerPortalBooking | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private portalService: CustomerPortalService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.portalService.getBookingDetail(id).subscribe({
        next: (data) => { this.booking = data; this.isLoading = false; },
        error: () => { this.isLoading = false; }
      });
    }
  }
}
