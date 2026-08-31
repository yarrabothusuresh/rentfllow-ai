import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseFulfillmentService, WarehouseMetrics } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="warehouse-dashboard animate-fade-in">
      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="badge-tag">WAREHOUSE OPERATIONS 2.0</div>
          <h2>🏬 Fulfillment Operations Center</h2>
          <p class="subtitle">Real-time Pick, Pack, Kit, Load & Driver Handoff Execution Pipeline.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/warehouse/my-work" class="btn btn-accent">👷 My Active Work</a>
          <a routerLink="/warehouse/pick-lists" class="btn btn-primary">🚜 Pick Lists</a>
          <a routerLink="/warehouse/pack-lists" class="btn btn-secondary">📦 Pack Lists</a>
          <a routerLink="/warehouse/load-lists" class="btn btn-secondary">🚚 Load & Handoff</a>
        </div>
      </div>

      <!-- Live Pipeline Funnel -->
      <div class="pipeline-funnel" *ngIf="metrics">
        <div class="funnel-step" routerLink="/warehouse/pick-lists" [queryParams]="{status: 'PENDING'}">
          <div class="step-icon">📋</div>
          <div class="step-info">
            <span class="step-num">{{ metrics.ordersReadyToPick }}</span>
            <span class="step-title">Ready to Pick</span>
          </div>
          <div class="step-arrow">➔</div>
        </div>

        <div class="funnel-step active-step" routerLink="/warehouse/pick-lists" [queryParams]="{status: 'IN_PROGRESS'}">
          <div class="step-icon">🚜</div>
          <div class="step-info">
            <span class="step-num text-warning">{{ metrics.ordersPicking }}</span>
            <span class="step-title">Picking</span>
          </div>
          <div class="step-arrow">➔</div>
        </div>

        <div class="funnel-step" routerLink="/warehouse/pick-lists" [queryParams]="{status: 'COMPLETED'}">
          <div class="step-icon">🔍</div>
          <div class="step-info">
            <span class="step-num text-info">{{ metrics.ordersVerifying }}</span>
            <span class="step-title">Verifying</span>
          </div>
          <div class="step-arrow">➔</div>
        </div>

        <div class="funnel-step" routerLink="/warehouse/pack-lists">
          <div class="step-icon">📦</div>
          <div class="step-info">
            <span class="step-num text-purple">{{ metrics.ordersPacking }}</span>
            <span class="step-title">Packing / Kits</span>
          </div>
          <div class="step-arrow">➔</div>
        </div>

        <div class="funnel-step" routerLink="/warehouse/load-lists">
          <div class="step-icon">🚚</div>
          <div class="step-info">
            <span class="step-num text-success">{{ metrics.ordersLoading }}</span>
            <span class="step-title">Loading Vehicle</span>
          </div>
          <div class="step-arrow">➔</div>
        </div>

        <div class="funnel-step" routerLink="/warehouse/load-lists" [queryParams]="{status: 'HANDED_OFF'}">
          <div class="step-icon">🤝</div>
          <div class="step-info">
            <span class="step-num text-cyan">{{ metrics.ordersHandedOff }}</span>
            <span class="step-title">Driver Handoff</span>
          </div>
        </div>
      </div>

      <!-- Key Alert Banners -->
      <div class="alerts-container" *ngIf="metrics">
        <div class="alert-card alert-danger" *ngIf="metrics.blockingExceptionsCount > 0" routerLink="/warehouse/exceptions">
          <span class="alert-icon">🚨</span>
          <div class="alert-content">
            <h4>{{ metrics.blockingExceptionsCount }} Blocking Warehouse Exception(s)</h4>
            <p>Unresolved shortage or damage holding up pick verification & fulfillment.</p>
          </div>
          <span class="btn btn-sm btn-danger">Resolve Now</span>
        </div>

        <div class="alert-card alert-warning" *ngIf="metrics.pendingSubstitutionsCount > 0" routerLink="/warehouse/substitutions">
          <span class="alert-icon">🔄</span>
          <div class="alert-content">
            <h4>{{ metrics.pendingSubstitutionsCount }} Substitution(s) Awaiting Manager Approval</h4>
            <p>Product substitutions proposed by warehouse crew require approval.</p>
          </div>
          <span class="btn btn-sm btn-warning">Review</span>
        </div>
      </div>

      <!-- Quick Action Navigation Hub -->
      <div class="modules-grid">
        <div class="module-card" routerLink="/warehouse/my-work">
          <div class="module-icon-wrap bg-blue">👷</div>
          <div class="module-body">
            <h3>Operator "My Work"</h3>
            <p>Focused single-operator view with assigned pick lists, pack lists, and quick load actions.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Open My Work ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/pick-lists">
          <div class="module-icon-wrap bg-orange">🚜</div>
          <div class="module-body">
            <h3>Pick Lists & Mobile Pick</h3>
            <p>Location-ordered pick routing, 1D/2D serialized scanning, bulk qty +/- buttons, and verification.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Manage Picks ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/pack-lists">
          <div class="module-icon-wrap bg-purple">📦</div>
          <div class="module-body">
            <h3>Packing & Kit Verification</h3>
            <p>Container packing (Bags, Cases, Rolling Carts) and composite kit component validation.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Manage Packs ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/load-lists">
          <div class="module-icon-wrap bg-green">🚚</div>
          <div class="module-body">
            <h3>Load Verification & Driver Handoff</h3>
            <p>Vehicle capacity checking, container-level loading, and driver sign-off integration.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Manage Loads ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/exceptions">
          <div class="module-icon-wrap bg-red">⚠️</div>
          <div class="module-body">
            <h3>Exception & Shortage Center</h3>
            <p>Log and resolve missing inventory, damaged goods, and wrong bin locations with full audit.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">View Exceptions ({{ metrics?.openExceptionsCount || 0 }}) ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/substitutions">
          <div class="module-icon-wrap bg-amber">🔄</div>
          <div class="module-body">
            <h3>Controlled Substitutions</h3>
            <p>Propose replacement items with real-time stock and price delta checks, then submit for manager sign-off.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Substitutions ({{ metrics?.pendingSubstitutionsCount || 0 }}) ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/containers">
          <div class="module-icon-wrap bg-teal">🧰</div>
          <div class="module-body">
            <h3>Containers & Transport Gear</h3>
            <p>Track reusable bags, flight cases, carts, and pallets with live allocation status.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">View Containers ({{ (metrics?.availableContainersCount || 0) + (metrics?.inUseContainersCount || 0) }}) ➔</span>
          </div>
        </div>

        <div class="module-card" routerLink="/warehouse/orders">
          <div class="module-icon-wrap bg-indigo">📋</div>
          <div class="module-body">
            <h3>All Warehouse Orders</h3>
            <p>Search and manage warehouse orders connected to Bookings, Events, and Invoices.</p>
          </div>
          <div class="module-footer">
            <span class="link-text">Browse Orders ➔</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .warehouse-dashboard {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
      color: var(--text-color, #e2e8f0);
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .badge-tag {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.08em;
      color: #38bdf8;
      background: rgba(56, 189, 248, 0.15);
      padding: 0.25rem 0.6rem;
      border-radius: 4px;
      margin-bottom: 0.4rem;
    }
    .page-header h2 {
      margin: 0 0 0.25rem 0;
      font-size: 1.8rem;
      font-weight: 700;
      color: #f8fafc;
    }
    .subtitle {
      margin: 0;
      color: #94a3b8;
      font-size: 0.95rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .btn {
      padding: 0.6rem 1.2rem;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      border: 1px solid transparent;
      transition: all 0.2s ease;
    }
    .btn-primary { background: #2563eb; color: #fff; }
    .btn-primary:hover { background: #1d4ed8; }
    .btn-secondary { background: #334155; color: #f8fafc; border-color: #475569; }
    .btn-secondary:hover { background: #475569; }
    .btn-accent { background: #7c3aed; color: #fff; }
    .btn-accent:hover { background: #6d28d9; }
    .btn-danger { background: #dc2626; color: #fff; }
    .btn-warning { background: #d97706; color: #fff; }
    .btn-sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; border-radius: 6px; }

    /* Pipeline Funnel */
    .pipeline-funnel {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
      gap: 0.75rem;
      margin-bottom: 2rem;
      background: #1e293b;
      padding: 1rem;
      border-radius: 12px;
      border: 1px solid #334155;
    }
    .funnel-step {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.75rem 1rem;
      background: #0f172a;
      border-radius: 8px;
      border: 1px solid #334155;
      cursor: pointer;
      transition: transform 0.15s ease, border-color 0.15s ease;
    }
    .funnel-step:hover {
      transform: translateY(-2px);
      border-color: #38bdf8;
    }
    .step-icon { font-size: 1.5rem; }
    .step-info { display: flex; flex-direction: column; }
    .step-num { font-size: 1.4rem; font-weight: 700; color: #f8fafc; }
    .step-title { font-size: 0.75rem; color: #94a3b8; font-weight: 500; }
    .step-arrow { color: #475569; font-size: 1.1rem; }

    .text-warning { color: #f59e0b !important; }
    .text-info { color: #38bdf8 !important; }
    .text-purple { color: #a855f7 !important; }
    .text-success { color: #10b981 !important; }
    .text-cyan { color: #06b6d4 !important; }

    /* Alerts */
    .alerts-container {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      margin-bottom: 2rem;
    }
    .alert-card {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem 1.25rem;
      border-radius: 10px;
      cursor: pointer;
      transition: transform 0.15s ease;
    }
    .alert-card:hover { transform: scale(1.01); }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.4); }
    .alert-warning { background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.4); }
    .alert-icon { font-size: 1.8rem; }
    .alert-content { flex: 1; }
    .alert-content h4 { margin: 0 0 0.2rem 0; font-size: 1rem; font-weight: 600; color: #f8fafc; }
    .alert-content p { margin: 0; font-size: 0.85rem; color: #cbd5e1; }

    /* Modules Grid */
    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.25rem;
    }
    .module-card {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .module-card:hover {
      transform: translateY(-3px);
      border-color: #60a5fa;
      box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.3);
    }
    .module-icon-wrap {
      width: 46px;
      height: 46px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      margin-bottom: 1rem;
    }
    .bg-blue { background: rgba(59, 130, 246, 0.2); }
    .bg-orange { background: rgba(249, 115, 22, 0.2); }
    .bg-purple { background: rgba(168, 85, 247, 0.2); }
    .bg-green { background: rgba(34, 197, 94, 0.2); }
    .bg-red { background: rgba(239, 68, 68, 0.2); }
    .bg-amber { background: rgba(245, 158, 11, 0.2); }
    .bg-teal { background: rgba(20, 184, 166, 0.2); }
    .bg-indigo { background: rgba(99, 102, 241, 0.2); }

    .module-body { flex: 1; }
    .module-body h3 { margin: 0 0 0.4rem 0; font-size: 1.1rem; color: #f8fafc; font-weight: 600; }
    .module-body p { margin: 0 0 1rem 0; font-size: 0.85rem; color: #94a3b8; line-height: 1.4; }
    .module-footer {
      border-top: 1px solid #334155;
      padding-top: 0.75rem;
    }
    .link-text {
      color: #38bdf8;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .module-card:hover .link-text { color: #60a5fa; text-decoration: underline; }
  `]
})
export class WarehouseDashboardComponent implements OnInit {
  metrics: WarehouseMetrics | null = null;
  loading = true;

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.loading = true;
    this.fulfillmentService.getMetrics().subscribe({
      next: (m) => {
        this.metrics = m;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
