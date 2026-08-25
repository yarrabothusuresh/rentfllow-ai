import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoleStateService } from './role-state.service';

export interface Delivery {
  id: string;
  tenantId: string;
  deliveryNumber: string;
  bookingId: string;
  bookingNumber?: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  customerId: string;
  customerName?: string;
  eventId: string;
  eventName?: string;
  deliveryType: 'DELIVERY' | 'PICKUP' | 'INTERNAL_TRANSFER';
  status: 'PENDING' | 'SCHEDULED' | 'ASSIGNED' | 'READY' | 'OUT_FOR_DELIVERY' | 'ARRIVED' | 'SETUP_IN_PROGRESS' | 'DELIVERED' | 'CANCELLED' | 'FAILED';
  customerFacingStatus?: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  scheduledDate?: string;
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  deliveryAddressSnapshot?: string;
  latitude?: number;
  longitude?: number;
  driverId?: string;
  driverName?: string;
  vehicleId?: string;
  vehicleNumber?: string;
  sequenceNumber?: number;
  estimatedDistance?: number;
  estimatedDuration?: number;
  actualStartTime?: string;
  actualArrivalTime?: string;
  actualCompletionTime?: string;
  notes?: string;
  customerNotes?: string;
  setupRequired: boolean;
  setupDurationMinutes?: number;
  failureReason?: string;
  failureNotes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Driver {
  id: string;
  tenantId: string;
  userId?: string;
  name: string;
  phone?: string;
  licenseNumber?: string;
  status: 'AVAILABLE' | 'ASSIGNED' | 'ON_DELIVERY' | 'OFF_DUTY';
  active: boolean;
}

export interface Vehicle {
  id: string;
  tenantId: string;
  vehicleNumber: string;
  name: string;
  type?: string;
  capacity?: number;
  status: 'AVAILABLE' | 'ASSIGNED' | 'IN_USE' | 'MAINTENANCE';
  active: boolean;
}

export interface DeliveryRouteStop {
  id: string;
  routeId: string;
  deliveryId: string;
  deliveryNumber?: string;
  sequenceNumber: number;
  estimatedArrivalTime?: string;
  estimatedDepartureTime?: string;
  distanceFromPreviousKm?: number;
  status: string;
}

export interface DeliveryRoute {
  id: string;
  tenantId: string;
  routeName: string;
  date: string;
  driverId?: string;
  driverName?: string;
  vehicleId?: string;
  vehicleNumber?: string;
  status: 'DRAFT' | 'OPTIMIZED' | 'IN_PROGRESS' | 'COMPLETED';
  totalStops: number;
  totalDistanceKm: number;
  totalDurationMinutes: number;
  stops: DeliveryRouteStop[];
  createdAt?: string;
}

export interface DeliveryDashboard {
  todaysDeliveries: number;
  outForDelivery: number;
  completedToday: number;
  failedDeliveries: number;
  unassignedDeliveries: number;
  activeDriversCount: number;
  activeVehiclesCount: number;
  deliveries: Delivery[];
}

@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  private baseUrl = '/api';

  constructor(
    private http: HttpClient,
    private roleStateService: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': this.roleStateService.getCurrentRole(),
      'X-User-Name': 'System Dispatcher'
    });
  }

  getDashboard(date?: string): Observable<DeliveryDashboard> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    return this.http.get<DeliveryDashboard>(`${this.baseUrl}/deliveries/dashboard`, {
      headers: this.getHeaders(),
      params
    });
  }

  getDeliveries(date?: string, status?: string, driverId?: string, vehicleId?: string): Observable<Delivery[]> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    if (status) params = params.set('status', status);
    if (driverId) params = params.set('driverId', driverId);
    if (vehicleId) params = params.set('vehicleId', vehicleId);

    return this.http.get<Delivery[]>(`${this.baseUrl}/deliveries`, {
      headers: this.getHeaders(),
      params
    });
  }

  getDeliveryById(id: string): Observable<Delivery> {
    return this.http.get<Delivery>(`${this.baseUrl}/deliveries/${id}`, {
      headers: this.getHeaders()
    });
  }

  createFromWarehouseOrder(warehouseOrderId: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/from-warehouse-order/${warehouseOrderId}`, {}, {
      headers: this.getHeaders()
    });
  }

  scheduleDelivery(id: string, date: string, startTime: string, endTime: string): Observable<Delivery> {
    return this.http.patch<Delivery>(`${this.baseUrl}/deliveries/${id}/schedule`, {
      date,
      startTime,
      endTime
    }, { headers: this.getHeaders() });
  }

  assignDriver(id: string, driverId: string): Observable<Delivery> {
    return this.http.patch<Delivery>(`${this.baseUrl}/deliveries/${id}/driver`, { driverId }, {
      headers: this.getHeaders()
    });
  }

  assignVehicle(id: string, vehicleId: string): Observable<Delivery> {
    return this.http.patch<Delivery>(`${this.baseUrl}/deliveries/${id}/vehicle`, { vehicleId }, {
      headers: this.getHeaders()
    });
  }

  startDelivery(id: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/${id}/start`, {}, {
      headers: this.getHeaders()
    });
  }

  arriveDelivery(id: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/${id}/arrive`, {}, {
      headers: this.getHeaders()
    });
  }

  startSetup(id: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/${id}/start-setup`, {}, {
      headers: this.getHeaders()
    });
  }

  completeDelivery(id: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/${id}/complete`, {}, {
      headers: this.getHeaders()
    });
  }

  failDelivery(id: string, failureReason: string, failureNotes?: string): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.baseUrl}/deliveries/${id}/fail`, {
      failureReason,
      failureNotes
    }, { headers: this.getHeaders() });
  }

  getDrivers(): Observable<Driver[]> {
    return this.http.get<Driver[]>(`${this.baseUrl}/drivers`, {
      headers: this.getHeaders()
    });
  }

  createDriver(name: string, phone?: string, licenseNumber?: string): Observable<Driver> {
    return this.http.post<Driver>(`${this.baseUrl}/drivers`, {
      name,
      phone,
      licenseNumber
    }, { headers: this.getHeaders() });
  }

  getVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(`${this.baseUrl}/vehicles`, {
      headers: this.getHeaders()
    });
  }

  createVehicle(vehicleNumber: string, name: string, type?: string, capacity?: number): Observable<Vehicle> {
    return this.http.post<Vehicle>(`${this.baseUrl}/vehicles`, {
      vehicleNumber,
      name,
      type,
      capacity
    }, { headers: this.getHeaders() });
  }

  getRoutes(date?: string): Observable<DeliveryRoute[]> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    return this.http.get<DeliveryRoute[]>(`${this.baseUrl}/delivery-routes`, {
      headers: this.getHeaders(),
      params
    });
  }

  createRoute(date: string, driverId?: string, vehicleId?: string, deliveryIds?: string[]): Observable<DeliveryRoute> {
    return this.http.post<DeliveryRoute>(`${this.baseUrl}/delivery-routes`, {
      date,
      driverId,
      vehicleId,
      deliveryIds
    }, { headers: this.getHeaders() });
  }

  updateRouteSequence(routeId: string, deliveryIds: string[]): Observable<DeliveryRoute> {
    return this.http.patch<DeliveryRoute>(`${this.baseUrl}/delivery-routes/${routeId}/sequence`, {
      deliveryIds
    }, { headers: this.getHeaders() });
  }
}
