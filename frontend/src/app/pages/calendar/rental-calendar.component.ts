import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

export interface CalendarEvent {
  id: string;
  tenantId: string;
  eventType: string;
  referenceType: string;
  referenceId: string;
  title: string;
  start: string;
  end: string;
  resourceType?: string;
  resourceId?: string;
  resourceName?: string;
  status: string;
  location?: string;
  customerName?: string;
  driverName?: string;
  vehicleName?: string;
  notes?: string;
}

@Component({
  selector: 'app-rental-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER & NAV BAR -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Operational Rental Calendar</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Live Backend Engine
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Centralized schedule across Bookings, Deliveries, Pickups, Returns, Maintenance & Fleet</p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <!-- NAVIGATION LINKS TO SUB-CALENDARS -->
          <a routerLink="/calendar/dashboard" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            📊 Dashboard
          </a>
          <a routerLink="/calendar/drivers" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            🚛 Driver Schedule
          </a>
          <a routerLink="/calendar/vehicles" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            🚐 Vehicle Schedule
          </a>
          <a routerLink="/calendar/warehouse" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            🏭 Warehouse Schedule
          </a>
          <a routerLink="/calendar/inventory" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            📦 Availability Matrix
          </a>
          <a routerLink="/calendar/conflicts" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border border-rose-500/30 transition-colors">
            ⚠️ Conflict Log
          </a>
        </div>
      </div>

      <!-- CONTROLS & VIEWS TOOLBAR -->
      <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-4 mb-6 shadow-xl backdrop-blur-sm">
        <div class="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4">
          <!-- DATE CONTROLS -->
          <div class="flex items-center gap-3">
            <button (click)="navigateDate(-1)" class="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
            </button>
            <button (click)="goToToday()" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200 border border-slate-700">
              Today
            </button>
            <button (click)="navigateDate(1)" class="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
            </button>
            <span class="text-lg font-semibold text-slate-100 ml-2">
              {{ currentDateLabel }}
            </span>
          </div>

          <!-- VIEW MODE SWITCHER -->
          <div class="flex items-center gap-1 bg-slate-950 p-1 rounded-lg border border-slate-800">
            <button *ngFor="let mode of ['MONTH', 'WEEK', 'DAY', 'AGENDA']"
                    (click)="setViewMode(mode)"
                    [class.bg-emerald-600]="viewMode === mode"
                    [class.text-white]="viewMode === mode"
                    [class.text-slate-400]="viewMode !== mode"
                    class="px-3 py-1.5 rounded-md text-xs font-semibold transition-all">
              {{ mode }}
            </button>
          </div>
        </div>

        <!-- EVENT TYPE FILTER CHIPS -->
        <div class="mt-4 pt-4 border-t border-slate-800/80 flex flex-wrap items-center gap-2">
          <span class="text-xs font-medium text-slate-400 mr-2">Filter Events:</span>
          <button (click)="toggleFilter('ALL')" 
                  [class.bg-emerald-500]="isFilterActive('ALL')" 
                  [class.text-slate-950]="isFilterActive('ALL')" 
                  class="px-2.5 py-1 rounded-full text-xs font-medium bg-slate-800 text-slate-300 hover:bg-slate-700 transition-colors">
            All
          </button>
          <button *ngFor="let t of availableEventTypes"
                  (click)="toggleFilter(t.key)"
                  [class.opacity-40]="!isFilterActive(t.key)"
                  [style.backgroundColor]="t.bgColor"
                  [style.color]="t.textColor"
                  class="px-2.5 py-1 rounded-full text-xs font-medium transition-all shadow-sm">
            {{ t.label }}
          </button>
        </div>
      </div>

      <!-- MAIN CALENDAR DISPLAY -->
      <div class="bg-slate-900/60 border border-slate-800 rounded-xl p-6 shadow-2xl backdrop-blur-sm min-h-[500px]">
        <div *ngIf="loading" class="flex flex-col items-center justify-center py-20">
          <div class="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Fetching calendar events from server...</p>
        </div>

        <div *ngIf="!loading && filteredEvents.length === 0" class="text-center py-20">
          <div class="w-16 h-16 bg-slate-800/50 rounded-full flex items-center justify-center mx-auto mb-3 text-slate-500">
            📅
          </div>
          <h3 class="text-slate-300 font-semibold">No operational events scheduled</h3>
          <p class="text-slate-500 text-xs mt-1">Try selecting a different date range or adjusting your event filters.</p>
        </div>

        <div *ngIf="!loading && filteredEvents.length > 0">
          <!-- AGENDA / LIST VIEW -->
          <div class="space-y-3">
            <div *ngFor="let ev of filteredEvents"
                 (click)="openEventDetail(ev)"
                 class="group relative p-4 rounded-xl border border-slate-800/80 hover:border-slate-700 bg-slate-950/80 hover:bg-slate-900/90 transition-all cursor-pointer shadow-md flex flex-col md:flex-row md:items-center justify-between gap-4">
              
              <div class="flex items-start gap-4">
                <!-- BADGE -->
                <div [class]="getBadgeStyle(ev.eventType)" class="px-2.5 py-1 rounded-lg text-xs font-bold uppercase tracking-wider shrink-0 mt-0.5">
                  {{ ev.eventType }}
                </div>
                <div>
                  <h4 class="text-base font-semibold text-slate-100 group-hover:text-emerald-400 transition-colors">
                    {{ ev.title }}
                  </h4>
                  <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400 mt-1">
                    <span class="flex items-center gap-1">
                      ⏰ {{ ev.start | date:'mediumDate' }} {{ ev.start | date:'shortTime' }} – {{ ev.end | date:'shortTime' }}
                    </span>
                    <span *ngIf="ev.customerName" class="flex items-center gap-1">
                      👤 {{ ev.customerName }}
                    </span>
                    <span *ngIf="ev.driverName && ev.driverName !== 'Unassigned'" class="flex items-center gap-1 text-emerald-400 font-medium">
                      🚛 {{ ev.driverName }}
                    </span>
                    <span *ngIf="ev.vehicleName && ev.vehicleName !== 'Unassigned'" class="flex items-center gap-1 text-sky-400 font-medium">
                      🚐 {{ ev.vehicleName }}
                    </span>
                    <span *ngIf="ev.location" class="flex items-center gap-1">
                      📍 {{ ev.location }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="flex items-center gap-3 shrink-0">
                <span class="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                  {{ ev.status }}
                </span>
                <span class="text-slate-500 group-hover:text-slate-300 transition-colors">→</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class RentalCalendarComponent implements OnInit {
  events: CalendarEvent[] = [];
  filteredEvents: CalendarEvent[] = [];
  loading: boolean = false;
  viewMode: string = 'WEEK';
  currentDate: Date = new Date();
  activeFilters: Set<string> = new Set(['ALL']);

  availableEventTypes = [
    { key: 'BOOKING', label: 'Bookings', bgColor: 'rgba(16, 185, 129, 0.2)', textColor: '#34d399' },
    { key: 'DELIVERY', label: 'Deliveries', bgColor: 'rgba(59, 130, 246, 0.2)', textColor: '#60a5fa' },
    { key: 'PICKUP', label: 'Pickups', bgColor: 'rgba(168, 85, 247, 0.2)', textColor: '#c084fc' },
    { key: 'RETURN', label: 'Returns', bgColor: 'rgba(234, 179, 8, 0.2)', textColor: '#fde047' },
    { key: 'MAINTENANCE', label: 'Maintenance', bgColor: 'rgba(239, 68, 68, 0.2)', textColor: '#f87171' },
    { key: 'WAREHOUSE_TASK', label: 'Warehouse', bgColor: 'rgba(249, 115, 22, 0.2)', textColor: '#fb923c' },
    { key: 'EVENT', label: 'Events', bgColor: 'rgba(14, 165, 233, 0.2)', textColor: '#38bdf8' }
  ];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadCalendarEvents();
  }

  get currentDateLabel(): string {
    const month = this.currentDate.toLocaleString('default', { month: 'long' });
    const year = this.currentDate.getFullYear();
    return `${month} ${year}`;
  }

  loadCalendarEvents() {
    this.loading = true;
    const startStr = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), 1).toISOString();
    const endStr = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 0, 23, 59, 59).toISOString();

    this.http.get<any>(`/api/calendar?start=${startStr}&end=${endStr}`).subscribe({
      next: (res) => {
        this.events = res.events || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load calendar events', err);
        this.loading = false;
      }
    });
  }

  setViewMode(mode: string) {
    this.viewMode = mode;
  }

  navigateDate(direction: number) {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + direction, 1);
    this.loadCalendarEvents();
  }

  goToToday() {
    this.currentDate = new Date();
    this.loadCalendarEvents();
  }

  toggleFilter(key: string) {
    if (key === 'ALL') {
      this.activeFilters.clear();
      this.activeFilters.add('ALL');
    } else {
      this.activeFilters.delete('ALL');
      if (this.activeFilters.has(key)) {
        this.activeFilters.delete(key);
      } else {
        this.activeFilters.add(key);
      }
      if (this.activeFilters.size === 0) {
        this.activeFilters.add('ALL');
      }
    }
    this.applyFilters();
  }

  isFilterActive(key: string): boolean {
    return this.activeFilters.has(key);
  }

  applyFilters() {
    if (this.activeFilters.has('ALL')) {
      this.filteredEvents = [...this.events];
    } else {
      this.filteredEvents = this.events.filter(e => this.activeFilters.has(e.eventType));
    }
  }

  getBadgeStyle(eventType: string): string {
    switch (eventType) {
      case 'BOOKING': return 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30';
      case 'DELIVERY': return 'bg-blue-500/20 text-blue-300 border border-blue-500/30';
      case 'PICKUP': return 'bg-purple-500/20 text-purple-300 border border-purple-500/30';
      case 'RETURN': return 'bg-amber-500/20 text-amber-300 border border-amber-500/30';
      case 'MAINTENANCE': return 'bg-red-500/20 text-red-300 border border-red-500/30';
      case 'WAREHOUSE_TASK': return 'bg-orange-500/20 text-orange-300 border border-orange-500/30';
      default: return 'bg-slate-800 text-slate-300 border border-slate-700';
    }
  }

  openEventDetail(ev: CalendarEvent) {
    switch (ev.eventType) {
      case 'BOOKING':
        this.router.navigate(['/bookings', ev.referenceId]);
        break;
      case 'DELIVERY':
      case 'PICKUP':
        this.router.navigate(['/delivery', ev.referenceId]);
        break;
      case 'RETURN':
        this.router.navigate(['/returns', ev.referenceId]);
        break;
      case 'MAINTENANCE':
        this.router.navigate(['/maintenance', ev.referenceId]);
        break;
      case 'WAREHOUSE_TASK':
        this.router.navigate(['/warehouse/orders', ev.referenceId]);
        break;
      default:
        break;
    }
  }
}
