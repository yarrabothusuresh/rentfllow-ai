import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InventoryAvailabilityService, InventorySummary, InventoryConflict } from '../../services/inventory-availability.service';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="inventory-dashboard-container">
      <header class="page-header">
        <div>
          <h1>📦 Inventory Control Center</h1>
          <p class="subtitle">Real-time availability tracking, active reservations, and conflict resolution</p>
        </div>
        <div class="header-actions">
          <a routerLink="/inventory/availability" class="btn btn-primary">📅 Check Availability</a>
          <a routerLink="/inventory/reservations" class="btn btn-secondary">📋 Reservations</a>
          <a routerLink="/inventory/conflicts" class="btn btn-danger-outline" *ngIf="conflicts.length > 0">
            ⚠️ Conflicts ({{ conflicts.length }})
          </a>
        </div>
      </header>

      <!-- Metric Cards Grid -->
      <div class="metrics-grid">
        <div class="metric-card card-total">
          <div class="metric-icon">🏢</div>
          <div class="metric-data">
            <span class="metric-label">Total Inventory Units</span>
            <span class="metric-value">{{ summary?.totalUnits || 0 }}</span>
            <span class="metric-sub">{{ summary?.totalProducts || 0 }} Catalog Products</span>
          </div>
        </div>

        <div class="metric-card card-available">
          <div class="metric-icon">🟢</div>
          <div class="metric-data">
            <span class="metric-label">Available Units</span>
            <span class="metric-value">{{ summary?.availableUnits || 0 }}</span>
            <span class="metric-sub">Ready for Instant Booking</span>
          </div>
        </div>

        <div class="metric-card card-reserved">
          <div class="metric-icon">📅</div>
          <div class="metric-data">
            <span class="metric-label">Reserved Units</span>
            <span class="metric-value">{{ summary?.reservedUnits || 0 }}</span>
            <span class="metric-sub">Committed to Confirmed Orders</span>
          </div>
        </div>

        <div class="metric-card card-maintenance">
          <div class="metric-icon">🔧</div>
          <div class="metric-data">
            <span class="metric-label">Maintenance & Repair</span>
            <span class="metric-value">{{ summary?.maintenanceUnits || 0 }}</span>
            <span class="metric-sub">Currently Out of Service</span>
          </div>
        </div>

        <div class="metric-card card-damaged">
          <div class="metric-icon">❌</div>
          <div class="metric-data">
            <span class="metric-label">Damaged / Lost</span>
            <span class="metric-value">{{ (summary?.damagedUnits || 0) + (summary?.lostUnits || 0) }}</span>
            <span class="metric-sub">Quarantined Inventory</span>
          </div>
        </div>
      </div>

      <!-- Conflicts Alert Section -->
      <div class="alert-section danger-alert" *ngIf="conflicts.length > 0">
        <div class="alert-header">
          <span class="alert-title">⚠️ {{ conflicts.length }} Active Inventory Conflict(s) Detected</span>
          <a routerLink="/inventory/conflicts" class="btn btn-sm btn-danger">Resolve Now</a>
        </div>
        <p>Shortages exist between confirmed customer event dates and physical warehouse quantity pools.</p>
      </div>

      <!-- Quick Navigation Panels -->
      <div class="panels-grid">
        <div class="panel-card">
          <div class="panel-header">
            <h2>📅 Date-Based Availability Calendar</h2>
            <a routerLink="/inventory/availability" class="link-action">Open Grid &rarr;</a>
          </div>
          <p>Query real-time available vs reserved unit balances for any specific event date or custom rental range.</p>
        </div>

        <div class="panel-card">
          <div class="panel-header">
            <h2>📋 Active Equipment Reservations</h2>
            <a routerLink="/inventory/reservations" class="link-action">View All &rarr;</a>
          </div>
          <p>Manage confirmed booking holds, manual quotes, maintenance freezes, and manual releases.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inventory-dashboard-container {
      padding: 1.5rem;
      max-width: 1400px;
      margin: 0 auto;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }
    .page-header h1 {
      font-size: 1.8rem;
      margin: 0;
      color: #0f172a;
    }
    .subtitle {
      color: #64748b;
      margin-top: 0.25rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
    .btn {
      padding: 0.6rem 1.2rem;
      border-radius: 6px;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      cursor: pointer;
      border: 1px solid transparent;
    }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-danger-outline { border-color: #ef4444; color: #ef4444; background: white; }
    .btn-danger { background: #ef4444; color: white; }
    .btn-sm { padding: 0.4rem 0.8rem; font-size: 0.85rem; }
    
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1.25rem;
      margin-bottom: 2rem;
    }
    .metric-card {
      background: white;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.06);
      border-left: 4px solid #cbd5e1;
    }
    .card-total { border-left-color: #3b82f6; }
    .card-available { border-left-color: #22c55e; }
    .card-reserved { border-left-color: #f59e0b; }
    .card-maintenance { border-left-color: #8b5cf6; }
    .card-damaged { border-left-color: #ef4444; }
    
    .metric-icon { font-size: 2rem; }
    .metric-data { display: flex; flex-direction: column; }
    .metric-label { font-size: 0.85rem; color: #64748b; font-weight: 500; }
    .metric-value { font-size: 1.8rem; font-weight: 700; color: #0f172a; line-height: 1.2; }
    .metric-sub { font-size: 0.75rem; color: #94a3b8; margin-top: 0.2rem; }

    .danger-alert {
      background: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 8px;
      padding: 1rem 1.25rem;
      margin-bottom: 2rem;
    }
    .alert-header { display: flex; justify-content: space-between; align-items: center; }
    .alert-title { font-weight: 700; color: #991b1b; }

    .panels-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
      gap: 1.5rem;
    }
    .panel-card {
      background: white;
      border-radius: 10px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    }
    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
    .panel-header h2 { font-size: 1.2rem; margin: 0; color: #0f172a; }
    .link-action { color: #3b82f6; text-decoration: none; font-weight: 600; }
  `]
})
export class InventoryDashboardComponent implements OnInit {
  summary: InventorySummary | null = null;
  conflicts: InventoryConflict[] = [];

  constructor(private inventoryService: InventoryAvailabilityService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.inventoryService.getDashboardSummary().subscribe(data => this.summary = data);
    this.inventoryService.getConflicts().subscribe(c => this.conflicts = c);
  }
}
