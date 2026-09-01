import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IntegrationService } from '../../services/integration.service';
import { ExternalApiKey, CreateApiKeyRequest } from '../../models/integration.model';

@Component({
  selector: 'app-api-keys',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './api-keys.component.html',
  styleUrls: ['./api-keys.component.scss']
})
export class ApiKeysComponent implements OnInit {
  apiKeys: ExternalApiKey[] = [];
  loading = true;
  showCreateModal = false;
  showKeyModal = false;
  newGeneratedKey = '';
  toastMessage = '';

  newKeyName = '';
  newRateLimit = 120;
  expiresInDays = 365;

  selectedScopes: { [key: string]: boolean } = {
    'customers:read': true,
    'customers:write': true,
    'products:read': true,
    'inventory:read': true,
    'bookings:read': true,
    'invoices:read': true,
    'quotes:write': true
  };

  availableScopes = [
    { code: 'customers:read', name: 'Read Customers', desc: 'Query and filter customer details' },
    { code: 'customers:write', name: 'Create/Update Customers', desc: 'Create or update customer profiles' },
    { code: 'products:read', name: 'Read Products', desc: 'Browse catalog, descriptions, and rental pricing' },
    { code: 'inventory:read', name: 'Check Inventory & Availability', desc: 'Query item availability for date ranges' },
    { code: 'bookings:read', name: 'Read Bookings', desc: 'View confirmed rental schedules and items' },
    { code: 'invoices:read', name: 'Read Invoices', desc: 'View billing totals, balance due, and payments' },
    { code: 'quotes:write', name: 'Submit Quote Inquiries', desc: 'Create new quote requests from websites or forms' }
  ];

  constructor(private integrationService: IntegrationService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.integrationService.getApiKeys().subscribe({
      next: (list) => {
        this.apiKeys = list;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  openCreateModal(): void {
    this.newKeyName = '';
    this.newRateLimit = 120;
    this.expiresInDays = 365;
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  closeKeyModal(): void {
    this.showKeyModal = false;
    this.newGeneratedKey = '';
    this.loadData();
  }

  submitCreate(): void {
    if (!this.newKeyName) {
      this.showToast('Please enter an API Key name');
      return;
    }

    const scopes = Object.keys(this.selectedScopes).filter((s) => this.selectedScopes[s]);
    if (scopes.length === 0) {
      this.showToast('Please select at least one permission scope');
      return;
    }

    const exp = new Date();
    exp.setDate(exp.getDate() + this.expiresInDays);

    const req: CreateApiKeyRequest = {
      name: this.newKeyName,
      scopes,
      rateLimitPerMinute: this.newRateLimit,
      expiresAt: exp.toISOString()
    };

    this.integrationService.generateApiKey(req).subscribe({
      next: (res) => {
        this.showCreateModal = false;
        this.newGeneratedKey = res.rawApiKey;
        this.showKeyModal = true;
      },
      error: () => this.showToast('Failed to generate API Key')
    });
  }

  revokeKey(id: string): void {
    if (confirm('Revoke this API Key? Any integrated service using it will immediately receive HTTP 401 Unauthorized.')) {
      this.integrationService.revokeApiKey(id).subscribe({
        next: () => {
          this.showToast('API Key revoked');
          this.loadData();
        },
        error: () => this.showToast('Failed to revoke API key')
      });
    }
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => (this.toastMessage = ''), 3500);
  }
}
