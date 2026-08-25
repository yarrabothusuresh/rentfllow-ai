import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DeliveryService, Driver, Vehicle } from '../../services/delivery.service';

@Component({
  selector: 'app-driver-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1 class="page-title">Drivers & Vehicle Fleet</h1>
          <p class="page-subtitle">Manage registered delivery drivers, license credentials, and fleet transport vehicles.</p>
        </div>
        <div class="action-buttons">
          <button class="btn btn-primary" (click)="showDriverModal = true">
            ➕ Add Driver
          </button>
          <button class="btn btn-secondary" (click)="showVehicleModal = true">
            ➕ Add Vehicle
          </button>
        </div>
      </div>

      <div class="fleet-grid">
        <!-- Drivers Section -->
        <div class="section-card glass-card">
          <h2>Registered Drivers</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Phone</th>
                <th>License #</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let drv of drivers">
                <td class="font-bold">👤 {{ drv.name }}</td>
                <td>{{ drv.phone || 'N/A' }}</td>
                <td class="font-mono">{{ drv.licenseNumber || 'N/A' }}</td>
                <td>
                  <span class="status-badge" [class]="drv.status.toLowerCase()">{{ drv.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Vehicles Section -->
        <div class="section-card glass-card">
          <h2>Fleet Vehicles</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>Vehicle #</th>
                <th>Name / Model</th>
                <th>Type</th>
                <th>Capacity</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let veh of vehicles">
                <td class="font-bold font-mono">🚛 {{ veh.vehicleNumber }}</td>
                <td>{{ veh.name }}</td>
                <td>{{ veh.type || 'Standard' }}</td>
                <td>{{ veh.capacity ? veh.capacity + ' lbs' : 'N/A' }}</td>
                <td>
                  <span class="status-badge" [class]="veh.status.toLowerCase()">{{ veh.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Add Driver Modal -->
      <div class="modal-overlay" *ngIf="showDriverModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Add New Driver</h3>
            <button class="close-btn" (click)="showDriverModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Full Name *</label>
              <input type="text" [(ngModel)]="newDriverName" placeholder="e.g. John Doe" />
            </div>
            <div class="form-group mt-3">
              <label>Phone Number</label>
              <input type="text" [(ngModel)]="newDriverPhone" placeholder="555-0199" />
            </div>
            <div class="form-group mt-3">
              <label>Driver License #</label>
              <input type="text" [(ngModel)]="newDriverLicense" placeholder="DL-998877" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showDriverModal = false">Cancel</button>
            <button class="btn btn-primary" (click)="createDriver()">Save Driver</button>
          </div>
        </div>
      </div>

      <!-- Add Vehicle Modal -->
      <div class="modal-overlay" *ngIf="showVehicleModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Add New Vehicle</h3>
            <button class="close-btn" (click)="showVehicleModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Vehicle Number / Tag *</label>
              <input type="text" [(ngModel)]="newVehicleNumber" placeholder="VAN-03" />
            </div>
            <div class="form-group mt-3">
              <label>Name / Model *</label>
              <input type="text" [(ngModel)]="newVehicleName" placeholder="Ford Transit 250" />
            </div>
            <div class="form-group mt-3">
              <label>Type</label>
              <input type="text" [(ngModel)]="newVehicleType" placeholder="Cargo Van / Box Truck" />
            </div>
            <div class="form-group mt-3">
              <label>Payload Capacity (lbs)</label>
              <input type="number" [(ngModel)]="newVehicleCapacity" placeholder="3500" />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showVehicleModal = false">Cancel</button>
            <button class="btn btn-primary" (click)="createVehicle()">Save Vehicle</button>
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
    .fleet-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .section-card h2 { font-size: 1.1rem; color: #f1f5f9; margin-top: 0; margin-bottom: 1rem; border-bottom: 1px solid rgba(255, 255, 255, 0.08); padding-bottom: 0.5rem; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.85rem; }
    .data-table th { padding: 0.6rem 0.75rem; background: rgba(15, 23, 42, 0.6); color: #94a3b8; font-weight: 600; text-transform: uppercase; }
    .data-table td { padding: 0.65rem 0.75rem; border-top: 1px solid rgba(255, 255, 255, 0.05); color: #e2e8f0; }
    .status-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; }
    .status-badge.available { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-badge.assigned, .status-badge.in_use { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-badge.on_delivery { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
    .status-badge.off_duty, .status-badge.maintenance { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }
    .btn { padding: 0.5rem 1rem; border-radius: 0.375rem; font-weight: 600; border: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.4rem; }
    .btn-primary { background: #0284c7; color: white; }
    .btn-secondary { background: rgba(51, 65, 85, 0.8); color: #f1f5f9; }
    .font-bold { font-weight: 600; }
    .font-mono { font-family: monospace; }
    .mt-3 { margin-top: 0.75rem; }
    .modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { width: 100%; max-width: 450px; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h3 { font-size: 1.1rem; color: #f8fafc; margin: 0; }
    .close-btn { background: transparent; border: none; color: #94a3b8; font-size: 1.5rem; cursor: pointer; }
    .modal-body { margin-bottom: 1.5rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .form-group label { font-size: 0.85rem; color: #cbd5e1; }
    .form-group input { background: rgba(15, 23, 42, 0.9); border: 1px solid rgba(255, 255, 255, 0.2); color: #f8fafc; padding: 0.55rem; border-radius: 0.375rem; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; }
  `]
})
export class DriverListComponent implements OnInit {
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];

  showDriverModal = false;
  newDriverName = '';
  newDriverPhone = '';
  newDriverLicense = '';

  showVehicleModal = false;
  newVehicleNumber = '';
  newVehicleName = '';
  newVehicleType = '';
  newVehicleCapacity?: number;

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.loadFleet();
  }

  loadFleet(): void {
    this.deliveryService.getDrivers().subscribe(data => this.drivers = data);
    this.deliveryService.getVehicles().subscribe(data => this.vehicles = data);
  }

  createDriver(): void {
    if (!this.newDriverName) return;
    this.deliveryService.createDriver(this.newDriverName, this.newDriverPhone, this.newDriverLicense).subscribe(() => {
      this.showDriverModal = false;
      this.newDriverName = '';
      this.newDriverPhone = '';
      this.newDriverLicense = '';
      this.loadFleet();
    });
  }

  createVehicle(): void {
    if (!this.newVehicleNumber || !this.newVehicleName) return;
    this.deliveryService.createVehicle(this.newVehicleNumber, this.newVehicleName, this.newVehicleType, this.newVehicleCapacity).subscribe(() => {
      this.showVehicleModal = false;
      this.newVehicleNumber = '';
      this.newVehicleName = '';
      this.newVehicleType = '';
      this.newVehicleCapacity = undefined;
      this.loadFleet();
    });
  }
}
