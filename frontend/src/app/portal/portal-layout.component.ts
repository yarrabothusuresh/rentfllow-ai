import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerAuthResponse } from '../models/customer-portal.models';

import { NotificationBellComponent } from '../pages/notifications/notification-bell/notification-bell.component';

@Component({
  selector: 'app-portal-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationBellComponent],
  templateUrl: './portal-layout.component.html',
  styleUrls: ['./portal-layout.component.css']
})
export class PortalLayoutComponent implements OnInit {
  customer: CustomerAuthResponse | null = null;

  constructor(
    private portalService: CustomerPortalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.portalService.currentCustomer$.subscribe(c => {
      this.customer = c;
    });
  }

  logout(): void {
    this.portalService.logout();
    this.router.navigate(['/portal/login']);
  }

  switchToAdmin(): void {
    this.router.navigate(['/dashboard']);
  }
}
