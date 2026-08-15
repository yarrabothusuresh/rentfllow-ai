import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  Lead,
  Customer,
  EventItem,
  EventRequirement,
  LeadConversionRequest,
  LeadConversionResult,
  LeadStatus
} from '../models/crm.models';
import { RoleStateService } from './role-state.service';

@Injectable({
  providedIn: 'root'
})
export class CrmService {
  private apiUrl = 'http://localhost:8080/api';
  private tenantId = '99999999-9999-9999-9999-999999999999';

  // In-memory fallback mock database for instant frontend rendering if backend isn't running
  private mockLeads: Lead[] = [
    {
      id: 'lead-001',
      tenantId: this.tenantId,
      firstName: 'Emily',
      lastName: 'Brown',
      companyName: 'Brown Wedding',
      email: 'emily.brown@example-demo.com',
      phone: '+1 555-010-1001',
      source: 'WEBSITE',
      eventType: 'WEDDING',
      eventDate: '2026-09-20',
      guestCount: 250,
      venueName: 'Dallas Garden Hall',
      notes: 'Wants Chiavari chairs, round tables, white linens, bistro string lights, and dance floor for 250 guests.',
      status: 'QUALIFIED',
      assignedTo: 'Sarah Miller',
      createdAt: '2026-08-10T09:30:00'
    },
    {
      id: 'lead-002',
      tenantId: this.tenantId,
      firstName: 'Alex',
      lastName: 'Morgan',
      companyName: 'TechCorp Inc',
      email: 'events@techcorp-demo.com',
      phone: '+1 555-019-2044',
      source: 'PARTNER',
      eventType: 'CORPORATE',
      eventDate: '2026-09-22',
      guestCount: 500,
      venueName: 'Austin Convention Center',
      notes: 'Annual employee gala requiring stage platform, audio system, 50 cocktail tables.',
      status: 'NEW',
      assignedTo: 'James Taylor',
      createdAt: '2026-08-11T14:15:00'
    },
    {
      id: 'lead-003',
      tenantId: this.tenantId,
      firstName: 'Sarah',
      lastName: 'Jenkins',
      companyName: 'Jenkins Reception',
      email: 'sjenkins@example-demo.com',
      phone: '+1 555-083-3912',
      source: 'REFERRAL',
      eventType: 'PRIVATE_PARTY',
      eventDate: '2026-09-19',
      guestCount: 150,
      venueName: 'Fort Worth Botanic Garden',
      notes: 'Outdoor anniversary reception.',
      status: 'QUOTE_REQUESTED',
      assignedTo: 'Sarah Miller',
      createdAt: '2026-08-12T11:00:00'
    },
    {
      id: 'lead-004',
      tenantId: this.tenantId,
      firstName: 'Marcus',
      lastName: 'Vance',
      companyName: 'Austin Festival Committee',
      email: 'info@austinfestival-demo.org',
      phone: '+1 555-099-1122',
      source: 'WALK_IN',
      eventType: 'FESTIVAL',
      eventDate: '2026-09-25',
      guestCount: 1000,
      venueName: 'Zilker Park, Austin TX',
      notes: '40x60 Frame tent and outdoor staging.',
      status: 'NEGOTIATION',
      assignedTo: 'James Taylor',
      createdAt: '2026-08-13T16:45:00'
    }
  ];

  private mockCustomers: Customer[] = [
    {
      id: 'customer-001',
      tenantId: this.tenantId,
      customerNumber: 'CUS-000001',
      firstName: 'Fairview',
      lastName: 'Hall Manager',
      companyName: 'Fairview Event Hall',
      email: 'contact@fairviewhall-demo.com',
      phone: '+1 555-042-2811',
      customerType: 'VENUE',
      billingAddress: '100 Fairview Blvd',
      city: 'Arlington',
      state: 'TX',
      zipCode: '76010',
      country: 'USA',
      notes: 'Preferred venue partner with monthly recurring corporate rentals.',
      status: 'ACTIVE',
      eventsCount: 1,
      createdAt: '2026-08-01T10:00:00'
    }
  ];

