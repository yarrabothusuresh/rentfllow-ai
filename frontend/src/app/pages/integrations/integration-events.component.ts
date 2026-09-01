import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import { IntegrationEvent } from '../../models/integration.model';

@Component({
  selector: 'app-integration-events',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './integration-events.component.html',
  styleUrls: ['./integration-events.component.scss']
})
export class IntegrationEventsComponent implements OnInit {
  events: IntegrationEvent[] = [];
  selectedEvent: IntegrationEvent | null = null;
  loading = true;

  constructor(private integrationService: IntegrationService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.integrationService.getEvents(50).subscribe({
      next: (list) => {
        this.events = list;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  viewPayload(e: IntegrationEvent): void {
    this.selectedEvent = e;
  }

  closeModal(): void {
    this.selectedEvent = null;
  }
}
