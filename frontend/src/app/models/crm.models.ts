export type LeadSource = 'WEBSITE' | 'PHONE' | 'EMAIL' | 'REFERRAL' | 'SOCIAL_MEDIA' | 'WALK_IN' | 'PARTNER' | 'OTHER';

export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'QUOTE_REQUESTED' | 'QUOTE_SENT' | 'NEGOTIATION' | 'CONVERTED' | 'LOST';

export type CustomerType = 'INDIVIDUAL' | 'BUSINESS' | 'VENUE' | 'EVENT_PLANNER' | 'CORPORATE' | 'NONPROFIT' | 'OTHER';

export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'BLOCKED';

export type EventType = 'WEDDING' | 'BIRTHDAY' | 'CORPORATE' | 'CONFERENCE' | 'FESTIVAL' | 'GRADUATION' | 'BABY_SHOWER' | 'PRIVATE_PARTY' | 'OTHER';

export type EventStatus = 'PLANNING' | 'QUOTED' | 'BOOKED' | 'PREPARING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Lead {
  id?: string;
  tenantId?: string;
  firstName: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phone?: string;
  source?: LeadSource;
  eventType?: EventType;
  eventDate?: string;
  guestCount?: number;
  venueName?: string;
  notes?: string;
  status?: LeadStatus;
  assignedTo?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Customer {
  id?: string;
  tenantId?: string;
  customerNumber?: string;
  firstName: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phone?: string;
  alternatePhone?: string;
  customerType?: CustomerType;
  billingAddress?: string;
  shippingAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  notes?: string;
  status?: CustomerStatus;
  eventsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface EventItem {
  id?: string;
  tenantId?: string;
  customerId: string;
  customerName?: string;
  eventName: string;
  eventType?: EventType;
  eventDate: string;
  startTime?: string;
  endTime?: string;
  guestCount?: number;
  venueName?: string;
  venueAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  specialInstructions?: string;
  status?: EventStatus;
  createdAt?: string;
  updatedAt?: string;
}

export type Event = EventItem;

export interface EventRequirement {
  id?: string;
  tenantId?: string;
  eventId?: string;
  productId?: string;
  description: string;
  quantity: number;
  notes?: string;
  createdAt?: string;
}

export interface LeadConversionRequest {
  useExistingCustomerId?: string;
  forceNewCustomer?: boolean;
}

export interface LeadConversionResult {
  leadId: string;
  customerId?: string;
  eventId?: string;
  customerNumber?: string;
  status: string;
  message: string;
  possibleDuplicateFound: boolean;
  duplicateCustomer?: Customer;
}
