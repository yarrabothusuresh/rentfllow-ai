import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AssetDetail,
  AssetScanResult,
  InventoryCycleCount,
  InventoryCycleCountItem,
  InventoryItem,
  InventorySummary2,
  InventoryTransfer,
  StockMovement
} from '../models/inventory.models';

@Injectable({
  providedIn: 'root'
})
export class Inventory2Service {
  private baseUrl = '/api/inventory';

  constructor(private http: HttpClient) {}

  getSummary(): Observable<InventorySummary2> {
    return this.http.get<InventorySummary2>(`${this.baseUrl}/v2/dashboard`);
  }

  getProductAssets(productId: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<any>(`${this.baseUrl}/products/${productId}/assets`, { params });
  }

  getAssetDetail(assetId: string): Observable<AssetDetail> {
    return this.http.get<AssetDetail>(`${this.baseUrl}/assets/${assetId}`);
  }

  scanAsset(code: string): Observable<AssetScanResult> {
    const params = new HttpParams().set('code', code);
    return this.http.get<AssetScanResult>(`${this.baseUrl}/scan`, { params });
  }

  scanToken(token: string): Observable<AssetScanResult> {
    return this.http.get<AssetScanResult>(`${this.baseUrl}/assets/scan/${token}`);
  }

  receiveStock(data: any): Observable<StockMovement[]> {
    return this.http.post<StockMovement[]>(`${this.baseUrl}/receive`, data);
  }

  adjustStock(data: any): Observable<StockMovement> {
    return this.http.post<StockMovement>(`${this.baseUrl}/adjust`, data);
  }

  getMovements(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<any>(`${this.baseUrl}/movements`, { params });
  }

  // Transfers
  getTransfers(): Observable<InventoryTransfer[]> {
    return this.http.get<InventoryTransfer[]>(`${this.baseUrl}/transfers`);
  }

  getTransferById(id: string): Observable<InventoryTransfer> {
    return this.http.get<InventoryTransfer>(`${this.baseUrl}/transfers/${id}`);
  }

  createTransfer(transfer: any): Observable<InventoryTransfer> {
    return this.http.post<InventoryTransfer>(`${this.baseUrl}/transfers`, transfer);
  }

  approveTransfer(id: string): Observable<InventoryTransfer> {
    return this.http.post<InventoryTransfer>(`${this.baseUrl}/transfers/${id}/approve`, {});
  }

  shipTransfer(id: string): Observable<InventoryTransfer> {
    return this.http.post<InventoryTransfer>(`${this.baseUrl}/transfers/${id}/ship`, {});
  }

  receiveTransfer(id: string, items?: any[]): Observable<InventoryTransfer> {
    return this.http.post<InventoryTransfer>(`${this.baseUrl}/transfers/${id}/receive`, items || []);
  }

  // Cycle Counts
  getCycleCounts(): Observable<InventoryCycleCount[]> {
    return this.http.get<InventoryCycleCount[]>(`${this.baseUrl}/counts`);
  }

  getCycleCountById(id: string): Observable<InventoryCycleCount> {
    return this.http.get<InventoryCycleCount>(`${this.baseUrl}/counts/${id}`);
  }

  getCycleCountItems(id: string): Observable<InventoryCycleCountItem[]> {
    return this.http.get<InventoryCycleCountItem[]>(`${this.baseUrl}/counts/${id}/items`);
  }

  createCycleCount(data: any): Observable<InventoryCycleCount> {
    return this.http.post<InventoryCycleCount>(`${this.baseUrl}/counts`, data);
  }

  recordCounts(id: string, items: any[]): Observable<InventoryCycleCount> {
    return this.http.post<InventoryCycleCount>(`${this.baseUrl}/counts/${id}/record`, items);
  }

  completeCycleCount(id: string): Observable<InventoryCycleCount> {
    return this.http.post<InventoryCycleCount>(`${this.baseUrl}/counts/${id}/complete`, {});
  }

  approveCycleCount(id: string): Observable<InventoryCycleCount> {
    return this.http.post<InventoryCycleCount>(`${this.baseUrl}/counts/${id}/approve`, {});
  }

  // Booking Allocation & Checkout Scan
  allocateAssets(bookingId: string, assetIds: string[]): Observable<any> {
    return this.http.post<any>(`/api/bookings/${bookingId}/inventory/allocate`, { assetIds });
  }

  checkoutScan(bookingId: string, assetCode: string): Observable<AssetScanResult> {
    return this.http.post<AssetScanResult>(`/api/bookings/${bookingId}/checkout/scan`, { assetCode });
  }

  // Return Scanning
  returnScan(returnId: string, assetCode: string, condition?: string): Observable<AssetScanResult> {
    return this.http.post<AssetScanResult>(`/api/returns/${returnId}/scan`, { assetCode, condition });
  }
}
