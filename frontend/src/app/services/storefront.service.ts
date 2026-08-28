import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PublicProduct {
  id: string;
  name: string;
  sku: string;
  categoryName?: string;
  description?: string;
  rentalPrice: number;
  imageUrl?: string;
  available: boolean;
  availableQuantity: number;
}

export interface PublicAvailabilityResponse {
  productId: string;
  requestedQuantity: number;
  availableQuantity: number;
  available: boolean;
}

export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  lineSubtotal: number;
  startDateTime?: string;
  endDateTime?: string;
  available: boolean;
  availableQuantity: number;
}

export interface Cart {
  cartToken: string;
  items: CartItem[];
  subtotal: number;
  estimatedTax: number;
  estimatedTotal: number;
  expiresAt: string;
  isValid: boolean;
  warnings: string[];
}

export interface TenantStorefront {
  id?: string;
  tenantId: string;
  tenantSlug: string;
  companyName: string;
  logoUrl?: string;
  primaryPhone?: string;
  primaryEmail?: string;
  websiteUrl?: string;
  termsAndConditions?: string;
  currency: string;
  timezone?: string;
}

@Injectable({
  providedIn: 'root'
})
export class StorefrontService {
  private publicUrl = '/api/public';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders().set('X-Tenant-Id', '99999999-9999-9999-9999-999999999999');
    const token = localStorage.getItem('rentflow_cart_token');
    if (token) {
      headers = headers.set('X-Cart-Token', token);
    }
    return headers;
  }

  getStorefrontConfig(tenantSlug: string): Observable<TenantStorefront> {
    return this.http.get<TenantStorefront>(`${this.publicUrl}/storefront/${tenantSlug}`, { headers: this.getHeaders() });
  }

  getCatalog(filters?: { category?: string; minPrice?: number; maxPrice?: number; search?: string; sortBy?: string }): Observable<PublicProduct[]> {
    let params: any = {};
    if (filters) {
      if (filters.category) params.category = filters.category;
      if (filters.minPrice) params.minPrice = filters.minPrice.toString();
      if (filters.maxPrice) params.maxPrice = filters.maxPrice.toString();
      if (filters.search) params.search = filters.search;
      if (filters.sortBy) params.sortBy = filters.sortBy;
    }
    return this.http.get<PublicProduct[]>(`${this.publicUrl}/catalog`, { headers: this.getHeaders(), params });
  }

  getProductDetail(id: string): Observable<PublicProduct> {
    return this.http.get<PublicProduct>(`${this.publicUrl}/catalog/${id}`, { headers: this.getHeaders() });
  }

  checkAvailability(productId: string, startDate?: string, endDate?: string, quantity: number = 1): Observable<PublicAvailabilityResponse> {
    let params: any = { quantity: quantity.toString() };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return this.http.get<PublicAvailabilityResponse>(`${this.publicUrl}/catalog/${productId}/availability`, { headers: this.getHeaders(), params });
  }

  getCart(): Observable<Cart> {
    return this.http.get<Cart>(`${this.publicUrl}/cart`, { headers: this.getHeaders() });
  }

  addToCart(productId: string, quantity: number, startDateTime?: string, endDateTime?: string): Observable<Cart> {
    const body = { productId, quantity, startDateTime, endDateTime };
    return this.http.post<Cart>(`${this.publicUrl}/cart/items`, body, { headers: this.getHeaders() });
  }

  updateCartItem(itemId: string, quantity: number, startDateTime?: string, endDateTime?: string): Observable<Cart> {
    const body = { quantity, startDateTime, endDateTime };
    return this.http.patch<Cart>(`${this.publicUrl}/cart/items/${itemId}`, body, { headers: this.getHeaders() });
  }

  removeFromCart(itemId: string): Observable<Cart> {
    return this.http.delete<Cart>(`${this.publicUrl}/cart/items/${itemId}`, { headers: this.getHeaders() });
  }

  validateCart(): Observable<Cart> {
    return this.http.post<Cart>(`${this.publicUrl}/cart/validate`, {}, { headers: this.getHeaders() });
  }

  submitQuoteRequest(request: any): Observable<any> {
    return this.http.post<any>(`${this.publicUrl}/quote-requests`, request, { headers: this.getHeaders() });
  }

  getStaffCustomerRequestsDashboard(): Observable<any> {
    const headers = new HttpHeaders().set('X-Tenant-Id', '99999999-9999-9999-9999-999999999999').set('X-User-Role', 'OWNER');
    return this.http.get<any>('/api/customer-requests/dashboard', { headers });
  }

  getCustomer360(customerId: string): Observable<any> {
    const headers = new HttpHeaders().set('X-Tenant-Id', '99999999-9999-9999-9999-999999999999').set('X-User-Role', 'OWNER');
    return this.http.get<any>(`/api/customers/${customerId}/360`, { headers });
  }
}
