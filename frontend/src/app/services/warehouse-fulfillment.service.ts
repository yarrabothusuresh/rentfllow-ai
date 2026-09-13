import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoleStateService } from './role-state.service';

export interface PickListItem {
  id: string;
  pickListId: string;
  bookingItemId: string;
  productId: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  locationCodeSnapshot: string;
  requiredQuantity: number;
  pickedQuantity: number;
  remainingQuantity: number;
  status: 'PENDING' | 'PARTIAL' | 'PICKED' | 'SHORT' | 'SUBSTITUTED';
  sequenceNumber: number;
  trackingType: 'QUANTITY' | 'SERIALIZED';
  scannedAssetCodes: string[];
  notes?: string;
}

export interface PickList {
  id: string;
  tenantId: string;
  pickListNumber: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  bookingNumber?: string;
  customerName?: string;
  eventName?: string;
  eventDate?: string;
  venueName?: string;
  warehouseId: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'VERIFIED' | 'CANCELLED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  assignedTo?: string;
  startedAt?: string;
  completedAt?: string;
  totalRequiredItems: number;
  totalPickedItems: number;
  progressPercentage: number;
  items: PickListItem[];
}

export interface PackListItem {
  id: string;
  packListId: string;
  bookingItemId: string;
  productId: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  requiredQuantity: number;
  packedQuantity: number;
  remainingQuantity: number;
  status: 'PENDING' | 'PARTIAL' | 'PACKED';
  containerCode?: string;
  isKitPackage: boolean;
  notes?: string;
}

export interface PackList {
  id: string;
  tenantId: string;
  packListNumber: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  bookingNumber?: string;
  customerName?: string;
  eventName?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'VERIFIED' | 'CANCELLED';
  assignedTo?: string;
  startedAt?: string;
  completedAt?: string;
  totalRequiredItems: number;
  totalPackedItems: number;
  progressPercentage: number;
  containerCount: number;
  containers: string[];
  items: PackListItem[];
}

export interface LoadListItem {
  id: string;
  loadListId: string;
  packListItemId?: string;
  productId?: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  containerCodeSnapshot?: string;
  requiredQuantity: number;
  loadedQuantity: number;
  remainingQuantity: number;
  status: 'PENDING' | 'PARTIAL' | 'LOADED';
  loadedAt?: string;
  notes?: string;
}

export interface LoadList {
  id: string;
  tenantId: string;
  loadListNumber: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  bookingNumber?: string;
  customerName?: string;
  eventName?: string;
  eventAddress?: string;
  deliveryId?: string;
  deliveryNumber?: string;
  vehicleId?: string;
  vehicleCodeSnapshot?: string;
  vehicleName?: string;
  driverId?: string;
  driverNameSnapshot?: string;
  status: 'PENDING' | 'LOADING' | 'LOADED' | 'VERIFIED' | 'HANDED_OFF' | 'CANCELLED';
  assignedTo?: string;
  startedAt?: string;
  completedAt?: string;
  driverHandoffAt?: string;
  driverNotes?: string;
  totalItems: number;
  loadedItems: number;
  progressPercentage: number;
  containers: string[];
  estimatedLoadWeightKg: number;
  vehicleCapacityWeightKg: number;
  capacityWarning: boolean;
  capacityWarningMessage?: string;
  items: LoadListItem[];
}

export interface PackingContainer {
  id: string;
  tenantId: string;
  containerCode: string;
  containerType: 'BAG' | 'CASE' | 'CART' | 'PALLET' | 'CRATE' | 'BOX';
  status: 'AVAILABLE' | 'IN_USE' | 'DAMAGED' | 'MAINTENANCE' | 'RETIRED';
  currentWarehouseOrderId?: string;
  notes?: string;
}

export interface WarehouseException {
  id: string;
  tenantId: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  bookingNumber?: string;
  pickListItemId?: string;
  productId?: string;
  productName?: string;
  productSku?: string;
  exceptionType: 'ITEM_NOT_FOUND' | 'INSUFFICIENT_STOCK' | 'DAMAGED_ITEM' | 'WRONG_LOCATION' | 'SUBSTITUTION_REQUIRED' | 'PACKING_ISSUE' | 'CONTAINER_MISMATCH' | 'LOADING_EXCEPTION';
  severity: 'BLOCKING' | 'WARNING';
  status: 'OPEN' | 'RESOLVED' | 'DISMISSED';
  affectedQuantity: number;
  reasonCode?: string;
  description?: string;
  resolutionNotes?: string;
  resolutionType?: 'SUBSTITUTION_APPROVED' | 'PARTIAL_SHIP' | 'EMERGENCY_REPAIR' | 'TRANSFER_REQUESTED' | 'CUSTOMER_CONTACTED' | 'RESOLVED_FOUND' | 'DISMISSED';
  reportedBy: string;
  reportedAt: string;
  resolvedBy?: string;
  resolvedAt?: string;
}

