import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment, RecordPaymentRequest, BookingFinancialSummary } from '../models/payment.models';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'OWNER', tenantId: string = '99999999-9999-9999-9999-999999999999'): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
      'X-Tenant-Id': tenantId,
    });
  }

  getBookingPayments(bookingId: string, role: string = 'OWNER'): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/bookings/${bookingId}/payments`, {
      headers: this.getHeaders(role),
    });
  }

  recordPayment(bookingId: string, request: RecordPaymentRequest, role: string = 'OWNER'): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/bookings/${bookingId}/payments`, request, {
      headers: this.getHeaders(role),
    });
  }

  getPaymentById(paymentId: string, role: string = 'OWNER'): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/payments/${paymentId}`, {
      headers: this.getHeaders(role),
    });
  }

  voidPayment(paymentId: string, reason?: string, role: string = 'OWNER'): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/payments/${paymentId}/void`, { reason }, {
      headers: this.getHeaders(role),
    });
  }

  getFinancialSummary(bookingId: string, role: string = 'OWNER'): Observable<BookingFinancialSummary> {
    return this.http.get<BookingFinancialSummary>(`${this.apiUrl}/bookings/${bookingId}/financial-summary`, {
      headers: this.getHeaders(role),
    });
  }
}
