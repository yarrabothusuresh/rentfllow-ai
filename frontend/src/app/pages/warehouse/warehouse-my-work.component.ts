import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WarehouseFulfillmentService, MyWorkItem } from '../../services/warehouse-fulfillment.service';
import { RoleStateService } from '../../services/role-state.service';

@Component({
  selector: 'app-warehouse-my-work',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="my-work-container animate-fade-in">
      <div class="page-header">
        <div>
          <a routerLink="/warehouse" class="back-link">← Back to Warehouse Center</a>
          <h2>👷 Operator Task Board (My Work)</h2>
          <p class="subtitle">Assigned pick tasks, packing workflows, and vehicle loads.</p>
        </div>
        <div class="header-actions">
          <button class="btn btn-secondary" (click)="loadMyWork()">🔄 Refresh</button>
        </div>
      </div>

      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Loading your assigned workload...</p>
      </div>

      <div class="work-content" *ngIf="!loading && work">
        <!-- Blocking Exceptions Alert -->
        <div class="alert-box" *ngIf="work.blockingExceptions.length > 0">
          <div class="alert-title">🚨 {{ work.blockingExceptions.length }} Blocking Exception(s) Require Attention</div>
          <div class="exception-cards">
            <div class="exc-card" *ngFor="let exc of work.blockingExceptions">
              <div class="exc-header">
                <span class="badge badge-danger">{{ exc.exceptionType }}</span>
                <span class="exc-date">{{ exc.reportedAt | date:'short' }}</span>
              </div>
              <div class="exc-body">
                <strong>{{ exc.productName || 'Order Exception' }}</strong>
                <p>{{ exc.description }}</p>
              </div>
              <a routerLink="/warehouse/exceptions" class="btn btn-sm btn-danger">Resolve Exception</a>
            </div>
          </div>
        </div>

        <!-- Section 1: Active Pick Lists -->
        <div class="work-section">
          <div class="section-header">
            <h3>🚜 Assigned Pick Lists ({{ work.pickLists.length }})</h3>
            <a routerLink="/warehouse/pick-lists" class="view-all">View all pick lists ➔</a>
          </div>

          <div class="empty-state" *ngIf="work.pickLists.length === 0">
            <p>No active pick lists assigned right now. Great job!</p>
          </div>

          <div class="cards-grid" *ngIf="work.pickLists.length > 0">
            <div class="task-card" *ngFor="let pl of work.pickLists">
              <div class="task-badge-row">
                <span class="badge badge-pick">{{ pl.pickListNumber }}</span>
                <span class="badge" [ngClass]="'badge-' + pl.priority.toLowerCase()">{{ pl.priority }}</span>
                <span class="badge badge-status">{{ pl.status }}</span>
              </div>

              <h4 class="task-title">{{ pl.eventName || 'Order ' + pl.warehouseOrderNumber }}</h4>
              <p class="task-meta">Order: <strong>{{ pl.warehouseOrderNumber }}</strong> | Booking: <strong>{{ pl.bookingNumber }}</strong></p>
              <p class="task-meta" *ngIf="pl.customerName">Customer: {{ pl.customerName }}</p>

              <div class="progress-bar-container">
                <div class="progress-labels">
                  <span>Progress</span>
                  <span>{{ pl.totalPickedItems }} / {{ pl.totalRequiredItems }} items ({{ pl.progressPercentage | number:'1.0-0' }}%)</span>
                </div>
                <div class="progress-track">
                  <div class="progress-fill fill-pick" [style.width.%]="pl.progressPercentage"></div>
                </div>
              </div>

              <div class="task-actions">
                <a [routerLink]="['/warehouse/pick-lists', pl.id, 'mobile']" class="btn btn-primary btn-mobile">
                  📱 Open Mobile Scanner
                </a>
                <a [routerLink]="['/warehouse/pick-lists', pl.id]" class="btn btn-secondary">
                  Details
                </a>
              </div>
            </div>
          </div>
        </div>

        <!-- Section 2: Active Pack Lists -->
        <div class="work-section">
          <div class="section-header">
            <h3>📦 Assigned Pack & Kit Lists ({{ work.packLists.length }})</h3>
            <a routerLink="/warehouse/pack-lists" class="view-all">View all pack lists ➔</a>
          </div>

          <div class="empty-state" *ngIf="work.packLists.length === 0">
            <p>No active packing tasks pending.</p>
          </div>

          <div class="cards-grid" *ngIf="work.packLists.length > 0">
            <div class="task-card" *ngFor="let pkl of work.packLists">
              <div class="task-badge-row">
                <span class="badge badge-pack">{{ pkl.packListNumber }}</span>
                <span class="badge badge-status">{{ pkl.status }}</span>
              </div>

              <h4 class="task-title">{{ pkl.eventName || 'Order ' + pkl.warehouseOrderNumber }}</h4>
              <p class="task-meta">Order: <strong>{{ pkl.warehouseOrderNumber }}</strong> | Containers: <strong>{{ pkl.containerCount }}</strong></p>

              <div class="progress-bar-container">
                <div class="progress-labels">
                  <span>Packed</span>
                  <span>{{ pkl.totalPackedItems }} / {{ pkl.totalRequiredItems }} ({{ pkl.progressPercentage | number:'1.0-0' }}%)</span>
                </div>
                <div class="progress-track">
                  <div class="progress-fill fill-pack" [style.width.%]="pkl.progressPercentage"></div>
                </div>
              </div>

              <div class="task-actions">
                <a [routerLink]="['/warehouse/pack-lists', pkl.id]" class="btn btn-purple">
                  📦 Pack Containers & Kits
                </a>
              </div>
            </div>
          </div>
        </div>

        <!-- Section 3: Active Load Lists -->
        <div class="work-section">
          <div class="section-header">
            <h3>🚚 Assigned Vehicle Loads ({{ work.loadLists.length }})</h3>
            <a routerLink="/warehouse/load-lists" class="view-all">View all load lists ➔</a>
          </div>

          <div class="empty-state" *ngIf="work.loadLists.length === 0">
            <p>No loading lists pending.</p>
          </div>

          <div class="cards-grid" *ngIf="work.loadLists.length > 0">
            <div class="task-card" *ngFor="let ld of work.loadLists">
              <div class="task-badge-row">
                <span class="badge badge-load">{{ ld.loadListNumber }}</span>
                <span class="badge badge-status">{{ ld.status }}</span>
              </div>

              <h4 class="task-title">{{ ld.eventName || 'Order ' + ld.warehouseOrderNumber }}</h4>
              <p class="task-meta">Vehicle: <strong>{{ ld.vehicleCodeSnapshot || ld.vehicleName || 'Unassigned' }}</strong> | Driver: <strong>{{ ld.driverNameSnapshot || 'Unassigned' }}</strong></p>

              <div class="progress-bar-container">
                <div class="progress-labels">
                  <span>Loaded</span>
                  <span>{{ ld.loadedItems }} / {{ ld.totalItems }} ({{ ld.progressPercentage | number:'1.0-0' }}%)</span>
                </div>
                <div class="progress-track">
                  <div class="progress-fill fill-load" [style.width.%]="ld.progressPercentage"></div>
                </div>
              </div>

              <div class="task-actions">
                <a [routerLink]="['/warehouse/load-lists', ld.id]" class="btn btn-success">
                  🚚 Load & Driver Sign-off
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .my-work-container {
      padding: 1.5rem;
      max-width: 1300px;
      margin: 0 auto;
      color: #f8fafc;
    }
    .back-link {
      display: inline-block;
      color: #38bdf8;
      font-size: 0.85rem;
      text-decoration: none;
      margin-bottom: 0.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }
    .page-header h2 { margin: 0 0 0.25rem 0; font-size: 1.7rem; }
    .subtitle { margin: 0; color: #94a3b8; font-size: 0.9rem; }
    .btn {
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.85rem;
      cursor: pointer;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      border: none;
    }
    .btn-primary { background: #2563eb; color: #fff; }
    .btn-mobile { background: linear-gradient(135deg, #2563eb, #1d4ed8); }
    .btn-secondary { background: #334155; color: #f8fafc; }
    .btn-purple { background: #7c3aed; color: #fff; }
    .btn-success { background: #10b981; color: #fff; }
    .btn-danger { background: #dc2626; color: #fff; }
    .btn-sm { padding: 0.35rem 0.7rem; font-size: 0.8rem; }

    .alert-box {
      background: rgba(239, 68, 68, 0.12);
      border: 1px solid rgba(239, 68, 68, 0.4);
      border-radius: 10px;
      padding: 1rem 1.25rem;
      margin-bottom: 2rem;
    }
    .alert-title { font-weight: 700; color: #fca5a5; margin-bottom: 0.75rem; font-size: 1rem; }
    .exception-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 0.75rem; }
    .exc-card { background: #1e293b; border-radius: 8px; padding: 0.75rem; border: 1px solid #475569; }
    .exc-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
    .exc-date { font-size: 0.75rem; color: #94a3b8; }
    .exc-body { margin-bottom: 0.75rem; }
    .exc-body strong { display: block; font-size: 0.9rem; margin-bottom: 0.2rem; }
    .exc-body p { margin: 0; font-size: 0.8rem; color: #cbd5e1; }

    .work-section {
      background: #1e293b;
      border: 1px solid #334155;
      border-radius: 12px;
      padding: 1.25rem;
      margin-bottom: 2rem;
    }
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
      border-bottom: 1px solid #334155;
      padding-bottom: 0.75rem;
    }
    .section-header h3 { margin: 0; font-size: 1.15rem; color: #f8fafc; }
    .view-all { color: #38bdf8; font-size: 0.85rem; text-decoration: none; }
    .empty-state { padding: 1.5rem; text-align: center; color: #94a3b8; font-size: 0.9rem; }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1rem;
    }
    .task-card {
      background: #0f172a;
      border: 1px solid #334155;
      border-radius: 10px;
      padding: 1.2rem;
      display: flex;
      flex-direction: column;
    }
    .task-badge-row { display: flex; gap: 0.5rem; margin-bottom: 0.75rem; flex-wrap: wrap; }
    .badge {
      font-size: 0.75rem;
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      font-weight: 600;
    }
    .badge-pick { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
    .badge-pack { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
    .badge-load { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
    .badge-status { background: #334155; color: #cbd5e1; }
    .badge-danger { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .badge-high, .badge-urgent { background: rgba(239, 68, 68, 0.2); color: #f87171; }
    .badge-normal, .badge-low { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }

    .task-title { margin: 0 0 0.35rem 0; font-size: 1.05rem; color: #f8fafc; }
    .task-meta { margin: 0 0 0.25rem 0; font-size: 0.8rem; color: #94a3b8; }

    .progress-bar-container { margin: 1rem 0; }
    .progress-labels { display: flex; justify-content: space-between; font-size: 0.75rem; color: #94a3b8; margin-bottom: 0.3rem; }
    .progress-track { height: 6px; background: #334155; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; transition: width 0.3s ease; }
    .fill-pick { background: #3b82f6; }
    .fill-pack { background: #a855f7; }
    .fill-load { background: #10b981; }

    .task-actions { display: flex; gap: 0.5rem; margin-top: auto; padding-top: 0.5rem; }
    .spinner { border: 3px solid rgba(255,255,255,0.1); border-left-color: #38bdf8; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 2rem auto 0.5rem; }
    .loading-state { text-align: center; color: #94a3b8; padding: 3rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class WarehouseMyWorkComponent implements OnInit {
  work: MyWorkItem | null = null;
  loading = true;

  constructor(
    private fulfillmentService: WarehouseFulfillmentService,
    public roleService: RoleStateService
  ) {}

  ngOnInit(): void {
    this.loadMyWork();
  }

  loadMyWork(): void {
    this.loading = true;
    this.fulfillmentService.getMyWork().subscribe({
      next: (data) => {
        this.work = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
