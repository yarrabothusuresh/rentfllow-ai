import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface VehicleScheduleItem {
  id: string;
  vehicleNumber: string;
  name: string;
  type: string;
  capacity: number;
  status: string;
  deliveries: Array<{
    id: string;
    deliveryNumber: string;
    driverName?: string;
    date: string;
    startTime: string;
    endTime: string;
    status: string;
    hasConflict?: boolean;
  }>;
}

@Component({
  selector: 'app-vehicle-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Vehicle Fleet Schedule</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-sky-500/10 text-sky-400 border border-sky-500/20">
              Fleet Capacity
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Vehicle assignment, loading schedule, and double-booking conflict control</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Full Calendar
          </a>
          <a routerLink="/calendar/drivers" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            🚛 Drivers
          </a>
        </div>
      </div>

      <!-- MAIN CONTENT -->
      <div class="space-y-6">
        <div *ngIf="loading" class="flex flex-col items-center justify-center py-20 bg-slate-900/60 rounded-xl border border-slate-800">
          <div class="w-8 h-8 border-4 border-sky-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Loading vehicle schedules...</p>
        </div>

        <div *ngIf="!loading && vehicles.length === 0" class="text-center py-16 bg-slate-900/60 rounded-xl border border-slate-800">
          <p class="text-slate-400">No active vehicles registered.</p>
        </div>

        <div *ngFor="let veh of vehicles" class="bg-slate-900/80 border border-slate-800 rounded-xl p-5 shadow-xl backdrop-blur-sm">
          <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 mb-4 border-b border-slate-800">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-sky-500/20 border border-sky-500/30 flex items-center justify-center text-sky-400 font-bold text-base">
                🚐
              </div>
              <div>
                <h3 class="text-lg font-bold text-slate-100">{{ veh.name }} <span class="text-xs text-sky-400 font-normal">({{ veh.vehicleNumber }})</span></h3>
                <p class="text-xs text-slate-400">Type: {{ veh.type || 'Standard Truck' }} • Max Capacity: {{ veh.capacity }} items</p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <span [class]="veh.status === 'AVAILABLE' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' : 'bg-sky-500/20 text-sky-300 border-sky-500/30'" 
                    class="px-2.5 py-1 rounded-full text-xs font-semibold border">
                {{ veh.status }}
              </span>
              <span class="text-xs text-slate-400 bg-slate-800 px-3 py-1 rounded-lg border border-slate-700">
                {{ veh.deliveries.length }} Active Route(s)
              </span>
            </div>
          </div>

          <!-- VEHICLE ASSIGNMENTS -->
          <div class="space-y-3">
            <div *ngFor="let del of veh.deliveries" 
                 [class.border-red-500]="del.hasConflict"
                 class="p-4 rounded-xl bg-slate-950 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-bold text-sky-400 text-sm">{{ del.deliveryNumber }}</span>
                  <span class="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-300">{{ del.date }}</span>
                  <span class="text-xs px-2 py-0.5 rounded bg-sky-500/20 text-sky-300 font-semibold">{{ del.startTime }} - {{ del.endTime }}</span>
                </div>
                <p class="text-xs text-slate-300 mt-1">🚛 Assigned Driver: <span class="text-emerald-400 font-medium">{{ del.driverName || 'Unassigned' }}</span></p>
                <div *ngIf="del.hasConflict" class="mt-2 text-xs font-medium text-red-400 flex items-center gap-1">
                  ⚠️ HARD CONFLICT: Vehicle double-booked in this time window!
                </div>
              </div>

              <div class="flex items-center gap-2">
                <span class="px-2.5 py-1 rounded-md text-xs font-medium bg-slate-800 text-slate-300 border border-slate-700">
                  {{ del.status }}
                </span>
                <a [routerLink]="['/delivery', del.id]" class="px-2.5 py-1 rounded-md text-xs font-medium bg-sky-600/20 hover:bg-sky-600/30 text-sky-300 border border-sky-500/30">
                  View Route
                </a>
              </div>
            </div>

            <div *ngIf="veh.deliveries.length === 0" class="text-slate-500 text-xs py-3 text-center italic">
              No routes scheduled for this vehicle today.
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class VehicleCalendarComponent implements OnInit {
  vehicles: VehicleScheduleItem[] = [];
  loading: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadVehicleSchedules();
  }

  loadVehicleSchedules() {
    this.loading = true;
    this.http.get<any>('/api/calendar/vehicles').subscribe({
      next: (res) => {
        this.vehicles = res.vehicles || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load vehicle calendar', err);
        this.vehicles = [
          {
            id: 'v1',
            vehicleNumber: 'VAN-01',
            name: 'Ford Transit 350',
            type: 'Box Truck',
            capacity: 500,
            status: 'AVAILABLE',
            deliveries: [
              {
                id: 'del-1',
                deliveryNumber: 'DEL-000123',
                driverName: 'John Smith',
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
