import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DeliveryService, DeliveryDashboard, Delivery } from '../../services/delivery.service';

@Component({
  selector: 'app-delivery-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Delivery & Fleet Dispatch</h1>
          <p class="page-subtitle">Manage driver assignments, delivery schedules, and route dispatch operations.</p>
        </div>
        <div class="action-buttons">
          <a routerLink="/dashboard/delivery/routes" class="btn btn-primary">
            <span class="icon">📍</span> Route Planner
          </a>
          <a routerLink="/dashboard/delivery/calendar" class="btn btn-secondary">
            <span class="icon">📅</span> Calendar View
          </a>
          <a routerLink="/dashboard/delivery/drivers" class="btn btn-secondary">
            <span class="icon">🚚</span> Drivers & Fleet
          </a>
        </div>
      </div>

      <!-- KPI Metrics Row -->
      <div class="metrics-grid" *ngIf="dashboard">
        <div class="metric-card glass-card">
          <div class="metric-icon blue">📦</div>
          <div class="metric-data">
            <span class="metric-value">{{ dashboard.todaysDeliveries }}</span>
            <span class="metric-label">Today's Deliveries</span>
          </div>
        </div>

        <div class="metric-card glass-card">
          <div class="metric-icon orange">🚚</div>
          <div class="metric-data">
            <span class="metric-value">{{ dashboard.outForDelivery }}</span>
            <span class="metric-label">Out on Delivery</span>
          </div>
        </div>

        <div class="metric-card glass-card">
          <div class="metric-icon green">✅</div>
          <div class="metric-data">
            <span class="metric-value">{{ dashboard.completedToday }}</span>
            <span class="metric-label">Delivered Today</span>
          </div>
        </div>

        <div class="metric-card glass-card">
          <div class="metric-icon red">⚠️</div>
          <div class="metric-data">
            <span class="metric-value">{{ dashboard.failedDeliveries }}</span>
            <span class="metric-label">Failed Attempts</span>
          </div>
        </div>

        <div class="metric-card glass-card">
          <div class="metric-icon purple">👤</div>
          <div class="metric-data">
            <span class="metric-value">{{ dashboard.unassignedDeliveries }}</span>
            <span class="metric-label">Unassigned Deliveries</span>
          </div>
        </div>
      </div>

      <!-- Main Section: Recent / Active Deliveries -->
      <div class="section-card glass-card mt-6">
        <div class="section-header">
          <h2>Active & Scheduled Deliveries</h2>
          <a routerLink="/dashboard/delivery/list" class="btn-link">View All Deliveries →</a>
        </div>

        <div class="table-container" *ngIf="dashboard && dashboard.deliveries && dashboard.deliveries.length > 0">
          <table class="data-table">
            <thead>
              <tr>
                <th>Delivery #</th>
                <th>Type</th>
                <th>Customer / Venue</th>
                <th>Schedule</th>
                <th>Priority</th>
                <th>Driver & Vehicle</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let d of dashboard.deliveries">
                <td class="font-bold">
                  <a [routerLink]="['/dashboard/delivery', d.id]" class="code-link">{{ d.deliveryNumber }}</a>
                </td>
                <td>
                  <span class="type-pill">{{ d.deliveryType }}</span>
                </td>
                <td>
                  <div class="customer-info">
                    <span class="cust-name">{{ d.customerName || 'Customer' }}</span>
                    <span class="venue-addr text-muted">{{ d.deliveryAddressSnapshot }}</span>
                  </div>
                </td>
                <td>
                  <div class="time-info">
                    <span>{{ d.scheduledDate || 'Today' }}</span>
                    <span class="text-muted" *ngIf="d.scheduledStartTime">{{ d.scheduledStartTime }} - {{ d.scheduledEndTime }}</span>
                  </div>
                </td>
                <td>
                  <span class="priority-badge" [class]="d.priority.toLowerCase()">{{ d.priority }}</span>
                </td>
                <td>
                  <div class="assignment-info" *ngIf="d.driverName || d.vehicleNumber; else unassigned">
                    <span *ngIf="d.driverName">👤 {{ d.driverName }}</span>
                    <span *ngIf="d.vehicleNumber" class="text-muted">🚛 {{ d.vehicleNumber }}</span>
                  </div>
                  <ng-template #unassigned>
                    <span class="unassigned-badge">Needs Driver</span>
                  </ng-template>
                </td>
                <td>
                  <span class="status-badge" [class]="d.status.toLowerCase()">{{ d.customerFacingStatus || d.status }}</span>
                </td>
                <td>
                  <a [routerLink]="['/dashboard/delivery', d.id]" class="btn-sm btn-secondary">Manage</a>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="empty-state" *ngIf="!dashboard || !dashboard.deliveries || dashboard.deliveries.length === 0">
          <p>No active deliveries found for today.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-title { font-size: 1.75rem; font-weight: 700; color: #f8fafc; margin: 0; }
    .page-subtitle { color: #94a3b8; margin-top: 0.25rem; font-size: 0.95rem; }
    .action-buttons { display: flex; gap: 0.75rem; }
    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 0.75rem; padding: 1.25rem; }
    .metric-card { display: flex; align-items: center; gap: 1rem; }
    .metric-icon { width: 44px; height: 44px; border-radius: 0.5rem; display: flex; align-items: center; justify-content: center; font-size: 1.3rem; }
    .metric-icon.blue { background: rgba(59, 130, 246, 0.2); }
    .metric-icon.orange { background: rgba(249, 115, 22, 0.2); }
    .metric-icon.green { background: rgba(34, 197, 94, 0.2); }
    .metric-icon.red { background: rgba(239, 68, 68, 0.2); }
    .metric-icon.purple { background: rgba(168, 85, 247, 0.2); }
    .metric-data { display: flex; flex-direction: column; }
    .metric-value { font-size: 1.5rem; font-weight: 700; color: #f8fafc; }
    .metric-label { font-size: 0.85rem; color: #94a3b8; }
    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .section-header h2 { font-size: 1.2rem; font-weight: 600; color: #f1f5f9; margin: 0; }
    .btn-link { color: #38bdf8; text-decoration: none; font-weight: 500; font-size: 0.9rem; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { padding: 0.75rem 1rem; background: rgba(15, 23, 42, 0.6); color: #94a3b8; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; }
    .data-table td { padding: 0.85rem 1rem; border-top: 1px solid rgba(255, 255, 255, 0.05); color: #e2e8f0; font-size: 0.9rem; }
    .code-link { color: #38bdf8; font-weight: 600; text-decoration: none; }
    .type-pill { background: rgba(51, 65, 85, 0.5); padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; color: #cbd5e1; }
    .customer-info { display: flex; flex-direction: column; }
    .cust-name { font-weight: 600; }
    .venue-addr { font-size: 0.8rem; color: #64748b; max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .time-info { display: flex; flex-direction: column; font-size: 0.85rem; }
    .priority-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .priority-badge.high, .priority-badge.urgent { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .priority-badge.normal { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .priority-badge.low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }
    .unassigned-badge { background: rgba(245, 158, 11, 0.2); color: #fbbf24; padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; }
    .assignment-info { display: flex; flex-direction: column; font-size: 0.85rem; }
    .status-badge { padding: 0.25rem 0.6rem; border-radius: 0.375rem; font-size: 0.8rem; font-weight: 600; display: inline-block; }
    .status-badge.pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-badge.scheduled { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-badge.assigned { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-badge.ready { background: rgba(236, 72, 153, 0.2); color: #f472b6; }
    .status-badge.out_for_delivery { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
    .status-badge.arrived, .status-badge.setup_in_progress { background: rgba(234, 179, 8, 0.2); color: #facc15; }
    .status-badge.delivered { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-badge.failed { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .btn { padding: 0.5rem 1rem; border-radius: 0.375rem; font-weight: 600; text-decoration: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.4rem; font-size: 0.9rem; }
    .btn-primary { background: #0284c7; color: white; border: none; }
    .btn-secondary { background: rgba(51, 65, 85, 0.8); color: #f1f5f9; border: 1px solid rgba(255, 255, 255, 0.1); }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 0.25rem; font-size: 0.8rem; text-decoration: none; }
    .text-muted { color: #64748b; }
    .mt-6 { margin-top: 1.5rem; }
    .empty-state { text-align: center; padding: 2rem; color: #94a3b8; }
  `]
})
export class DeliveryDashboardComponent implements OnInit {
  dashboard?: DeliveryDashboard;

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.deliveryService.getDashboard().subscribe({
      next: (data) => this.dashboard = data,
      error: (err) => console.error('Failed to load delivery dashboard', err)
    });
  }
}
