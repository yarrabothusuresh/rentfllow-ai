import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Quote,
  QuoteItem,
  QuoteStatus,
  QuoteCalculationRequest,
  QuoteCalculationResponse,
} from '../models/quote.models';

@Injectable({
  providedIn: 'root',
})
export class QuoteService {
  private apiUrl = '/api/quotes';

  constructor(private http: HttpClient) {}

  private getHeaders(role: string = 'OWNER', tenantId: string = '11111111-1111-1111-1111-111111111111'): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
      'X-Tenant-Id': tenantId,
    });
  }

  getQuotes(role: string = 'OWNER'): Observable<Quote[]> {
    return this.http.get<Quote[]>(this.apiUrl, { headers: this.getHeaders(role) });
  }

  getQuoteById(id: string, role: string = 'OWNER'): Observable<Quote> {
    return this.http.get<Quote>(`${this.apiUrl}/${id}`, { headers: this.getHeaders(role) });
  }

  createQuote(quote: Quote, role: string = 'OWNER'): Observable<Quote> {
    return this.http.post<Quote>(this.apiUrl, quote, { headers: this.getHeaders(role) });
  }

  updateQuote(id: string, quote: Quote, role: string = 'OWNER'): Observable<Quote> {
    return this.http.put<Quote>(`${this.apiUrl}/${id}`, quote, { headers: this.getHeaders(role) });
  }

  deleteQuote(id: string, role: string = 'OWNER'): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders(role) });
  }

  calculateQuote(id: string, request?: QuoteCalculationRequest): Observable<QuoteCalculationResponse> {
    return this.http.post<QuoteCalculationResponse>(`${this.apiUrl}/${id}/calculate`, request || {}, {
      headers: this.getHeaders(),
    });
  }

  updateStatus(id: string, status: QuoteStatus, role: string = 'OWNER'): Observable<Quote> {
    return this.http.patch<Quote>(`${this.apiUrl}/${id}/status?status=${status}`, {}, {
      headers: this.getHeaders(role),
    });
  }

  duplicateQuote(id: string, role: string = 'OWNER'): Observable<Quote> {
    return this.http.post<Quote>(`${this.apiUrl}/${id}/duplicate`, {}, {
      headers: this.getHeaders(role),
    });
  }

  addQuoteItem(quoteId: string, item: QuoteItem, role: string = 'OWNER'): Observable<Quote> {
    return this.http.post<Quote>(`${this.apiUrl}/${quoteId}/items`, item, {
      headers: this.getHeaders(role),
    });
  }

  updateQuoteItem(quoteId: string, itemId: string, item: QuoteItem, role: string = 'OWNER'): Observable<Quote> {
    return this.http.put<Quote>(`${this.apiUrl}/${quoteId}/items/${itemId}`, item, {
      headers: this.getHeaders(role),
    });
  }

  deleteQuoteItem(quoteId: string, itemId: string, role: string = 'OWNER'): Observable<Quote> {
    return this.http.delete<Quote>(`${this.apiUrl}/${quoteId}/items/${itemId}`, {
      headers: this.getHeaders(role),
    });
  }
}
