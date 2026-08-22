import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerRequest } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid py-3">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>💬 Support Messages & Inquiries</h2>
        <button class="btn btn-info" (click)="showNewRequestModal = true">+ Contact Support / Request</button>
      </div>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ successMessage }}
      </div>

      <div *ngIf="!isLoading && requests.length === 0" class="alert alert-secondary">
        No support requests or messages logged yet.
      </div>

      <div class="row g-4" *ngIf="!isLoading">
        <div class="col-md-6" *ngFor="let req of requests">
          <div class="card-glass p-4 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="badge bg-secondary">{{ req.requestType }}</span>
              <span class="badge" [ngClass]="getStatusBadgeClass(req.status)">{{ req.status }}</span>
            </div>
            <h4 class="text-light font-weight-bold mb-2">{{ req.subject }}</h4>
            <p class="text-muted mb-3">{{ req.message }}</p>
            <small class="text-muted d-block border-top border-secondary pt-2">Created: {{ req.createdAt | date:'medium' }}</small>
          </div>
        </div>
      </div>
    </div>

    <!-- New Request Modal -->
    <div class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.7);" *ngIf="showNewRequestModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-light border-secondary">
          <div class="modal-header border-secondary">
            <h5 class="modal-title">Submit Support Message / Question</h5>
            <button type="button" class="btn-close btn-close-white" (click)="showNewRequestModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label text-muted">Inquiry Category</label>
              <select class="form-select bg-black text-light border-secondary" [(ngModel)]="newType">
                <option value="DELIVERY_QUESTION">🚚 Delivery / Pickup Question</option>
                <option value="BOOKING_QUESTION">📅 Booking Order Inquiry</option>
                <option value="BILLING_QUESTION">💳 Billing & Payment Question</option>
                <option value="QUOTE_CHANGE">📄 Proposal / Quote Change</option>
                <option value="GENERAL">💬 General Inquiry</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label text-muted">Subject Title</label>
              <input type="text" class="form-control bg-black text-light border-secondary" [(ngModel)]="newSubject" placeholder="e.g. Delivery timing before 10 AM" required />
            </div>

            <div class="mb-3">
              <label class="form-label text-muted">Message Details</label>
              <textarea class="form-control bg-black text-light border-secondary" rows="4" [(ngModel)]="newMessage" placeholder="Write your question or request message here..."></textarea>
            </div>
          </div>
          <div class="modal-footer border-secondary">
            <button type="button" class="btn btn-secondary" (click)="showNewRequestModal = false">Cancel</button>
            <button type="button" class="btn btn-info px-4" (click)="submitRequest()" [disabled]="isSubmitting || !newSubject || !newMessage">
              {{ isSubmitting ? 'Submitting...' : 'Send Message' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalRequestsComponent implements OnInit {
  requests: CustomerRequest[] = [];
  isLoading = true;
  isSubmitting = false;
  showNewRequestModal = false;
  successMessage: string | null = null;

  newType: 'QUOTE_CHANGE' | 'DELIVERY_QUESTION' | 'BOOKING_QUESTION' | 'BILLING_QUESTION' | 'GENERAL' = 'DELIVERY_QUESTION';
  newSubject = '';
  newMessage = '';

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.isLoading = true;
    this.portalService.getRequests().subscribe({
      next: (data) => { this.requests = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  submitRequest(): void {
    if (!this.newSubject || !this.newMessage) return;
    this.isSubmitting = true;

    this.portalService.createRequest({
      type: this.newType,
      subject: this.newSubject,
      message: this.newMessage
    }).subscribe({
      next: (res) => {
        this.requests.unshift(res);
        this.isSubmitting = false;
        this.showNewRequestModal = false;
        this.newSubject = '';
        this.newMessage = '';
        this.successMessage = 'Your support message has been sent to our staff!';
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'bg-primary';
      case 'IN_PROGRESS': return 'bg-warning text-dark';
      case 'RESOLVED': return 'bg-success';
      case 'CLOSED': return 'bg-secondary';
      default: return 'bg-info';
    }
  }
}
