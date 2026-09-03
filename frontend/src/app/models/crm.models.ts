// --- DAY 26 CRM MODELS & TYPES ---

export type LeadSource =
  | 'STOREFRONT_REQUEST'
  | 'WEBSITE_INQUIRY'
  | 'PHONE'
  | 'EMAIL'
  | 'WALK_IN'
  | 'REFERRAL'
  | 'SOCIAL'
  | 'MANUAL'
  | 'OTHER'
  | 'WEBSITE'
  | 'SOCIAL_MEDIA'
  | 'PARTNER';

export type LeadStage =
  | 'NEW'
  | 'CONTACTED'
  | 'NEEDS_DISCOVERY'
  | 'QUALIFIED'
  | 'QUOTE_PREPARED'
  | 'QUOTE_SENT'
  | 'FOLLOW_UP'
  | 'NEGOTIATION'
  | 'WON'
  | 'LOST'
  | 'DISQUALIFIED';

export type LeadPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export type LeadLostReason =
  | 'PRICE'
  | 'AVAILABILITY'
  | 'DATE_CHANGED'
  | 'COMPETITOR'
  | 'NO_RESPONSE'
  | 'EVENT_CANCELLED'
  | 'NOT_A_FIT'
  | 'DUPLICATE'
  | 'CUSTOMER_CANCELLED'
  | 'OTHER';

export type ActivityType =
  | 'NOTE'
  | 'CALL'
  | 'EMAIL'
  | 'SMS'
  | 'MEETING'
  | 'STATUS_CHANGE'
  | 'ASSIGNMENT'
  | 'FOLLOW_UP_CREATED'
  | 'FOLLOW_UP_COMPLETED'
  | 'RENTAL_REQUEST_LINKED'
  | 'CUSTOMER_LINKED'
  | 'CUSTOMER_CREATED'
  | 'QUOTE_CREATED'
  | 'QUOTE_SENT'
  | 'WON'
  | 'LOST'
  | 'REOPENED';

export type ActivityDirection = 'INBOUND' | 'OUTBOUND' | 'INTERNAL';

export type CallOutcome = 'CONNECTED' | 'NO_ANSWER' | 'VOICEMAIL' | 'FOLLOW_UP_REQUIRED';

export type FollowUpType = 'CALL' | 'EMAIL' | 'SMS' | 'MEETING' | 'QUOTE' | 'GENERAL' | 'OTHER';

export type FollowUpStatus = 'OPEN' | 'COMPLETED' | 'CANCELLED' | 'OVERDUE';

export type EventType =
  | 'WEDDING'
  | 'BIRTHDAY'
  | 'CORPORATE'
  | 'CONFERENCE'
  | 'FESTIVAL'
  | 'GRADUATION'
  | 'BABY_SHOWER'
  | 'PRIVATE_PARTY'
  | 'OTHER';

export interface LeadSummary {
  id: string;
  leadNumber: string;
  source: LeadSource;
  stage: LeadStage;
  priority: LeadPriority;
  contactName: string;
  companyName?: string;
  email: string;
  phone?: string;
  eventName?: string;
  eventType?: EventType;
  eventDate?: string;
  estimatedValue?: number;
  assignedSalesUserId?: string;
  assignedSalesUserName?: string;
  nextFollowUpAt?: string;
  lastContactedAt?: string;
  createdAt: string;
  overdueFollowUp?: boolean;
}

export interface LeadDetail {
  id: string;
  tenantId?: string;
  leadNumber?: string;
  source?: LeadSource;
  stage?: LeadStage;
  priority?: LeadPriority;
  firstName?: string;
  lastName?: string;
  contactName?: string;
  companyName?: string;
  email: string;
  phone?: string;
  preferredContactMethod?: string;
  eventName?: string;
  eventType?: EventType;
  eventDate?: string;
  rentalStartDate?: string;
  rentalEndDate?: string;
  venueName?: string;
  venueAddressSnapshot?: string;
  guestCount?: number;
  estimatedBudget?: number;
  estimatedValue?: number;
  customerNotes?: string;
  internalNotes?: string;
  assignedSalesUserId?: string;
  assignedSalesUserName?: string;
  rentalRequestId?: string;
  customerId?: string;
  customerName?: string;
  quoteId?: string;
  quoteNumber?: string;
  bookingId?: string;
  bookingNumber?: string;
  lostReason?: LeadLostReason;
  lostReasonNotes?: string;
  reopenReason?: string;
  nextFollowUpAt?: string;
  lastContactedAt?: string;
  qualifiedAt?: string;
  convertedAt?: string;
  wonAt?: string;
  lostAt?: string;
  reopenedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  status?: LeadStatus;
}

