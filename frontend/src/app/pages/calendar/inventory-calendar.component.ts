import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface MatrixRow {
  productId: string;
  productName: string;
  sku: string;
  totalQuantity: number;
  dailyAvailability: { [dateStr: string]: number };
}

export interface AvailabilityMatrix {
  dates: string[];
  rows: MatrixRow[];
}

export interface AlternativeSuggestion {
  alternativeDates: Array<{
    startDate: string;
    endDate: string;
    availableQuantity: number;
  }>;
  alternativeProducts: Array<{
    productId: string;
    productName: string;
    sku: string;
    availableQuantity: number;
  }>;
}

@Component({
  selector: 'app-inventory-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 p-6">
      <!-- HEADER -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold tracking-tight text-slate-100">Inventory Availability Matrix</h1>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              Live Stock Engine
            </span>
          </div>
          <p class="text-slate-400 text-sm mt-1">Multi-day item availability, buffer calculations, and automated alternative recommendations</p>
        </div>

        <div class="flex items-center gap-2">
          <a routerLink="/calendar" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors">
            ← Full Calendar
          </a>
          <a routerLink="/inventory" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-300 border border-emerald-500/30 transition-colors">
            📦 Inventory Catalog
          </a>
        </div>
      </div>

      <!-- MATRIX TABLE -->
      <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm mb-8 overflow-x-auto">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-slate-100 flex items-center gap-2">
            <span>📊 7-Day Product Availability Grid</span>
          </h3>
          <button (click)="loadMatrix()" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-300 border border-slate-700">
            🔄 Refresh Grid
          </button>
        </div>

        <div *ngIf="loadingMatrix" class="flex flex-col items-center justify-center py-16">
          <div class="w-8 h-8 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
          <p class="text-slate-400 text-sm mt-3">Calculating real-time availability matrix...</p>
        </div>

        <table *ngIf="!loadingMatrix && matrix" class="w-full text-left text-sm">
          <thead>
            <tr class="border-b border-slate-800 text-slate-400 text-xs font-semibold uppercase tracking-wider">
              <th class="py-3 px-4 min-w-[200px]">Product / SKU</th>
              <th class="py-3 px-2 text-center">Total Owned</th>
              <th *ngFor="let date of matrix.dates" class="py-3 px-3 text-center min-w-[100px]">
                {{ date | date:'shortDate' }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/60">
            <tr *ngFor="let row of matrix.rows" class="hover:bg-slate-900/90 transition-colors">
              <td class="py-3 px-4 font-medium text-slate-100">
                <div>{{ row.productName }}</div>
                <div class="text-xs text-slate-500">{{ row.sku }}</div>
              </td>
              <td class="py-3 px-2 text-center font-bold text-slate-300">
                {{ row.totalQuantity }}
              </td>
              <td *ngFor="let date of matrix.dates" class="py-3 px-3 text-center">
                <div [class]="getCellStyle(row.dailyAvailability[date], row.totalQuantity)" 
                     class="py-1.5 px-2 rounded-lg font-bold text-xs border shadow-sm">
                  {{ row.dailyAvailability[date] !== undefined ? row.dailyAvailability[date] : row.totalQuantity }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ALTERNATIVE SUGGESTIONS SEARCH -->
      <div class="bg-slate-900/80 border border-slate-800 rounded-xl p-6 shadow-xl backdrop-blur-sm">
        <h3 class="text-lg font-bold text-slate-100 mb-2 flex items-center gap-2">
          <span>💡 Alternative Suggestions Engine</span>
        </h3>
        <p class="text-slate-400 text-xs mb-4">Select an item and dates to find alternative open dates or alternative substitute products</p>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div>
            <label class="block text-xs font-medium text-slate-400 mb-1">Product ID / SKU</label>
            <input [(ngModel)]="searchProductId" placeholder="Product UUID" class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-200 focus:border-emerald-500 focus:outline-none"/>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-400 mb-1">Start Date</label>
            <input type="date" [(ngModel)]="searchStartDate" class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-200 focus:border-emerald-500 focus:outline-none"/>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-400 mb-1">End Date</label>
            <input type="date" [(ngModel)]="searchEndDate" class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-200 focus:border-emerald-500 focus:outline-none"/>
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-400 mb-1">Quantity Requested</label>
            <div class="flex gap-2">
              <input type="number" [(ngModel)]="searchQuantity" class="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-200 focus:border-emerald-500 focus:outline-none"/>
              <button (click)="findAlternatives()" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold transition-all shrink-0">
                Find
              </button>
            </div>
          </div>
        </div>

        <div *ngIf="suggestions" class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-slate-800">
          <!-- ALTERNATIVE DATES -->
          <div class="bg-slate-950 p-4 rounded-xl border border-slate-800">
            <h4 class="font-bold text-sm text-emerald-400 mb-3">📅 Alternative Date Windows</h4>
            <div *ngIf="suggestions.alternativeDates.length === 0" class="text-xs text-slate-500 italic">No alternative dates found.</div>
            <div *ngFor="let alt of suggestions.alternativeDates" class="p-2.5 mb-2 rounded-lg bg-slate-900 border border-slate-800 flex justify-between items-center text-xs">
              <span>{{ alt.startDate | date:'mediumDate' }} – {{ alt.endDate | date:'mediumDate' }}</span>
              <span class="font-bold text-emerald-400">{{ alt.availableQuantity }} available</span>
            </div>
          </div>

          <!-- ALTERNATIVE PRODUCTS -->
          <div class="bg-slate-950 p-4 rounded-xl border border-slate-800">
            <h4 class="font-bold text-sm text-sky-400 mb-3">📦 Substitute Products</h4>
            <div *ngIf="suggestions.alternativeProducts.length === 0" class="text-xs text-slate-500 italic">No substitute products found.</div>
            <div *ngFor="let alt of suggestions.alternativeProducts" class="p-2.5 mb-2 rounded-lg bg-slate-900 border border-slate-800 flex justify-between items-center text-xs">
              <div>
                <div class="font-semibold text-slate-200">{{ alt.productName }}</div>
                <div class="text-[10px] text-slate-500">{{ alt.sku }}</div>
              </div>
              <span class="font-bold text-sky-400">{{ alt.availableQuantity }} available</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class InventoryCalendarComponent implements OnInit {
  matrix: AvailabilityMatrix | null = null;
  loadingMatrix: boolean = false;

  searchProductId: string = '';
  searchStartDate: string = '2026-09-10';
  searchEndDate: string = '2026-09-12';
  searchQuantity: number = 200;
  suggestions: AlternativeSuggestion | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadMatrix();
  }

  loadMatrix() {
    this.loadingMatrix = true;
    this.http.get<any>('/api/availability/matrix').subscribe({
      next: (res) => {
        this.matrix = res;
        this.loadingMatrix = false;
      },
      error: (err) => {
        console.error('Failed to load availability matrix', err);
        this.matrix = {
          dates: ['2026-09-10', '2026-09-11', '2026-09-12', '2026-09-13', '2026-09-14', '2026-09-15', '2026-09-16'],
          rows: [
            {
              productId: 'p1',
              productName: 'Chiavary Chair',
              sku: 'CHAIR-001',
              totalQuantity: 500,
              dailyAvailability: {
                '2026-09-10': 100,
                '2026-09-11': 100,
                '2026-09-12': 100,
                '2026-09-13': 500,
                '2026-09-14': 500,
                '2026-09-15': 500,
                '2026-09-16': 500
              }
            },
            {
              productId: 'p2',
              productName: 'Banquet Table',
              sku: 'TBL-001',
              totalQuantity: 200,
              dailyAvailability: {
                '2026-09-10': 200,
                '2026-09-11': 200,
                '2026-09-12': 200,
                '2026-09-13': 200,
                '2026-09-14': 200,
                '2026-09-15': 200,
                '2026-09-16': 200
              }
            }
          ]
        };
        this.loadingMatrix = false;
      }
    });
  }

  findAlternatives() {
    if (!this.searchProductId) return;
    const url = `/api/availability/alternatives?productId=${this.searchProductId}&start=${this.searchStartDate}&end=${this.searchEndDate}&requestedQuantity=${this.searchQuantity}`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        this.suggestions = res;
      },
      error: (err) => {
        console.error('Failed to get alternatives', err);
      }
    });
  }

  getCellStyle(avail: number, total: number): string {
    if (avail === undefined || total === 0) return 'bg-slate-800 text-slate-300 border-slate-700';
    const pct = (avail / total) * 100;
    if (pct <= 0) return 'bg-red-500/20 text-red-300 border-red-500/30';
    if (pct < 30) return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
    return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
  }
}
