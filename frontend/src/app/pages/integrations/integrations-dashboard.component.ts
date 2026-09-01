import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IntegrationService } from '../../services/integration.service';
import {
  IntegrationConnection,
  IntegrationDashboardSummary,
  IntegrationProvider,
  ConnectIntegrationRequest
} from '../../models/integration.model';

@Component({
  selector: 'app-integrations-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './integrations-dashboard.component.html',
  styleUrls: ['./integrations-dashboard.component.scss']
})
export class IntegrationsDashboardComponent implements OnInit {
  summary: IntegrationDashboardSummary | null = null;
  connections: IntegrationConnection[] = [];
  loading = true;
  showConnectModal = false;
  selectedProvider: IntegrationProvider = 'MOCK_ACCOUNTING';
  connectionName = '';
  configText = '';
  apiSecret = '';
  toastMessage = '';

  availableCatalog: {
    provider: IntegrationProvider;
    name: string;
    category: string;
    description: string;
    icon: string;
    supported: boolean;
  }[] = [
    {
      provider: 'MOCK_ACCOUNTING',
      name: 'QuickBooks Online (Sandbox)',
      category: 'Accounting',
      description: 'Synchronize invoices, payments, refunds and customers automatically.',
      icon: '💼',
      supported: true
    },
    {
      provider: 'MOCK_CRM',
      name: 'HubSpot CRM (Sandbox)',
      category: 'CRM & Pipeline',
      description: 'Sync leads, customers, approved quotes and rental deals.',
      icon: '🎯',
      supported: true
    },
    {
      provider: 'MOCK_COMMERCE',
      name: 'Shopify Storefront (Demo)',
      category: 'E-Commerce',
      description: 'Rental catalog sync and date-aware availability integration.',
      icon: '🛍️',
      supported: true
    },
    {
      provider: 'ZAPIER',
      name: 'Zapier Webhooks',
      category: 'Automation',
      description: 'Trigger 5,000+ app automations via signed outbound webhooks.',
      icon: '⚡',
      supported: true
    },
    {
      provider: 'MAKE',
      name: 'Make (Integromat)',
      category: 'Automation',
      description: 'Custom visual rental workflows and scenario integrations.',
      icon: '🔄',
      supported: true
    },
    {
      provider: 'N8N',
      name: 'n8n Workflow Automation',
      category: 'Self-Hosted Automation',
      description: 'Direct webhook automation with enterprise self-hosted n8n.',
      icon: '⚙️',
      supported: true
    },
    {
      provider: 'CUSTOM_WEBHOOK',
      name: 'Custom HTTPS Webhook',
      category: 'Developer Platform',
      description: 'Deliver HMAC-SHA256 signed JSON payloads to your own servers.',
      icon: '📡',
      supported: true
    }
  ];

  constructor(private integrationService: IntegrationService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.integrationService.getDashboardSummary().subscribe({
      next: (summary) => (this.summary = summary),
      error: () => {}
    });

    this.integrationService.getConnections().subscribe({
      next: (conns) => {
        this.connections = conns;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  isConnected(provider: IntegrationProvider): boolean {
    const conn = this.connections.find((c) => c.provider === provider);
    return conn ? conn.status === 'CONNECTED' : false;
  }

  getConnection(provider: IntegrationProvider): IntegrationConnection | undefined {
    return this.connections.find((c) => c.provider === provider);
  }

  openConnectModal(provider: IntegrationProvider, name: string): void {
    this.selectedProvider = provider;
    this.connectionName = name;
    this.configText = '{"environment":"sandbox","syncEnabled":true}';
    this.apiSecret = '';
    this.showConnectModal = true;
  }

  closeConnectModal(): void {
    this.showConnectModal = false;
  }

  submitConnect(): void {
    const req: ConnectIntegrationRequest = {
      provider: this.selectedProvider,
      name: this.connectionName,
      configuration: this.configText,
      apiKeyOrSecret: this.apiSecret
    };

    this.integrationService.connect(req).subscribe({
      next: () => {
        this.showToast('Successfully connected ' + this.connectionName);
        this.closeConnectModal();
        this.loadData();
      },
      error: () => this.showToast('Failed to connect provider')
    });
  }

  syncConnection(id: string): void {
    this.integrationService.triggerSync(id).subscribe({
      next: (job) => {
        this.showToast(`Sync completed: ${job.recordsProcessed} records processed.`);
        this.loadData();
      },
      error: () => this.showToast('Sync failed')
    });
  }

  testConnection(id: string): void {
    this.integrationService.testConnection(id).subscribe({
      next: (res) => this.showToast(res.message),
      error: () => this.showToast('Connection test failed')
    });
  }

  disconnect(id: string): void {
    if (confirm('Are you sure you want to disconnect this integration?')) {
      this.integrationService.disconnect(id).subscribe({
        next: () => {
          this.showToast('Integration disconnected');
          this.loadData();
        },
        error: () => this.showToast('Failed to disconnect')
      });
    }
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => (this.toastMessage = ''), 3500);
  }
}
