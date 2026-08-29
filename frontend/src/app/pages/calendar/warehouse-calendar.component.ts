import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface WarehouseCapacityMetrics {
  warehouseName: string;
  dailyPickCapacity: number;
  currentPicks: number;
  dailyPackCapacity: number;
  currentPacks: number;
  dailyCheckinCapacity: number;
  currentCheckins: number;
}

export interface WarehouseTaskItem {
  id: string;
  orderNumber: string;
  bookingNumber?: string;
  customerName?: string;
  type: string;
  scheduledDate: string;
  status: string;
  itemCount: number;
  priority: string;
}

@Component({
  selector: 'app-warehouse-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Warehouse Operational Schedule</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-orange-500/10 text-orange-400 border border-orange-500/20">
              Pick / Pack / Check-in Throughput
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Staging tasks, pick list prep, return check-ins, and daily capacity monitoring</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Full Calendar
          </a>
          <a routerLink="/warehouse" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-orange-600/20 hover:bg-orange-600/30 text-orange-300 border border-orange-500/30 transition-colors">
            🏭 Warehouse Hub
          </a>
        </div>
      </div>

      <!-- CAPACITY PROGRESS CARDS -->
      <div *ngIf="capacity" class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <!-- PICK CAPACITY -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Daily Pick Capacity</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
              {{ capacity.currentPicks }} / {{ capacity.dailyPickCapacity }} Orders
            </span>
          </div>
          <div class="w-full bg-slate-950 rounded-full h-3 border border-slate-800 overflow-hidden mt-3">
            <div [style.width.%]="getPercentage(capacity.currentPicks, capacity.dailyPickCapacity)" 
                 [class.bg-red-500]="isOverCapacity(capacity.currentPicks, capacity.dailyPickCapacity)"
                 [class.bg-blue-500]="!isOverCapacity(capacity.currentPicks, capacity.dailyPickCapacity)"
                 class="h-full transition-all duration-500"></div>
          </div>
          <p *ngIf="isOverCapacity(capacity.currentPicks, capacity.dailyPickCapacity)" class="text-xs text-red-400 font-medium mt-2">
            ⚠️ Warning: Exceeds standard daily pick capacity!
          </p>
        </div>

        <!-- PACK CAPACITY -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Daily Pack Capacity</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-orange-500/20 text-orange-300 border border-orange-500/30">
              {{ capacity.currentPacks }} / {{ capacity.dailyPackCapacity }} Orders
            </span>
          </div>
          <div class="w-full bg-slate-950 rounded-full h-3 border border-slate-800 overflow-hidden mt-3">
            <div [style.width.%]="getPercentage(capacity.currentPacks, capacity.dailyPackCapacity)" 
                 [class.bg-red-500]="isOverCapacity(capacity.currentPacks, capacity.dailyPackCapacity)"
                 [class.bg-orange-500]="!isOverCapacity(capacity.currentPacks, capacity.dailyPackCapacity)"
                 class="h-full transition-all duration-500"></div>
          </div>
          <p *ngIf="isOverCapacity(capacity.currentPacks, capacity.dailyPackCapacity)" class="text-xs text-red-400 font-medium mt-2">
            ⚠️ Warning: Exceeds standard daily pack capacity!
          </p>
        </div>

        <!-- CHECKIN CAPACITY -->
        <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Daily Check-in Capacity</span>
            <span class="text-xs font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              {{ capacity.currentCheckins }} / {{ capacity.dailyCheckinCapacity }} Orders
            </span>
          </div>
          <div class="w-full bg-slate-950 rounded-full h-3 border border-slate-800 overflow-hidden mt-3">
            <div [style.width.%]="getPercentage(capacity.currentCheckins, capacity.dailyCheckinCapacity)" 
                 [class.bg-red-500]="isOverCapacity(capacity.currentCheckins, capacity.dailyCheckinCapacity)"
                 [class.bg-emerald-500]="!isOverCapacity(capacity.currentCheckins, capacity.dailyCheckinCapacity)"
                 class="h-full transition-all duration-500"></div>
          </div>
          <p *ngIf="isOverCapacity(capacity.currentCheckins, capacity.dailyCheckinCapacity)" class="text-xs text-red-400 font-medium mt-2">
            ⚠️ Warning: Exceeds standard daily check-in capacity!
          </p>
        </div>
      </div>

      <!-- SCHEDULED WAREHOUSE TASKS -->
      <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
        <h3 class="text-lg font-bold text-slate-100 mb-4 flex items-center gap-2">
          <span>📋 Scheduled Staging & Return Check-in Tasks</span>
        </h3>

        <div *ngIf="loading" class="flex flex-col items-center justify-center py-16">
          <div class="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Loading warehouse tasks...</p>
        </div>

        <div *ngIf="!loading && tasks.length === 0" class="text-center py-12 text-slate-400">
          No warehouse staging tasks scheduled for this date.
        </div>

        <div *ngIf="!loading && tasks.length > 0" class="space-y-3">
          <div *ngFor="let t of tasks" class="p-4 rounded-xl bg-slate-950 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2">
                <span class="font-bold text-orange-400 text-sm">{{ t.orderNumber }}</span>
                <span class="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300">{{ t.scheduledDate | date:'mediumDate' }}</span>
                <span class="text-xs px-2 py-0.5 rounded bg-orange-500/20 text-orange-300 font-semibold uppercase">{{ t.type }}</span>
              </div>
              <p class="text-sm text-slate-200 mt-1">👤 Customer: <span class="font-medium text-slate-100">{{ t.customerName || 'N/A' }}</span></p>
              <p class="text-xs text-slate-400">📦 Total Items: {{ t.itemCount }} | Priority: {{ t.priority }}</p>
            </div>

            <div class="flex items-center gap-2">
              <span class="px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                {{ t.status }}
              </span>
              <a [routerLink]="['/warehouse/orders', t.id]" class="px-2.5 py-1 rounded-md text-xs font-medium bg-orange-600/20 hover:bg-orange-600/30 text-orange-300 border border-orange-500/30">
                Open Task
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class WarehouseCalendarComponent implements OnInit {
  capacity: WarehouseCapacityMetrics | null = null;
  tasks: WarehouseTaskItem[] = [];
  loading: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadWarehouseSchedule();
  }

  loadWarehouseSchedule() {
    this.loading = true;
    this.http.get<any>('/api/calendar/warehouse').subscribe({
      next: (res) => {
        this.capacity = res.capacity || {
          warehouseName: 'Main Warehouse',
          dailyPickCapacity: 20,
          currentPicks: 5,
          dailyPackCapacity: 20,
          currentPacks: 4,
          dailyCheckinCapacity: 20,
          currentCheckins: 2
        };
        this.tasks = res.tasks || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load warehouse schedule', err);
        this.capacity = {
          warehouseName: 'Main Warehouse',
          dailyPickCapacity: 20,
          currentPicks: 5,
          dailyPackCapacity: 20,
          currentPacks: 4,
          dailyCheckinCapacity: 20,
          currentCheckins: 2
        };
        this.tasks = [];
        this.loading = false;
      }
    });
  }

  getPercentage(current: number, capacity: number): number {
    if (!capacity || capacity === 0) return 0;
    const pct = Math.round((current / capacity) * 100);
    return Math.min(pct, 100);
  }

  isOverCapacity(current: number, capacity: number): boolean {
    return current > capacity;
  }
}