export interface WarehouseSubstitution {
  id: string;
  tenantId: string;
  warehouseOrderId: string;
  warehouseOrderNumber?: string;
  bookingId?: string;
  bookingNumber?: string;
  originalProductId: string;
  originalProductName?: string;
  originalProductSku?: string;
  originalQuantity: number;
  originalPrice?: number;
  replacementProductId: string;
  replacementProductName?: string;
  replacementProductSku?: string;
  replacementQuantity: number;
  replacementPrice?: number;
  priceDifference?: number;
  replacementAvailableStock: number;
  status: 'PROPOSED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  reason?: string;
  proposedBy: string;
  proposedAt: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectionReason?: string;
}

export interface WarehouseChecklist {
  id: string;
  warehouseOrderId: string;
  stage: 'PICK' | 'PACK' | 'LOAD';
  taskDescription: string;
  mandatory: boolean;
  completed: boolean;
  completedBy?: string;
  completedAt?: string;
  notes?: string;
}

export interface WarehouseMetrics {
  totalActiveOrders: number;
  ordersReadyToPick: number;
  ordersPicking: number;
  ordersVerifying: number;
  ordersPacking: number;
  ordersLoading: number;
  ordersHandedOff: number;
  openExceptionsCount: number;
  blockingExceptionsCount: number;
  pendingSubstitutionsCount: number;
  availableContainersCount: number;
  inUseContainersCount: number;
}

export interface MyWorkItem {
  pickLists: PickList[];
  packLists: PackList[];
  loadLists: LoadList[];
  blockingExceptions: WarehouseException[];
  pendingSubstitutions: WarehouseSubstitution[];
}

@Injectable({
  providedIn: 'root'
})
export class WarehouseFulfillmentService {
  private apiUrl = '/api/warehouse';

