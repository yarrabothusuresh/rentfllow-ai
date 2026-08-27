import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DamageClaimsService, RepairOrder } from '../../services/damage-claims.service';

@Component({
  selector: 'app-repair-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="repair-detail-container" *ngIf="repair">
      <div class="page-header mb-4">
        <div>
          <a routerLink="/maintenance" class="text-decoration-none text-muted mb-1 d-inline-block">
            <i class="bi bi-arrow-left"></i> Back to Maintenance Center
          </a>
          <h2>
            REPAIR ORDER {{ repair.repairNumber }}
            <span class="badge status-badge ms-2" [ngClass]="'status-' + repair.status">
              {{ repair.status.replace('_', ' ') }}
            </span>
          </h2>
        </div>

        <div class="header-actions">
          <button *ngIf="repair.status === 'PENDING' || repair.status === 'ASSIGNED'" 
                  class="btn btn-purple text-white me-2" (click)="startRepair()">
            <i class="bi bi-play-circle"></i> Start Repair Work
          </button>

          <button *ngIf="repair.status === 'IN_PROGRESS'" 
                  class="btn btn-success me-2" (click)="openCompleteModal()">
            <i class="bi bi-check-circle"></i> Complete Repair & Verify Condition
          </button>

          <button *ngIf="repair.status === 'IN_PROGRESS' || repair.status === 'PENDING'" 
                  class="btn btn-outline-danger" (click)="failRepair()">
            <i class="bi bi-x-circle"></i> Mark Repair Unfeasible / Failed
          </button>
        </div>
      </div>

      <!-- Inventory Maintenance Warning Banner (Rule 17) -->
      <div class="alert alert-warning shadow-sm mb-4">
        <div class="d-flex align-items-center">
          <i class="bi bi-exclamation-triangle-fill fs-3 me-3 text-warning"></i>
          <div>
            <h6 class="fw-bold mb-0">Inventory Maintenance Mode Active</h6>
            <small class="text-dark">These {{ repair.quantity }} units are currently in <strong>MAINTENANCE</strong> state and are blocked from new rental reservations until repair is completed with <strong>GOOD</strong> condition verification.</small>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <div class="col-md-8">
          <div class="card mb-4 shadow-sm border-0">
            <div class="card-header bg-white py-3">
              <h5 class="mb-0 fw-bold"><i class="bi bi-info-circle"></i> Repair Specifications</h5>
            </div>
            <div class="card-body">
              <div class="row g-3">
                <div class="col-md-6">
                  <span class="text-muted small">Associated Claim</span>
                  <div class="fw-bold text-primary">{{ repair.claimNumber || 'CLM-000123' }}</div>
                </div>
                <div class="col-md-6">
                  <span class="text-muted small">Assigned Technician</span>
                  <div class="fw-bold">{{ repair.assignedTo || 'Warehouse Tech' }}</div>
                </div>
                <div class="col-md-6">
                  <span class="text-muted small">Target Product</span>
                  <div class="fw-bold">{{ repair.productName || 'Chiavari Chair' }}</div>
                  <small class="text-muted">{{ repair.productSku || 'SKU-CHAIR-01' }}</small>
                </div>
                <div class="col-md-6">
                  <span class="text-muted small">Quantity In Repair</span>
                  <div class="fw-bold fs-5 text-secondary">{{ repair.quantity }} units</div>
                </div>
                <div class="col-12">
                  <span class="text-muted small">Repair Description</span>
                  <p class="mb-0 bg-light p-3 rounded border">{{ repair.description || 'Sanding and re-staining wood finish' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-4">
          <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3">
              <h5 class="mb-0 fw-bold"><i class="bi bi-currency-dollar"></i> Cost Tracking</h5>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <span class="text-muted small">Estimated Repair Cost</span>
                <h3 class="fw-bold text-primary my-1">$ {{ repair.estimatedCost | number:'1.2-2' }}</h3>
              </div>
              <div>
                <span class="text-muted small">Actual Logged Cost</span>
                <h3 class="fw-bold text-success my-1">$ {{ repair.actualCost | number:'1.2-2' }}</h3>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Complete Repair Modal (Step 18 Verification) -->
      <div class="modal fade show d-block" tabindex="-1" *ngIf="showCompleteModal" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header bg-success text-white">
              <h5 class="modal-title fw-bold"><i class="bi bi-check-circle"></i> Complete Repair Verification</h5>
              <button type="button" class="btn-close btn-close-white" (click)="showCompleteModal = false"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label fw-bold">Post-Repair Verified Condition</label>
                <select class="form-select" [(ngModel)]="postRepairCondition">
                  <option value="GOOD">GOOD (Restores items to AVAILABLE inventory)</option>
                  <option value="MINOR_DAMAGE">MINOR DAMAGE (Keeps in maintenance)</option>
                  <option value="UNUSABLE">UNUSABLE (Transitions items to DAMAGED)</option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Quantity Verified Repaired</label>
                <input type="number" class="form-control" [(ngModel)]="qtyRepaired">
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Actual Final Cost ($)</label>
                <input type="number" class="form-control" [(ngModel)]="actualRepairCost">
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Technician Completion Notes</label>
                <textarea class="form-control" rows="2" [(ngModel)]="repairNotes" placeholder="Describe work completed..."></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showCompleteModal = false">Cancel</button>
              <button type="button" class="btn btn-success" (click)="submitCompleteRepair()">Complete & Update Inventory</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .repair-detail-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .btn-purple { background-color: #7c3aed; }
    .status-badge {
      padding: 0.4rem 0.8rem;
      border-radius: 20px;
      font-size: 0.85rem;
    }
    .status-PENDING { background-color: #fef3c7; color: #b45309; }
    .status-ASSIGNED { background-color: #dbeafe; color: #1e40af; }
    .status-IN_PROGRESS { background-color: #f3e8ff; color: #6b21a8; }
    .status-COMPLETED { background-color: #dcfce7; color: #15803d; }
    .status-FAILED { background-color: #fee2e2; color: #b91c1c; }
  `]
})
export class RepairDetailComponent implements OnInit {
  repair: RepairOrder | null = null;
  showCompleteModal = false;
  postRepairCondition = 'GOOD';
  qtyRepaired = 5;
  actualRepairCost = 95;
  repairNotes = 'Completed leg stain refinishing.';

  constructor(
    private route: ActivatedRoute,
    private claimsService: DamageClaimsService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadRepair(id);
    }
  }

  loadRepair(id: string): void {
    this.claimsService.getRepairById(id).subscribe(data => {
      this.repair = data;
      if (this.repair) {
        this.qtyRepaired = this.repair.quantity;
        this.actualRepairCost = this.repair.estimatedCost;
      }
    });
  }

  startRepair(): void {
    if (!this.repair) return;
    this.claimsService.startRepair(this.repair.id).subscribe(() => this.loadRepair(this.repair!.id));
  }

  openCompleteModal(): void {
    this.showCompleteModal = true;
  }

  submitCompleteRepair(): void {
    if (!this.repair) return;
    this.claimsService.completeRepair(this.repair.id, this.qtyRepaired, this.postRepairCondition, this.actualRepairCost, this.repairNotes).subscribe(() => {
      this.showCompleteModal = false;
      this.loadRepair(this.repair!.id);
    });
  }

  failRepair(): void {
    if (!this.repair) return;
    const reason = prompt('Enter reason for repair failure:');
    if (reason) {
      this.claimsService.failRepair(this.repair.id, reason).subscribe(() => this.loadRepair(this.repair!.id));
    }
  }
}
