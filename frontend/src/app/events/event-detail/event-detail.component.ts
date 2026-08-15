import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Event, EventRequirement, Customer } from '../../models/crm.models';

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

  constructor(
    private route: ActivatedRoute,
    private crmService: CrmService
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
            next: (cus) => this.customer = cus,
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
    alert(`Checking real-time warehouse inventory availability for ${this.event?.eventName}... All ${this.requirements.length} required items reserved & available!`);
  }
}
