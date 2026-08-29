import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ConflictItem {
  type: string;
  severity: string;
  resource: string;
  resourceId?: string;
  requested?: number;
  available?: number;
  conflictingReference?: string;
  message: string;
  suggestedAction?: string;
}

export interface ConflictResponse {
  valid: boolean;
  hasConflict: boolean;
  hardConflicts: ConflictItem[];
  warnings: ConflictItem[];
}

@Component({
  selector: 'app-conflict-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="conflictData && conflictData.hasConflict" class="conflict-wrapper my-4">
      <!-- HARD CONFLICT BANNER -->
      <div *ngIf="conflictData.hardConflicts && conflictData.hardConflicts.length > 0" 
           class="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 mb-3 shadow-lg">
        <div class="flex items-center gap-2 font-semibold text-lg mb-2">
          <svg class="w-6 h-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
          </svg>
          <span>Scheduling Conflict Detected (Hard Block)</span>
        </div>
        <div *ngFor="let item of conflictData.hardConflicts" class="ml-8 mb-2 text-sm">
          <p class="font-medium text-red-300">{{ item.message }}</p>
          <p *ngIf="item.suggestedAction" class="text-xs text-red-400/80 mt-0.5">💡 {{ item.suggestedAction }}</p>
          <div class="flex gap-2 mt-2" *ngIf="item.conflictingReference">
            <button (click)="viewReference.emit(item.conflictingReference)" 
                    class="px-2.5 py-1 text-xs rounded bg-red-500/20 hover:bg-red-500/30 text-red-200 border border-red-500/40">
              View Conflicting Record ({{ item.conflictingReference }})
            </button>
          </div>
        </div>
      </div>

      <!-- WARNING BANNER -->
      <div *ngIf="conflictData.warnings && conflictData.warnings.length > 0" 
           class="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400 mb-3 shadow-lg">
        <div class="flex items-center gap-2 font-semibold text-lg mb-2">
          <svg class="w-6 h-6 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
          </svg>
          <span>Operational Capacity Warning</span>
        </div>
        <div *ngFor="let item of conflictData.warnings" class="ml-8 mb-2 text-sm">
          <p class="font-medium text-amber-300">{{ item.message }}</p>
          <p *ngIf="item.suggestedAction" class="text-xs text-amber-400/80 mt-0.5">💡 {{ item.suggestedAction }}</p>
          <div class="mt-2" *ngIf="allowOverride">
            <button (click)="requestOverride.emit(item)" 
                    class="px-3 py-1 text-xs rounded bg-amber-500/20 hover:bg-amber-500/30 text-amber-200 border border-amber-500/40 font-medium">
              Override Warning (Log Reason)
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ConflictAlertComponent {
  @Input() conflictData: ConflictResponse | null = null;
  @Input() allowOverride: boolean = false;
  @Output() viewReference = new EventEmitter<string>();
  @Output() requestOverride = new EventEmitter<ConflictItem>();
}
