import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface ConflictRecord {
  id: string;
  tenantId: string;
  conflictType: string;
  severity: string;
  resourceName: string;
  resourceId?: string;
  referenceType?: string;
  referenceId?: string;
  message: string;
  suggestedAction?: string;
  status: string;
  overrideReason?: string;
  overriddenBy?: string;
  overriddenAt?: string;
  createdAt: string;
}

@Component({
  selector: 'app-conflict-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Operational Conflict & Warning Audit</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">
              Audit & Override Log
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Review operational capacity warnings, double-booking flags, and warning overrides</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Full Calendar
          </a>
          <a routerLink="/calendar/dashboard" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            📊 Dashboard
          </a>
        </div>
      </div>

      <!-- MAIN TABLE & LIST -->
      <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <h3 class="text-lg font-bold text-slate-100 flex items-center gap-2">
            <span>⚠️ Conflict Log Records</span>
          </h3>

          <div class="flex items-center gap-2">
            <select [(ngModel)]="statusFilter" (change)="applyFilter()" class="px-3 py-1.5 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-200 focus:border-rose-500 focus:outline-none">
              <option value="ALL">All Statuses</option>
              <option value="OPEN">OPEN (Requires Attention)</option>
              <option value="OVERRIDDEN">OVERRIDDEN</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          </div>
        </div>

        <div *ngIf="loading" class="flex flex-col items-center justify-center py-16">
          <div class="w-8 h-8 border-4 border-rose-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Loading conflicts...</p>
        </div>

        <div *ngIf="!loading && filteredConflicts.length === 0" class="text-center py-16 text-slate-400">
          <p class="text-slate-300 font-semibold">No operational conflicts found.</p>
          <p class="text-xs text-slate-500 mt-1">All resource schedules and warehouse capacities are operating within limits.</p>
        </div>

        <div *ngIf="!loading && filteredConflicts.length > 0" class="space-y-4">
          <div *ngFor="let c of filteredConflicts" 
               [class.border-red-500]="c.severity === 'HARD_CONFLICT'"
               [class.border-amber-500]="c.severity === 'WARNING'"
               class="p-5 rounded-xl bg-slate-950 border border-slate-800 flex flex-col md:flex-row md:items-start justify-between gap-4 shadow-md">
            
            <div class="space-y-2">
              <div class="flex items-center gap-2">
                <span [class]="getSeverityBadgeClass(c.severity)" class="px-2.5 py-0.5 rounded-md text-xs font-bold uppercase tracking-wider">
                  {{ c.severity }}
                </span>
                <span class="px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-xs font-semibold uppercase">
                  {{ c.conflictType }}
                </span>
                <span class="text-xs text-slate-500">{{ c.createdAt | date:'medium' }}</span>
              </div>

              <h4 class="text-base font-semibold text-slate-100">{{ c.message }}</h4>
              <p *ngIf="c.suggestedAction" class="text-xs text-slate-400">💡 Recommended Action: {{ c.suggestedAction }}</p>

              <div *ngIf="c.status === 'OVERRIDDEN'" class="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-xs text-amber-300">
                <p class="font-bold">Overridden by {{ c.overriddenBy || 'Operations Manager' }} at {{ c.overriddenAt | date:'short' }}</p>
                <p class="mt-0.5 italic">Reason: "{{ c.overrideReason }}"</p>
              </div>
            </div>

            <div class="flex flex-col items-end gap-2 shrink-0">
              <span [class]="c.status === 'OPEN' ? 'bg-red-500/20 text-red-300 border-red-500/30' : 'bg-slate-800 text-slate-300 border-slate-700'" 
                    class="px-2.5 py-1 rounded-full text-xs font-semibold border">
                {{ c.status }}
              </span>

              <button *ngIf="c.status === 'OPEN' && c.severity === 'WARNING'" 
                      (click)="openOverrideModal(c)"
                      class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-600/20 hover:bg-amber-600/30 text-amber-300 border border-amber-500/40 transition-colors">
                Override Warning
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- OVERRIDE MODAL -->
      <div *ngIf="selectedConflictForOverride" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-lg w-full shadow-2xl space-y-4">
          <h3 class="text-lg font-bold text-slate-100 flex items-center gap-2">
            <span>⚠️ Override Operational Warning</span>
          </h3>

          <p class="text-xs text-slate-400">
            You are overriding an operational capacity warning: <span class="text-amber-300 font-medium">{{ selectedConflictForOverride.message }}</span>
          </p>

          <div>
            <label class="block text-xs font-medium text-slate-300 mb-1">Reason for Override (Audit Log)</label>
            <textarea [(ngModel)]="overrideReasonInput" rows="3" placeholder="e.g., Arranged 2 additional temporary warehouse staff for staging peak..." class="w-full p-3 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-100 focus:border-amber-500 focus:outline-none"></textarea>
          </div>

          <div>
            <label class="block text-xs font-medium text-slate-300 mb-1">Overridden By (Operations Manager Name)</label>
            <input [(ngModel)]="overriddenByInput" placeholder="Operations Manager" class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-100 focus:border-amber-500 focus:outline-none"/>
          </div>

          <div class="flex items-center justify-end gap-3 pt-2">
            <button (click)="selectedConflictForOverride = null" class="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300">
              Cancel
            </button>
            <button (click)="submitOverride()" class="px-4 py-2 rounded-lg bg-amber-600 hover:bg-amber-500 text-white text-xs font-bold">
              Confirm Override
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ConflictDashboardComponent implements OnInit {
  conflicts: ConflictRecord[] = [];
  filteredConflicts: ConflictRecord[] = [];
  loading: boolean = false;
  statusFilter: string = 'ALL';

  selectedConflictForOverride: ConflictRecord | null = null;
  overrideReasonInput: string = '';
  overriddenByInput: string = 'Operations Manager';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadConflicts();
  }

  loadConflicts() {
    this.loading = true;
    this.http.get<any>('/api/calendar/conflicts').subscribe({
      next: (res) => {
        this.conflicts = res.conflicts || res || [];
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load conflicts', err);
        this.conflicts = [];
        this.applyFilter();
        this.loading = false;
      }
    });
  }

  applyFilter() {
    if (this.statusFilter === 'ALL') {
      this.filteredConflicts = [...this.conflicts];
    } else {
      this.filteredConflicts = this.conflicts.filter(c => c.status === this.statusFilter);
    }
  }

  getSeverityBadgeClass(severity: string): string {
    switch (severity) {
      case 'HARD_CONFLICT': return 'bg-red-500/20 text-red-300 border border-red-500/40';
      case 'WARNING': return 'bg-amber-500/20 text-amber-300 border border-amber-500/40';
      default: return 'bg-blue-500/20 text-blue-300 border border-blue-500/40';
    }
  }

  openOverrideModal(conflict: ConflictRecord) {
    this.selectedConflictForOverride = conflict;
    this.overrideReasonInput = '';
  }

  submitOverride() {
    if (!this.selectedConflictForOverride || !this.overrideReasonInput) return;

    const payload = {
      overrideReason: this.overrideReasonInput,
      overriddenBy: this.overriddenByInput
    };

    this.http.post<any>(`/api/calendar/conflicts/${this.selectedConflictForOverride.id}/override`, payload).subscribe({
      next: (res) => {
        this.selectedConflictForOverride = null;
        this.loadConflicts();
      },
      error: (err) => {
        console.error('Failed to override conflict warning', err);
      }
    });
  }
}
