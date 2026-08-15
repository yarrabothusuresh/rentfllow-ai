import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Customer, Event } from '../../models/crm.models';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './customer-detail.component.html',
  styleUrl: './customer-detail.component.scss'
})
export class CustomerDetailComponent implements OnInit {
  customer: Customer | null = null;
  events: Event[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private crmService: CrmService
  ) {}

  ngOnInit(): void {
    const customerId = this.route.snapshot.paramMap.get('id');
    if (customerId) {
      this.loadCustomerData(customerId);
    } else {
      this.error = 'Invalid Customer ID';
      this.loading = false;
    }
  }

  loadCustomerData(id: string): void {
    this.loading = true;
    this.crmService.getCustomerById(id).subscribe({
      next: (data) => {
        this.customer = data;
        this.loadCustomerEvents(id);
      },
      error: (err) => {
        this.error = err.message || 'Customer profile not found.';
        this.loading = false;
      }
    });
  }

  loadCustomerEvents(customerId: string): void {
    this.crmService.getCustomerEvents(customerId).subscribe({
      next: (eventList) => {
        this.events = eventList;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  getEventStatusClass(status?: string): string {
    switch (status) {
      case 'BOOKED': return 'status-booked';
      case 'QUOTED': return 'status-quoted';
      case 'PREPARING': return 'status-preparing';
      case 'COMPLETED': return 'status-completed';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-planning';
    }
  }
}
