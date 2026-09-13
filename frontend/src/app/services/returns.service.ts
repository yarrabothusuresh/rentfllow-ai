import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoleStateService } from './role-state.service';

export interface ReturnOrderItem {
  id: string;
  tenantId: string;
  returnOrderId: string;
  bookingItemId?: string;
  productId: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  quantityExpected: number;
  quantityReceived: number;
  quantityMissing: number;
  quantityDamaged: number;
  quantityGood: number;
  status: 'PENDING' | 'PARTIAL' | 'RECEIVED' | 'MISSING' | 'DAMAGED' | 'INSPECTED';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ReturnOrder {
  id: string;
  tenantId: string;
  returnNumber: string;
  bookingId: string;
  bookingNumber?: string;
  deliveryId?: string;
  customerId: string;
  customerName?: string;
  eventId?: string;
  eventName?: string;
  status: 'PENDING' | 'SCHEDULED' | 'ASSIGNED' | 'READY_FOR_PICKUP' | 'OUT_FOR_PICKUP' | 'ARRIVED' | 'PICKED_UP' | 'CHECK_IN' | 'INSPECTION' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  scheduledDate?: string;
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  pickupAddressSnapshot?: string;
  driverId?: string;
  driverName?: string;
  vehicleId?: string;
  vehicleNumber?: string;
  notes?: string;
  actualPickupStartTime?: string;
  actualArrivalTime?: string;
  actualPickupTime?: string;
  actualCheckInTime?: string;
  actualInspectionTime?: string;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  items?: ReturnOrderItem[];
  totalExpectedItems?: number;
  totalReceivedItems?: number;
  totalMissingItems?: number;
  totalDamagedItems?: number;
}

export interface ReturnsDashboard {
  todaysPickups: number;
  unassigned: number;
  scheduled: number;
  outForPickup: number;
  pickedUp: number;
  pendingInspection: number;
  missingItems: number;
  damagedItems: number;
  todaysQueue: ReturnOrder[];
}

export interface InspectionDashboard {
  pendingInspection: number;
  goodItems: number;
  damagedItems: number;
  missingItems: number;
  maintenanceRequired: number;
  pendingInspectionList: ReturnOrder[];
}

export interface DamageRecord {
  id: string;
  tenantId: string;
  inspectionId?: string;
  returnItemId?: string;
  returnNumber?: string;
  productId: string;
  productName?: string;
  productSku?: string;
  customerName?: string;
  quantity: number;
  category: 'BROKEN' | 'STAINED' | 'SCRATCHED' | 'MISSING_PART' | 'WATER_DAMAGE' | 'OTHER';
  severity: 'MINOR' | 'MAJOR' | 'CRITICAL';
  description?: string;
  estimatedRepairCost?: number;
  estimatedReplacementCost?: number;
  status: string;
  createdAt?: string;
}

export interface CustomerReturnSummary {
  returnOrderId: string;
  returnNumber: string;
  bookingNumber?: string;
  eventName?: string;
  status: string;
  returnDate?: string;
  completedAt?: string;
  items: {
    productName: string;
    quantityExpected: number;
    quantityReturned: number;
    quantityMissing: number;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class ReturnsService {
  private apiUrl = '/api';

  constructor(
    private http: HttpClient,
    private roleStateService: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders();
  }

  createFromBooking(bookingId: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/from-booking/${bookingId}`, {}, { headers: this.getHeaders() });
  }

  getReturns(filters?: { date?: string; status?: string; driverId?: string; customerId?: string; priority?: string; search?: string }): Observable<ReturnOrder[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.date) params = params.set('date', filters.date);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.driverId) params = params.set('driverId', filters.driverId);
      if (filters.customerId) params = params.set('customerId', filters.customerId);
      if (filters.priority) params = params.set('priority', filters.priority);
      if (filters.search) params = params.set('search', filters.search);
    }
    return this.http.get<ReturnOrder[]>(`${this.apiUrl}/returns`, { headers: this.getHeaders(), params });
  }

  getReturnById(id: string): Observable<ReturnOrder> {
    return this.http.get<ReturnOrder>(`${this.apiUrl}/returns/${id}`, { headers: this.getHeaders() });
  }

  getReturnsDashboard(): Observable<ReturnsDashboard> {
    return this.http.get<ReturnsDashboard>(`${this.apiUrl}/returns/dashboard`, { headers: this.getHeaders() });
  }

  getInspectionDashboard(): Observable<InspectionDashboard> {
    return this.http.get<InspectionDashboard>(`${this.apiUrl}/returns/inspection`, { headers: this.getHeaders() });
  }

  getDamageRecords(): Observable<DamageRecord[]> {
    return this.http.get<DamageRecord[]>(`${this.apiUrl}/inventory/damage`, { headers: this.getHeaders() });
  }

  getCustomerReturnSummary(id: string): Observable<CustomerReturnSummary> {
    return this.http.get<CustomerReturnSummary>(`${this.apiUrl}/returns/${id}/summary`, { headers: this.getHeaders() });
  }

  scheduleReturn(id: string, date: string, startTime: string, endTime: string): Observable<ReturnOrder> {
    return this.http.patch<ReturnOrder>(`${this.apiUrl}/returns/${id}/schedule`, { date, startTime, endTime }, { headers: this.getHeaders() });
  }

  assignDriver(id: string, driverId: string): Observable<ReturnOrder> {
    return this.http.patch<ReturnOrder>(`${this.apiUrl}/returns/${id}/driver`, { driverId }, { headers: this.getHeaders() });
  }

  assignVehicle(id: string, vehicleId: string): Observable<ReturnOrder> {
    return this.http.patch<ReturnOrder>(`${this.apiUrl}/returns/${id}/vehicle`, { vehicleId }, { headers: this.getHeaders() });
  }

  startPickup(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/start`, {}, { headers: this.getHeaders() });
  }

  arrivePickup(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/arrive`, {}, { headers: this.getHeaders() });
  }

  pickupComplete(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/pickup-complete`, {}, { headers: this.getHeaders() });
  }

  startCheckIn(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/start-check-in`, {}, { headers: this.getHeaders() });
  }

  recordCheckIn(id: string, items: { returnItemId: string; quantityReceived: number }[]): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/check-in`, { items }, { headers: this.getHeaders() });
  }

  startInspection(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/start-inspection`, {}, { headers: this.getHeaders() });
  }

  recordInspection(id: string, items: {
    returnItemId: string;
    goodQuantity: number;
    damagedQuantity: number;
    condition?: string;
    notes?: string;
    damageCategory?: string;
    damageSeverity?: string;
    damageDescription?: string;
    estimatedRepairCost?: number;
    estimatedReplacementCost?: number;
  }[]): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/inspection`, { items }, { headers: this.getHeaders() });
  }

  completeReturn(id: string): Observable<ReturnOrder> {
    return this.http.post<ReturnOrder>(`${this.apiUrl}/returns/${id}/complete`, {}, { headers: this.getHeaders() });
  }
}
