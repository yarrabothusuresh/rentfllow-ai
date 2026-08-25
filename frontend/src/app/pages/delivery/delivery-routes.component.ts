import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DeliveryService, DeliveryRoute, Delivery, Driver, Vehicle } from '../../services/delivery.service';

@Component({
  selector: 'app-delivery-routes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Route Planning & Sequencing</h1>
          <p class="page-subtitle">Optimize multi-stop delivery routes and calculate estimated distances and times.</p>
        </div>
        <div class="action-buttons">
          <button class="btn btn-primary" (click)="showCreateModal = true">
            <span>➕</span> Create New Route
          </button>
        </div>
      </div>

      <!-- Filter Controls -->
      <div class="filter-bar glass-card">
        <div class="filter-group">
          <label>Route Date</label>
          <input type="date" [(ngModel)]="selectedDate" (change)="loadRoutes()" />
        </div>
      </div>

      <!-- Routes List Section -->
      <div class="section-card glass-card mt-4">
        <h2>Active Delivery Routes ({{ selectedDate || 'Today' }})</h2>

        <div class="routes-container" *ngIf="routes.length > 0; else noRoutes">
          <div class="route-card" *ngFor="let r of routes">
            <div class="route-header">
              <div class="route-title-block">
                <span class="route-name font-bold">{{ r.routeName }}</span>
                <span class="status-badge" [class]="r.status.toLowerCase()">{{ r.status }}</span>
              </div>
              <div class="route-metrics">
                <span>📍 {{ r.totalStops }} Stops</span>
                <span>🛣️ {{ r.totalDistanceKm }} km</span>
                <span>⏱️ {{ r.totalDurationMinutes }} mins</span>
              </div>
            </div>

            <div class="route-assignments">
              <span>👤 Driver: {{ r.driverName || 'Unassigned' }}</span>
              <span>🚛 Vehicle: {{ r.vehicleNumber || 'Unassigned' }}</span>
            </div>

            <!-- Stop Sequence Table -->
            <div class="stops-list" *ngIf="r.stops && r.stops.length > 0">
              <table class="stops-table">
                <thead>
                  <tr>
                    <th>Seq #</th>
                    <th>Delivery #</th>
                    <th>Est. Arrival</th>
                    <th>Leg Distance</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let s of r.stops">
                    <td class="font-bold">#{{ s.sequenceNumber }}</td>
                    <td>{{ s.deliveryNumber }}</td>
                    <td>{{ s.estimatedArrivalTime || 'N/A' }}</td>
                    <td>{{ s.distanceFromPreviousKm ? s.distanceFromPreviousKm + ' km' : 'Origin' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <ng-template #noRoutes>
          <div class="empty-state">
            <p>No routes generated for this date yet. Click "Create New Route" to generate an optimized route.</p>
          </div>
        </ng-template>
      </div>

      <!-- Create Route Modal -->
      <div class="modal-overlay" *ngIf="showCreateModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Generate Optimized Route</h3>
            <button class="close-btn" (click)="showCreateModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Date</label>
              <input type="date" [(ngModel)]="newRouteDate" />
            </div>

            <div class="form-group mt-3">
              <label>Assign Driver</label>
              <select [(ngModel)]="newRouteDriverId">
                <option value="">-- Optional Driver --</option>
                <option *ngFor="let drv of drivers" [value]="drv.id">{{ drv.name }}</option>
              </select>
            </div>

            <div class="form-group mt-3">
              <label>Assign Vehicle</label>
              <select [(ngModel)]="newRouteVehicleId">
                <option value="">-- Optional Vehicle --</option>
                <option *ngFor="let veh of vehicles" [value]="veh.id">{{ veh.vehicleNumber }} - {{ veh.name }}</option>
              </select>
            </div>

            <div class="form-group mt-3">
              <label>Select Deliveries to Include</label>
              <div class="deliveries-checklist">
                <div class="chk-item" *ngFor="let d of availableDeliveries">
                  <input type="checkbox" [value]="d.id" (change)="toggleDeliverySelection(d.id)" />
                  <span>{{ d.deliveryNumber }} - {{ d.customerName }} ({{ d.deliveryAddressSnapshot }})</span>
                </div>
                <p class="text-muted text-xs" *ngIf="availableDeliveries.length === 0">No available unrouted deliveries found.</p>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showCreateModal = false">Cancel</button>
            <button class="btn btn-primary" (click)="confirmCreateRoute()">Generate & Sequence Route</button>
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
    .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 0.75rem; padding: 1.25rem; }
    .filter-bar { display: flex; gap: 1rem; align-items: flex-end; }
    .filter-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .filter-group label { font-size: 0.8rem; color: #94a3b8; font-weight: 600; text-transform: uppercase; }
    .filter-group input { background: rgba(15, 23, 42, 0.8); border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 0.375rem; color: #f1f5f9; padding: 0.45rem 0.75rem; font-size: 0.9rem; }
    .mt-4 { margin-top: 1rem; }
    .mt-3 { margin-top: 0.75rem; }
    .routes-container { display: flex; flex-direction: column; gap: 1rem; }
    .route-card { background: rgba(15, 23, 42, 0.6); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 0.5rem; padding: 1rem; }
    .route-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255, 255, 255, 0.08); padding-bottom: 0.5rem; margin-bottom: 0.5rem; }
    .route-name { font-size: 1.1rem; color: #f8fafc; margin-right: 0.5rem; }
    .route-metrics { display: flex; gap: 1rem; color: #94a3b8; font-size: 0.85rem; }
    .route-assignments { display: flex; gap: 1.5rem; font-size: 0.85rem; color: #cbd5e1; margin-bottom: 0.75rem; }
    .stops-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.85rem; }
    .stops-table th { padding: 0.4rem 0.75rem; background: rgba(30, 41, 59, 0.5); color: #94a3b8; font-weight: 600; }
    .stops-table td { padding: 0.5rem 0.75rem; border-top: 1px solid rgba(255, 255, 255, 0.05); color: #f1f5f9; }
    .status-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; }
    .status-badge.draft { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-badge.optimized { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-badge.in_progress { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
    .status-badge.completed { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .btn { padding: 0.5rem 1rem; border-radius: 0.375rem; font-weight: 600; border: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.4rem; }
    .btn-primary { background: #0284c7; color: white; }
    .btn-secondary { background: rgba(51, 65, 85, 0.8); color: #f1f5f9; }
    .empty-state { text-align: center; padding: 2rem; color: #94a3b8; }
    .font-bold { font-weight: 600; }
    .modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { width: 100%; max-width: 500px; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h3 { font-size: 1.1rem; color: #f8fafc; margin: 0; }
    .close-btn { background: transparent; border: none; color: #94a3b8; font-size: 1.5rem; cursor: pointer; }
    .modal-body { margin-bottom: 1.5rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .form-group label { font-size: 0.85rem; color: #cbd5e1; }
    .form-group select, .form-group input { background: rgba(15, 23, 42, 0.9); border: 1px solid rgba(255, 255, 255, 0.2); color: #f8fafc; padding: 0.5rem; border-radius: 0.375rem; }
    .deliveries-checklist { max-height: 150px; overflow-y: auto; background: rgba(15, 23, 42, 0.5); padding: 0.5rem; border-radius: 0.375rem; display: flex; flex-direction: column; gap: 0.4rem; }
    .chk-item { display: flex; gap: 0.5rem; align-items: center; font-size: 0.85rem; color: #e2e8f0; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; }
    .text-muted { color: #64748b; }
    .text-xs { font-size: 0.8rem; }
  `]
})
export class DeliveryRoutesComponent implements OnInit {
  routes: DeliveryRoute[] = [];
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];
  availableDeliveries: Delivery[] = [];
  selectedDeliveryIds: string[] = [];

  selectedDate = new Date().toISOString().substring(0, 10);
  showCreateModal = false;

  newRouteDate = new Date().toISOString().substring(0, 10);
  newRouteDriverId = '';
  newRouteVehicleId = '';

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.loadRoutes();
    this.loadResources();
  }

  loadRoutes(): void {
    this.deliveryService.getRoutes(this.selectedDate).subscribe({
      next: (data) => this.routes = data,
      error: (err) => console.error('Failed to load routes', err)
    });
  }

  loadResources(): void {
    this.deliveryService.getDrivers().subscribe(data => this.drivers = data);
    this.deliveryService.getVehicles().subscribe(data => this.vehicles = data);
    this.deliveryService.getDeliveries(this.newRouteDate).subscribe(data => this.availableDeliveries = data);
  }

  toggleDeliverySelection(id: string): void {
    const index = this.selectedDeliveryIds.indexOf(id);
    if (index > -1) {
      this.selectedDeliveryIds.splice(index, 1);
    } else {
      this.selectedDeliveryIds.push(id);
    }
  }

  confirmCreateRoute(): void {
    this.deliveryService.createRoute(
      this.newRouteDate,
      this.newRouteDriverId || undefined,
      this.newRouteVehicleId || undefined,
      this.selectedDeliveryIds.length > 0 ? this.selectedDeliveryIds : undefined
    ).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.loadRoutes();
      },
      error: (err) => alert(err?.error?.error || 'Failed to create route')
    });
  }
}
