import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DeliveryService, Delivery, Driver } from '../../services/delivery.service';

@Component({
  selector: 'app-delivery-calendar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Delivery Schedule & Calendar</h1>
          <p class="page-subtitle">Schedule grid by date and driver dispatch allocations.</p>
        </div>
        <div class="action-buttons">
          <a routerLink="/dashboard/delivery/dashboard" class="btn btn-secondary">Dashboard</a>
          <a routerLink="/dashboard/delivery/routes" class="btn btn-secondary">Route Planner</a>
        </div>
      </div>

      <!-- Date Selector & Driver Filter -->
      <div class="filter-bar glass-card">
        <div class="filter-group">
          <label>Filter Date</label>
          <input type="date" [(ngModel)]="selectedDate" (change)="loadSchedule()" />
        </div>

        <div class="filter-group">
          <label>Filter Driver</label>
          <select [(ngModel)]="selectedDriverId" (change)="loadSchedule()">
            <option value="">All Drivers</option>
            <option *ngFor="let drv of drivers" [value]="drv.id">{{ drv.name }}</option>
          </select>
        </div>
      </div>

      <!-- Calendar Schedule Grid -->
      <div class="section-card glass-card mt-4">
        <h2>Schedule Grid ({{ selectedDate || 'Today' }})</h2>
        <div class="schedule-grid" *ngIf="deliveries.length > 0; else noItems">
          <div class="schedule-card" *ngFor="let d of deliveries">
            <div class="card-top">
              <span class="delivery-num font-bold">{{ d.deliveryNumber }}</span>
              <span class="status-badge" [class]="d.status.toLowerCase()">{{ d.customerFacingStatus || d.status }}</span>
            </div>

            <div class="card-body">
              <div class="cust-info font-bold">{{ d.customerName }}</div>
              <div class="addr-info text-muted">{{ d.deliveryAddressSnapshot }}</div>
              <div class="time-window">
                ⏰ Window: {{ d.scheduledStartTime || '09:00' }} - {{ d.scheduledEndTime || '17:00' }}
              </div>
              <div class="driver-info">
                👤 Driver: {{ d.driverName || 'Unassigned' }} | 🚛 Vehicle: {{ d.vehicleNumber || 'Unassigned' }}
              </div>
            </div>

            <div class="card-footer">
              <a [routerLink]="['/dashboard/delivery', d.id]" class="btn-sm btn-secondary">Manage Schedule</a>
            </div>
          </div>
        </div>

        <ng-template #noItems>
          <div class="empty-state">
            <p>No deliveries scheduled for this date.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-title { font-size: 1.75rem; font-weight: 700; color: #f8fafc; margin: 0; }
    .page-subtitle { color: #94a3b8; margin-top: 0.25rem; font-size: 0.95rem; }
    .action-buttons { display: flex; gap: 0.75rem; }
    .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 0.75rem; padding: 1.25rem; }
    .filter-bar { display: flex; gap: 1rem; align-items: flex-end; }
    .filter-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .filter-group label { font-size: 0.8rem; color: #94a3b8; font-weight: 600; text-transform: uppercase; }
    .filter-group select, .filter-group input { background: rgba(15, 23, 42, 0.8); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 0.375rem; color: #f1f5f9; padding: 0.45rem 0.75rem; font-size: 0.9rem; }
    .mt-4 { margin-top: 1rem; }
    .section-card h2 { font-size: 1.1rem; color: #f1f5f9; margin-top: 0; margin-bottom: 1rem; }
    .schedule-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; }
    .schedule-card { background: rgba(15, 23, 42, 0.6); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 0.5rem; padding: 1rem; display: flex; flex-direction: column; justify-content: space-between; }
    .card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .delivery-num { color: #38bdf8; }
    .card-body { display: flex; flex-direction: column; gap: 0.4rem; font-size: 0.85rem; }
    .cust-info { color: #f8fafc; font-size: 0.95rem; }
    .addr-info { font-size: 0.8rem; }
    .time-window { color: #fbbf24; font-weight: 500; margin-top: 0.3rem; }
    .driver-info { color: #94a3b8; }
    .card-footer { margin-top: 1rem; text-align: right; }
    .status-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; }
    .status-badge.pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-badge.scheduled { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-badge.assigned { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-badge.ready { background: rgba(236, 72, 153, 0.2); color: #f472b6; }
    .status-badge.out_for_delivery { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
    .status-badge.delivered { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .btn { padding: 0.5rem 1rem; border-radius: 0.375rem; font-weight: 600; text-decoration: none; cursor: pointer; }
    .btn-secondary { background: rgba(51, 65, 85, 0.8); color: #f1f5f9; border: 1px solid rgba(255, 255, 255, 0.1); }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 0.25rem; font-size: 0.8rem; text-decoration: none; }
    .font-bold { font-weight: 600; }
    .text-muted { color: #64748b; }
    .empty-state { text-align: center; padding: 2rem; color: #94a3b8; }
  `]
})
export class DeliveryCalendarComponent implements OnInit {
  deliveries: Delivery[] = [];
  drivers: Driver[] = [];
  selectedDate = new Date().toISOString().substring(0, 10);
  selectedDriverId = '';

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.loadSchedule();
    this.deliveryService.getDrivers().subscribe(data => this.drivers = data);
  }

  loadSchedule(): void {
    this.deliveryService.getDeliveries(this.selectedDate, undefined, this.selectedDriverId || undefined).subscribe({
      next: (data) => this.deliveries = data,
      error: (err) => console.error('Failed to load schedule', err)
    });
  }
}
