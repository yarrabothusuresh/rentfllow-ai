import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalEvent } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-event-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="mb-3">
        <a routerLink="/portal/events" class="text-info text-decoration-none">← Back to Events</a>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && event" class="card-glass p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2>{{ event.eventName }}</h2>
          <span class="badge bg-success fs-6">{{ event.status }}</span>
        </div>

        <div class="row mb-4">
          <div class="col-md-6">
            <p><strong>Event Date:</strong> {{ event.eventDate }}</p>
            <p><strong>Event Type:</strong> {{ event.eventType }}</p>
            <p><strong>Timings:</strong> {{ event.startTime || '08:00' }} - {{ event.endTime || '18:00' }}</p>
          </div>
          <div class="col-md-6">
            <p><strong>Venue:</strong> {{ event.venueName }}</p>
            <p><strong>Address:</strong> {{ event.venueAddress || 'Address TBD' }}, {{ event.city }}, {{ event.state }} {{ event.zipCode }}</p>
          </div>
        </div>

        <h4 class="border-bottom border-secondary pb-2 mb-3">Requested Equipment & Requirements</h4>
        <div *ngIf="!event.requirements || event.requirements.length === 0" class="text-muted">
          No items listed for this event yet.
        </div>
        <ul class="list-group list-group-flush bg-transparent" *ngIf="event.requirements && event.requirements.length > 0">
          <li *ngFor="let req of event.requirements" class="list-group-item bg-transparent text-light border-secondary">
            📦 {{ req }}
          </li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalEventDetailComponent implements OnInit {
  event: CustomerPortalEvent | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private portalService: CustomerPortalService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.portalService.getEventDetail(id).subscribe({
        next: (data) => { this.event = data; this.isLoading = false; },
        error: () => { this.isLoading = false; }
      });
    }
  }
}
