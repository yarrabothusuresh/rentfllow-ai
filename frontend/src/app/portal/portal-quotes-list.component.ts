import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalQuote } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-quotes-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>📄 My Proposals & Quotes</h2>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="!isLoading && quotes.length === 0" class="alert alert-secondary">
        No active proposals found.
      </div>

      <div class="row g-4" *ngIf="!isLoading">
        <div class="col-md-6 col-lg-4" *ngFor="let q of quotes">
          <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
            <div>
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="font-monospace text-info">{{ q.quoteNumber }}</span>
                <span class="badge" [ngClass]="getStatusBadgeClass(q.status)">{{ q.status }}</span>
              </div>
              <h4 class="text-light font-weight-bold">{{ q.eventName || 'Proposal for Event' }}</h4>
              <p class="text-muted mb-1">📅 Valid Until: {{ q.validUntil }}</p>
              <h3 class="text-info font-weight-bold my-3">\${{ q.totalAmount | number:'1.2-2' }}</h3>
            </div>
            <div>
              <a [routerLink]="['/portal/quotes', q.id]" class="btn btn-info w-100">Review & Accept Proposal →</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalQuotesListComponent implements OnInit {
  quotes: CustomerPortalQuote[] = [];
  isLoading = true;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.portalService.getQuotes().subscribe({
      next: (data) => { this.quotes = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACCEPTED': return 'bg-success';
      case 'SENT': return 'bg-primary';
      case 'CHANGE_REQUESTED': return 'bg-warning text-dark';
      case 'EXPIRED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
