import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerPortalService } from '../services/customer-portal.service';
import { CustomerPortalDashboard } from '../models/customer-portal.models';

@Component({
  selector: 'app-portal-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './portal-dashboard.component.html',
  styleUrls: ['./portal-dashboard.component.css']
})
export class PortalDashboardComponent implements OnInit {
  dashboard: CustomerPortalDashboard | null = null;
  isLoading = true;
  errorMessage: string | null = null;

  constructor(private portalService: CustomerPortalService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.portalService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load customer portal dashboard.';
        this.isLoading = false;
      }
    });
  }
}
