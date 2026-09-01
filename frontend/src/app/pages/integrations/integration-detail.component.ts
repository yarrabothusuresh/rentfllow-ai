import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import {
  IntegrationConnection,
  IntegrationSyncJob,
  ExternalEntityMapping
} from '../../models/integration.model';

@Component({
  selector: 'app-integration-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './integration-detail.component.html',
  styleUrls: ['./integration-detail.component.scss']
})
export class IntegrationDetailComponent implements OnInit {
  connectionId: string | null = null;
  connection: IntegrationConnection | null = null;
  syncJobs: IntegrationSyncJob[] = [];
  mappings: ExternalEntityMapping[] = [];
  loading = true;
  toastMessage = '';

  constructor(
    private route: ActivatedRoute,
    private integrationService: IntegrationService
  ) {}

  ngOnInit(): void {
    this.connectionId = this.route.snapshot.paramMap.get('id');
    if (this.connectionId) {
      this.loadData();
    }
  }

  loadData(): void {
    if (!this.connectionId) return;
    this.loading = true;

    this.integrationService.getConnection(this.connectionId).subscribe({
      next: (conn) => {
        this.connection = conn;
        this.loading = false;
        this.loadMappings();
      },
      error: () => (this.loading = false)
    });

    this.integrationService.getSyncJobs(this.connectionId).subscribe({
      next: (jobs) => (this.syncJobs = jobs),
      error: () => {}
    });
  }

  loadMappings(): void {
    if (!this.connection) return;
    this.integrationService.getEntityMappings(this.connection.provider).subscribe({
      next: (maps) => (this.mappings = maps),
      error: () => {}
    });
  }

  syncNow(): void {
    if (!this.connectionId) return;
    this.integrationService.triggerSync(this.connectionId).subscribe({
      next: (job) => {
        this.showToast(`Sync completed: ${job.recordsProcessed} records processed.`);
        this.loadData();
      },
      error: () => this.showToast('Sync failed')
    });
  }

  testConnection(): void {
    if (!this.connectionId) return;
    this.integrationService.testConnection(this.connectionId).subscribe({
      next: (res) => this.showToast(res.message),
      error: () => this.showToast('Connection test failed')
    });
  }

  disconnect(): void {
    if (!this.connectionId) return;
    if (confirm('Are you sure you want to disconnect this integration?')) {
      this.integrationService.disconnect(this.connectionId).subscribe({
        next: () => {
          this.showToast('Disconnected');
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
