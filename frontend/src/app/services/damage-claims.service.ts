import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DamageClaimItem {
  id: string;
  claimId: string;
  returnItemId?: string;
  inspectionId?: string;
  productId: string;
  productNameSnapshot: string;
  skuSnapshot: string;
  quantity: number;
  claimType: string;
  condition: string;
  damageCategory: string;
  severity: string;
  description?: string;
  unitRepairCost: number;
  unitReplacementCost: number;
  estimatedCost: number;
  approvedCost: number;
  finalCost: number;
  resolution?: string;
  notes?: string;
}

export interface ClaimEstimate {
  id: string;
  claimId: string;
  version: number;
  repairCost: number;
  replacementCost: number;
  laborCost: number;
  transportCost: number;
  otherCost: number;
  discount: number;
  tax: number;
  subtotal: number;
  total: number;
  currency: string;
  notes?: string;
  createdBy?: string;
  createdAt?: string;
}

export interface DamageClaim {
  id: string;
  claimNumber: string;
  bookingId: string;
  bookingNumber?: string;
  returnOrderId: string;
  returnNumber?: string;
  customerId: string;
  customerName?: string;
  status: string;
  claimType: string;
  priority: string;
  description?: string;
  reportedAt?: string;
  reportedBy?: string;
  assessedAt?: string;
  assessedBy?: string;
  customerVisible: boolean;
  customerNotes?: string;
  internalNotes?: string;
  disputeReason?: string;
  disputedAt?: string;
  disputedBy?: string;
  waiveReason?: string;
  waivedAt?: string;
  waivedBy?: string;
  estimatedTotalCost: number;
  approvedTotalCost: number;
  finalTotalCost: number;
  currency: string;
  resolution?: string;
  resolutionNotes?: string;
  resolvedAt?: string;
  resolvedBy?: string;
  createdAt?: string;
  updatedAt?: string;
  items: DamageClaimItem[];
  estimates: ClaimEstimate[];
}

export interface ClaimDashboard {
  openClaims: number;
  underReview: number;
  customerReview: number;
  approved: number;
  disputed: number;
  repairInProgress: number;
  replacementRequired: number;
  resolved: number;
  estimatedExposure: number;
  approvedTotal: number;
  resolvedTotal: number;
  damageRate: number;
  missingRate: number;
  currency: string;
  recentClaims: DamageClaim[];
}

export interface RepairOrder {
  id: string;
  repairNumber: string;
  claimId: string;
  claimNumber?: string;
  productId: string;
  productName?: string;
  productSku?: string;
  quantity: number;
  status: string;
  repairType?: string;
  description?: string;
  estimatedCost: number;
  actualCost: number;
  assignedTo?: string;
  startedAt?: string;
  completedAt?: string;
  notes?: string;
  createdAt?: string;
}

export interface RepairDashboard {
  pendingRepairs: number;
  inProgress: number;
  completed: number;
  failed: number;
  estimatedCost: number;
  actualCost: number;
  recentRepairs: RepairOrder[];
}

export interface ReplacementOrder {
  id: string;
  replacementNumber: string;
  claimId: string;
  claimNumber?: string;
  productId: string;
  productName?: string;
  productSku?: string;
  quantity: number;
  status: string;
  unitCost: number;
  totalCost: number;
  reason?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DamageClaimsService {

  private claimsUrl = '/api/damage-claims';
  private repairsUrl = '/api/repairs';
  private replacementsUrl = '/api/replacements';

  constructor(private http: HttpClient) {}

  getClaims(filters?: { search?: string; status?: string; type?: string; customerId?: string; priority?: string }): Observable<DamageClaim[]> {
    let params = new HttpParams();
    if (filters?.search) params = params.set('search', filters.search);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.type) params = params.set('type', filters.type);
    if (filters?.customerId) params = params.set('customerId', filters.customerId);
    if (filters?.priority) params = params.set('priority', filters.priority);
    return this.http.get<DamageClaim[]>(this.claimsUrl, { params });
  }

  getDashboard(): Observable<ClaimDashboard> {
    return this.http.get<ClaimDashboard>(`${this.claimsUrl}/dashboard`);
  }

