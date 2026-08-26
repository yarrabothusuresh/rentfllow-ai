import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReturnsService, ReturnOrder, ReturnOrderItem } from '../../services/returns.service';
import { DeliveryService, Driver, Vehicle } from '../../services/delivery.service';

@Component({
  selector: 'app-return-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="return-detail-container" *ngIf="returnOrder">
      <!-- Breadcrumb & Header -->
      <div class="page-header">
        <div>
          <a routerLink="/returns" class="text-decoration-none text-muted mb-1 d-inline-block">
            <i class="bi bi-arrow-left"></i> Back to Returns
          </a>
          <h2>
            RETURN {{ returnOrder.returnNumber }}
            <span class="badge status-badge ms-2" [ngClass]="'status-' + returnOrder.status">
              {{ formatStatus(returnOrder.status) }}
            </span>
          </h2>
        </div>

        <!-- Workflow Action Toolbar -->
        <div class="header-actions">
          <button *ngIf="returnOrder.status === 'PENDING' || returnOrder.status === 'SCHEDULED' || returnOrder.status === 'ASSIGNED'" 
                  class="btn btn-outline-primary" (click)="openScheduleModal()">
            <i class="bi bi-calendar-event"></i> Schedule & Assign
          </button>

          <!-- Driver Mobile Actions -->
          <button *ngIf="returnOrder.status === 'SCHEDULED' || returnOrder.status === 'ASSIGNED' || returnOrder.status === 'READY_FOR_PICKUP'"
                  class="btn btn-primary btn-lg mobile-action-btn" (click)="startPickup()">
            <i class="bi bi-play-circle"></i> Start Pickup
          </button>

          <button *ngIf="returnOrder.status === 'OUT_FOR_PICKUP'" 
                  class="btn btn-warning btn-lg mobile-action-btn" (click)="arrivePickup()">
            <i class="bi bi-geo-alt-fill"></i> Arrive at Venue
          </button>

          <button *ngIf="returnOrder.status === 'ARRIVED'" 
                  class="btn btn-success btn-lg mobile-action-btn" (click)="confirmPickup()">
            <i class="bi bi-check-circle-fill"></i> Confirm Pickup
          </button>

          <!-- Warehouse Check-In Action -->
          <button *ngIf="returnOrder.status === 'PICKED_UP'" 
                  class="btn btn-warning btn-lg" (click)="startCheckIn()">
            <i class="bi bi-box-arrow-in-down"></i> Start Check-In
          </button>

          <!-- Warehouse Inspection Action -->
          <button *ngIf="returnOrder.status === 'CHECK_IN'" 
                  class="btn btn-info text-white btn-lg" (click)="startInspection()">
            <i class="bi bi-search"></i> Start Inspection
          </button>

          <!-- Complete Return Action -->
          <button *ngIf="returnOrder.status === 'INSPECTION'" 
                  class="btn btn-success btn-lg" (click)="completeReturn()">
            <i class="bi bi-check-lg"></i> Complete Return
          </button>
        </div>
      </div>

      <!-- Info Cards Grid -->
      <div class="row g-3 mb-4">
        <!-- Customer & Event Card -->
        <div class="col-md-4">
          <div class="card info-card h-100">
            <div class="card-body">
              <h5 class="card-title text-muted fs-6"><i class="bi bi-person-circle"></i> Customer & Event</h5>
              <h4 class="fw-bold mb-1">{{ returnOrder.customerName || 'ABC Events LLC' }}</h4>
              <p class="text-primary fw-semibold mb-2"><i class="bi bi-calendar-event"></i> {{ returnOrder.eventName || 'Wedding Reception' }}</p>
              <p class="mb-0 text-muted small"><i class="bi bi-hash"></i> Booking: <strong>{{ returnOrder.bookingNumber || 'BOOK-000123' }}</strong></p>
            </div>
          </div>
        </div>

        <!-- Pickup & Logistics Card -->
        <div class="col-md-4">
          <div class="card info-card h-100">
            <div class="card-body">
              <h5 class="card-title text-muted fs-6"><i class="bi bi-truck"></i> Pickup Schedule & Driver</h5>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <div>
                  <span class="fw-bold">{{ returnOrder.scheduledDate || 'August 31, 2026' }}</span>
                  <div class="text-muted small">{{ returnOrder.scheduledStartTime || '10:00 AM' }} - {{ returnOrder.scheduledEndTime || '12:00 PM' }}</div>
                </div>
                <button class="btn btn-sm btn-outline-secondary" (click)="openScheduleModal()"><i class="bi bi-pencil"></i></button>
              </div>
              <div class="d-flex gap-3 text-muted small">
                <div><i class="bi bi-person"></i> Driver: <strong>{{ returnOrder.driverName || 'Unassigned' }}</strong></div>
                <div><i class="bi bi-truck"></i> Vehicle: <strong>{{ returnOrder.vehicleNumber || 'Unassigned' }}</strong></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Location Card -->
        <div class="col-md-4">
          <div class="card info-card h-100">
            <div class="card-body">
              <h5 class="card-title text-muted fs-6"><i class="bi bi-geo-alt"></i> Pickup Address</h5>
              <p class="card-text fw-medium">{{ returnOrder.pickupAddressSnapshot || '123 Main Street, New York, NY 10001' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Equipment Items Table -->
      <div class="card items-card mb-4">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-boxes"></i> Equipment Items Breakdown</h5>
          <div class="d-flex gap-2">
            <button *ngIf="returnOrder.status === 'CHECK_IN'" class="btn btn-warning btn-sm" (click)="openCheckInModal()">
              <i class="bi bi-pencil-square"></i> Enter Received Quantities
            </button>
            <button *ngIf="returnOrder.status === 'INSPECTION'" class="btn btn-info text-white btn-sm" (click)="openInspectionModal()">
              <i class="bi bi-clipboard-check"></i> Inspect Items
            </button>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Product</th>
                <th class="text-center">Expected</th>
                <th class="text-center">Received</th>
                <th class="text-center">Missing</th>
                <th class="text-center">Damaged</th>
                <th class="text-center">Good</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of returnOrder.items">
                <td>
                  <div class="fw-bold">{{ item.productNameSnapshot }}</div>
                  <small class="text-muted">{{ item.skuSnapshot }}</small>
                </td>
                <td class="text-center fw-bold">{{ item.quantityExpected }}</td>
                <td class="text-center">
                  <span class="badge bg-light text-dark fs-6">{{ item.quantityReceived }}</span>
                </td>
                <td class="text-center">
                  <span class="badge" [ngClass]="item.quantityMissing > 0 ? 'bg-danger fs-6' : 'bg-light text-muted'">
                    {{ item.quantityMissing }}
                  </span>
                </td>
                <td class="text-center">
                  <span class="badge" [ngClass]="item.quantityDamaged > 0 ? 'bg-warning text-dark fs-6' : 'bg-light text-muted'">
                    {{ item.quantityDamaged }}
                  </span>
                </td>
                <td class="text-center">
                  <span class="badge bg-success fs-6">{{ item.quantityGood }}</span>
                </td>
                <td>
                  <span class="badge" [ngClass]="getItemStatusClass(item.status)">
                    {{ item.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Schedule Modal -->
      <div class="modal fade show d-block" tabindex="-1" *ngIf="showScheduleModal" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Schedule Pickup & Assignment</h5>
              <button type="button" class="btn-close" (click)="showScheduleModal = false"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Pickup Date</label>
                <input type="date" class="form-control" [(ngModel)]="schedDate">
              </div>
              <div class="row g-2 mb-3">
                <div class="col-6">
                  <label class="form-label">Start Time</label>
                  <input type="time" class="form-control" [(ngModel)]="schedStart">
                </div>
                <div class="col-6">
                  <label class="form-label">End Time</label>
                  <input type="time" class="form-control" [(ngModel)]="schedEnd">
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Driver</label>
                <select class="form-select" [(ngModel)]="selectedDriverId">
                  <option value="">Select Driver...</option>
                  <option *ngFor="let d of drivers" [value]="d.id">{{ d.name }} ({{ d.status }})</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label">Vehicle</label>
                <select class="form-select" [(ngModel)]="selectedVehicleId">
                  <option value="">Select Vehicle...</option>
                  <option *ngFor="let v of vehicles" [value]="v.id">{{ v.vehicleNumber }} - {{ v.name }}</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showScheduleModal = false">Cancel</button>
              <button type="button" class="btn btn-primary" (click)="saveScheduleAndAssignment()">Save Schedule</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Warehouse Check-In Modal (Step 42 Fast Touch Controls) -->
      <div class="modal fade show d-block" tabindex="-1" *ngIf="showCheckInModal" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header bg-warning text-dark">
              <h5 class="modal-title fw-bold"><i class="bi bi-box-arrow-in-down"></i> Warehouse Check-In: {{ returnOrder.returnNumber }}</h5>
              <button type="button" class="btn-close" (click)="showCheckInModal = false"></button>
            </div>
            <div class="modal-body">
              <p class="text-muted">Enter actual quantity received for each product. Missing items will be calculated automatically.</p>
              <div class="list-group">
                <div *ngFor="let item of checkInItems" class="list-group-item d-flex justify-content-between align-items-center py-3">
                  <div>
                    <h6 class="mb-0 fw-bold">{{ item.productNameSnapshot }}</h6>
                    <small class="text-muted">Expected: <strong>{{ item.quantityExpected }}</strong></small>
                  </div>
                  <div class="d-flex align-items-center gap-2">
                    <button class="btn btn-outline-secondary btn-lg" (click)="decrementCheckIn(item)">-</button>
                    <input type="number" class="form-control text-center fw-bold fs-5" style="width: 90px" [(ngModel)]="item.tempReceived" [max]="item.quantityExpected" min="0">
                    <button class="btn btn-outline-secondary btn-lg" (click)="incrementCheckIn(item)">+</button>
                  </div>
                </div>
              </div>
              <div *ngIf="errorMessage" class="alert alert-danger mt-3 mb-0">{{ errorMessage }}</div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showCheckInModal = false">Cancel</button>
              <button type="button" class="btn btn-warning" (click)="submitCheckIn()">Save Received Quantities</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Warehouse Inspection Modal (Step 43 Item-by-Item) -->
      <div class="modal fade show d-block" tabindex="-1" *ngIf="showInspectionModal" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header bg-info text-white">
              <h5 class="modal-title fw-bold"><i class="bi bi-clipboard-check"></i> Equipment Inspection: {{ returnOrder.returnNumber }}</h5>
              <button type="button" class="btn-close btn-close-white" (click)="showInspectionModal = false"></button>
            </div>
            <div class="modal-body" *ngIf="currentInspectionItem">
              <div class="item-summary mb-3 p-3 bg-light rounded">
                <h5 class="fw-bold mb-1">{{ currentInspectionItem.productNameSnapshot }}</h5>
                <span class="badge bg-secondary me-2">Expected: {{ currentInspectionItem.quantityExpected }}</span>
                <span class="badge bg-dark me-2">Received: {{ currentInspectionItem.quantityReceived }}</span>
                <span class="badge bg-danger" *ngIf="currentInspectionItem.quantityMissing > 0">Missing: {{ currentInspectionItem.quantityMissing }}</span>
              </div>

              <div class="row g-3">
                <div class="col-6">
                  <label class="form-label fw-bold">Good Quantity</label>
                  <input type="number" class="form-control form-control-lg" [(ngModel)]="currentInspectionItem.tempGood" (ngModelChange)="validateInspectionNumbers(currentInspectionItem)">
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold text-danger">Damaged Quantity</label>
                  <input type="number" class="form-control form-control-lg" [(ngModel)]="currentInspectionItem.tempDamaged" (ngModelChange)="validateInspectionNumbers(currentInspectionItem)">
                </div>
              </div>

              <div class="mt-3" *ngIf="currentInspectionItem.tempDamaged > 0">
                <h6 class="fw-bold text-danger border-bottom pb-2">Damage Details</h6>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Condition</label>
                    <select class="form-select" [(ngModel)]="currentInspectionItem.tempCondition">
                      <option value="GOOD">GOOD</option>
                      <option value="MINOR_DAMAGE">MINOR DAMAGE</option>
                      <option value="MAJOR_DAMAGE">MAJOR DAMAGE</option>
                      <option value="UNUSABLE">UNUSABLE</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Damage Category</label>
                    <select class="form-select" [(ngModel)]="currentInspectionItem.tempCategory">
                      <option value="BROKEN">BROKEN</option>
                      <option value="STAINED">STAINED</option>
                      <option value="SCRATCHED">SCRATCHED</option>
                      <option value="MISSING_PART">MISSING PART</option>
                      <option value="WATER_DAMAGE">WATER DAMAGE</option>
                      <option value="OTHER">OTHER</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Severity</label>
                    <select class="form-select" [(ngModel)]="currentInspectionItem.tempSeverity">
                      <option value="MINOR">MINOR</option>
                      <option value="MAJOR">MAJOR</option>
                      <option value="CRITICAL">CRITICAL</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Est. Repair Cost ($)</label>
                    <input type="number" class="form-control" [(ngModel)]="currentInspectionItem.tempRepairCost">
                  </div>
                  <div class="col-12">
                    <label class="form-label">Damage Description & Notes</label>
                    <textarea class="form-control" rows="2" [(ngModel)]="currentInspectionItem.tempNotes" placeholder="Describe damage..."></textarea>
                  </div>
                </div>
              </div>

              <div *ngIf="errorMessage" class="alert alert-danger mt-3 mb-0">{{ errorMessage }}</div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showInspectionModal = false">Cancel</button>
              <button type="button" class="btn btn-info text-white" (click)="submitInspection()">Save Inspection Results</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .return-detail-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
    .mobile-action-btn {
      padding: 0.75rem 1.5rem;
      font-weight: 700;
      border-radius: 10px;
    }
    .info-card {
      border-radius: 12px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
      border: 1px solid #e2e8f0;
    }
    .items-card {
      border-radius: 12px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
      border: 1px solid #e2e8f0;
      overflow: hidden;
    }
    .status-badge {
      padding: 0.4rem 0.8rem;
      border-radius: 20px;
      font-size: 0.85rem;
    }
    .status-PENDING { background-color: #f1f5f9; color: #475569; }
    .status-SCHEDULED { background-color: #e0f2fe; color: #0369a1; }
    .status-ASSIGNED { background-color: #dbeafe; color: #1e40af; }
    .status-READY_FOR_PICKUP { background-color: #fef3c7; color: #b45309; }
    .status-OUT_FOR_PICKUP { background-color: #f3e8ff; color: #6b21a8; }
    .status-ARRIVED { background-color: #fae8ff; color: #86198f; }
    .status-PICKED_UP { background-color: #ccfbf1; color: #0f766e; }
    .status-CHECK_IN { background-color: #ffedd5; color: #c2410c; }
    .status-INSPECTION { background-color: #fed7aa; color: #9a3412; }
    .status-COMPLETED { background-color: #dcfce7; color: #15803d; }
  `]
})
export class ReturnDetailComponent implements OnInit {
  returnOrder: ReturnOrder | null = null;
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];

  showScheduleModal = false;
  schedDate = '';
  schedStart = '10:00';
  schedEnd = '12:00';
  selectedDriverId = '';
  selectedVehicleId = '';

  showCheckInModal = false;
  checkInItems: any[] = [];

  showInspectionModal = false;
  currentInspectionItem: any = null;

  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private returnsService: ReturnsService,
    private deliveryService: DeliveryService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadReturn(id);
    }
    this.deliveryService.getDrivers().subscribe(d => this.drivers = d);
    this.deliveryService.getVehicles().subscribe(v => this.vehicles = v);
  }

  loadReturn(id: string): void {
    this.returnsService.getReturnById(id).subscribe(data => {
      this.returnOrder = data;
      this.schedDate = data.scheduledDate || new Date().toISOString().split('T')[0];
      this.schedStart = data.scheduledStartTime || '10:00';
      this.schedEnd = data.scheduledEndTime || '12:00';
      this.selectedDriverId = data.driverId || '';
      this.selectedVehicleId = data.vehicleId || '';
    });
  }

  openScheduleModal(): void {
    this.showScheduleModal = true;
  }

  saveScheduleAndAssignment(): void {
    if (!this.returnOrder) return;
    this.returnsService.scheduleReturn(this.returnOrder.id, this.schedDate, this.schedStart, this.schedEnd).subscribe(() => {
      if (this.selectedDriverId) {
        this.returnsService.assignDriver(this.returnOrder!.id, this.selectedDriverId).subscribe();
      }
      if (this.selectedVehicleId) {
        this.returnsService.assignVehicle(this.returnOrder!.id, this.selectedVehicleId).subscribe();
      }
      this.showScheduleModal = false;
      this.loadReturn(this.returnOrder!.id);
    });
  }

  startPickup(): void {
    if (!this.returnOrder) return;
    this.returnsService.startPickup(this.returnOrder.id).subscribe(() => this.loadReturn(this.returnOrder!.id));
  }

  arrivePickup(): void {
    if (!this.returnOrder) return;
    this.returnsService.arrivePickup(this.returnOrder.id).subscribe(() => this.loadReturn(this.returnOrder!.id));
  }

  confirmPickup(): void {
    if (!this.returnOrder) return;
    this.returnsService.pickupComplete(this.returnOrder.id).subscribe(() => this.loadReturn(this.returnOrder!.id));
  }

  startCheckIn(): void {
    if (!this.returnOrder) return;
    this.returnsService.startCheckIn(this.returnOrder.id).subscribe(() => {
      this.loadReturn(this.returnOrder!.id);
      this.openCheckInModal();
    });
  }

  openCheckInModal(): void {
    if (!this.returnOrder || !this.returnOrder.items) return;
    this.errorMessage = '';
    this.checkInItems = this.returnOrder.items.map(i => ({
      ...i,
      tempReceived: i.quantityReceived || i.quantityExpected
    }));
    this.showCheckInModal = true;
  }

  incrementCheckIn(item: any): void {
    if (item.tempReceived < item.quantityExpected) item.tempReceived++;
  }

  decrementCheckIn(item: any): void {
    if (item.tempReceived > 0) item.tempReceived--;
  }

  submitCheckIn(): void {
    if (!this.returnOrder) return;
    const payload = this.checkInItems.map(i => ({
      returnItemId: i.id,
      quantityReceived: i.tempReceived
    }));

    this.returnsService.recordCheckIn(this.returnOrder.id, payload).subscribe({
      next: () => {
        this.showCheckInModal = false;
        this.loadReturn(this.returnOrder!.id);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Check-in validation failed.';
      }
    });
  }

  startInspection(): void {
    if (!this.returnOrder) return;
    this.returnsService.startInspection(this.returnOrder.id).subscribe(() => {
      this.loadReturn(this.returnOrder!.id);
      this.openInspectionModal();
    });
  }

  openInspectionModal(): void {
    if (!this.returnOrder || !this.returnOrder.items || this.returnOrder.items.length === 0) return;
    this.errorMessage = '';
    const item = this.returnOrder.items[0];
    this.currentInspectionItem = {
      ...item,
      tempGood: item.quantityGood || item.quantityReceived,
      tempDamaged: item.quantityDamaged || 0,
      tempCondition: 'GOOD',
      tempCategory: 'SCRATCHED',
      tempSeverity: 'MINOR',
      tempRepairCost: 0,
      tempNotes: ''
    };
    this.showInspectionModal = true;
  }

  validateInspectionNumbers(item: any): void {
    if (item.tempGood + item.tempDamaged !== item.quantityReceived) {
      this.errorMessage = 'Good and damaged quantities must equal received quantity (' + item.quantityReceived + ').';
    } else {
      this.errorMessage = '';
    }
  }

  submitInspection(): void {
    if (!this.returnOrder || !this.currentInspectionItem) return;
    const item = this.currentInspectionItem;

    if (item.tempGood + item.tempDamaged !== item.quantityReceived) {
      this.errorMessage = 'Good and damaged quantities must equal received quantity.';
      return;
    }

    const payload = [{
      returnItemId: item.id,
      goodQuantity: item.tempGood,
      damagedQuantity: item.tempDamaged,
      condition: item.tempCondition,
      damageCategory: item.tempCategory,
      damageSeverity: item.tempSeverity,
      estimatedRepairCost: item.tempRepairCost,
      notes: item.tempNotes
    }];

    this.returnsService.recordInspection(this.returnOrder.id, payload).subscribe({
      next: () => {
        this.showInspectionModal = false;
        this.loadReturn(this.returnOrder!.id);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Inspection recording failed.';
      }
    });
  }

  completeReturn(): void {
    if (!this.returnOrder) return;
    this.returnsService.completeReturn(this.returnOrder.id).subscribe(() => {
      this.loadReturn(this.returnOrder!.id);
    });
  }

  formatStatus(status: string): string {
    return status ? status.replace(/_/g, ' ') : '';
  }

  getItemStatusClass(status: string): string {
    if (status === 'MISSING') return 'bg-danger';
    if (status === 'DAMAGED') return 'bg-warning text-dark';
    if (status === 'INSPECTED') return 'bg-success';
    return 'bg-secondary';
  }
}
