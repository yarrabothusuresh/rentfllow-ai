import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalEvent } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-events-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>🎉 My Events</h2>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && events.length === 0" class="alert alert-secondary">
        No events found for your account.
      </div>

      <div class="row g-4" *ngIf="!isLoading">
        <div class="col-md-6 col-lg-4" *ngFor="let ev of events">
          <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
            <div>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-info text-dark">{{ ev.eventType }}</span>
                <span class="badge bg-success">{{ ev.status }}</span>
              </div>
              <h4 class="text-light font-weight-bold">{{ ev.eventName }}</h4>
              <p class="text-muted mb-2">📅 {{ ev.eventDate }}</p>
              <p class="text-muted mb-3">📍 {{ ev.venueName || 'Venue TBD' }} ({{ ev.city || 'Dallas' }})</p>
            </div>
            <div>
              <a [routerLink]="['/portal/events', ev.id]" class="btn btn-outline-info w-100">View Event Details →</a>
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
export class PortalEventsListComponent implements OnInit {
  events: CustomerPortalEvent[] = [];
  isLoading = true;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.portalService.getEvents().subscribe({
      next: (data) => { this.events = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }
}
