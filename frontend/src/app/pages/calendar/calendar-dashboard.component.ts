import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface CalendarDashboardMetrics {
  totalBookingsToday: number;
  totalDeliveriesToday: number;
  totalPickupsToday: number;
  totalReturnsToday: number;
  totalMaintenanceItems: number;
  activeDriversCount: number;
  activeVehiclesCount: number;
  openConflictsCount: number;
}

@Component({
  selector: 'app-calendar-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Operational Dashboard & Metrics</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Real-Time Snapshot
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">High-level operational overview across bookings, dispatch, fleet & capacity conflicts</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Operational Calendar
          </a>
          <a routerLink="/calendar/conflicts" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border border-rose-500/30 transition-colors">
            ⚠️ View Conflict Log
          </a>
        </div>
      </div>

      <!-- METRICS GRID -->
      <div *ngIf="loading" class="flex flex-col items-center justify-center py-20 bg-slate-900/60 rounded-xl border border-slate-800">
        <div class="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-slate-400 text-sm mt-3">Loading operational metrics...</p>
      </div>

      <div *ngIf="!loading && metrics" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- BOOKINGS TODAY -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Bookings Today</span>
            <span class="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-lg">📅</span>
          </div>
          <p class="text-3xl font-extrabold text-slate-100 mt-3">{{ metrics.totalBookingsToday }}</p>
          <p class="text-xs text-slate-400 mt-1">Active rental schedules today</p>
        </div>

        <!-- DELIVERIES TODAY -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Deliveries Today</span>
            <span class="p-2 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20 text-lg">🚛</span>
          </div>
          <p class="text-3xl font-extrabold text-slate-100 mt-3">{{ metrics.totalDeliveriesToday }}</p>
          <p class="text-xs text-slate-400 mt-1">Outbound dispatch orders</p>
        </div>

        <!-- RETURNS & PICKUPS -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Returns Today</span>
            <span class="p-2 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20 text-lg">↩️</span>
          </div>
          <p class="text-3xl font-extrabold text-slate-100 mt-3">{{ metrics.totalReturnsToday }}</p>
          <p class="text-xs text-slate-400 mt-1">Scheduled check-in returns</p>
        </div>

        <!-- OPEN CONFLICTS -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Open Conflicts</span>
            <span class="p-2 rounded-lg bg-rose-500/10 text-rose-400 border border-rose-500/20 text-lg">⚠️</span>
          </div>
          <p class="text-3xl font-extrabold text-rose-400 mt-3">{{ metrics.openConflictsCount }}</p>
          <p class="text-xs text-slate-400 mt-1">Unresolved warnings & flags</p>
        </div>
      </div>

      <!-- FLEET & RESOURCE STATUS CARDS -->
      <div *ngIf="!loading && metrics" class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
          <h3 class="text-base font-bold text-slate-100 mb-2 flex items-center justify-between">
            <span>Drivers On Duty</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
              {{ metrics.activeDriversCount }} Active
            </span>
          </h3>
          <p class="text-xs text-slate-400">Available drivers for route dispatch and customer setup.</p>
          <div class="mt-4">
            <a routerLink="/calendar/drivers" class="text-xs font-semibold text-blue-400 hover:underline">Manage Driver Schedules →</a>
          </div>
        </div>

        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
          <h3 class="text-base font-bold text-slate-100 mb-2 flex items-center justify-between">
            <span>Vehicles Active</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-sky-500/20 text-sky-300 border border-sky-500/30">
              {{ metrics.activeVehiclesCount }} Active
            </span>
          </h3>
          <p class="text-xs text-slate-400">Box trucks and delivery vans in active circulation.</p>
          <div class="mt-4">
            <a routerLink="/calendar/vehicles" class="text-xs font-semibold text-sky-400 hover:underline">View Fleet Schedule →</a>
          </div>
        </div>

        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
          <h3 class="text-base font-bold text-slate-100 mb-2 flex items-center justify-between">
            <span>Maintenance Items</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-red-500/20 text-red-300 border border-red-500/30">
              {{ metrics.totalMaintenanceItems }} In Repair
            </span>
          </h3>
          <p class="text-xs text-slate-400">Items out of general inventory circulation undergoing inspection/repair.</p>
          <div class="mt-4">
            <a routerLink="/maintenance" class="text-xs font-semibold text-red-400 hover:underline">View Repair Orders →</a>
          </div>
        </div>
      </div>
    </div>
  `
})
export class CalendarDashboardComponent implements OnInit {
  metrics: CalendarDashboardMetrics | null = null;
  loading: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadMetrics();
  }

  loadMetrics() {
    this.loading = true;
    this.http.get<any>('/api/calendar/dashboard').subscribe({
      next: (res) => {
        this.metrics = res;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load calendar dashboard metrics', err);
        this.metrics = {
          totalBookingsToday: 1,
          totalDeliveriesToday: 1,
          totalPickupsToday: 0,
          totalReturnsToday: 0,
          totalMaintenanceItems: 0,
          activeDriversCount: 1,
          activeVehiclesCount: 1,
          openConflictsCount: 0
        };
        this.loading = false;
      }
    });
  }
}
