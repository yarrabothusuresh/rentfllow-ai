import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Customer, CustomerType, CustomerStatus } from '../../models/crm.models';

@Component({
  selector: 'app-customers-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './customers-list.component.html',
  styleUrl: './customers-list.component.scss'
})
export class CustomersListComponent implements OnInit {
  customers: Customer[] = [];
  filteredCustomers: Customer[] = [];
  loading = true;

  searchQuery = '';
  selectedType = '';
  selectedStatus = '';

  showCreateModal = false;
  newCustomer: Customer = this.initEmptyCustomer();

  constructor(private crmService: CrmService) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.crmService.getCustomers().subscribe({
      next: (data) => {
        this.customers = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    let result = [...this.customers];

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      result = result.filter(c =>
        (c.firstName + ' ' + (c.lastName || '')).toLowerCase().includes(q) ||
        c.email.toLowerCase().includes(q) ||
        (c.phone && c.phone.includes(q)) ||
        (c.customerNumber && c.customerNumber.toLowerCase().includes(q)) ||
        (c.companyName && c.companyName.toLowerCase().includes(q))
      );
    }

    if (this.selectedType) {
      result = result.filter(c => c.customerType === this.selectedType);
    }

    if (this.selectedStatus) {
      result = result.filter(c => c.status === this.selectedStatus);
    }

    this.filteredCustomers = result;
  }

  openCreateModal(): void {
    this.newCustomer = this.initEmptyCustomer();
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createCustomer(): void {
    if (!this.newCustomer.firstName || !this.newCustomer.email) {
      alert('Please fill in First Name and Email.');
      return;
    }
    this.crmService.createCustomer(this.newCustomer).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadCustomers();
      }
    });
  }

  getTypeBadgeClass(type?: string): string {
    switch (type) {
      case 'CORPORATE': return 'badge-corporate';
      case 'VENUE': return 'badge-venue';
      case 'EVENT_PLANNER': return 'badge-planner';
      case 'BUSINESS': return 'badge-business';
      default: return 'badge-individual';
    }
  }

  private initEmptyCustomer(): Customer {
    return {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      companyName: '',
      customerType: 'INDIVIDUAL',
      billingAddress: '',
      city: 'Dallas',
      state: 'TX',
      zipCode: '75201',
      country: 'USA',
      notes: '',
      status: 'ACTIVE'
    };
  }
}
