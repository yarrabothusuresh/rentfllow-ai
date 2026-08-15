import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { CatalogService } from '../../services/catalog.service';
import { Event, EventRequirement, Customer } from '../../models/crm.models';
import { AvailabilityResult } from '../../models/catalog.models';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './event-detail.component.html',
  styleUrl: './event-detail.component.scss'
})
export class EventDetailComponent implements OnInit {
  event: Event | null = null;
  customer: Customer | null = null;
  requirements: EventRequirement[] = [];
  loading = true;
  error: string | null = null;

  showRequirementModal = false;
  newReq: EventRequirement = { description: '', quantity: 100, notes: '' };

  // Date-Based Availability Engine state
  checkingAvailability = false;
  availabilityChecked = false;
  availabilityCheckResults: { req: EventRequirement; result?: AvailabilityResult }[] = [];

  constructor(
    private route: ActivatedRoute,
    private crmService: CrmService,
    private catalogService: CatalogService
  ) {}

  ngOnInit(): void {
    const eventId = this.route.snapshot.paramMap.get('id');
    if (eventId) {
      this.loadEventData(eventId);
    } else {
      this.error = 'Invalid Event ID';
      this.loading = false;
    }
  }

  loadEventData(id: string): void {
    this.loading = true;
    this.crmService.getEventById(id).subscribe({
      next: (ev) => {
        this.event = ev;
        this.loadRequirements(id);

        if (ev.customerId) {
          this.crmService.getCustomerById(ev.customerId).subscribe({
            next: (cus) => (this.customer = cus),
            error: () => {}
          });
        }
      },
      error: (err) => {
        this.error = err.message || 'Event not found.';
        this.loading = false;
      }
    });
  }

  loadRequirements(eventId: string): void {
    this.crmService.getEventRequirements(eventId).subscribe({
      next: (reqs) => {
        this.requirements = reqs;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openAddRequirementModal(): void {
    this.newReq = { description: '', quantity: 50, notes: '' };
    this.showRequirementModal = true;
  }

  closeRequirementModal(): void {
    this.showRequirementModal = false;
  }

  addRequirement(): void {
    if (!this.event || !this.event.id || !this.newReq.description) {
      alert('Description is required.');
      return;
    }

    this.crmService.addEventRequirement(this.event.id, this.newReq).subscribe({
      next: () => {
        this.closeRequirementModal();
        this.loadRequirements(this.event!.id!);
      }
    });
  }

  deleteRequirement(reqId?: string): void {
    if (!this.event || !this.event.id || !reqId) return;
    if (confirm('Are you sure you want to remove this rental requirement?')) {
      this.crmService.deleteEventRequirement(this.event.id, reqId).subscribe({
        next: () => this.loadRequirements(this.event!.id!)
      });
    }
  }

  checkProductAvailability(): void {
    if (!this.event || this.requirements.length === 0) return;
    this.checkingAvailability = true;
    this.availabilityChecked = false;
    this.availabilityCheckResults = [];

    const start = (this.event.eventDate || '2026-09-20') + 'T' + (this.event.startTime || '08:00:00');
    const end = (this.event.eventDate || '2026-09-22') + 'T' + (this.event.endTime || '18:00:00');

    let completed = 0;
    this.requirements.forEach((req) => {
      // Use Chiavari Chair product ID as default fallback for demo if req doesn't have specific productId
      const prodId = req.productId || '11111111-1111-1111-1111-111111111111';
      this.catalogService.checkAvailability(prodId, req.quantity || 1, start, end).subscribe({
        next: (res) => {
          this.availabilityCheckResults.push({ req, result: res });
          completed++;
          if (completed === this.requirements.length) {
            this.checkingAvailability = false;
            this.availabilityChecked = true;
          }
        },
        error: () => {
          completed++;
          if (completed === this.requirements.length) {
            this.checkingAvailability = false;
            this.availabilityChecked = true;
          }
        }
      });
    });
  }
}
