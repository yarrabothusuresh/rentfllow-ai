import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';

@Component({
  selector: 'app-portal-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './portal-login.component.html',
  styleUrls: ['./portal-login.component.css']
})
export class PortalLoginComponent {
  email = 'customer@abcevents.demo';
  password = 'demo';
  errorMessage: string | null = null;
  isLoading = false;

  constructor(
    private portalService: CustomerPortalService,
    private router: Router
  ) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter email and password.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.portalService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/portal/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Invalid customer portal login credentials.';
      }
    });
  }

  fillDemoCustomerA(): void {
    this.email = 'customer@abcevents.demo';
    this.password = 'demo';
    this.onLogin();
  }

  fillDemoCustomerB(): void {
    this.email = 'customer.b@xyzevents.demo';
    this.password = 'demo';
    this.onLogin();
  }
}
