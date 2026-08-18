import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking } from '../models/booking.models';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  private apiUrl = '/api/bookings';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'OWNER', tenantId: string = '11111111-1111-1111-1111-111111111111'): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
      'X-Tenant-Id': tenantId,
    });
  }

  getBookings(role: string = 'OWNER'): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.apiUrl, { headers: this.getHeaders(role) });
  }

  getBookingById(id: string, role: string = 'OWNER'): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/${id}`, { headers: this.getHeaders(role) });
  }

  createBookingFromQuote(quoteId: string, role: string = 'OWNER'): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/from-quote/${quoteId}`, { confirmation: true }, {
      headers: this.getHeaders(role),
    });
  }

  confirmBooking(bookingId: string, role: string = 'OWNER'): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/${bookingId}/confirm`, {}, {
      headers: this.getHeaders(role),
    });
  }

  cancelBooking(bookingId: string, role: string = 'OWNER'): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/${bookingId}/cancel`, {}, {
      headers: this.getHeaders(role),
    });
  }
}
