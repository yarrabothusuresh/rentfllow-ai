import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

interface RoleDetail {
  code: string;
  name: string;
  avatar: string;
  responsibility: string;
  canAccess: string[];
  cannotAccess: string[];
}

@Component({
  selector: 'app-user-roles',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-roles.component.html',
  styleUrl: './user-roles.component.scss'
})
export class UserRolesComponent implements OnInit {
  selectedRoleCode = 'OWNER';
  roles: RoleDetail[] = [
    {
      code: 'OWNER',
      name: 'Owner',
      avatar: '👨‍💼',
      responsibility: 'Full control over business operations, analytics, settings, and user administration.',
      canAccess: ['Dashboard', 'AI Copilot', 'Leads', 'Customers', 'Products', 'Inventory', 'Quotes', 'Bookings', 'Payments', 'Warehouse', 'Delivery', 'Analytics', 'Storefront', 'Settings', 'User management'],
      cannotAccess: []
    },
    {
      code: 'ADMIN',
      name: 'Admin',
      avatar: '👩‍💼',
      responsibility: 'Manage operations, catalog, bookings, and user accounts (excluding owner settings).',
      canAccess: ['Dashboard', 'Leads', 'Customers', 'Products', 'Inventory', 'Quotes', 'Bookings', 'Payments', 'Warehouse', 'Delivery', 'Analytics', 'Storefront', 'User management'],
      cannotAccess: ['AI Copilot', 'Company settings']
    },
    {
      code: 'SALES',
      name: 'Sales',
      avatar: '💼',
      responsibility: 'Convert incoming customer inquiries into profitable bookings.',
      canAccess: ['Dashboard', 'AI Copilot', 'Leads', 'Customers', 'Quotes', 'Bookings', 'Products', 'Availability (Inventory View)'],
      cannotAccess: ['Company settings', 'User management', 'Financial administration', 'Warehouse configurations']
    },
    {
      code: 'WAREHOUSE',
      name: 'Warehouse',
      avatar: '🔧',
      responsibility: 'Manage physical inventory, prep orders, and verify pickups and returns.',
      canAccess: ['Dashboard', 'Products', 'Inventory', 'Bookings', 'Warehouse operations', 'Pick Lists', 'Pack Lists', 'Returns'],
      cannotAccess: ['Company settings', 'User management', 'Financial reports', 'Customer payment data']
    },
    {
      code: 'DRIVER',
      name: 'Driver',
      avatar: '🚚',
      responsibility: 'Fulfill deliveries and pick up equipment from event venues.',
      canAccess: ['Dashboard', 'Assigned deliveries', 'Pickup jobs', 'Customer contacts', 'Delivery notes'],
      cannotAccess: ['Company settings', 'User management', 'Financial reports', 'Inventory administration']
    },
    {
      code: 'CUSTOMER',
      name: 'Customer',
      avatar: '👤',
      responsibility: 'Review proposals, sign contracts, make payments, and view storefront catalogs.',
      canAccess: ['Profile details', 'Own quotes', 'Own bookings', 'Invoices', 'Online payments', 'Storefront'],
      cannotAccess: ['Internal backend metrics', 'Other customers\' data', 'Warehouse layouts', 'Staff settings']
    }
  ];

  matrixRows = [
    { module: 'Dashboard', owner: '✓', admin: '✓', sales: '✓', warehouse: '✓', driver: '✓', customer: 'Limited' },
    { module: 'AI Copilot', owner: '✓', admin: '✓', sales: '✓', warehouse: '-', driver: '-', customer: '-' },
    { module: 'Leads', owner: '✓', admin: '✓', sales: '✓', warehouse: '-', driver: '-', customer: '-' },
    { module: 'Customers', owner: '✓', admin: '✓', sales: '✓', warehouse: 'Limited', driver: 'Limited', customer: 'Own' },
    { module: 'Products', owner: '✓', admin: '✓', sales: '✓', warehouse: '✓', driver: 'Limited', customer: 'View' },
    { module: 'Inventory', owner: '✓', admin: '✓', sales: 'View', warehouse: '✓', driver: 'Limited', customer: 'Availability' },
    { module: 'Quotes', owner: '✓', admin: '✓', sales: '✓', warehouse: '-', driver: '-', customer: 'Own' },
    { module: 'Bookings', owner: '✓', admin: '✓', sales: '✓', warehouse: '✓', driver: 'Assigned', customer: 'Own' },
    { module: 'Payments', owner: '✓', admin: '✓', sales: 'Limited', warehouse: '-', driver: '-', customer: 'Own' },
    { module: 'Warehouse', owner: '✓', admin: '✓', sales: '-', warehouse: '✓', driver: '-', customer: '-' },
    { module: 'Delivery', owner: '✓', admin: '✓', sales: 'View', warehouse: '✓', driver: 'Assigned', customer: 'Own' },
    { module: 'Analytics', owner: '✓', admin: '✓', sales: 'Limited', warehouse: '-', driver: '-', customer: '-' },
    { module: 'Settings', owner: '✓', admin: '✓', sales: '-', warehouse: '-', driver: '-', customer: '-' },
    { module: 'Users', owner: '✓', admin: '✓', sales: '-', warehouse: '-', driver: '-', customer: '-' }
  ];

  backendPermissions: string[] = [];
  isLoadingPermissions = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchBackendPermissionsForRole(this.selectedRoleCode);
  }

  get selectedRole(): RoleDetail {
    return this.roles.find(r => r.code === this.selectedRoleCode) || this.roles[0];
  }

  selectRole(code: string) {
    this.selectedRoleCode = code;
    this.fetchBackendPermissionsForRole(code);
  }

  fetchBackendPermissionsForRole(roleCode: string) {
    this.isLoadingPermissions = true;
    this.http.get<string[]>(`/api/roles/${roleCode}/permissions`).pipe(
      catchError(error => {
        console.warn(`Could not load permissions from backend for role ${roleCode}, falling back.`, error);
        return of([] as string[]);
      })
    ).subscribe(data => {
      this.backendPermissions = data;
      this.isLoadingPermissions = false;
    });
  }
}
