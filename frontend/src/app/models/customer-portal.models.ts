export interface CustomerAuthResponse {
  token: string;
  userId: string;
  customerId: string;
  tenantId: string;
  email: string;
  customerName: string;
  companyName: string;
  role: string;
}

export interface CustomerPortalEvent {
  id: string;
  eventName: string;
  eventType: string;
  eventDate: string;
  startTime?: string;
  endTime?: string;
  venueName?: string;
  venueAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  status: string;
  requirements?: string[];
}

export interface CustomerPortalQuoteItem {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  lineSubtotal: number;
}

export interface CustomerPortalQuote {
  id: string;
  quoteNumber: string;
  eventName?: string;
  quoteDate: string;
  validUntil: string;
  rentalStartDateTime?: string;
  rentalEndDateTime?: string;
  subtotal: number;
  discountAmount?: number;
  fees?: number;
  taxAmount?: number;
  totalAmount: number;
  depositRequired?: number;
  status: string;
  notes?: string;
  items?: CustomerPortalQuoteItem[];
}

export interface CustomerPortalBookingItem {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  lineSubtotal: number;
}

export interface CustomerPortalBooking {
  id: string;
  bookingNumber: string;
  eventName?: string;
  bookingDate: string;
  rentalStartDateTime?: string;
  rentalEndDateTime?: string;
  subtotal: number;
  discountAmount?: number;
  fees?: number;
  taxAmount?: number;
  totalAmount: number;
  depositPaid?: number;
  balanceDue: number;
  status: string;
  venueName?: string;
  venueAddress?: string;
  notes?: string;
  items?: CustomerPortalBookingItem[];
}

export interface CustomerPortalInvoice {
  id: string;
  invoiceNumber: string;
  bookingId?: string;
  customerId?: string;
  customerName?: string;
  companyName?: string;
  email?: string;
  phone?: string;
  billingAddress?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  issueDate: string;
  dueDate: string;
  subtotal: number;
  discount?: number;
  fees?: number;
  tax?: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  status: string;
  notes?: string;
  items?: any[];
}

export interface CustomerProfile {
  customerId: string;
  customerNumber?: string;
  firstName: string;
  lastName: string;
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
}

export interface CustomerRequest {
  id: string;
  requestType: 'QUOTE_CHANGE' | 'DELIVERY_QUESTION' | 'BOOKING_QUESTION' | 'BILLING_QUESTION' | 'GENERAL';
  subject: string;
  message: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  quoteId?: string;
  bookingId?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface RecentActivity {
  type: string;
  title: string;
  description: string;
  timestamp: string;
}

export interface CustomerPortalDashboard {
  customerName: string;
  companyName?: string;
  upcomingEvent?: CustomerPortalEvent;
  activeQuotesCount: number;
  activeBookingsCount: number;
  invoicesCount: number;
  outstandingBalance: number;
  recentActivities: RecentActivity[];
}
