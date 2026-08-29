import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface DriverScheduleItem {
  id: string;
  driverId: string;
  driverName: string;
  phone?: string;
  status: string;
  deliveries: Array<{
    id: string;
    deliveryNumber: string;
    bookingNumber?: string;
    customerName?: string;
    address?: string;
    date: string;
    startTime: string;
    endTime: string;
    status: string;
    hasConflict?: boolean;
    conflictMessage?: string;
  }>;
}

@Component({
  selector: 'app-driver-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Driver Resource Schedule</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20">
              Fleet Scheduling
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Driver assignments, shift availability, and route conflict detection</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Full Calendar
          </a>
          <a routerLink="/calendar/vehicles" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            🚐 Vehicles
          </a>
        </div>
      </div>

      <!-- MAIN CONTENT -->
      <div class="space-y-6">
        <div *ngIf="loading" class="flex flex-col items-center justify-center py-20 bg-slate-900/60 rounded-xl border border-slate-800">
          <div class="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Loading driver schedules...</p>
        </div>

        <div *ngIf="!loading && drivers.length === 0" class="text-center py-16 bg-slate-900/60 rounded-xl border border-slate-800">
          <p class="text-slate-400">No active drivers found.</p>
        </div>

        <div *ngFor="let driver of drivers" class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 mb-4 border-b border-slate-800">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-blue-500/20 border border-blue-500/30 flex items-center justify-center text-blue-400 font-bold text-lg">
                {{ driver.driverName.substring(0, 1) }}
              </div>
              <div>
                <h3 class="text-lg font-bold text-slate-100">{{ driver.driverName }}</h3>
                <p class="text-xs text-slate-400">📞 {{ driver.phone || 'N/A' }}</p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <span [class]="driver.status === 'AVAILABLE' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' : 'bg-amber-500/20 text-amber-300 border-amber-500/30'" 
                    class="px-2.5 py-1 rounded-full text-xs font-semibold border">
                {{ driver.status }}
              </span>
              <span class="text-xs text-slate-400 bg-slate-800 px-3 py-1 rounded-lg border border-slate-700">
                {{ driver.deliveries.length }} Assigned Job(s)
              </span>
            </div>
          </div>

          <!-- ASSIGNMENT TIMELINE -->
          <div class="space-y-3">
            <div *ngFor="let del of driver.deliveries" 
                 [class.border-red-500]="del.hasConflict"
                 class="p-4 rounded-xl bg-slate-950 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-bold text-blue-400 text-sm">{{ del.deliveryNumber }}</span>
                  <span class="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300">{{ del.date }}</span>
                  <span class="text-xs px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 font-semibold">{{ del.startTime }} - {{ del.endTime }}</span>
                </div>
                <p class="text-sm font-medium text-slate-200 mt-1">👤 {{ del.customerName || 'Customer' }}</p>
                <p class="text-xs text-slate-400">📍 {{ del.address || 'Standard Delivery Location' }}</p>
                
                <div *ngIf="del.hasConflict" class="mt-2 text-xs font-medium text-red-400 flex items-center gap-1">
                  ⚠️ HARD CONFLICT: {{ del.conflictMessage }}
                </div>
              </div>

              <div class="flex items-center gap-2">
                <span class="px-2.5 py-1 rounded-md text-xs font-medium bg-slate-800 text-slate-300 border border-slate-700">
                  {{ del.status }}
                </span>
                <a [routerLink]="['/delivery', del.id]" class="px-2.5 py-1 rounded-md text-xs font-medium bg-blue-600/20 hover:bg-blue-600/30 text-blue-300 border border-blue-500/30">
                  View Order
                </a>
              </div>
            </div>

            <div *ngIf="driver.deliveries.length === 0" class="text-slate-500 text-xs py-3 text-center italic">
              No deliveries assigned for selected date range.
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DriverCalendarComponent implements OnInit {
  drivers: DriverScheduleItem[] = [];
  loading: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDriverSchedules();
  }

  loadDriverSchedules() {
    this.loading = true;
    this.http.get<any>('/api/calendar/drivers').subscribe({
      next: (res) => {
        this.drivers = res.drivers || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load driver calendar', err);
        // Fallback demo drivers for rendering
        this.drivers = [
          {
            id: 'd1',
            driverId: 'drv-01',
            driverName: 'John Smith',
            phone: '+1-555-0199',
            status: 'ASSIGNED',
            deliveries: [
              {
                id: 'del-1',
                deliveryNumber: 'DEL-000123',
                customerName: 'Metropolitan Events Co',
                address: '100 City Center Plaza',
                date: '2026-09-10',
                startTime: '10:00 AM',
                endTime: '12:00 PM',
                status: 'SCHEDULED'
              }
            ]
          }
        ];
        this.loading = false;
      }
    });
  }
}