  constructor(
    private http: HttpClient,
    private roleService: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  // Dashboard & Metrics
  getMetrics(): Observable<WarehouseMetrics> {
    return this.http.get<WarehouseMetrics>(`${this.apiUrl}/metrics`, { headers: this.getHeaders() });
  }

  getMyWork(): Observable<MyWorkItem> {
    return this.http.get<MyWorkItem>(`${this.apiUrl}/my-work`, { headers: this.getHeaders() });
  }

  // Pick Lists
  getPickLists(status?: string): Observable<PickList[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<PickList[]>(`${this.apiUrl}/pick-lists${params}`, { headers: this.getHeaders() });
  }

  getPickList(id: string): Observable<PickList> {
    return this.http.get<PickList>(`${this.apiUrl}/pick-lists/${id}`, { headers: this.getHeaders() });
  }

  generatePickList(orderId: string): Observable<PickList> {
    return this.http.post<PickList>(`${this.apiUrl}/orders/${orderId}/generate-pick-list`, {}, { headers: this.getHeaders() });
  }

  startPickList(id: string): Observable<PickList> {
    return this.http.post<PickList>(`${this.apiUrl}/pick-lists/${id}/start`, {}, { headers: this.getHeaders() });
  }

  scanPickItem(pickListId: string, barcode: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/pick-lists/${pickListId}/scan`, { barcode }, { headers: this.getHeaders() });
  }

  pickQuantity(pickListId: string, itemId: string, quantity: number, notes?: string): Observable<PickList> {
    return this.http.post<PickList>(`${this.apiUrl}/pick-lists/${pickListId}/items/${itemId}/pick-quantity`, { quantity, notes }, { headers: this.getHeaders() });
  }

  completePickList(pickListId: string): Observable<PickList> {
    return this.http.post<PickList>(`${this.apiUrl}/pick-lists/${pickListId}/complete`, {}, { headers: this.getHeaders() });
  }

  verifyPickList(pickListId: string, notes?: string): Observable<PickList> {
    return this.http.post<PickList>(`${this.apiUrl}/pick-lists/${pickListId}/verify`, { notes }, { headers: this.getHeaders() });
  }

  reportShortage(pickListId: string, itemId: string, shortQuantity: number, reasonCode: string, description: string): Observable<WarehouseException> {
    return this.http.post<WarehouseException>(`${this.apiUrl}/pick-lists/${pickListId}/items/${itemId}/report-shortage`, { shortQuantity, reasonCode, description }, { headers: this.getHeaders() });
  }

  reportDamage(pickListId: string, itemId: string, damagedQuantity: number, description: string, assetCode?: string): Observable<WarehouseException> {
    return this.http.post<WarehouseException>(`${this.apiUrl}/pick-lists/${pickListId}/items/${itemId}/report-damage`, { damagedQuantity, description, assetCode }, { headers: this.getHeaders() });
  }

  // Pack Lists
  getPackLists(status?: string): Observable<PackList[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<PackList[]>(`${this.apiUrl}/pack-lists${params}`, { headers: this.getHeaders() });
  }

  getPackList(id: string): Observable<PackList> {
    return this.http.get<PackList>(`${this.apiUrl}/pack-lists/${id}`, { headers: this.getHeaders() });
  }

  generatePackList(orderId: string): Observable<PackList> {
    return this.http.post<PackList>(`${this.apiUrl}/orders/${orderId}/generate-pack-list`, {}, { headers: this.getHeaders() });
  }

  startPacking(packListId: string): Observable<PackList> {
    return this.http.post<PackList>(`${this.apiUrl}/pack-lists/${packListId}/start`, {}, { headers: this.getHeaders() });
  }

  packItem(packListId: string, itemId: string, quantity: number, containerCode?: string, notes?: string): Observable<PackList> {
    return this.http.post<PackList>(`${this.apiUrl}/pack-lists/${packListId}/items/${itemId}/pack`, { quantity, containerCode, notes }, { headers: this.getHeaders() });
  }

  completePackList(packListId: string): Observable<PackList> {
    return this.http.post<PackList>(`${this.apiUrl}/pack-lists/${packListId}/complete`, {}, { headers: this.getHeaders() });
  }

  verifyPackList(packListId: string): Observable<PackList> {
    return this.http.post<PackList>(`${this.apiUrl}/pack-lists/${packListId}/verify`, {}, { headers: this.getHeaders() });
  }

  // Load Lists
  getLoadLists(status?: string): Observable<LoadList[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<LoadList[]>(`${this.apiUrl}/load-lists${params}`, { headers: this.getHeaders() });
  }

  getLoadList(id: string): Observable<LoadList> {
    return this.http.get<LoadList>(`${this.apiUrl}/load-lists/${id}`, { headers: this.getHeaders() });
  }

  generateLoadList(orderId: string, deliveryId?: string, vehicleId?: string, driverId?: string): Observable<LoadList> {
    return this.http.post<LoadList>(`${this.apiUrl}/orders/${orderId}/generate-load-list`, { deliveryId, vehicleId, driverId }, { headers: this.getHeaders() });
  }

  startLoading(loadListId: string): Observable<LoadList> {
    return this.http.post<LoadList>(`${this.apiUrl}/load-lists/${loadListId}/start`, {}, { headers: this.getHeaders() });
  }

  loadItem(loadListId: string, itemId: string, quantity: number, notes?: string): Observable<LoadList> {
    return this.http.post<LoadList>(`${this.apiUrl}/load-lists/${loadListId}/items/${itemId}/load`, { quantity, notes }, { headers: this.getHeaders() });
  }

  verifyLoad(loadListId: string): Observable<LoadList> {
    return this.http.post<LoadList>(`${this.apiUrl}/load-lists/${loadListId}/verify`, {}, { headers: this.getHeaders() });
  }

  driverHandoff(loadListId: string, driverName?: string, driverNotes?: string): Observable<LoadList> {
    return this.http.post<LoadList>(`${this.apiUrl}/load-lists/${loadListId}/driver-handoff`, { driverName, driverNotes }, { headers: this.getHeaders() });
  }

  // Containers
  getContainers(status?: string): Observable<PackingContainer[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<PackingContainer[]>(`${this.apiUrl}/containers${params}`, { headers: this.getHeaders() });
  }

  createContainer(container: Partial<PackingContainer>): Observable<PackingContainer> {
    return this.http.post<PackingContainer>(`${this.apiUrl}/containers`, container, { headers: this.getHeaders() });
  }

  // Exceptions
  getExceptions(status?: string): Observable<WarehouseException[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<WarehouseException[]>(`${this.apiUrl}/exceptions${params}`, { headers: this.getHeaders() });
  }

  createException(request: Partial<WarehouseException>): Observable<WarehouseException> {
    return this.http.post<WarehouseException>(`${this.apiUrl}/exceptions`, request, { headers: this.getHeaders() });
  }

  resolveException(exceptionId: string, resolutionNotes?: string, resolutionType?: string): Observable<WarehouseException> {
    return this.http.post<WarehouseException>(`${this.apiUrl}/exceptions/${exceptionId}/resolve`, { resolutionNotes, resolutionType }, { headers: this.getHeaders() });
  }

  // Substitutions
  getSubstitutions(status?: string): Observable<WarehouseSubstitution[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<WarehouseSubstitution[]>(`${this.apiUrl}/substitutions${params}`, { headers: this.getHeaders() });
  }

  proposeSubstitution(request: any): Observable<WarehouseSubstitution> {
    return this.http.post<WarehouseSubstitution>(`${this.apiUrl}/substitutions/propose`, request, { headers: this.getHeaders() });
  }

  approveSubstitution(subId: string): Observable<WarehouseSubstitution> {
    return this.http.post<WarehouseSubstitution>(`${this.apiUrl}/substitutions/${subId}/approve`, {}, { headers: this.getHeaders() });
  }

  rejectSubstitution(subId: string, reason: string): Observable<WarehouseSubstitution> {
    return this.http.post<WarehouseSubstitution>(`${this.apiUrl}/substitutions/${subId}/reject`, { reason }, { headers: this.getHeaders() });
  }

  // Checklists
  getOrderChecklists(orderId: string): Observable<WarehouseChecklist[]> {
    return this.http.get<WarehouseChecklist[]>(`${this.apiUrl}/orders/${orderId}/checklists`, { headers: this.getHeaders() });
  }

  toggleChecklistItem(checklistId: string, completed: boolean, notes?: string): Observable<WarehouseChecklist> {
    return this.http.post<WarehouseChecklist>(`${this.apiUrl}/checklists/${checklistId}/toggle`, { completed, notes }, { headers: this.getHeaders() });
  }
}
