import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invoice, InvoiceStatus, CreateInvoiceRequest } from '../models/invoice.models';
import { Payment } from '../models/payment.models';

@Injectable({
  providedIn: 'root',
})
export class InvoiceService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'OWNER', tenantId: string = '99999999-9999-9999-9999-999999999999'): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
      'X-Tenant-Id': tenantId,
    });
  }

  getInvoices(filters?: { status?: InvoiceStatus; customerId?: string; bookingId?: string; search?: string }, role: string = 'OWNER'): Observable<Invoice[]> {
    let params = new HttpParams();
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.customerId) params = params.set('customerId', filters.customerId);
    if (filters?.bookingId) params = params.set('bookingId', filters.bookingId);
    if (filters?.search) params = params.set('search', filters.search);

    return this.http.get<Invoice[]>(`${this.apiUrl}/invoices`, {
      headers: this.getHeaders(role),
      params,
    });
  }

  getInvoiceById(id: string, role: string = 'OWNER'): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/invoices/${id}`, {
      headers: this.getHeaders(role),
    });
  }

  createInvoiceFromBooking(bookingId: string, request?: CreateInvoiceRequest, role: string = 'OWNER'): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/invoices/from-booking/${bookingId}`, request || {}, {
      headers: this.getHeaders(role),
    });
  }

  updateInvoiceStatus(id: string, status: InvoiceStatus, role: string = 'OWNER'): Observable<Invoice> {
    return this.http.patch<Invoice>(`${this.apiUrl}/invoices/${id}/status`, { status }, {
      headers: this.getHeaders(role),
    });
  }

  voidInvoice(id: string, reason?: string, role: string = 'OWNER'): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/invoices/${id}/void`, { reason }, {
      headers: this.getHeaders(role),
    });
  }

  getBookingInvoice(bookingId: string, role: string = 'OWNER'): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/bookings/${bookingId}/invoice`, {
      headers: this.getHeaders(role),
    });
  }

  getInvoicePayments(invoiceId: string, role: string = 'OWNER'): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/invoices/${invoiceId}/payments`, {
      headers: this.getHeaders(role),
    });
  }
}
