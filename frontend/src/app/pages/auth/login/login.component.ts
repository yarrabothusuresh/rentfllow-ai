import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  email: string = '';
  password: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  returnUrl: string = '/dashboard';

  demoAccounts = [
    { label: '👑 Owner', email: 'owner@demo.local', role: 'OWNER' },
    { label: '⚡ Admin', email: 'admin@demo.local', role: 'ADMIN' },
    { label: '💼 Sales', email: 'sales@demo.local', role: 'SALES' },
    { label: '📦 Warehouse', email: 'warehouse@demo.local', role: 'WAREHOUSE' },
    { label: '🚚 Driver', email: 'driver@demo.local', role: 'DRIVER' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    if (this.authService.isAuthenticated()) {
      this.router.navigate([this.returnUrl]);
    }
  }

  useDemoAccount(account: { email: string; role: string }): void {
    this.email = account.email;
    this.password = 'ChangeMe123!';
    this.errorMessage = '';
  }

  onLogin(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please provide both email and password.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate([this.returnUrl]);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Invalid email or password.';
        } else if (err.status === 429) {
          this.errorMessage = 'Too many failed login attempts. Please wait a moment.';
        } else {
          this.errorMessage = err.error?.message || 'Authentication failed. Please check server connection.';
        }
      }
    });
  }
}
