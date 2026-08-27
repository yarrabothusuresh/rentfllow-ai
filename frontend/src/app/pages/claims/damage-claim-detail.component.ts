import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DamageClaimsService, DamageClaim, DamageClaimItem, ClaimEstimate } from '../../services/damage-claims.service';

@Component({
  selector: 'app-damage-claim-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="claim-detail-container" *ngIf="claim">
      <!-- Breadcrumb & Header -->
      <div class="page-header mb-4">
        <div>
          <a routerLink="/damage-claims" class="text-decoration-none text-muted mb-1 d-inline-block">
            <i class="bi bi-arrow-left"></i> Back to Claims
          </a>
          <h2>
            CLAIM {{ claim.claimNumber }}
            <span class="badge status-badge ms-2" [ngClass]="'status-' + claim.status">
              {{ claim.status.replace('_', ' ') }}
            </span>
          </h2>
        </div>

        <!-- Action Buttons -->
        <div class="header-actions">
          <button *ngIf="claim.status === 'OPEN' || claim.status === 'UNDER_REVIEW' || claim.status === 'ESTIMATE_CREATED'" 
                  class="btn btn-outline-primary" (click)="openEstimateModal()">
            <i class="bi bi-calculator"></i> Create / Update Estimate
          </button>

          <button *ngIf="claim.status === 'ESTIMATE_CREATED' || claim.status === 'UNDER_REVIEW'" 
                  class="btn btn-warning" (click)="sendToCustomer()">
            <i class="bi bi-send"></i> Send to Customer
          </button>

          <button *ngIf="claim.status === 'CUSTOMER_REVIEW' || claim.status === 'UNDER_REVIEW' || claim.status === 'ESTIMATE_CREATED'" 
                  class="btn btn-success" (click)="approveClaim()">
            <i class="bi bi-check-circle"></i> Approve Claim
          </button>

          <button *ngIf="claim.status === 'CUSTOMER_REVIEW'" 
                  class="btn btn-outline-danger" (click)="openDisputeModal()">
            <i class="bi bi-exclamation-triangle"></i> Record Dispute
          </button>

          <button *ngIf="claim.status === 'APPROVED'" 
                  class="btn btn-purple text-white" (click)="startRepair()">
            <i class="bi bi-tools"></i> Dispatch Repair Order
          </button>

          <button *ngIf="claim.status === 'APPROVED'" 
                  class="btn btn-orange text-white" (click)="requestReplacement()">
            <i class="bi bi-box-seam"></i> Order Replacement
          </button>

          <button *ngIf="claim.status !== 'RESOLVED' && claim.status !== 'WAIVED'" 
                  class="btn btn-outline-secondary" (click)="openWaiveModal()">
            <i class="bi bi-slash-circle"></i> Waive Claim
          </button>

          <button *ngIf="claim.status === 'APPROVED' || claim.status === 'REPAIR_IN_PROGRESS' || claim.status === 'REPLACEMENT_REQUIRED'" 
                  class="btn btn-success" (click)="openResolveModal()">
            <i class="bi bi-check-lg"></i> Resolve Claim
          </button>
        </div>
      </div>

      <!-- Financial Summary Exposure Banner -->
      <div class="row g-3 mb-4">
        <div class="col-md-3">
          <div class="card info-card h-100">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Customer & Event</span>
              <h5 class="fw-bold my-1">{{ claim.customerName || 'ABC Events LLC' }}</h5>
              <div class="small text-muted"><i class="bi bi-hash"></i> Booking: {{ claim.bookingNumber || 'BOOK-000123' }}</div>
              <div class="small text-muted"><i class="bi bi-arrow-return-left"></i> Return: {{ claim.returnNumber || 'RET-000123' }}</div>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card info-card h-100">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Estimated Total Exposure</span>
              <h3 class="fw-bold text-danger my-1">$ {{ claim.estimatedTotalCost | number:'1.2-2' }}</h3>
              <small class="text-muted">Calculated server-side</small>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card info-card h-100">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Approved Total Amount</span>
              <h3 class="fw-bold text-success my-1">$ {{ claim.approvedTotalCost | number:'1.2-2' }}</h3>
              <small class="text-muted">Draft charge prepared</small>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card info-card h-100">
            <div class="card-body">
              <span class="text-muted small fw-semibold">Claim Type & Priority</span>
              <h5 class="fw-bold my-1">{{ claim.claimType }}</h5>
              <span class="badge bg-secondary">{{ claim.priority }} PRIORITY</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Dispute Alert Banner if Disputed -->
      <div *ngIf="claim.status === 'DISPUTED'" class="alert alert-danger shadow-sm mb-4">
        <h5 class="alert-heading fw-bold"><i class="bi bi-exclamation-triangle-fill"></i> Customer Disputed Claim</h5>
        <p class="mb-1"><strong>Dispute Reason:</strong> {{ claim.disputeReason || 'Customer disputed liability.' }}</p>
        <small class="text-muted">Disputed at {{ claim.disputedAt | date:'medium' }} by {{ claim.disputedBy }}</small>
      </div>

      <!-- Items Breakdown Table -->
      <div class="card items-card mb-4">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-boxes"></i> Damaged / Missing Items Detail</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Product</th>
                <th class="text-center">Qty</th>
                <th>Claim Type</th>
                <th>Category</th>
                <th>Severity</th>
                <th class="text-end">Repair Est / Item</th>
                <th class="text-end">Replace Est / Item</th>
                <th class="text-end">Line Est Total</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of claim.items">
                <td>
                  <div class="fw-bold">{{ item.productNameSnapshot }}</div>
                  <small class="text-muted">{{ item.skuSnapshot }}</small>
                </td>
                <td class="text-center"><span class="badge bg-danger fs-6">{{ item.quantity }}</span></td>
                <td><span class="badge bg-light text-dark">{{ item.claimType }}</span></td>
                <td><span class="badge bg-warning text-dark">{{ item.damageCategory || 'OTHER' }}</span></td>
                <td><span class="badge bg-secondary">{{ item.severity || 'MINOR' }}</span></td>
                <td class="text-end fw-semibold">$ {{ item.unitRepairCost | number:'1.2-2' }}</td>
                <td class="text-end fw-semibold">$ {{ item.unitReplacementCost | number:'1.2-2' }}</td>
                <td class="text-end fw-bold text-danger">$ {{ item.estimatedCost | number:'1.2-2' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Historical Estimates Section -->
      <div class="card items-card mb-4" *ngIf="claim.estimates && claim.estimates.length > 0">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-file-earmark-spreadsheet"></i> Historical Cost Estimates</h5>
        </div>
        <div class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Version</th>
                <th>Repair Cost</th>
                <th>Replacement Cost</th>
                <th>Labor</th>
                <th>Transport</th>
                <th>Subtotal</th>
                <th>Tax</th>
                <th>Total</th>
                <th>Created By</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let est of claim.estimates">
                <td class="fw-bold text-primary">Version {{ est.version }}</td>
                <td>$ {{ est.repairCost | number:'1.2-2' }}</td>
                <td>$ {{ est.replacementCost | number:'1.2-2' }}</td>
                <td>$ {{ est.laborCost | number:'1.2-2' }}</td>
                <td>$ {{ est.transportCost | number:'1.2-2' }}</td>
                <td class="fw-semibold">$ {{ est.subtotal | number:'1.2-2' }}</td>
                <td>$ {{ est.tax | number:'1.2-2' }}</td>
                <td class="fw-bold text-danger">$ {{ est.total | number:'1.2-2' }}</td>
                <td class="small text-muted">{{ est.createdBy }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Timeline Audit Trail -->
      <div class="card items-card">
        <div class="card-header bg-white py-3">
          <h5 class="mb-0 fw-bold"><i class="bi bi-clock-history"></i> Claim Activity Timeline</h5>
        </div>
        <div class="card-body">
          <ul class="timeline">
            <li *ngFor="let audit of timeline">
              <div class="fw-bold text-primary">{{ audit.action }}</div>
              <p class="mb-1 text-muted">{{ audit.details }}</p>

              <small class="text-secondary">{{ audit.timestamp | date:'medium' }} by {{ audit.performedBy }}</small>
            </li>
            <li *ngIf="timeline.length === 0" class="text-muted">No timeline activity recorded yet.</li>
          </ul>
        </div>
      </div>

      <!-- Estimate Modal -->
      <div class="modal fade show d-block" tabindex="-1" *ngIf="showEstimateModal" style="background: rgba(0,0,0,0.5)">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header bg-primary text-white">
              <h5 class="modal-title fw-bold"><i class="bi bi-calculator"></i> Create Cost Estimate: {{ claim.claimNumber }}</h5>
              <button type="button" class="btn-close btn-close-white" (click)="showEstimateModal = false"></button>
            </div>
            <div class="modal-body">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Repair Cost ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estRepairCost">
                </div>
                <div class="col-md-6">
                  <label class="form-label">Replacement Cost ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estReplacementCost">
                </div>
                <div class="col-md-4">
                  <label class="form-label">Labor Cost ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estLaborCost">
                </div>
                <div class="col-md-4">
                  <label class="form-label">Transport Cost ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estTransportCost">
                </div>
                <div class="col-md-4">
                  <label class="form-label">Other Cost ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estOtherCost">
                </div>
                <div class="col-md-6">
                  <label class="form-label">Discount ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estDiscount">
                </div>
                <div class="col-md-6">
                  <label class="form-label">Tax ($)</label>
                  <input type="number" class="form-control" [(ngModel)]="estTax">
                </div>
                <div class="col-12">
                  <label class="form-label">Estimate Notes</label>
                  <textarea class="form-control" rows="2" [(ngModel)]="estNotes" placeholder="Cost breakdown notes..."></textarea>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="showEstimateModal = false">Cancel</button>
              <button type="button" class="btn btn-primary" (click)="submitEstimate()">Save Estimate Version</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .claim-detail-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .header-actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }
    .info-card {
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
    }
    .items-card {
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 2px 6px rgba(0,0,0,0.04);
      overflow: hidden;
    }
    .btn-purple { background-color: #7c3aed; }
    .btn-orange { background-color: #ea580c; }
    .status-badge {
      padding: 0.4rem 0.8rem;
      border-radius: 20px;
      font-size: 0.85rem;
    }
    .status-OPEN { background-color: #e0f2fe; color: #0369a1; }
    .status-UNDER_REVIEW { background-color: #e0e7ff; color: #3730a3; }
    .status-ESTIMATE_CREATED { background-color: #ddd6fe; color: #5b21b6; }
    .status-CUSTOMER_REVIEW { background-color: #fef3c7; color: #b45309; }
    .status-APPROVED { background-color: #dcfce7; color: #15803d; }
    .status-DISPUTED { background-color: #fee2e2; color: #b91c1c; }
    .status-REPAIR_IN_PROGRESS { background-color: #f3e8ff; color: #6b21a8; }
    .status-REPLACEMENT_REQUIRED { background-color: #ffedd5; color: #c2410c; }
    .status-RESOLVED { background-color: #d1fae5; color: #065f46; }
    .status-WAIVED { background-color: #f1f5f9; color: #475569; }

    .timeline {
      list-style-type: none;
      padding-left: 1rem;
      border-left: 2px solid #cbd5e1;
    }
    .timeline li {
      margin-bottom: 1rem;
      position: relative;
      padding-left: 1rem;
    }
    .timeline li::before {
      content: '';
      position: absolute;
      left: -1.35rem;
      top: 0.2rem;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: #3b82f6;
    }
  `]
})
export class DamageClaimDetailComponent implements OnInit {
  claim: DamageClaim | null = null;
  timeline: any[] = [];

  showEstimateModal = false;
  estRepairCost = 125;
  estReplacementCost = 0;
  estLaborCost = 25;
  estTransportCost = 15;
  estOtherCost = 0;
  estDiscount = 0;
  estTax = 10;
  estNotes = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private claimsService: DamageClaimsService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadClaim(id);
    }
  }

  loadClaim(id: string): void {
    this.claimsService.getClaimById(id).subscribe(data => {
      this.claim = data;
    });
    this.claimsService.getClaimTimeline(id).subscribe(data => {
      this.timeline = data || [];
    });
  }

  openEstimateModal(): void {
    this.showEstimateModal = true;
  }

  submitEstimate(): void {
    if (!this.claim) return;
    this.claimsService.createEstimate(this.claim.id, {
      repairCost: this.estRepairCost,
      replacementCost: this.estReplacementCost,
      laborCost: this.estLaborCost,
      transportCost: this.estTransportCost,
      otherCost: this.estOtherCost,
      discount: this.estDiscount,
      tax: this.estTax,
      notes: this.estNotes
    }).subscribe(() => {
      this.showEstimateModal = false;
      this.loadClaim(this.claim!.id);
    });
  }

  sendToCustomer(): void {
    if (!this.claim) return;
    this.claimsService.sendToCustomer(this.claim.id).subscribe(() => this.loadClaim(this.claim!.id));
  }

  approveClaim(): void {
    if (!this.claim) return;
    this.claimsService.customerApprove(this.claim.id).subscribe(() => this.loadClaim(this.claim!.id));
  }

  openDisputeModal(): void {
    if (!this.claim) return;
    const reason = prompt('Enter dispute reason:');
    if (reason) {
      this.claimsService.customerDispute(this.claim.id, reason).subscribe(() => this.loadClaim(this.claim!.id));
    }
  }

  openWaiveModal(): void {
    if (!this.claim) return;
    const reason = prompt('Enter waiver reason:');
    if (reason) {
      this.claimsService.waiveClaim(this.claim.id, reason).subscribe(() => this.loadClaim(this.claim!.id));
    }
  }

  startRepair(): void {
    if (!this.claim || !this.claim.items || this.claim.items.length === 0) return;
    const firstItem = this.claim.items[0];
    this.claimsService.startRepairForClaim(this.claim.id, firstItem.productId, firstItem.quantity, 'Scratch repair', 125).subscribe(() => {
      this.loadClaim(this.claim!.id);
    });
  }

  requestReplacement(): void {
    if (!this.claim || !this.claim.items || this.claim.items.length === 0) return;
    const firstItem = this.claim.items[0];
    this.claimsService.replacementRequiredForClaim(this.claim.id, firstItem.productId, firstItem.quantity, 'Unrepairable damage replacement', 150).subscribe(() => {
      this.loadClaim(this.claim!.id);
    });
  }

  openResolveModal(): void {
    if (!this.claim) return;
    this.claimsService.resolveClaim(this.claim.id, 'REPAIRED', 'Claim resolved successfully.').subscribe(() => {
      this.loadClaim(this.claim!.id);
    });
  }
}
