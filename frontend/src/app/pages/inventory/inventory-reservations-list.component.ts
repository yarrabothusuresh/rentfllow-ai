import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InventoryAvailabilityService, InventoryReservation } from '../../services/inventory-availability.service';

@Component({
  selector: 'app-inventory-reservations-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="reservations-container">
      <header class="page-header">
        <div>
          <h1>📋 Inventory Reservations</h1>
          <p class="subtitle">View, search, filter, and manually manage equipment holds and reservations</p>
        </div>
        <a routerLink="/inventory/dashboard" class="btn btn-secondary">&larr; Back to Dashboard</a>
      </header>

      <!-- Toolbar Filters -->
      <div class="filter-card">
        <div class="filter-row">
          <div class="field-group">
            <label>Filter by Status</label>
            <select [(ngModel)]="selectedStatus" (change)="loadReservations()" class="form-control">
              <option value="ALL">All Statuses</option>
              <option value="CONFIRMED">Confirmed / Reserved</option>
              <option value="PENDING">Pending</option>
              <option value="HOLD">Hold</option>
              <option value="RELEASED">Released</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="EXPIRED">Expired</option>
            </select>
          </div>

          <div class="field-group flex-grow">
            <label>Search Reservations</label>
            <input type="text" [(ngModel)]="searchQuery" (keyup.enter)="loadReservations()" placeholder="Search product, booking, or reservation ID..." class="form-control" />
          </div>

          <div class="field-group">
            <label>&nbsp;</label>
            <button (click)="loadReservations()" class="btn btn-primary">🔍 Filter</button>
          </div>
        </div>
      </div>

      <!-- Reservations List Table -->
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>Reservation ID</th>
              <th>Product</th>
              <th>Quantity</th>
              <th>Start Date / Time</th>
              <th>End Date / Time</th>
              <th>Status</th>
              <th>Created By</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of reservations">
              <td><code>{{ r.id.substring(0, 8) }}...</code></td>
              <td class="font-semibold">{{ r.productName || 'Product (' + r.productId.substring(0,6) + ')' }}</td>
              <td class="font-bold text-amber">{{ r.quantity }} units</td>
              <td>{{ r.startDateTime | date:'medium' }}</td>
              <td>{{ r.endDateTime | date:'medium' }}</td>
              <td>
                <span class="status-pill" [ngClass]="'status-' + r.status.toLowerCase()">
                  {{ r.status }}
                </span>
              </td>
              <td>{{ r.createdBy || 'System' }}</td>
              <td>
                <button *ngIf="r.status === 'RESERVED' || r.status === 'CONFIRMED' || r.status === 'PENDING' || r.status === 'HOLD'"
                        (click)="release(r.id)" class="btn btn-xs btn-outline-danger">
                  Release
                </button>
                <span *ngIf="r.status === 'RELEASED' || r.status === 'CANCELLED' || r.status === 'EXPIRED'" class="text-gray">
                  Completed
                </span>
              </td>
            </tr>
            <tr *ngIf="reservations.length === 0">
              <td colspan="8" class="text-center py-4 text-gray">No reservations match the specified filters.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .reservations-container { padding: 1.5rem; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .page-header h1 { font-size: 1.8rem; margin: 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; }
    .btn { padding: 0.6rem 1.2rem; border-radius: 6px; font-weight: 600; text-decoration: none; cursor: pointer; border: 1px solid transparent; }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-secondary { background: #64748b; color: white; }
    .btn-outline-danger { border-color: #ef4444; color: #ef4444; background: white; }
    .btn-xs { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
    
    .filter-card { background: white; padding: 1.25rem; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 1.5rem; }
    .filter-row { display: flex; gap: 1rem; align-items: flex-end; }
    .field-group { display: flex; flex-direction: column; gap: 0.4rem; }
    .flex-grow { flex: 1; }
    .field-group label { font-size: 0.85rem; font-weight: 600; color: #475569; }
    .form-control { padding: 0.55rem 0.8rem; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.9rem; }
    
    .table-card { background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); overflow: hidden; }
    .data-table { width: 100%; border-collapse: collapse; text-align: left; }
    .data-table th, .data-table td { padding: 1rem; border-bottom: 1px solid #f1f5f9; }
    .data-table th { background: #f8fafc; font-size: 0.85rem; color: #475569; text-transform: uppercase; }
    .text-amber { color: #d97706; }
    .font-semibold { font-weight: 600; }
    .font-bold { font-weight: 700; }
    .text-gray { color: #94a3b8; font-size: 0.85rem; }
    
    .status-pill { padding: 0.25rem 0.65rem; border-radius: 12px; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; }
    .status-confirmed, .status-reserved { background: #dbeafe; color: #1e40af; }
    .status-pending, .status-hold { background: #fef3c7; color: #92400e; }
    .status-released { background: #dcfce7; color: #166534; }
    .status-cancelled, .status-expired { background: #fee2e2; color: #991b1b; }
  `]
})
export class InventoryReservationsListComponent implements OnInit {
  selectedStatus: string = 'ALL';
  searchQuery: string = '';
  reservations: InventoryReservation[] = [];

  constructor(private inventoryService: InventoryAvailabilityService) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.inventoryService.getReservations(this.selectedStatus, this.searchQuery)
      .subscribe(data => this.reservations = data);
  }

  release(id: string): void {
    if (confirm('Are you sure you want to release this reservation?')) {
      this.inventoryService.releaseReservation(id).subscribe(() => this.loadReservations());
    }
  }
}
