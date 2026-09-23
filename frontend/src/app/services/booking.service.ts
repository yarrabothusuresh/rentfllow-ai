import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Booking } from '../models/booking.models';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  private apiUrl = '/api/bookings';

  constructor(private http: HttpClient) {}

  private getHeaders(_role?: string, _tenantId?: string): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
    });
  }

  getBookings(role: string = 'OWNER'): Observable<Booking[]> {
    return this.http.get<any>(this.apiUrl, { headers: this.getHeaders(role) }).pipe(
      map(res => (Array.isArray(res) ? res : res?.content || []))
    );
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
