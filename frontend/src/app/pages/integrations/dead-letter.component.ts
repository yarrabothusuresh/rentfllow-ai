import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import { WebhookDelivery } from '../../models/integration.model';

@Component({
  selector: 'app-dead-letter',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dead-letter.component.html',
  styleUrls: ['./dead-letter.component.scss']
})
export class DeadLetterComponent implements OnInit {
  deadDeliveries: WebhookDelivery[] = [];
  selectedDelivery: WebhookDelivery | null = null;
  loading = true;
  toastMessage = '';

  constructor(private integrationService: IntegrationService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.integrationService.getDeadLetterDeliveries().subscribe({
      next: (list) => {
        this.deadDeliveries = list;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  retry(id: string): void {
    this.integrationService.retryDelivery(id).subscribe({
      next: (res) => {
        this.showToast(`Retry dispatched: ${res.status} (HTTP ${res.httpStatus || 200})`);
        this.loadData();
      },
      error: () => this.showToast('Manual retry failed')
    });
  }

  ignore(id: string): void {
    if (confirm('Acknowledge and dismiss this dead-letter item?')) {
      this.deadDeliveries = this.deadDeliveries.filter((d) => d.id !== id);
      this.showToast('Dead-letter item dismissed');
    }
  }

  viewDetail(d: WebhookDelivery): void {
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
