import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerProfile } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid py-3">
      <h2 class="mb-4">👤 Customer Account Profile</h2>

      <div *ngIf="isLoading" class="text-center py-5">
        <div class="spinner-border text-info" role="status"></div>
      </div>

      <div *ngIf="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ successMessage }}
      </div>
      <div *ngIf="errorMessage" class="alert alert-danger" role="alert">
        {{ errorMessage }}
      </div>

      <div *ngIf="!isLoading && profile" class="card-glass p-4">
        <form (ngSubmit)="saveProfile()">
          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label text-muted">First Name</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.firstName" name="firstName" required />
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Last Name</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.lastName" name="lastName" required />
            </div>
          </div>

          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label text-muted">Company / Organization Name</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.companyName" name="companyName" />
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Email Address (Primary)</label>
              <input type="email" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.email" name="email" readonly />
              <small class="text-muted">Email is linked to your login identity.</small>
            </div>
          </div>

          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <label class="form-label text-muted">Phone Number</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.phone" name="phone" />
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Alternate Phone</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.alternatePhone" name="alternatePhone" />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label text-muted">Billing Address</label>
            <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.billingAddress" name="billingAddress" placeholder="1234 Main St" />
          </div>

          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label text-muted">City</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.city" name="city" />
            </div>
            <div class="col-md-4">
              <label class="form-label text-muted">State</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.state" name="state" />
            </div>
            <div class="col-md-4">
              <label class="form-label text-muted">Zip Code</label>
              <input type="text" class="form-control bg-dark text-light border-secondary" [(ngModel)]="profile.zipCode" name="zipCode" />
            </div>
          </div>

          <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-info px-4" [disabled]="isSaving">
              {{ isSaving ? 'Saving Changes...' : 'Save Profile Updates' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .card-glass { background: #161b22; border: 1px solid #30363d; border-radius: 12px; }
  `]
})
export class PortalProfileComponent implements OnInit {
  profile: CustomerProfile | null = null;
  isLoading = true;
  isSaving = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.portalService.getProfile().subscribe({
      next: (data) => { this.profile = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  saveProfile(): void {
    if (!this.profile) return;
    this.isSaving = true;
    this.successMessage = null;
    this.errorMessage = null;

    this.portalService.updateProfile(this.profile).subscribe({
      next: (res) => {
        this.profile = res;
        this.isSaving = false;
        this.successMessage = 'Profile updated successfully!';
      },
      error: (err) => {
        this.isSaving = false;
        this.errorMessage = err.error?.error || 'Failed to update profile.';
      }
    });
  }
}
