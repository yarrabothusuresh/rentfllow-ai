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
    return {
      token: 'demo-portal-token',
      userId: '66666666-6666-6666-6666-666666666666',
      customerId: '33333333-3333-3333-3333-333333333333',
      tenantId: '99999999-9999-9999-9999-999999999999',
      email: 'customer@abcevents.demo',
      customerName: 'Emily Brown',
      companyName: 'ABC Events LLC',
      role: 'CUSTOMER'
    };
  }

  private getHeaders(): HttpHeaders {
    const session = this.currentCustomerSubject.value;
    let headers = new HttpHeaders();
    if (session) {
      headers = headers
        .set('X-Tenant-Id', session.tenantId)
        .set('X-Customer-Id', session.customerId)
        .set('X-User-Role', 'CUSTOMER');
    } else {
      headers = headers
        .set('X-Tenant-Id', '99999999-9999-9999-9999-999999999999')
        .set('X-Customer-Id', '33333333-3333-3333-3333-333333333333')
        .set('X-User-Role', 'CUSTOMER');
    }
    return headers;
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

  acceptQuote(id: string): Observable<CustomerPortalQuote> {
    return this.http.post<CustomerPortalQuote>(`${this.apiUrl}/quotes/${id}/accept`, {}, { headers: this.getHeaders() });
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

  getInvoicePayments(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/invoices/${id}/payments`, { headers: this.getHeaders() });
  }

  getRequests(): Observable<CustomerRequest[]> {
    return this.http.get<CustomerRequest[]>(`${this.apiUrl}/requests`, { headers: this.getHeaders() });
  }

  createRequest(request: { type: string; subject: string; message: string; quoteId?: string; bookingId?: string }): Observable<CustomerRequest> {
    return this.http.post<CustomerRequest>(`${this.apiUrl}/requests`, request, { headers: this.getHeaders() });
  }
}
