import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Event, EventType, EventStatus } from '../../models/crm.models';

@Component({
  selector: 'app-events-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './events-list.component.html',
  styleUrl: './events-list.component.scss'
})
export class EventsListComponent implements OnInit {
  events: Event[] = [];
  filteredEvents: Event[] = [];
  loading = true;

  searchQuery = '';
  selectedType = '';
  selectedStatus = '';

  showCreateModal = false;
  newEvent: Event = this.initEmptyEvent();

  constructor(private crmService: CrmService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.crmService.getEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.events];

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      result = result.filter(e =>
        e.eventName.toLowerCase().includes(q) ||
        (e.venueName && e.venueName.toLowerCase().includes(q)) ||
        (e.city && e.city.toLowerCase().includes(q))
      );
    }

    if (this.selectedType) {
      result = result.filter(e => e.eventType === this.selectedType);
    }

    if (this.selectedStatus) {
      result = result.filter(e => e.status === this.selectedStatus);
    }

    this.filteredEvents = result;
  }

  openCreateModal(): void {
    this.newEvent = this.initEmptyEvent();
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createEvent(): void {
    if (!this.newEvent.eventName || !this.newEvent.customerId) {
      alert('Event Name and Customer ID are required.');
      return;
    }
    this.crmService.createEvent(this.newEvent).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadEvents();
      }
    });
  }

  getStatusBadgeClass(status?: string): string {
    switch (status) {
      case 'BOOKED': return 'badge-booked';
      case 'QUOTED': return 'badge-quoted';
      case 'PREPARING': return 'badge-preparing';
      case 'COMPLETED': return 'badge-completed';
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-planning';
    }
  }

  private initEmptyEvent(): Event {
    return {
      eventName: '',
      eventType: 'WEDDING',
      eventDate: '2026-09-20',
      startTime: '14:00',
      endTime: '23:00',
      guestCount: 150,
      venueName: '',
      venueAddress: '',
      city: 'Dallas',
      state: 'TX',
      zipCode: '75201',
      specialInstructions: '',
      status: 'PLANNING',
      customerId: ''
    };
  }
}
