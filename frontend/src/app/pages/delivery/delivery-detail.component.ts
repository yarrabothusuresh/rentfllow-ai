import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DeliveryService, Delivery, Driver, Vehicle } from '../../services/delivery.service';

@Component({
  selector: 'app-delivery-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-container" *ngIf="delivery">
      <div class="page-header">
        <div class="header-main">
          <a routerLink="/dashboard/delivery/list" class="back-link">← Back to Deliveries</a>
          <h1 class="page-title">Delivery {{ delivery.deliveryNumber }}</h1>
          <div class="status-header">
            <span class="status-badge" [class]="delivery.status.toLowerCase()">{{ delivery.customerFacingStatus || delivery.status }}</span>
            <span class="priority-badge" [class]="delivery.priority.toLowerCase()">Priority: {{ delivery.priority }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <button *ngIf="delivery.status === 'ASSIGNED' || delivery.status === 'READY'" class="btn btn-primary" (click)="startDelivery()">
            🚀 Dispatch / Out For Delivery
          </button>
          <button *ngIf="delivery.status === 'OUT_FOR_DELIVERY'" class="btn btn-warning" (click)="arriveDelivery()">
            📍 Arrived at Location
          </button>
          <button *ngIf="delivery.status === 'ARRIVED' && delivery.setupRequired" class="btn btn-info" (click)="startSetup()">
            🛠️ Start Setup
          </button>
          <button *ngIf="delivery.status === 'ARRIVED' || delivery.status === 'SETUP_IN_PROGRESS'" class="btn btn-success" (click)="completeDelivery()">
            ✅ Complete Delivery
          </button>
          <button *ngIf="delivery.status !== 'DELIVERED' && delivery.status !== 'CANCELLED' && delivery.status !== 'FAILED'" class="btn btn-danger" (click)="showFailModal = true">
            ❌ Report Failure
          </button>
        </div>
      </div>

      <!-- Main Overview Grid -->
      <div class="detail-grid">
        <!-- Left Panel: Delivery Details & Schedule -->
        <div class="left-col">
          <div class="section-card glass-card">
            <h2>Delivery & Location Information</h2>
            <div class="info-rows">
              <div class="info-row">
                <span class="label">Customer</span>
                <span class="val font-bold">{{ delivery.customerName || 'N/A' }}</span>
              </div>
              <div class="info-row">
                <span class="label">Destination Address</span>
                <span class="val">{{ delivery.deliveryAddressSnapshot || 'No address specified' }}</span>
              </div>
              <div class="info-row" *ngIf="delivery.latitude && delivery.longitude">
                <span class="label">GPS Coordinates</span>
                <span class="val font-mono">{{ delivery.latitude }}, {{ delivery.longitude }}</span>
              </div>
              <div class="info-row">
                <span class="label">Event / Booking</span>
                <span class="val">
                  <span *ngIf="delivery.eventName">Event: {{ delivery.eventName }}</span>
                  <span *ngIf="delivery.bookingNumber"> | Booking: {{ delivery.bookingNumber }}</span>
                </span>
              </div>
              <div class="info-row">
                <span class="label">Scheduled Window</span>
                <span class="val">{{ delivery.scheduledDate || 'Unscheduled' }} {{ delivery.scheduledStartTime ? '(' + delivery.scheduledStartTime + ' - ' + delivery.scheduledEndTime + ')' : '' }}</span>
              </div>
              <div class="info-row">
                <span class="label">Setup Requirement</span>
                <span class="val">
                  <span *ngIf="delivery.setupRequired">Yes ({{ delivery.setupDurationMinutes || 30 }} mins estimated)</span>
                  <span *ngIf="!delivery.setupRequired">No setup required</span>
                </span>
              </div>
            </div>
          </div>

          <!-- Notes Section -->
          <div class="section-card glass-card mt-4">
            <h2>Instructions & Notes</h2>
            <div class="notes-block">
              <p><strong>Dispatcher Notes:</strong> {{ delivery.notes || 'None' }}</p>
              <p><strong>Customer Special Notes:</strong> {{ delivery.customerNotes || 'None' }}</p>
            </div>
          </div>
        </div>

        <!-- Right Panel: Fleet Assignment & Execution Timeline -->
        <div class="right-col">
          <!-- Fleet Assignment Card -->
          <div class="section-card glass-card">
            <h2>Driver & Vehicle Assignment</h2>
            <div class="assignment-block">
              <div class="assign-item">
                <span class="label">Assigned Driver:</span>
                <div class="assign-val">
                  <span *ngIf="delivery.driverName">👤 {{ delivery.driverName }}</span>
                  <span *ngIf="!delivery.driverName" class="text-muted">Unassigned</span>
                  <button class="btn-xs btn-outline ml-2" (click)="showDriverModal = true">Change</button>
                </div>
              </div>

              <div class="assign-item mt-3">
                <span class="label">Assigned Vehicle:</span>
                <div class="assign-val">
                  <span *ngIf="delivery.vehicleNumber">🚛 {{ delivery.vehicleNumber }}</span>
                  <span *ngIf="!delivery.vehicleNumber" class="text-muted">Unassigned</span>
                  <button class="btn-xs btn-outline ml-2" (click)="showVehicleModal = true">Change</button>
                </div>
              </div>
            </div>
          </div>

          <!-- Execution Timeline -->
          <div class="section-card glass-card mt-4">
            <h2>Delivery Timeline</h2>
            <div class="timeline">
              <div class="timeline-item" [class.active]="delivery.createdAt">
                <div class="t-icon">📝</div>
                <div class="t-content">
                  <span class="t-title">Created</span>
                  <span class="t-time">{{ delivery.createdAt | date:'short' }}</span>
                </div>
              </div>
              <div class="timeline-item" [class.active]="delivery.actualStartTime">
                <div class="t-icon">🚚</div>
                <div class="t-content">
                  <span class="t-title">Dispatched / Out for Delivery</span>
                  <span class="t-time">{{ delivery.actualStartTime | date:'short' }}</span>
                </div>
              </div>
              <div class="timeline-item" [class.active]="delivery.actualArrivalTime">
                <div class="t-icon">📍</div>
                <div class="t-content">
                  <span class="t-title">Arrived at Venue</span>
                  <span class="t-time">{{ delivery.actualArrivalTime | date:'short' }}</span>
                </div>
              </div>
              <div class="timeline-item" [class.active]="delivery.actualCompletionTime">
                <div class="t-icon">✅</div>
                <div class="t-content">
                  <span class="t-title">Completed</span>
                  <span class="t-time">{{ delivery.actualCompletionTime | date:'short' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Report Failure Modal -->
      <div class="modal-overlay" *ngIf="showFailModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Report Delivery Failure</h3>
            <button class="close-btn" (click)="showFailModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Failure Reason</label>
              <select [(ngModel)]="failureReason">
                <option value="CUSTOMER_NOT_AVAILABLE">Customer Not Available</option>
                <option value="ACCESS_DENIED">Access Denied / Gated</option>
                <option value="WEATHER">Weather Delay</option>
                <option value="VEHICLE_BREAKDOWN">Vehicle Breakdown</option>
                <option value="WRONG_ADDRESS">Wrong Address</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div class="form-group mt-3">
              <label>Additional Notes</label>
              <textarea rows="3" [(ngModel)]="failureNotes" placeholder="Describe issue..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showFailModal = false">Cancel</button>
            <button class="btn btn-danger" (click)="confirmFailDelivery()">Confirm Failure</button>
          </div>
        </div>
      </div>

      <!-- Change Driver Modal -->
      <div class="modal-overlay" *ngIf="showDriverModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Assign Driver</h3>
            <button class="close-btn" (click)="showDriverModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Driver</label>
              <select [(ngModel)]="selectedDriverId">
                <option *ngFor="let drv of drivers" [value]="drv.id">{{ drv.name }} ({{ drv.status }})</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showDriverModal = false">Cancel</button>
            <button class="btn btn-primary" (click)="confirmAssignDriver()">Save Driver</button>
          </div>
        </div>
      </div>

      <!-- Change Vehicle Modal -->
      <div class="modal-overlay" *ngIf="showVehicleModal">
        <div class="modal-card glass-card">
          <div class="modal-header">
            <h3>Assign Vehicle</h3>
            <button class="close-btn" (click)="showVehicleModal = false">&times;</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Vehicle</label>
              <select [(ngModel)]="selectedVehicleId">
                <option *ngFor="let veh of vehicles" [value]="veh.id">{{ veh.vehicleNumber }} - {{ veh.name }}</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="showVehicleModal = false">Cancel</button>
            <button class="btn btn-primary" (click)="confirmAssignVehicle()">Save Vehicle</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { padding: 1.5rem; }
    .back-link { color: #38bdf8; text-decoration: none; font-size: 0.85rem; margin-bottom: 0.5rem; display: inline-block; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .page-title { font-size: 1.75rem; font-weight: 700; color: #f8fafc; margin: 0; }
    .status-header { display: flex; gap: 0.5rem; margin-top: 0.4rem; }
    .action-buttons { display: flex; gap: 0.5rem; flex-wrap: wrap; }
    .detail-grid { display: grid; grid-template-columns: 3fr 2fr; gap: 1.5rem; }
    .glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 0.75rem; padding: 1.25rem; }
    .glass-card h2 { font-size: 1.1rem; font-weight: 600; color: #f1f5f9; margin-top: 0; margin-bottom: 1rem; border-bottom: 1px solid rgba(255, 255, 255, 0.08); padding-bottom: 0.5rem; }
    .info-rows { display: flex; flex-direction: column; gap: 0.75rem; }
    .info-row { display: flex; justify-content: space-between; font-size: 0.9rem; border-bottom: 1px dashed rgba(255, 255, 255, 0.05); padding-bottom: 0.4rem; }
    .info-row .label { color: #94a3b8; }
    .info-row .val { color: #f1f5f9; }
    .notes-block { background: rgba(15, 23, 42, 0.5); padding: 0.85rem; border-radius: 0.375rem; color: #cbd5e1; font-size: 0.9rem; display: flex; flex-direction: column; gap: 0.5rem; }
    .assignment-block { display: flex; flex-direction: column; gap: 0.75rem; }
    .assign-item { display: flex; justify-content: space-between; align-items: center; }
    .assign-item .label { color: #94a3b8; font-size: 0.9rem; }
    .assign-val { display: flex; align-items: center; font-size: 0.9rem; color: #f8fafc; font-weight: 600; }
    .timeline { display: flex; flex-direction: column; gap: 1rem; position: relative; padding-left: 1rem; border-left: 2px solid rgba(255, 255, 255, 0.1); }
    .timeline-item { display: flex; gap: 0.75rem; opacity: 0.5; }
    .timeline-item.active { opacity: 1; }
    .t-icon { font-size: 1.1rem; }
    .t-content { display: flex; flex-direction: column; }
    .t-title { font-size: 0.85rem; font-weight: 600; color: #f8fafc; }
    .t-time { font-size: 0.75rem; color: #64748b; }
    .status-badge { padding: 0.25rem 0.6rem; border-radius: 0.375rem; font-size: 0.8rem; font-weight: 600; }
    .status-badge.pending { background: rgba(148, 163, 184, 0.2); color: #cbd5e1; }
    .status-badge.scheduled { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .status-badge.assigned { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .status-badge.ready { background: rgba(236, 72, 153, 0.2); color: #f472b6; }
    .status-badge.out_for_delivery { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
    .status-badge.arrived, .status-badge.setup_in_progress { background: rgba(234, 179, 8, 0.2); color: #facc15; }
    .status-badge.delivered { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .status-badge.failed { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .priority-badge { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .priority-badge.high, .priority-badge.urgent { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .priority-badge.normal { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .priority-badge.low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }
    .btn { padding: 0.5rem 1rem; border-radius: 0.375rem; font-weight: 600; border: none; cursor: pointer; font-size: 0.85rem; }
    .btn-primary { background: #0284c7; color: white; }
    .btn-secondary { background: rgba(51, 65, 85, 0.8); color: #f1f5f9; }
    .btn-warning { background: #d97706; color: white; }
    .btn-info { background: #0891b2; color: white; }
    .btn-success { background: #16a34a; color: white; }
    .btn-danger { background: #dc2626; color: white; }
    .btn-xs { padding: 0.15rem 0.4rem; font-size: 0.75rem; border-radius: 0.2rem; cursor: pointer; }
    .btn-outline { background: transparent; border: 1px solid rgba(255, 255, 255, 0.2); color: #cbd5e1; }
    .mt-3 { margin-top: 0.75rem; }
    .mt-4 { margin-top: 1rem; }
    .ml-2 { margin-left: 0.5rem; }
    .font-bold { font-weight: 600; }
    .font-mono { font-family: monospace; }
    .text-muted { color: #64748b; }
    .modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-card { width: 100%; max-width: 450px; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .modal-header h3 { font-size: 1.1rem; color: #f8fafc; margin: 0; }
    .close-btn { background: transparent; border: none; color: #94a3b8; font-size: 1.5rem; cursor: pointer; }
    .modal-body { margin-bottom: 1.5rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.5rem; }
    .form-group label { font-size: 0.85rem; color: #cbd5e1; }
    .form-group select, .form-group textarea { background: rgba(15, 23, 42, 0.9); border: 1px solid rgba(255, 255, 255, 0.2); color: #f8fafc; padding: 0.6rem; border-radius: 0.375rem; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; }
  `]
})
export class DeliveryDetailComponent implements OnInit {
  delivery?: Delivery;
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];

  showFailModal = false;
  failureReason = 'CUSTOMER_NOT_AVAILABLE';
  failureNotes = '';

  showDriverModal = false;
  selectedDriverId = '';

  showVehicleModal = false;
  selectedVehicleId = '';

  constructor(
    private route: ActivatedRoute,
    private deliveryService: DeliveryService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadDelivery(id);
      this.loadResources();
    }
  }

  loadDelivery(id: string): void {
    this.deliveryService.getDeliveryById(id).subscribe({
      next: (d) => {
        this.delivery = d;
        this.selectedDriverId = d.driverId || '';
        this.selectedVehicleId = d.vehicleId || '';
      },
      error: (err) => console.error('Error fetching delivery', err)
    });
  }

  loadResources(): void {
    this.deliveryService.getDrivers().subscribe(data => this.drivers = data);
    this.deliveryService.getVehicles().subscribe(data => this.vehicles = data);
  }

  startDelivery(): void {
    if (!this.delivery) return;
    this.deliveryService.startDelivery(this.delivery.id).subscribe(() => this.loadDelivery(this.delivery!.id));
  }

  arriveDelivery(): void {
    if (!this.delivery) return;
    this.deliveryService.arriveDelivery(this.delivery.id).subscribe(() => this.loadDelivery(this.delivery!.id));
  }

  startSetup(): void {
    if (!this.delivery) return;
    this.deliveryService.startSetup(this.delivery.id).subscribe(() => this.loadDelivery(this.delivery!.id));
  }

  completeDelivery(): void {
    if (!this.delivery) return;
    this.deliveryService.completeDelivery(this.delivery.id).subscribe(() => this.loadDelivery(this.delivery!.id));
  }

  confirmFailDelivery(): void {
    if (!this.delivery) return;
    this.deliveryService.failDelivery(this.delivery.id, this.failureReason, this.failureNotes).subscribe(() => {
      this.showFailModal = false;
      this.loadDelivery(this.delivery!.id);
    });
  }

  confirmAssignDriver(): void {
    if (!this.delivery || !this.selectedDriverId) return;
    this.deliveryService.assignDriver(this.delivery.id, this.selectedDriverId).subscribe(() => {
      this.showDriverModal = false;
      this.loadDelivery(this.delivery!.id);
    });
  }

  confirmAssignVehicle(): void {
    if (!this.delivery || !this.selectedVehicleId) return;
    this.deliveryService.assignVehicle(this.delivery.id, this.selectedVehicleId).subscribe(() => {
      this.showVehicleModal = false;
      this.loadDelivery(this.delivery!.id);
    });
  }
}
