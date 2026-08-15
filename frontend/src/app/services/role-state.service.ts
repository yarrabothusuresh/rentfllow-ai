import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type RoleType = 'OWNER' | 'ADMIN' | 'SALES' | 'WAREHOUSE' | 'DRIVER' | 'CUSTOMER';

export interface DemoUser {
  id: string;
  name: string;
  role: RoleType;
}

@Injectable({
  providedIn: 'root'
})
export class RoleStateService {
  private currentRoleSubject = new BehaviorSubject<RoleType>('OWNER');
  currentRole$ = this.currentRoleSubject.asObservable();

  private activePermissionsSubject = new BehaviorSubject<Set<string>>(new Set());
  activePermissions$ = this.activePermissionsSubject.asObservable();

  demoUsers: DemoUser[] = [
    { id: '11111111-1111-1111-1111-111111111111', name: 'John Anderson', role: 'OWNER' },
    { id: '22222222-2222-2222-2222-222222222222', name: 'Sarah Miller', role: 'ADMIN' },
    { id: '33333333-3333-3333-3333-333333333333', name: 'Mike Johnson', role: 'SALES' },
    { id: '44444444-4444-4444-4444-444444444444', name: 'Robert Smith', role: 'WAREHOUSE' },
    { id: '55555555-5555-5555-5555-555555555555', name: 'David Wilson', role: 'DRIVER' },
    { id: '66666666-6666-6666-6666-666666666666', name: 'Emily Brown', role: 'CUSTOMER' }
  ];

  // Map of static fallback permissions if backend is offline
  private fallbackPermissions: Record<RoleType, string[]> = {
    OWNER: [
      'DASHBOARD_VIEW', 'AI_COPILOT_USE', 'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_UPDATE',
      'LEAD_VIEW', 'LEAD_CREATE', 'LEAD_UPDATE', 'PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_UPDATE',
      'PRODUCT_DELETE', 'INVENTORY_VIEW', 'INVENTORY_UPDATE', 'INVENTORY_RESERVE', 'QUOTE_VIEW',
      'QUOTE_CREATE', 'QUOTE_UPDATE', 'QUOTE_SEND', 'BOOKING_VIEW', 'BOOKING_CREATE', 'BOOKING_UPDATE',
      'BOOKING_CANCEL', 'PAYMENT_VIEW', 'PAYMENT_CREATE', 'PAYMENT_REFUND', 'WAREHOUSE_VIEW',
      'WAREHOUSE_UPDATE', 'DELIVERY_VIEW', 'DELIVERY_UPDATE', 'ANALYTICS_VIEW', 'STOREFRONT_VIEW',
      'STOREFRONT_UPDATE', 'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DISABLE',
      'COMPANY_SETTINGS_VIEW', 'COMPANY_SETTINGS_UPDATE'
    ],
    ADMIN: [
      'DASHBOARD_VIEW', 'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_UPDATE',
      'LEAD_VIEW', 'LEAD_CREATE', 'LEAD_UPDATE', 'PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_UPDATE',
      'PRODUCT_DELETE', 'INVENTORY_VIEW', 'INVENTORY_UPDATE', 'INVENTORY_RESERVE', 'QUOTE_VIEW',
      'QUOTE_CREATE', 'QUOTE_UPDATE', 'QUOTE_SEND', 'BOOKING_VIEW', 'BOOKING_CREATE', 'BOOKING_UPDATE',
      'BOOKING_CANCEL', 'PAYMENT_VIEW', 'PAYMENT_CREATE', 'PAYMENT_REFUND', 'WAREHOUSE_VIEW',
      'WAREHOUSE_UPDATE', 'DELIVERY_VIEW', 'DELIVERY_UPDATE', 'ANALYTICS_VIEW', 'STOREFRONT_VIEW',
      'STOREFRONT_UPDATE', 'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DISABLE'
    ],
    SALES: [
      'DASHBOARD_VIEW', 'AI_COPILOT_USE', 'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_UPDATE',
      'LEAD_VIEW', 'LEAD_CREATE', 'LEAD_UPDATE', 'PRODUCT_VIEW', 'INVENTORY_VIEW', 'QUOTE_VIEW',
      'QUOTE_CREATE', 'QUOTE_UPDATE', 'QUOTE_SEND', 'BOOKING_VIEW', 'BOOKING_CREATE', 'BOOKING_UPDATE',
      'DELIVERY_VIEW'
    ],
    WAREHOUSE: [
      'DASHBOARD_VIEW', 'PRODUCT_VIEW', 'INVENTORY_VIEW', 'INVENTORY_UPDATE', 'BOOKING_VIEW',
      'WAREHOUSE_VIEW', 'WAREHOUSE_UPDATE', 'DELIVERY_VIEW'
    ],
    DRIVER: [
      'DASHBOARD_VIEW', 'BOOKING_VIEW', 'DELIVERY_VIEW', 'DELIVERY_UPDATE'
    ],
    CUSTOMER: [
      'STOREFRONT_VIEW', 'DASHBOARD_VIEW', 'CUSTOMER_VIEW', 'QUOTE_VIEW', 'BOOKING_VIEW',
      'PAYMENT_VIEW', 'PAYMENT_CREATE'
    ]
  };

  constructor(private http: HttpClient) {
    // Initial fetch for OWNER role
    this.fetchPermissionsForRole('OWNER');
  }

  getCurrentRole(): RoleType {
    return this.currentRoleSubject.value;
  }

  setRole(role: RoleType) {
    this.currentRoleSubject.next(role);
    this.fetchPermissionsForRole(role);
  }

  hasPermission(permission: string): boolean {
    return this.activePermissionsSubject.value.has(permission.toUpperCase());
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.includes(this.getCurrentRole());
  }

  fetchPermissionsForRole(role: RoleType) {
    const user = this.demoUsers.find(u => u.role === role);
    if (!user) return;

    this.http.get<any>(`/api/users/${user.id}/permissions`).pipe(
      catchError(error => {
        console.warn(`Backend fetch failed for user ${user.name}, using fallback.`, error);
        return of({
          permissions: this.fallbackPermissions[role]
        });
      })
    ).subscribe((data: any) => {
      const perms = new Set<string>(data.permissions || []);
      this.activePermissionsSubject.next(perms);
    });
  }
}
