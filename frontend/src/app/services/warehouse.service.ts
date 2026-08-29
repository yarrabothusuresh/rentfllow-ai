import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RoleStateService } from './role-state.service';

export interface WarehouseOrderItem {
  id: string;
  warehouseOrderId: string;
  bookingItemId: string;
  productId: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  locationSnapshot: string;
  quantityRequired: number;
  quantityPicked: number;
  quantityPacked: number;
  status: 'PENDING' | 'PICKED' | 'PARTIALLY_PICKED' | 'PACKED' | 'SHORT' | 'DAMAGED';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WarehouseOrder {
  id: string;
  tenantId: string;
  bookingId: string;
  bookingNumber?: string;
  eventId: string;
  eventName?: string;
  eventDate?: string;
  venueName?: string;
  customerId: string;
  customerName?: string;
  orderNumber: string;
  scheduledDate?: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'PENDING' | 'READY_TO_PICK' | 'PICKING' | 'PICKED' | 'PACKING' | 'PACKED' | 'READY_FOR_DELIVERY' | 'CANCELLED';
  customerFacingStatus?: string;
  assignedTo?: string;
  notes?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
  completedAt?: string;
  totalQuantityRequired: number;
  totalQuantityPicked: number;
  totalQuantityPacked: number;
  pickingProgressPercentage: number;
  items: WarehouseOrderItem[];
}

export interface WarehouseDashboard {
  todaysOrders: number;
  readyToPick: number;
  picking: number;
  packing: number;
  readyForDelivery: number;
  shortItems: number;
  totalActiveWorkOrders: number;
  ordersInPicking: number;
  ordersInPacking: number;
  readyForDeliveryOrders: number;
  shortageOrdersCount: number;
  todaysPriorities: WarehouseOrder[];
  urgentOrders: WarehouseOrder[];
  recentActivity: any[];
}

export interface WarehouseLocation {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class WarehouseService {
  private apiUrl = '/api/warehouse';

  constructor(
    private http: HttpClient,
    private roleState: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Tenant-ID': '00000000-0000-0000-0000-000000000001',
      'X-User-Role': this.roleState.getCurrentRole()
    });
  }

  getDashboard(): Observable<WarehouseDashboard> {
    return this.http.get<WarehouseDashboard>(`${this.apiUrl}/dashboard`, { headers: this.getHeaders() })
      .pipe(catchError(() => of(this.getMockDashboard())));
  }

  getOrders(status?: string, priority?: string, search?: string): Observable<WarehouseOrder[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (priority) params = params.set('priority', priority);
    if (search) params = params.set('search', search);

    return this.http.get<WarehouseOrder[]>(`${this.apiUrl}/orders`, { headers: this.getHeaders(), params })
      .pipe(catchError(() => of(this.getMockOrders())));
  }

  getOrderById(id: string): Observable<WarehouseOrder> {
    return this.http.get<WarehouseOrder>(`${this.apiUrl}/orders/${id}`, { headers: this.getHeaders() })
      .pipe(catchError(() => of(this.getMockOrders().find(o => o.id === id) || this.getMockOrders()[0])));
  }

