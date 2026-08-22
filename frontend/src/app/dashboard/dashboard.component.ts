import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { BusinessProfile } from '../models/business-profile.model';
import { RoleStateService, RoleType } from '../services/role-state.service';

import { NotificationBellComponent } from '../pages/notifications/notification-bell/notification-bell.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule, NotificationBellComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  isCollapsed = false;
  toastVisible = false;
  
  businessProfile!: BusinessProfile;
  tempProfile!: BusinessProfile;
  isEditModalOpen = false;
  currentRole: RoleType = 'OWNER';

  constructor(
    private profileService: ProfileService,
    private roleStateService: RoleStateService,
    private router: Router
  ) {}

  ngOnInit() {
    this.profileService.getProfile().subscribe(profile => {
      this.businessProfile = profile;
    });
    this.roleStateService.currentRole$.subscribe(role => {
      this.currentRole = role;
    });
  }

  switchRole(role: any) {
    this.roleStateService.setRole(role as RoleType);
    if (role === 'CUSTOMER') {
      this.router.navigate(['/portal/dashboard']);
    }
  }

  hasPermission(permission: string): boolean {
    return this.roleStateService.hasPermission(permission);
  }

  hasAnyPermissionInOperations(): boolean {
    return this.hasPermission('LEAD_VIEW') || this.hasPermission('CUSTOMER_VIEW') || this.hasPermission('QUOTE_VIEW') || this.hasPermission('BOOKING_VIEW');
  }

  hasAnyPermissionInFulfillment(): boolean {
    return this.hasPermission('INVENTORY_VIEW') || this.hasPermission('PRODUCT_VIEW') || this.hasPermission('WAREHOUSE_VIEW') || this.hasPermission('DELIVERY_VIEW');
  }

  hasAnyPermissionInFinanceAdmin(): boolean {
    return this.hasPermission('PAYMENT_VIEW') || this.hasPermission('ANALYTICS_VIEW') || this.hasPermission('STOREFRONT_VIEW') || this.hasPermission('USER_VIEW') || this.hasPermission('COMPANY_SETTINGS_VIEW');
  }
  
  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }
  
  showFutureToast() {
    this.toastVisible = true;
    setTimeout(() => {
      this.toastVisible = false;
    }, 3000);
  }

  openEditModal() {
    this.tempProfile = { ...this.businessProfile };
    this.isEditModalOpen = true;
  }

  closeEditModal() {
    this.isEditModalOpen = false;
  }

  saveProfile() {
    this.businessProfile = { ...this.tempProfile };
    this.isEditModalOpen = false;
  }
}

