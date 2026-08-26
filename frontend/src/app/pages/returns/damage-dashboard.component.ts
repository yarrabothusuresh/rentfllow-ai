import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReturnsService, DamageRecord } from '../../services/returns.service';

@Component({
  selector: 'app-damage-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="damage-dashboard-container">
      <div class="header-section mb-4">
        <div>
          <h2>Equipment Damage Log</h2>
          <p class="text-muted">Tracking damaged equipment, severity levels, repair/replacement cost estimates, and open issues</p>
        </div>
        <div class="header-actions">
          <a routerLink="/returns" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Returns Dashboard
          </a>
        </div>
      </div>

      <!-- Damage Records Table -->
      <div class="card shadow-sm border-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Product</th>
                <th>Return #</th>
                <th>Customer</th>
                <th>Quantity</th>
                <th>Damage Category</th>
                <th>Severity</th>
                <th>Est. Repair Cost</th>
                <th>Est. Replacement Cost</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of damageRecords">
                <td>
                  <div class="fw-bold">{{ item.productName || 'Chiavari Chair' }}</div>
                  <small class="text-muted">{{ item.productSku || 'SKU-CHAIR-01' }}</small>
                </td>
                <td>
                  <a [routerLink]="['/returns', item.inspectionId]" class="fw-bold text-decoration-none" *ngIf="item.returnNumber">
                    {{ item.returnNumber }}
                  </a>
                  <span *ngIf="!item.returnNumber" class="fw-bold text-primary">RET-000123</span>
                </td>
                <td>{{ item.customerName || 'ABC Events LLC' }}</td>
                <td><span class="badge bg-danger fs-6">{{ item.quantity }}</span></td>
                <td><span class="badge bg-warning text-dark">{{ item.category }}</span></td>
                <td>
                  <span class="badge" [ngClass]="getSeverityClass(item.severity)">
                    {{ item.severity }}
                  </span>
                </td>
                <td class="fw-semibold text-muted">{{ item.estimatedRepairCost ? ('$' + item.estimatedRepairCost) : '$0.00' }}</td>
                <td class="fw-semibold text-muted">{{ item.estimatedReplacementCost ? ('$' + item.estimatedReplacementCost) : '$0.00' }}</td>
                <td>
                  <span class="badge bg-secondary">{{ item.status || 'OPEN' }}</span>
                </td>
              </tr>
              <tr *ngIf="damageRecords.length === 0">
                <td colspan="9" class="text-center py-5 text-muted">
                  <i class="bi bi-shield-check fs-1 text-success"></i>
                  <p class="mt-2">No damage records found.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .damage-dashboard-container {
      padding: 1.5rem;
    }
    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .severity-MINOR { background-color: #fef08a; color: #854d0e; }
    .severity-MAJOR { background-color: #fed7aa; color: #9a3412; }
    .severity-CRITICAL { background-color: #fecdd3; color: #9f1239; }
  `]
})
export class DamageDashboardComponent implements OnInit {
  damageRecords: DamageRecord[] = [];

  constructor(private returnsService: ReturnsService) {}

  ngOnInit(): void {
    this.returnsService.getDamageRecords().subscribe(data => {
      this.damageRecords = data || [];
    });
  }

  getSeverityClass(severity: string): string {
    return 'severity-' + severity;
  }
}