  createOrderFromBooking(bookingId: string): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(`${this.apiUrl}/orders/from-booking/${bookingId}`, {}, { headers: this.getHeaders() });
  }

  startPicking(id: string, userId?: string): Observable<WarehouseOrder> {
    const params = userId ? new HttpParams().set('userId', userId) : new HttpParams();
    return this.http.post<WarehouseOrder>(`${this.apiUrl}/orders/${id}/start-picking`, {}, { headers: this.getHeaders(), params });
  }

  pickItem(orderId: string, itemId: string, quantity: number, notes?: string, isShortage?: boolean): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(
      `${this.apiUrl}/orders/${orderId}/items/${itemId}/pick`,
      { quantity, notes, isShortage },
      { headers: this.getHeaders() }
    );
  }

  completePicking(orderId: string, confirmShortage: boolean = false, notes?: string): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(
      `${this.apiUrl}/orders/${orderId}/complete-picking`,
      { confirmShortage, notes },
      { headers: this.getHeaders() }
    );
  }

  startPacking(orderId: string): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(`${this.apiUrl}/orders/${orderId}/start-packing`, {}, { headers: this.getHeaders() });
  }

  packItem(orderId: string, itemId: string, quantity: number, notes?: string): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(
      `${this.apiUrl}/orders/${orderId}/items/${itemId}/pack`,
      { quantity, notes },
      { headers: this.getHeaders() }
    );
  }

  completePacking(orderId: string, confirmShortage: boolean = false, notes?: string): Observable<WarehouseOrder> {
    return this.http.post<WarehouseOrder>(
      `${this.apiUrl}/orders/${orderId}/complete-packing`,
      { confirmShortage, notes },
      { headers: this.getHeaders() }
    );
  }

  assignOrder(orderId: string, userId: string): Observable<WarehouseOrder> {
    return this.http.patch<WarehouseOrder>(
      `${this.apiUrl}/orders/${orderId}/assign`,
      { userId },
      { headers: this.getHeaders() }
    );
  }

  getLocations(): Observable<WarehouseLocation[]> {
    return this.http.get<WarehouseLocation[]>(`${this.apiUrl}/locations`, { headers: this.getHeaders() })
      .pipe(catchError(() => of(this.getMockLocations())));
  }

  getWarehouses(): Observable<WarehouseLocation[]> {
    return this.getLocations();
  }

  // Fallback Mock Data for UI robustness
  private getMockOrders(): WarehouseOrder[] {
    return [
      {
        id: 'w1111111-1111-1111-1111-111111111123',
        tenantId: 'tenant-evergreen',
        bookingId: 'b0000000-0000-0000-0000-000000000123',
        bookingNumber: 'BOOK-000123',
        eventId: 'e4444444-4444-4444-4444-444444444444',
        eventName: 'Wedding Reception',
        eventDate: '2026-08-30T10:00:00',
        venueName: 'Grand Ballroom',
        customerId: 'c3333333-3333-3333-3333-333333333333',
        customerName: 'ABC Events LLC',
        orderNumber: 'WH-000123',
        scheduledDate: '2026-08-30T08:00:00',
        priority: 'HIGH',
        status: 'READY_TO_PICK',
        customerFacingStatus: 'PREPARING',
        assignedTo: 'John Warehouse',
        notes: 'Ensure pristine gold finish on Chiavari chairs.',
        totalQuantityRequired: 130,
        totalQuantityPicked: 0,
        totalQuantityPacked: 0,
        pickingProgressPercentage: 0,
        items: [
          {
            id: 'item-1',
            warehouseOrderId: 'w1111111-1111-1111-1111-111111111123',
            bookingItemId: 'bitem-1',
            productId: 'p-1',
            productNameSnapshot: 'Chiavari Chair',
            skuSnapshot: 'CHI-001',
            locationSnapshot: 'A-01-03',
            quantityRequired: 100,
            quantityPicked: 0,
            quantityPacked: 0,
            status: 'PENDING'
          },
          {
            id: 'item-2',
            warehouseOrderId: 'w1111111-1111-1111-1111-111111111123',
            bookingItemId: 'bitem-2',
            productId: 'p-2',
            productNameSnapshot: 'Round Table',
            skuSnapshot: 'TBL-060',
            locationSnapshot: 'B-02-01',
            quantityRequired: 10,
            quantityPicked: 0,
            quantityPacked: 0,
            status: 'PENDING'
          },
          {
            id: 'item-3',
            warehouseOrderId: 'w1111111-1111-1111-1111-111111111123',
            bookingItemId: 'bitem-3',
            productId: 'p-3',
            productNameSnapshot: 'White Linen',
            skuSnapshot: 'LIN-WHT',
            locationSnapshot: 'C-01-02',
            quantityRequired: 20,
            quantityPicked: 0,
            quantityPacked: 0,
            status: 'PENDING'
          }
        ]
      },
      {
        id: 'w2222222-2222-2222-2222-222222222124',
        tenantId: 'tenant-evergreen',
        bookingId: 'b0000000-0000-0000-0000-000000000124',
        bookingNumber: 'BOOK-000124',
        eventId: 'e4444444-4444-4444-4444-444444444444',
        eventName: 'Corporate Annual Gala',
        eventDate: '2026-08-31T09:00:00',
        venueName: 'Austin Convention Center',
        customerId: 'c3333333-3333-3333-3333-333333333333',
        customerName: 'TechCorp Inc',
        orderNumber: 'WH-000124',
        scheduledDate: '2026-08-31T07:00:00',
        priority: 'URGENT',
        status: 'PICKING',
        customerFacingStatus: 'PREPARING',
        assignedTo: 'Sarah Miller',
        notes: 'Corporate event chair shortage scenario.',
        totalQuantityRequired: 100,
        totalQuantityPicked: 95,
        totalQuantityPacked: 0,
        pickingProgressPercentage: 95.0,
        items: [
          {
            id: 'item-124',
            warehouseOrderId: 'w2222222-2222-2222-2222-222222222124',
            bookingItemId: 'bitem-124',
            productId: 'p-1',
            productNameSnapshot: 'Chiavari Chair',
            skuSnapshot: 'CHI-001',
            locationSnapshot: 'A-01-03',
            quantityRequired: 100,
            quantityPicked: 95,
            quantityPacked: 0,
            status: 'SHORT',
            notes: '5 chairs unavailable in aisle A-01-03.'
          }
        ]
      }
    ];
  }

  private getMockDashboard(): WarehouseDashboard {
    const orders = this.getMockOrders();
    return {
      todaysOrders: 12,
      readyToPick: 4,
      picking: 3,
      packing: 2,
      readyForDelivery: 3,
      shortItems: 2,
      totalActiveWorkOrders: 12,
      ordersInPicking: 3,
      ordersInPacking: 2,
      readyForDeliveryOrders: 3,
      shortageOrdersCount: 2,
      todaysPriorities: orders,
      urgentOrders: orders,
      recentActivity: [
        { id: '1', action: 'WAREHOUSE_ORDER_CREATED', performedBy: 'System', timestamp: new Date().toISOString(), details: 'Created Work Order WH-000123' },
        { id: '2', action: 'PICKING_STARTED', performedBy: 'Warehouse Operator', timestamp: new Date().toISOString(), details: 'Started picking for WH-000123' },
        { id: '3', action: 'SHORTAGE_REPORTED', performedBy: 'Warehouse Operator', timestamp: new Date().toISOString(), details: 'Reported shortage of 5 Chiavari Chairs' }
      ]
    };
  }

  private getMockLocations(): WarehouseLocation[] {
    return [
      { id: 'l1', tenantId: 'tenant-evergreen', code: 'A-01-01', name: 'Aisle A, Rack 01, Shelf 01', active: true },
      { id: 'l2', tenantId: 'tenant-evergreen', code: 'A-01-02', name: 'Aisle A, Rack 01, Shelf 02', active: true },
      { id: 'l3', tenantId: 'tenant-evergreen', code: 'A-01-03', name: 'Aisle A, Rack 01, Shelf 03', active: true },
      { id: 'l4', tenantId: 'tenant-evergreen', code: 'B-01-01', name: 'Aisle B, Rack 01, Shelf 01', active: true },
      { id: 'l5', tenantId: 'tenant-evergreen', code: 'B-02-01', name: 'Aisle B, Rack 02, Shelf 01', active: true },
      { id: 'l6', tenantId: 'tenant-evergreen', code: 'C-01-01', name: 'Aisle C, Rack 01, Shelf 01', active: true }
    ];
  }
}
