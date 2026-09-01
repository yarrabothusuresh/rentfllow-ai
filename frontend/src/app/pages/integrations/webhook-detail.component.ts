import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import { WebhookEndpoint, WebhookDelivery } from '../../models/integration.model';

@Component({
  selector: 'app-webhook-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './webhook-detail.component.html',
  styleUrls: ['./webhook-detail.component.scss']
})
export class WebhookDetailComponent implements OnInit {
  endpointId: string | null = null;
  endpoint: WebhookEndpoint | null = null;
  deliveries: WebhookDelivery[] = [];
  selectedDelivery: WebhookDelivery | null = null;
  loading = true;
  toastMessage = '';

  constructor(
    private route: ActivatedRoute,
    private integrationService: IntegrationService
  ) {}

  ngOnInit(): void {
    this.endpointId = this.route.snapshot.paramMap.get('id');
    if (this.endpointId) {
      this.loadData();
    }
  }

  loadData(): void {
    if (!this.endpointId) return;
    this.loading = true;

    this.integrationService.getWebhook(this.endpointId).subscribe({
      next: (ep) => {
        this.endpoint = ep;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });

    this.integrationService.getWebhookDeliveries(this.endpointId, 50).subscribe({
      next: (list) => (this.deliveries = list),
      error: () => {}
    });
  }

  testWebhook(): void {
    if (!this.endpointId) return;
    this.integrationService.testWebhook(this.endpointId).subscribe({
      next: (res) => {
        this.showToast(`Test delivery completed with status ${res.status} (HTTP ${res.httpStatus})`);
        this.loadData();
      },
      error: () => this.showToast('Test failed')
    });
  }

  retryDelivery(deliveryId: string): void {
    this.integrationService.retryDelivery(deliveryId).subscribe({
      next: (res) => {
        this.showToast(`Retry attempt #${res.attemptNumber} completed: ${res.status}`);
        this.loadData();
      },
      error: () => this.showToast('Retry request failed')
    });
  }

  viewPayload(d: WebhookDelivery): void {
    this.selectedDelivery = d;
  }

  closeModal(): void {
    this.selectedDelivery = null;
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => (this.toastMessage = ''), 3500);
  }
}