  private mockEvents: EventItem[] = [
    {
      id: 'event-001',
      tenantId: this.tenantId,
      customerId: 'customer-001',
      customerName: 'Fairview Hall Manager',
      eventName: 'Fairview Autumn Gala',
      eventType: 'CORPORATE',
      eventDate: '2026-09-21',
      startTime: '18:00',
      endTime: '23:00',
      guestCount: 120,
      venueName: 'Fairview Event Hall',
      venueAddress: '100 Fairview Blvd',
      city: 'Arlington',
      state: 'TX',
      specialInstructions: 'Deliver to loading bay B by 2 PM.',
      status: 'BOOKED',
      createdAt: '2026-08-01T11:00:00'
    }
  ];

  private mockRequirements: { [eventId: string]: EventRequirement[] } = {
    'event-001': [
      { id: 'req-1', tenantId: this.tenantId, eventId: 'event-001', description: 'Chiavari Chairs', quantity: 120, notes: 'Black chiavari chairs' },
      { id: 'req-2', tenantId: this.tenantId, eventId: 'event-001', description: 'Round Tables', quantity: 12, notes: '60-inch round tables' }
    ]
  };

  constructor(
    private http: HttpClient,
    private roleStateService: RoleStateService
  ) {}

  private getHeaders(): HttpHeaders {
    const role = this.roleStateService.getCurrentRole();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-Tenant-Id': this.tenantId,
      'X-User-Role': role
    });
  }

  // LEADS API
  getLeads(query?: string, status?: string): Observable<Lead[]> {
    let params: any = {};
    if (query) params.query = query;
    if (status) params.status = status;

    return this.http.get<Lead[]>(`${this.apiUrl}/leads`, { headers: this.getHeaders(), params }).pipe(
      catchError(() => {
        let res = [...this.mockLeads];
        if (status) res = res.filter(l => l.status === status);
        if (query) {
          const q = query.toLowerCase();
          res = res.filter(l =>
            l.firstName.toLowerCase().includes(q) ||
            (l.lastName && l.lastName.toLowerCase().includes(q)) ||
            l.email.toLowerCase().includes(q) ||
            (l.companyName && l.companyName.toLowerCase().includes(q))
          );
        }
        return of(res);
      })
    );
  }

  getLeadById(id: string): Observable<Lead> {
    return this.http.get<Lead>(`${this.apiUrl}/leads/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const found = this.mockLeads.find(l => l.id === id);
        if (found) return of(found);
        return throwError(() => new Error('Lead not found'));
      })
    );
  }

  createLead(lead: Lead): Observable<Lead> {
    return this.http.post<Lead>(`${this.apiUrl}/leads`, lead, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const newLead: Lead = {
          ...lead,
          id: 'lead-' + Date.now(),
          tenantId: this.tenantId,
          status: lead.status || 'NEW',
          createdAt: new Date().toISOString()
        };
        this.mockLeads.unshift(newLead);
        return of(newLead);
      })
    );
  }

  updateLead(id: string, lead: Lead): Observable<Lead> {
    return this.http.put<Lead>(`${this.apiUrl}/leads/${id}`, lead, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const idx = this.mockLeads.findIndex(l => l.id === id);
        if (idx !== -1) {
          this.mockLeads[idx] = { ...this.mockLeads[idx], ...lead };
          return of(this.mockLeads[idx]);
        }
        return throwError(() => new Error('Lead not found'));
      })
    );
  }

  updateLeadStatus(id: string, status: LeadStatus): Observable<Lead> {
    return this.http.patch<Lead>(`${this.apiUrl}/leads/${id}/status`, { status }, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const found = this.mockLeads.find(l => l.id === id);
        if (found) {
          found.status = status;
          return of(found);
        }
        return throwError(() => new Error('Lead not found'));
      })
    );
  }

  convertLead(id: string, req?: LeadConversionRequest): Observable<LeadConversionResult> {
    return this.http.post<LeadConversionResult>(`${this.apiUrl}/leads/${id}/convert`, req || {}, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const lead = this.mockLeads.find(l => l.id === id);
        if (!lead) return throwError(() => new Error('Lead not found'));

        // Check duplicate mock
        const existingCust = this.mockCustomers.find(c => c.email.toLowerCase() === lead.email.toLowerCase());
        if (existingCust && (!req || (!req.forceNewCustomer && !req.useExistingCustomerId))) {
          return of({
            leadId: lead.id!,
            status: 'DUPLICATE_FOUND',
            message: 'An existing customer with this email was found.',
            possibleDuplicateFound: true,
            duplicateCustomer: existingCust
          });
        }

        let custId: string;
        let custNum: string;

        if (req && req.useExistingCustomerId) {
          const found = this.mockCustomers.find(c => c.id === req.useExistingCustomerId);
          custId = found ? found.id! : 'customer-001';
          custNum = found ? found.customerNumber! : 'CUS-000001';
        } else if (existingCust && (!req || !req.forceNewCustomer)) {
          custId = existingCust.id!;
          custNum = existingCust.customerNumber!;
        } else {
          custNum = `CUS-${String(this.mockCustomers.length + 1).padStart(6, '0')}`;
          const newCust: Customer = {
            id: 'customer-' + Date.now(),
            tenantId: this.tenantId,
            customerNumber: custNum,
            firstName: lead.firstName,
            lastName: lead.lastName,
            companyName: lead.companyName,
            email: lead.email,
            phone: lead.phone,
            customerType: lead.companyName ? 'BUSINESS' : 'INDIVIDUAL',
            city: 'Dallas',
            state: 'TX',
            country: 'USA',
            status: 'ACTIVE',
            eventsCount: 1,
            createdAt: new Date().toISOString()
          };
          this.mockCustomers.unshift(newCust);
          custId = newCust.id!;
        }

        // Create mock event
        const newEvent: EventItem = {
          id: 'event-' + Date.now(),
          tenantId: this.tenantId,
          customerId: custId,
          customerName: lead.firstName + ' ' + (lead.lastName || ''),
          eventName: lead.firstName + "'s " + (lead.eventType || 'Event'),
          eventType: lead.eventType || 'WEDDING',
          eventDate: lead.eventDate || '2026-09-20',
          guestCount: lead.guestCount || 100,
          venueName: lead.venueName || 'Dallas Garden Hall',
          city: 'Dallas',
          state: 'TX',
          status: 'PLANNING',
          createdAt: new Date().toISOString()
        };
        this.mockEvents.unshift(newEvent);

        // Add mock requirements
        const guests = lead.guestCount || 250;
        const tables = Math.ceil(guests / 10);
        this.mockRequirements[newEvent.id!] = [
          { id: 'req-a', tenantId: this.tenantId, eventId: newEvent.id!, description: 'Chiavari Chairs', quantity: guests, notes: 'Gold chiavari chairs' },
          { id: 'req-b', tenantId: this.tenantId, eventId: newEvent.id!, description: 'Round Tables', quantity: tables, notes: '60-inch round folding tables' },
          { id: 'req-c', tenantId: this.tenantId, eventId: newEvent.id!, description: 'White Table Linens', quantity: tables, notes: '120-inch round polyester tablecloths' }
        ];

        lead.status = 'CONVERTED';

        return of({
          leadId: lead.id!,
          customerId: custId,
          eventId: newEvent.id!,
          customerNumber: custNum,
          status: 'SUCCESS',
          message: `Lead converted successfully. Customer (${custNum}) and Event created.`,
          possibleDuplicateFound: false
        });
      })
    );
  }

  deleteLead(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/leads/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        this.mockLeads = this.mockLeads.filter(l => l.id !== id);
        return of(void 0);
      })
    );
  }

  // CUSTOMERS API
  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/customers`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockCustomers))
    );
  }

  searchCustomers(query: string): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/customers/search`, { headers: this.getHeaders(), params: { query } }).pipe(
      catchError(() => {
        const q = query.toLowerCase();
        return of(this.mockCustomers.filter(c =>
          c.firstName.toLowerCase().includes(q) ||
          (c.lastName && c.lastName.toLowerCase().includes(q)) ||
          c.email.toLowerCase().includes(q) ||
          (c.phone && c.phone.includes(q)) ||
          (c.customerNumber && c.customerNumber.toLowerCase().includes(q)) ||
          (c.companyName && c.companyName.toLowerCase().includes(q))
        ));
      })
    );
  }

  getCustomerById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/customers/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const found = this.mockCustomers.find(c => c.id === id);
        if (found) return of(found);
        return throwError(() => new Error('Customer not found'));
      })
    );
  }

  createCustomer(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(`${this.apiUrl}/customers`, customer, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const custNum = `CUS-${String(this.mockCustomers.length + 1).padStart(6, '0')}`;
        const created: Customer = {
          ...customer,
          id: 'customer-' + Date.now(),
          tenantId: this.tenantId,
          customerNumber: custNum,
          status: customer.status || 'ACTIVE',
          eventsCount: 0,
          createdAt: new Date().toISOString()
        };
        this.mockCustomers.unshift(created);
        return of(created);
      })
    );
  }

  updateCustomer(id: string, customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/customers/${id}`, customer, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const idx = this.mockCustomers.findIndex(c => c.id === id);
        if (idx !== -1) {
          this.mockCustomers[idx] = { ...this.mockCustomers[idx], ...customer };
          return of(this.mockCustomers[idx]);
        }
        return throwError(() => new Error('Customer not found'));
      })
    );
  }

  getCustomerEvents(customerId: string): Observable<EventItem[]> {
    return this.http.get<EventItem[]>(`${this.apiUrl}/customers/${customerId}/events`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockEvents.filter(e => e.customerId === customerId)))
    );
  }

  // EVENTS API
  getEvents(): Observable<EventItem[]> {
    return this.http.get<EventItem[]>(`${this.apiUrl}/events`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockEvents))
    );
  }

  getEventById(id: string): Observable<EventItem> {
    return this.http.get<EventItem>(`${this.apiUrl}/events/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const found = this.mockEvents.find(e => e.id === id);
        if (found) return of(found);
        return throwError(() => new Error('Event not found'));
      })
    );
  }

  createEvent(event: EventItem): Observable<EventItem> {
    return this.http.post<EventItem>(`${this.apiUrl}/events`, event, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const created: EventItem = {
          ...event,
          id: 'event-' + Date.now(),
          tenantId: this.tenantId,
          status: event.status || 'PLANNING',
          createdAt: new Date().toISOString()
        };
        this.mockEvents.unshift(created);
        return of(created);
      })
    );
  }

  updateEvent(id: string, event: EventItem): Observable<EventItem> {
    return this.http.put<EventItem>(`${this.apiUrl}/events/${id}`, event, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const idx = this.mockEvents.findIndex(e => e.id === id);
        if (idx !== -1) {
          this.mockEvents[idx] = { ...this.mockEvents[idx], ...event };
          return of(this.mockEvents[idx]);
        }
        return throwError(() => new Error('Event not found'));
      })
    );
  }

  // EVENT REQUIREMENTS API
  getRequirements(eventId: string): Observable<EventRequirement[]> {
    return this.http.get<EventRequirement[]>(`${this.apiUrl}/events/${eventId}/requirements`, { headers: this.getHeaders() }).pipe(
      catchError(() => of(this.mockRequirements[eventId] || []))
    );
  }

  getEventRequirements(eventId: string): Observable<EventRequirement[]> {
    return this.getRequirements(eventId);
  }

  addRequirement(eventId: string, req: EventRequirement): Observable<EventRequirement> {
    return this.http.post<EventRequirement>(`${this.apiUrl}/events/${eventId}/requirements`, req, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        const created: EventRequirement = {
          ...req,
          id: 'req-' + Date.now(),
          tenantId: this.tenantId,
          eventId,
          createdAt: new Date().toISOString()
        };
        if (!this.mockRequirements[eventId]) {
          this.mockRequirements[eventId] = [];
        }
        this.mockRequirements[eventId].push(created);
        return of(created);
      })
    );
  }

  addEventRequirement(eventId: string, req: EventRequirement): Observable<EventRequirement> {
    return this.addRequirement(eventId, req);
  }

  deleteRequirement(eventId: string, requirementId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/events/${eventId}/requirements/${requirementId}`, { headers: this.getHeaders() }).pipe(
      catchError(() => {
        if (this.mockRequirements[eventId]) {
          this.mockRequirements[eventId] = this.mockRequirements[eventId].filter(r => r.id !== requirementId);
        }
        return of(void 0);
      })
    );
  }

  deleteEventRequirement(eventId: string, requirementId: string): Observable<void> {
    return this.deleteRequirement(eventId, requirementId);
  }
}