export interface LeadActivity {
  id?: string;
  leadId: string;
  type: ActivityType;
  direction: ActivityDirection;
  subject: string;
  summary?: string;
  notes?: string;
  callOutcome?: CallOutcome;
  referenceType?: string;
  referenceId?: string;
  occurredAt?: string;
  createdBy?: string;
  createdAt?: string;
}

export interface LeadFollowUp {
  id: string;
  leadId: string;
  leadNumber?: string;
  contactName?: string;
  assignedTo?: string;
  assignedToName?: string;
  type: FollowUpType;
  title: string;
  notes?: string;
  dueAt: string;
  status: FollowUpStatus;
  overdue?: boolean;
  completedAt?: string;
  completedBy?: string;
  createdAt?: string;
}

export interface CrmDashboard {
  newLeads: number;
  unassignedLeads: number;
  myActiveLeads: number;
  contactedLeads: number;
  qualifiedLeads: number;
  quotePreparedLeads: number;
  quoteSentLeads: number;
  followUpLeads: number;
  wonLeads: number;
  lostLeads: number;
  totalActiveLeads: number;
  followUpsDueToday: number;
  overdueFollowUps: number;
  upcomingFollowUps: number;
  unassignedQueue: LeadSummary[];
  priorityFollowUps: LeadFollowUp[];
  recentLeads: LeadSummary[];
}

export interface CrmPipelineColumn {
  stage: LeadStage;
  stageName: string;
  count: number;
  totalValue: number;
  leads: LeadSummary[];
}

export interface CrmPipeline {
  columns: CrmPipelineColumn[];
  totalLeads: number;
  totalPipelineValue: number;
}

export interface CustomerMatch {
  customerId: string;
  customerNumber: string;
  name: string;
  email: string;
  phone?: string;
  companyName?: string;
  matchType: string;
}

export interface PublicInquiryRequest {
  name: string;
  company?: string;
  email: string;
  phone?: string;
  eventType?: EventType;
  eventDate?: string;
  message: string;
  honeypot?: string;
}

// --- LEGACY & CONVENIENCE MODELS FOR EXISTING SCREENS ---

export type CustomerType = 'INDIVIDUAL' | 'CORPORATE' | 'EVENT_PLANNER' | 'NON_PROFIT' | 'OTHER';
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'VIP' | 'BLOCKED';

export interface Customer {
  id?: string;
  tenantId?: string;
  customerNumber?: string;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phone?: string;
  alternatePhone?: string;
  billingAddress?: string;
  shippingAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  customerType?: CustomerType;
  status?: CustomerStatus;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type LeadStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'QUALIFIED'
  | 'QUOTE_REQUESTED'
  | 'QUOTE_SENT'
  | 'NEGOTIATION'
  | 'CONVERTED'
  | 'LOST'
  | 'DISQUALIFIED';

export interface Lead {
  id?: string;
  tenantId?: string;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email: string;
  phone?: string;
  source?: LeadSource;
  eventType?: EventType;
  eventDate?: string;
  rentalStartDate?: string;
  rentalEndDate?: string;
  venueName?: string;
  guestCount?: number;
  estimatedBudget?: number;
  notes?: string;
  status?: LeadStatus;
  assignedTo?: string;
  customerId?: string;
  quoteId?: string;
  bookingId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeadConversionResult {
  customerId?: string;
  customerNumber?: string;
  eventId?: string;
  quoteId?: string;
  message?: string;
  status?: string;
  possibleDuplicateFound?: boolean;
  duplicateCustomer?: Customer;
}

export type EventStatus =
  | 'INQUIRY'
  | 'PLANNING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface EventItem {
  id?: string;
  productId?: string;
  productName?: string;
  eventName?: string;
  eventDate?: string;
  quantity?: number;
  unitPrice?: number;
  totalPrice?: number;
}

export interface EventRequirement {
  id?: string;
  eventId?: string;
  description?: string;
  quantity?: number;
  notes?: string;
  productId?: string;
  category?: string;
  estimatedCost?: number;
  status?: string;
  isFulfilled?: boolean;
}

export interface Event {
  id?: string;
  tenantId?: string;
  customerId?: string;
  eventName: string;
  eventType?: EventType;
  eventDate?: string;
  startTime?: string;
  endTime?: string;
  venueName?: string;
  venueAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  guestCount?: number;
  specialInstructions?: string;
  status?: EventStatus;
  createdAt?: string;
  updatedAt?: string;
}
