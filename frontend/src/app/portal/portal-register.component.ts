import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerPortalService } from '../services/customer-portal.service';

@Component({
  selector: 'app-portal-register',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="register-layout">
      <div class="register-card">
        <div class="card-header">
          <h2>Create Customer Account</h2>
          <p>Access your quotes, track live bookings, view invoices & claims</p>
        </div>

        <form (ngSubmit)="onRegister()">
          <div class="form-row">
            <div class="form-group">
              <label>First Name *</label>
              <input type="text" [(ngModel)]="firstName" name="firstName" required class="form-control" />
            </div>
            <div class="form-group">
              <label>Last Name *</label>
              <input type="text" [(ngModel)]="lastName" name="lastName" required class="form-control" />
            </div>
          </div>

          <div class="form-group">
            <label>Company / Organization</label>
            <input type="text" [(ngModel)]="company" name="company" placeholder="ABC Events LLC" class="form-control" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Email Address *</label>
              <input type="email" [(ngModel)]="email" name="email" required class="form-control" />
            </div>
            <div class="form-group">
              <label>Phone Number *</label>
              <input type="tel" [(ngModel)]="phone" name="phone" required class="form-control" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Password *</label>
              <input type="password" [(ngModel)]="password" name="password" required class="form-control" />
            </div>
            <div class="form-group">
              <label>Confirm Password *</label>
              <input type="password" [(ngModel)]="confirmPassword" name="confirmPassword" required class="form-control" />
            </div>
          </div>

          <div class="terms-group">
            <label>
              <input type="checkbox" [(ngModel)]="termsAccepted" name="terms" required />
              I accept the rental terms and conditions
            </label>
          </div>

          <div class="error-msg" *ngIf="errorMessage">{{ errorMessage }}</div>

          <button type="submit" class="btn-register" [disabled]="loading">
            {{ loading ? 'Creating Account...' : 'Register Account' }}
          </button>
        </form>

        <div class="card-footer">
          Already have a portal account? <a routerLink="/portal/login">Log in here</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .register-layout {
      min-height: 100vh;
      background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .register-card {
      background: white;
      border-radius: 16px;
      padding: 2.5rem;
      max-width: 520px;
      width: 100%;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2);
    }
    .card-header h2 {
      margin: 0;
      font-size: 1.75rem;
      color: #0f172a;
    }
    .card-header p {
      color: #64748b;
      margin: 0.5rem 0 1.5rem 0;
      font-size: 0.9rem;
    }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 0.35rem; }
    .form-control { width: 100%; padding: 0.65rem; border: 1px solid #cbd5e1; border-radius: 8px; box-sizing: border-box; font-size: 0.9rem; }
    .terms-group { margin: 1rem 0; font-size: 0.85rem; color: #475569; }
    .error-msg { background: #fee2e2; color: #991b1b; padding: 0.6rem; border-radius: 6px; font-size: 0.85rem; margin-bottom: 1rem; }
    .btn-register { width: 100%; background: #2563eb; color: white; border: none; padding: 0.85rem; border-radius: 8px; font-weight: 700; font-size: 1rem; cursor: pointer; }
    .card-footer { margin-top: 1.5rem; text-align: center; font-size: 0.85rem; color: #64748b; }
    .card-footer a { color: #2563eb; text-decoration: none; font-weight: 600; }
  `]
})
export class PortalRegisterComponent {
  firstName: string = '';
  lastName: string = '';
  company: string = '';
  email: string = '';
  phone: string = '';
  password: string = '';
  confirmPassword: string = '';
  termsAccepted: boolean = false;

  loading: boolean = false;
  errorMessage: string = '';

  constructor(private portalService: CustomerPortalService, private router: Router) {}

  onRegister(): void {
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const payload = {
      firstName: this.firstName,
      lastName: this.lastName,
      company: this.company,
      email: this.email,
      phone: this.phone,
      password: this.password,
      termsAccepted: this.termsAccepted
    };

    this.portalService.register(payload).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/portal/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.error || 'Registration failed. Please check your information.';
      }
    });
  }
}
