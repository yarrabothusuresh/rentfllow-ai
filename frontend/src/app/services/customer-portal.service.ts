import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import {
  CustomerAuthResponse,
  CustomerPortalDashboard,
  CustomerPortalEvent,
  CustomerPortalQuote,
  CustomerPortalBooking,
  CustomerPortalInvoice,
  CustomerProfile,
  CustomerRequest
} from '../models/customer-portal.models';

export interface CustomerAddress {
  id?: string;
  customerId?: string;
  addressType: 'HOME' | 'OFFICE' | 'VENUE' | 'OTHER';
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  zipCode: string;
  country?: string;
  isDefault?: boolean;
  deliveryInstructions?: string;
  contactPerson?: string;
  phone?: string;
}

export interface CustomerMessageItem {
  id: string;
  senderType: string;
  senderId?: string;
  message: string;
  createdAt: string;
}

export interface CustomerConversation {
  id: string;
  customerId: string;
  bookingId?: string;
  bookingNumber?: string;
  quoteId?: string;
  subject: string;
  status: 'OPEN' | 'WAITING_FOR_CUSTOMER' | 'WAITING_FOR_STAFF' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
  messages: CustomerMessageItem[];
}

@Injectable({
  providedIn: 'root'
})
export class CustomerPortalService {
  private apiUrl = '/api/portal';
  private currentCustomerSubject = new BehaviorSubject<CustomerAuthResponse | null>(this.getStoredCustomer());
  public currentCustomer$ = this.currentCustomerSubject.asObservable();

  constructor(private http: HttpClient) {}

  private getStoredCustomer(): CustomerAuthResponse | null {
    const data = localStorage.getItem('rentflow_customer_session');
    if (data) {
      try { return JSON.parse(data); } catch { return null; }
    }
    return null;
  }

  private getHeaders(): HttpHeaders {
    const session = this.currentCustomerSubject.value;
    let headers = new HttpHeaders();
    if (session?.token) {
      headers = headers.set('Authorization', `Bearer ${session.token}`);
    }
    return headers;
  }

  register(data: any): Observable<CustomerAuthResponse> {
    return this.http.post<CustomerAuthResponse>(`${this.apiUrl}/auth/register`, data).pipe(
      tap(res => {
        localStorage.setItem('rentflow_customer_session', JSON.stringify(res));
        this.currentCustomerSubject.next(res);
      })
    );
  }