  getClaimById(id: string): Observable<DamageClaim> {
    return this.http.get<DamageClaim>(`${this.claimsUrl}/${id}`);
  }

  createClaimFromReturn(returnId: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/from-return/${returnId}`, {});
  }

  createEstimate(claimId: string, req: { repairCost: number; replacementCost: number; laborCost: number; transportCost: number; otherCost: number; discount: number; tax: number; notes?: string }): Observable<ClaimEstimate> {
    return this.http.post<ClaimEstimate>(`${this.claimsUrl}/${claimId}/estimate`, req);
  }

  sendToCustomer(claimId: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/${claimId}/send-to-customer`, {});
  }

  customerApprove(claimId: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/${claimId}/customer-approve`, {});
  }

  customerDispute(claimId: string, reason: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/${claimId}/customer-dispute`, { reason });
  }

  waiveClaim(claimId: string, reason: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/${claimId}/waive`, { reason });
  }

  startRepairForClaim(claimId: string, productId: string, quantity: number, description: string, estimatedCost: number): Observable<RepairOrder> {
    return this.http.post<RepairOrder>(`${this.claimsUrl}/${claimId}/start-repair`, { productId, quantity, description, estimatedCost });
  }

  replacementRequiredForClaim(claimId: string, productId: string, quantity: number, reason: string, unitCost: number): Observable<ReplacementOrder> {
    return this.http.post<ReplacementOrder>(`${this.claimsUrl}/${claimId}/replacement-required`, { productId, quantity, reason, unitCost });
  }

  resolveClaim(claimId: string, resolution: string, notes: string): Observable<DamageClaim> {
    return this.http.post<DamageClaim>(`${this.claimsUrl}/${claimId}/resolve`, { resolution, notes });
  }

  getClaimTimeline(claimId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.claimsUrl}/${claimId}/timeline`);
  }

  // Repairs API
  getRepairs(filters?: { search?: string; status?: string; productId?: string }): Observable<RepairOrder[]> {
    let params = new HttpParams();
    if (filters?.search) params = params.set('search', filters.search);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.productId) params = params.set('productId', filters.productId);
    return this.http.get<RepairOrder[]>(this.repairsUrl, { params });
  }

  getRepairDashboard(): Observable<RepairDashboard> {
    return this.http.get<RepairDashboard>(`${this.repairsUrl}/dashboard`);
  }

  getRepairById(id: string): Observable<RepairOrder> {
    return this.http.get<RepairOrder>(`${this.repairsUrl}/${id}`);
  }

  startRepair(id: string): Observable<RepairOrder> {
    return this.http.post<RepairOrder>(`${this.repairsUrl}/${id}/start`, {});
  }

  completeRepair(id: string, quantityRepaired: number, condition: string, actualCost: number, notes: string): Observable<RepairOrder> {
    return this.http.post<RepairOrder>(`${this.repairsUrl}/${id}/complete`, { quantityRepaired, condition, actualCost, notes });
  }

  failRepair(id: string, reason: string): Observable<RepairOrder> {
    return this.http.post<RepairOrder>(`${this.repairsUrl}/${id}/fail`, { reason });
  }

  // Replacements API
  getReplacements(filters?: { search?: string; status?: string }): Observable<ReplacementOrder[]> {
    let params = new HttpParams();
    if (filters?.search) params = params.set('search', filters.search);
    if (filters?.status) params = params.set('status', filters.status);
    return this.http.get<ReplacementOrder[]>(this.replacementsUrl, { params });
  }

  getReplacementById(id: string): Observable<ReplacementOrder> {
    return this.http.get<ReplacementOrder>(`${this.replacementsUrl}/${id}`);
  }

  orderReplacement(id: string): Observable<ReplacementOrder> {
    return this.http.post<ReplacementOrder>(`${this.replacementsUrl}/${id}/order`, {});
  }

  receiveReplacement(id: string): Observable<ReplacementOrder> {
    return this.http.post<ReplacementOrder>(`${this.replacementsUrl}/${id}/receive`, {});
  }

  completeReplacement(id: string): Observable<ReplacementOrder> {
    return this.http.post<ReplacementOrder>(`${this.replacementsUrl}/${id}/complete`, {});
  }
}
