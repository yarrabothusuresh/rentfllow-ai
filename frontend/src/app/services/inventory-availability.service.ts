import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoleStateService } from './role-state.service';

export interface AvailabilityResult {
  productId: string;
  productName: string;
  sku: string;
  requestedQuantity: number;
  quantityOwned: number;
  quantityInMaintenance: number;
  quantityDamaged: number;
  quantityLost: number;
  quantityReserved: number;
  availableQuantity: number;
  shortage: number;
  available: boolean;
  startDateTime: string;
  endDateTime: string;
  conflictingReservations?: InventoryReservation[];
}

export interface InventoryReservation {
  id: string;
  tenantId: string;
  productId: string;
  productName?: string;
  eventId?: string;
  eventName?: string;
  bookingId?: string;
  quantity: number;
  startDateTime: string;
  endDateTime: string;
  status: 'PENDING' | 'CONFIRMED' | 'RESERVED' | 'HOLD' | 'RELEASED' | 'CANCELLED' | 'EXPIRED';
  reservationType?: 'BOOKING' | 'HOLD' | 'MAINTENANCE' | 'OTHER';
  createdBy?: string;
  expiresAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface InventoryConflict {
  productId: string;
  productName: string;
  sku: string;
  bookingId?: string;
  bookingNumber?: string;
  eventId?: string;
  eventName?: string;
  eventDate?: string;
  requestedQuantity: number;
  availableQuantity: number;
  shortageQuantity: number;
  priority: string;
  suggestedAlternatives?: {
    productId: string;
    productName: string;
    sku: string;
    availableQuantity: number;
  }[];
}

export interface InventorySummary {
  totalProducts: number;
  totalUnits: number;
  availableUnits: number;
  reservedUnits: number;
  maintenanceUnits: number;
  damagedUnits: number;
  lostUnits: number;
  lowStockProducts: number;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryAvailabilityService {
  private baseUrl = 'http://localhost:8080/api/inventory';

  constructor(
    private http: HttpClient,
    private roleState: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Tenant-Id': 'evergreen',
      'X-User-Role': this.roleState.getCurrentRole() || 'OWNER'
    });
  }

  getDashboardSummary(): Observable<InventorySummary> {
    return this.http.get<InventorySummary>(`${this.baseUrl}/dashboard`, { headers: this.getHeaders() });
  }

  getAvailability(productId: string, quantity: number, startDateTime: string, endDateTime: string): Observable<AvailabilityResult> {
    const params = new HttpParams()
      .set('productId', productId)
      .set('quantity', quantity.toString())
      .set('startDateTime', startDateTime)
      .set('endDateTime', endDateTime);
    return this.http.get<AvailabilityResult>(`${this.baseUrl}/availability`, { headers: this.getHeaders(), params });
  }

  checkBulkAvailability(payload: { startDateTime: string; endDateTime: string; items: { productId: string; quantity: number }[] }): Observable<{ available: boolean; items: any[] }> {
    return this.http.post<{ available: boolean; items: any[] }>(`${this.baseUrl}/availability/check`, payload, { headers: this.getHeaders() });
  }

  getProductAvailabilityTimeline(productId: string, days: number = 7): Observable<AvailabilityResult[]> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<AvailabilityResult[]>(`${this.baseUrl}/products/${productId}/availability`, { headers: this.getHeaders(), params });
  }

  getProductAlternatives(productId: string, quantity: number = 1, startDateTime?: string, endDateTime?: string): Observable<any[]> {
    let params = new HttpParams().set('quantity', quantity.toString());
    if (startDateTime) params = params.set('startDateTime', startDateTime);
    if (endDateTime) params = params.set('endDateTime', endDateTime);
    return this.http.get<any[]>(`${this.baseUrl}/products/${productId}/alternatives`, { headers: this.getHeaders(), params });
  }

  getReservations(status?: string, search?: string): Observable<InventoryReservation[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (search) params = params.set('search', search);
    return this.http.get<InventoryReservation[]>(`${this.baseUrl}/reservations`, { headers: this.getHeaders(), params });
  }

  getReservationById(id: string): Observable<InventoryReservation> {
    return this.http.get<InventoryReservation>(`${this.baseUrl}/reservations/${id}`, { headers: this.getHeaders() });
  }

  createReservation(reservation: Partial<InventoryReservation>): Observable<InventoryReservation> {
    return this.http.post<InventoryReservation>(`${this.baseUrl}/reservations`, reservation, { headers: this.getHeaders() });
  }

  releaseReservation(id: string): Observable<InventoryReservation> {
    return this.http.post<InventoryReservation>(`${this.baseUrl}/reservations/${id}/release`, {}, { headers: this.getHeaders() });
  }

  getConflicts(): Observable<InventoryConflict[]> {
    return this.http.get<InventoryConflict[]>(`${this.baseUrl}/conflicts`, { headers: this.getHeaders() });
  }
}