  login(credentials: { email: string; password: string }): Observable<CustomerAuthResponse> {
    return this.http.post<CustomerAuthResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap(res => {
        localStorage.setItem('rentflow_customer_session', JSON.stringify(res));
        this.currentCustomerSubject.next(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('rentflow_customer_session');
    this.currentCustomerSubject.next(null);
  }

  getCurrentCustomer(): CustomerAuthResponse | null {
    return this.currentCustomerSubject.value;
  }

  getDashboard(): Observable<CustomerPortalDashboard> {
    return this.http.get<CustomerPortalDashboard>(`${this.apiUrl}/dashboard`, { headers: this.getHeaders() });
  }

  getProfile(): Observable<CustomerProfile> {
    return this.http.get<CustomerProfile>(`${this.apiUrl}/profile`, { headers: this.getHeaders() });
  }

  updateProfile(profile: Partial<CustomerProfile>): Observable<CustomerProfile> {
    return this.http.put<CustomerProfile>(`${this.apiUrl}/profile`, profile, { headers: this.getHeaders() });
  }

  getEvents(): Observable<CustomerPortalEvent[]> {
    return this.http.get<CustomerPortalEvent[]>(`${this.apiUrl}/events`, { headers: this.getHeaders() });
  }

  getEventDetail(id: string): Observable<CustomerPortalEvent> {
    return this.http.get<CustomerPortalEvent>(`${this.apiUrl}/events/${id}`, { headers: this.getHeaders() });
  }

  getQuotes(): Observable<CustomerPortalQuote[]> {
    return this.http.get<CustomerPortalQuote[]>(`${this.apiUrl}/quotes`, { headers: this.getHeaders() });
  }

  getQuoteDetail(id: string): Observable<CustomerPortalQuote> {
    return this.http.get<CustomerPortalQuote>(`${this.apiUrl}/quotes/${id}`, { headers: this.getHeaders() });
  }

  approveQuote(id: string): Observable<CustomerPortalQuote> {
    return this.http.post<CustomerPortalQuote>(`${this.apiUrl}/quotes/${id}/approve`, {}, { headers: this.getHeaders() });
  }

  acceptQuote(id: string): Observable<CustomerPortalQuote> {
    return this.approveQuote(id);
  }

  declineQuote(id: string, reason: string): Observable<CustomerPortalQuote> {
    return this.http.post<CustomerPortalQuote>(`${this.apiUrl}/quotes/${id}/decline`, { reason }, { headers: this.getHeaders() });
  }

  requestQuoteChanges(id: string, message: string): Observable<CustomerPortalQuote> {
    return this.http.post<CustomerPortalQuote>(`${this.apiUrl}/quotes/${id}/request-changes`, { message }, { headers: this.getHeaders() });
  }

  getBookings(): Observable<CustomerPortalBooking[]> {
    return this.http.get<CustomerPortalBooking[]>(`${this.apiUrl}/bookings`, { headers: this.getHeaders() });
  }

  getBookingDetail(id: string): Observable<CustomerPortalBooking> {
    return this.http.get<CustomerPortalBooking>(`${this.apiUrl}/bookings/${id}`, { headers: this.getHeaders() });
  }

  getInvoices(): Observable<CustomerPortalInvoice[]> {
    return this.http.get<CustomerPortalInvoice[]>(`${this.apiUrl}/invoices`, { headers: this.getHeaders() });
  }

  getInvoiceDetail(id: string): Observable<CustomerPortalInvoice> {
    return this.http.get<CustomerPortalInvoice>(`${this.apiUrl}/invoices/${id}`, { headers: this.getHeaders() });
  }

  getPayments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/payments`, { headers: this.getHeaders() });
  }

  getInvoicePayments(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/${id}/payments`, { headers: this.getHeaders() });
  }

  getClaims(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/claims`, { headers: this.getHeaders() });
  }

  getClaimDetail(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/claims/${id}`, { headers: this.getHeaders() });
  }

  approveClaim(id: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/claims/${id}/approve`, {}, { headers: this.getHeaders() });
  }

  disputeClaim(id: string, reason: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/claims/${id}/dispute`, { reason }, { headers: this.getHeaders() });
  }

  getMessages(): Observable<CustomerConversation[]> {
    return this.http.get<CustomerConversation[]>(`${this.apiUrl}/messages`, { headers: this.getHeaders() });
  }

  sendMessage(messageReq: { conversationId?: string; bookingId?: string; quoteId?: string; subject?: string; message: string }): Observable<CustomerConversation> {
    return this.http.post<CustomerConversation>(`${this.apiUrl}/messages`, messageReq, { headers: this.getHeaders() });
  }

  getAddresses(): Observable<CustomerAddress[]> {
    return this.http.get<CustomerAddress[]>(`${this.apiUrl}/addresses`, { headers: this.getHeaders() });
  }

  createAddress(address: CustomerAddress): Observable<CustomerAddress> {
    return this.http.post<CustomerAddress>(`${this.apiUrl}/addresses`, address, { headers: this.getHeaders() });
  }

  updateAddress(id: string, address: Partial<CustomerAddress>): Observable<CustomerAddress> {
    return this.http.patch<CustomerAddress>(`${this.apiUrl}/addresses/${id}`, address, { headers: this.getHeaders() });
  }

  deleteAddress(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/addresses/${id}`, { headers: this.getHeaders() });
  }

  getRequests(): Observable<CustomerRequest[]> {
    return this.http.get<CustomerRequest[]>(`${this.apiUrl}/requests`, { headers: this.getHeaders() });
  }

  createRequest(request: { type: string; subject: string; message: string; quoteId?: string; bookingId?: string }): Observable<CustomerRequest> {
    return this.http.post<CustomerRequest>(`${this.apiUrl}/requests`, request, { headers: this.getHeaders() });
  }
}
