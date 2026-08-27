import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DamageClaimsService, ReplacementOrder } from '../../services/damage-claims.service';

@Component({
  selector: 'app-replacements-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="replacements-container">
      <div class="page-header mb-4">
        <div>
          <h2>Replacement Orders</h2>
          <p class="text-muted mb-0">Track equipment replacements for lost, missing, or unrepairable rental inventory</p>
        </div>
        <a routerLink="/damage-claims" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i> Damage Claims
        </a>
      </div>

      <div class="card shadow-sm border-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Replacement #</th>
                <th>Claim #</th>
                <th>Product</th>
                <th class="text-center">Qty</th>
                <th class="text-end">Unit Cost</th>
                <th class="text-end">Total Cost</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let order of replacements">
                <td class="fw-bold text-primary">{{ order.replacementNumber }}</td>
                <td>{{ order.claimNumber || 'CLM-000124' }}</td>
                <td>
                  <div class="fw-bold">{{ order.productName || 'Banquet Table' }}</div>
                  <small class="text-muted">{{ order.productSku || 'SKU-TAB-RND' }}</small>
                </td>
                <td class="text-center"><span class="badge bg-secondary fs-6">{{ order.quantity }}</span></td>
                <td class="text-end fw-semibold">$ {{ order.unitCost | number:'1.2-2' }}</td>
                <td class="text-end fw-bold text-danger">$ {{ order.totalCost | number:'1.2-2' }}</td>
                <td>
                  <span class="badge status-badge" [ngClass]="'status-' + order.status">
                    {{ order.status }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button *ngIf="order.status === 'PENDING'" class="btn btn-outline-primary" (click)="orderReplacement(order.id)">
                      Mark Ordered
                    </button>
                    <button *ngIf="order.status === 'ORDERED'" class="btn btn-outline-info" (click)="receiveReplacement(order.id)">
                      Mark Received
                    </button>
                    <button *ngIf="order.status === 'RECEIVED'" class="btn btn-success" (click)="completeReplacement(order.id)">
                      Complete & Update Catalog
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="replacements.length === 0">
                <td colspan="8" class="text-center py-5 text-muted">No replacement orders found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .replacements-container {
      padding: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .status-badge {
      padding: 0.35rem 0.65rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .status-PENDING { background-color: #fef3c7; color: #b45309; }
    .status-ORDERED { background-color: #dbeafe; color: #1e40af; }
    .status-RECEIVED { background-color: #e0e7ff; color: #3730a3; }
    .status-COMPLETED { background-color: #dcfce7; color: #15803d; }
  `]
})
export class ReplacementsListComponent implements OnInit {
  replacements: ReplacementOrder[] = [];

  constructor(private claimsService: DamageClaimsService) {}

  ngOnInit(): void {
    this.loadReplacements();
  }

  loadReplacements(): void {
    this.claimsService.getReplacements().subscribe(data => {
      this.replacements = data || [];
    });
  }

  orderReplacement(id: string): void {
    this.claimsService.orderReplacement(id).subscribe(() => this.loadReplacements());
  }

  receiveReplacement(id: string): void {
    this.claimsService.receiveReplacement(id).subscribe(() => this.loadReplacements());
  }

  completeReplacement(id: string): void {
    this.claimsService.completeReplacement(id).subscribe(() => this.loadReplacements());
  }
}
