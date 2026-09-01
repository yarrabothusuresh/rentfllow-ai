import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IntegrationService } from '../../services/integration.service';
import { WebhookEndpoint, CreateWebhookRequest } from '../../models/integration.model';

@Component({
  selector: 'app-webhooks-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './webhooks-list.component.html',
  styleUrls: ['./webhooks-list.component.scss']
})
export class WebhooksListComponent implements OnInit {
  webhooks: WebhookEndpoint[] = [];
  loading = true;
  showCreateModal = false;
  showSecretModal = false;
  newSigningSecret = '';
  toastMessage = '';

  newWebhookName = '';
  newWebhookUrl = '';
  newWebhookDesc = '';
  selectedEvents: { [key: string]: boolean } = {
    'booking.created': true,
    'booking.cancelled': true,
    'invoice.created': true,
    'invoice.sent': false,
    'payment.received': true,
    'quote.approved': true,
    'damage_claim.created': true,
    'inventory.updated': false
  };

  availableEventTypes = [
    { code: 'booking.created', label: 'Booking Created (Confirmed)' },
    { code: 'booking.cancelled', label: 'Booking Cancelled' },
    { code: 'invoice.created', label: 'Invoice Issued' },
    { code: 'invoice.sent', label: 'Invoice Sent to Customer' },
    { code: 'payment.received', label: 'Payment Received & Applied' },
    { code: 'quote.approved', label: 'Quote Approved by Customer' },
    { code: 'damage_claim.created', label: 'Damage Claim Registered' },
    { code: 'inventory.updated', label: 'Inventory Stock Adjusted' }
  ];

  constructor(private integrationService: IntegrationService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.integrationService.getWebhooks().subscribe({
      next: (list) => {
        this.webhooks = list;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  openCreateModal(): void {
    this.newWebhookName = '';
    this.newWebhookUrl = 'https://';
    this.newWebhookDesc = '';
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  closeSecretModal(): void {
    this.showSecretModal = false;
    this.newSigningSecret = '';
    this.loadData();
  }

  submitCreate(): void {
    if (!this.newWebhookName || !this.newWebhookUrl) {
      this.showToast('Please provide a name and endpoint URL');
      return;
    }

    const events = Object.keys(this.selectedEvents).filter((k) => this.selectedEvents[k]);

    const req: CreateWebhookRequest = {
      name: this.newWebhookName,
      endpointUrl: this.newWebhookUrl,
      description: this.newWebhookDesc,
      subscribedEvents: events.length > 0 ? events : ['*']
    };

    this.integrationService.createWebhook(req).subscribe({
      next: (res) => {
        this.showCreateModal = false;
        this.newSigningSecret = res.signingSecret;
        this.showSecretModal = true;
      },
      error: () => this.showToast('Failed to create webhook endpoint')
    });
  }

  testWebhook(id: string): void {
    this.integrationService.testWebhook(id).subscribe({
      next: (res) => {
        this.showToast(`Test dispatched! HTTP ${res.httpStatus || 200} (${res.status})`);
        this.loadData();
      },
      error: () => this.showToast('Test webhook delivery failed')
    });
  }

  rotateSecret(id: string): void {
    if (confirm('Rotate signing secret? You will need to update your consumer verification code.')) {
      this.integrationService.rotateWebhookSecret(id).subscribe({
        next: (res) => {
          this.newSigningSecret = res.newSigningSecret;
          this.showSecretModal = true;
        },
        error: () => this.showToast('Failed to rotate secret')
      });
    }
  }

  deleteWebhook(id: string): void {
    if (confirm('Delete this webhook endpoint?')) {
      this.integrationService.deleteWebhook(id).subscribe({
        next: () => {
          this.showToast('Webhook endpoint deleted');
          this.loadData();
        },
        error: () => this.showToast('Failed to delete webhook')
      });
    }
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => (this.toastMessage = ''), 3500);
  }
}
