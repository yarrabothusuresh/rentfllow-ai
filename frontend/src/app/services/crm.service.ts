import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  CrmDashboard,
  CrmPipeline,
  Customer,
  CustomerMatch,
  Event,
  EventRequirement,
  Lead,
  LeadActivity,
  LeadConversionResult,
  LeadDetail,
  LeadFollowUp,
  LeadLostReason,
  LeadStage,
  LeadSummary,
  PublicInquiryRequest
} from '../models/crm.models';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class CrmService {
  private readonly baseUrl = '/api/crm';
  private readonly publicUrl = '/api/public/storefront';

  constructor(private http: HttpClient) {}

  // --- Day 26 CRM Dashboard & Pipeline ---

  getDashboard(): Observable<CrmDashboard> {
    return this.http.get<CrmDashboard>(`${this.baseUrl}/dashboard`);
  }

  getPipeline(): Observable<CrmPipeline> {
    return this.http.get<CrmPipeline>(`${this.baseUrl}/pipeline`);
  }

  // --- Day 26 Leads CRUD & Search ---

  getLeads(
    query?: string,
    stage?: LeadStage,
    assignedUserId?: string,
    unassignedOnly = false,
    page = 0,
    size = 20
  ): Observable<PageResponse<LeadSummary>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('unassignedOnly', unassignedOnly.toString());

    if (query) params = params.set('query', query);
    if (stage) params = params.set('stage', stage);
    if (assignedUserId) params = params.set('assignedUserId', assignedUserId);

    return this.http.get<PageResponse<LeadSummary>>(`${this.baseUrl}/leads`, { params });
  }

  getLeadById(id: string): Observable<LeadDetail> {
    return this.http.get<LeadDetail>(`${this.baseUrl}/leads/${id}`);
  }

  createLead(data: Partial<LeadDetail>): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads`, data);
  }

  updateLead(id: string, data: Partial<LeadDetail>): Observable<LeadDetail> {
    return this.http.patch<LeadDetail>(`${this.baseUrl}/leads/${id}`, data);
  }

  // --- Day 26 Lifecycle Actions ---

  assignLead(id: string, assignedSalesUserId?: string, assignedSalesUserName?: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/assign`, {
      assignedSalesUserId,
      assignedSalesUserName
    });
  }

  transitionStage(id: string, targetStage: LeadStage, reason?: string, notes?: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/transition`, {
      targetStage,
      reason,
      notes
    });
  }

  qualifyLead(id: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/qualify`, {});
  }

  disqualifyLead(id: string, reason: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/disqualify`, { reason });
  }

  markWon(id: string, quoteId?: string, bookingId?: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/won`, { quoteId, bookingId });
  }

  markLost(id: string, lostReason: LeadLostReason, lostReasonNotes?: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/lost`, { lostReason, lostReasonNotes });
  }

  reopenLead(id: string, reopenReason: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/reopen`, { reopenReason });
  }

  // --- Day 26 Customer & Quote Integrations ---

  getCustomerMatches(id: string): Observable<CustomerMatch[]> {
    return this.http.get<CustomerMatch[]>(`${this.baseUrl}/leads/${id}/matches`);
  }

  linkCustomer(id: string, customerId: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/link-customer`, { customerId });
  }

  convertCustomer(id: string, forceNew = false): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/convert-customer`, { forceNew });
  }

  createQuoteDraft(id: string): Observable<LeadDetail> {
    return this.http.post<LeadDetail>(`${this.baseUrl}/leads/${id}/quote`, {});
  }

  // --- Day 26 Activities & Timeline ---

  getActivities(id: string): Observable<LeadActivity[]> {
    return this.http.get<LeadActivity[]>(`${this.baseUrl}/leads/${id}/activities`);
  }

  logActivity(id: string, activity: Partial<LeadActivity>): Observable<LeadActivity> {
    return this.http.post<LeadActivity>(`${this.baseUrl}/leads/${id}/activities`, activity);
  }

  // --- Day 26 Follow-Ups ---

  getFollowUps(id: string): Observable<LeadFollowUp[]> {
    return this.http.get<LeadFollowUp[]>(`${this.baseUrl}/leads/${id}/follow-ups`);
  }

  scheduleFollowUp(id: string, followUp: Partial<LeadFollowUp>): Observable<LeadFollowUp> {
    return this.http.post<LeadFollowUp>(`${this.baseUrl}/leads/${id}/follow-ups`, followUp);
  }

  completeFollowUp(followUpId: string): Observable<LeadFollowUp> {
    return this.http.post<LeadFollowUp>(`${this.baseUrl}/follow-ups/${followUpId}/complete`, {});
  }

  cancelFollowUp(followUpId: string): Observable<LeadFollowUp> {
    return this.http.post<LeadFollowUp>(`${this.baseUrl}/follow-ups/${followUpId}/cancel`, {});
  }

  // --- Day 26 Public Storefront Inquiries ---

  submitPublicInquiry(tenantSlug: string, req: PublicInquiryRequest): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(`${this.publicUrl}/${tenantSlug}/inquiries`, req);
  }

  // --- CUSTOMER APIs (for Customer screens) ---

  getCustomers(query?: string): Observable<Customer[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    return this.http.get<any>('/api/customers', { params }).pipe(
      map(res => (Array.isArray(res) ? res : res?.content || []))
    );
  }

  getCustomerById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`/api/customers/${id}`);
  }

  createCustomer(customer: Partial<Customer>): Observable<Customer> {
    return this.http.post<Customer>('/api/customers', customer);
  }

  updateCustomer(id: string, customer: Partial<Customer>): Observable<Customer> {
    return this.http.put<Customer>(`/api/customers/${id}`, customer);
  }

  deleteCustomer(id: string): Observable<void> {
    return this.http.delete<void>(`/api/customers/${id}`);
  }

  // --- EVENT APIs (for Event screens) ---

  getEvents(query?: string): Observable<Event[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    return this.http.get<Event[]>('/api/events', { params });
  }

  getEventById(id: string): Observable<Event> {
    return this.http.get<Event>(`/api/events/${id}`);
  }

  getEventsByCustomer(customerId: string): Observable<Event[]> {
    return this.http.get<Event[]>(`/api/events/customer/${customerId}`);
  }

  getCustomerEvents(customerId: string): Observable<Event[]> {
    return this.getEventsByCustomer(customerId);
  }

  createEvent(event: Partial<Event>): Observable<Event> {
    return this.http.post<Event>('/api/events', event);
  }

  updateEvent(id: string, event: Partial<Event>): Observable<Event> {
    return this.http.put<Event>(`/api/events/${id}`, event);
  }

  deleteEvent(id: string): Observable<void> {
    return this.http.delete<void>(`/api/events/${id}`);
  }

  getEventRequirements(eventId: string): Observable<EventRequirement[]> {
    return this.http.get<EventRequirement[]>(`/api/events/${eventId}/requirements`);
  }

  addEventRequirement(eventId: string, requirement: Partial<EventRequirement>): Observable<EventRequirement> {
    return this.http.post<EventRequirement>(`/api/events/${eventId}/requirements`, requirement);
  }

  deleteEventRequirement(eventId: string, requirementId: string): Observable<void> {
    return this.http.delete<void>(`/api/events/${eventId}/requirements/${requirementId}`);
  }

  // --- LEGACY LEAD APIs ---

  getLegacyLeads(query?: string): Observable<Lead[]> {
    let params = new HttpParams();
    if (query) params = params.set('query', query);
    return this.http.get<Lead[]>('/api/leads', { params });
  }

  convertLead(id: string, request: any): Observable<LeadConversionResult> {
    return this.http.post<LeadConversionResult>(`/api/leads/${id}/convert`, request);
  }
}
