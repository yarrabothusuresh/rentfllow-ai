import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CrmService } from '../../services/crm.service';
import { Lead, LeadStatus, LeadSource, EventType } from '../../models/crm.models';

@Component({
  selector: 'app-leads-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './leads-list.component.html',
  styleUrl: './leads-list.component.scss'
})
export class LeadsListComponent implements OnInit {
  leads: Lead[] = [];
  filteredLeads: Lead[] = [];
  loading = true;

  // Filters
  searchQuery = '';
  selectedStatus = '';
  selectedSource = '';
  selectedEventType = '';

  // Stats
  newCount = 0;
  qualifiedCount = 0;
  quoteRequestedCount = 0;
  quoteSentCount = 0;
  negotiationCount = 0;
  convertedCount = 0;
  lostCount = 0;

  // Modal
  showCreateModal = false;
  newLead: Lead = this.initEmptyLead();

  constructor(private crmService: CrmService) {}

  ngOnInit(): void {
    this.loadLeads();
  }

  loadLeads(): void {
    this.loading = true;
    this.crmService.getLeads().subscribe({
      next: (data) => {
        this.leads = data;
        this.calculateStats();
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  calculateStats(): void {
    this.newCount = this.leads.filter(l => l.status === 'NEW').length;
    this.qualifiedCount = this.leads.filter(l => l.status === 'QUALIFIED').length;
    this.quoteRequestedCount = this.leads.filter(l => l.status === 'QUOTE_REQUESTED').length;
    this.quoteSentCount = this.leads.filter(l => l.status === 'QUOTE_SENT').length;
    this.negotiationCount = this.leads.filter(l => l.status === 'NEGOTIATION').length;
    this.convertedCount = this.leads.filter(l => l.status === 'CONVERTED').length;
    this.lostCount = this.leads.filter(l => l.status === 'LOST').length;
  }

  applyFilters(): void {
    let result = [...this.leads];

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      result = result.filter(l =>
        (l.firstName + ' ' + (l.lastName || '')).toLowerCase().includes(q) ||
        l.email.toLowerCase().includes(q) ||
        (l.phone && l.phone.includes(q)) ||
        (l.venueName && l.venueName.toLowerCase().includes(q)) ||
        (l.companyName && l.companyName.toLowerCase().includes(q))
      );
    }

    if (this.selectedStatus) {
      result = result.filter(l => l.status === this.selectedStatus);
    }

    if (this.selectedSource) {
      result = result.filter(l => l.source === this.selectedSource);
    }

    if (this.selectedEventType) {
      result = result.filter(l => l.eventType === this.selectedEventType);
    }

    this.filteredLeads = result;
  }

  openCreateModal(): void {
    this.newLead = this.initEmptyLead();
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createLead(): void {
    if (!this.newLead.firstName || !this.newLead.email) {
      alert('Please fill in required fields (First Name and Email).');
      return;
    }
    this.crmService.createLead(this.newLead).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadLeads();
      }
    });
  }

  getStatusClass(status?: string): string {
    switch (status) {
      case 'NEW': return 'badge-new';
      case 'QUALIFIED': return 'badge-qualified';
      case 'QUOTE_REQUESTED': return 'badge-quote-req';
      case 'QUOTE_SENT': return 'badge-quote-sent';
      case 'NEGOTIATION': return 'badge-negotiation';
      case 'CONVERTED': return 'badge-converted';
      case 'LOST': return 'badge-lost';
      default: return 'badge-default';
    }
  }

  private initEmptyLead(): Lead {
    return {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      companyName: '',
      source: 'WEBSITE',
      eventType: 'WEDDING',
      eventDate: new Date().toISOString().split('T')[0],
      guestCount: 100,
      venueName: '',
      notes: '',
      status: 'NEW',
      assignedTo: 'Sarah Miller'
    };
  }
}
