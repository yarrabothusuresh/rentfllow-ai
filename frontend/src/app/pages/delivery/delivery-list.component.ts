import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DeliveryService, Delivery, Driver, Vehicle } from '../../services/delivery.service';

@Component({
  selector: 'app-delivery-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Deliveries</h1>
          <p class="page-subtitle">Full list of scheduled, active, and completed rental equipment deliveries.</p>
        </div>
        <div class="action-buttons">
          <a routerLink="/dashboard/delivery/routes" class="btn btn-secondary">Route Planner</a>
          <a routerLink="/dashboard/delivery/calendar" class="btn btn-secondary">Calendar</a>
        </div>
      </div>

      <!-- Filters & Controls Bar -->
      <div class="filter-bar glass-card">
        <div class="filter-group">
          <label>Status</label>
          <select [(ngModel)]="selectedStatus" (change)="loadDeliveries()">
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="READY">Ready</option>
            <option value="OUT_FOR_DELIVERY">Out For Delivery</option>
            <option value="ARRIVED">Arrived</option>
            <option value="SETUP_IN_PROGRESS">Setup In Progress</option>
            <option value="DELIVERED">Delivered</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Date</label>
          <input type="date" [(ngModel)]="selectedDate" (change)="loadDeliveries()" />
        </div>

        <div class="filter-group">
          <label>Driver</label>
          <select [(ngModel)]="selectedDriverId" (change)="loadDeliveries()">
            <option value="">All Drivers</option>
            <option *ngFor="let drv of drivers" [value]="drv.id">{{ drv.name }}</option>
          </select>
        </div>

        <div class="filter-group">
          <label>Vehicle</label>
          <select [(ngModel)]="selectedVehicleId" (change)="loadDeliveries()">
            <option value="">All Vehicles</option>
            <option *ngFor="let veh of vehicles" [value]="veh.id">{{ veh.vehicleNumber }} ({{ veh.name }})</option>
          </select>
        </div>

        <button class="btn btn-outline" (click)="resetFilters()">Reset</button>
      </div>

      <!-- Deliveries Data Table -->
      <div class="section-card glass-card mt-4">
        <div class="table-container" *ngIf="deliveries.length > 0; else noData">
          <table class="data-table">
            <thead>
              <tr>
                <th>Delivery #</th>
                <th>Type</th>
                <th>Customer / Address</th>
                <th>Scheduled Date & Time</th>
                <th>Priority</th>
                <th>Driver</th>
                <th>Vehicle</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let d of deliveries">
                <td class="font-bold">
                  <a [routerLink]="['/dashboard/delivery', d.id]" class="code-link">{{ d.deliveryNumber }}</a>
                </td>
                <td>
                  <span class="type-pill">{{ d.deliveryType }}</span>
                </td>
                <td>
                  <div class="customer-cell">
                    <span class="cust-name">{{ d.customerName || 'Customer' }}</span>
                    <span class="addr-text text-muted">{{ d.deliveryAddressSnapshot }}</span>
                  </div>
                </td>
                <td>
                  <div class="time-cell">
                    <span>{{ d.scheduledDate || 'Not set' }}</span>
                    <span class="text-muted" *ngIf="d.scheduledStartTime">{{ d.scheduledStartTime }} - {{ d.scheduledEndTime }}</span>
                  </div>
                </td>
                <td>
                  <span class="priority-badge" [class]="d.priority.toLowerCase()">{{ d.priority }}</span>
                </td>
                <td>
                  <span *ngIf="d.driverName">👤 {{ d.driverName }}</span>
                  <button *ngIf="!d.driverName" class="btn-xs btn-assign" (click)="openAssignDriverModal(d)">Assign</button>
                </td>
                <td>
                  <span *ngIf="d.vehicleNumber">🚛 {{ d.vehicleNumber }}</span>
                  <button *ngIf="!d.vehicleNumber" class="btn-xs btn-assign" (click)="openAssignVehicleModal(d)">Assign</button>
                </td>
                <td>
                  <span class="status-badge" [class]="d.status.toLowerCase()">{{ d.customerFacingStatus || d.status }}</span>
                </td>
                <td>
                  <div class="action-btn-group">
                    <a [routerLink]="['/dashboard/delivery', d.id]" class="btn-sm btn-secondary">View</a>
                    <button *ngIf="d.status === 'ASSIGNED' || d.status === 'READY'" class="btn-sm btn-action" (click)="startDelivery(d)">Start</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noData>
          <div class="empty-state">
            <p>No deliveries match the selected filters.</p>
          </div>
        </ng-template>
      </div>

      <!-- Assign Driver Modal -->
      <div class="modal-overlay" *ngIf="selectedDeliveryForDriver">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Assign Driver to {{ selectedDeliveryForDriver.deliveryNumber }}</h3>
            <button class="close-btn" (click)="selectedDeliveryForDriver = null">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Select Available Driver</label>
              <select [(ngModel)]="assigningDriverId">
                <option value="">-- Choose Driver --</option>
                <option *ngFor="let drv of drivers" [value]="drv.id">{{ drv.name }} ({{ drv.status }})</option>
              </select>
            </div>
            <p class="error-msg" *ngIf="assignError">{{ assignError }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="selectedDeliveryForDriver = null">Cancel</button>
            <button class="btn btn-primary" (click)="confirmAssignDriver()">Confirm Driver</button>
          </div>
        </div>
      </div>

      <!-- Assign Vehicle Modal -->
      <div class="modal-overlay" *ngIf="selectedDeliveryForVehicle">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Assign Vehicle to {{ selectedDeliveryForVehicle.deliveryNumber }}</h3>
            <button class="close-btn" (click)="selectedDeliveryForVehicle = null">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Select Available Vehicle</label>
              <select [(ngModel)]="assigningVehicleId">
                <option value="">-- Choose Vehicle --</option>
                <option *ngFor="let veh of vehicles" [value]="veh.id">{{ veh.vehicleNumber }} - {{ veh.name }} ({{ veh.status }})</option>
              </select>
            </div>
            <p class="error-msg" *ngIf="assignError">{{ assignError }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="selectedDeliveryForVehicle = null">Cancel</button>
            <button class="btn btn-primary" (click)="confirmAssignVehicle()">Confirm Vehicle</button>
          </div>
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
    .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 0.75rem; padding: 1.25rem; }
    .filter-bar { display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap; }
    .filter-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .filter-group label { font-size: 0.8rem; color: #94a3b8; font-weight: 600; text-transform: uppercase; }
    .filter-group select, .filter-group input { background: rgba(15, 23, 42, 0.8); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 0.375rem; color: #f1f5f9; padding: 0.45rem 0.75rem; font-size: 0.9rem; }
    .mt-4 { margin-top: 1rem; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th { padding: 0.75rem 1rem; background: rgba(15, 23, 42, 0.6); color: #94a3b8; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; }
    .data-table td { padding: 0.85rem 1rem; border-top: 1px solid rgba(255, 255, 255, 0.05); color: #e2e8f0; font-size: 0.9rem; }
    .code-link { color: #38bdf8; font-weight: 600; text-decoration: none; }
    .type-pill { background: rgba(51, 65, 85, 0.5); padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; color: #cbd5e1; }
    .customer-cell { display: flex; flex-direction: column; }
    .cust-name { font-weight: 600; }
    .addr-text { font-size: 0.8rem; color: #64748b; max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .time-cell { display: flex; flex-direction: column; font-size: 0.85rem; }
    .priority-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .priority-badge.high, .priority-badge.urgent { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .priority-badge.normal { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .priority-badge.low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }
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
    .btn-outline { background: transparent; color: #94a3b8; border: 1px solid rgba(255, 255, 255, 0.15); }
    .btn-sm { padding: 0.3rem 0.6rem; border-radius: 0.25rem; font-size: 0.8rem; text-decoration: none; }
    .btn-xs { padding: 0.15rem 0.4rem; border-radius: 0.2rem; font-size: 0.75rem; border: none; cursor: pointer; }
    .btn-assign { background: rgba(245, 158, 11, 0.2); color: #fbbf24; }
    .btn-action { background: rgba(34, 197, 94, 0.2); color: #4ade80; border: none; }
    .action-btn-group { display: flex; gap: 0.4rem; }
    .text-muted { color: #64748b; }
    .empty-state { text-align: center; padding: 2rem; color: #94a3b8; }
    .modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { width: 100%; max-width: 450px; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h3 { font-size: 1.1rem; color: #f8fafc; margin: 0; }
    .close-btn { background: transparent; border: none; color: #94a3b8; font-size: 1.5rem; cursor: pointer; }
    .modal-body { margin-bottom: 1.5rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.5rem; }
    .form-group label { font-size: 0.85rem; color: #cbd5e1; }
    .form-group select { background: rgba(15, 23, 42, 0.9); border: 1px solid rgba(255, 255, 255, 0.2); color: #f8fafc; padding: 0.6rem; border-radius: 0.375rem; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; }
    .error-msg { color: #f87171; font-size: 0.85rem; margin-top: 0.5rem; }
  `]
})
export class DeliveryListComponent implements OnInit {
  deliveries: Delivery[] = [];
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];

  selectedStatus = '';
  selectedDate = '';
  selectedDriverId = '';
  selectedVehicleId = '';

  selectedDeliveryForDriver: Delivery | null = null;
  assigningDriverId = '';

  selectedDeliveryForVehicle: Delivery | null = null;
  assigningVehicleId = '';

  assignError = '';

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.loadDeliveries();
    this.loadResources();
  }

  loadDeliveries(): void {
    this.deliveryService.getDeliveries(
      this.selectedDate || undefined,
      this.selectedStatus || undefined,
      this.selectedDriverId || undefined,
      this.selectedVehicleId || undefined
    ).subscribe({
      next: (data) => this.deliveries = data,
      error: (err) => console.error('Failed to load deliveries', err)
    });
  }

  loadResources(): void {
    this.deliveryService.getDrivers().subscribe(data => this.drivers = data);
    this.deliveryService.getVehicles().subscribe(data => this.vehicles = data);
  }

  resetFilters(): void {
    this.selectedStatus = '';
    this.selectedDate = '';
    this.selectedDriverId = '';
    this.selectedVehicleId = '';
    this.loadDeliveries();
  }

  openAssignDriverModal(d: Delivery): void {
    this.selectedDeliveryForDriver = d;
    this.assigningDriverId = d.driverId || '';
    this.assignError = '';
  }

  confirmAssignDriver(): void {
    if (!this.selectedDeliveryForDriver || !this.assigningDriverId) return;
    this.deliveryService.assignDriver(this.selectedDeliveryForDriver.id, this.assigningDriverId).subscribe({
      next: () => {
        this.selectedDeliveryForDriver = null;
        this.loadDeliveries();
      },
      error: (err) => {
        this.assignError = err?.error?.error || err?.error?.message || 'Driver collision or assignment error';
      }
    });
  }

  openAssignVehicleModal(d: Delivery): void {
    this.selectedDeliveryForVehicle = d;
    this.assigningVehicleId = d.vehicleId || '';
    this.assignError = '';
  }

  confirmAssignVehicle(): void {
    if (!this.selectedDeliveryForVehicle || !this.assigningVehicleId) return;
    this.deliveryService.assignVehicle(this.selectedDeliveryForVehicle.id, this.assigningVehicleId).subscribe({
      next: () => {
        this.selectedDeliveryForVehicle = null;
        this.loadDeliveries();
      },
      error: (err) => {
        this.assignError = err?.error?.error || err?.error?.message || 'Vehicle collision or assignment error';
      }
    });
  }

  startDelivery(d: Delivery): void {
    this.deliveryService.startDelivery(d.id).subscribe({
      next: () => this.loadDeliveries(),
      error: (err) => alert(err?.error?.error || 'Failed to start delivery')
    });
  }
}
